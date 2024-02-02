package com.heima.utils.thread;

import com.heima.model.user.pojos.ApUser;

public class AppThreadLocalUtils {
    private final static ThreadLocal<ApUser> WM_USER_THREAD_LOCAL = new ThreadLocal<>();

    //存入数据
    public static void setUser(ApUser apUser) {
        WM_USER_THREAD_LOCAL.set(apUser);
    }

    //获取数据
    public static ApUser getUser() {
        return WM_USER_THREAD_LOCAL.get();
    }

    //删除数据
    public static void clear() {
        WM_USER_THREAD_LOCAL.remove();
    }
}
