package qf.huesos.kernel;

import qf.huesos.apps.Terminal;
import qf.huesos.apps.settings.SystemSettings;
import qf.huesos.kernel.provider.ImageProvider;
import qf.huesos.kernel.scheduler.Process;
import qf.huesos.kernel.scheduler.RunnableApplication;
import qf.huesos.kernel.scheduler.Scheduler;

public class Kernel {
    private static Kernel instance;
    private final Scheduler scheduler;
    private final Filesystem filesystem;
    private final SystemSettings settings;
    public Terminal terminal;

    private final String loadingImagePath = "fs/system/sys/boot/img/loading.img";

    public static final Screen screen = new Screen(800, 640);

    private final ImageProvider imageProvider;

    public Kernel() throws Exception {
        if (instance != null) {
            throw new IllegalStateException("Kernel already initialized");
        }
        instance = this;

        filesystem = new Filesystem("fs");

        imageProvider = new ImageProvider();

        scheduler = new Scheduler(this, screen);
        scheduler.start();

        imageProvider.drawImage(loadingImagePath, 0, 0, 800, 640);

        settings = new SystemSettings();
        settings.loadSettings();

        terminal = new Terminal(screen);
        runApplication(terminal);
    }

    public static Kernel getInstance() {
        if (instance == null) {
            throw new IllegalStateException("Kernel not initialized");
        }
        return instance;
    }

    public Filesystem getFilesystem() {
        return filesystem;
    }

    public Scheduler getScheduler() {
        return scheduler;
    }

    public SystemSettings getSettings() {
        return settings;
    }

    public long runApplication(RunnableApplication app) {
        return scheduler.runApplication(app);
    }

    public void onProcessKilled(Process killed) {
        if (terminal == null || killed.getApplication() == terminal) {
            return;
        }

        boolean hasOtherApps = scheduler.getProcessesByID().values().stream()
                .anyMatch(p -> p.getApplication() != terminal);

        if (!hasOtherApps) {
            terminal.setActive(true);
            terminal.start();
        }
    }
}
