/**
 * Copyright 2024 Kiselev Oleg
 */
package com.example.wordstatistic.globalstatistic.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;

/**
 * @author Kiselev Oleg
 */
@OpenAPIDefinition(
    info = @Info(
        title = "wordStatistic",
        description = "a service for get word count statistic", version = "1.0.0",
        contact = @Contact(
            name = "Kiselev Oleg"
        //email = "email@email.com",
        //url = "contact information is unavailable"
        )
    )
)
@SecurityScheme(
    name = "JWT",
    type = SecuritySchemeType.HTTP,
    bearerFormat = "JWT",
    scheme = "bearer"
)
public class SwaggerConfig {
}
