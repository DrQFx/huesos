package qf.huesos.kernel.provider.image;

public class Pixel {
    private final int x;
    private final int y;
    private final int color;

    public Pixel(int x, int y, int color) {
        this.x = x;
        this.y = y;
        this.color = color;
    }

    public int getX() {
        return this.x;
    }
    public int getY() {
        return this.y;
    }
    public int getColor() {
        return this.color;
    }

    @Override
    public String toString() {
        return "X: " + x + "\n" + "Y: " + y + "\n" + "Color: " + color;
    }
}
