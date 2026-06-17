package com.fishing.tracker;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.fishing.tracker.mapper")
public class FishingTrackerApplication {

    public static void main(String[] args) {
        SpringApplication.run(FishingTrackerApplication.class, args);
    }
}
