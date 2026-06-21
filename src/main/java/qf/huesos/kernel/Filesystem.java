package qf.huesos.kernel;

import qf.huesos.kernel.nsfw.NSFW;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class Filesystem {
    private String directory;
    private final NSFW nsfw;

    public Filesystem(String pathname) {
        this.nsfw = new NSFW();
        this.nsfw.init(pathname);
        this.directory = pathname;
    }

    public List<String> parseFile(String name) {
        return nsfw.parseFile(name);
    }

    public void writeToFile(String fileName, String data) throws IOException {
        nsfw.writeToFile(fileName, data);
    }

    public void writeToFile(String fileName, List<String> data) throws IOException {
        nsfw.writeToFile(fileName, data);
    }

    public void makeDirectory(String name) throws IOException {
        nsfw.makeDirectory(name);
    }

    public void makeDirectory(String name, String path) throws IOException {
        nsfw.makeDirectory(name, path);
    }

    public void newFile(String name) {
        nsfw.newFile(name);
    }

    public void newFile(String name, String path) {
        nsfw.newFile(name, path);
    }

    public List<String> getAllFilesInDirectory() {
        return nsfw.getAllFilesInDirectory();
    }

    public List<String> getAllFilesInDirectory(File file) {
        return nsfw.getAllFilesInDirectory(file);
    }

    public void deleteFile(String path) throws IOException {
        nsfw.deleteFile(path);
    }
    public void moveFile(String path, String target) throws IOException {
        nsfw.moveFile(path, target);
    }
    public void copyFile(String path, String target) throws IOException {
        nsfw.copyFile(path, target);
    }

    public void setDirectory(String pathname) {
        this.directory = pathname;
        nsfw.setCurrentDirectory(pathname);
    }

    public String getDirectory() {
        return this.directory;
    }
}