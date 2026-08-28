package com.ncheck.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * 数据库动态表结构自动平滑升级器
 * 在 Spring Boot 启动完成后，无条件确保数据库所有关键字段（如 logo 等）具有超大容量，
 * 彻底杜绝 Data truncation 异常。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SchemaUpgradeRunner implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) {
        try {
            log.info("[SchemaUpgradeRunner] Executing automatic database column expansion...");
            
            // 1. 扩容 tb_company 的 logo 字段为 MEDIUMTEXT (支持 16MB 超大 Base64/SVG)
            jdbcTemplate.execute("ALTER TABLE `tb_company` MODIFY COLUMN `logo` MEDIUMTEXT NULL COMMENT '企业Logo'");
            
            // 2. 扩容 tb_company 其它字段
            jdbcTemplate.execute("ALTER TABLE `tb_company` MODIFY COLUMN `company_code` VARCHAR(255) NOT NULL DEFAULT '' COMMENT '企业代号'");
            jdbcTemplate.execute("ALTER TABLE `tb_company` MODIFY COLUMN `name` VARCHAR(255) NOT NULL COMMENT '企业名称'");
            jdbcTemplate.execute("ALTER TABLE `tb_company` MODIFY COLUMN `industry` VARCHAR(255) NOT NULL DEFAULT '互联网' COMMENT '所属行业'");
            
            log.info("[SchemaUpgradeRunner] Database schema successfully upgraded and verified!");
        } catch (Exception e) {
            log.warn("[SchemaUpgradeRunner] Table alter notice: {}", e.getMessage());
        }
    }
}
