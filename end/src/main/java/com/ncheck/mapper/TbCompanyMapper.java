package com.ncheck.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ncheck.entity.TbCompany;
import com.ncheck.vo.CompanyVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TbCompanyMapper extends BaseMapper<TbCompany> {

    /**
     * 查询某用户的企业列表并聚合统计题目数、掌握数
     */
    List<CompanyVO> selectCompanyVOListByUserId(@Param("userId") Long userId, @Param("keyword") String keyword);

    /**
     * 查询指定企业的聚合详情
     */
    CompanyVO selectCompanyVOById(@Param("userId") Long userId, @Param("companyId") Long companyId);
}
