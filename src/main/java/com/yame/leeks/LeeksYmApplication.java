package com.yame.leeks;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
//@EnableScheduling
@MapperScan("com.yame.leeks.mapper.**")
@EnableTransactionManagement
public class LeeksYmApplication {

    public static void main(String[] args) {
        SpringApplication.run(LeeksYmApplication.class, args);
    }

}
