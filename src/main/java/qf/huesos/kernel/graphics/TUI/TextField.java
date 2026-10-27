package qf.huesos.kernel.graphics.TUI;

import qf.huesos.kernel.Screen;

import java.util.ArrayList;
import java.util.List;

public class TextField extends TUIElement {
    public Action onClick;

    private List<String> lines = new ArrayList<>();

    private int cursorX = 0;
    private int cursorY = 0;

    private int scrollY = 0;

    private boolean focused = false;
    private boolean singleLine = false;

    private int textColor;

    public TextField(Screen screen) {
        super(screen);
        lines.add("");
    }

    public boolean isFocused() {
        return focused;
    }

    public void click(int mouseX, int mouseY) {
        focused = true;

        int lineHeight = 16;
        int charWidth = 8;

        cursorY = scrollY + (mouseY - y) / lineHeight;

        if (cursorY >= lines.size()) {
            cursorY = lines.size() - 1;
        }

        if (cursorY < 0) {
            cursorY = 0;
        }

        cursorX = Math.max(0, (mouseX - x - 4) / charWidth);
        clampCursor();
    }

    public void onKeyTyped(char c) {
        if (!focused) return;

        clampCursor();
        String line = lines.get(cursorY);
        line = line.substring(0, cursorX) + c + line.substring(cursorX);
        lines.set(cursorY, line);

        cursorX++;
    }

    public void backspace() {

        if (!focused) return;
        clampCursor();

        if (cursorX > 0) {

            String line = lines.get(cursorY);

            line = line.substring(0, cursorX - 1) + line.substring(cursorX);

            lines.set(cursorY, line);

            cursorX--;

        } else if (cursorY > 0) {

            String prev = lines.get(cursorY - 1);
            String current = lines.get(cursorY);

            cursorX = prev.length();

            lines.set(cursorY - 1, prev + current);
            lines.remove(cursorY);

            cursorY--;
        }
    }

    public void enter() {
        if (!focused) return;
        clampCursor();

        if (singleLine) {
            focused = false;
            return;
        }

        String line = lines.get(cursorY);
        String left = line.substring(0, cursorX);
        String right = line.substring(cursorX);

        lines.set(cursorY, left);

        lines.add(cursorY + 1, right);

        cursorY++;
        cursorX = 0;
        keepCursorVisible();
    }

    public void moveLeft() {
        if (cursorX > 0) {
            cursorX--;
        } else if (cursorY > 0) {
            cursorY--;
            cursorX = lines.get(cursorY).length();
        }
        keepCursorVisible();
    }

    public void moveRight() {
        if (cursorX < lines.get(cursorY).length()) {
            cursorX ++;
        } else if (cursorY < lines.size() - 1) {
            cursorY ++;
            cursorX = 0;
        }
        keepCursorVisible();
    }

    public void moveUp() {
        if (cursorY > 0) {
            cursorY--;
            clampCursor();
        }
        keepCursorVisible();
    }

    public void moveDown() {
        if (cursorY < lines.size() - 1) {
            cursorY++;
            clampCursor();
        }
        keepCursorVisible();
    }

    public void pageUp() {
        cursorY = Math.max(0, cursorY - Math.max(1, visibleLineCount()));
        clampCursor();
        keepCursorVisible();
    }

    public void pageDown() {
        cursorY = Math.min(lines.size() - 1, cursorY + Math.max(1, visibleLineCount()));
        clampCursor();
        keepCursorVisible();
    }

    public void setFocused(boolean focused) {
        this.focused = focused;
    }

    public void setSingleLine(boolean singleLine) {
        this.singleLine = singleLine;
    }

    public boolean isSingleLine() {
        return singleLine;
    }

    public void setLines(List<String> newLines) {
        lines = new ArrayList<>(newLines);
        if (lines.isEmpty()) {
            lines.add("");
        }
        cursorX = 0;
        cursorY = 0;
        scrollY = 0;
        clampCursor();
    }

    public List<String> getLines() {
        return new ArrayList<>(lines);
    }

    public String getText() {
        return lines.isEmpty() ? "" : lines.get(0);
    }

    public void clearText() {
        lines.clear();
        lines.add("");
        cursorX = 0;
        cursorY = 0;
        scrollY = 0;
    }

    @Override
    public void draw() {

        screen.fillRect(x, y, width, height, color);

        int lineHeight = 16;

        for (int i = 0; i < height / lineHeight; i++) {

            int lineIndex = scrollY + i;

            if (lineIndex >= lines.size()) break;

            screen.drawText(
                    lines.get(lineIndex),
                    x + 4,
                    y + 4 + i * lineHeight,
                    textColor
            );
        }

        if (focused) {

            int charWidth = 8;

            if (cursorY >= scrollY && cursorY < scrollY + height / lineHeight) {

                int cx = x + 4 + cursorX * charWidth;
                int cy = y + 4 + (cursorY - scrollY) * lineHeight;

                screen.fillRect(cx, cy, 2, lineHeight, 0xFFFFFF);
            }
        }
    }

    public void setTextColor(int color) {
        this.textColor = color;
    }

    private int visibleLineCount() {
        return Math.max(1, height / 16);
    }

    private void clampCursor() {
        if (lines.isEmpty()) {
            lines.add("");
        }

        cursorY = Math.max(0, Math.min(cursorY, lines.size() - 1));
        cursorX = Math.max(0, Math.min(cursorX, lines.get(cursorY).length()));
    }

    private void keepCursorVisible() {
        int visibleLines = visibleLineCount();

        if (cursorY < scrollY) {
            scrollY = cursorY;
        } else if (cursorY >= scrollY + visibleLines) {
            scrollY = cursorY - visibleLines + 1;
        }

        scrollY = Math.max(0, Math.min(scrollY, Math.max(0, lines.size() - visibleLines)));
    }
}
