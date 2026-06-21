package qf.huesos.kernel.scheduler;

import qf.huesos.kernel.Kernel;
import qf.huesos.kernel.Screen;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class Process {
    private static final AtomicLong COUNTER = new AtomicLong(1);

    private final long id;
    private int priority;
    private RunnableApplication application;

    private long quantumStartTime;
    private long wakeUpTime;

    private final List<String> tags = new ArrayList<>();
    private Status status;

    private String name;

    public Process() {
        this.id = COUNTER.getAndIncrement();
        this.status = Status.NEW;
    }

    public void step(Screen screen) throws Exception {
        if (application == null) {
            throw new IllegalStateException("Application is not set for process " + id);
        }

        if (status == Status.TERMINATED) {
            return;
        }

        status = Status.RUNNING;
        application.step(screen, this);
    }

    public boolean isFinished() {
        return application != null && application.isFinished();
    }

    public void kill() {
        status = Status.TERMINATED;
    }

    public void waitFor(long millis, long currentTime) {
        this.status = Status.WAITING;
        this.wakeUpTime = currentTime + millis;
    }

    public boolean isReadyToWake(long currentTime) {
        return status == Status.WAITING && currentTime >= wakeUpTime;
    }

    public long getId() {
        return id;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public RunnableApplication getApplication() {
        return application;
    }

    public void setApplication(RunnableApplication application) {
        this.application = application;
    }

    public long getQuantumStartTime() {
        return quantumStartTime;
    }

    public void setQuantumStartTime(long quantumStartTime) {
        this.quantumStartTime = quantumStartTime;
    }

    public long getWakeUpTime() {
        return wakeUpTime;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public List<String> getTags() {
        return tags;
    }

    public void addTag(String tag) {
        tags.add(tag);
    }

    public void deleteTag(String tag) {
        tags.remove(tag);
    }

    @Override
    public String toString() {
        return "Process{id=" + id +
                ", status=" + status +
                ", priority=" + priority +
                ", quantumStartTime=" + quantumStartTime +
                ", wakeUpTime=" + wakeUpTime +
                '}';
    }
}
