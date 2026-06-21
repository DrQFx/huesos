package qf.huesos.kernel.scheduler;

import qf.huesos.kernel.Screen;

public interface RunnableApplication {
    void step(Screen screen, Process process) throws Exception;
    boolean isFinished();

    default void setName(String name) {}
    default void onStop() {}

    String getName();
}
