package com.ncheck.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.net.URI;

/**
 * 数据源智能自适应配置
 * 自动识别并解析 Railway 等云平台的 MYSQL_URL / DATABASE_URL 环境变量，
 * 彻底解决云端数据库用户名、密码、端口、数据库名不匹配导致的 Access Denied 问题。
 */
@Slf4j
@Configuration
public class DataSourceConfig {

    @Bean
    @Primary
    public DataSource dataSource(DataSourceProperties properties) {
        String rawMysqlUrl = System.getenv("MYSQL_URL");
        if (!StringUtils.hasText(rawMysqlUrl)) {
            rawMysqlUrl = System.getenv("DATABASE_URL");
        }

        HikariConfig config = new HikariConfig();

        if (StringUtils.hasText(rawMysqlUrl) && rawMysqlUrl.startsWith("mysql://")) {
            log.info("Detected Railway cloud MYSQL_URL / DATABASE_URL, parsing cloud connection info...");
            try {
                // 使用 URI 解析 mysql://user:password@host:port/database
                URI uri = new URI(rawMysqlUrl.replace("mysql://", "http://"));
                String host = uri.getHost();
                int port = uri.getPort() == -1 ? 3306 : uri.getPort();
                String path = uri.getPath();
                String dbName = StringUtils.hasText(path) && path.length() > 1 ? path.substring(1) : "ncheck_db";

                String username = properties.getUsername();
                String password = properties.getPassword();

                String userInfo = uri.getUserInfo();
                if (StringUtils.hasText(userInfo) && userInfo.contains(":")) {
                    String[] parts = userInfo.split(":", 2);
                    username = parts[0];
                    password = parts[1];
                }

                String jdbcUrl = String.format(
                        "jdbc:mysql://%s:%d/%s?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true&useSSL=false",
                        host, port, dbName
                );

                log.info("Successfully parsed cloud MySQL: Host={}, Port={}, DB={}, User={}", host, port, dbName, username);
                config.setJdbcUrl(jdbcUrl);
                config.setUsername(username);
                config.setPassword(password);
                config.setDriverClassName("com.mysql.cj.jdbc.Driver");
            } catch (Exception e) {
                log.error("Failed to parse MYSQL_URL, falling back to application properties", e);
                config.setJdbcUrl(properties.getUrl());
                config.setUsername(properties.getUsername());
                config.setPassword(properties.getPassword());
                config.setDriverClassName(properties.getDriverClassName());
            }
        } else {
            // 本地开发环境直接使用 application.yml 的配置
            log.info("Using standard DataSourceProperties: URL={}, User={}", properties.getUrl(), properties.getUsername());
            config.setJdbcUrl(properties.getUrl());
            config.setUsername(properties.getUsername());
            config.setPassword(properties.getPassword());
            config.setDriverClassName(properties.getDriverClassName());
        }

        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setIdleTimeout(30000);
        config.setConnectionTimeout(20000);
        config.setMaxLifetime(1800000);

        return new HikariDataSource(config);
    }
}
