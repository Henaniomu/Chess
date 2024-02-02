import java.awt.*;
import java.awt.geom.Path2D;
/**
 * @author Oleh Tabachynskyi
 */
public class Rook extends Piece {


    public Rook(Color color, int width, int height) {
        super(color, width, height);
    }

    @Override
    public void paint(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        double width = this.getWidth() / 8;
        double height = this.getHeight() / 8;
        g2.setStroke(new BasicStroke(2));
        if (color == Color.white) {
            drawBlackRook(g2, width, height);
        } else {
            drawWhiteRook(g2, width, height);
        }

    }

    private static void drawBlackRook(Graphics2D g2, double width, double height) {
        g2.setColor(Color.BLACK);

        double[] posXup = new double[]{
                width / 7, 2 * width / 7, 2 * width / 7, 3 * width / 7, 3 * width / 7, 4 * width / 7, 4 * width / 7,
                5 * width / 7, 5 * width / 7, 6 * width / 7, 6 * width / 7, width / 7, width / 7
        };

        double[] posYup = new double[]{
                height / 12, height / 12, height / 12 + height / 16, height / 12 + height / 16, height / 12, height / 12,
                height / 12 + height / 16, height / 12 + height / 16, height / 12, height / 12,
                height / 12 + 2 * height / 16, height / 12 + 2 * height / 16, height / 12
        };

        Path2D path2D = new Path2D.Double();
        path2D.moveTo(posXup[0], posYup[0]);
        for (int i = 1; i < posYup.length; i++) {
            path2D.lineTo(posXup[i], posYup[i]);
        }
        path2D.closePath();


        double[] posXmiddle = new double[]{
                width / 4, 3 * width / 4, 3 * width / 4, width / 4
        };
        double[] posYmiddle = new double[]{
                height / 12 + 2 * height / 10, height / 12 + 2 * height / 10, height / 12 + 6 * height / 10,
                height / 12 + 6 * height / 10
        };

        path2D.moveTo(posXmiddle[0], posYmiddle[0]);

        for (int i = 1; i < posYmiddle.length; i++) {
            path2D.lineTo(posXmiddle[i], posYmiddle[i]);
        }
        path2D.lineTo(posXmiddle[0], posYmiddle[0]);
        path2D.lineTo(posXup[11], posYup[11]);
        path2D.moveTo(posXmiddle[1], posYmiddle[1]);
        path2D.lineTo(posXup[10], posYup[10]);


        double[] posXdown = new double[]{
                width / 7, 6 * width / 7, 6 * width / 7, width / 7, width / 7, width / 4
        };
        double[] posYdown = new double[]{
                height / 12 + 6.8 * height / 10, height / 12 + 6.8 * height / 10, height / 12 + 7.5 * height / 10,
                height / 12 + 7.5 * height / 10, height / 12 + 6.8 * height / 10, height / 12 + 6 * height / 10
        };

        path2D.moveTo(posXdown[0], posYdown[0]);
        for (int i = 1; i < posYdown.length; i++) {
            path2D.lineTo(posXdown[i], posYdown[i]);
        }
        path2D.moveTo(posXmiddle[2], posYmiddle[2]);
        path2D.lineTo(posXdown[1], posYdown[1]);

        double[] posXPlatform = new double[]{
                width / 10, 9 * width / 10, 9 * width / 10, width / 10, width / 10
        };
        double[] posYPlatform = new double[]{
                height / 12 + 7.5 * height / 10, height / 12 + 7.5 * height / 10, height / 12 + 8 * height / 10,
                height / 12 + 8 * height / 10, height / 12 + 7.5 * height / 10
        };


        path2D.moveTo(posXPlatform[0], posYPlatform[0]);
        for (int i = 1; i < posXPlatform.length; i++) {
            path2D.lineTo(posXPlatform[i], posYPlatform[i]);
        }

        g2.setColor(Color.WHITE);
        g2.fill(path2D);
        g2.setColor(Color.BLACK);
        g2.draw(path2D);
    }

