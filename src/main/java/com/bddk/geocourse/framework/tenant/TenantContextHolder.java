package com.bddk.geocourse.framework.tenant;

public final class TenantContextHolder {

    private static final ThreadLocal<Long> TENANT_HOLDER = new ThreadLocal<>();

    private TenantContextHolder() {
    }

    public static Long getTenantId() {
        return TENANT_HOLDER.get();
    }

    public static void setTenantId(Long tenantId) {
        TENANT_HOLDER.set(tenantId);
    }

    public static void clear() {
        TENANT_HOLDER.remove();
    }

}

