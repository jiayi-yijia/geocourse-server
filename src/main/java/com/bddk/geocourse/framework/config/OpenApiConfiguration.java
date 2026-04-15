package com.bddk.geocourse.framework.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class OpenApiConfiguration {

    @Bean
    public OpenAPI geocourseOpenApi(com.bddk.geocourse.framework.config.GeocourseInfoProperties infoProperties) {
        return new OpenAPI().info(new Info()
                .title(infoProperties.getName())
                .version(infoProperties.getVersion())
                .description(infoProperties.getDescription()));
    }

}

