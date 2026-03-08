package qf.huesos.apps;

import qf.huesos.kernel.Kernel;
import qf.huesos.kernel.RunnableApplication;
import qf.huesos.kernel.Screen;
import qf.huesos.kernel.graphics.TUI.Button;
import qf.huesos.kernel.graphics.TUI.CheckBox;
import qf.huesos.kernel.graphics.TerminalApplication;
import qf.huesos.provider.QsonHandler;

import java.io.IOException;
import java.util.Map;

public class Settings extends TerminalApplication implements RunnableApplication {

    private final String path = "fs/system/apps/settings/main.htui";
    private final String settingsPath = "fs/system/sys/settings.qson";
    private final int bg = 0x121212;

    private final QsonHandler qson = new QsonHandler();

    public Settings(Screen screen) throws Exception {
        super(screen);
        setPath(path);
        setBgColor(bg);
        start();
    }

    @Override
    protected void onUiLoaded() throws IOException {
        bindButtons();
        parseSettings();
        bindUiLogic();
    }

    private void parseSettings() {
        String value = qson.parseStatement(settingsPath, "test");

        CheckBox box = (CheckBox) TUIElementsByID.get("test_box");
        box.isClicked = "true".equals(value);
    }

    private void bindUiLogic() {

        CheckBox box = (CheckBox) TUIElementsByID.get("test_box");

        box.onClick = () -> {
            qson.writeStatement(settingsPath, "test", box.isClicked ? "true" : "false");
        };
    }

    private void bindButtons() {
        ((Button) TUIElementsByID.get("exit_button")).onClick = () -> {
            System.exit(0);
        };
    }
}
