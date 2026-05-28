package com.example.haksikmokjang;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class HaksikMokjangApplication {

    public static void main(String[] args) {
        SpringApplication.run(HaksikMokjangApplication.class, args);
    }

}
