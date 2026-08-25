package com.emcs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class EmcsApplication {

    public static void main(String[] args) {
        SpringApplication.run(EmcsApplication.class, args);
    }
}
