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
import javax.swing.JFrame; // Используем Swing для стабильности, но можно и Frame

public class Screen extends JFrame {
    // Графический буфер
    private BufferedImage displayBuffer;
    private int[] pixelData;

    // Размеры
    private final int width;
    private final int height;

    // "Железные" настройки шрифта (вшиты в экран)
    private final Font systemFont;
    private final FontMetrics fontMetrics;

    // Компоненты
    private final DisplayCanvas canvas;

    public final int COLOR_BLACK = 0x000000;
    public final int COLOR_WHITE = 0xFFFFFF;
    public final int COLOR_RED = 0xFF0000;
    public final int COLOR_GREEN = 0x00FF00;
    public final int COLOR_BLUE = 0x0000FF;

    public Screen(int width, int height) {
        this.width = width;
        this.height = height;

        // Инициализация "видеопамяти"
        displayBuffer = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        pixelData = ((DataBufferInt) displayBuffer.getRaster().getDataBuffer()).getData();

        // Настройка встроенного шрифта (моноширинный для терминала)
        systemFont = new Font("Courier New", Font.PLAIN, 16);

        // Получаем метрики шрифта (нужен фиктивный графикс для расчетов)
        Graphics g = displayBuffer.getGraphics();
        g.setFont(systemFont);
        fontMetrics = g.getFontMetrics();
        g.dispose();

        // Настройка окна
        setTitle("HuesOS 0.1");
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        canvas = new DisplayCanvas();
        canvas.setPreferredSize(new Dimension(width, height));

        add(canvas);
        pack();
        setLocationRelativeTo(null); // Центр экрана
        setVisible(true);

        // Фокус на канвас для ввода
        canvas.requestFocus();
    }

    // ============ PUBLIC API ЭКРАНА ============

    /**
     * Очистка всего экрана цветом
     */
    public void clear(int color) {
        for (int i = 0; i < pixelData.length; i++) {
            pixelData[i] = color;
        }
        repaintCanvas();
    }

    /**
     * Заливка прямоугольной области (нужно для стирания букв)
     */
    public void fillRect(int x, int y, int w, int h, int color) {
        // Простая реализация через циклы
        for (int dy = 0; dy < h; dy++) {
            for (int dx = 0; dx < w; dx++) {
                setPixelSafe(x + dx, y + dy, color);
            }
        }
    }

    /**
     * Рисует текст в заданных координатах.
     * Экран сам разбирается, как превратить строку в пиксели.
     */
    public void drawText(String text, int x, int y, int color) {
        Graphics2D g = displayBuffer.createGraphics();
        // Настройки для четкости текста (пиксель-арт стиль)
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);

        g.setFont(systemFont);
        g.setColor(new Color(color));

        // y + getAscent, потому что drawString рисует от базовой линии, а мы хотим от верхнего угла
        g.drawString(text, x, y + fontMetrics.getAscent());
        g.dispose();
    }

    /**
     * Аппаратный скроллинг экрана вверх.
     * @param amount количество пикселей сдвига
     * @param fillColor цвет, которым зальется новая область снизу
     */
    public void scrollUp(int amount, int fillColor) {
        int totalPixels = width * height;
        int shiftSize = amount * width;

        if (shiftSize >= totalPixels) {
            clear(fillColor);
            return;
        }

        // Сдвигаем память (memmove)
        System.arraycopy(pixelData, shiftSize, pixelData, 0, totalPixels - shiftSize);

        // Заливаем низ
        for (int i = totalPixels - shiftSize; i < totalPixels; i++) {
            pixelData[i] = fillColor;
        }
    }

    // === Вспомогательные методы для Терминала ===

    public int getCharWidth() {
        return fontMetrics.charWidth('W'); // Для моноширинного шрифта
    }

    public int getLineHeight() {
        return fontMetrics.getHeight();
    }

    public int getWidth() { return width; }
    public int getHeight() { return height; }

    public void addInputListener(KeyListener kl) {
        canvas.addKeyListener(kl);
    }

    public void repaintCanvas() {
        canvas.repaint();
    }

    // === Приватные методы ===

    private void setPixelSafe(int x, int y, int color) {
        if (x >= 0 && x < width && y >= 0 && y < height) {
            pixelData[y * width + x] = color;
        }
    }

    // Компонент отрисовки
    private class DisplayCanvas extends Canvas {
        @Override
        public void paint(Graphics g) {
            g.drawImage(displayBuffer, 0, 0, null);
        }
        @Override
        public void update(Graphics g) {
            paint(g); // Убираем мерцание стандартного AWT
        }
    }
}
