import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.data.category.DefaultCategoryDataset;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.NumberFormat;
import java.util.*;
import java.util.List;
import java.util.Timer;

/**
 * @author Oleh Tabachynskyi
 */


public class Main extends JPanel {
    private static boolean isWhiteTurn = true;

    private static long start;
    File file = new File("moves.txt");
    static FileWriter fw;

    static int blackKingPos = 4;
    static int whiteKingPos = 60;

    static {
        try {
            fw = new FileWriter("moves.txt");
        } catch (IOException ignored) {
        }
    }

    public static void main(String[] args) {
        JFrame okno = new JFrame();
        okno.setTitle("ChessGame");


        Board board = new Board(720);


        okno.add(board);
        okno.pack();

        board.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                super.componentResized(e);
                board.setRes(true);
                board.repaint();
            }
        });

        /**
         *   'R' - reset hry
         */
        keyListener(board);
        /**
         *  Stisknuti tlacitka zapise kamen do pameti, potom jeho prekresli v bunce, ve ktere jste tlacitko uvolnil.
         */
        mouseListener(okno, board);

        start = System.currentTimeMillis();
        okno.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        okno.setLocationRelativeTo(null); //vycentrovat na obrazovce.
        okno.setVisible(true);
    }

    private static void mouseListener(JFrame okno, Board board) {
        board.addMouseListener(new MouseListener() {
            Piece piece = null;
            int index;

            @Override
            public void mouseClicked(MouseEvent e) {
            }

            @Override
            public void mousePressed(MouseEvent e) {
                /*
                 *  Cyklus pro overeni, jestli pri stisknuti mysi ona byla na bunce s kamenem.
                 */
                for (int i = 0; i < 64; i++) {
                    if (board.getCells().get(i).isNotEmpty(e.getX(), e.getY())) {
                        if (piece != null) {
                            piece = null;
                        }
                        piece = board.getCells().get(i).getPiece();

                        index = i;
                        board.remove(index);
                        board.repaint();
                        System.out.println(i);
                        System.out.println(piece);
                    }
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                /*
                 *  Cyklus overuje, kde se uvolnilo tlacitko, pripadne kresli kamen.
                 */
                if (piece != null) {
                    for (int i = 0; i < 64; i++) {
                        if (board.getCells().get(i).isEmpty(e.getX(), e.getY())) {
                            if (moved(i)) {
                                break;
                            }
                        } else if (board.getCells().get(i).isNotEmpty(e.getX(), e.getY())) {
                            if (moved(i)) {
                                break;
                            }
                        }
                    }
                }
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        control();
                    }
                }, 1000);
            }

            private void control() {
                isNewQueen();
                try {
                    if (isCheckMate()) {
                        board.setCheckMate(true);
                        board.repaint();
                    } else if (isStaleMate()) {
                        board.setStaleMate(true);
                        board.repaint();
                    }
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }

            private boolean moved(int i) {
                Piece killed = board.getCells().get(i).getPiece();
                if ((piece.color.equals(Color.white) && isWhiteTurn) ||
                        ((piece.color.equals(Color.black)) && !isWhiteTurn)) {
                    try {
                        if (canMove(piece, index, i)) {
                            board.getCells().get(i).setPiece(piece);
                            board.getCells().get(index).setPiece(null);
                            for (int j = 0; j < 64; j++) {
                                if (board.getCells().get(j).getPiece() != null) {
                                    if (board.getCells().get(j).getPiece() instanceof King) {
                                        if (board.getCells().get(j).getPiece().color.equals(Color.black))
                                            blackKingPos = j;
                                    }
                                    if (board.getCells().get(j).getPiece() instanceof King) {
                                        if (board.getCells().get(j).getPiece().color.equals(Color.white))
                                            whiteKingPos = j;
                                    }
                                }
                            }
                            try {
                                if ((!isCheck(board.whiteKing, whiteKingPos) && isWhiteTurn) ||
                                        (!isCheck(board.blackKing, blackKingPos) && !isWhiteTurn)) {
                                    long time = (System.currentTimeMillis() - start);
                                    time /= 1000;
                                    String temp = piece.getClass().toString();
                                    fw.write(temp.substring(temp.indexOf(" ") + 1) + " " + index +
                                            " " + i + " " + time + "\n");
                                    fw.flush();
                                    start = System.currentTimeMillis();
                                    board.remove(i);
                                    board.animation(piece, index, i);
                                    isWhiteTurn = !isWhiteTurn;
                                    piece = null;
                                    board.repaint();
                                    return true;
                                } else {
                                    board.getCells().get(i).setPiece(killed);
                                    board.getCells().get(index).setPiece(piece);
                                    piece = null;
                                    board.repaint();
                                    return false;
                                }
                            } catch (IOException ignored) {
                            }
                        }
                    } catch (IOException ignored) {
                    }
                } else {
                    board.getCells().get(index).setPiece(piece);
                    piece = null;
                    board.repaint();
                    return false;
                }
                board.getCells().get(index).setPiece(piece);
                piece = null;
                board.repaint();
                return false;
            }


            public void isNewQueen() {
                /////////////////Zachranim stav sachovnici///////////////
                List<Piece> startPieces = new ArrayList<>();
                List<Integer> startPositions = new ArrayList<>();

                for (int i = 0; i < board.getCells().size(); i++) {
                    if (board.getCells().get(i).getPiece() != null) {
                        startPieces.add(board.getCells().get(i).getPiece());
                        startPositions.add(i);
                    }
                }

                //Je nahore bily?
                for (int i = 0; i < 8; i++) {
                    if (board.getCells().get(i).getPiece() != null) {
                        if (board.getCells().get(i).getPiece() instanceof Pawn) {
                            for (int j = 0; j < startPositions.size(); j++) {
                                board.getCells().get(startPositions.get(j)).setPiece(startPieces.get(j));
                            }
                            board.getCells().get(i).setPiece(new Queen(Color.white,
                                    board.getWidth(), board.getWidth()));
                        }
                    }
                }
                //Je dolu cerny?
                for (int i = 56; i < 64; i++) {
                    if (board.getCells().get(i).getPiece() != null) {
                        if (board.getCells().get(i).getPiece() instanceof Pawn) {
                            for (int j = 0; j < startPositions.size(); j++) {
                                board.getCells().get(startPositions.get(j)).setPiece(startPieces.get(j));
                            }
                            board.getCells().get(i).setPiece(new Queen(Color.black,
                                    board.getWidth(), board.getWidth()));
                        }
                    }
                }
            }


            private boolean isCheckMate() throws IOException {
                if (isCheck(board.blackKing, blackKingPos) && !isWhiteTurn) {
                    /////////////////Zachranim stav sachovnici///////////////
                    List<Piece> startPieces = new ArrayList<>();
                    List<Integer> startPositions = new ArrayList<>();

                    for (int i = 0; i < board.getCells().size(); i++) {
                        if (board.getCells().get(i).getPiece() != null) {
                            startPieces.add(board.getCells().get(i).getPiece());
                            startPositions.add(i);
                        }
                    }
                    ArrayList<Piece> dangerPiece = new ArrayList<>();
                    ArrayList<Integer> dangerPiecePos = new ArrayList<>();
                    ArrayList<Integer> dangerIndex = new ArrayList<>();

                    Piece p;
                    for (int i = 0; i < 64; i++) {
                        if (board.getCells().get(i).getPiece() != null) {
                            if (!board.getCells().get(i).getPiece().color.equals(Color.black)) {
                                p = board.getCells().get(i).getPiece();
                                if (canMove(p, i, blackKingPos)) {
                                    dangerPiece.add(p);
                                    dangerPiecePos.add(i);
                                }
                            }
                        }
                    }
                    ////////Dama??????//////
                    int temp = blackKingPos;

                    try {
                        while (true) {

                            temp -= 8;
                            if (board.getCells().get(temp).getPiece() != null) {
                                break;
                            }
                        }
                        if (board.getCells().get(temp).getPiece() instanceof Queen ||
                                board.getCells().get(temp).getPiece() instanceof Rook) {
                            if (!board.getCells().get(temp).getPiece().color.equals(Color.black)) {
                                dangerPiece.add(board.getCells().get(temp).getPiece());
                                dangerPiecePos.add(temp);
                                while (true) {
                                    temp += 8;
                                    if (temp == blackKingPos) {
                                        break;
                                    }
                                    dangerIndex.add(temp);
                                }
                            }
                        }
                    } catch (Exception ignored) {

                    }
                    temp = blackKingPos;
                    try {
                        while (true) {
                            temp += 8;
                            if (board.getCells().get(temp).getPiece() != null) {
                                break;
                            }
                        }
                        if (board.getCells().get(temp).getPiece() instanceof Queen ||
                                board.getCells().get(temp).getPiece() instanceof Rook) {
                            if (!board.getCells().get(temp).getPiece().color.equals(Color.black)) {
                                dangerPiece.add(board.getCells().get(temp).getPiece());
                                dangerPiecePos.add(temp);
                                while (true) {
                                    temp -= 8;
                                    if (temp == blackKingPos) {
                                        break;
                                    }
                                    dangerIndex.add(temp);
                                }
                            }
                        }
                    } catch (Exception ignored) {

                    }
                    temp = blackKingPos;
                    try {
                        while (true) {
                            temp += 1;
                            if (temp % 8 == 0) {
                                temp -= 1;
                                break;
                            }
                            if (board.getCells().get(temp).getPiece() != null) {
                                break;
                            }
                        }
                        if (board.getCells().get(temp).getPiece() instanceof Queen ||
                                board.getCells().get(temp).getPiece() instanceof Rook) {
                            if (!board.getCells().get(temp).getPiece().color.equals(Color.black)) {
                                dangerPiece.add(board.getCells().get(temp).getPiece());
                                dangerPiecePos.add(temp);
                                while (true) {
                                    temp -= 1;
                                    if (temp == blackKingPos) {
                                        break;
                                    }
                                    dangerIndex.add(temp);
                                }
                            }
                        }
                    } catch (Exception ignored) {

                    }
                    temp = blackKingPos;
                    try {
                        while (true) {
                            temp -= 1;
                            if (temp % 8 == 0) {
                                temp += 1;
                                break;
                            }
                            if (board.getCells().get(temp).getPiece() != null) {
                                break;
                            }
                        }
                        if (board.getCells().get(temp).getPiece() instanceof Queen ||
                                board.getCells().get(temp).getPiece() instanceof Rook) {
                            if (!board.getCells().get(temp).getPiece().color.equals(Color.black)) {
                                dangerPiece.add(board.getCells().get(temp).getPiece());
                                dangerPiecePos.add(temp);
                                while (true) {
                                    temp += 1;
                                    if (temp == blackKingPos) {
                                        break;
                                    }
                                    dangerIndex.add(temp);
                                }
                            }
                        }
                    } catch (Exception ignored) {

                    }

////////////////////////////////////////// Nevim proc se mi nepocita dama, pocitam jeste jednou//////
                    temp = blackKingPos;
                    //chci vedet kdy jsem u kraje, abych zastavil prohledavani
                    ArrayList<Integer> lBoreder = new ArrayList<>();
                    ArrayList<Integer> rBoreder = new ArrayList<>();
                    for (int i = 0; i < 8; i++) {
                        lBoreder.add(i * 8);
                        rBoreder.add(7 + i * 8);
                    }
                    try {
                        while (true) {
                            if (lBoreder.contains(temp)) {
                                break;
                            }
                            temp -= 9;
                            if (board.getCells().get(temp).getPiece() != null) {
                                break;
                            }
                        }
                        if (board.getCells().get(temp).getPiece() instanceof Queen ||
                                board.getCells().get(temp).getPiece() instanceof Bishop) {
                            if (!board.getCells().get(temp).getPiece().color.equals(Color.black)) {
                                dangerPiece.add(board.getCells().get(temp).getPiece());
                                dangerPiecePos.add(temp);
                                while (true) {
                                    temp -= 9;
                                    if (temp == blackKingPos) {
                                        break;
                                    }
                                    dangerIndex.add(temp);
                                }
                            }
                        }
                    } catch (Exception ignored) {

                    }
                    temp = blackKingPos;
                    try {
                        while (true) {
                            if (rBoreder.contains(temp)) {
                                break;
                            }
                            temp += 9;
                            if (board.getCells().get(temp).getPiece() != null) {
                                break;
                            }
                        }
                        if (board.getCells().get(temp).getPiece() instanceof Queen ||
                                board.getCells().get(temp).getPiece() instanceof Bishop) {
                            if (!board.getCells().get(temp).getPiece().color.equals(Color.black)) {
                                dangerPiece.add(board.getCells().get(temp).getPiece());
                                dangerPiecePos.add(temp);
                                while (true) {
                                    temp -= 9;
                                    if (temp == blackKingPos) {
                                        break;
                                    }
                                    dangerIndex.add(temp);
                                }
                            }
                        }
                    } catch (Exception ignored) {

                    }
                    temp = blackKingPos;
                    try {
                        while (true) {
                            if (lBoreder.contains(temp)) {
                                break;
                            }
                            temp += 7;
                            if (board.getCells().get(temp).getPiece() != null) {
                                break;
                            }
                        }
                        if (board.getCells().get(temp).getPiece() instanceof Queen ||
                                board.getCells().get(temp).getPiece() instanceof Bishop) {
                            if (!board.getCells().get(temp).getPiece().color.equals(Color.black)) {
                                dangerPiece.add(board.getCells().get(temp).getPiece());
                                dangerPiecePos.add(temp);
                                while (true) {
                                    temp -= 7;
                                    if (temp == blackKingPos) {
                                        break;
                                    }
                                    dangerIndex.add(temp);
                                }
                            }
                        }
                    } catch (Exception ignored) {

                    }
                    temp = blackKingPos;
                    try {
                        while (true) {
                            if (rBoreder.contains(temp)) {
                                break;
                            }
                            temp -= 7;
                            if (board.getCells().get(temp).getPiece() != null) {
                                break;
                            }
                        }
                        if (board.getCells().get(temp).getPiece() instanceof Queen ||
                                board.getCells().get(temp).getPiece() instanceof Bishop) {
                            if (!board.getCells().get(temp).getPiece().color.equals(Color.black)) {
                                dangerPiece.add(board.getCells().get(temp).getPiece());
                                dangerPiecePos.add(temp);
                                while (true) {
                                    temp += 7;
                                    if (temp == blackKingPos) {
                                        break;
                                    }
                                    dangerIndex.add(temp);
                                }
                            }
                        }
                    } catch (Exception ignored) {

                    }


                    ArrayList<Integer> kingMoves = getKingMoves(blackKingPos);
                    ArrayList<Integer> possible = new ArrayList<>();
                    ArrayList<Piece> mistake = new ArrayList<>();
                    ArrayList<Integer> mistakeIndex = new ArrayList<>();

                    for (int i = 0; i < kingMoves.size(); i++) {
                        if (kingMoves.get(i) >= 0 && kingMoves.get(i) <= 63) {
                            mistakeIndex.add(kingMoves.get(i));
                            mistake.add(board.getCells().get(kingMoves.get(i)).getPiece());
                            if (canMove(board.blackKing, blackKingPos, kingMoves.get(i))) {
                                possible.add(kingMoves.get(i));
                            }
                        }
                    }


                    for (int i = 0; i < mistake.size(); i++) {
                        board.getCells().get(mistakeIndex.get(i)).setPiece(mistake.get(i));
                    }

                    for (int i = 0; i < dangerPiece.size(); i++) {
                        for (int j = 0; j < dangerPiece.size(); j++) {
                            if (dangerPiece.get(i).equals(dangerPiece.get(j))) {
                                dangerPiece.remove(dangerPiece.get(j));
                            }
                        }
                    }

                    if (dangerPiece.size() >= 2) {
                        if (possible.size() == 0) {
                            return true;
                        }
                    }
                    if (possible.size() > 0) {
                        return false;
                    }
                    for (int i = 0; i < dangerPiece.size(); i++) {
                        p = dangerPiece.get(i);

                        if (dangerPiecePos.get(i) < blackKingPos) {
                            for (int j = dangerPiecePos.get(i) + 1; j < blackKingPos; j++) {
                                if (canMove(p, dangerPiecePos.get(i), j)) {
                                    dangerIndex.add(j);
                                }
                            }
                        } else if (dangerPiecePos.get(i) > blackKingPos) {
                            for (int j = blackKingPos + 1; j < dangerPiecePos.get(i); j++) {
                                if (canMove(p, dangerPiecePos.get(i), j)) {
                                    dangerIndex.add(j);
                                }
                            }
                        }
                    }
                    for (int i = 0; i < 64; i++) {
                        if (board.getCells().get(i).getPiece() != null && i != blackKingPos) {
                            if (board.getCells().get(i).getPiece().color.equals(Color.black)) {
                                p = board.getCells().get(i).getPiece();
                                for (Integer index : dangerIndex) {
                                    if (canMove(p, i, index)) {
                                        for (int j = 0; j < startPositions.size(); j++) {
                                            board.getCells().get(startPositions.get(j)).setPiece(startPieces.get(j));
                                        }
                                        return false;
                                    }
                                }
                            }
                        }
                    }

                    for (int i = 0; i < 64; i++) {
                        if (board.getCells().get(i).getPiece() != null) {
                            if (board.getCells().get(i).getPiece().color.equals(Color.black)) {
                                p = board.getCells().get(i).getPiece();
                                for (int j = 0; j < dangerPiecePos.size(); j++) {
                                    if (canMove(p, i, dangerPiecePos.get(j))) {
                                        for (int k = 0; k < startPositions.size(); k++) {
                                            board.getCells().get(startPositions.get(k)).setPiece(startPieces.get(k));
                                        }
                                        return false;
                                    }
                                }
                            }
                        }
                    }
                    for (int j = 0; j < startPositions.size(); j++) {
                        board.getCells().get(startPositions.get(j)).setPiece(startPieces.get(j));
                    }
                    return true;
                }
                if (isCheck(board.whiteKing, whiteKingPos) && isWhiteTurn) {
                    List<Piece> startPieces = new ArrayList<>();
                    List<Integer> startPositions = new ArrayList<>();

                    for (int i = 0; i < board.getCells().size(); i++) {
                        if (board.getCells().get(i).getPiece() != null) {
                            startPieces.add(board.getCells().get(i).getPiece());
                            startPositions.add(i);
                        }
                    }

                    ArrayList<Piece> dangerPiece = new ArrayList<>();
                    ArrayList<Integer> dangerIndex = new ArrayList<>();
                    ArrayList<Integer> dangerPiecePos = new ArrayList<>();


                    Piece p;
                    for (int i = 0; i < 64; i++) {
                        if (board.getCells().get(i).getPiece() != null) {
                            if (!board.getCells().get(i).getPiece().color.equals(Color.white)) {
                                p = board.getCells().get(i).getPiece();
                                if (canMove(p, i, whiteKingPos)) {
                                    dangerPiece.add(p);
                                    dangerPiecePos.add(i);
                                }
                            }
                        }
                    }
                    ////////Dama nevim poc se nepocita zase//////
                    int temp = whiteKingPos;

                    try {
                        while (true) {

                            temp -= 8;
                            if (board.getCells().get(temp).getPiece() != null) {
                                break;
                            }
                        }
                        if (board.getCells().get(temp).getPiece() instanceof Queen ||
                                board.getCells().get(temp).getPiece() instanceof Rook) {
                            if (!board.getCells().get(temp).getPiece().color.equals(Color.white)) {
                                dangerPiece.add(board.getCells().get(temp).getPiece());
                                dangerPiecePos.add(temp);
                                while (true) {
                                    temp += 8;
                                    if (temp == whiteKingPos) {
                                        break;
                                    }
                                    dangerIndex.add(temp);
                                }
                            }
                        }
                    } catch (Exception ignored) {

                    }
                    temp = whiteKingPos;
                    try {
                        while (true) {
                            temp += 8;
                            if (board.getCells().get(temp).getPiece() != null) {
                                break;
                            }
                        }
                        if (board.getCells().get(temp).getPiece() instanceof Queen ||
                                board.getCells().get(temp).getPiece() instanceof Rook) {
                            if (!board.getCells().get(temp).getPiece().color.equals(Color.white)) {
                                dangerPiece.add(board.getCells().get(temp).getPiece());
                                dangerPiecePos.add(temp);
                                while (true) {
                                    temp -= 8;
                                    if (temp == whiteKingPos) {
                                        break;
                                    }
                                    dangerIndex.add(temp);
                                }
                            }
                        }
                    } catch (Exception ignored) {

                    }
                    temp = whiteKingPos;
                    try {
                        while (true) {
                            temp += 1;
                            if (temp % 8 == 0) {
                                temp -= 1;
                                break;
                            }
                            if (board.getCells().get(temp).getPiece() != null) {
                                break;
                            }
                        }
                        if (board.getCells().get(temp).getPiece() instanceof Queen ||
                                board.getCells().get(temp).getPiece() instanceof Rook) {
                            if (!board.getCells().get(temp).getPiece().color.equals(Color.white)) {
                                dangerPiece.add(board.getCells().get(temp).getPiece());
                                dangerPiecePos.add(temp);
                                while (true) {
                                    temp -= 1;
                                    if (temp == whiteKingPos) {
                                        break;
                                    }
                                    dangerIndex.add(temp);
                                }
                            }
                        }
                    } catch (Exception ignored) {

                    }
                    temp = whiteKingPos;
                    try {
                        while (true) {
                            temp -= 1;
                            if (temp % 8 == 0) {
                                temp += 1;
                                break;
                            }
                            if (board.getCells().get(temp).getPiece() != null) {
                                break;
                            }
                        }
                        if (board.getCells().get(temp).getPiece() instanceof Queen ||
                                board.getCells().get(temp).getPiece() instanceof Rook) {
                            if (!board.getCells().get(temp).getPiece().color.equals(Color.white)) {
                                dangerPiece.add(board.getCells().get(temp).getPiece());
                                dangerPiecePos.add(temp);
                                while (true) {
                                    temp += 1;
                                    if (temp == whiteKingPos) {
                                        break;
                                    }
                                    dangerIndex.add(temp);
                                }
                            }
                        }
                    } catch (Exception ignored) {

                    }

////////////////////////////////////////// Nevim proc se mi nepocita dama, pocitam jeste jednou//////
                    temp = whiteKingPos;
                    //chci vedet kdy jsem u kraje, abych zastavil prohledavani
                    ArrayList<Integer> lBoreder = new ArrayList<>();
                    ArrayList<Integer> rBoreder = new ArrayList<>();
                    for (int i = 0; i < 8; i++) {
                        lBoreder.add(i * 8);
                        rBoreder.add(7 + i * 8);
                    }
                    try {
                        while (true) {
                            if (lBoreder.contains(temp)) {
                                break;
                            }
                            temp -= 9;
                            if (board.getCells().get(temp).getPiece() != null) {
                                break;
                            }
                        }
                        if (board.getCells().get(temp).getPiece() instanceof Queen ||
                                board.getCells().get(temp).getPiece() instanceof Bishop) {
                            if (!board.getCells().get(temp).getPiece().color.equals(Color.white)) {
                                dangerPiece.add(board.getCells().get(temp).getPiece());
                                dangerPiecePos.add(temp);
                                while (true) {
                                    temp += 9;
                                    if (temp == whiteKingPos) {
                                        break;
                                    }
                                    dangerIndex.add(temp);
                                }
                            }
                        }
                    } catch (Exception ignored) {

                    }
                    temp = whiteKingPos;
                    try {
                        while (true) {
                            if (rBoreder.contains(temp)) {
                                break;
                            }
                            temp += 9;
                            if (board.getCells().get(temp).getPiece() != null) {
                                break;
                            }
                        }
                        if (board.getCells().get(temp).getPiece() instanceof Queen ||
                                board.getCells().get(temp).getPiece() instanceof Bishop) {
                            if (!board.getCells().get(temp).getPiece().color.equals(Color.white)) {
                                dangerPiece.add(board.getCells().get(temp).getPiece());
                                dangerPiecePos.add(temp);
                                while (true) {
                                    temp -= 9;
                                    if (temp == whiteKingPos) {
                                        break;
                                    }
                                    dangerIndex.add(temp);
                                }
                            }
                        }
                    } catch (Exception ignored) {

                    }
                    temp = whiteKingPos;
                    try {
                        while (true) {
                            if (lBoreder.contains(temp)) {
                                break;
                            }
                            temp += 7;
                            if (board.getCells().get(temp).getPiece() != null) {
                                break;
                            }
                        }
                        if (board.getCells().get(temp).getPiece() instanceof Queen ||
                                board.getCells().get(temp).getPiece() instanceof Bishop) {
                            if (!board.getCells().get(temp).getPiece().color.equals(Color.white)) {
                                dangerPiece.add(board.getCells().get(temp).getPiece());
                                dangerPiecePos.add(temp);
                                while (true) {
                                    temp -= 7;
                                    if (temp == whiteKingPos) {
                                        break;
                                    }
                                    dangerIndex.add(temp);
                                }
                            }
                        }
                    } catch (Exception ignored) {

                    }
                    temp = whiteKingPos;
                    try {
                        while (true) {
                            if (rBoreder.contains(temp)) {
                                break;
                            }
                            temp -= 7;
                            if (board.getCells().get(temp).getPiece() != null) {
                                break;
                            }
                        }
                        if (board.getCells().get(temp).getPiece() instanceof Queen ||
                                board.getCells().get(temp).getPiece() instanceof Bishop) {
                            if (!board.getCells().get(temp).getPiece().color.equals(Color.white)) {
                                dangerPiece.add(board.getCells().get(temp).getPiece());
                                dangerPiecePos.add(temp);
                                while (true) {
                                    temp += 7;
                                    if (temp == whiteKingPos) {
                                        break;
                                    }
                                    dangerIndex.add(temp);
                                }
                            }
                        }
                    } catch (Exception ignored) {

                    }


                    ArrayList<Integer> kingMoves = getKingMoves(whiteKingPos);
                    ArrayList<Integer> possible = new ArrayList<>();
                    ArrayList<Piece> mistake = new ArrayList<>();
                    ArrayList<Integer> mistakeIndex = new ArrayList<>();

                    for (int i = 0; i < kingMoves.size(); i++) {
                        if (kingMoves.get(i) >= 0 && kingMoves.get(i) <= 63) {
                            mistakeIndex.add(kingMoves.get(i));
                            mistake.add(board.getCells().get(kingMoves.get(i)).getPiece());
                            if (canMove(board.whiteKing, whiteKingPos, kingMoves.get(i))) {
                                possible.add(kingMoves.get(i));
                            }
                        }
                    }


                    for (int i = 0; i < mistake.size(); i++) {
                        board.getCells().get(mistakeIndex.get(i)).setPiece(mistake.get(i));
                    }

                    for (int i = 0; i < dangerPiece.size(); i++) {
                        for (int j = 0; j < dangerPiece.size(); j++) {
                            if (dangerPiece.get(i).equals(dangerPiece.get(j))) {
                                dangerPiece.remove(dangerPiece.get(j));
                            }
                        }
                    }

                    if (dangerPiece.size() >= 2) {
                        if (possible.size() == 0) {
                            return true;
                        }
                    }
                    if (possible.size() > 0) {
                        return false;
                    }
                    for (int i = 0; i < dangerPiece.size(); i++) {
                        p = dangerPiece.get(i);

                        if (dangerPiecePos.get(i) < whiteKingPos) {
                            for (int j = dangerPiecePos.get(i) + 1; j < whiteKingPos; j++) {
                                if (canMove(p, dangerPiecePos.get(i), j)) {
                                    dangerIndex.add(j);
                                }
                            }
                        } else if (dangerPiecePos.get(i) > whiteKingPos) {
                            for (int j = whiteKingPos + 1; j < dangerPiecePos.get(i); j++) {
                                if (canMove(p, dangerPiecePos.get(i), j)) {
                                    dangerIndex.add(j);
                                }
                            }
                        }
                    }
                    for (int i = 0; i < 64; i++) {
                        if (board.getCells().get(i).getPiece() != null && i != whiteKingPos) {
                            if (board.getCells().get(i).getPiece().color.equals(Color.white)) {
                                p = board.getCells().get(i).getPiece();
                                for (Integer index : dangerIndex) {
                                    if (canMove(p, i, index)) {
                                        for (int j = 0; j < startPositions.size(); j++) {
                                            board.getCells().get(startPositions.get(j)).setPiece(startPieces.get(j));
                                        }
                                        return false;
                                    }
                                }
                            }
                        }
                    }
                    for (int i = 0; i < 64; i++) {
                        if (board.getCells().get(i).getPiece() != null) {
                            if (board.getCells().get(i).getPiece().color.equals(Color.white)) {
                                p = board.getCells().get(i).getPiece();
                                for (int j = 0; j < dangerPiecePos.size(); j++) {
                                    if (canMove(p, i, dangerPiecePos.get(j))) {
                                        for (int k = 0; k < startPositions.size(); k++) {
                                            board.getCells().get(startPositions.get(k)).setPiece(startPieces.get(k));
                                        }
                                        return false;
                                    }
                                }
                            }
                        }
                    }
                    for (int j = 0; j < startPositions.size(); j++) {
                        board.getCells().get(startPositions.get(j)).setPiece(startPieces.get(j));
                    }
                    return true;
                }
                return false;
            }


            //Vraci cislo radky, kde se nachazi index num
            private int getVertical(int num) {
                if (num < 8) return 1;
                if (num < 16) return 2;
                if (num < 24) return 3;
                if (num < 32) return 4;
                if (num < 40) return 5;
                if (num < 48) return 6;
                if (num < 56) return 7;
                if (num < 64) return 8;
                return -1;
            }

            private boolean isStaleMate() throws IOException {
                /////////////////Zachranim stav sachovnici///////////////
                List<Piece> startPieces = new ArrayList<>();
                List<Integer> startPositions = new ArrayList<>();

                for (int i = 0; i < board.getCells().size(); i++) {
                    if (board.getCells().get(i).getPiece() != null) {
                        startPieces.add(board.getCells().get(i).getPiece());
                        startPositions.add(i);
                    }
                }
                if (!isCheck(board.blackKing, blackKingPos) && !isWhiteTurn) {
                    for (int i = 0; i < 64; i++) {
                        if (board.getCells().get(i).getPiece() != null) {
                            if (board.getCells().get(i).getPiece().color.equals(Color.black)) {
                                Piece p = board.getCells().get(i).getPiece();
                                for (int j = 0; j < 64; j++) {
                                    if (canMove(p, i, j)) {
                                        Piece moved = board.getCells().get(i).getPiece();
                                        board.getCells().get(i).setPiece(null);
                                        if (!isCheck(board.blackKing, blackKingPos)) {
                                            for (int k = 0; k < startPositions.size(); k++) {
                                                board.getCells().get(startPositions.get(k)).setPiece(startPieces.get(k));
                                            }
                                            return false;
                                        }
                                        board.getCells().get(i).setPiece(moved);
                                        for (int k = 0; k < startPositions.size(); k++) {
                                            board.getCells().get(startPositions.get(k)).setPiece(startPieces.get(k));
                                        }
                                        return true;
                                    }
                                }
                            }
                        }
                    }
                    return true;
                } else if (!isCheck(board.whiteKing, whiteKingPos) && isWhiteTurn) {
                    for (int i = 0; i < 64; i++) {
                        if (board.getCells().get(i).getPiece() != null) {
                            if (board.getCells().get(i).getPiece().color.equals(Color.white)) {
                                Piece p = board.getCells().get(i).getPiece();
                                for (int j = 0; j < 64; j++) {
                                    if (canMove(p, i, j)) {
                                        Piece moved = board.getCells().get(i).getPiece();
                                        board.getCells().get(i).setPiece(null);
                                        if (!isCheck(board.whiteKing, whiteKingPos)) {
                                            for (int k = 0; k < startPositions.size(); k++) {
                                                board.getCells().get(startPositions.get(k)).setPiece(startPieces.get(k));
                                            }
                                            return false;
                                        }
                                        board.getCells().get(i).setPiece(moved);
                                        for (int k = 0; k < startPositions.size(); k++) {
                                            board.getCells().get(startPositions.get(k)).setPiece(startPieces.get(k));
                                        }
                                        return true;
                                    }
                                }
                            }
                        }
                    }
                    return true;
                }
                return false;
            }


            public boolean canMove(Piece piece, int initial, int target) throws IOException {
                //Je kamen v ruce?
                if (piece != null) {
                    //Nemuze jit na stejnu bunku
                    if (initial == target) {
                        return false;
                    }
                    //Nemuze jit na bunku s kamenem stejne barvy
                    if (board.getCells().get(target).getPiece() != null) {
                        if (board.getCells().get(target).getPiece().color.equals(piece.color)) {
                            return false;
                        }
                    }
                    //Jestli stoji na zacatku, jde o 2 neobsazene bunky, potom 1
                    if (piece instanceof Pawn) {
                        boolean isFirstMove = false;

                        int[] lastMove = getLastMove();

                        Path path = Paths.get("moves.txt");
                        Scanner sc = new Scanner(path);
                        String lastPiece = "";
                        int count = Files.readAllLines(path).size();
                        for (int i = 0; i < count; i++) {
                            if (sc.hasNext()) {
                                lastPiece = sc.nextLine();
                                lastPiece = lastPiece.substring(0, lastPiece.indexOf(" "));
                            }
                        }
                        if (piece.color.equals(Color.BLACK)) {
                            if (initial >= 8 && initial <= 15) {
                                isFirstMove = true;
                            }
                            if ((target == initial + 8) || ((target == initial + 16) && isFirstMove)) {
                                if (board.getCells().get(target).getPiece() != null) {
                                    return false;
                                } else if (board.getCells().get(target).getPiece() == null) {
                                    if (target == initial + 16) {
                                        if (board.getCells().get(initial + 8).getPiece() != null)
                                            return false;
                                    }
                                    return true;
                                }
                            }
                            if (initial % 8 != 0) {
                                if ((target == initial + 7) && board.getCells().get(target).getPiece() != null) {
                                    if (!board.getCells().get(target).getPiece().getColor().equals(piece.color)) {
                                        return true;
                                    }
                                }
                            }
                            if ((initial - 7) % 8 != 0) {
                                if ((target == initial + 9) && board.getCells().get(target).getPiece() != null) {
                                    if (!board.getCells().get(target).getPiece().getColor().equals(piece.color)) {
                                        return true;
                                    }
                                }
                            }
                            if (lastPiece.equals("Pawn")) {
                                if (lastMove[0] >= 48 && lastMove[0] <= 55) {
                                    if (lastMove[1] == initial - 1 || lastMove[1] == initial + 1) {
                                        if (target == lastMove[1] + 8) {
                                            board.remove(lastMove[1]);
                                            return true;
                                        }
                                    }
                                }
                            }
                        }
                        if (piece.color.equals(Color.WHITE)) {
                            if (initial >= 48 && initial <= 55) {
                                isFirstMove = true;
                            }
                            if ((target == initial - 8) || ((target == initial - 16)) && isFirstMove) {
                                if (board.getCells().get(target).getPiece() != null) {
                                    return false;
                                } else if (board.getCells().get(target).getPiece() == null) {
                                    if (target == initial - 8) {
                                        if (board.getCells().get(initial - 8).getPiece() != null)
                                            return false;
                                    }
                                    return true;
                                }
                            }
                            if ((initial - 7) % 8 != 0) {
                                if ((target == initial - 7) && board.getCells().get(target).getPiece() != null) {
                                    if (!board.getCells().get(target).getPiece().getColor().equals(piece.color)) {
                                        return true;
                                    }
                                }
                            }
                            if (initial % 8 != 0) {
                                if ((target == initial - 9) && board.getCells().get(target).getPiece() != null) {
                                    if (!board.getCells().get(target).getPiece().getColor().equals(piece.color)) {
                                        return true;
                                    }
                                }
                            }
                            if (lastPiece.equals("Pawn")) {
                                if (lastMove[0] >= 8 && lastMove[0] <= 15) {
                                    if (lastMove[1] == initial - 1 || lastMove[1] == initial + 1) {
                                        if (target == lastMove[1] - 8) {
                                            board.remove(lastMove[1]);
                                            return true;
                                        }
                                    }
                                }
                            }
                        }
                    }
                    //Hledam "kriz", kam muze nastoupit
                    if (piece instanceof Rook) {
                        List<Integer> horiz = new ArrayList<>();
                        List<Integer> vert = new ArrayList<>();
                        int row = getVertical(initial);
                        int startX = initial;
                        int startY = -1;
                        while (startX - 8 >= 0) {
                            startX -= 8;
                        }
                        switch (row) {
                            case 1 -> startY = 0;
                            case 2 -> startY = 8;
                            case 3 -> startY = 16;
                            case 4 -> startY = 24;
                            case 5 -> startY = 32;
                            case 6 -> startY = 40;
                            case 7 -> startY = 48;
                            case 8 -> startY = 56;
                        }
                        for (int i = 0; i < 8; i++) {
                            horiz.add(startY + i);
                            vert.add(startX + 8 * i);
                        }

                        int indexHor = horiz.indexOf(initial);
                        int indexVert = vert.indexOf(initial);

                        System.out.println(indexHor);
                        System.out.println(indexVert);

                        if (target > initial) {
                            int countHor = 0;
                            for (int i = indexHor; i < horiz.size(); i++) {
                                if (!board.getCells().get(horiz.get(i)).isPiece()) {
                                    countHor++;
                                } else {
                                    if (!board.getCells().get(horiz.get(i)).getPiece().getColor().equals(piece.getColor()))
                                        countHor++;
                                    break;
                                }
                            }
                            int countVer = 0;
                            for (int i = indexVert; i < vert.size(); i++) {
                                if (!board.getCells().get(vert.get(i)).isPiece()) {
                                    countVer++;
                                } else {
                                    if (!board.getCells().get(vert.get(i)).getPiece().getColor().equals(piece.getColor()))
                                        countVer++;
                                    break;
                                }
                            }

                            for (int i = 0; i < countHor; i++) {
                                if (horiz.get(indexHor + i) == target) {
                                    return true;
                                }
                            }
                            for (int i = 0; i < countVer; i++) {
                                if (vert.get(indexVert + i) == target) {
                                    return true;
                                }
                            }
                        } else {
                            int countHor = 0;
                            for (int i = indexHor; i >= 0; i--) {
                                if (!board.getCells().get(horiz.get(i)).isPiece()) {
                                    countHor++;
                                } else {
                                    if (!board.getCells().get(horiz.get(i)).getPiece().getColor().equals(piece.getColor()))
                                        countHor++;
                                    break;
                                }

                            }
                            int countVert = 0;
                            for (int i = indexVert; i >= 0; i--) {
                                if (!board.getCells().get(vert.get(i)).isPiece()) {
                                    countVert++;
                                } else {
                                    if (!board.getCells().get(vert.get(i)).getPiece().getColor().equals(piece.getColor()))
                                        countVert++;
                                    break;
                                }
                            }

                            for (int i = 0; i < countHor; i++) {
                                if (horiz.get(indexHor - i) == target) {
                                    return true;
                                }
                            }
                            for (int i = 0; i < countVert; i++) {
                                if (vert.get(indexVert - i) == target) {
                                    return true;
                                }
                            }
                        }
                    }
                    //Specialni pripad, nemuze jit na bunky, kde dostaneme sach
                    if (piece instanceof King) {
                        return !isKingMoveCheck(piece, initial, target) || castling(piece, initial, target);
                    }
                    //Jednoduse nachazim bunky, kam muze jit
                    if (piece instanceof Horse) {
                        List<Integer> moves = new ArrayList<>();
                        int vert = getVertical(initial);
                        int targetVert = getVertical(target);
                        if (targetVert == vert) {
                            return false;
                        }

                        if (vert - getVertical(initial + 1 + 1 - 8) == 1)
                            moves.add(initial + 1 + 1 - 8);
                        if (vert - getVertical(initial - 1 - 1 - 8) == 1)
                            moves.add(initial - 1 - 1 - 8);
                        if (getVertical(initial + 1 + 1 + 8) - vert == 1)
                            moves.add(initial + 1 + 1 + 8);
                        if (getVertical(initial - 1 - 1 + 8) - vert == 1)
                            moves.add(initial - 1 - 1 + 8);
                        if (vert - getVertical(initial - 8 - 8 + 1) == 2)
                            moves.add(initial - 8 - 8 + 1);
                        if (vert - getVertical(initial - 8 - 8 - 1) == 2)
                            moves.add(initial - 8 - 8 - 1);
                        if (getVertical(initial + 8 + 8 - 1) - vert == 2)
                            moves.add(initial + 8 + 8 - 1);
                        if (getVertical(initial + 8 + 8 + 1) - vert == 2)
                            moves.add(initial + 8 + 8 + 1);

                        return moves.contains(target);
                    }
                    //Hledam diagonalu a bunku, na ktere musi zastavit
                    if (piece instanceof Bishop) {
                        List<Integer> borders = new ArrayList<>();
                        List<Integer> rBorders = new ArrayList<>();
                        List<Integer> lBorders = new ArrayList<>();
                        List<Integer> leftDiagonal = new ArrayList<>();
                        List<Integer> rightDiagonal = new ArrayList<>();
                        for (int i = 0; i < 8; i++) {
                            lBorders.add(i * 8);
                            rBorders.add(7 + i * 8);
                            borders.add(i);
                            borders.add(56 + i);
                            borders.add(i * 8);
                            borders.add(7 + i * 8);
                        }
                        int startLeft = initial;
                        int startRight = initial;

                        if (initial > 7) {
                            do {
                                if ((piece.color.equals(Color.white) && initial == 8)) {
                                    break;
                                }
                                startLeft -= 9;
                            } while (!borders.contains(startLeft));
                            do {
                                startRight -= 7;
                            } while (!borders.contains(startRight));
                        }

                        if (lBorders.contains(initial)) {
                            startLeft = initial;
                        }
                        if (rBorders.contains(initial)) {
                            startRight = initial;
                        }

                        do {
                            if (initial == 56) {
                                break;
                            }
                            leftDiagonal.add(startLeft);
                            startLeft += 9;
                        } while (!borders.contains(startLeft));
                        leftDiagonal.add(startLeft);
                        do {
                            if (initial == 63) {
                                break;
                            }
                            rightDiagonal.add(startRight);
                            startRight += 7;
                        } while (!borders.contains(startRight));
                        rightDiagonal.add(startRight);


                        if (target < initial) {
                            int positionIndex = -1;
                            int blockIndex = 0;
                            if (leftDiagonal.contains(target)) {
                                for (int i = 0; i < leftDiagonal.size(); i++) {
                                    if (leftDiagonal.get(i).equals(initial)) {
                                        positionIndex = i;
                                        break;
                                    }
                                }
                                for (int i = positionIndex; i > 0; i--) {
                                    if (board.getCells().get(leftDiagonal.get(i)).getPiece() != null) {
                                        blockIndex = i;
                                        if (board.getCells().get(leftDiagonal.get(i)).getPiece().color.equals(piece.color)) {
                                            blockIndex += 1;
                                            break;
                                        }
                                    }
                                }
                                for (int i = blockIndex; i < positionIndex; i++) {
                                    if (leftDiagonal.get(i).equals(target)) {
                                        return true;
                                    }
                                }
                            }
                            if (rightDiagonal.contains(target)) {
                                for (int i = 0; i < rightDiagonal.size(); i++) {
                                    if (rightDiagonal.get(i).equals(initial)) {
                                        positionIndex = i;
                                        break;
                                    }
                                }
                                for (int i = positionIndex; i > 0; i--) {
                                    if (board.getCells().get(rightDiagonal.get(i)).getPiece() != null) {
                                        blockIndex = i;
                                        if (board.getCells().get(rightDiagonal.get(i)).getPiece().color.equals(piece.color)) {
                                            blockIndex += 1;
                                            break;
                                        }
                                    }
                                }
                                for (int i = blockIndex; i < positionIndex; i++) {
                                    if (rightDiagonal.get(i).equals(target)) {
                                        return true;
                                    }
                                }
                            }
                        } else {
                            int positionIndex = -1;
                            int blockIndex = 0;
                            if (leftDiagonal.contains(target)) {
                                for (int i = 0; i < leftDiagonal.size(); i++) {
                                    if (leftDiagonal.get(i).equals(initial)) {
                                        positionIndex = i;
                                        blockIndex = positionIndex;
                                        break;
                                    }
                                }
                                for (int i = positionIndex + 1; i < leftDiagonal.size(); i++) {
                                    blockIndex += 1;
                                    if (board.getCells().get(leftDiagonal.get(i)).getPiece() != null) {
                                        blockIndex = i;
                                        break;
                                    }
                                }
                                if (board.getCells().get(leftDiagonal.get(blockIndex)).getPiece() != null) {
                                    if (board.getCells().get(leftDiagonal.get(blockIndex)).getPiece().color.equals(piece.color)) {
                                        blockIndex -= 1;
                                    }
                                }
                                for (int i = positionIndex; i <= blockIndex; i++) {
                                    if (leftDiagonal.get(i).equals(target)) {
                                        return true;
                                    }
                                }
                            }
                            if (rightDiagonal.contains(target)) {
                                for (int i = 0; i < rightDiagonal.size(); i++) {
                                    if (rightDiagonal.get(i).equals(initial)) {
                                        positionIndex = i;
                                        blockIndex = positionIndex;
                                        break;
                                    }
                                }
                                for (int i = positionIndex + 1; i < rightDiagonal.size(); i++) {
                                    blockIndex += 1;
                                    if (board.getCells().get(rightDiagonal.get(i)).getPiece() != null) {
                                        blockIndex = i;
                                        break;
                                    }
                                }
                                if (board.getCells().get(rightDiagonal.get(blockIndex)).getPiece() != null) {
                                    if (board.getCells().get(rightDiagonal.get(blockIndex)).getPiece().color.equals(piece.color)) {
                                        blockIndex -= 1;
                                    }
                                }
                                for (int i = positionIndex; i <= blockIndex; i++) {
                                    if (rightDiagonal.get(i).equals(target)) {
                                        return true;
                                    }
                                }
                            }
                        }
                    }
                    //Bishop+Rook
                    if (piece instanceof Queen) {
                        List<Integer> horiz = new ArrayList<>();
                        List<Integer> vert = new ArrayList<>();
                        int row = getVertical(initial);
                        int startX = initial;
                        int startY = -1;
                        while (startX - 8 >= 0) {
                            startX -= 8;
                        }
                        switch (row) {
                            case 1 -> startY = 0;
                            case 2 -> startY = 8;
                            case 3 -> startY = 16;
                            case 4 -> startY = 24;
                            case 5 -> startY = 32;
                            case 6 -> startY = 40;
                            case 7 -> startY = 48;
                            case 8 -> startY = 56;
                        }
                        for (int i = 0; i < 8; i++) {
                            horiz.add(startY + i);
                            vert.add(startX + 8 * i);
                        }

                        int indexHor = horiz.indexOf(initial);
                        int indexVert = vert.indexOf(initial);


                        if (target > initial) {
                            int countHor = 0;
                            for (int i = indexHor; i < horiz.size(); i++) {
                                if (!board.getCells().get(horiz.get(i)).isPiece()) {
                                    countHor++;
                                } else {
                                    if (!board.getCells().get(horiz.get(i)).getPiece().getColor().equals(piece.getColor()))
                                        countHor++;
                                    break;
                                }
                            }
                            int countVer = 0;
                            for (int i = indexVert; i < vert.size(); i++) {
                                if (!board.getCells().get(vert.get(i)).isPiece()) {
                                    countVer++;
                                } else {
                                    if (!board.getCells().get(vert.get(i)).getPiece().getColor().equals(piece.getColor()))
                                        countVer++;
                                    break;
                                }
                            }

                            for (int i = 0; i < countHor; i++) {
                                if (horiz.get(indexHor + i) == target) {
                                    return true;
                                }
                            }
                            for (int i = 0; i < countVer; i++) {
                                if (vert.get(indexVert + i) == target) {
                                    return true;
                                }
                            }
                        } else {
                            int countHor = 0;
                            for (int i = indexHor; i >= 0; i--) {
                                if (!board.getCells().get(horiz.get(i)).isPiece()) {
                                    countHor++;
                                } else {
                                    if (!board.getCells().get(horiz.get(i)).getPiece().getColor().equals(piece.getColor()))
                                        countHor++;
                                    break;
                                }

                            }
                            int countVert = 0;
                            for (int i = indexVert; i >= 0; i--) {
                                if (!board.getCells().get(vert.get(i)).isPiece()) {
                                    countVert++;
                                } else {
                                    if (!board.getCells().get(vert.get(i)).getPiece().getColor().equals(piece.getColor()))
                                        countVert++;
                                    break;
                                }
                            }

                            for (int i = 0; i < countHor; i++) {
                                if (horiz.get(indexHor - i) == target) {
                                    return true;
                                }
                            }
                            for (int i = 0; i < countVert; i++) {
                                if (vert.get(indexVert - i) == target) {
                                    return true;
                                }
                            }
                        }
                        List<Integer> borders = new ArrayList<>();
                        List<Integer> rBorders = new ArrayList<>();
                        List<Integer> lBorders = new ArrayList<>();
                        List<Integer> leftDiagonal = new ArrayList<>();
                        List<Integer> rightDiagonal = new ArrayList<>();
                        for (int i = 0; i < 8; i++) {
                            lBorders.add(i * 8);
                            rBorders.add(7 + i * 8);
                            borders.add(i);
                            borders.add(56 + i);
                            borders.add(i * 8);
                            borders.add(7 + i * 8);
                        }
                        int startLeft = initial;
                        int startRight = initial;

                        if (initial > 7) {
                            do {
                                if ((piece.color.equals(Color.white) && initial == 8)) {
                                    break;
                                }
                                startLeft -= 9;
                            } while (!borders.contains(startLeft));
                            do {
                                startRight -= 7;
                            } while (!borders.contains(startRight));
                        }

                        if (lBorders.contains(initial)) {
                            startLeft = initial;
                        }
                        if (rBorders.contains(initial)) {
                            startRight = initial;
                        }


                        do {
                            if (initial == 56) {
                                break;
                            }
                            leftDiagonal.add(startLeft);
                            startLeft += 9;
                        } while (!borders.contains(startLeft));
                        leftDiagonal.add(startLeft);
                        do {
                            if (initial == 63) {
                                break;
                            }
                            rightDiagonal.add(startRight);
                            startRight += 7;
                        } while (!borders.contains(startRight));
                        rightDiagonal.add(startRight);


                        if (target < initial) {
                            int positionIndex = -1;
                            int blockIndex = 0;
                            if (leftDiagonal.contains(target)) {
                                for (int i = 0; i < leftDiagonal.size(); i++) {
                                    if (leftDiagonal.get(i).equals(initial)) {
                                        positionIndex = i;
                                        break;
                                    }
                                }
                                for (int i = positionIndex; i > 0; i--) {
                                    if (board.getCells().get(leftDiagonal.get(i)).getPiece() != null) {
                                        blockIndex = i;
                                        if (board.getCells().get(leftDiagonal.get(i)).getPiece().color.equals(piece.color)) {
                                            blockIndex += 1;
                                            break;
                                        }
                                    }
                                }
                                for (int i = blockIndex; i < positionIndex; i++) {
                                    if (leftDiagonal.get(i).equals(target)) {
                                        return true;
                                    }
                                }
                            }
                            if (rightDiagonal.contains(target)) {
                                for (int i = 0; i < rightDiagonal.size(); i++) {
                                    if (rightDiagonal.get(i).equals(initial)) {
                                        positionIndex = i;
                                        break;
                                    }
                                }
                                for (int i = positionIndex; i > 0; i--) {
                                    if (board.getCells().get(rightDiagonal.get(i)).getPiece() != null) {
                                        blockIndex = i;
                                        if (board.getCells().get(rightDiagonal.get(i)).getPiece().color.equals(piece.color)) {
                                            blockIndex += 1;
                                            break;
                                        }
                                    }
                                }
                                for (int i = blockIndex; i < positionIndex; i++) {
                                    if (rightDiagonal.get(i).equals(target)) {
                                        return true;
                                    }
                                }
                            }
                        } else {
                            int positionIndex = -1;
                            int blockIndex = -1;
                            if (leftDiagonal.contains(target)) {
                                for (int i = 0; i < leftDiagonal.size(); i++) {
                                    if (leftDiagonal.get(i).equals(initial)) {
                                        positionIndex = i;
                                        blockIndex = positionIndex;
                                        break;
                                    }
                                }
                                for (int i = positionIndex + 1; i < leftDiagonal.size(); i++) {
                                    blockIndex += 1;
                                    if (board.getCells().get(leftDiagonal.get(i)).getPiece() != null) {
                                        blockIndex = i;
                                        break;
                                    }
                                }
                                if (board.getCells().get(leftDiagonal.get(blockIndex)).getPiece() != null) {
                                    if (board.getCells().get(leftDiagonal.get(blockIndex)).getPiece().color.equals(piece.color)) {
                                        blockIndex -= 1;
                                    }
                                }
                                for (int i = positionIndex; i <= blockIndex; i++) {
                                    if (leftDiagonal.get(i).equals(target)) {
                                        return true;
                                    }
                                }
                            }
                            if (rightDiagonal.contains(target)) {
                                for (int i = 0; i < rightDiagonal.size(); i++) {
                                    if (rightDiagonal.get(i).equals(initial)) {
                                        positionIndex = i;
                                        blockIndex = positionIndex;
                                        break;
                                    }
                                }
                                for (int i = positionIndex + 1; i < rightDiagonal.size(); i++) {
                                    blockIndex += 1;
                                    if (board.getCells().get(rightDiagonal.get(i)).getPiece() != null) {
                                        blockIndex = i;
                                        break;
                                    }
                                }
                                if (board.getCells().get(rightDiagonal.get(blockIndex)).getPiece() != null) {
                                    if (board.getCells().get(rightDiagonal.get(blockIndex)).getPiece().color.equals(piece.color)) {
                                        blockIndex -= 1;
                                    }
                                }
                                for (int i = positionIndex; i <= blockIndex; i++) {
                                    if (rightDiagonal.get(i).equals(target)) {
                                        return true;
                                    }
                                }
                            }
                        }
                    }
                }
                return false;
            }

            private boolean castling(Piece piece, int initial, int target) throws IOException {
                boolean blackLeftRookMoved = false;
                boolean blackRightRookMoved = false;
                boolean whiteLeftRookMoved = false;
                boolean whiteRightRookMoved = false;
                boolean blackKingMoved = false;
                boolean whiteKingMoved = false;

                Path path = Paths.get("moves.txt");
                Scanner sc = new Scanner(path);
                while (sc.hasNext()) {
                    String temp = sc.nextLine();
                    if (temp.substring(0, temp.indexOf(" ")).equals("Rook")) {
                        temp = temp.substring(temp.indexOf(" ") + 1);
                        if (temp.substring(0, temp.indexOf(" ")).equals("0"))
                            blackLeftRookMoved = true;
                        if (temp.substring(0, temp.indexOf(" ")).equals("7"))
                            blackRightRookMoved = true;
                        if (temp.substring(0, temp.indexOf(" ")).equals("56"))
                            whiteLeftRookMoved = true;
                        if (temp.substring(0, temp.indexOf(" ")).equals("63"))
                            whiteRightRookMoved = true;
                    } else if (temp.substring(0, temp.indexOf(" ")).equals("King")) {
                        temp = temp.substring(temp.indexOf(" ") + 1);
                        if (temp.substring(0, temp.indexOf(" ")).equals("4"))
                            blackKingMoved = true;
                        if (temp.substring(0, temp.indexOf(" ")).equals("60"))
                            whiteKingMoved = true;
                    }
                }

                if (!whiteKingMoved) {
                    if (piece.color.equals(Color.WHITE) && target == 62 && initial == 60 && !whiteRightRookMoved) {
                        if (board.getCells().get(63).getPiece() != null) {
                            if (!(board.getCells().get(63).getPiece() instanceof Rook)) {
                                return false;
                            }
                        }
                        if (board.getCells().get(63).getPiece() == null) {
                            return false;
                        }
                        if (isCheck(piece, initial)) {
                            return false;
                        }
                        for (int i = 61; i < 63; i++) {
                            if (board.getCells().get(i).getPiece() != null) {
                                return false;
                            }
                            for (int j = 0; j < 63; j++) {
                                if (board.getCells().get(j).getPiece() != null) {
                                    if (!board.getCells().get(j).getPiece().color.equals(piece.color)) {
                                        if (canMove(board.getCells().get(j).getPiece(), j, i)) {
                                            return false;
                                        }
                                    }
                                }
                            }
                        }
                        board.getCells().get(62).setPiece(piece);
                        Piece Rook = board.getCells().get(63).getPiece();
                        board.getCells().get(63).setPiece(null);
                        board.getCells().get(61).setPiece(Rook);
                        board.animation(board.whiteKing, initial, target);
                        board.animation(board.whiteRook1, 63, 61);
                        return true;
                    } else if (piece.color.equals(Color.WHITE) && target == 58 && initial == 60 && !whiteLeftRookMoved) {
                        if (board.getCells().get(56).getPiece() != null) {
                            if (!(board.getCells().get(56).getPiece() instanceof Rook)) {
                                return false;
                            }
                        }
                        if (board.getCells().get(56).getPiece() == null) {
                            return false;
                        }
                        if (isCheck(piece, initial)) {
                            return false;
                        }
                        for (int i = 57; i < 60; i++) {
                            if (board.getCells().get(i).getPiece() != null) {
                                return false;
                            }
                            for (int j = 0; j < 64; j++) {
                                if (board.getCells().get(j).getPiece() != null) {
                                    if (!board.getCells().get(j).getPiece().color.equals(piece.color)) {
                                        if (canMove(board.getCells().get(j).getPiece(), j, i)) {
                                            return false;
                                        }
                                    }
                                }
                            }
                        }
                        board.getCells().get(58).setPiece(piece);
                        Piece Rook = board.getCells().get(56).getPiece();
                        board.getCells().get(56).setPiece(null);
                        board.getCells().get(59).setPiece(Rook);
                        board.animation(board.whiteKing, initial, target);
                        board.animation(board.whiteRook0, 56, 59);
                        return true;
                    }
                }
                if (!blackKingMoved) {
                    if (piece.color.equals(Color.black) && target == 2 && initial == 4 && !blackLeftRookMoved) {
                        if (board.getCells().get(0).getPiece() != null) {
                            if (!(board.getCells().get(0).getPiece() instanceof Rook)) {
                                return false;
                            }
                        }
                        if (board.getCells().get(0).getPiece() == null) {
                            return false;
                        }
                        if (isCheck(piece, initial)) {
                            return false;
                        }
                        for (int i = 1; i < initial; i++) {
                            if (board.getCells().get(i).getPiece() != null) {
                                return false;
                            }
                            for (int j = 0; j < 64; j++) {
                                if (board.getCells().get(j).getPiece() != null) {
                                    if (!board.getCells().get(j).getPiece().color.equals(piece.color)) {
                                        if (canMove(board.getCells().get(j).getPiece(), j, i)) {
                                            return false;
                                        }
                                    }
                                }
                            }
                        }
                        board.getCells().get(2).setPiece(piece);
                        Piece Rook = board.getCells().get(0).getPiece();
                        board.getCells().get(0).setPiece(null);
                        board.getCells().get(3).setPiece(Rook);
                        board.animation(board.blackKing, initial, target);
                        board.animation(board.blackRook0, 0, 3);
                        return true;
                    } else if (piece.color.equals(Color.black) && target == 6 && initial == 4 && !blackRightRookMoved) {
                        if (board.getCells().get(7).getPiece() != null) {
                            if (!(board.getCells().get(7).getPiece() instanceof Rook)) {
                                return false;
                            }
                        }
                        if (board.getCells().get(7).getPiece() == null) {
                            return false;
                        }
                        if (isCheck(piece, initial)) {
                            return false;
                        }
                        for (int i = initial + 1; i < 7; i++) {
                            if (board.getCells().get(i).getPiece() != null) {
                                return false;
                            }
                            for (int j = 0; j < 64; j++) {
                                if (board.getCells().get(j).getPiece() != null) {
                                    if (!board.getCells().get(j).getPiece().color.equals(piece.color)) {
                                        if (canMove(board.getCells().get(j).getPiece(), j, i)) {
                                            return false;
                                        }
                                    }
                                }
                            }
                        }
                        board.getCells().get(6).setPiece(piece);
                        Piece Rook = board.getCells().get(7).getPiece();
                        board.getCells().get(7).setPiece(null);
                        board.getCells().get(5).setPiece(Rook);
                        board.animation(board.blackKing, initial, target);
                        board.animation(board.blackRook1, 7, 5);
                        return true;
                    }
                }
                return false;
            }

            private boolean isKingMoveCheck(Piece piece, int initial, int target) throws IOException {
                Piece targetPiece = board.getCells().get(target).getPiece();
                board.getCells().get(target).setPiece(piece);
                ArrayList<Integer> moves = getKingMoves(initial);
                if (!moves.contains(target)) {
                    board.getCells().get(target).setPiece(targetPiece);
                    return true;
                }

                for (int i = 0; i < 64; i++) {
                    if (board.getCells().get(i).getPiece() != null) {
                        if (!(board.getCells().get(i).getPiece() instanceof King) &&
                                !(board.getCells().get(i).getPiece().color.equals(piece.color))) {
                            if (canMove(board.getCells().get(i).getPiece(), i, target)) {
                                board.getCells().get(target).setPiece(targetPiece);
                                return true;
                            }
                        }
                    }
                }
////////////////////////////////////////Zvlast pocitam krale//////////////////////////////////////
                int enemyKingPos = -1;

                for (int i = 0; i < 64; i++) {
                    if ((board.getCells().get(i).getPiece() instanceof King) &&
                            !(board.getCells().get(i).getPiece().getColor().equals(piece.color))) {
                        enemyKingPos = i;
                    }
                }
                ////////////////////Nevim proc se nepocita dama, jeste jednou pocitam a pro jistotu Rook a Bishop///
                int temp = target;
                try {
                    while (true) {
                        temp -= 8;
                        if (board.getCells().get(temp).getPiece() != null) {
                            break;
                        }
                    }
                    if (board.getCells().get(temp).getPiece() instanceof Queen ||
                            board.getCells().get(temp).getPiece() instanceof Rook) {
                        if (!board.getCells().get(temp).getPiece().color.equals(piece.color)) {
                            board.getCells().get(target).setPiece(targetPiece);
                            return true;
                        }
                    }
                } catch (Exception ignored) {

                }
                temp = target;
                try {
                    while (true) {
                        temp += 8;
                        if (board.getCells().get(temp).getPiece() != null) {
                            break;
                        }
                    }
                    if (board.getCells().get(temp).getPiece() instanceof Queen ||
                            board.getCells().get(temp).getPiece() instanceof Rook) {
                        if (!board.getCells().get(temp).getPiece().color.equals(piece.color)) {
                            board.getCells().get(target).setPiece(targetPiece);
                            return true;
                        }
                    }
                } catch (Exception ignored) {

                }
                temp = target;
                try {
                    while (true) {
                        temp += 1;
                        if (temp % 8 == 0) {
                            temp -= 1;
                            break;
                        }
                        if (board.getCells().get(temp).getPiece() != null) {
                            break;
                        }
                    }
                    if (board.getCells().get(temp).getPiece() instanceof Queen ||
                            board.getCells().get(temp).getPiece() instanceof Rook) {
                        if (!board.getCells().get(temp).getPiece().color.equals(piece.color)) {
                            board.getCells().get(target).setPiece(targetPiece);
                            return true;
                        }
                    }
                } catch (Exception ignored) {

                }
                temp = target;
                try {
                    while (true) {
                        temp -= 1;
                        if (temp % 8 == 0) {
                            temp += 1;
                            break;
                        }
                        if (board.getCells().get(temp).getPiece() != null) {
                            break;
                        }
                    }
                    if (board.getCells().get(temp).getPiece() instanceof Queen ||
                            board.getCells().get(temp).getPiece() instanceof Rook) {
                        if (!board.getCells().get(temp).getPiece().color.equals(piece.color)) {
                            board.getCells().get(target).setPiece(targetPiece);
                            return true;
                        }
                    }
                } catch (Exception ignored) {

                }

////////////////////////////////////////// Nevim proc se mi nepocita dama, pocitam jeste jednou//////
                temp = target;
                //chci vedet kdy jsem u kraje, abych zastavil prohledavani
                ArrayList<Integer> lBoreder = new ArrayList<>();
                ArrayList<Integer> rBoreder = new ArrayList<>();
                for (int i = 0; i < 8; i++) {
                    lBoreder.add(i * 8);
                    rBoreder.add(7 + i * 8);
                }
                try {
                    while (true) {
                        if (lBoreder.contains(temp)) {
                            break;
                        }
                        temp -= 9;
                        if (board.getCells().get(temp).getPiece() != null) {
                            break;
                        }
                    }
                    if (board.getCells().get(temp).getPiece() instanceof Queen ||
                            board.getCells().get(temp).getPiece() instanceof Bishop) {
                        if (!board.getCells().get(temp).getPiece().color.equals(piece.color)) {
                            board.getCells().get(target).setPiece(targetPiece);
                            return true;
                        }
                    }
                } catch (Exception ignored) {

                }
                temp = target;
                try {
                    while (true) {
                        if (rBoreder.contains(temp)) {
                            break;
                        }
                        temp += 9;
                        if (board.getCells().get(temp).getPiece() != null) {
                            break;
                        }
                    }
                    if (board.getCells().get(temp).getPiece() instanceof Queen ||
                            board.getCells().get(temp).getPiece() instanceof Bishop) {
                        if (!board.getCells().get(temp).getPiece().color.equals(piece.color)) {
                            board.getCells().get(target).setPiece(targetPiece);
                            return true;
                        }
                    }
                } catch (Exception ignored) {

                }
                temp = target;
                try {
                    while (true) {
                        if (lBoreder.contains(temp)) {
                            break;
                        }
                        temp += 7;
                        if (board.getCells().get(temp).getPiece() != null) {
                            break;
                        }
                    }
                    if (board.getCells().get(temp).getPiece() instanceof Queen ||
                            board.getCells().get(temp).getPiece() instanceof Bishop) {
                        if (!board.getCells().get(temp).getPiece().color.equals(piece.color)) {
                            board.getCells().get(target).setPiece(targetPiece);
                            return true;
                        }
                    }
                } catch (Exception ignored) {

                }
                temp = target;
                try {
                    while (true) {
                        if (rBoreder.contains(temp)) {
                            break;
                        }
                        temp -= 7;
                        if (board.getCells().get(temp).getPiece() != null) {
                            break;
                        }
                    }
                    if (board.getCells().get(temp).getPiece() instanceof Queen ||
                            board.getCells().get(temp).getPiece() instanceof Bishop) {
                        if (!board.getCells().get(temp).getPiece().color.equals(piece.color)) {
                            board.getCells().get(target).setPiece(targetPiece);
                            return true;
                        }
                    }
                } catch (Exception ignored) {

                }
                ArrayList<Integer> enemyKingMoves = getKingMoves(enemyKingPos);
                if (!enemyKingMoves.contains(target)) {
                    return false;
                }
                board.getCells().get(target).setPiece(targetPiece);
                return true;
            }

            public boolean isCheck(Piece piece, int pos) throws IOException {
                /////////////////Zachranim stav sachovnici///////////////
                List<Piece> startPieces = new ArrayList<>();
                List<Integer> startPositions = new ArrayList<>();

                for (int i = 0; i < board.getCells().size(); i++) {
                    if (board.getCells().get(i).getPiece() != null) {
                        startPieces.add(board.getCells().get(i).getPiece());
                        startPositions.add(i);
                    }
                }
                for (int i = 0; i < 64; i++) {
                    if (board.getCells().get(i).getPiece() != null) {
                        if (!board.getCells().get(i).getPiece().getColor().equals(piece.color)) {
                            if (canMove(board.getCells().get(i).getPiece(), i, pos)) {
                                for (int j = 0; j < startPositions.size(); j++) {
                                    board.getCells().get(startPositions.get(j)).setPiece(startPieces.get(j));
                                }
                                return true;
                            }
                        }
                    }
                }
////////////////////////////////Zase nevim proc se nepocita zvlast pro cerneho krale sach damou atd, zvlast///////
                int temp = pos;
                try {
                    while (true) {
                        temp -= 8;
                        if (board.getCells().get(temp).getPiece() != null) {
                            break;
                        }
                    }
                    if (board.getCells().get(temp).getPiece() instanceof Queen ||
                            board.getCells().get(temp).getPiece() instanceof Rook) {
                        if (!board.getCells().get(temp).getPiece().color.equals(piece.color)) {
                            for (int j = 0; j < startPositions.size(); j++) {
                                board.getCells().get(startPositions.get(j)).setPiece(startPieces.get(j));
                            }
                            return true;
                        }
                    }
                } catch (Exception ignored) {

                }
                temp = pos;
                try {
                    while (true) {
                        temp += 8;
                        if (board.getCells().get(temp).getPiece() != null) {
                            break;
                        }
                    }
                    if (board.getCells().get(temp).getPiece() instanceof Queen ||
                            board.getCells().get(temp).getPiece() instanceof Rook) {
                        if (!board.getCells().get(temp).getPiece().color.equals(piece.color)) {
                            for (int j = 0; j < startPositions.size(); j++) {
                                board.getCells().get(startPositions.get(j)).setPiece(startPieces.get(j));
                            }
                            return true;
                        }
                    }
                } catch (Exception ignored) {

                }
                temp = pos;
                try {
                    while (true) {
                        temp += 1;
                        if (temp % 8 == 0) {
                            temp -= 1;
                            break;
                        }
                        if (board.getCells().get(temp).getPiece() != null) {
                            break;
                        }
                    }
                    if (board.getCells().get(temp).getPiece() instanceof Queen ||
                            board.getCells().get(temp).getPiece() instanceof Rook) {
                        if (!board.getCells().get(temp).getPiece().color.equals(piece.color)) {
                            for (int j = 0; j < startPositions.size(); j++) {
                                board.getCells().get(startPositions.get(j)).setPiece(startPieces.get(j));
                            }
                            return true;
                        }
                    }
                } catch (Exception ignored) {

                }
                temp = pos;
                try {
                    while (true) {
                        temp -= 1;
                        if (temp % 8 == 0) {
                            temp += 1;
                            break;
                        }
                        if (board.getCells().get(temp).getPiece() != null) {
                            break;
                        }
                    }
                    if (board.getCells().get(temp).getPiece() instanceof Queen ||
                            board.getCells().get(temp).getPiece() instanceof Rook) {
                        if (!board.getCells().get(temp).getPiece().color.equals(piece.color)) {
                            for (int j = 0; j < startPositions.size(); j++) {
                                board.getCells().get(startPositions.get(j)).setPiece(startPieces.get(j));
                            }
                            return true;
                        }
                    }
                } catch (Exception ignored) {

                }

////////////////////////////////////////// Nevim proc se mi nepocita dama, pocitam jeste jednou//////
                temp = pos;
                //chci vedet kdy jsem u kraje, abych zastavil prohledavani
                ArrayList<Integer> lBoreder = new ArrayList<>();
                ArrayList<Integer> rBoreder = new ArrayList<>();
                for (int i = 0; i < 8; i++) {
                    lBoreder.add(i * 8);
                    rBoreder.add(7 + i * 8);
                }
                try {
                    while (true) {
                        if (lBoreder.contains(temp)) {
                            break;
                        }
                        temp -= 9;
                        if (board.getCells().get(temp).getPiece() != null) {
                            break;
                        }
                    }
                    if (board.getCells().get(temp).getPiece() instanceof Queen ||
                            board.getCells().get(temp).getPiece() instanceof Bishop) {
                        if (!board.getCells().get(temp).getPiece().color.equals(piece.color)) {
                            for (int j = 0; j < startPositions.size(); j++) {
                                board.getCells().get(startPositions.get(j)).setPiece(startPieces.get(j));
                            }
                            return true;
                        }
                    }
                } catch (Exception ignored) {

                }
                temp = pos;
                try {
                    while (true) {
                        if (rBoreder.contains(temp)) {
                            break;
                        }
                        temp += 9;
                        if (board.getCells().get(temp).getPiece() != null) {
                            break;
                        }
                    }
                    if (board.getCells().get(temp).getPiece() instanceof Queen ||
                            board.getCells().get(temp).getPiece() instanceof Bishop) {
                        if (!board.getCells().get(temp).getPiece().color.equals(piece.color)) {
                            for (int j = 0; j < startPositions.size(); j++) {
                                board.getCells().get(startPositions.get(j)).setPiece(startPieces.get(j));
                            }
                            return true;
                        }
                    }
                } catch (Exception ignored) {

                }
                temp = pos;
                try {
                    while (true) {
                        if (lBoreder.contains(temp)) {
                            break;
                        }
                        temp += 7;
                        if (board.getCells().get(temp).getPiece() != null) {
                            break;
                        }
                    }
                    if (board.getCells().get(temp).getPiece() instanceof Queen ||
                            board.getCells().get(temp).getPiece() instanceof Bishop) {
                        if (!board.getCells().get(temp).getPiece().color.equals(piece.color)) {
                            for (int j = 0; j < startPositions.size(); j++) {
                                board.getCells().get(startPositions.get(j)).setPiece(startPieces.get(j));
                            }
                            return true;
                        }
                    }
                } catch (Exception ignored) {

                }
                temp = pos;
                try {
                    while (true) {
                        if (rBoreder.contains(temp)) {
                            break;
                        }
                        temp -= 7;
                        if (board.getCells().get(temp).getPiece() != null) {
                            break;
                        }
                    }
                    if (board.getCells().get(temp).getPiece() instanceof Queen ||
                            board.getCells().get(temp).getPiece() instanceof Bishop) {
                        if (!board.getCells().get(temp).getPiece().color.equals(piece.color)) {
                            for (int j = 0; j < startPositions.size(); j++) {
                                board.getCells().get(startPositions.get(j)).setPiece(startPieces.get(j));
                            }
                            return true;
                        }
                    }
                } catch (Exception ignored) {

                }

                for (int j = 0; j < startPositions.size(); j++) {
                    board.getCells().get(startPositions.get(j)).setPiece(startPieces.get(j));
                }
                return false;
            }

            private ArrayList<Integer> getKingMoves(int pos) {
                ArrayList<Integer> moves = new ArrayList<>();
                int vert = getVertical(pos);

                for (int i = 0; i < 3; i++) {
                    if (pos % 8 == 0 && i == 0) {
                        continue;
                    }
                    if (pos - 7 % 8 == 0 && i == 2) {
                        continue;
                    }
                    if (pos - 9 + i >= 0) {
                        if (vert - getVertical(pos - 9 + i) == 1) {
                            moves.add(pos - 9 + i);
                        }
                    }
                }
                for (int i = 0; i < 3; i++) {
                    if (pos % 8 == 0 && i == 0) {
                        continue;
                    }
                    if (pos - 7 % 8 == 0 && i == 2) {
                        continue;
                    }
                    if (pos + 7 + i <= 63) {
                        if (getVertical(pos + 7 + i) - vert == 1)
                            moves.add(pos + 7 + i);
                    }
                }
                for (int i = 0; i < 3; i++) {
                    if (pos % 8 == 0 && i == 0) {
                        continue;
                    }
                    if (pos - 7 % 8 == 0 && i == 2) {
                        continue;
                    }
                    if (getVertical(pos - 1 + i) - vert == 0) {
                        moves.add(pos - 1 + i);
                    }
                }

                return moves;
            }

            private int[] getLastMove() throws IOException {
                int[] arr = new int[2];
                Path path = Paths.get("moves.txt");
                Scanner sc = new Scanner(path);
                int count = Files.readAllLines(path).size();
                for (int i = 0; i < count - 1; i++) {
                    sc.nextLine();
                }
                if (sc.hasNext()) {
                    String temp = sc.nextLine();
                    temp = temp.substring(temp.indexOf(" ") + 1);
                    arr[0] = Integer.parseInt(temp.substring(0, temp.indexOf(" ")));
                    arr[1] = Integer.parseInt(temp.substring(temp.indexOf(" ") + 1, temp.lastIndexOf(" ")));
                }

                return arr;
            }

            @Override
            public void mouseEntered(MouseEvent e) {

            }

            @Override
            public void mouseExited(MouseEvent e) {

            }
        });
    }

    private static void keyListener(Board board) {
        board.setFocusable(true);
        board.requestFocus();

        board.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {

            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyChar() == 'r') {
                    isWhiteTurn = true;
                    board.setCheckMate(false);
                    board.setStaleMate(false);
                    board.initPieces();
                    board.repaint();
                }
                if (e.getKeyChar() == 'w') {
                    JFrame win = new JFrame();
                    win.setTitle("Graph");
                    Moves whiteMoves;
                    try {
                        whiteMoves = getWhiteMoves();
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                    ArrayList<Moves> dataWhite = new ArrayList<>();
                    dataWhite.add(whiteMoves);
                    ChartPanel panelWhite = new ChartPanel(
                            createBarChart(dataWhite)
                    );
                    win.add(panelWhite);

                    win.pack();

                    win.setLocationRelativeTo(null);
                    win.setVisible(true);
                }
                if (e.getKeyChar() == 'b') {
                    JFrame win = new JFrame();
                    win.setTitle("Graph");
                    Moves blackMoves;
                    try {
                        blackMoves = getBlackMoves();
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                    ArrayList<Moves> dataBlack = new ArrayList<>();
                    dataBlack.add(blackMoves);
                    ChartPanel panelBlack = new ChartPanel(
                            createBarChart(dataBlack)
                    );
                    win.add(panelBlack);

                    win.pack();

                    win.setLocationRelativeTo(null);
                    win.setVisible(true);
                }
            }

            private JFreeChart createBarChart(ArrayList<Moves> data) {
                DefaultCategoryDataset dataset = new DefaultCategoryDataset();
                for (Moves move : data) {
                    for (Move item : move.statistics) {
                        dataset.addValue((Number) item.time, item.piece,
                                Integer.toString(item.initial) + "->" + Integer.toString(item.target));
                    }
                }

                JFreeChart chart = ChartFactory.createBarChart("Graf tah " + data.get(0).color + " hráče",
                        "Taha",
                        "Čas",
                        dataset);

                CategoryPlot plot = chart.getCategoryPlot();

                plot.setBackgroundPaint(Color.WHITE);
                plot.setRangeGridlinePaint(Color.LIGHT_GRAY);
                plot.getDomainAxis().setVisible(false);

                CategoryItemRenderer renderer = plot.getRenderer();


                renderer.setDefaultItemLabelFont(new Font("Calibri", Font.PLAIN, 10));
                renderer.setDefaultItemLabelGenerator(new StandardCategoryItemLabelGenerator(
                        "{2}sec", NumberFormat.getIntegerInstance()
                ));
                renderer.setDefaultItemLabelsVisible(true);
                BarRenderer br = (BarRenderer) renderer;

                br.setItemMargin(0.05);
                br.setBarPainter(new StandardBarPainter());

                return chart;
            }

            private Moves getWhiteMoves() throws IOException {
                Moves moves = new Moves();
                moves.color = "White";
                moves.statistics = new ArrayList<>();
                Path path = Paths.get("moves.txt");
                Scanner sc = new Scanner(path);
                while (sc.hasNext()) {
                    String temp = sc.nextLine();
                    if (sc.hasNext()) {
                        sc.nextLine();
                    }
                    Move m = new Move();
                    m.piece = temp.substring(0, temp.indexOf(" "));
                    temp = temp.substring(temp.indexOf(" ") + 1);
                    m.initial = Integer.parseInt(temp.substring(0, temp.indexOf(" ")));
                    temp = temp.substring(temp.indexOf(" ") + 1);
                    m.target = Integer.parseInt(temp.substring(0, temp.indexOf(" ")));
                    temp = temp.substring(temp.indexOf(" ") + 1);
                    m.time = Integer.parseInt(temp);
                    moves.statistics.add(m);
                }
                return moves;
            }

            private Moves getBlackMoves() throws IOException {
                Moves moves = new Moves();
                moves.color = "Black";
                moves.statistics = new ArrayList<>();
                Path path = Paths.get("moves.txt");
                Scanner sc = new Scanner(path);
                while (sc.hasNext()) {
                    sc.nextLine();
                    if (sc.hasNext()) {
                        String temp = sc.nextLine();
                        Move m = new Move();
                        m.piece = temp.substring(0, temp.indexOf(" "));
                        temp = temp.substring(temp.indexOf(" ") + 1);
                        m.initial = Integer.parseInt(temp.substring(0, temp.indexOf(" ")));
                        temp = temp.substring(temp.indexOf(" ") + 1);
                        m.target = Integer.parseInt(temp.substring(0, temp.indexOf(" ")));
                        temp = temp.substring(temp.indexOf(" ") + 1);
                        m.time = Integer.parseInt(temp);
                        moves.statistics.add(m);
                    }
                }
                return moves;
            }

            @Override
            public void keyReleased(KeyEvent e) {

            }
        });
    }
}