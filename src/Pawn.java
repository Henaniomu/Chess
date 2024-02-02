import java.awt.*;

/**
 * @author Oleh Tabachynskyi
 */
public class Pawn extends Piece {

    public Pawn(Color color, int width, int height) {
        super(color, width, height);
    }

    @Override
    public void paint(Graphics g) {

        Graphics2D g2d = (Graphics2D) g;

        int width = this.getWidth() / 8;
        int height = this.getHeight() / 8;

        if (color.equals(Color.black)) {
            g2d.setColor(Color.white);
            g2d.drawArc(width / 4, 3 * height / 5, 2 * width / 4, 3 * height / 6, 0, 180);
            g2d.setColor(Color.black);
            g2d.fillArc(width / 4, 3 * height / 5, 2 * width / 4, 3 * height / 6, 0, 180);
            g2d.setColor(Color.white);
            g2d.drawOval(9 * width / 27, 4 * height / 9, width / 3, height / 3);
            g2d.setColor(Color.black);
            g2d.fillOval(9 * width / 27, 4 * height / 9, width / 3, height / 3);
            g2d.setColor(Color.white);
            g2d.drawOval(15 * width / 36, height / 3, width / 6, height / 6);
            g2d.setColor(Color.black);
            g2d.fillOval(15 * width / 36, height / 3, width / 6, height / 6);
        } else {
            g2d.setColor(Color.black);
            g2d.drawArc(width / 4, 3 * height / 5, 2 * width / 4, 3 * height / 6, 0, 180);
            g2d.setColor(Color.white);
            g2d.fillArc(width / 4, 3 * height / 5, 2 * width / 4, 3 * height / 6, 0, 180);
            g2d.setColor(Color.black);
            g2d.drawOval(9 * width / 27, 4 * height / 9, width / 3, height / 3);
            g2d.setColor(Color.white);
            g2d.fillOval(9 * width / 27, 4 * height / 9, width / 3, height / 3);
            g2d.setColor(Color.black);
            g2d.drawOval(15 * width / 36, height / 3, width / 6, height / 6);
            g2d.setColor(Color.white);
            g2d.fillOval(15 * width / 36, height / 3, width / 6, height / 6);
        }
    }
}
