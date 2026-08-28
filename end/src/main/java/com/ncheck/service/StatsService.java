package com.ncheck.service;

import com.ncheck.vo.CompanyDistributionVO;
import com.ncheck.vo.DashboardStatsVO;
import com.ncheck.vo.RadarDimensionVO;

import java.util.List;

public interface StatsService {

    /**
     * 获取工作台总览聚合统计指标大盘
     */
    DashboardStatsVO getDashboardStats();

    /**
     * 获取六维技术雷达图打分与统计
     */
    List<RadarDimensionVO> getRadarStats();

    /**
     * 获取企业题库收纳占比分布
     */
    List<CompanyDistributionVO> getCompanyDistribution();
}
