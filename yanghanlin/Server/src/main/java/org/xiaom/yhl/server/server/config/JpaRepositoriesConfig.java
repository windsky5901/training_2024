package org.xiaom.yhl.server.server.config;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

/**
 * ClassName: JpaRepositoriesConfig
 * Package: org.xiaom.yhl.server.server.config
 * Description:
 *
 * @Author 杨瀚林
 * @Create 2024/6/12 20:45
 * @Version 1.0
 */

// metricsEntityManagerFactory这个有问题，没有找到这个对象
/*@EnableJpaRepositories(
        basePackages = "org.xiaom.yhl.server.server.repository.jpa",
        entityManagerFactoryRef = "metricsEntityManagerFactory",
        transactionManagerRef = "metricsTransactionManager"
)*/
@Configuration
@EnableJpaRepositories(
        basePackages = "org.xiaom.yhl.server.server.repository.jpa",
        transactionManagerRef = "metricsTransactionManager"
)
public class JpaRepositoriesConfig {
    // 此配置类可以保持空，因为所有的 Bean 都在 MetricsDataSourceConfig 中定义了
}
