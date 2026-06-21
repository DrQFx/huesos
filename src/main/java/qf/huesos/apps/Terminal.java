package qf.huesos.apps;

import qf.huesos.kernel.Kernel;
import qf.huesos.kernel.Screen;
import qf.huesos.kernel.SysCall;
import qf.huesos.kernel.scheduler.Process;
import qf.huesos.kernel.scheduler.RunnableApplication;
import qf.huesos.provider.RAHandler;

import java.io.File;
import java.util.List;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.util.List;

public class Terminal implements RunnableApplication, KeyListener {
    protected final Screen screen;
    protected SysCall call;
    protected RAHandler ra = new RAHandler();

    protected int cursorX = 0;
    protected int cursorY = 0;

    protected final int charWidth;
    protected final int lineHeight;

    protected boolean active = true;
    protected boolean started = false;

    protected StringBuilder commandInput = new StringBuilder();
    protected final String PROMPT = "> ";

    private String name;

    public Terminal(Screen screen) {
        this.screen = screen;
        this.call = new SysCall();

        this.charWidth = screen.getCharWidth();
        this.lineHeight = screen.getLineHeight();

        setName("Terminal");

        screen.addInputListener(this);
    }

    public void start() {
        screen.clear(screen.COLOR_BLACK);
        cursorX = 0;
        cursorY = 0;
        commandInput.setLength(0);
        println("HuesOS Terminal v1.0", screen.COLOR_WHITE);
        println("Type 'help' for commands.", screen.COLOR_WHITE);
        print(PROMPT, screen.COLOR_WHITE);
        screen.repaintCanvas();
    }

    public void print(String text, int rgb) {
        for (char c : text.toCharArray()) {
            drawChar(c, rgb);
        }
    }

    public void println(String text, int rgb) {
        print(text, rgb);
        newLine();
        screen.repaintCanvas();
    }

    protected void drawChar(char c, int rgb) {
        screen.drawText(String.valueOf(c), cursorX, cursorY, rgb);

        cursorX += charWidth;

        if (cursorX >= screen.getWidth() - charWidth) {
            newLine();
        }
    }

    protected void newLine() {
        cursorX = 0;
        cursorY += lineHeight;

        if (cursorY + lineHeight > screen.getHeight()) {
            screen.scrollUp(lineHeight, screen.COLOR_BLACK);
            cursorY -= lineHeight;
        }
    }

