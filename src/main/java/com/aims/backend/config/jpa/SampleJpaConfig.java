package com.aims.backend.config.jpa;

import com.aims.backend.properties.AppDataSourceProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
@EnableJpaRepositories(
    basePackages = "com.aims.backend.repository.sample",
    entityManagerFactoryRef = "sampleEntityManagerFactory",
    transactionManagerRef = "sampleTransactionManager"
)
@RequiredArgsConstructor
public class SampleJpaConfig {
    private final AppDataSourceProperties props;

    @Bean
    public DataSource sampleDataSource() {
        return DataSourceBuilder.create()
            .url(props.getSample().getJdbcUrl())
            .username(props.getSample().getUsername())
            .password(props.getSample().getPassword())
            .driverClassName(props.getSample().getDriverClassName())
            .build();
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean sampleEntityManagerFactory() {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(sampleDataSource());
        em.setPackagesToScan("com.aims.backend.domain.sample", "com.aims.backend.domain.dashboard");
        em.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        return em;
    }

    @Bean
    public PlatformTransactionManager sampleTransactionManager() {
        return new JpaTransactionManager(sampleEntityManagerFactory().getObject());
    }
}
