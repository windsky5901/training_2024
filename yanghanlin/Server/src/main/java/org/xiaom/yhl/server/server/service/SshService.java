package org.xiaom.yhl.server.server.service;

import com.jcraft.jsch.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.xiaom.yhl.server.server.config.SshProperties;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

/**
 * ClassName: SshService
 * Package: org.xiaom.yhl.server.server.service
 * Description:
 *
 * @Author 杨瀚林
 * @Create 2024/6/12 19:54
 * @Version 1.0
 */
@Service
public class SshService {

    private static final Logger logger = LoggerFactory.getLogger(SshService.class);

    @Autowired
    private SshProperties sshProperties;

    public boolean executeCommand(String command) {
        JSch jsch = new JSch();
        Session session = null;
        ChannelExec channel = null;
        boolean success = false;

        try {
            logger.info("Connecting to SSH server {}:{}", sshProperties.getHost(), sshProperties.getPort());
            session = jsch.getSession(sshProperties.getUser(), sshProperties.getHost(), sshProperties.getPort());
            session.setPassword(sshProperties.getPassword());

            Properties config = new Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);

            session.connect();
            logger.info("SSH session connected");

            channel = (ChannelExec) session.openChannel("exec");
            channel.setCommand(command);
            channel.setErrStream(System.err);
            BufferedReader reader = new BufferedReader(new InputStreamReader(channel.getInputStream()));

            channel.connect();
            logger.info("Executing command: {}", command);

            String line;
            while ((line = reader.readLine()) != null) {
                logger.info(line);
            }

            int exitStatus = channel.getExitStatus();
            logger.info("Command exit status: {}", exitStatus);
            if (exitStatus == 0) {
                success = true;
            } else {
                logger.error("Command execution failed with exit status: {}", exitStatus);
            }

        } catch (JSchException | IOException e) {
            logger.error("Exception occurred while executing command: {}", e.getMessage());
        } finally {
            if (channel != null) {
                channel.disconnect();
                logger.info("SSH channel disconnected");
            }
            if (session != null) {
                session.disconnect();
                logger.info("SSH session disconnected");
            }
        }

        return success;
    }
}
