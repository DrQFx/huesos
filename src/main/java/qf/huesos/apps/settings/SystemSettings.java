package qf.huesos.apps.settings;

import qf.huesos.kernel.provider.QsonProvider;

public class SystemSettings {
    private final QsonProvider qson = new QsonProvider();
    private final String path = "fs/system/apps/settings/general.htui";
    private final String settingsPath = "fs/system/sys/settings.qson";

    public static String TERMINAL_COLOR;
    public static String TEST_VALUE;

    private final String DEFAULT_TERMINAL_COLOR = "0x000000";

    public void loadSettings() throws Exception {
        TEST_VALUE = qson.parseStatement(settingsPath, "test");
        TERMINAL_COLOR = (qson.parseStatement(settingsPath, "terminal-color"));

        if (!TERMINAL_COLOR.startsWith("0x")) {
            TERMINAL_COLOR = DEFAULT_TERMINAL_COLOR;
        }
    }
}
