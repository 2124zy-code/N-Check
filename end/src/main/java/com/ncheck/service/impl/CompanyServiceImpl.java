package com.ncheck.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ncheck.common.context.UserContext;
import com.ncheck.common.exception.BusinessException;
import com.ncheck.common.result.ResultCode;
import com.ncheck.dto.CompanyDTO;
import com.ncheck.entity.TbCompany;
import com.ncheck.entity.TbEntry;
import com.ncheck.mapper.TbCompanyMapper;
import com.ncheck.mapper.TbEntryMapper;
import com.ncheck.service.CompanyService;
import com.ncheck.vo.CompanyVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CompanyServiceImpl implements CompanyService {

    private final TbCompanyMapper companyMapper;
    private final TbEntryMapper entryMapper;

    @Override
    public List<CompanyVO> getCompanyList(String keyword) {
        Long userId = UserContext.getUserId();
        return companyMapper.selectCompanyVOListByUserId(userId, keyword);
    }

    @Override
    public CompanyVO getCompanyById(Long companyId) {
        Long userId = UserContext.getUserId();
        CompanyVO vo = companyMapper.selectCompanyVOById(userId, companyId);
        if (vo == null) {
            throw new BusinessException(ResultCode.COMPANY_NOT_FOUND);
        }
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CompanyVO createCompany(CompanyDTO dto) {
        Long userId = UserContext.getUserId();

        String code = StringUtils.hasText(dto.getCompanyCode())
                ? dto.getCompanyCode()
                : "comp-" + System.currentTimeMillis();
        String logo = StringUtils.hasText(dto.getLogo()) ? dto.getLogo() : "default";
        String industry = StringUtils.hasText(dto.getIndustry()) ? dto.getIndustry() : "互联网";

        TbCompany company = TbCompany.builder()
                .userId(userId)
                .companyCode(code)
                .name(dto.getName())
                .logo(logo)
                .industry(industry)
                .build();

        companyMapper.insert(company);

        return companyMapper.selectCompanyVOById(userId, company.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CompanyVO updateCompany(Long companyId, CompanyDTO dto) {
        Long userId = UserContext.getUserId();

        TbCompany exist = companyMapper.selectOne(new LambdaQueryWrapper<TbCompany>()
                .eq(TbCompany::getId, companyId)
                .eq(TbCompany::getUserId, userId));

        if (exist == null) {
            throw new BusinessException(ResultCode.COMPANY_NOT_FOUND);
        }

        if (StringUtils.hasText(dto.getName())) {
            exist.setName(dto.getName());
        }
        if (StringUtils.hasText(dto.getLogo())) {
            exist.setLogo(dto.getLogo());
        }
        if (StringUtils.hasText(dto.getIndustry())) {
            exist.setIndustry(dto.getIndustry());
        }
        if (StringUtils.hasText(dto.getCompanyCode())) {
            exist.setCompanyCode(dto.getCompanyCode());
        }

        companyMapper.updateById(exist);

        return companyMapper.selectCompanyVOById(userId, companyId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCompany(Long companyId) {
        Long userId = UserContext.getUserId();

        // 1. 删除企业
        int deleted = companyMapper.delete(new LambdaQueryWrapper<TbCompany>()
                .eq(TbCompany::getId, companyId)
                .eq(TbCompany::getUserId, userId));

        if (deleted == 0) {
            throw new BusinessException(ResultCode.COMPANY_NOT_FOUND);
        }

        // 2. 级联清理属于该企业的全部题目
        entryMapper.delete(new LambdaQueryWrapper<TbEntry>()
                .eq(TbEntry::getCompanyId, companyId)
                .eq(TbEntry::getUserId, userId));
    }
}
