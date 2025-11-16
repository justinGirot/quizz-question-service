package com.quizz.question.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "Question Service API",
        version = "1.0",
        description = "API for managing quiz questions with workflow (draft → pending → validated/rejected/archived)",
        contact = @Contact(
            name = "Quiz Team",
            url = "https://github.com/justinGirot/quizz-question-service"
        )
    ),
    servers = {
        @Server(url = "http://localhost:8080", description = "API Gateway"),
        @Server(url = "http://localhost:8082", description = "Question Service")
    }
)
@SecurityScheme(
    name = "cookieAuth",
    type = SecuritySchemeType.APIKEY,
    in = io.swagger.v3.oas.annotations.enums.SecuritySchemeIn.COOKIE,
    paramName = "auth_token"
)
public class OpenApiConfig {
}
