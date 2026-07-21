package qf.huesos.apps;

import qf.huesos.kernel.Screen;
import qf.huesos.kernel.SysCall;
import qf.huesos.kernel.Kernel;
import qf.huesos.kernel.graphics.TUI.*;
import qf.huesos.kernel.graphics.TerminalApplication;
import qf.huesos.kernel.nsfw.NSFW;
import qf.huesos.kernel.scheduler.RunnableApplication;

import java.io.File;

public class FileExplorer extends TerminalApplication implements RunnableApplication {

    private final String pathToUI = "fs/system/apps/explorer/general.htui";
    private final int bg = 0x202020;

    private String currentPath = "fs";

    private NSFW fs = new NSFW();
    private final SysCall call = new SysCall();
    private long PROCESS_ID;

    public FileExplorer(Screen screen) throws Exception {
        super(screen);
        setPath(pathToUI);
        setBgColor(bg);

        fs.init(currentPath);

        start();
    }

    @Override
    protected void onUiLoaded() throws Exception {
        refresh();
    }

    private void refresh() {
        TUIElements.removeIf(e -> !(e instanceof Cursor));
        TUIElementsByID.clear();

        Label pathLabel = new Label(screen);
        pathLabel.id = "path_label";
        pathLabel.x = 10;
        pathLabel.y = 5;
        pathLabel.color = 0xFFFFFF;
        pathLabel.text = currentPath;
        add(pathLabel);

        Button back = new Button(screen);
        back.id = "back_btn";
        back.setText("..");
        back.x = 10;
        back.y = 30;
        back.color = 0x42014f;
        back.setRgbHover(0x510161);
        back.setRgbClick(0x790191);
        back.setTextColor(0xffffff);
        back.width = 60;
        back.height = 20;

        back.onClick = () -> {
            File dir = new File(currentPath);
            String parent = dir.getParent();

            if (parent != null) {
                currentPath = parent;
                fs.setCurrentDirectory(currentPath);
                refresh();
            }
        };

        add(back);

        File dir = new File(currentPath);
        File[] files = dir.listFiles();

        if (files == null) return;

        int y = 60;

        for (File file : files) {
            Button fileBtn = new Button(screen);

            fileBtn.id = "file_" + file.getName();
            fileBtn.setText(file.getName());

            fileBtn.x = 10;
            fileBtn.y = y;
            fileBtn.width = 200;
            fileBtn.height = 20;
            fileBtn.color = 0x42014f;
            fileBtn.setRgbHover(0x510161);
            fileBtn.setRgbClick(0x790191);
            fileBtn.setTextColor(0xffffff);

            if (file.isDirectory()) {
                fileBtn.onClick = () -> {
                    currentPath = file.getPath();
                    fs.setCurrentDirectory(currentPath);
                    refresh();
                };
            } else {
                fileBtn.onClick = () -> {
                    openFile(file);
                };
            }

            add(fileBtn);
            y += 25;
        }
    }

    private void openFile(File file) throws Exception {
        String path = file.getPath();
        String lower = file.getName().toLowerCase();

        stop();
        call.killProcess(PROCESS_ID);

        if (lower.endsWith(".img")) {
            Kernel.getInstance().runApplication(new ImageViewer(Kernel.screen, path));
            return;
        }

        TextEditor editor = new TextEditor(Kernel.screen, path);
        long id = Kernel.getInstance().runApplication(editor);
        editor.setProcessID(id);
    }

    @Override
    public void step(Screen screen, qf.huesos.kernel.scheduler.Process process) throws Exception {
        if (PROCESS_ID == 0) {
            PROCESS_ID = process.getId();
        }
    }

    @Override
    public boolean isFinished() {
        return false;
    }

    @Override
    public void onStop() {
        stop();
    }

    @Override
    public String getName() {
        return "File Explorer";
    }
}
