package qf.huesos.kernel.graphics;

import qf.huesos.kernel.Screen;
import qf.huesos.kernel.graphics.TUI.Cursor;
import qf.huesos.kernel.graphics.TUI.TUIElement;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TerminalUserInterface implements KeyListener{
    public Screen screen;

    public List<TUIElement> TUIElements = new ArrayList<>();
    public Map<String, TUIElement> TUIElementsByID = new HashMap<>();

    public int cursorX = 0;
    public int cursorY = 0;

    public int BG_COLOR;
    Cursor cursor;

    public TerminalUserInterface(Screen screen) {
        this.screen = screen;

        cursor = new Cursor(screen);
        cursor.setColor(screen.COLOR_WHITE);
        cursor.setWidth(16);
        cursor.setHeight(16);

        TUIElements.add(cursor);

        screen.addInputListener(this);
    }

    public void start() throws Exception {
        new Thread(() -> {
            while (true) {
                cursor.setPosition(cursorX, cursorY);
                input();
                update();
                draw();
                try {
                    Thread.sleep(16); // ~60 FPS
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void update() {}
    public void input() {}

    private void draw() {
        screen.clear(BG_COLOR);

        for (TUIElement TUIElement : TUIElements) {
            TUIElement.draw();

            if (cursor.overlaps(TUIElement)) {
                TUIElement.hover();
            } else {
                TUIElement.unHover();
            }
        }
    }

    public void add(TUIElement TUIElement) {
        TUIElements.add(TUIElement);
    }

    public void setBgColor(int color) {
        BG_COLOR = color;
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_RIGHT -> cursorX += 10;
            case KeyEvent.VK_LEFT  -> cursorX -= 10;
            case KeyEvent.VK_UP    -> cursorY -= 10;
            case KeyEvent.VK_DOWN  -> cursorY += 10;

            case KeyEvent.VK_ENTER -> {
                for (TUIElement TUIElement : TUIElements) {
                    if (cursor.overlaps(TUIElement)) {
                        TUIElement.click();
                    }
                }
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {

    }
}
