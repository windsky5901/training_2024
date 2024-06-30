package org.xiaom.yhl.server.server.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.xiaom.yhl.server.server.entity.LogEntry;
import org.xiaom.yhl.server.server.entity.Metric;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ArrayList;
import java.io.File;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xiaom.yhl.server.server.repository.jdbc.LogRepository;
import org.xiaom.yhl.server.server.repository.jpa.MetricRepository;

/**
 * ClassName: MetricService
 * Package: org.xiaom.yhl.server.server.service
 * Description:
 *
 * @Author 杨瀚林
 * @Create 2024/5/30 15:32
 * @Version 1.0
 */
@Service
public class MetricService {

    @Autowired
    private MetricRepository metricRepository;

    @Autowired
    private LogRepository logRepository;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private SshService sshService;

    @Autowired
    @Qualifier("metricsJdbcTemplate")
    private JdbcTemplate metricsJdbcTemplate;

    @Autowired
    @Qualifier("logsJdbcTemplate")
    private JdbcTemplate logsJdbcTemplate;

    private static final Logger logger = LoggerFactory.getLogger(MetricService.class);

    private static final String CPU_RECENT_METRICS_KEY = "recentCpuMetrics";
    private static final String MEM_RECENT_METRICS_KEY = "recentMemMetrics";
    private String logStorage;

    public void saveMetric(Metric metric) {

        metricRepository.save(metric);
        String redisKey = metric.getMetric().equals("cpu.used.percent") ? CPU_RECENT_METRICS_KEY : MEM_RECENT_METRICS_KEY;
        redisTemplate.opsForList().leftPush(redisKey, metric);
        if (redisTemplate.opsForList().size(redisKey) > 10) {
            redisTemplate.opsForList().rightPop(redisKey);
        }
    }

    public List<Metric> queryMetrics(String endpoint, String metric, long startTs, long endTs) {
        return metricRepository.findByEndpointAndMetricAndTimestampBetween(endpoint, metric, startTs, endTs);
    }

    public void saveLogs(String hostname, String file, List<String> logs) {
        if (logStorage == null) {
            logger.error("Log storage type is not set. Please set log storage type.");
            return;
        }
        logger.info("Saving logs for file: {} with log storage: {}", file, logStorage);
        if ("local_file".equals(logStorage)) {
            saveLogsToLocalFile(hostname, file, logs);
        } else if ("mysql".equals(logStorage)) {
            saveLogsToMySQL(hostname, file, logs);
        }
    }

    private void saveLogsToMySQL(String hostname, String file, List<String> logs) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmm"));
        String tableName = "logs_" + timestamp;

        // 创建表如果不存在
        String createTableQuery = "CREATE TABLE IF NOT EXISTS " + tableName + " (" +
                "OriginPath VARCHAR(255), " +
                "Name VARCHAR(255), " +
                "Log LONGTEXT)";
        logsJdbcTemplate.execute(createTableQuery);

        String originPath = new File(file).getParent();
        String name = new File(file).getName();

        // 插入日志记录
        for (String log : logs) {
            String insertQuery = "INSERT INTO " + tableName + " (OriginPath, Name, Log) VALUES (?, ?, ?)";
            logsJdbcTemplate.update(insertQuery, originPath, name, log);
            logger.info("Log written to MySQL table: {}", tableName);
        }
    }

    private void saveLogsToLocalFile(String hostname, String file, List<String> logs) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmm"));
        String remoteDir = "/home/yhl/Logresult/" + timestamp + "/OriginPath:" + new File(file).getParent();
        String remoteFile = remoteDir + "/" + new File(file).getName();

        logger.info("Creating directory: {}", remoteDir);
        // 创建目录
        boolean dirCreated = sshService.executeCommand("mkdir -p " + remoteDir);
        if (dirCreated) {
            logger.info("Directory created: {}", remoteDir);

            // 写入日志
            for (String log : logs) {
                boolean logWritten = sshService.executeCommand("echo \"" + log + "\" >> " + remoteFile);
                if (logWritten) {
                    logger.info("Log written to remote file: {}", remoteFile);
                } else {
                    logger.error("Failed to write log to remote file: {}", remoteFile);
                }
            }
        } else {
            logger.error("Failed to create directory: {}", remoteDir);
        }
    }


    public List<String> queryLogs(String hostname, String file) {
        List<LogEntry> logEntries = logRepository.findByHostnameAndFile(hostname, file);
        List<String> logs = new ArrayList<>();
        for (LogEntry logEntry : logEntries) {
            logs.add(logEntry.getLog());
        }
        return logs;
    }



    public void updateLogStorage(String logStorage) {
        this.logStorage = logStorage;
        logger.info("Updated log storage to: {}", logStorage);
    }


}
