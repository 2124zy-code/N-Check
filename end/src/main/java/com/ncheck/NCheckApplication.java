package com.ncheck;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * N-Check 面经与八股算法收纳平台 - 后端启动入口
 */
@SpringBootApplication
@MapperScan("com.ncheck.mapper")
public class NCheckApplication {

    public static void main(String[] args) {
        SpringApplication.run(NCheckApplication.class, args);
        System.out.println("=================================================");
        System.out.println("  N-Check Backend Services Started Successfully! ");
        System.out.println("  API Docs: http://localhost:8080/swagger-ui.html   ");
        System.out.println("=================================================");
    }
}
