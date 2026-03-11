package com.chipcook.api.config.multitenancy;

import org.hibernate.cfg.AvailableSettings;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;

import java.util.Map;

@Configuration
public class MultitenancyJpaConfiguration {

    public MultitenancyJpaConfiguration(
            SchemaMultiTenantConnectionProvider multiTenantConnectionProvider,
            CurrentTenantIdentifierResolverImpl currentTenantIdentifierResolver,
            ConfigurableListableBeanFactory beanFactory) {

        String[] beanNames = beanFactory.getBeanNamesForType(LocalContainerEntityManagerFactoryBean.class);
        if (beanNames.length > 0) {
            LocalContainerEntityManagerFactoryBean em =
                    (LocalContainerEntityManagerFactoryBean) beanFactory.getBean(beanNames[0]);

            Map<String, Object> properties = em.getJpaPropertyMap();
            properties.put(AvailableSettings.MULTI_TENANT_CONNECTION_PROVIDER, multiTenantConnectionProvider);
            properties.put(AvailableSettings.MULTI_TENANT_IDENTIFIER_RESOLVER, currentTenantIdentifierResolver);
        }
    }
}
