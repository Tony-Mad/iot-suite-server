package com.tuya.iot.suite.web.Interceptor.connector;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p> TODO
 *
 * @author 哲也
 * @since 2021/5/31
 */
public class MultiConnectorApplication {

    private String baseUrl;

    private static final ConcurrentHashMap<String, ConnectorApplication> map = new ConcurrentHashMap<>();

    public static ConnectorApplication getConnectorApplication(String appName) {
        return map.get(appName);
    }

    public static void addConnectorApplication(String appName, ConnectorApplication connectorApplication) {
        map.put(appName, connectorApplication);
    }
}
