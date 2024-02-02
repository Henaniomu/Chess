import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Timer;
import java.util.TimerTask;

public class MovingCell extends JPanel {
    Piece piece;
    int startX;
    int startY;
    int endX;
    int endY;
    int lX, lY;
    double deltaX, deltaY;
    private Timer t;
    Graphics2D g;
    double moveX, moveY;

    Board b;
    Cell end;

    public MovingCell(Piece p, Cell start, Cell end, Graphics g, Board b) {
        piece = p;
        this.end = end;
        startX = start.getX();
        startY = start.getY();
        endX = end.getX();
        endY = end.getY();
        lX = endX - startX;
        lY = endY - startY;
        deltaX = (double) lX / 500;
        deltaY = (double) lY / 500;
        moveX = 0;
        moveY = 0;
        this.g = (Graphics2D) g;
        this.b = b;
    }


    public void paint() {
        t = new Timer();
        t.schedule(new TimerTask() {
            @Override
            public void run() {
                drawPiece();
                b.repaint();
                if (startX > endX) {
                    if (startY > endY) {
                        if (moveX < lX || moveY < lY) {
                            end.setPiece(piece);
                            t.cancel();
                        }
                    } else if (startY < endY) {
                        if (moveX < lX || moveY > lY) {
                            end.setPiece(piece);
                            t.cancel();
                        }
                    }
                } else if (startX < endX) {
                    if (startY > endY) {
                        if (moveX > lX || moveY < lY) {
                            end.setPiece(piece);
                            t.cancel();
                        }
                    } else if (startY < endY) {
                        if (moveX > lX || moveY > lY) {
                            end.setPiece(piece);
                            t.cancel();
                        }
                    }
                } else {
                    if (startY > endY) {
                        if (moveY < lY) {
                            end.setPiece(piece);
                            t.cancel();
                        }
                    } else if (startY < endY) {
                        if (moveY > lY) {
                            end.setPiece(piece);
                            t.cancel();
                        }
                    }
                }
                if (startY == endY) {
                    if (startX > endX) {
                        if (moveX < lX) {
                            end.setPiece(piece);
                            t.cancel();
                        }
                    } else if (startX < endX) {
                        if (moveX > lX) {
                            end.setPiece(piece);
                            t.cancel();
                        }
                    }
                }
            }
        }, 0, 1);
    }

    private void drawPiece() {
        g.translate(startX + moveX, startY + moveY);
        piece.paint(g);
        g.translate(-(startX + moveX), -(startY + moveY));
        moveX += deltaX;
        moveY += deltaY;
    }
}
