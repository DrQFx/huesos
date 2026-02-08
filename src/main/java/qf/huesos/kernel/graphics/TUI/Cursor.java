package qf.huesos.kernel.graphics.TUI;


import qf.huesos.kernel.Screen;

public class Cursor extends TUIElement {

    public Cursor(Screen screen) {
        super(screen);
    }

    public boolean overlaps(TUIElement TUIElement) {
        boolean xOverlap = this.getX() < TUIElement.getX() + TUIElement.getWidth() &&
                this.getX() + this.getWidth() > TUIElement.getX();
        boolean yOverlap = this.getY() < TUIElement.getY() + TUIElement.getHeight() &&
                this.getY() + this.getHeight() > TUIElement.getY();

        if (TUIElement == this) {
            return false;
        }

        return xOverlap && yOverlap;
    }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }
}
