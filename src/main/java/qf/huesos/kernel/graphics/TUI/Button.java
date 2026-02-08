package qf.huesos.kernel.graphics.TUI;

import qf.huesos.kernel.Screen;

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
    public void click() {
        if (onClick != null) {
            onClick.run();
        }
        isClicked = !isClicked;
    }

    @Override
    public void draw() {
        if (isHovered) {
            screen.fillRect(x, y, width, height, rgbHover);
        } else if (isClicked) {
            screen.fillRect(x, y, width, height, rgbClick);
        } else if (!(this.text == null)){
            screen.drawText(text, x, y, textColor);
            screen.fillRect(x, y, width, height, color);
        }
        else {
            screen.fillRect(x, y, width, height, color);
        }
    }
}
