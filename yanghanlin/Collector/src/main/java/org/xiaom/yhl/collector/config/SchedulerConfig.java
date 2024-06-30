package org.xiaom.yhl.collector.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.xiaom.yhl.collector.model.FileStatus;
import org.xiaom.yhl.collector.service.CPUAndMemUsageService;
import org.xiaom.yhl.collector.service.LogFileService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ClassName: SchedulerConfig
 * Package: org.xiaom.yhl.collector.config
 * Description:
 *
 * @Author 杨瀚林
 * @Create 2024/5/29 21:03
 * @Version 1.0
 */
@Component
public class SchedulerConfig {

    private static final Logger logger = LoggerFactory.getLogger(SchedulerConfig.class);
    private final CPUAndMemUsageService cpuAndMemUsageService;
    private final MetricsUploader metricsUploader;
    private final LogFileService logFileService;


    @Autowired
    public SchedulerConfig(CPUAndMemUsageService cpuAndMemUsageService, MetricsUploader metricsUploader, LogFileService logFileService) {
        this.cpuAndMemUsageService = cpuAndMemUsageService;
        this.metricsUploader = metricsUploader;
        this.logFileService = logFileService;
    }

    @Scheduled(fixedRate = 60000)
    public void collectUsage() {
        try {
            double cpuUsage = cpuAndMemUsageService.getCpuUsage();
            double memoryUsage = cpuAndMemUsageService.getMemoryUsage();
            System.out.println(String.format("CPU Usage: %.2f%%", cpuUsage * 100));
            System.out.println(String.format("Memory Usage: %.2f%%", memoryUsage * 100));
            metricsUploader.uploadMetric("cpu.used.percent", cpuUsage);
            metricsUploader.uploadMetric("mem.used.percent", memoryUsage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Scheduled(fixedRate = 60000)
    public void collectLogs() {
        try {
            List<String> logFiles = logFileService.getLogFiles();
            for (String logFile : logFiles) {
                List<String> logs = logFileService.readLogs(logFile);
                if (!logs.isEmpty()) {
                    metricsUploader.uploadLogs("my-computer", logFile, logs);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Scheduled(fixedRate = 60000)
    public void checkRemoteFileStatus() {
        try {
            for (String logFile : logFileService.getLogFiles()) {
                FileStatus currentStatus = logFileService.getRemoteFileStatus(logFile);
                FileStatus previousStatus = remoteFileStatusMap.get(logFile);

                if (previousStatus == null || currentStatus.getSize() != previousStatus.getSize() || currentStatus.getLastModified() != previousStatus.getLastModified()) {
                    logger.info("Remote log file changed: {}", logFile);
                    List<String> logs = logFileService.readRemoteLogs(logFile);
                    if (!logs.isEmpty()) {
                        metricsUploader.uploadLogs("my-computer", logFile, logs);
                    }
                    remoteFileStatusMap.put(logFile, currentStatus);
                }
            }
        } catch (Exception e) {
            logger.error("Error checking remote log file status", e);
        }
    }
    private final Map<String, FileStatus> remoteFileStatusMap = new HashMap<>();

}
