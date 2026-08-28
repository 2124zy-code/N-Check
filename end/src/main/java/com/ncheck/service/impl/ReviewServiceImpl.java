package com.ncheck.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ncheck.common.context.UserContext;
import com.ncheck.dto.DailyNoteDTO;
import com.ncheck.entity.TbDailyNote;
import com.ncheck.mapper.TbDailyNoteMapper;
import com.ncheck.mapper.TbEntryMapper;
import com.ncheck.service.ReviewService;
import com.ncheck.vo.EntryVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final TbDailyNoteMapper dailyNoteMapper;
    private final TbEntryMapper entryMapper;

    @Override
    public Map<String, Object> getDailyReview(String dateStr) {
        Long userId = UserContext.getUserId();

        // 1. 查询复盘随笔
        TbDailyNote note = dailyNoteMapper.selectByUserAndDate(userId, dateStr);
        String noteContent = note != null ? note.getNoteContent() : "";

        // 2. 查询当日创建或更新的题目列表
        List<EntryVO> entries = entryMapper.selectEntriesByDate(userId, dateStr);

        Map<String, Object> result = new HashMap<>();
        result.put("date", dateStr);
        result.put("noteContent", noteContent);
        result.put("entries", entries);
        result.put("count", entries.size());

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String saveDailyNote(DailyNoteDTO dto) {
        Long userId = UserContext.getUserId();

        TbDailyNote exist = dailyNoteMapper.selectOne(new LambdaQueryWrapper<TbDailyNote>()
                .eq(TbDailyNote::getUserId, userId)
                .eq(TbDailyNote::getReviewDate, dto.getReviewDate()));

        if (exist != null) {
            exist.setNoteContent(dto.getNoteContent());
            dailyNoteMapper.updateById(exist);
        } else {
            TbDailyNote note = TbDailyNote.builder()
                    .userId(userId)
                    .reviewDate(dto.getReviewDate())
                    .noteContent(dto.getNoteContent())
                    .build();
            dailyNoteMapper.insert(note);
        }

        return dto.getNoteContent();
    }
}
