package qf.huesos.kernel;

import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.awt.event.*;
import java.awt.image.*;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import javax.swing.JFrame;

public class Screen extends JFrame {
    private BufferedImage displayBuffer;
    private int[] pixelData;

    private final int width;
    private final int height;

    private final Font systemFont;
    private final FontMetrics fontMetrics;

    private final DisplayCanvas canvas;

    public final int COLOR_BLACK = 0x000000;
    public final int COLOR_WHITE = 0xFFFFFF;
    public final int COLOR_RED = 0xFF0000;
    public final int COLOR_GREEN = 0x00FF00;
    public final int COLOR_BLUE = 0x0000FF;
    public final int COLOR_YELLOW = 0xFFF70;
    public final int COLOR_CYAN = 0x00FFFF;

    public Screen(int width, int height) {
        this.width = width;
        this.height = height;

        displayBuffer = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        pixelData = ((DataBufferInt) displayBuffer.getRaster().getDataBuffer()).getData();

        systemFont = new Font("Courier New", Font.PLAIN, 16);

        Graphics g = displayBuffer.getGraphics();
        g.setFont(systemFont);
        fontMetrics = g.getFontMetrics();
        g.dispose();

        setTitle("HuesOS 0.1");
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        canvas = new DisplayCanvas();
        canvas.setPreferredSize(new Dimension(width, height));

        add(canvas);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);

        canvas.createBufferStrategy(2);

        canvas.requestFocus();
    }

    // API

    public void clear(int color) {
        for (int i = 0; i < pixelData.length; i++) {
            pixelData[i] = color;
        }
    }

    public void fillRect(int x, int y, int w, int h, int color) {
        for (int dy = 0; dy < h; dy++) {
            int row = y + dy;
            if (row < 0 || row >= height) continue;
            for (int dx = 0; dx < w; dx++) {
                int col = x + dx;
                if (col < 0 || col >= width) continue;
                pixelData[row * width + col] = color;
            }
        }
    }

    public void drawText(String text, int x, int y, int color) {
        Graphics2D g = displayBuffer.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);

        g.setFont(systemFont);
        g.setColor(new Color(color));

        g.drawString(text, x, y + fontMetrics.getAscent());
        g.dispose();
    }

    public void scrollUp(int amount, int fillColor) {
        int totalPixels = width * height;
        int shiftSize = amount * width;

        if (shiftSize >= totalPixels) {
            clear(fillColor);
            return;
        }

        System.arraycopy(pixelData, shiftSize, pixelData, 0, totalPixels - shiftSize);

        for (int i = totalPixels - shiftSize; i < totalPixels; i++) {
            pixelData[i] = fillColor;
        }
    }

    public void render() {
        BufferStrategy bs = canvas.getBufferStrategy();
        if (bs == null) return;

        Graphics g = bs.getDrawGraphics();
        g.drawImage(displayBuffer, 0, 0, null);
        g.dispose();

        bs.show();
        Toolkit.getDefaultToolkit().sync();
    }

    // ---------------------------------------

    public int getCharWidth() {
        return fontMetrics.charWidth('W');
    }

    public int getLineHeight() {
        return fontMetrics.getHeight();
    }

    public int getWidth() { return width; }
    public int getHeight() { return height; }

    public void addInputListener(KeyListener kl) {
        canvas.addKeyListener(kl);
    }

    public void removeInputListener(KeyListener kl) {
        canvas.removeKeyListener(kl);
    }

    public void repaintCanvas() {
        canvas.repaint();
    }

    // ----------------------------------------------

    private void setPixelSafe(int x, int y, int color) {
        if (x >= 0 && x < width && y >= 0 && y < height) {
            pixelData[y * width + x] = color;
        }
    }

    private class DisplayCanvas extends Canvas {
        @Override
        public void paint(Graphics g) {
            g.drawImage(displayBuffer, 0, 0, null);
        }
        @Override
        public void update(Graphics g) {
            paint(g);
        }
    }
}
