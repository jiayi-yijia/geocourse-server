package com.bddk.geocourse.framework.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "geocourse.info")
public class GeocourseInfoProperties {

    private String name;

    private String version;

    private String description;

    private String apiPrefix;

}

