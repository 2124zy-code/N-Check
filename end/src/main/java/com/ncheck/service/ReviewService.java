package com.ncheck.service;

import com.ncheck.dto.DailyNoteDTO;
import com.ncheck.vo.EntryVO;

import java.util.List;
import java.util.Map;

public interface ReviewService {

    /**
     * 获取指定日期的复盘详情 (含复盘日记随笔与当日攻克的题目列表)
     */
    Map<String, Object> getDailyReview(String dateStr);

    /**
     * 保存/更新指定日期的复盘随笔
     */
    String saveDailyNote(DailyNoteDTO dto);
}
