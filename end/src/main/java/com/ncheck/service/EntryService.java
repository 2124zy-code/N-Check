package com.ncheck.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.ncheck.dto.EntryDTO;
import com.ncheck.dto.EntryQueryDTO;
import com.ncheck.dto.MockDrawDTO;
import com.ncheck.vo.EntryVO;

import java.util.List;

public interface EntryService {

    /**
     * 多条件动态分页查询题目列表
     */
    IPage<EntryVO> getEntryPage(EntryQueryDTO query);

    /**
     * 获取指定题目详情
     */
    EntryVO getEntryById(Long entryId);

    /**
     * 录入新面试题
     */
    EntryVO createEntry(EntryDTO dto);

    /**
     * 编辑更新面试题
     */
    EntryVO updateEntry(Long entryId, EntryDTO dto);

    /**
     * 删除题目
     */
    void deleteEntry(Long entryId);

    /**
     * 快速切换高频星标状态
     */
    EntryVO toggleStar(Long entryId);

    /**
     * 快速更新掌握熟练度状态
     */
    EntryVO updateMasteryStatus(Long entryId, String status);

    /**
     * 模拟面试智能随机抽题
     */
    List<EntryVO> drawMockInterview(MockDrawDTO dto);
}
