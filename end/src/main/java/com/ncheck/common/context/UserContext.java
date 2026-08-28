package com.ncheck.common.context;

/**
 * 基于 ThreadLocal 的当前线程用户上下文
 */
public class UserContext {

    private static final ThreadLocal<UserInfo> USER_THREAD_LOCAL = new ThreadLocal<>();

    public static void setUser(UserInfo userInfo) {
        USER_THREAD_LOCAL.set(userInfo);
    }

    public static UserInfo getUser() {
        return USER_THREAD_LOCAL.get();
    }

    public static Long getUserId() {
        UserInfo userInfo = USER_THREAD_LOCAL.get();
        return userInfo != null ? userInfo.getId() : null;
    }

    public static void clear() {
        USER_THREAD_LOCAL.remove();
    }
}
