package com.tuya.iot.suite.web.Interceptor.connector;

import com.tuya.connector.api.config.ApiDataSource;
import com.tuya.connector.api.config.Configuration;
import com.tuya.connector.api.plugin.ConnectorInterceptor;
import com.tuya.connector.api.plugin.Invocation;
import com.tuya.connector.spring.annotations.Interceptor;
import org.springframework.util.StringUtils;

import java.util.Objects;


/**
 * <p> TODO
 *
 * @author 哲也
 * @since 2021/5/31
 */
@Interceptor
public class ApplicationInterceptor implements ConnectorInterceptor {

    private Configuration configuration;

    public ApplicationInterceptor(Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        // 获取开发者配置的应用名
        String appName = ConnectorAppContextHolder.getAppName();
        if (!StringUtils.isEmpty(appName)) {
            // 获取应用配置
            ConnectorApplication connectorApplication = MultiConnectorApplication.getConnectorApplication(appName);
            if (Objects.nonNull(connectorApplication)) {
                // 切换应用
                ApiDataSource currentApiDataSource = copyApiDataSource(configuration.getApiDataSource());
                currentApiDataSource.setAk(connectorApplication.getAk());
                currentApiDataSource.setSk(connectorApplication.getSk());
                Configuration.setApiDataSourceThreadLocal(currentApiDataSource);
            }
        }
        Object result = invocation.proceed();

        // 清除 ThreadLocal
        ConnectorAppContextHolder.clear();
        Configuration.clearApiDataSourceThreadLocal();

        return result;
    }

    public ApiDataSource copyApiDataSource(ApiDataSource apiDataSource) {
        ApiDataSource currentApiDataSource = new ApiDataSource();
        currentApiDataSource.setBaseUrl(apiDataSource.getBaseUrl());
        currentApiDataSource.setAk(apiDataSource.getAk());
        currentApiDataSource.setSk(apiDataSource.getSk());
        currentApiDataSource.setAutoRefreshToken(apiDataSource.isAutoRefreshToken());
        currentApiDataSource.setAutoSetHeader(apiDataSource.isAutoSetHeader());
        currentApiDataSource.setConnectionPool(apiDataSource.getConnectionPool());
        currentApiDataSource.setContextManager(apiDataSource.getContextManager());
        currentApiDataSource.setErrorProcessorRegister(apiDataSource.getErrorProcessorRegister());
        currentApiDataSource.setHeaderProcessor(apiDataSource.getHeaderProcessor());
        currentApiDataSource.setAutoSetHeader(apiDataSource.isAutoSetHeader());
        currentApiDataSource.setTimeout(apiDataSource.getTimeout());
        currentApiDataSource.setLoggingLevel(apiDataSource.getLoggingLevel());
        currentApiDataSource.setLoggingStrategy(apiDataSource.getLoggingStrategy());
        currentApiDataSource.setTokenManager(apiDataSource.getTokenManager());
        return currentApiDataSource;
    }
}
