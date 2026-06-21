package qf.huesos.kernel.nsfw;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class NSFW {
    private final Map<FileType, String> fileMap = new HashMap<>();
    private final List<Metadata> filesMetadata = new ArrayList<>();

    private String rootDirectory;
    private String currentDirectory;

    private File rootFile;

    public void init(String directory) {
        this.rootDirectory = directory;
        this.currentDirectory = directory;

        rootFile = new File(rootDirectory);

        File[] files = rootFile.listFiles();
        if (files == null) return;

        for (File file : files) {
            if (!file.isFile()) continue;

            List<String> content = parseFile(file.getName());
            List<String> headers = parseHeaders(content);

            if (headers.size() < 2) continue;

            FileType type = parseFileExtension(headers.get(0));
            String version = parseFileVersion(headers.get(1));

            Metadata meta = generateMetadata(file.getPath(), type, version);

            filesMetadata.add(meta);
            fileMap.put(type, file.getPath());
        }
    }

    // HEADERS

    private List<String> parseHeaders(List<String> parsedFile) {
        List<String> headers = new ArrayList<>();

        for (String line : parsedFile) {
            if (line.startsWith("#")) {
                headers.add(line);
            }
        }

        return headers;
    }

    private FileType parseFileExtension(String header) {
        String value = header.replace("#type:", "").trim();

        return switch (value) {
            case "tx" -> FileType.TX;
            case "qson" -> FileType.QSON;
            case "htui" -> FileType.HTUI;
            case "img" -> FileType.IMG;
            case "ra" -> FileType.RA;
            default -> FileType.UNKNOWN;
        };
    }

    private String parseFileVersion(String header) {
        return header.replace("#version:", "").trim();
    }

    private Metadata generateMetadata(String filepath, FileType fileType, String fileVersion) {
        File file = new File(filepath);

        Metadata metadata = new Metadata(filepath);
        metadata.setFile(file);
        metadata.setFileType(fileType);
        metadata.setVersion(fileVersion);

        return metadata;
    }

    // FILES

    public List<String> parseFile(String filename) {
        List<String> lines = new ArrayList<>();

        File file = resolveFile(filename);

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;

            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }

        } catch (IOException e) {
            throw new RuntimeException("Error reading file: " + filename, e);
        }

        return lines;
    }

    public void writeToFile(String fileName, String data) throws IOException {
        File file = resolveFile(fileName);

        try (FileWriter writer = new FileWriter(file, false)) {
            writer.write(data);
        }
    }

    public void writeToFile(String fileName, List<String> data) throws IOException {
        File file = resolveFile(fileName);

        try (FileWriter writer = new FileWriter(file, false)) {
            for (String line : data) {
                writer.write(line + System.lineSeparator());
            }
        }
    }

    public void makeDirectory(String name) throws IOException {
        Path dir = Path.of(currentDirectory, name);
        Files.createDirectories(dir);
    }

    public void makeDirectory(String name, String path) throws IOException {
        Path dir = Path.of(path, name);
        Files.createDirectories(dir);
    }

    public void newFile(String name) {
        try {
            File file = new File(currentDirectory, name);
            file.createNewFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void newFile(String name, String path) {
        try {
            File file = new File(path, name);
            file.createNewFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public List<String> getAllFilesInDirectory() {
        return getAllFilesInDirectory(rootFile);
    }

    public List<String> getAllFilesInDirectory(File dir) {
        List<String> list = new ArrayList<>();

        File[] files = dir.listFiles();
        if (files == null) return list;

        for (File file : files) {
            list.add(file.getName());
        }

        return list;
    }

    public void deleteFile(String path) throws IOException {
        Files.deleteIfExists(Path.of(path));
    }
    public void moveFile(String source, String target) throws IOException {
        Files.move(Path.of(source), Path.of(target), StandardCopyOption.REPLACE_EXISTING);
    }
    public void copyFile(String source, String target) throws IOException {
        Files.copy(Path.of(source), Path.of(target), StandardCopyOption.REPLACE_EXISTING);
    }

    // GENERAL

    public void setCurrentDirectory(String dir) {
        this.currentDirectory = dir;
    }

    private File resolveFile(String filename) {
        File direct = new File(filename);
        if (direct.exists() || filename.contains("/") || filename.contains("\\")) {
            return direct;
        }
        return new File(currentDirectory, filename);
    }
}