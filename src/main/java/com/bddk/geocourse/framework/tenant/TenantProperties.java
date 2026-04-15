package com.bddk.geocourse.framework.tenant;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@Data
@ConfigurationProperties(prefix = "geocourse.tenant")
public class TenantProperties {

    private boolean enabled = true;

    private String headerName = "X-Tenant-Id";

    private Long defaultTenantId;

    private List<String> ignorePaths = new ArrayList<>();

}

