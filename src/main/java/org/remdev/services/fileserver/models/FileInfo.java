package org.remdev.services.fileserver.models;

public class FileInfo {
    private String filename;
    private String size;

    public FileInfo() {
    }

    public FileInfo(String filename, String size) {
        this.filename = filename;
        this.size = size;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }
}
