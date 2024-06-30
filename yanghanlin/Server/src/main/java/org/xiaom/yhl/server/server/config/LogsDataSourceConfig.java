package org.xiaom.yhl.server.server.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * ClassName: LogsDataSourceConfig
 * Package: org.xiaom.yhl.server.server.config
 * Description:
 *
 * @Author 杨瀚林
 * @Create 2024/6/12 20:17
 * @Version 1.0
 */
@Configuration
public class LogsDataSourceConfig {

    @Bean(name = "logsDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.logs")
    public HikariDataSource logsDataSource() {
        return new HikariDataSource();
    }

    @Bean(name = "logsJdbcTemplate")
    public JdbcTemplate logsJdbcTemplate(@Qualifier("logsDataSource") HikariDataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
}