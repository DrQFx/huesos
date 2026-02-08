package qf.huesos.kernel.graphics.TUI;


import qf.huesos.kernel.Screen;

public class CheckBox extends TUIElement {
    public CheckBox(Screen screen) {
        super(screen);
    }

    @Override
    public void draw() {
        int size = Math.min(width, height);
        int border = size / 8;
        int padding = size / 4;

        screen.fillRect(x, y, size, border, color);
        screen.fillRect(x, y + size - border, size, border, color);
        screen.fillRect(x, y, border, size, color);
        screen.fillRect(x + size - border, y, border, size, color);

        if (isClicked) {
            screen.fillRect(
                    x + padding,
                    y + padding,
                    size - padding * 2,
                    size - padding * 2,
                    color
            );
        }
    }
}