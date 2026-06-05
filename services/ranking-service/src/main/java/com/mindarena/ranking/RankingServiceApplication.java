package com.mindarena.ranking;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@SpringBootApplication
public class RankingServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(RankingServiceApplication.class, args);
    }
}
