package com.tuya.iot.suite.web.Interceptor.connector;

/**
 * <p> TODO
 *
 * @author 哲也
 * @since 2021/5/31
 */
public class ConnectorAppContextHolder {

    private static final ThreadLocal<String> holder = new ThreadLocal<>();

    public static void setAppName(String appName) {
        holder.set(appName);
    }

    public static String getAppName() {
        return holder.get();
    }

    public static void clear() {
        holder.remove();
    }
}
