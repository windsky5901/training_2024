package org.xiaom.yhl.server.server.config;

import com.zaxxer.hikari.HikariDataSource;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;


import javax.sql.DataSource;
/**
 * ClassName: MetricsDataSourceConfig
 * Package: org.xiaom.yhl.server.server.config
 * Description:
 *
 * @Author 杨瀚林
 * @Create 2024/6/12 20:16
 * @Version 1.0
 */
@Configuration
public class MetricsDataSourceConfig {

    @Primary
    @Bean(name = "metricsDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.metrics")
    public HikariDataSource metricsDataSource() {
        return new HikariDataSource();
    }

    @Primary
    @Bean(name = "metricsJdbcTemplate")
    public JdbcTemplate metricsJdbcTemplate(@Qualifier("metricsDataSource") HikariDataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
}