    protected void backspace() {
        if (commandInput.length() > 0) {
            commandInput.setLength(commandInput.length() - 1);

            cursorX -= charWidth;
            if (cursorX < 0) {
                cursorX = 0;
            }

            screen.fillRect(cursorX, cursorY, charWidth, lineHeight, screen.COLOR_BLACK);
            screen.repaintCanvas();
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
        if (!active) return;

        char c = e.getKeyChar();

        if (c == '\n') {
            String cmd = commandInput.toString();
            newLine();
            try {
                executeCommand(cmd);
            } catch (Exception err) {
                println("Error: " + err, screen.COLOR_RED);
            }
            if (!active) {
                commandInput.setLength(0);
                return;
            }
            commandInput.setLength(0);
            print(PROMPT, screen.COLOR_WHITE);
            screen.repaintCanvas();

        } else if (c == '\b') {
            backspace();

        } else if (!Character.isISOControl(c)) {
            commandInput.append(c);
            drawChar(c, screen.COLOR_WHITE);
            screen.repaintCanvas();
        }
    }

    protected void executeCommand(String cmd) throws Exception {
        cmd = cmd.trim();
        if (cmd.isEmpty()) return;

        String[] parts = cmd.split("\\s+", 2);
        String command = parts[0].toLowerCase();
        String args = parts.length > 1 ? parts[1] : "";

        switch (command) {
            case "help":
                help(args);
                break;

            case "clear":
                screen.clear(screen.COLOR_BLACK);
                cursorX = 0;
                cursorY = 0;
                break;

            case "sinfo":
                println("Res: " + screen.getWidth() + "x" + screen.getHeight(), screen.COLOR_WHITE);
                println("Char size: " + charWidth + "x" + lineHeight, screen.COLOR_WHITE);
                break;

            case "fetch":
                fetch();
                break;

            case "down":
                System.exit(0);
                break;

            case "ls":
                ls();
                break;

            case "md":
                if (args.isEmpty()) {
                    println("Usage: md <directory_name>", screen.COLOR_WHITE);
                } else {
                    call.makeDirectory(args);
                }
                break;

            case "mf":
                if (args.isEmpty()) {
                    println("Usage: mf <file_name>", screen.COLOR_WHITE);
                } else {
                    call.newFile(args);
                }
                break;

            case "applist":
                println("Available applications: calculator, settings, ted, imgedit, explorer, pid", screen.COLOR_WHITE);
                break;
            case "cd":
                call.setDirectory(args);
                break;

            case "crd":
                println(call.getDirectory(), screen.COLOR_WHITE);
                break;

            case "del":
                call.deleteFile(args);
                break;

            case "mv":
                if (args.isEmpty()) {
                    println("Usage: mv <source> <target>", screen.COLOR_WHITE);
                } else {
                    String[] mvParts = args.split("\\s+", 2);
                    if (mvParts.length < 2) {
                        println("Usage: mv <source> <target>", screen.COLOR_WHITE);
                    } else {
                        call.moveFile(mvParts[0], mvParts[1]);
                    }
                }
                break;

            case "cp":
                if (args.isEmpty()) {
                    println("Usage: cp <source> <target>", screen.COLOR_WHITE);
                } else {
                    String[] cpParts = args.split("\\s+", 2);
                    if (cpParts.length < 2) {
                        println("Usage: cp <source> <target>", screen.COLOR_WHITE);
                    } else {
                        call.copyFile(cpParts[0], cpParts[1]);
                    }
                }
                break;

            case "calculator":
                runApp(new Calculator(Kernel.screen));
                break;

            case "settings":
                runApp(new Settings(Kernel.screen));
                break;

            case "ted":
                TextEditor ted = new TextEditor(Kernel.screen, args);
                long id = runApp(ted);
                ted.setProcessID(id);
                break;

            case "pid":
                runApp(new TaskManager(Kernel.screen));
                break;

            case "explorer":
                runApp(new FileExplorer(Kernel.screen));
                break;

            case "view":
                runApp(new ImageViewer(Kernel.screen, args));
                break;

            case "imgedit":
                runApp(new ImageViewer(Kernel.screen, args));
                break;

            default:
                if (isExecutable(cmd)) {
                    ra.run(call.getDirectory() + "/" + cmd);
                } else {
                    println("Unknown command: " + command, screen.COLOR_RED);
                }
        }
    }

    protected void help(String args) {
        if (args.isEmpty() || args.equals(" ")) {
            println("Commands: help, clear, sinfo, fetch, down, ls, cd, crd, md, mf, del, mv, cp", screen.COLOR_WHITE);
            println("Apps: calculator, settings, ted <file>, imgedit <file>, explorer, pid", screen.COLOR_WHITE);
            newLine();
            println("You can also run an application like calculator by just typing its name.", screen.COLOR_WHITE);
            println("To get a list of applications run 'applist'", screen.COLOR_WHITE);
            newLine();
            println("To see what a command does type help <command>", screen.COLOR_WHITE);
        } else {
            switch (args) {
                case "help":
                    println("Helps you", screen.COLOR_WHITE);
                    break;
                case "fetch":
                    println("Shows you system information", screen.COLOR_WHITE);
                    println("Usage: fetch", screen.COLOR_BLUE);
                    break;
                case "sinfo":
                    println("Shows you screen information like resolution", screen.COLOR_WHITE);
                    println("Usage: sinfo", screen.COLOR_BLUE);
                    break;
                case "down":
                    println("Shuts the system down", screen.COLOR_WHITE);
                    println("Usage: down", screen.COLOR_BLUE);
                    break;
                case "ls":
                    println("Shows all files and directories in a directory", screen.COLOR_WHITE);
                    println("Usage: ls", screen.COLOR_BLUE);
                    break;
                case "applist":
                    println("Shows a list of available applications", screen.COLOR_WHITE);
                    println("Usage: applist", screen.COLOR_BLUE);
                    break;
                case "ted":
                    println("Opens the text editor.", screen.COLOR_WHITE);
                    println("Usage: ted <path>", screen.COLOR_BLUE);
                    break;
                case "imgedit":
                    println("Opens the image editor for .img files", screen.COLOR_WHITE);
                    println("Usage: imgedit <path>", screen.COLOR_BLUE);
                    break;
                case "explorer":
                    println("Opens the file explorer.", screen.COLOR_WHITE);
                    break;
                case "cd":
                    println("Changes the current directory.", screen.COLOR_WHITE);
                    println("Usage: cd <path>", screen.COLOR_BLUE);
                    break;
                case "del":
                    println("Deletes a file.", screen.COLOR_WHITE);
                    println("Usage: del <path>", screen.COLOR_BLUE);
                    break;
                case "mv":
                    println("Moves or renames a file.", screen.COLOR_WHITE);
                    println("Usage: mv <source> <target>", screen.COLOR_BLUE);
                    break;
                case "cp":
                    println("Copies a file.", screen.COLOR_WHITE);
                    println("Usage: cp <source> <target>", screen.COLOR_BLUE);
                    break;
                case "mf":
                    println("Makes a new file", screen.COLOR_WHITE);
                    println("Usage: mf <name>", screen.COLOR_BLUE);
                    break;
                case "md":
                    println("Makes a new directory", screen.COLOR_WHITE);
                    println("Usage: md <name>", screen.COLOR_BLUE);
                    break;
                default:
                    println("Unknown command: " + args, screen.COLOR_RED);
            }
        }
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    protected long runApp(RunnableApplication rapp) throws Exception {
        this.active = false;


        return Kernel.getInstance().runApplication(rapp);
    }

    protected void ls() throws Exception {
        File dir = new File(call.getDirectory());
        List<String> files = call.getAllFilesInDirectory(dir);

        if (files.isEmpty()) {
            println("The directory is empty", screen.COLOR_YELLOW);
            return;
        }

        for (String file : files) {
            println(file, screen.COLOR_WHITE);
        }
    }

    protected boolean isExecutable(String filename) throws Exception {
        List<String> files = call.getAllFilesInDirectory();

        for (String file : files) {
            if (file.equals(filename)) {
                return true;
            }
        }
        return false;
    }

    protected void fetch() {
        screen.fillRect(cursorX + 16, cursorY, 150, 176, screen.COLOR_GREEN);
        screen.fillRect(cursorX + 32, cursorY + 16, 32, 144, screen.COLOR_BLUE);
        screen.fillRect(cursorX + 48, cursorY + 72, 88, 32, screen.COLOR_BLUE);
        screen.fillRect(cursorX + 118, cursorY + 16, 32, 144, screen.COLOR_BLUE);

        cursorX = cursorX + 200;
        print("HuesOS v0.1", screen.COLOR_RED);
        newLine();
        cursorX = cursorX + 200;
        print(" by DrQF", 0xFF00FF);
        cursorY = cursorY + 176;
        cursorX = 0;

        screen.repaintCanvas();
    }

    @Override public void keyPressed(KeyEvent e) {}
    @Override public void keyReleased(KeyEvent e) {}

    @Override
    public void onStop() {
        screen.removeInputListener(this);
    }

    @Override
    public void step(Screen screen, Process process) throws Exception {
        if (!started) {
            start();
            started = true;
        }
    }

    @Override
    public boolean isFinished() {
        return false;
    }

    @Override
    public String getName() {
        return this.name;
    }
    @Override
    public void setName(String name) {
        this.name = name;
    }
}
