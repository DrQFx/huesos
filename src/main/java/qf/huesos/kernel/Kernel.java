package qf.huesos.kernel;

import qf.huesos.apps.Terminal;

public class Kernel {
    private static Kernel instance;
    private final Scheduler scheduler;
    private final Filesystem filesystem;

    public static final Screen screen = new Screen(800, 640);


    public Kernel() {
        Terminal terminal = new Terminal(screen);
        scheduler = new Scheduler();
        filesystem = new Filesystem("fs");
    }

    public static Kernel getInstance() {
        if (instance == null) {
            instance = new Kernel();
        }
        return instance;
    }

    public Filesystem getFilesystem() {
        return filesystem;
    }

    public void runApplication(RunnableApplication app) {
        scheduler.runApplication(app, screen);
    }
}