package qf.huesos.apps.settings;

import qf.huesos.kernel.Kernel;
import qf.huesos.kernel.graphics.TUI.TextField;
import qf.huesos.kernel.scheduler.Process;
import qf.huesos.kernel.scheduler.RunnableApplication;
import qf.huesos.kernel.Screen;
import qf.huesos.kernel.SysCall;
import qf.huesos.kernel.graphics.TUI.Button;
import qf.huesos.kernel.graphics.TUI.CheckBox;
import qf.huesos.kernel.graphics.TUI.Label;
import qf.huesos.kernel.graphics.TerminalApplication;
import qf.huesos.kernel.provider.QsonProvider;

import java.util.List;

public class Settings extends TerminalApplication implements RunnableApplication {

    SystemSettings settings = new SystemSettings();

    private final String generalPath = "fs/system/apps/settings/general.htui";
    private final String systemVariablesPath = "fs/system/apps/settings/sys_variables.htui";
    private final String languagesPath = "fs/system/apps/settings/languages.htui";
    private final String settingsPath = "fs/system/sys/settings.qson";
    private final int bg = 0x121212;

    private final QsonProvider qson = new QsonProvider();
    private final SysCall call = new SysCall();

    private String name;
    private long PROCESS_ID;

    private Label statusLabel;
    private CheckBox test_box;
    private TextField terminalColorField;
    private Button category;

    private String currentCategory;

    public Settings(Screen screen) throws Exception {
        super(screen);
        setPath(generalPath);
        setBgColor(bg); 
        currentCategory = "category_general";
        setName("Settings");
        start();
    }

    @Override
    protected void onUiLoaded() throws Exception {
        statusLabel = (Label) TUIElementsByID.get("status_label");

        bindButtons();
        bindUiLogic();
    }

    private void bindUiLogic() {
        terminalColorField = (TextField) TUIElementsByID.get("terminal_color");
        test_box = (CheckBox) TUIElementsByID.get("test_box");

        category = (Button) TUIElementsByID.get(currentCategory);

        test_box.isClicked = "true".equals(SystemSettings.TEST_VALUE);

        terminalColorField.setLines(List.of(SystemSettings.TERMINAL_COLOR));
        terminalColorField.setTextColor(Kernel.screen.COLOR_WHITE);

        if (category != (Button) TUIElementsByID.get(currentCategory)) {
            category.setColor(0x4d4d4d);
        } else {
            category.setColor(0xadadad);
        }

        test_box.onClick = () -> {
            try {
                qson.writeStatement(settingsPath, "test", test_box.isClicked ? "true" : "false");
                setStatus("Saved: test = " + test_box.isClicked);
            } catch (Exception e) {
                setStatus("Cannot save settings: " + e.getMessage());
            }
        };
    }

    private void bindButtons() {
        ((Button) TUIElementsByID.get("apply_button")).onClick = () -> {
            applySettings();
            setStatus("Settings applied");
        };

        ((Button) TUIElementsByID.get("category_general")).onClick = () -> {
            currentCategory = "category_general";
            changeCategory("category_general");
            setStatus("General settings selected");
        };
        ((Button) TUIElementsByID.get("category_system_variables")).onClick = () -> {
            currentCategory = "category_system_variables";
            changeCategory("category_system_variables");
            setStatus("System Variables selected");
        };
        ((Button) TUIElementsByID.get("category_languages")).onClick = () -> {
            currentCategory = "category_languages";
            changeCategory("category_languages");
            setStatus("Languages category selected");
        };

        ((Button) TUIElementsByID.get("exit_button")).onClick = () -> {
            stop();
            call.killProcess(PROCESS_ID);
        };
    }

    private void changeCategory(String category) throws Exception {
        switch (category) {
            case "category_general":
                stop();
                setPath(generalPath);
                removeAll();
                start();
                break;
            case "category_system_variables":
                stop();
                setPath(systemVariablesPath);
                removeAll();
                start();
                break;
            case "category_languages":
                stop();
                setPath(languagesPath);
                removeAll();
                start();
                break;
            default:
                throw new Exception("Invalid category");
        }
    }

    private void applySettings() throws Exception {
        qson.writeStatement(settingsPath, "terminal-color", terminalColorField.getText());
        settings.loadSettings();

        setStatus("Settings applied");
    }

    private void setStatus(String message) {
        if (statusLabel != null) {
            statusLabel.setText(message);
        }
    }

    @Override
    public void step(Screen screen, Process process) throws Exception {
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
        return this.name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }
}