    private static void drawWhiteRook(Graphics2D g2, double width, double height) {
        g2.setColor(Color.BLACK);

        double[] posXup = new double[]{
                width / 7, 2 * width / 7, 2 * width / 7, 3 * width / 7, 3 * width / 7, 4 * width / 7, 4 * width / 7,
                5 * width / 7, 5 * width / 7, 6 * width / 7, 6 * width / 7, width / 7, width / 7
        };

        double[] posYup = new double[]{
                height / 12, height / 12, height / 12 + height / 16, height / 12 + height / 16, height / 12, height / 12,
                height / 12 + height / 16, height / 12 + height / 16, height / 12, height / 12,
                height / 12 + 2 * height / 16, height / 12 + 2 * height / 16, height / 12
        };


        Path2D path2D = new Path2D.Double();
        path2D.moveTo(posXup[0], posYup[0]);
        for (int i = 1; i < posYup.length; i++) {
            path2D.lineTo(posXup[i], posYup[i]);
        }
        path2D.closePath();
        g2.fill(path2D);
        g2.setColor(Color.WHITE);
        g2.draw(path2D);


        double[] posXmiddle = new double[]{
                width / 4, 3 * width / 4, 3 * width / 4, width / 4
        };
        double[] posYmiddle = new double[]{
                height / 12 + 2 * height / 10, height / 12 + 2 * height / 10, height / 12 + 6 * height / 10,
                height / 12 + 6 * height / 10
        };

        path2D.moveTo(posXup[11], posYup[11]);
        path2D.lineTo(posXmiddle[0], posYmiddle[0]);
        path2D.lineTo(posXmiddle[1], posYmiddle[1]);
        path2D.lineTo(posXup[10], posYup[10]);
        g2.setColor(Color.WHITE);
        g2.draw(path2D);
        g2.setColor(Color.BLACK);
        g2.fill(path2D);


        path2D.moveTo(posXmiddle[0], posYmiddle[0]);

        for (int i = 1; i < posYmiddle.length; i++) {
            path2D.lineTo(posXmiddle[i], posYmiddle[i]);
        }
        path2D.lineTo(posXmiddle[0], posYmiddle[0]);
        path2D.lineTo(posXup[11], posYup[11]);
        path2D.moveTo(posXmiddle[1], posYmiddle[1]);
        path2D.lineTo(posXup[10], posYup[10]);


        g2.setColor(Color.WHITE);
        g2.fill(path2D);

        g2.setColor(Color.BLACK);
        g2.fill(path2D);

        double[] posXdown = new double[]{
                width / 7, 6 * width / 7, 6 * width / 7, width / 7, width / 7, width / 4
        };
        double[] posYdown = new double[]{
                height / 12 + 6.8 * height / 10, height / 12 + 6.8 * height / 10, height / 12 + 7.5 * height / 10,
                height / 12 + 7.5 * height / 10, height / 12 + 6.8 * height / 10, height / 12 + 6 * height / 10
        };

        path2D.moveTo(posXdown[0], posYdown[0]);
        for (int i = 1; i < posYdown.length; i++) {
            path2D.lineTo(posXdown[i], posYdown[i]);
        }
        path2D.moveTo(posXmiddle[2], posYmiddle[2]);
        path2D.lineTo(posXdown[1], posYdown[1]);


        double[] posXPlatform = new double[]{
                width / 10, 9 * width / 10, 9 * width / 10, width / 10, width / 10
        };
        double[] posYPlatform = new double[]{
                height / 12 + 7.5 * height / 10, height / 12 + 7.5 * height / 10, height / 12 + 8 * height / 10,
                height / 12 + 8 * height / 10, height / 12 + 7.5 * height / 10
        };


        path2D.moveTo(posXPlatform[0], posYPlatform[0]);
        for (int i = 1; i < posXPlatform.length; i++) {
            path2D.lineTo(posXPlatform[i], posYPlatform[i]);
        }
        g2.setColor(Color.BLACK);
        g2.fill(path2D);
        g2.setColor(Color.WHITE);
        g2.draw(path2D);

        path2D.moveTo(posXdown[0], posYdown[0]);
        path2D.lineTo(posXmiddle[3], posYmiddle[3]);
        path2D.lineTo(posXmiddle[2], posYmiddle[2]);
        path2D.lineTo(posXdown[1], posYdown[1]);
        g2.setColor(Color.white);
        g2.draw(path2D);
        g2.setColor(Color.BLACK);
        g2.fill(path2D);
    }
}
