package qf.huesos.kernel.graphics.TUI;

import qf.huesos.kernel.Screen;

import java.io.IOException;

public class Button extends TUIElement {
    public Action onClick;

    private String text;
    private int textColor;

    public Button(Screen screen) {
        super(screen);
    }

    public void setRgbHover(int rgbHover) {
        this.rgbHover = rgbHover;
    }
    public void setRgbClick(int rgbClick) {
        this.rgbClick = rgbClick;
    }
    public void setText(String text) {
        this.text = text;
    }
    public void setTextColor(int color) {
        this.textColor = color;
    }

    @Override
    public void click() throws IOException {
        if (onClick != null) {
            onClick.run();
        }
    }

    @Override
    public void draw() {
        if (isHovered) {
            screen.fillRect(x, y, width, height, rgbHover);
        } else if (isClicked) {
            screen.fillRect(x, y, width, height, rgbClick);
        } else {
            screen.fillRect(x, y, width, height, color);
        }

        if (text != null) {
            screen.drawText(text, x, y, textColor);
        }
    }
}
