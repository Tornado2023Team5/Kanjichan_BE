package com.github.tornado2023team5.kanjichan;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

@SpringBootApplication()
public class KanjichanApplication {

    public static void main(String[] args) {
//        setup();
        SpringApplication.run(KanjichanApplication.class, args);
    }
}
