package org.xiaom.yhl.collector.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import org.apache.commons.io.monitor.FileAlterationListener;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.xiaom.yhl.collector.model.FileStatus;
import org.xiaom.yhl.collector.service.LogFileService;



import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.jcraft.jsch.*;


/**
 * ClassName: ConfigFileWatcher
 * Package: org.xiaom.yhl.collector.config
 * Description:
 *
 * @Author 杨瀚林
 * @Create 2024/6/12 16:33
 * @Version 1.0
 */
@Component
public class ConfigFileWatcher {
    private static final Logger logger = LoggerFactory.getLogger(ConfigFileWatcher.class);
    private static final String CONFIG_FILE_PATH = "E:/Document/Github/training_2024/yanghanlin/Collector/src/main/resources/cfg.json";

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final CollectorConfig collectorConfig;
    private final MetricsUploader metricsUploader;
    private final LogFileService logFileService;



    @Autowired
    private SshProperties sshProperties;

    @Autowired
    public ConfigFileWatcher(CollectorConfig collectorConfig, MetricsUploader metricsUploader, LogFileService logFileService) {
        this.collectorConfig = collectorConfig;
        this.metricsUploader = metricsUploader;
        this.logFileService = logFileService;

    }


    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        File configFile = new File(CONFIG_FILE_PATH);
        FileAlterationObserver observer = new FileAlterationObserver(configFile.getParent());
        FileAlterationMonitor monitor = new FileAlterationMonitor(5000);

        observer.addListener(new FileAlterationListener() {
            @Override
            public void onStart(FileAlterationObserver observer) {
                logger.info("FileAlterationObserver started");
            }

            @Override
            public void onDirectoryCreate(File directory) {
            }

            @Override
            public void onDirectoryChange(File directory) {
            }

            @Override
            public void onDirectoryDelete(File directory) {
            }

            @Override
            public void onFileCreate(File file) {
                logger.info("File created: {}", file.getAbsolutePath());
                if (file.equals(configFile)) {
                    loadConfig(file);
                }
            }

            @Override
            public void onFileChange(File file) {
                logger.info("File changed: {}", file.getAbsolutePath());
                if (file.equals(configFile)) {
                    loadConfig(file);
                } else if (collectorConfig.getFiles().contains(file.getAbsolutePath())) {
                    logger.info("Log file changed: {}", file.getAbsolutePath());
                    uploadLogFile(file.getAbsolutePath());
                }
            }

            @Override
            public void onFileDelete(File file) {
                logger.info("File deleted: {}", file.getAbsolutePath());
            }

            @Override
            public void onStop(FileAlterationObserver observer) {
                logger.info("FileAlterationObserver stopped");
            }
        });

        monitor.addObserver(observer);
        try {
            monitor.start();
            logger.info("FileAlterationMonitor started");
            loadConfig(configFile); // Initial load
        } catch (Exception e) {
            logger.error("Error starting config file monitor", e);
        }
    }

    private void loadConfig(File configFile) {
        try {
            CollectorConfig newConfig = objectMapper.readValue(configFile, CollectorConfig.class);
            logger.info("Loaded new configuration: {}", newConfig);
            if (!newConfig.getLogStorage().equals(collectorConfig.getLogStorage())) {
                collectorConfig.setLogStorage(newConfig.getLogStorage());
                metricsUploader.uploadLogStorageChange(newConfig.getLogStorage());

            }
            collectorConfig.setFiles(newConfig.getFiles());


            // 初始化对远程日志文件的定时监控任务
            initializeRemoteLogFileMonitoring();
        } catch (IOException e) {
            logger.error("Error loading config file", e);
        }
    }

    private void initializeRemoteLogFileMonitoring() {
        // 使用定时任务定期检查远程日志文件的变化
        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleAtFixedRate(() -> {
            for (String logFile : collectorConfig.getFiles()) {
                uploadLogFile(logFile);
            }
        }, 0, 1, TimeUnit.MINUTES);
    }

    private void uploadLogFile(String filePath) {
        try {
            List<String> logs = logFileService.readRemoteLogs(filePath);
            if (!logs.isEmpty()) {
                metricsUploader.uploadLogs("my-computer", filePath, logs);
            }
        } catch (Exception e) {
            logger.error("Error reading log file: {}", filePath, e);
        }
    }

    private void checkRemoteFileChanges(String filePath) {
        try {
            FileStatus currentStatus = logFileService.getRemoteFileStatus(filePath);
            FileStatus previousStatus = remoteFileStatusMap.get(filePath);

            if (previousStatus == null || currentStatus.getSize() != previousStatus.getSize() || currentStatus.getLastModified() != previousStatus.getLastModified()) {
                logger.info("Remote log file changed: {}", filePath);
                uploadLogFile(filePath);
                remoteFileStatusMap.put(filePath, currentStatus);
            }
        } catch (Exception e) {
            logger.error("Error checking remote log file: {}", filePath, e);
        }
    }
    private final Map<String, FileStatus> remoteFileStatusMap = new HashMap<>();
}

