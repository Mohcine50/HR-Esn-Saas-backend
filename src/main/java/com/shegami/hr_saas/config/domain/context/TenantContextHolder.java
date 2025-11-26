package com.shegami.hr_saas.config.domain.context;

public class TenantContextHolder {


    private static final ThreadLocal<String> currentTenantId = new ThreadLocal<>();

    public static void setCurrentTenant(String tenantId) {
        currentTenantId.set(tenantId);
    }
    public static String getCurrentTenant() {
        return currentTenantId.get();
    }

    public static void clearCurrentTenant() {
        currentTenantId.remove();
    }

}
