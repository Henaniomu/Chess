import java.awt.*;

/**
 * @author Oleh Tabachynskyi
 */
public abstract class Piece extends Main {

    Color color;
    int width;
    int height;

    public Piece(Color color, int width, int height) {
        this.color = color;
        this.height = height;
        this.width = width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public Color getColor() {
        return color;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    /*Metoda pro vykresleni samostatnych kamenu*/
    public abstract void paint(Graphics g);
}
