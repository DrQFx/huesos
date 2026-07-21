package qf.huesos.kernel.provider;

import qf.huesos.kernel.SysCall;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QsonProvider {
    SysCall call = new SysCall();
    public QsonProvider() {

    }

    public Map<String, String> parse(String path) throws Exception {
        Map<String, String> result = new HashMap<>();
        List<String> lines = call.parseFile(path);

        for (String line : lines) {
            String[] parts = line.trim().split("=", 2);
            if (parts.length == 2) {
                result.put(parts[0], parts[1]);
            }
        }

        return result;
    }

    public String parseStatement(String path, String key) throws Exception {
        List<String> lines = call.parseFile(path);

        for (String line : lines) {
            String[] parts = line.trim().split("=", 2);

            if (parts.length == 2 && parts[0].equals(key)) {
                return parts[1];
            }
        }

        return null;
    }

    public void writeStatement(String path, String key, String value) throws Exception {
        List<String> lines = call.parseFile(path);
        boolean found = false;

        for (int i = 0; i < lines.size(); i++) {
            String[] parts = lines.get(i).trim().split("=", 2);

            if (parts.length == 2 && parts[0].equals(key)) {
                lines.set(i, key + "=" + value);
                found = true;
                break;
            }
        }

        if (!found) {
            lines.add(key + "=" + value);
        }

        call.writeToFile(path, lines);
    }
}
