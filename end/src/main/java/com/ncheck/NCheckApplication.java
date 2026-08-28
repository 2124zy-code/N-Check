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
        // 在 Spring 容器初始化之前，自动从 Railway 注入的 MYSQL_URL / DATABASE_URL 提取真实数据库凭据
        configureCloudDatabaseEnvironment();

        SpringApplication.run(NCheckApplication.class, args);
        System.out.println("=================================================");
        System.out.println("  N-Check Backend Services Started Successfully! ");
        System.out.println("  API Docs: http://localhost:8080/swagger-ui.html   ");
        System.out.println("=================================================");
    }

    private static void configureCloudDatabaseEnvironment() {
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

                    System.setProperty("spring.datasource.url", jdbcUrl);
                    System.setProperty("spring.datasource.username", username);
                    System.setProperty("spring.datasource.password", password);
                    System.setProperty("spring.flyway.url", jdbcUrl);
                    System.setProperty("spring.flyway.user", username);
                    System.setProperty("spring.flyway.password", password);
                    log.info("Successfully configured cloud MySQL for Spring & Flyway: Host={}, Port={}, DB={}, User={}", host, port, dbName, username);
                }
            } catch (Exception e) {
                log.error("Failed to parse cloud MYSQL_URL in main", e);
            }
        }
    }
}
