package com.heima.utils.thread;

import com.heima.model.wemedia.pojos.WmUser;

public class WimThreadLocalUtils {
    private final static ThreadLocal<WmUser> WM_USER_THREAD_LOCAL = new ThreadLocal<>();

    //存入数据
    public static void setUser(WmUser wmUser) {
        WM_USER_THREAD_LOCAL.set(wmUser);
    }

    //获取数据
    public static WmUser getUser() {
        return WM_USER_THREAD_LOCAL.get();
    }

    //删除数据
    public static void clear() {
        WM_USER_THREAD_LOCAL.remove();
    }
}
