package com.ncheck.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ncheck.common.context.UserContext;
import com.ncheck.entity.TbCompany;
import com.ncheck.entity.TbEntry;
import com.ncheck.mapper.TbCompanyMapper;
import com.ncheck.mapper.TbEntryMapper;
import com.ncheck.service.StatsService;
import com.ncheck.vo.CompanyDistributionVO;
import com.ncheck.vo.DashboardStatsVO;
import com.ncheck.vo.RadarDimensionVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {

    private final TbEntryMapper entryMapper;
    private final TbCompanyMapper companyMapper;

    private static final List<String> PALETTE = Arrays.asList(
            "#2563eb", "#4f46e5", "#0284c7", "#059669", "#d97706", "#dc2626", "#8b5cf6", "#06b6d4"
    );

    @Override
    public DashboardStatsVO getDashboardStats() {
        Long userId = UserContext.getUserId();

        Map<String, Object> map = entryMapper.selectUserMasteryStats(userId);
        long totalEntries = map != null && map.get("totalEntries") != null ? ((Number) map.get("totalEntries")).longValue() : 0L;
        long masteredEntries = map != null && map.get("masteredEntries") != null ? ((Number) map.get("masteredEntries")).longValue() : 0L;
        long learningEntries = map != null && map.get("learningEntries") != null ? ((Number) map.get("learningEntries")).longValue() : 0L;
        long unmasteredEntries = map != null && map.get("unmasteredEntries") != null ? ((Number) map.get("unmasteredEntries")).longValue() : 0L;
        long starredEntries = map != null && map.get("starredEntries") != null ? ((Number) map.get("starredEntries")).longValue() : 0L;

        Long totalCompanies = companyMapper.selectCount(new LambdaQueryWrapper<TbCompany>().eq(TbCompany::getUserId, userId));

        int masteryRate = totalEntries > 0 ? (int) Math.round(((double) masteredEntries / totalEntries) * 100) : 0;

        List<RadarDimensionVO> radarStats = getRadarStats();
        List<CompanyDistributionVO> companyDistribution = getCompanyDistribution();

        return DashboardStatsVO.builder()
                .totalEntries(totalEntries)
                .masteredEntries(masteredEntries)
                .learningEntries(learningEntries)
                .unmasteredEntries(unmasteredEntries)
                .starredEntries(starredEntries)
                .totalCompanies(totalCompanies)
                .masteryRate(masteryRate)
                .radarStats(radarStats)
                .companyDistribution(companyDistribution)
                .build();
    }

    @Override
    public List<RadarDimensionVO> getRadarStats() {
        Long userId = UserContext.getUserId();
        List<TbEntry> allEntries = entryMapper.selectList(new LambdaQueryWrapper<TbEntry>()
                .eq(TbEntry::getUserId, userId));

        List<DimensionConfig> configs = Arrays.asList(
                new DimensionConfig("cs_basics", "计算机基础与网络", Arrays.asList("网络", "tcp", "http", "https", "udp", "socket", "ip", "协议", "dns", "操作系统", "进程", "线程", "linux", "epoll", "io", "内存")),
                new DimensionConfig("java_jvm", "Java核心与JVM", Arrays.asList("java", "jvm", "垃圾回收", "gc", "g1", "zgc", "oom", "hashmap", "集合", "类加载", "反射", "源码", "对象", "泛型")),
                new DimensionConfig("juc_concurrent", "JUC并发与锁机制", Arrays.asList("并发", "juc", "线程池", "锁", "synchronized", "aqs", "cas", "volatile", "threadlocal", "reentrantlock", "高并发", "原子")),
                new DimensionConfig("mysql_storage", "MySQL与存储引擎", Arrays.asList("mysql", "数据库", "索引", "b+tree", "事务", "mvcc", "隔离级别", "sql", "慢查询", "explain", "分库分表", "innodb", "行锁")),
                new DimensionConfig("spring_middleware", "Spring与中间件微服务", Arrays.asList("spring", "springboot", "aop", "ioc", "mybatis", "redis", "缓存", "分布式锁", "kafka", "rocketmq", "mq", "消息队列", "微服务", "dubbo", "rpc", "分布式")),
                new DimensionConfig("algorithms", "手撕算法与数据结构", Arrays.asList("算法", "算法题", "双指针", "树", "动态规划", "链表", "二叉树", "滑动窗口", "贪心", "回溯", "排序", "二分", "图", "栈", "堆"))
        );

        List<RadarDimensionVO> result = new ArrayList<>();

        for (DimensionConfig dim : configs) {
            long count = 0;
            long masteredCount = 0;

            for (TbEntry e : allEntries) {
                boolean matched = false;
                if ("algorithms".equals(dim.key) && "算法题".equals(e.getType())) {
                    matched = true;
                } else {
                    String fullText = (e.getTitle() + " " + (e.getTags() != null ? e.getTags() : "")).toLowerCase();
                    for (String kw : dim.keywords) {
                        if (fullText.contains(kw)) {
                            matched = true;
                            break;
                        }
                    }
                }

                if (matched) {
                    count++;
                    if ("已掌握".equals(e.getStatus())) {
                        masteredCount++;
                    }
                }
            }

            int score = 30;
            if (count > 0) {
                double ratio = (double) masteredCount / count;
                score = (int) Math.min(100, Math.round(30 + count * 10 + ratio * 40));
            }

            result.add(RadarDimensionVO.builder()
                    .key(dim.key)
                    .dimension(dim.name)
                    .count(count)
                    .masteredCount(masteredCount)
                    .score(score)
                    .build());
        }

        return result;
    }

    @Override
    public List<CompanyDistributionVO> getCompanyDistribution() {
        Long userId = UserContext.getUserId();
        List<TbCompany> companies = companyMapper.selectList(new LambdaQueryWrapper<TbCompany>()
                .eq(TbCompany::getUserId, userId));

        Long total = entryMapper.selectCount(new LambdaQueryWrapper<TbEntry>().eq(TbEntry::getUserId, userId));
        long totalEntries = total != null && total > 0 ? total : 1L;

        List<CompanyDistributionVO> list = new ArrayList<>();

        for (int i = 0; i < companies.size(); i++) {
            TbCompany c = companies.get(i);
            Long count = entryMapper.selectCount(new LambdaQueryWrapper<TbEntry>()
                    .eq(TbEntry::getUserId, userId)
                    .eq(TbEntry::getCompanyId, c.getId()));
            long cCount = count != null ? count : 0L;
            int percentage = (int) Math.round(((double) cCount / totalEntries) * 100);

            list.add(CompanyDistributionVO.builder()
                    .id(c.getId())
                    .name(c.getName())
                    .count(cCount)
                    .percentage(percentage)
                    .color(PALETTE.get(i % PALETTE.size()))
                    .build());
        }

        list.sort((a, b) -> Long.compare(b.getCount(), a.getCount()));
        return list;
    }

    private static class DimensionConfig {
        String key;
        String name;
        List<String> keywords;

        DimensionConfig(String key, String name, List<String> keywords) {
            this.key = key;
            this.name = name;
            this.keywords = keywords;
        }
    }
}
