package org.xiaom.yhl.server.server.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * ClassName: SshProperties
 * Package: org.xiaom.yhl.server.server.config
 * Description:
 *
 * @Author 杨瀚林
 * @Create 2024/6/12 19:47
 * @Version 1.0
 */
@Setter
@Getter
@Configuration
@ConfigurationProperties(prefix = "collector.ssh")
public class SshProperties {
    private String host;
    private String user;
    private String password;
    private int port;



}
