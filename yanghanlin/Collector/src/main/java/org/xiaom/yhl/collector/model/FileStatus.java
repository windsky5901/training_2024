package org.xiaom.yhl.collector.model;

/**
 * ClassName: FileStatus
 * Package: org.xiaom.yhl.collector.model
 * Description:
 *
 * @Author 杨瀚林
 * @Create 2024/6/16 19:43
 * @Version 1.0
 */
public class FileStatus {
    private long size;
    private long lastModified;

    public FileStatus(long size, long lastModified) {
        this.size = size;
        this.lastModified = lastModified;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public long getLastModified() {
        return lastModified;
    }

    public void setLastModified(long lastModified) {
        this.lastModified = lastModified;
    }
}
