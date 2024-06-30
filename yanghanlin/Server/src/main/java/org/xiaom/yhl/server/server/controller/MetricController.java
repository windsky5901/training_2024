package org.xiaom.yhl.server.server.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.xiaom.yhl.server.server.entity.Metric;
import org.xiaom.yhl.server.server.service.MetricService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ClassName: MetricController
 * Package: org.xiaom.yhl.server.server.controller
 * Description:
 *
 * @Author 杨瀚林
 * @Create 2024/5/30 15:29
 * @Version 1.0
 */
@RestController
@RequestMapping("/api/metric")
public class MetricController {
    private static final Logger logger = LoggerFactory.getLogger(MetricController.class);

    @Autowired
    private MetricService metricService;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadMetrics(@RequestBody List<Metric> metrics) {
        logger.info("Received metric upload request with {} metrics", metrics.size());
        for (Metric metric : metrics) {
            metricService.saveMetric(metric);
        }
        Map<String, Object> response = new HashMap<>();
        response.put("code", HttpStatus.OK.value());
        response.put("message", "ok");
        response.put("data", "");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/query")
    public ResponseEntity<?> queryMetrics(
            @RequestParam String endpoint,
            @RequestParam(required = false) String metric,
            @RequestParam long startTs,
            @RequestParam long endTs) {
        logger.info("Received query metrics request: endpoint={}, metric={}, startTs={}, endTs={}",
                endpoint, metric, startTs, endTs);
        List<Metric> metrics = metricService.queryMetrics(endpoint, metric, startTs, endTs);
        Map<String, Object> response = new HashMap<>();
        response.put("code", HttpStatus.OK.value());
        response.put("message", "ok");
        response.put("data", metrics);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/log/upload")
    public ResponseEntity<?> uploadLogs(@RequestBody List<Map<String, Object>> logs) {
        logger.info("Received log upload request with {} log entries", logs.size());
        for (Map<String, Object> log : logs) {
            String hostname = (String) log.get("hostname");
            String file = (String) log.get("file");
            List<String> logEntries = (List<String>) log.get("logs");
            logger.info("Received logs for file: {}", file);
            metricService.saveLogs(hostname, file, logEntries);
        }
        Map<String, Object> response = new HashMap<>();
        response.put("code", HttpStatus.OK.value());
        response.put("message", "ok");
        response.put("data", "");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/log/query")
    public ResponseEntity<?> queryLogs(
            @RequestParam String hostname,
            @RequestParam String file) {
        logger.info("Received query logs request: hostname={}, file={}", hostname, file);
        List<String> logs = metricService.queryLogs(hostname, file);
        Map<String, Object> response = new HashMap<>();
        response.put("code", HttpStatus.OK.value());
        response.put("message", "ok");
        response.put("data", logs);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/config/upload")
    public ResponseEntity<?> uploadLogStorageConfig(@RequestBody Map<String, String> config) {
        String logStorage = config.get("log_storage");
        logger.info("Received log storage config update: {}", logStorage);
        metricService.updateLogStorage(logStorage);
        Map<String, Object> response = new HashMap<>();
        response.put("code", HttpStatus.OK.value());
        response.put("message", "ok");
        response.put("data", "");
        return ResponseEntity.ok(response);
    }
}
