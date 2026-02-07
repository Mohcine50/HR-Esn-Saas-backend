package com.shegami.hr_saas.config.domain.context;

import com.shegami.hr_saas.shared.model.UserContext;

public class UserContextHolder {


    private static final ThreadLocal<UserContext> context = new ThreadLocal<>();

    public static void setCurrentUserContext(UserContext userContext) {
        context.set(userContext);
    }
    public static UserContext getCurrentUserContext() {
        return context.get();
    }

    public static void clearCurrentUserContext() {
        context.remove();
    }

}
