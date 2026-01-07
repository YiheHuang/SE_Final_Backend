package com.javaee.se_final_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class SeFinalBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(SeFinalBackendApplication.class, args);
    }

}
