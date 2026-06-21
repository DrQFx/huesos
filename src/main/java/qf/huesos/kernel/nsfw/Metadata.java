package qf.huesos.kernel.nsfw;

import java.io.File;

public class Metadata {
    File file;
    FileType fileType;
    long createdAt;
    String version;

    private String path;

    public Metadata(String path) {
        this.path = path;
    }

    public void setFile(File file) {
        this.file = file;
    }
    public void setFileType(FileType fileType) {
        this.fileType = fileType;
    }
    public void setCreatedAt(long time) {
        this.createdAt = time;
    }
    public void setVersion(String version) {
        this.version = version;
    }

    public File getFile() {
        return this.file;
    }
    public FileType getFileType() {
        return this.fileType;
    }
    public long getCreatedAt() {
        return this.createdAt;
    }
    public String getVersion() {
        return this.version;
    }
    public String getPath() {
        return this.path;
    }
}
