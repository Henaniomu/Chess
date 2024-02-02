import java.awt.*;
import java.awt.geom.Path2D;

/**
 * @author Oleh Tabachynskyi
 */
public class King extends Piece {


    public King(Color color, int width, int height) {
        super(color, width, height);
    }

    @Override
    public void paint(Graphics g) {


        Graphics2D g2 = (Graphics2D) g;

        double width = this.getWidth() / 8;
        double height = this.getHeight() / 8;

        g2.setStroke(new BasicStroke(2));
        if (color == Color.black) {
            drawBlackKing(g2, width, height);
        }
        if (color == Color.white) {
            drawWhiteKing(g2, width, height);
        }

    }

    private static void drawWhiteKing(Graphics2D g2, double width, double height) {
        g2.setColor(Color.black);
        double[] positionY = new double[]{
                height, height, 6 * height / 7, 6 * height / 7, 5 * height / 7, 5 * height / 7, 6 * height / 7,
                6 * height / 7, height,
                5 * height / 7, 3 * height / 7, 3 * height / 7, 5 * height / 7,
                3 * height / 7, 3 * height / 7, height / 7, 2 * height / 7, height / 7, 2 * height / 7, height / 7,
                3 * height / 7
        };
        double[] positionX = new double[]{
                width / 4, 3 * width / 4, 3 * width / 4, 2 * width / 3, 2 * width / 3, width / 3, width / 3, width / 4,
                width / 4,
                2 * width / 5, 2 * width / 5, 3 * width / 5, 3 * width / 5,
                width / 3, 2 * width / 3, 2 * width / 3, (2 * width / 3) - 10, 6 * width / 12, (width / 3) + 10, width / 3,
                width / 3
        };


        Path2D path2D = new Path2D.Double();
        path2D.moveTo(positionX[0], positionY[0]);
        for (int i = 1; i < 9; i++) {
            path2D.lineTo(positionX[i], positionY[i]);
        }
        path2D.moveTo(positionX[9], positionY[9]);
        for (int i = 9; i < 13; i++) {
            path2D.lineTo(positionX[i], positionY[i]);
        }
        path2D.moveTo(positionX[13], positionY[13]);
        for (int i = 14; i < 21; i++) {
            path2D.lineTo(positionX[i], positionY[i]);
        }
        g2.draw(path2D);
        g2.setColor(Color.white);
        g2.fill(path2D);
    }

    private static void drawBlackKing(Graphics2D g2, double width, double height) {
        g2.setColor(Color.white);
        double[] positionY = new double[]{
                height, height, 6 * height / 7, 6 * height / 7, 5 * height / 7, 5 * height / 7, 6 * height / 7,
                6 * height / 7, height,
                5 * height / 7, 3 * height / 7, 3 * height / 7, 5 * height / 7,
                3 * height / 7, 3 * height / 7, height / 7, 2 * height / 7, height / 7, 2 * height / 7, height / 7,
                3 * height / 7
        };
        double[] positionX = new double[]{
                width / 4, 3 * width / 4, 3 * width / 4, 2 * width / 3, 2 * width / 3, width / 3, width / 3, width / 4,
                width / 4,
                2 * width / 5, 2 * width / 5, 3 * width / 5, 3 * width / 5,
                width / 3, 2 * width / 3, 2 * width / 3, (2 * width / 3) - 10, 6 * width / 12, (width / 3) + 10, width / 3,
                width / 3
        };


        Path2D path2D = new Path2D.Double();
        path2D.moveTo(positionX[0], positionY[0]);
        for (int i = 1; i < 9; i++) {
            path2D.lineTo(positionX[i], positionY[i]);
        }
        path2D.moveTo(positionX[9], positionY[9]);
        for (int i = 9; i < 13; i++) {
            path2D.lineTo(positionX[i], positionY[i]);
        }
        path2D.moveTo(positionX[13], positionY[13]);
        for (int i = 14; i < 21; i++) {
            path2D.lineTo(positionX[i], positionY[i]);
        }
        g2.draw(path2D);
        g2.setColor(Color.black);
        g2.fill(path2D);
    }
}
