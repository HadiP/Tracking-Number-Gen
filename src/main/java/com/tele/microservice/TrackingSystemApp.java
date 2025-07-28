package com.tele.microservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.tele.microservice.repository")
@EntityScan(basePackages = "com.tele.microservice.entity")
@ComponentScan(basePackages = {
        "com.tele.microservice.mongodb",
        "com.tele.microservice.service",
        "com.tele.microservice.aop",
        "com.tele.microservice.controller"
})
@EnableAsync
public class TrackingSystemApp {

    public static void main(String[] args) {
        SpringApplication.run(TrackingSystemApp.class, args);
    }

}
