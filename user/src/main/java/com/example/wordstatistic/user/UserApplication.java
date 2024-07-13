/**
 * Copyright 2024 Kiselev Oleg
 */
package com.example.wordstatistic.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author Kiselev Oleg
 */
@SpringBootApplication
@SuppressWarnings({"PMD.UseUtilityClass", "PMD.HideUtilityClassConstructor"})
public class UserApplication {
    public static void main(String[] args) {
        SpringApplication.run(UserApplication.class, args);
    }
}
