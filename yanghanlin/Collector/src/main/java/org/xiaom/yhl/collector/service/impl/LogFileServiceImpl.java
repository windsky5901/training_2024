package org.xiaom.yhl.collector.service.impl;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.xiaom.yhl.collector.config.SshProperties;
import org.xiaom.yhl.collector.model.FileStatus;
import org.xiaom.yhl.collector.service.LogFileService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * ClassName: LogFileServiceImpl
 * Package: org.xiaom.yhl.collector.service.impl
 * Description:
 *
 * @Author 杨瀚林
 * @Create 2024/6/12 15:19
 * @Version 1.0
 */
@Service
public class LogFileServiceImpl implements LogFileService {

    private static final Logger logger = LoggerFactory.getLogger(LogFileServiceImpl.class);

    @Autowired
    private SshProperties sshProperties;

    private final Map<String, Long> fileReadPositions = new HashMap<>();
    private final Map<String, List<String>> previousLogsMap = new HashMap<>();
    @Value("${collector.log.files}")
    private List<String> logFiles;

    @Override
    public List<String> getLogFiles() {
        return logFiles;
    }


    @Override
    public List<String> readLogs(String filePath) {
        List<String> logs = new ArrayList<>();
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
            channel.setCommand("cat " + filePath);
            channel.setErrStream(System.err);
            BufferedReader reader = new BufferedReader(new InputStreamReader(channel.getInputStream()));

            channel.connect();
            logger.info("SSH channel connected, executing command: cat {}", filePath);

            String line;
            while ((line = reader.readLine()) != null) {
                logs.add(line);
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

        return logs;
    }

    @Override
    public List<String> readRemoteLogs(String filePath) {
        List<String> logs = new ArrayList<>();
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

            long previousReadPosition = fileReadPositions.getOrDefault(filePath, 0L);
            channel = (ChannelExec) session.openChannel("exec");
            channel.setCommand("tail -c +" + (previousReadPosition + 1) + " " + filePath);
            channel.setErrStream(System.err);
            BufferedReader reader = new BufferedReader(new InputStreamReader(channel.getInputStream()));

            channel.connect();
            logger.info("SSH channel connected, executing command: tail -c +{} {}", previousReadPosition + 1, filePath);

            String line;
            while ((line = reader.readLine()) != null) {
                logs.add(line);
            }

            // 更新文件读取位置
            long newReadPosition = previousReadPosition + logs.stream().mapToLong(String::length).sum() + logs.size(); // 加上行尾换行符的长度
            fileReadPositions.put(filePath, newReadPosition);
            List<String> previousLogs = previousLogsMap.get(filePath);
            if (previousLogs == null || !previousLogs.equals(logs)) {
                previousLogsMap.put(filePath, new ArrayList<>(logs)); // 更新上次读取的日志
                return logs; // 返回新的日志内容
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

        return Collections.emptyList();
    }
    @Override
    public FileStatus getRemoteFileStatus(String filePath) throws JSchException, IOException {
        JSch jsch = new JSch();
        Session session = null;
        ChannelExec channel = null;
        BufferedReader reader = null;

        try {
            session = jsch.getSession(sshProperties.getUser(), sshProperties.getHost(), sshProperties.getPort());
            session.setPassword(sshProperties.getPassword());
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect();

            channel = (ChannelExec) session.openChannel("exec");
            channel.setCommand("stat -c '%s %Y' " + filePath);
            channel.setErrStream(System.err);
            reader = new BufferedReader(new InputStreamReader(channel.getInputStream()));
            channel.connect();

            String output = reader.readLine();
            if (output != null) {
                String[] parts = output.split(" ");
                long size = Long.parseLong(parts[0]);
                long lastModified = Long.parseLong(parts[1]);
                return new FileStatus(size, lastModified);
            } else {
                throw new IOException("Failed to get file status for " + filePath);
            }
        } finally {
            if (reader != null) reader.close();
            if (channel != null) channel.disconnect();
            if (session != null) session.disconnect();
        }
    }
}


