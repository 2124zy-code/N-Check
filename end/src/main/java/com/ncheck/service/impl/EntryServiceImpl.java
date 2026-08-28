package com.ncheck.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ncheck.common.context.UserContext;
import com.ncheck.common.exception.BusinessException;
import com.ncheck.common.result.ResultCode;
import com.ncheck.dto.EntryDTO;
import com.ncheck.dto.EntryQueryDTO;
import com.ncheck.dto.MockDrawDTO;
import com.ncheck.entity.TbCompany;
import com.ncheck.entity.TbEntry;
import com.ncheck.mapper.TbCompanyMapper;
import com.ncheck.mapper.TbEntryMapper;
import com.ncheck.service.EntryService;
import com.ncheck.vo.EntryVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EntryServiceImpl implements EntryService {

    private final TbEntryMapper entryMapper;
    private final TbCompanyMapper companyMapper;
    private final ObjectMapper objectMapper;

    @Override
    public IPage<EntryVO> getEntryPage(EntryQueryDTO query) {
        Long userId = UserContext.getUserId();
        Page<EntryVO> page = new Page<>(query.getPageNum(), query.getPageSize());

        IPage<EntryVO> voPage = entryMapper.selectEntryVOPage(page, userId, query);
        populateTagsForVOList(voPage.getRecords(), userId);
        return voPage;
    }

    @Override
    public EntryVO getEntryById(Long entryId) {
        Long userId = UserContext.getUserId();
        EntryVO vo = entryMapper.selectEntryVOById(userId, entryId);
        if (vo == null) {
            throw new BusinessException(ResultCode.ENTRY_NOT_FOUND);
        }

        TbEntry entry = entryMapper.selectById(entryId);
        vo.setTags(parseTagsJson(entry.getTags()));
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public EntryVO createEntry(EntryDTO dto) {
        Long userId = UserContext.getUserId();

        // 校验企业是否属于当前用户
        TbCompany company = companyMapper.selectOne(new LambdaQueryWrapper<TbCompany>()
                .eq(TbCompany::getId, dto.getCompanyId())
                .eq(TbCompany::getUserId, userId));
        if (company == null) {
            throw new BusinessException(ResultCode.COMPANY_NOT_FOUND);
        }

        String tagsJson = serializeTags(dto.getTags());

        TbEntry entry = TbEntry.builder()
                .userId(userId)
                .companyId(dto.getCompanyId())
                .type(dto.getType())
                .title(dto.getTitle())
                .difficulty(StringUtils.hasText(dto.getDifficulty()) ? dto.getDifficulty() : "中等")
                .status(StringUtils.hasText(dto.getStatus()) ? dto.getStatus() : "未掌握")
                .isStarred(dto.getIsStarred() != null ? dto.getIsStarred() : 0)
                .tags(tagsJson)
                .content(dto.getContent() != null ? dto.getContent() : "")
                .build();

        entryMapper.insert(entry);

        EntryVO vo = entryMapper.selectEntryVOById(userId, entry.getId());
        vo.setTags(dto.getTags() != null ? dto.getTags() : Collections.emptyList());
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public EntryVO updateEntry(Long entryId, EntryDTO dto) {
        Long userId = UserContext.getUserId();

        TbEntry exist = entryMapper.selectOne(new LambdaQueryWrapper<TbEntry>()
                .eq(TbEntry::getId, entryId)
                .eq(TbEntry::getUserId, userId));
        if (exist == null) {
            throw new BusinessException(ResultCode.ENTRY_NOT_FOUND);
        }

        if (dto.getCompanyId() != null) {
            exist.setCompanyId(dto.getCompanyId());
        }
        if (StringUtils.hasText(dto.getType())) {
            exist.setType(dto.getType());
        }
        if (StringUtils.hasText(dto.getTitle())) {
            exist.setTitle(dto.getTitle());
        }
        if (StringUtils.hasText(dto.getDifficulty())) {
            exist.setDifficulty(dto.getDifficulty());
        }
        if (StringUtils.hasText(dto.getStatus())) {
            exist.setStatus(dto.getStatus());
        }
        if (dto.getIsStarred() != null) {
            exist.setIsStarred(dto.getIsStarred());
        }
        if (dto.getTags() != null) {
            exist.setTags(serializeTags(dto.getTags()));
        }
        if (dto.getContent() != null) {
            exist.setContent(dto.getContent());
        }

        entryMapper.updateById(exist);

        EntryVO vo = entryMapper.selectEntryVOById(userId, entryId);
        vo.setTags(parseTagsJson(exist.getTags()));
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteEntry(Long entryId) {
        Long userId = UserContext.getUserId();
        int deleted = entryMapper.delete(new LambdaQueryWrapper<TbEntry>()
                .eq(TbEntry::getId, entryId)
                .eq(TbEntry::getUserId, userId));

        if (deleted == 0) {
            throw new BusinessException(ResultCode.ENTRY_NOT_FOUND);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public EntryVO toggleStar(Long entryId) {
        Long userId = UserContext.getUserId();
        TbEntry exist = entryMapper.selectOne(new LambdaQueryWrapper<TbEntry>()
                .eq(TbEntry::getId, entryId)
                .eq(TbEntry::getUserId, userId));
        if (exist == null) {
            throw new BusinessException(ResultCode.ENTRY_NOT_FOUND);
        }

        exist.setIsStarred(exist.getIsStarred() == 1 ? 0 : 1);
        entryMapper.updateById(exist);

        EntryVO vo = entryMapper.selectEntryVOById(userId, entryId);
        vo.setTags(parseTagsJson(exist.getTags()));
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public EntryVO updateMasteryStatus(Long entryId, String status) {
        Long userId = UserContext.getUserId();
        TbEntry exist = entryMapper.selectOne(new LambdaQueryWrapper<TbEntry>()
                .eq(TbEntry::getId, entryId)
                .eq(TbEntry::getUserId, userId));
        if (exist == null) {
            throw new BusinessException(ResultCode.ENTRY_NOT_FOUND);
        }

        exist.setStatus(status);
        exist.setLastReviewedAt(LocalDateTime.now());
        entryMapper.updateById(exist);

        EntryVO vo = entryMapper.selectEntryVOById(userId, entryId);
        vo.setTags(parseTagsJson(exist.getTags()));
        return vo;
    }

    @Override
    public List<EntryVO> drawMockInterview(MockDrawDTO dto) {
        Long userId = UserContext.getUserId();
        int baguLimit = dto.getBaguCount() != null ? dto.getBaguCount() : 3;
        int algoLimit = dto.getAlgoCount() != null ? dto.getAlgoCount() : 1;

        List<EntryVO> baguList = entryMapper.selectRandomEntries(userId, dto.getCompanyId(), "八股文", baguLimit);
        List<EntryVO> algoList = entryMapper.selectRandomEntries(userId, dto.getCompanyId(), "算法题", algoLimit);

        List<EntryVO> result = new ArrayList<>();
        result.addAll(baguList);
        result.addAll(algoList);

        populateTagsForVOList(result, userId);
        return result;
    }

    private void populateTagsForVOList(List<EntryVO> list, Long userId) {
        if (CollectionUtils.isEmpty(list)) return;
        for (EntryVO vo : list) {
            TbEntry entry = entryMapper.selectById(vo.getId());
            if (entry != null) {
                vo.setTags(parseTagsJson(entry.getTags()));
            }
        }
    }

    private String serializeTags(List<String> tags) {
        if (tags == null) return "[]";
        try {
            return objectMapper.writeValueAsString(tags);
        } catch (Exception e) {
            log.error("Failed to serialize tags: {}", e.getMessage());
            return "[]";
        }
    }

    private List<String> parseTagsJson(String json) {
        if (!StringUtils.hasText(json)) return Collections.emptyList();
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            log.error("Failed to parse tags json: {}", e.getMessage());
            return Collections.emptyList();
        }
    }
}
