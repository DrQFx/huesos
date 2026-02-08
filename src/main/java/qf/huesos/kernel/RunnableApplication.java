package qf.huesos.kernel;

public interface RunnableApplication {
    default void run(Screen screen) throws Exception {}
}
