package com.ncheck.service;

import com.ncheck.dto.CompanyDTO;
import com.ncheck.vo.CompanyVO;

import java.util.List;

public interface CompanyService {

    /**
     * 获取当前用户的目标企业列表 (含八股/算法/掌握度统计)
     */
    List<CompanyVO> getCompanyList(String keyword);

    /**
     * 获取单家企业详情
     */
    CompanyVO getCompanyById(Long companyId);

    /**
     * 新增目标企业
     */
    CompanyVO createCompany(CompanyDTO dto);

    /**
     * 修改目标企业信息
     */
    CompanyVO updateCompany(Long companyId, CompanyDTO dto);

    /**
     * 删除目标企业 (并级联删除所属题目)
     */
    void deleteCompany(Long companyId);
}
