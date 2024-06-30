package org.xiaom.yhl.server.server.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.xiaom.yhl.server.server.entity.Metric;

import java.util.List;

/**
 * ClassName: MetricRepository
 * Package: org.xiaom.yhl.server.server.repository.jpa
 * Description:
 *
 * @Author 杨瀚林
 * @Create 2024/6/12 20:57
 * @Version 1.0
 */
public interface MetricRepository extends JpaRepository<Metric, Long> {
    List<Metric> findByEndpointAndMetricAndTimestampBetween(String endpoint, String metric, long startTs, long endTs);
}
