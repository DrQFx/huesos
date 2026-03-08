package qf.huesos.kernel;

import qf.huesos.apps.Terminal;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class SysCall {

    // General
    public void print(String text, int color) {
        Kernel.getInstance().terminal.println(text, color);
    }
    public void fillRectangle(int x, int y, int w, int h, int color) {
        Kernel.screen.fillRect(x, y, w, h, color);
    }

    // Files
    public void newFile(String name) {
        Kernel.getInstance().getFilesystem().newFile(name);
    }
    public void newFile(String name, String path) {
        Kernel.getInstance().getFilesystem().newFile(name, path);
    }
    public void makeDirectory(String name) throws IOException {
        Kernel.getInstance().getFilesystem().makeDirectory(name);
    }
    public void makeDirectory(String name, String path) throws IOException {
        Kernel.getInstance().getFilesystem().makeDirectory(name, path);
    }
    public List<String> getAllFilesInDirectory() {
        return Kernel.getInstance().getFilesystem().getAllFilesInDirectory();
    }
    public List<String> getAllFilesInDirectory(File file) {
        return Kernel.getInstance().getFilesystem().getAllFilesInDirectory(file);
    }
    public List<String> parseFile(String name) {
        return Kernel.getInstance().getFilesystem().parseFile(name);
    }
    public void writeToFile(String name, String data) throws IOException {
        Kernel.getInstance().getFilesystem().writeToFile(name, data);
    }
    public void writeToFile(String name, List<String> data) throws IOException {
        Kernel.getInstance().getFilesystem().writeToFile(name, data);
    }
    public void setDirectory(String pathname) {
        Kernel.getInstance().getFilesystem().setDirectory(pathname);
    }
    public String getDirectory() {
        return Kernel.getInstance().getFilesystem().getDirectory();
    }
}
