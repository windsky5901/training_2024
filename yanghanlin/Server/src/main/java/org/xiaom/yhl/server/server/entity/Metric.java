package org.xiaom.yhl.server.server.entity;

import jakarta.persistence.*;

import java.util.HashMap;
import java.util.Map;
/**
 * ClassName: Metric
 * Package: org.xiaom.yhl.server.server.entity
 * Description:
 *
 * @Author 杨瀚林
 * @Create 2024/5/30 15:25
 * @Version 1.0
 */
@Entity
@Table(name = "metric")
public class Metric {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String metric;

    @Column(nullable = false)
    private String endpoint;

    @Column(nullable = false)
    private long timestamp;

    @Column(nullable = false)
    private long step;

    @Column(nullable = false)
    private double value;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "metric_tags", joinColumns = @JoinColumn(name = "metric_id"))
    @MapKeyColumn(name = "tag_key")
    @Column(name = "tag_value")
    private Map<String, String> tags = new HashMap<>();

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMetric() {
        return metric;
    }

    public void setMetric(String metric) {
        this.metric = metric;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public long getStep() {
        return step;
    }

    public void setStep(long step) {
        this.step = step;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public Map<String, String> getTags() {
        return tags;
    }

    public void setTags(Map<String, String> tags) {
        this.tags = tags;
    }
}
