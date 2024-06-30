package org.xiaom.yhl.collector.service.impl;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.xiaom.yhl.collector.config.SshProperties;
import org.xiaom.yhl.collector.service.CPUAndMemUsageService;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.beans.factory.annotation.Value;

/**
 * ClassName: CpuUsageServiceImpl
 * Package: org.xiaom.yhl.collector.service.impl
 * Description:
 *
 * @Author 杨瀚林
 * @Create 2024/5/29 21:01
 * @Version 1.0
 */
@Service
public class CPUAndMemUsageServiceImpl implements CPUAndMemUsageService {
    private static final Logger logger = LoggerFactory.getLogger(CPUAndMemUsageServiceImpl.class);

    @Autowired
    private SshProperties sshProperties;

    private final AtomicLong previousIdleTime = new AtomicLong(0);
    private final AtomicLong previousTotalTime = new AtomicLong(0);

    @Override
    public double getCpuUsage() {
        JSch jsch = new JSch();
        Session session = null;
        ChannelExec channel = null;

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
            channel.setCommand("grep 'cpu ' /proc/stat");
            channel.setErrStream(System.err);
            BufferedReader reader = new BufferedReader(new InputStreamReader(channel.getInputStream()));

            channel.connect();
            logger.info("SSH channel connected, executing command: grep 'cpu ' /proc/stat");

            String line = reader.readLine();
            logger.info("Command output: " + line);

            if (line != null) {
                String[] values = line.trim().split("\\s+");
                long idleTime = Long.parseLong(values[4]);
                long totalTime = 0;
                for (int i = 1; i < values.length; i++) {
                    totalTime += Long.parseLong(values[i]);
                }

                long previousIdleTimeValue = previousIdleTime.getAndSet(idleTime);
                long previousTotalTimeValue = previousTotalTime.getAndSet(totalTime);
                long idleTimeDelta = idleTime - previousIdleTimeValue;
                long totalTimeDelta = totalTime - previousTotalTimeValue;

                return 1 - ((double) idleTimeDelta / totalTimeDelta);
            }
        } catch (JSchException e) {
            logger.error("JSchException occurred: {}", e.getMessage());
        } catch (IOException e) {
            logger.error("IOException occurred: {}", e.getMessage());
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
        return 0.0;
    }

    @Override
    public double getMemoryUsage() {
        JSch jsch = new JSch();
        Session session = null;
        ChannelExec channel = null;

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
            channel.setCommand("free -m");
            channel.setErrStream(System.err);
            BufferedReader reader = new BufferedReader(new InputStreamReader(channel.getInputStream()));

            channel.connect();
            logger.info("SSH channel connected, executing command: free -m");

            reader.readLine(); // Skip the header line
            String line = reader.readLine();
            logger.info("Command output: " + line);

            if (line != null) {
                String[] values = line.trim().split("\\s+");
                long totalMemory = Long.parseLong(values[1]);
                long usedMemory = Long.parseLong(values[2]);

                return (double) usedMemory / totalMemory;
            }
        } catch (JSchException e) {
            logger.error("JSchException occurred: {}", e.getMessage());
        } catch (IOException e) {
            logger.error("IOException occurred: {}", e.getMessage());
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
        return 0.0;
    }
}
