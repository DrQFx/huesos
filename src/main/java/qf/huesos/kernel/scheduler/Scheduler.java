package qf.huesos.kernel.scheduler;

import qf.huesos.kernel.Kernel;
import qf.huesos.kernel.Screen;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

public class Scheduler {
    public volatile long SYS_TIME;

    private final long TIME_QUANTUM_MS = 10;
    private final long TICK_MS = 1;

    private final Map<Long, Process> processesByID = new ConcurrentHashMap<>();
    private final Deque<Process> readyQueue = new ConcurrentLinkedDeque<>();
    private final List<Process> waitingQueue = Collections.synchronizedList(new ArrayList<>());

    private final Kernel kernel;
    private final Screen screen;

    private Process currentProcess;
    private boolean running = false;
    private Thread schedulerThread;

    public Scheduler(Kernel kernel, Screen screen) {
        this.kernel = kernel;
        this.screen = screen;
    }

    public void start() {
        if (running) {
            return;
        }

        running = true;

        schedulerThread = new Thread(() -> {
            while (running) {
                try {
                    tick();
                    Thread.sleep(TICK_MS);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        schedulerThread.setDaemon(true);
        schedulerThread.start();
    }

    public void stop() {
        running = false;
    }

    private void tick() throws Exception {
        SYS_TIME = System.currentTimeMillis();

        wakeUpWaitingProcesses();

        if (currentProcess == null) {
            scheduleNext();
        }

        if (currentProcess == null) {
            return;
        }

        currentProcess.step(screen);

        if (currentProcess.isFinished()) {
            terminateCurrentProcess();
            return;
        }

        if (currentProcess.getStatus() == Status.WAITING) {
            moveCurrentToWaiting();
            return;
        }

        if (currentProcess.getStatus() == Status.TERMINATED) {
            terminateCurrentProcess();
            return;
        }

        if (SYS_TIME - currentProcess.getQuantumStartTime() >= TIME_QUANTUM_MS) {
            preemptCurrentProcess();
        }
    }

    private void scheduleNext() {
        Process next = readyQueue.pollFirst();
        if (next == null) {
            currentProcess = null;
            return;
        }

        currentProcess = next;
        currentProcess.setStatus(Status.RUNNING);
        currentProcess.setQuantumStartTime(SYS_TIME);
    }

    private void wakeUpWaitingProcesses() {
        Iterator<Process> it = waitingQueue.iterator();

        while (it.hasNext()) {
            Process process = it.next();

            if (process.isReadyToWake(SYS_TIME)) {
                process.setStatus(Status.READY);
                readyQueue.addLast(process);
                it.remove();
            }
        }
    }

    private void preemptCurrentProcess() {
        if (currentProcess == null) {
            return;
        }

        currentProcess.setStatus(Status.READY);
        readyQueue.addLast(currentProcess);
        currentProcess = null;
    }

    private void moveCurrentToWaiting() {
        if (currentProcess == null) {
            return;
        }

        waitingQueue.add(currentProcess);
        currentProcess = null;
    }

    private void terminateCurrentProcess() {
        if (currentProcess == null) {
            return;
        }

        Process process = currentProcess;
        process.getApplication().onStop();
        process.kill();

        readyQueue.remove(process);
        waitingQueue.remove(process);
        processesByID.remove(process.getId());

        currentProcess = null;
        kernel.onProcessKilled(process);
    }

    public long runApplication(RunnableApplication app) {
        Process process = new Process();
        process.setApplication(app);
        process.setName(app.getName());
        process.setStatus(Status.READY);

        processesByID.put(process.getId(), process);
        readyQueue.addLast(process);

        return process.getId();
    }

    public void killProcess(long id) {
        Process process = processesByID.get(id);
        if (process == null) return;

        process.getApplication().onStop();
        process.kill();

        readyQueue.remove(process);
        waitingQueue.remove(process);

        if (process == currentProcess) {
            currentProcess = null;
        }

        processesByID.remove(id);
        kernel.onProcessKilled(process);
    }

    public void killCurrentProcess() {
        if (currentProcess != null) {
            killProcess(currentProcess.getId());
        }
    }

    public Map<Long, Process> getProcessesByID() {
        return processesByID;
    }

    public Process getCurrentProcess() {
        return currentProcess;
    }

    public List<Process> getReadyQueueSnapshot() {
        return new ArrayList<>(readyQueue);
    }

    public List<Process> getWaitingQueueSnapshot() {
        return new ArrayList<>(waitingQueue);
    }
}
