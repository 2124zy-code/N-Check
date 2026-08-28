package com.ncheck;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.util.StringUtils;

import java.net.URI;

@Slf4j
@SpringBootApplication
public class NCheckApplication {

    public static void main(String[] args) {
        // 在 Spring Boot & Flyway 初始化前，全方位自适应提取并注入云端数据库连接凭据
        configureCloudDatabaseEnvironment();

        SpringApplication.run(NCheckApplication.class, args);
        System.out.println("=================================================");
        System.out.println("  N-Check Backend Services Started Successfully! ");
        System.out.println("  API Docs: http://localhost:8080/swagger-ui.html   ");
        System.out.println("=================================================");
    }

    private static void configureCloudDatabaseEnvironment() {
        // 1. 优先尝试解析 MYSQL_URL / DATABASE_URL
        String rawMysqlUrl = System.getenv("MYSQL_URL");
        if (!StringUtils.hasText(rawMysqlUrl)) {
            rawMysqlUrl = System.getenv("DATABASE_URL");
        }

        if (StringUtils.hasText(rawMysqlUrl) && rawMysqlUrl.startsWith("mysql://")) {
            try {
                URI uri = new URI(rawMysqlUrl.replace("mysql://", "http://"));
                String host = uri.getHost();
                int port = uri.getPort() == -1 ? 3306 : uri.getPort();
                String path = uri.getPath();
                String dbName = StringUtils.hasText(path) && path.length() > 1 ? path.substring(1) : "ncheck_db";

                String userInfo = uri.getUserInfo();
                if (StringUtils.hasText(userInfo) && userInfo.contains(":")) {
                    String[] parts = userInfo.split(":", 2);
                    String username = parts[0];
                    String password = parts[1];

                    String jdbcUrl = String.format(
                            "jdbc:mysql://%s:%d/%s?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true&useSSL=false",
                            host, port, dbName
                    );

                    applySystemProperties(jdbcUrl, username, password);
                    log.info("Successfully configured cloud MySQL from MYSQL_URL: Host={}, Port={}, DB={}, User={}", host, port, dbName, username);
                    return;
                }
            } catch (Exception e) {
                log.error("Failed to parse cloud MYSQL_URL", e);
            }
        }

        // 2. 如果没有 MYSQL_URL，尝试从离散环境变量提取 (MYSQLHOST, MYSQLPORT, MYSQL_ROOT_PASSWORD, etc.)
        String host = getEnvAny("MYSQLHOST", "MYSQL_HOST");
        String port = getEnvAny("MYSQLPORT", "MYSQL_PORT", "MYSQL_TCP_PORT");
        String dbName = getEnvAny("MYSQLDATABASE", "MYSQL_DATABASE");
        String username = getEnvAny("MYSQLUSER", "MYSQL_USER");
        String password = getEnvAny("SPRING_DATASOURCE_PASSWORD", "MYSQL_ROOT_PASSWORD", "MYSQLPASSWORD", "MYSQL_PASSWORD");

        if (StringUtils.hasText(host) && StringUtils.hasText(password)) {
            if (!StringUtils.hasText(port)) port = "3306";
            if (!StringUtils.hasText(dbName)) dbName = "ncheck_db";
            if (!StringUtils.hasText(username)) username = "root";

            String jdbcUrl = String.format(
                    "jdbc:mysql://%s:%s/%s?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true&useSSL=false",
                    host, port, dbName
            );

            applySystemProperties(jdbcUrl, username, password);
            log.info("Successfully configured cloud MySQL from discrete envs: Host={}, Port={}, DB={}, User={}", host, port, dbName, username);
        }
    }

    private static void applySystemProperties(String jdbcUrl, String username, String password) {
        System.setProperty("spring.datasource.url", jdbcUrl);
        System.setProperty("spring.datasource.username", username);
        System.setProperty("spring.datasource.password", password);
        System.setProperty("spring.flyway.url", jdbcUrl);
        System.setProperty("spring.flyway.user", username);
        System.setProperty("spring.flyway.password", password);
    }

    private static String getEnvAny(String... keys) {
        for (String key : keys) {
            String val = System.getenv(key);
            if (StringUtils.hasText(val)) {
                return val.trim();
            }
        }
        return null;
    }
}
