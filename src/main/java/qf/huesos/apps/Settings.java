package qf.huesos.apps;

import qf.huesos.kernel.scheduler.Process;
import qf.huesos.kernel.scheduler.RunnableApplication;
import qf.huesos.kernel.Screen;
import qf.huesos.kernel.SysCall;
import qf.huesos.kernel.graphics.TUI.Button;
import qf.huesos.kernel.graphics.TUI.CheckBox;
import qf.huesos.kernel.graphics.TUI.Label;
import qf.huesos.kernel.graphics.TerminalApplication;
import qf.huesos.provider.QsonHandler;

import java.io.IOException;

public class Settings extends TerminalApplication implements RunnableApplication {

    private final String path = "fs/system/apps/settings/main.htui";
    private final String settingsPath = "fs/system/sys/settings.qson";
    private final int bg = 0x121212;

    private final QsonHandler qson = new QsonHandler();
    private final SysCall call = new SysCall();

    private String name;
    private long PROCESS_ID;
    private Label statusLabel;

    public Settings(Screen screen) throws Exception {
        super(screen);
        setPath(path);
        setBgColor(bg);
        setName("Settings");
        start();
    }

    @Override
    protected void onUiLoaded() throws Exception {
        statusLabel = (Label) TUIElementsByID.get("status_label");
        bindButtons();
        parseSettings();
        bindUiLogic();
    }

    private void parseSettings() throws Exception {
        String value = qson.parseStatement(settingsPath, "test");

        CheckBox box = (CheckBox) TUIElementsByID.get("test_box");
        box.isClicked = "true".equals(value);
    }

    private void bindUiLogic() {

        CheckBox box = (CheckBox) TUIElementsByID.get("test_box");

        box.onClick = () -> {
            try {
                qson.writeStatement(settingsPath, "test", box.isClicked ? "true" : "false");
                setStatus("Saved: test = " + box.isClicked);
            } catch (Exception e) {
                setStatus("Cannot save settings: " + e.getMessage());
            }
        };
    }

    private void bindButtons() {
        ((Button) TUIElementsByID.get("category_general")).onClick = () -> {
            setStatus("General settings selected");
        };

        ((Button) TUIElementsByID.get("exit_button")).onClick = () -> {
            stop();
            call.killProcess(PROCESS_ID);
        };
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
