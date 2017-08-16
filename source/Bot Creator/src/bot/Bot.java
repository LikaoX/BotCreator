package bot;

import com.sddev.botmaker.ImageFinder;

import javax.imageio.ImageIO;
import java.awt.Robot;
import java.awt.Rectangle;
import java.awt.GraphicsEnvironment;
import java.awt.GraphicsDevice;
import java.awt.DisplayMode;
import java.awt.AWTException;
import java.awt.event.InputEvent;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public final class Bot {
    public static Path projectDirectory;
    private static Robot robot;
    private static Rectangle rectangle;
    public static final int LEFT = 1, RIGHT = 2, MIDDLE = 3;
    static {
        try {
            robot = new Robot();
        } catch (AWTException ex) {
            ex.printStackTrace();
        }
        GraphicsDevice[] devices = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
        rectangle = new Rectangle(0, devices[0].getDisplayMode().getHeight());
        for(GraphicsDevice device : devices) {
            DisplayMode displayMode = device.getDisplayMode();
            rectangle.width += displayMode.getWidth();
        }
    }

    private Bot() {}

    public static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ex) {

        }
    }

    public static void left_click(int x, int y) {
        mouse_move(x, y);
        mouse_press(LEFT);
        mouse_release(LEFT);
    }

    public static void right_click(int x, int y) {
        mouse_move(x, y);
        mouse_press(RIGHT);
        mouse_release(RIGHT);
    }

    public static void middle_click(int x, int y) {
        mouse_move(x, y);
        mouse_press(MIDDLE);
        mouse_release(MIDDLE);
    }

    public static void click(int x, int y, int button) {
        mouse_move(x, y);
        mouse_press(button);
        mouse_release(button);
    }

    public static void double_left_click(int x, int y) {
        mouse_move(x, y);
        mouse_press(LEFT);
        mouse_release(LEFT);
        mouse_press(LEFT);
        mouse_release(LEFT);
    }

    public static void double_right_click(int x, int y) {
        mouse_move(x, y);
        mouse_press(RIGHT);
        mouse_release(RIGHT);
        mouse_press(RIGHT);
        mouse_release(RIGHT);
    }

    public static void double_middle_click(int x, int y) {
        mouse_move(x, y);
        mouse_press(MIDDLE);
        mouse_release(MIDDLE);
        mouse_press(MIDDLE);
        mouse_release(MIDDLE);
    }

    public static void double_click(int x, int y, int button) {
        mouse_move(x, y);
        mouse_press(button);
        mouse_release(button);
        mouse_press(button);
        mouse_release(button);
    }

    public static void mouse_move(int x, int y) {
        robot.mouseMove(x, y);
    }

    public static void mouse_press(int buttons) {
        int b = 0;
        if(buttons == 1)
            b = InputEvent.BUTTON1_MASK;
        else if(buttons == 3)
            b = InputEvent.BUTTON2_MASK;
        else if(buttons == 2)
            b = InputEvent.BUTTON3_MASK;
        robot.mousePress(b);
    }

    public static void mouse_release(int buttons) {
        int b = 0;
        if(buttons == 1)
            b = InputEvent.BUTTON1_MASK;
        else if(buttons == 3)
            b = InputEvent.BUTTON2_MASK;
        else if(buttons == 2)
            b = InputEvent.BUTTON3_MASK;
        robot.mouseRelease(b);
    }

    public static void key_press(int keyCode) {
        robot.keyPress(keyCode);
    }

    public static void key_release(int keyCode) {
        robot.keyRelease(keyCode);
    }

    public static Color get_pixel_color(int x, int y) {
        java.awt.Color color = robot.getPixelColor(x, y);
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
    }

    public static Image load_image(String path) {
        try {
            try(InputStream inputStream = Files.newInputStream(projectDirectory.resolve(path))) {
                return new Image(ImageIO.read(inputStream));
            }
        } catch(IOException ex) {
            return null;
        }
    }

    public static Image create_screenshot() {
        return new Image(robot.createScreenCapture(rectangle));
    }

    public static Image create_screenshot(int x, int y, int width, int height) {
        return new Image(robot.createScreenCapture(new Rectangle(x, y, width, height)));
    }

    public static Point find_image_on_screen(Image image) {
        return find_image(new Image(robot.createScreenCapture(rectangle)), image);
    }

    public static Point find_image_on_screen(Image image, int x, int y, int width, int height) {
        return find_image(new Image(robot.createScreenCapture(new Rectangle(x, y, width, height))), image);
    }

    public static boolean click_image_on_screen(Image image, int button) {
        Point point = find_image(new Image(robot.createScreenCapture(rectangle)), image);
        if(point == null)
            return false;
        else
            click(point.x, point.y, button);
        return true;
    }

    public static boolean double_click_image_on_screen(Image image, int button) {
        Point point = find_image(new Image(robot.createScreenCapture(rectangle)), image);
        if(point == null)
            return false;
        else
            double_click(point.x, point.y, button);
        return true;
    }

    public static Point find_image(Image parent, Image child) {
        java.awt.Point point = ImageFinder.findImage(parent.get_buffered_image(), child.get_buffered_image(), 20);
        return new Point(point.x, point.y);
    }

    public static Point find_image(Image parent, Image child, int tolerance) {
        java.awt.Point point = ImageFinder.findImage(parent.get_buffered_image(), child.get_buffered_image(), tolerance);
        return new Point(point.x, point.y);
    }
}
