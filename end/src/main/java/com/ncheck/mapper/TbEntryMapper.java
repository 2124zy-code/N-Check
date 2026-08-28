package com.ncheck.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ncheck.dto.EntryQueryDTO;
import com.ncheck.entity.TbEntry;
import com.ncheck.vo.EntryVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface TbEntryMapper extends BaseMapper<TbEntry> {

    /**
     * 多条件动态分页检索题目 (含企业名称与Logo关联)
     */
    IPage<EntryVO> selectEntryVOPage(Page<EntryVO> page,
                                     @Param("userId") Long userId,
                                     @Param("query") EntryQueryDTO query);

    /**
     * 查询单条题目详情 (含企业信息)
     */
    EntryVO selectEntryVOById(@Param("userId") Long userId, @Param("entryId") Long entryId);

    /**
     * 智能抽题抽取指定题型
     */
    List<EntryVO> selectRandomEntries(@Param("userId") Long userId,
                                      @Param("companyId") Long companyId,
                                      @Param("type") String type,
                                      @Param("limit") int limit);

    /**
     * 按日期查询当日创建或更新的题目列表
     */
    List<EntryVO> selectEntriesByDate(@Param("userId") Long userId, @Param("dateStr") String dateStr);

    /**
     * 聚合统计用户的题库总数、已掌握数、学习中数、未掌握数、星标数
     */
    Map<String, Object> selectUserMasteryStats(@Param("userId") Long userId);
}
