package qf.huesos.apps;

import qf.huesos.kernel.Kernel;
import qf.huesos.kernel.SysCall;
import qf.huesos.kernel.scheduler.Process;
import qf.huesos.kernel.scheduler.RunnableApplication;
import qf.huesos.kernel.Screen;
import qf.huesos.kernel.graphics.TUI.Button;
import qf.huesos.kernel.graphics.TUI.Label;
import qf.huesos.kernel.graphics.TUI.SelectMenu;
import qf.huesos.kernel.graphics.TUI.TextField;
import qf.huesos.kernel.graphics.TerminalApplication;

import java.awt.event.KeyEvent;
import java.util.List;

public class TextEditor extends TerminalApplication implements RunnableApplication {
    private final String path = "fs/system/apps/ted/general.htui";
    private final int bg = 0x262626;

    private long PROCESS_ID;

    SysCall call = new SysCall();

    private String name;
    private String currentFile = "fs/test.tx";
    private TextField editorField;
    private TextField pathField;
    private Label statusLabel;

    public TextEditor(Screen screen) throws Exception {
        this(screen, "fs/test.tx");
    }

    public TextEditor(Screen screen, String initialFile) throws Exception {
        super(screen);
        this.currentFile = initialFile == null || initialFile.trim().isEmpty() ? "fs/test.tx" : initialFile.trim();
        setPath(path);
        setBgColor(bg);
        setName("Text Editor");
        start();
    }

    @Override
    protected void onUiLoaded() {
        editorField = ((TextField) TUIElementsByID.get("editor_field"));
        pathField = ((TextField) TUIElementsByID.get("path_field"));
        statusLabel = ((Label) TUIElementsByID.get("status_label"));
        pathField.setLines(List.of(currentFile));
        editorField.setFocused(true);
        openCurrentFile();
        bindButtons();
    }

    private void bindButtons() {
        SelectMenu fileMenu = ((SelectMenu) TUIElementsByID.get("file_menu"));
        SelectMenu optionsMenu = ((SelectMenu) TUIElementsByID.get("options_menu"));

        Button openButton = ((Button) TUIElementsByID.get("file_button_open"));
        Button closeButton = ((Button) TUIElementsByID.get("file_button_close"));
        Button saveButton = ((Button) TUIElementsByID.get("file_button_save"));
        Button saveAsButton = ((Button) TUIElementsByID.get("file_button_save_as"));
        Button exitButton = ((Button) TUIElementsByID.get("file_button_exit"));

        Button helpButton = ((Button) TUIElementsByID.get("help_button_help"));


        fileMenu.addItem(openButton);
        fileMenu.addItem(closeButton);
        fileMenu.addItem(saveButton);
        fileMenu.addItem(saveAsButton);
        fileMenu.addItem(exitButton);

        optionsMenu.addItem(helpButton);

        openButton.onClick = this::openCurrentFile;
        closeButton.onClick = () -> {
            editorField.clearText();
            setStatus("Document closed");
        };
        saveButton.onClick = this::saveCurrentFile;
        saveAsButton.onClick = () -> {
            currentFile = getPathFromField();
            saveCurrentFile();
        };
        helpButton.onClick = () -> setStatus("Ctrl+O open, Ctrl+S save, Ctrl+N clear, F6 switch fields");
        exitButton.onClick = () -> {
            stop();
            call.killProcess(PROCESS_ID);
        };
    }

    private void openCurrentFile() {
        currentFile = getPathFromField();

        try {
            editorField.setLines(call.parseFile(currentFile));
            pathField.setLines(List.of(currentFile));
            setStatus("Opened " + currentFile);
        } catch (Exception e) {
            editorField.clearText();
            setStatus("Cannot open " + currentFile + ": " + e.getMessage());
        }
    }

    private void saveCurrentFile() {
        currentFile = getPathFromField();

        try {
            call.writeToFile(currentFile, editorField.getLines());
            pathField.setLines(List.of(currentFile));
            setStatus("Saved " + currentFile);
        } catch (Exception e) {
            setStatus("Cannot save " + currentFile + ": " + e.getMessage());
        }
    }

    private String getPathFromField() {
        String path = pathField.getText().trim();
        if (path.isEmpty()) {
            return currentFile;
        }

        return path;
    }

    private void setStatus(String message) {
        if (statusLabel != null) {
            statusLabel.setText(message);
        }
    }

    public void setProcessID(long id) {
        this.PROCESS_ID = id;
    }

    @Override
    public void step(Screen screen, Process process) throws Exception {
        if (PROCESS_ID == 0) {
            PROCESS_ID = process.getId();
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.isControlDown()) {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_O -> {
                    openCurrentFile();
                    return;
                }
                case KeyEvent.VK_S -> {
                    saveCurrentFile();
                    return;
                }
                case KeyEvent.VK_N -> {
                    editorField.clearText();
                    setStatus("New empty document");
                    return;
                }
            }
        }

        if (e.getKeyCode() == KeyEvent.VK_F6) {
            focusNextTextField();
            return;
        }

        super.keyPressed(e);
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
        return this.name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }
}
