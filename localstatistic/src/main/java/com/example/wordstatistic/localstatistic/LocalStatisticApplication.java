/**
 * Copyright 2024 Kiselev Oleg
 */
package com.example.wordstatistic.localstatistic;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author Kiselev Oleg
 */
@SpringBootApplication
@SuppressWarnings({"PMD.UseUtilityClass", "PMD.HideUtilityClassConstructor"})
public class LocalStatisticApplication {
    public static void main(String[] args) {
        SpringApplication.run(LocalStatisticApplication.class, args);
    }
}
