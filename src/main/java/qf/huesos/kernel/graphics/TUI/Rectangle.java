package qf.huesos.kernel.graphics.TUI;

import qf.huesos.kernel.Screen;

public class Rectangle extends TUIElement{
    public Rectangle(Screen screen) {
        super(screen);
    }

    @Override
    public void draw() {
        screen.fillRect(x, y, width, height, color);
    }
}
