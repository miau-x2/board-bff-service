package com.example.board.bff.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
public class BoardBffServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(BoardBffServiceApplication.class, args);
    }

}
