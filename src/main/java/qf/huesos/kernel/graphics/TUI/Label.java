package qf.huesos.kernel.graphics.TUI;


import qf.huesos.kernel.Screen;

public class Label extends TUIElement {
    public String text;

    public Label(Screen screen) {
        super(screen);
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public void draw() {
        screen.drawText(text, x, y, color);
    }
}
