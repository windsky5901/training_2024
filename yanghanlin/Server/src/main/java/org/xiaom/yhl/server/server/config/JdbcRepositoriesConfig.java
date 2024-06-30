package org.xiaom.yhl.server.server.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories;

/**
 * ClassName: JdbcRepositoriesConfig
 * Package: org.xiaom.yhl.server.server.config
 * Description:
 *
 * @Author 杨瀚林
 * @Create 2024/6/12 20:45
 * @Version 1.0
 */
@Configuration
@EnableJdbcRepositories(
        basePackages = "org.xiaom.yhl.server.server.repository.jdbc",
        dataAccessStrategyRef = "logsDataAccessStrategy",
        jdbcOperationsRef = "logsJdbcTemplate"
)
public class JdbcRepositoriesConfig {
}