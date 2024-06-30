package org.xiaom.yhl.collector.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * ClassName: CollectorConfig
 * Package: org.xiaom.yhl.collector.config
 * Description:
 *
 * @Author 杨瀚林
 * @Create 2024/6/12 16:32
 * @Version 1.0
 */
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "collector")
public class CollectorConfig {
    private List<String> files;

    @JsonProperty("log_storage")
    private String logStorage;

}