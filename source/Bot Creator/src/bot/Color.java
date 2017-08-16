package bot;

public class Color {
    private int red, green, blue, alpha;

    public Color(int red, int green, int blue, int alpha) {
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.alpha = alpha;
    }

    public Color(int red, int green, int blue) {
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.alpha = 255;
    }

    public int get_red() {
        return red;
    }

    public void set_red(int red) {
        this.red = red;
    }

    public int get_green() {
        return green;
    }

    public void set_green(int green) {
        this.green = green;
    }

    public int get_blue() {
        return blue;
    }

    public void set_blue(int blue) {
        this.blue = blue;
    }

    public int get_alpha() {
        return alpha;
    }

    public void set_alpha(int alpha) {
        this.alpha = alpha;
    }

    public boolean equals(Color color) {
        return color.red == red && color.green == green && color.blue == blue && color.alpha == alpha;
    }
}
