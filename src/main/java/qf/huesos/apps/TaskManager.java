package qf.huesos.apps;

import qf.huesos.kernel.Kernel;
import qf.huesos.kernel.Screen;
import qf.huesos.kernel.scheduler.Process;
import qf.huesos.kernel.scheduler.RunnableApplication;
import qf.huesos.kernel.scheduler.Scheduler;
import qf.huesos.kernel.scheduler.Status;

import javax.lang.model.element.Element;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class TaskManager extends Terminal implements RunnableApplication{
    private final Scheduler scheduler;
    private final String TASK_PROMPT = "TaskManager> ";

    private long lastRedrawTime = 0;
    private static final long REDRAW_INTERVAL = 300;
    private long PROCESS_ID;

    public String name;

    public TaskManager(Screen screen) throws Exception {
        super(screen);
        setName("Task Manager");
        this.scheduler = Kernel.getInstance().getScheduler();
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void start() {
        screen.clear(screen.COLOR_BLACK);
        cursorX = 0;
        cursorY = 0;
        println("HuesOS Task Manager v1.0", screen.COLOR_WHITE);
        println("Type 'help' for commands.", screen.COLOR_WHITE);
        newLine();
        print(TASK_PROMPT, screen.COLOR_WHITE);
        screen.repaintCanvas();
    }

    @Override
    public void step(Screen screen, Process process) throws Exception {
        if (!started) {
            start();
            started = true;
        }

        if (PROCESS_ID == 0) {
            PROCESS_ID = process.getId();
        }

        long now = System.currentTimeMillis();
        if (now - lastRedrawTime >= REDRAW_INTERVAL) {
            redraw();
            lastRedrawTime = now;
        }
    }

    private void redraw() {
        screen.clear(screen.COLOR_BLACK);
        cursorX = 0;
        cursorY = 0;

        println("HuesOS Task Manager v1.0", screen.COLOR_WHITE);
        println("Commands: ps, kill <pid>, info <pid>, clear, help", screen.COLOR_WHITE);
        newLine();

        List<Process> processes = getProcesses();

        if (processes.isEmpty()) {
            println("No processes found.", screen.COLOR_YELLOW);
        } else {
            println("PID   | NAME     | STATUS      | PRIORITY", screen.COLOR_CYAN);
            println("-----------------------------------------", screen.COLOR_CYAN);

            for (Process p : processes) {
                println(formatShort(p), colorByStatus(p.getStatus()));
            }
        }

        newLine();
        print(TASK_PROMPT + commandInput, screen.COLOR_WHITE);
        screen.repaintCanvas();
    }

    private List<Process> getProcesses() {
        Map<Long, Process> processMap = scheduler.getProcessesByID();
        List<Process> processes = new ArrayList<>(processMap.values());
        processes.sort(Comparator.comparingLong(Process::getId));
        return processes;
    }

    private String formatShort(Process p) {
        return String.format("%3d | %-11s | %-11s | %8d",
                p.getId(),
                p.getName(),
                p.getStatus(),
                p.getPriority());
    }

    private int colorByStatus(Status status) {
        switch (status) {
            case RUNNING:
                return screen.COLOR_GREEN;
            case WAITING:
                return screen.COLOR_YELLOW;
            case TERMINATED:
                return screen.COLOR_RED;
            case READY:
                return screen.COLOR_WHITE;
            default:
                return screen.COLOR_CYAN;
        }
    }

    @Override
    protected void executeCommand(String cmd) throws Exception {
        cmd = cmd.trim();
        if (cmd.isEmpty()) return;

        String[] parts = cmd.split("\\s+", 2);
        String command = parts[0].toLowerCase();
        String args = parts.length > 1 ? parts[1].trim() : "";

        switch (command) {
            case "help":
                help(args);
                break;

            case "ps":
            case "list":
                showProcesses();
                break;

            case "info":
                showProcessInfo(args);
                break;

            case "kill":
                killCommand(args);
                break;

            case "clear":
                screen.clear(screen.COLOR_BLACK);
                cursorX = 0;
                cursorY = 0;
                break;

            case "exit":
            case "quit":
                active = false;
                scheduler.killProcess(PROCESS_ID);
                break;

            default:
                println("Unknown command: " + command, screen.COLOR_RED);
        }
    }

    @Override
    protected void help(String args) {
        if (args.isBlank()) {
            println("Commands: help, ps, list, info <pid>, kill <pid>, clear", screen.COLOR_WHITE);
            return;
        }

        switch (args) {
            case "ps":
            case "list":
                println("Shows all running/known processes.", screen.COLOR_WHITE);
                break;
            case "info":
                println("Shows detailed info about a process.", screen.COLOR_WHITE);
                println("Usage: info <pid>", screen.COLOR_BLUE);
                break;
            case "kill":
                println("Terminates a process.", screen.COLOR_WHITE);
                println("Usage: kill <pid>", screen.COLOR_BLUE);
                break;
            case "clear":
                println("Clears the screen.", screen.COLOR_WHITE);
                break;
            default:
                println("Unknown command: " + args, screen.COLOR_RED);
        }
    }

    private void showProcesses() {
        List<Process> processes = getProcesses();

        if (processes.isEmpty()) {
            println("No processes found.", screen.COLOR_YELLOW);
            return;
        }

        println("PID   | NAME     | STATUS      | PRIORITY", screen.COLOR_CYAN);
        println("-------------------------------", screen.COLOR_CYAN);

        for (Process p : processes) {
            println(formatShort(p), colorByStatus(p.getStatus()));
        }
    }

    private void showProcessInfo(String args) {
        if (args.isEmpty()) {
            println("Usage: info <pid>", screen.COLOR_YELLOW);
            return;
        }

        long pid;
        try {
            pid = Long.parseLong(args);
        } catch (NumberFormatException e) {
            println("Invalid PID: " + args, screen.COLOR_RED);
            return;
        }

        Process process = scheduler.getProcessesByID().get(pid);
        if (process == null) {
            println("Process not found: " + pid, screen.COLOR_RED);
            return;
        }

        println(process.toString(), screen.COLOR_WHITE);
    }

    private void killCommand(String args) {
        if (args.isEmpty()) {
            println("Usage: kill <pid>", screen.COLOR_YELLOW);
            return;
        }

        long pid;
        try {
            pid = Long.parseLong(args);
        } catch (NumberFormatException e) {
            println("Invalid PID: " + args, screen.COLOR_RED);
            return;
        }


        Process process = scheduler.getProcessesByID().get(pid);
        if (process == null) {
            println("Process not found: " + pid, screen.COLOR_RED);
            return;
        }

        scheduler.killProcess(pid);
        println("Killed process " + pid, screen.COLOR_GREEN);
    }

    @Override
    public String getName() {
        return this.name;
    }
}
