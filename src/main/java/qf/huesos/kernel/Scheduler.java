package qf.huesos.kernel;

public class Scheduler {

    public Scheduler() {
    }

    public void runApplication(RunnableApplication app, Screen screen) {
        Thread thread = new Thread(() -> {
            try {
                app.run(screen);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        thread.setDaemon(true);
        thread.start();
    }
}
