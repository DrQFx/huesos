package qf.huesos.apps;

import qf.huesos.kernel.Screen;
import qf.huesos.kernel.SysCall;
import qf.huesos.kernel.graphics.TUI.Button;
import qf.huesos.kernel.graphics.TUI.Label;
import qf.huesos.kernel.graphics.TUI.SelectMenu;
import qf.huesos.kernel.graphics.TUI.TUIElement;
import qf.huesos.kernel.graphics.TUI.TextField;
import qf.huesos.kernel.graphics.TerminalApplication;
import qf.huesos.kernel.provider.ImageProvider;
import qf.huesos.kernel.provider.image.Pixel;
import qf.huesos.kernel.scheduler.Process;
import qf.huesos.kernel.scheduler.RunnableApplication;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ImageViewer extends TerminalApplication implements RunnableApplication {

    private static final int IMAGE_WIDTH = 32;
    private static final int IMAGE_HEIGHT = 24;
    private static final int CELL_SIZE = 16;
    private static final int EMPTY = -1;

    private final String uiPath = "fs/system/apps/imviewer/general.htui";
    private final int bg = 0x202020;
    private final SysCall call = new SysCall();
    private final int[][] pixels = new int[IMAGE_HEIGHT][IMAGE_WIDTH];

    private long PROCESS_ID;
    private String filePath;
    private String name = "Image Editor";
    private int selectedColor = 0x111111;
    private boolean erasing = false;

    private Label fileLabel;
    private Label toolLabel;
    private Label statusLabel;
    private TextField pathField;

    public ImageViewer(Screen screen, String path) throws Exception {
        super(screen);
        this.filePath = path == null || path.trim().isEmpty() ? "fs/system/sys/boot/img/loading.img" : path.trim();
        setPath(uiPath);
        setBgColor(bg);
        clearPixels();
        start();
    }

    @Override
    protected void onUiLoaded() {
        fileLabel = (Label) TUIElementsByID.get("file_label");
        toolLabel = (Label) TUIElementsByID.get("tool_label");
        statusLabel = (Label) TUIElementsByID.get("status_label");
        pathField = (TextField) TUIElementsByID.get("path_field");
        pathField.setLines(List.of(filePath));

        pathField.onClick = () -> {
          focusTextField(pathField);
        };

        PixelCanvas canvas = new PixelCanvas(screen);
        canvas.id = "image_canvas";
        canvas.x = 148;
        canvas.y = 82;
        canvas.width = IMAGE_WIDTH * CELL_SIZE;
        canvas.height = IMAGE_HEIGHT * CELL_SIZE;
        canvas.color = 0x181818;
        add(canvas);

        bindButtons();
        loadImage();
    }

    private void bindButtons() {
        SelectMenu fileMenu = (SelectMenu) TUIElementsByID.get("file_menu");
        SelectMenu optionsMenu = (SelectMenu) TUIElementsByID.get("options_menu");

        Button reloadButton = (Button) TUIElementsByID.get("file_button_open");
        Button saveButton = (Button) TUIElementsByID.get("file_button_save");
        Button saveAsButton = (Button) TUIElementsByID.get("file_button_save_as");
        Button clearButton = (Button) TUIElementsByID.get("file_button_close");
        Button exitButton = (Button) TUIElementsByID.get("file_button_exit");
        Button helpButton = (Button) TUIElementsByID.get("help_button_help");

        fileMenu.addItem(reloadButton);
        fileMenu.addItem(saveButton);
        fileMenu.addItem(saveAsButton);
        fileMenu.addItem(clearButton);
        fileMenu.addItem(exitButton);
        optionsMenu.addItem(helpButton);

        reloadButton.onClick = () -> {
            filePath = getPathFromField();
            loadImage();
        };
        saveButton.onClick = this::saveImage;
        saveAsButton.onClick = () -> {
            filePath = getPathFromField();
            saveImage();
            updateFileLabel();
        };
        clearButton.onClick = () -> {
            clearPixels();
            setStatus("Canvas cleared");
        };
        exitButton.onClick = () -> {
            stop();
            call.killProcess(PROCESS_ID);
        };
        helpButton.onClick = () -> setStatus("Enter paints. B brush, E erase, C clear, S save, 1-5 colors");

        bindTool("tool_brush", false);
        bindTool("tool_erase", true);
        bindColor("color_black", 0x111111);
        bindColor("color_white", 0xEEEEEE);
        bindColor("color_red", 0xB33939);
        bindColor("color_green", 0x2E8B57);
        bindColor("color_blue", 0x3267B8);
    }

    private void bindTool(String id, boolean eraseMode) {
        Button button = (Button) TUIElementsByID.get(id);
        button.onClick = () -> {
            erasing = eraseMode;
            updateToolLabel();
        };
    }

    private void bindColor(String id, int color) {
        Button button = (Button) TUIElementsByID.get(id);
        button.onClick = () -> {
            selectedColor = color;
            erasing = false;
            updateToolLabel();
        };
    }

    private void loadImage() {
        ImageProvider provider = new ImageProvider();

        filePath = getPathFromField();
        clearPixels();

        try {
            for (Pixel pixel : provider.parseImage(filePath)) {
                int x = pixel.getX();
                int y = pixel.getY();
                int color = pixel.getColor();

                if (isInsideImage(x, y)) {
                    pixels[y][x] = color;
                }
            }

            updateFileLabel();
            setStatus("Loaded " + filePath);
        } catch (Exception e) {
            updateFileLabel();
            setStatus("Cannot load " + filePath + ": " + e.getMessage());
        }
    }

    private void saveImage() throws Exception {
        filePath = getPathFromField();

        if (filePath == null || filePath.trim().isEmpty()) {
            filePath = "fs/test.img";
        }

        List<String> lines = new ArrayList<>();
        lines.add("#type:img");
        lines.add("#version:1");

        for (int y = 0; y < IMAGE_HEIGHT; y++) {
            for (int x = 0; x < IMAGE_WIDTH; x++) {
                if (pixels[y][x] != EMPTY) {
                    lines.add(x + "/" + y + "/" + toHex(pixels[y][x]));
                }
            }
        }

        call.writeToFile(filePath, lines);
        if (pathField != null) {
            pathField.setLines(List.of(filePath));
        }
        setStatus("Saved " + filePath);
    }

    private void clearPixels() {
        for (int[] row : pixels) {
            Arrays.fill(row, EMPTY);
        }
    }

    private void paintAtCursor(PixelCanvas canvas) {
        int cellX = (cursorX - canvas.x) / CELL_SIZE;
        int cellY = (cursorY - canvas.y) / CELL_SIZE;

        if (!isInsideImage(cellX, cellY)) {
            setStatus("Cursor is outside the canvas");
            return;
        }

        pixels[cellY][cellX] = erasing ? EMPTY : selectedColor;
        setStatus((erasing ? "Erased " : "Painted ") + cellX + "," + cellY);
    }

    private boolean isInsideImage(int x, int y) {
        return x >= 0 && x < IMAGE_WIDTH && y >= 0 && y < IMAGE_HEIGHT;
    }

    private void updateFileLabel() {
        if (fileLabel != null) {
            fileLabel.setText(filePath);
        }
        if (pathField != null) {
            pathField.setLines(List.of(filePath));
        }
    }

    private void updateToolLabel() {
        if (toolLabel != null) {
            toolLabel.setText(erasing ? "Tool: erase" : "Tool: brush " + toHex(selectedColor));
        }
    }

    private void setStatus(String message) {
        if (statusLabel != null) {
            statusLabel.setText(message);
        }
    }

    private String toHex(int color) {
        return String.format("0x%06X", color & 0xFFFFFF);
    }

    private String getPathFromField() {
        if (pathField == null) {
            return filePath;
        }

        String path = pathField.getText().trim();
        return path.isEmpty() ? filePath : path;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (getFocusedTextField() != null) {
            super.keyPressed(e);
            return;
        }

        switch (e.getKeyCode()) {
            case KeyEvent.VK_B -> {
                erasing = false;
                updateToolLabel();
                return;
            }
            case KeyEvent.VK_E -> {
                erasing = true;
                updateToolLabel();
                return;
            }
            case KeyEvent.VK_C -> {
                clearPixels();
                setStatus("Canvas cleared");
                return;
            }
            case KeyEvent.VK_S -> {
                try {
                    saveImage();
                } catch (Exception ex) {
                    setStatus("Cannot save " + filePath + ": " + ex.getMessage());
                }
                return;
            }
            case KeyEvent.VK_1 -> selectedColor = 0x111111;
            case KeyEvent.VK_2 -> selectedColor = 0xEEEEEE;
            case KeyEvent.VK_3 -> selectedColor = 0xB33939;
            case KeyEvent.VK_4 -> selectedColor = 0x2E8B57;
            case KeyEvent.VK_5 -> selectedColor = 0x3267B8;
            default -> {
                super.keyPressed(e);
                return;
            }
        }

        erasing = false;
        updateToolLabel();
    }

    @Override
    public void step(Screen screen, Process process) {
        if (PROCESS_ID == 0) {
            PROCESS_ID = process.getId();
        }
    }

    @Override
    public boolean isFinished() {
        return false;
    }

    @Override
    public void onStop() {
        stop();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    private class PixelCanvas extends TUIElement {
        PixelCanvas(Screen screen) {
            super(screen);
        }

        @Override
        public void click() {
            paintAtCursor(this);
        }

        @Override
        public void draw() {
            ImageViewer.this.screen.fillRect(x, y, width, height, color);

            for (int py = 0; py < IMAGE_HEIGHT; py++) {
                for (int px = 0; px < IMAGE_WIDTH; px++) {
                    int drawX = x + px * CELL_SIZE;
                    int drawY = y + py * CELL_SIZE;
                    int cellColor = pixels[py][px] == EMPTY ? 0x242424 : pixels[py][px];

                    ImageViewer.this.screen.fillRect(drawX + 1, drawY + 1, CELL_SIZE - 2, CELL_SIZE - 2, cellColor);
                }
            }
        }
    }
}
