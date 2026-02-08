package qf.huesos.kernel.graphics;

import qf.huesos.kernel.Screen;
import qf.huesos.kernel.graphics.TUI.Button;
import qf.huesos.kernel.graphics.TUI.CheckBox;
import qf.huesos.kernel.graphics.TUI.Label;
import qf.huesos.kernel.graphics.TUI.TUIElement;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class TerminalApplication extends TerminalUserInterface {
    String path;

    TUIElement current;
    List<String> lines;

    public TerminalApplication(Screen screen) throws IOException {
        super(screen);
    }

    public void load() throws Exception {
        lines = Files.readAllLines(Path.of(path));

        for (String line : lines) {
            line = line.trim();

            if (line.isEmpty()) {
                continue;
            }

            if (line.endsWith("{")) {
                String type = line.replace("{", "").trim();

                current = switch (type) {
                    case "Button" -> new Button(screen);
                    case "Label" -> new Label(screen);
                    case "Checkbox" -> new CheckBox(screen);
                    default -> throw new RuntimeException("Unknown UI type: " + type);
                };

                continue;
            }

            if (line.equals("}")) {
                add(current);
                current = null;
                continue;
            }

            String[] parts = line.split(":", 2);
            String key = parts[0].trim();
            String value = parts[1].trim().replace("\"", "");

            applyProperty(current, key, value);
        }
    }

    @Override
    public void start() throws Exception {
        load();
        onUiLoaded();
        super.start();
    }

    protected void onUiLoaded() {}

    void applyProperty(TUIElement element, String key, String value) throws Exception {
        switch (key) {
            case "x" -> element.x = Integer.parseInt(value);
            case "y" -> element.y = Integer.parseInt(value);
            case "width" -> element.width = Integer.parseInt(value);
            case "height" -> element.height = Integer.parseInt(value);
            case "color" -> element.color = parseColor(value);
            case "id" -> {
                element.id = value;
                TUIElementsByID.put(element.id, element);
            }

            case "hover" -> {
                if (element instanceof Button) {
                    element.rgbHover = parseColor(value);
                } else {
                    throw new Exception("The element is not a button");
                }
            }
            case "click-color" -> {
                if (element instanceof Button) {
                    element.rgbClick = parseColor(value);
                } else {
                    throw new Exception("The element is not a button");
                }
            }

            case "text" -> {
                if (element instanceof Label label) label.text = value;
                if (element instanceof Button button) button.setText(value);
            }

            case "checked" -> {
                if (element instanceof CheckBox checkBox)
                    checkBox.isClicked = Boolean.parseBoolean(value);
            }
        }

        if (element.id == null) {
            throw new Exception("Element without id");
        }
    }

    int parseColor(String value) {
        value = value.trim().toLowerCase();

        if (value.startsWith("0x")) {
            return Integer.parseUnsignedInt(value.substring(2), 16);
        }

        return Integer.parseInt(value);
    }

    public void setPath(String path) {
        this.path = path;
    }
}
