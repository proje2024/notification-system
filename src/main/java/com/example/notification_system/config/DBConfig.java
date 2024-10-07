package com.example.notification_system.config;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Configuration
@EnableTransactionManagement
public class DBConfig {

    @Bean(name = "primaryDataSource")
    public DataSource primaryDataSource() {
        return DataSourceBuilder.create()
                .url(System.getenv("SPRING_DATASOURCE_URL_PRIMARY"))
                .username(System.getenv("SPRING_DATASOURCE_USERNAME"))
                .password(System.getenv("SPRING_DATASOURCE_PASSWORD"))
                .driverClassName(System.getenv("SPRING_DATASOURCE_DRIVER_CLASS_NAME"))
                .build();
    }

    @Bean(name = "replicaDataSource")
    public DataSource replicaDataSource() {
        return DataSourceBuilder.create()
                .url(System.getenv("SPRING_DATASOURCE_URL_REPLICA"))
                .username(System.getenv("SPRING_DATASOURCE_USERNAME"))
                .password(System.getenv("SPRING_DATASOURCE_PASSWORD"))
                .driverClassName(System.getenv("SPRING_DATASOURCE_DRIVER_CLASS_NAME"))
                .build();
    }

    @Bean
    public DataSource routingDataSource() {
        AbstractRoutingDataSource routingDataSource = new AbstractRoutingDataSource() {
            @Override
            protected Object determineCurrentLookupKey() {
                if (TransactionSynchronizationManager.isCurrentTransactionReadOnly()) {
                    return "replica";
                }
                return "primary";
            }
        };

        Map<Object, Object> dataSources = new HashMap<>();
        dataSources.put("primary", primaryDataSource());
        dataSources.put("replica", replicaDataSource());

        routingDataSource.setTargetDataSources(dataSources);
        routingDataSource.setDefaultTargetDataSource(primaryDataSource());

        return routingDataSource;
    }
}
