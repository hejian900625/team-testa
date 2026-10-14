package com.yugi.workbench;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 智能体工作台后端启动类。
 */
@SpringBootApplication(scanBasePackages = "com.yugi.workbench")
@MapperScan("com.yugi.workbench.mapper")
public class WorkbenchApplication {

    public static void main(String[] args) {
        SpringApplication.run(WorkbenchApplication.class, args);
    }
}
