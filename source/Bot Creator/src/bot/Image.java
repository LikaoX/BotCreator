package bot;

import java.awt.image.BufferedImage;

public class Image {
    private BufferedImage bufferedImage;

    public Image(BufferedImage bufferedImage) {
        this.bufferedImage = bufferedImage;
    }

    public int get_RGB(int x, int y) {
        return bufferedImage.getRGB(x, y);
    }

    public void set_RGB(int x, int y, int rgb) {
        bufferedImage.setRGB(x, y, rgb);
    }

    public int get_width() {
        return bufferedImage.getWidth();
    }

    public int get_height() {
        return bufferedImage.getHeight();
    }

    public Image get_subimage(int x, int y, int w, int h) {
        return new Image(bufferedImage.getSubimage(x, y, w, h));
    }

    public BufferedImage get_buffered_image() {
        return bufferedImage;
    }
}
