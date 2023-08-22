package com.github.tornado2023team5.kanjichan;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

@SpringBootApplication
public class KanjichanApplication {

    public static void main(String[] args) {
        setup();
        SpringApplication.run(KanjichanApplication.class, args);
    }

    public static void setup() {
        String jdbcUrl = System.getenv("DOCKER_DATABASE_URL");

        try (Connection conn = DriverManager.getConnection(jdbcUrl)) {

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM mytable");

            while (rs.next()) {
                System.out.println(rs.getString("column_name"));
            }

            rs.close();
            stmt.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
