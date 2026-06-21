package qf.huesos.kernel.graphics;

import qf.huesos.kernel.Screen;
import qf.huesos.kernel.graphics.TUI.*;

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

            if (line.startsWith("#")) continue;

            if (line.endsWith("{")) {
                String type = line.replace("{", "").trim();

                current = switch (type) {
                    case "Button" -> new Button(screen);
                    case "Label" -> new Label(screen);
                    case "Checkbox" -> new CheckBox(screen);
                    case "Rectangle" -> new Rectangle(screen);
                    case "SelectMenu" -> new SelectMenu(screen);
                    case "TextField" -> new TextField(screen);
                    case "Canvas" -> new Canvas(screen);
                    default -> throw new RuntimeException("Unknown UI type: " + type);
                };

                continue;
            }

            if (line.equals("}")) {
                if (current != null) {
                    if (current.id == null) {
                        throw new Exception("Element without id");
                    }
                    add(current);
                    current = null;
                }
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

    protected void onUiLoaded() throws Exception {}

    void applyProperty(TUIElement element, String key, String value) throws Exception {
        switch (key) {
            case "x" -> element.x = Integer.parseInt(value);
            case "y" -> element.y = Integer.parseInt(value);
            case "width" -> element.width = Integer.parseInt(value);
            case "height" -> element.height = Integer.parseInt(value);
            case "color" -> element.color = parseColor(value);
            case "text-color" -> {
                if (element instanceof Button) {
                    ((Button) element).setTextColor(parseColor(value));
                } else if (element instanceof SelectMenu) {
                    ((SelectMenu) element).setTextColor(parseColor(value));
                } else if (element instanceof TextField) {
                    ((TextField) element).setTextColor(parseColor(value));
                } else {
                    throw new Exception("The element does not support text color");
                }
            }

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

            case "opened-width" -> {
                if (element instanceof SelectMenu) {
                    ((SelectMenu) element).setOpenedWidth(Integer.parseInt(value));
                } else {
                    throw new Exception("The element is not a button");
                }
            }

            case "opened-height" -> {
                if (element instanceof SelectMenu) {
                    ((SelectMenu) element).setOpenedHeight(Integer.parseInt(value));;
                } else {
                    throw new Exception("The element is not a button");
                }
            }

            case "text" -> {
                if (element instanceof Label label) label.text = value;
                if (element instanceof Button button) button.setText(value);
                if (element instanceof SelectMenu selectMenu) selectMenu.setText(value);
            }

            case "checked" -> {
                if (element instanceof CheckBox checkBox)
                    checkBox.isClicked = Boolean.parseBoolean(value);
            }

            case "single-line" -> {
                if (element instanceof TextField textField) {
                    textField.setSingleLine(Boolean.parseBoolean(value));
                } else {
                    throw new Exception("The element is not a text field");
                }
            }
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
