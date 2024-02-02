import java.awt.*;
import java.awt.geom.Path2D;

/**
 * @author Oleh Tabachynskyi
 */
public class Horse extends Piece {


    public Horse(Color color, int width, int height) {
        super(color, width, height);
    }

    @Override
    public void paint(Graphics g) {

        Graphics2D g2 = (Graphics2D) g;


        double width = this.getWidth() / 8;
        double height = this.getHeight() / 8;

        drawHorse(g2, width, height);

    }

    private void drawHorse(Graphics2D g2, double width, double height) {
        g2.setStroke(new BasicStroke(3));

        double[] positionY = new double[]{
                7 * height / 8, 7 * height / 8, 2 * height / 8, 2 * height / 8, 3 * height / 8, 4 * height / 8,
                8 * height / 17
        };
        double[] positionX = new double[]{
                width / 3, 2 * width / 3, 2 * width / 3, width / 3, width / 6, width / 4, width / 3,
        };

        Path2D path2D = new Path2D.Double();

        path2D.moveTo(positionX[0], positionY[0]);
        for (int i = 0; i < 3; i++) {
            path2D.lineTo(positionX[i], positionY[i]);
        }

        path2D.curveTo(positionX[2], positionY[2], width / 2, 0, width / 3, positionY[2]);

        path2D.moveTo(positionX[3], positionY[3]);

        for (int i = 3; i < positionX.length; i++) {
            path2D.lineTo(positionX[i], positionY[i]);
        }

        path2D.curveTo(positionX[6], positionY[6], width / 2, height / 2, width / 3, 5 * height / 8);

        path2D.curveTo(width / 3, 5 * height / 8, width / 5, 6 * height / 8, width / 5, positionY[0]);

        path2D.lineTo(positionX[0], positionY[0]);

        path2D.moveTo(positionX[2], positionY[2]);
        path2D.lineTo(positionX[2], height / 15);
        path2D.lineTo(3 * width / 7, height / 4);
        if (color.equals(Color.black)) {
            g2.setColor(Color.white);
            g2.draw(path2D);
            g2.setColor(Color.black);
            g2.fill(path2D);
        } else {
            g2.setColor(Color.black);
            g2.draw(path2D);
            g2.setColor(Color.white);
            g2.fill(path2D);
        }
    }
}
