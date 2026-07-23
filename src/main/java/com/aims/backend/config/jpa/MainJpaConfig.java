package com.aims.backend.config.jpa;

import com.aims.backend.properties.AppDataSourceProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
@EnableJpaRepositories(
    basePackages = "com.aims.backend.repository",
    excludeFilters = @org.springframework.context.annotation.ComponentScan.Filter(
        type = org.springframework.context.annotation.FilterType.REGEX,
        pattern = "com\\.aims\\.backend\\.repository\\.sample\\..*"
    ),
    entityManagerFactoryRef = "mainEntityManagerFactory",
    transactionManagerRef = "mainTransactionManager"
)
@RequiredArgsConstructor
public class MainJpaConfig {
    private final AppDataSourceProperties props;

    @Primary
    @Bean
    public DataSource mainDataSource() {
        return DataSourceBuilder.create()
            .url(props.getMain().getJdbcUrl())
            .username(props.getMain().getUsername())
            .password(props.getMain().getPassword())
            .driverClassName(props.getMain().getDriverClassName())
            .build();
    }

    @Primary
    @Bean
    public LocalContainerEntityManagerFactoryBean mainEntityManagerFactory() {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(mainDataSource());
        em.setPackagesToScan(
            "com.aims.backend.domain.commons", 
            "com.aims.backend.domain.mainpage", 
            "com.aims.backend.domain.user", 
            "com.aims.backend.domain.dashboard", 
            "com.aims.backend.domain.alert",
            "com.aims.backend.domain.eventAnalysis");
        em.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        return em;
    }

    @Primary
    @Bean
    public PlatformTransactionManager mainTransactionManager() {
        return new JpaTransactionManager(mainEntityManagerFactory().getObject());
    }
}
