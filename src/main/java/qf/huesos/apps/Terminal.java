package qf.huesos.apps;

import qf.huesos.kernel.Kernel;
import qf.huesos.kernel.Screen;
import qf.huesos.kernel.SysCall;

import java.io.IOException;
import java.util.List;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class Terminal implements KeyListener {
    private final Screen screen;
    private SysCall call;

    private int cursorX = 0;
    private int cursorY = 0;

    private final int charWidth;
    private final int lineHeight;


    private StringBuilder commandInput = new StringBuilder();
    private final String PROMPT = "> ";

    public Terminal(Screen screen) {
        this.screen = screen;

        call = new SysCall();

        this.charWidth = screen.getCharWidth();
        this.lineHeight = screen.getLineHeight();

        screen.addInputListener(this);

        start();
    }

    public void start() {
        screen.clear(screen.COLOR_BLACK);
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

    private void drawChar(char c, int rgb) {
        screen.drawText(String.valueOf(c), cursorX, cursorY, rgb);

        cursorX += charWidth;

        if (cursorX >= screen.getWidth() - charWidth) {
            newLine();
        }
    }

    private void newLine() {
        cursorX = 0;
        cursorY += lineHeight;

        if (cursorY + lineHeight > screen.getHeight()) {
            screen.scrollUp(lineHeight, screen.COLOR_BLACK);
            cursorY -= lineHeight;
        }
    }

    private void backspace() {
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
        char c = e.getKeyChar();

        if (c == '\n') {
            String cmd = commandInput.toString();
            newLine();
            try {
                executeCommand(cmd);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
            commandInput.setLength(0);
            print(PROMPT, screen.COLOR_WHITE);
        } else if (c == '\b') {
            backspace();
        } else if (!Character.isISOControl(c)) {
            commandInput.append(c);
            drawChar(c, screen.COLOR_WHITE);
            screen.repaintCanvas();
        }
    }

    private void executeCommand(String cmd) throws Exception {
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
                println("Available applications: calculator", screen.COLOR_WHITE);
                break;
            case "calculator":
                Kernel.getInstance().runApplication(new Calculator(Kernel.screen));
                break;
            default:
                println("Unknown command: " + command, screen.COLOR_RED);
        }
    }

    private void help(String args) {
        if (args.isEmpty() || args == " ") {
            println("Commands: help, clear, sinfo, down, ls, md <name>, mf <name>, applist", screen.COLOR_WHITE);
            newLine();
            println("You can also run an application like calculator by just typing its name.\nTo get a list of the applications run 'applist'", screen.COLOR_WHITE);
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
                    println("Usage: ls <path>", screen.COLOR_BLUE);
                    break;
                case "applist":
                    println("Shows you a list of available applications", screen.COLOR_WHITE);
                    println("Usage: applist", screen.COLOR_BLUE);
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

    private void ls() {
        List<String> files = call.getAllFilesInDirectory();

        for (String file : files) {
            println(file, screen.COLOR_WHITE);
        }
    }

    private void fetch() {
        screen.fillRect(cursorX+16, cursorY, 150, 176, screen.COLOR_GREEN);
        screen.fillRect(cursorX+32, cursorY+16, 32, 144, screen.COLOR_BLUE);
        screen.fillRect(cursorX+48, cursorY+72, 88, 32, screen.COLOR_BLUE);
        screen.fillRect(cursorX+118, cursorY+16, 32, 144, screen.COLOR_BLUE);

        cursorX = cursorX+200;
        print("HuesOS v0.1", screen.COLOR_RED);
        newLine();
        cursorX = cursorX+200;
        print(" by DrQF", 0xFF00FF);
        cursorY = cursorY+176;
        cursorX = 0;

        screen.repaintCanvas();
    }

    @Override public void keyPressed(KeyEvent e) {}
    @Override public void keyReleased(KeyEvent e) {}
}