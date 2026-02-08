package qf.huesos.kernel;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Filesystem {
    File file;
    String directory;

    public Filesystem(String pathname) {
        this.directory = pathname;
        file = new File(pathname);
        if (!file.exists() && !file.isDirectory()) {
            throw new RuntimeException("File does not exist or it's not a directory");
        }
        init();
    }

    private void init() {

    }

    public List<String> parseFile(String name) {
        List<String> strings = new ArrayList<>();
        BufferedReader reader;

        try {
            reader = new BufferedReader(new FileReader(name));
            String line = reader.readLine();

            while (line != null) {
                strings.add(line);

                line = reader.readLine();
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return strings;
    }

    public void writeToFile(String fileName, String data) throws IOException {
        FileWriter writer = new FileWriter(fileName, false);

        try {
            writer.write(data);

            writer.flush();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
    public void writeToFile(String fileName, List<String> data) throws IOException {
        FileWriter writer = new FileWriter(fileName, false);

        try {
            for (String line : data) {
                writer.write(line + "\n");
                writer.flush();
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public void makeDirectory(String name) throws IOException {
        Path dir = Paths.get(directory + "/" + name);
        Files.createDirectory(dir);
    }
    public void makeDirectory(String name, String path) throws IOException {
        Path dir = Paths.get(path + "/" + name);
        Files.createDirectory(dir);
    }

    public void newFile(String name) {
        try {
            File file = new File("fs/" + name);
            file.createNewFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public void newFile(String name, String path) {
        try {
            File file = new File(path + "/" + name);
            file.createNewFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public List<String> getAllFilesInDirectory() {
        List<String> list = new ArrayList<>();
        File[] files = this.file.listFiles();

        for (File file1 : files) {
            list.add(file1.getName());
        }

        return list;
    }
    public List<String> getAllFilesInDirectory(File file) {
        List<String> list = new ArrayList<>();
        File[] files = file.listFiles();

        for (File file1 : files) {
            list.add(file1.getName());
        }

        return list;
    }
}
