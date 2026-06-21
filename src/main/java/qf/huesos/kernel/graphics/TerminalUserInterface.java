package qf.huesos.kernel.graphics;

import qf.huesos.kernel.Screen;
import qf.huesos.kernel.graphics.TUI.Cursor;
import qf.huesos.kernel.graphics.TUI.TUIElement;
import qf.huesos.kernel.graphics.TUI.TextField;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
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
    private volatile boolean running = false;

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
        running = true;
        new Thread(() -> {
            while (running) {
                cursor.setPosition(cursorX, cursorY);
                input();
                update();
                try {
                    draw();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                screen.render();

                try {
                    Thread.sleep(16); // ~60 FPS
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void stop() {
        running = false;
        screen.removeInputListener(this);
    }

    public void update() {}
    public void input() {}

    private void draw() throws IOException {
        screen.clear(BG_COLOR);

        for (TUIElement element : TUIElements) {
            if (element == cursor) continue;
            element.draw();

            if (cursor.overlaps(element)) {
                element.hover();
            } else {
                element.unHover();
            }
        }

        cursor.draw();
    }

    public void add(TUIElement TUIElement) {
        TUIElements.add(TUIElement);
    }

    public void setBgColor(int color) {
        BG_COLOR = color;
    }

    @Override
    public void keyTyped(KeyEvent e) {
        TextField textField = getFocusedTextField();
        if (textField != null) {
            char c = e.getKeyChar();
            if (c == '\b') {
                textField.backspace();
            } else if (c == '\n') {
                textField.enter();
            } else if (!Character.isISOControl(c)) {
                textField.onKeyTyped(c);
            }
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        TextField textField = getFocusedTextField();
        if (textField != null) {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_TAB -> focusNextTextField();
                case KeyEvent.VK_LEFT -> textField.moveLeft();
                case KeyEvent.VK_RIGHT -> textField.moveRight();
                case KeyEvent.VK_UP -> textField.moveUp();
                case KeyEvent.VK_DOWN -> textField.moveDown();
                case KeyEvent.VK_PAGE_UP -> textField.pageUp();
                case KeyEvent.VK_PAGE_DOWN -> textField.pageDown();
                case KeyEvent.VK_ESCAPE -> textField.setFocused(false);
            }
            return;
        }

        switch (e.getKeyCode()) {
            case KeyEvent.VK_TAB -> focusNextTextField();
            case KeyEvent.VK_RIGHT -> cursorX += 10;
            case KeyEvent.VK_LEFT  -> cursorX -= 10;
            case KeyEvent.VK_UP    -> cursorY -= 10;
            case KeyEvent.VK_DOWN  -> cursorY += 10;

            case KeyEvent.VK_ENTER -> {
                for (TUIElement TUIElement : TUIElements) {
                    if (!TUIElement.visible) {
                        continue;
                    }

                    if (cursor.overlaps(TUIElement)) {
                        try {
                            if (TUIElement instanceof TextField) {
                                focusTextField((TextField) TUIElement);
                                ((TextField) TUIElement).click(cursorX, cursorY);
                            }
                            TUIElement.click();
                        } catch (IOException ex) {
                            throw new RuntimeException(ex);
                        } catch (Exception ex) {
                            throw new RuntimeException(ex);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {

    }

    protected TextField getFocusedTextField() {
        for (TUIElement element : TUIElements) {
            if (element instanceof TextField textField && textField.isFocused()) {
                return textField;
            }
        }

        return null;
    }

    protected void focusTextField(TextField field) {
        for (TUIElement element : TUIElements) {
            if (element instanceof TextField textField) {
                textField.setFocused(textField == field);
            }
        }
    }

    protected void focusNextTextField() {
        List<TextField> fields = new ArrayList<>();

        for (TUIElement element : TUIElements) {
            if (element instanceof TextField textField && textField.visible) {
                fields.add(textField);
            }
        }

        if (fields.isEmpty()) {
            return;
        }

        TextField focused = getFocusedTextField();
        int index = focused == null ? -1 : fields.indexOf(focused);
        TextField next = fields.get((index + 1) % fields.size());
        focusTextField(next);
    }
}
