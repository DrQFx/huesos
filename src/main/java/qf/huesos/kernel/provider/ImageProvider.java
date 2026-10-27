package qf.huesos.kernel.provider;

import qf.huesos.kernel.Kernel;
import qf.huesos.kernel.SysCall;
import qf.huesos.kernel.provider.image.Pixel;

import java.util.ArrayList;
import java.util.List;

public class ImageProvider implements Provider{

    public final int DEFAULT_PIXEL_WIDTH = 16;

    SysCall call = new SysCall();

    public ImageProvider() {

    }

    public List<Pixel> parseImage(String filePath) {
        List<Pixel> pixels = new ArrayList<>();

        for (String line : call.parseFile(filePath)) {
            line = line.trim();

            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }

            String[] parts = line.split("/", 3);
            if (parts.length != 3) {
                continue;
            }

            int x = Integer.parseInt(parts[0].trim());
            int y = Integer.parseInt(parts[1].trim());
            int color = Integer.decode(parts[2].trim());

            pixels.add(new Pixel(x, y, color));
        }

        return pixels;
    }

    public void drawImage(String path, int x, int y, int width, int height) {
        List<Pixel> pixels = parseImage(path);

        int imageWidth = pixels.getLast().getX();
        int imageHeight = pixels.getLast().getY();

        int scaleX = width / imageWidth;
        int scaleY = height / imageHeight;

        for (Pixel pixel : pixels) {
            int pX = pixel.getX();
            int pY = pixel.getY();
            int sX = pixel.getX() * scaleX;
            int sY = pixel.getY() * scaleY;
            int pColor = pixel.getColor();

            call.fillRectangle(sX, sY, scaleX, scaleY, pColor);
        }
    }
}
