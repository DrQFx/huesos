package qf.huesos.kernel.graphics.TUI;

import qf.huesos.kernel.Screen;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SelectMenu extends TUIElement{
    private List<TUIElement> items = new ArrayList<>();

    public int openedWidth;
    public int openedHeight;

    private String text;
    private int textColor;

    public SelectMenu(Screen screen) {
        super(screen);
    }

    @Override
    public void draw() throws IOException {
        if (!visible) return;

        screen.fillRect(x, y, width, height, color);

        if (text != null) {
            screen.drawText(text, x, y, textColor);
        }

        if (isClicked) {
            screen.fillRect(x, y + height, openedWidth, openedHeight, color);

            for (TUIElement item : items) {
                item.visible = true;
                item.draw();
            }
        } else {
            for (TUIElement item : items) {
                item.visible = false;
            }
        }
    }

    public void setOpenedWidth(int width) { this.openedWidth = width; }
    public void setOpenedHeight(int height) { this.openedHeight = height; }
    public void setText(String text) {
        this.text = text;
    }
    public void setTextColor(int color) {
        this.textColor = color;
    }
    public void addItem(TUIElement element) {
        element.x = x;
        element.y = y + height + items.size() * element.height;

        items.add(element);
    }
}
