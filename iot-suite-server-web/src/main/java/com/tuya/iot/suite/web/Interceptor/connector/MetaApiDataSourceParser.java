package com.tuya.iot.suite.web.Interceptor.connector;

import com.tuya.connector.api.config.ApiDataSource;
import com.tuya.connector.api.config.Configuration;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * <p> TODO
 *
 * @author 哲也
 * @since 2021/5/31
 */
@Component
public class MetaApiDataSourceParser implements EnvironmentAware, InitializingBean {

    private static final String PREFIX = "connector.api.";

    private Environment env;

    private Configuration configuration;

    public MetaApiDataSourceParser(Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.env = environment;
    }

    public ConnectorApplication getConnectorApplication(String appName) {
        StandardEnvironment standardEnv = (StandardEnvironment) this.env;
        standardEnv.setIgnoreUnresolvableNestedPlaceholders(true);
        String ak = standardEnv.getProperty(PREFIX + appName + ".ak");
        String sk = standardEnv.getProperty(PREFIX + appName + ".sk");
        String code = standardEnv.getProperty(PREFIX + appName + ".code");
        String name = standardEnv.getProperty(PREFIX + appName + ".name");

        if (!StringUtils.isEmpty(ak) && !StringUtils.isEmpty(sk)) {
            ConnectorApplication connectorApplication = new ConnectorApplication();
            connectorApplication.setAk(ak);
            connectorApplication.setSk(sk);
            connectorApplication.setCode(code);
            connectorApplication.setName(name);
            return connectorApplication;
        }
        return null;
    }

    public List<String> getAppNames() {
        List<String> result = null;
        StandardEnvironment standardEnv = (StandardEnvironment) this.env;
        standardEnv.setIgnoreUnresolvableNestedPlaceholders(true);
        String dataSourceNames = standardEnv.getProperty("connector.api.name");
        if (StringUtils.isEmpty(dataSourceNames)) {
            dataSourceNames = standardEnv.getProperty("connector.api.names");
        }
        if (!StringUtils.isEmpty(dataSourceNames)) {
            dataSourceNames = dataSourceNames.replaceAll(" ", "");
            String[] split = dataSourceNames.split(",");
            result = Arrays.asList(split);
        }
        return result;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        List<String> appNames = getAppNames();
        if (CollectionUtils.isEmpty(appNames)) {
            return;
        }
        appNames.forEach(appName -> {
            ConnectorApplication ca = getConnectorApplication(appName);
            if (Objects.nonNull(ca)) {
                MultiConnectorApplication.addConnectorApplication(appName, ca);
            }
        });

        // 配置默认
        String property = this.env.getProperty(PREFIX + "default");
        ConnectorApplication connectorApplication = MultiConnectorApplication.getConnectorApplication(property);
        ApiDataSource apiDataSource = configuration.getApiDataSource();
        apiDataSource.setAk(connectorApplication.getAk());
        apiDataSource.setSk(connectorApplication.getSk());
    }

}
