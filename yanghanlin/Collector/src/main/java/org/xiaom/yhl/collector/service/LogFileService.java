package org.xiaom.yhl.collector.service;

import com.jcraft.jsch.JSchException;
import org.xiaom.yhl.collector.model.FileStatus;

import java.io.IOException;
import java.util.List;

/**
 * ClassName: LogFileService
 * Package: org.xiaom.yhl.collector.service
 * Description:
 *
 * @Author 杨瀚林
 * @Create 2024/6/12 15:19
 * @Version 1.0
 */
public interface LogFileService {
    List<String> getLogFiles();
    List<String> readLogs(String filePath);

    List<String> readRemoteLogs(String filePath);
    FileStatus getRemoteFileStatus(String filePath) throws Exception;

}
