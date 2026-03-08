package qf.huesos.kernel.graphics.TUI;

import qf.huesos.kernel.Screen;

import java.io.IOException;

public class TUIElement {
    Screen screen;
    public int x;
    public int y;
    public int color;
    public int width;
    public int height;

    public String id;

    public int rgbHover;
    public int rgbClick;

    boolean isHovered = false;
    public boolean isClicked = false;

    public TUIElement(Screen screen) {
        this.screen = screen;
    }

    public int getX() {
        return x;
    }
    public int getY() {
        return y;
    }
    public int getColor() {
        return color;
    }
    public int getWidth() {
        return width;
    }
    public int getHeight() {
        return height;
    }

    public void setX(int x) {
        this.x = x;
    }
    public void setY(int y) {
        this.y = y;
    }
    public void setColor(int rgb) {
        this.color = rgb;
    }
    public void setWidth(int width) {
        this.width = width;
    }
    public void setHeight(int height) {
        this.height = height;
    }

    public void hover() {
        isHovered = true;
    }
    public void unHover() {
        isHovered = false;
    }

    public void click() throws IOException {
        isClicked = !isClicked;
    }

    public void draw() throws IOException {
        screen.fillRect(x, y, width, height, color);
    }
}
