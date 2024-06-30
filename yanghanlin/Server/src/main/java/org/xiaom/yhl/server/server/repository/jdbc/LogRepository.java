package org.xiaom.yhl.server.server.repository.jdbc;

import org.springframework.data.repository.CrudRepository;
import org.xiaom.yhl.server.server.entity.LogEntry;

import java.util.List;

/**
 * ClassName: LogRepository
 * Package: org.xiaom.yhl.server.server.repository.jdbc
 * Description:
 *
 * @Author 杨瀚林
 * @Create 2024/6/12 21:01
 * @Version 1.0
 */
public interface LogRepository extends CrudRepository<LogEntry, Long> {
    List<LogEntry> findByHostnameAndFile(String hostname, String file);
}