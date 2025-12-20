package com.example.AppNotiDo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.web.config.EnableSpringDataWebSupport;

@SpringBootApplication
@EnableSpringDataWebSupport(pageSerializationMode = EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO)
public class AppNotiDoApplication {
    public static void main(String[] args) {
        SpringApplication.run(AppNotiDoApplication.class, args);
    }
}
