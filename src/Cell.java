import java.awt.*;
/**
 * @author Oleh Tabachynskyi
 */
public class Cell {
    private int x;
    private int y;

    private int width;

    private int heigth;
    private Piece piece;
    public Cell(int x, int y, int width, int heigth, Piece piece) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.heigth = heigth;
        this.piece = piece;
    }


    public void paint(Graphics2D g2) {
        if (piece != null) {
            g2.translate(x, y);
            piece.paint(g2);
            g2.translate(-x, -y);
        }
    }

    public int getWidth() {
        return width;
    }

    public Piece getPiece() {
        return piece;
    }
    public void setPiece(Piece piece) {
        this.piece = piece;
    }

    public boolean isNotEmpty(int x, int y) {
        if (isPiece()) {
            return (x < (this.x + width)) && (x > this.x) && (y < (this.y + heigth)) && (y > this.y);
        }
        return false;
    }

    public boolean isPiece() {
        return piece != null;
    }

    public boolean isEmpty(int x, int y) {
        if (!isPiece()) {
            return (x < (this.x + width)) && (x > this.x) && (y < (this.y + heigth)) && (y > this.y);
        }
        return false;
    }

    public int getX() {
        return x;
    }
    public int getY() {
        return y;
    }
}
