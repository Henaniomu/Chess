import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

/**
 * @author Oleh Tabachynskyi
 */
public class Board extends JPanel {
    List<Piece> pieces = new ArrayList<>();
    Piece blackRook0;
    Piece blackHorse0;
    Piece blackBishop0;
    Piece blackQueen;
    Piece blackKing;
    Piece blackBishop1;
    Piece blackHorse1;
    Piece blackRook1;
    Piece blackPawn0;
    Piece blackPawn1;
    Piece blackPawn2;
    Piece blackPawn3;
    Piece blackPawn4;
    Piece blackPawn5;
    Piece blackPawn6;
    Piece blackPawn7;
    Piece whiteRook0;
    Piece whiteHorse0;
    Piece whiteBishop0;
    Piece whiteQueen;
    Piece whiteKing;
    Piece whiteBishop1;
    Piece whiteHorse1;
    Piece whiteRook1;
    Piece whitePawn0;
    Piece whitePawn1;
    Piece whitePawn2;
    Piece whitePawn3;
    Piece whitePawn4;
    Piece whitePawn5;
    Piece whitePawn6;
    Piece whitePawn7;
    private int size;
    private List<Cell> cells;

    private int startX;
    private int startY;
    private boolean isCheckMate = false;

    private boolean isStaleMate = false;
    private boolean isStart = true;
    private boolean isRes = false;


    public Board(int size) {
        this.setPreferredSize(new Dimension(size, size));
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        Graphics2D g2d = (Graphics2D) g;

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = this.getWidth();
        int height = this.getHeight();

        this.size = Math.min(width, height);
        drawBoard(g2d);
        if (isStart) {
            initialization();
            isStart = false;
        }
        if (isRes) {
            System.out.println("res");
            saveGame(size);
            isRes = false;
        }
        if (isCheckMate) {
            showCheckMate(g2d);
        }
        if (isStaleMate) {
            showStaleMate(g2d);
        }


        cells.forEach(o -> o.paint(g2d));

        Path path = Paths.get("moves.txt");
        try {
            Scanner sc = new Scanner(path);
            String temp = "";
            while (sc.hasNext()) {
                temp = sc.nextLine();
            }
            if (!temp.equals("")) {
                temp = temp.substring(temp.indexOf(" ") + 1);
                int i = Integer.parseInt(temp.substring(0, temp.indexOf(" ")));
                int j = Integer.parseInt(temp.substring(temp.indexOf(" ") + 1, temp.lastIndexOf(" ")));

                int x1, y1, x2, y2, size;
                x1 = cells.get(i).getX();
                y1 = cells.get(i).getY();
                size = cells.get(i).getWidth();
                x2 = cells.get(j).getX();
                y2 = cells.get(j).getY();

                g2d.setColor(Color.magenta);
                g2d.drawRect(x1, y1, size, size);
                g2d.drawRect(x2, y2, size, size);
            }
        } catch (IOException ignored) {

        }

    }


    public void setCheckMate(boolean checkMate) {
        isCheckMate = checkMate;
    }

    public void showCheckMate(Graphics g) {
        super.paint(g);
        String s = "Check And Mate";
        g.setFont(new Font("Arial", Font.ITALIC, 30));
        g.drawString(s, this.getWidth() / 2 - g.getFontMetrics().stringWidth(s) / 2, this.getHeight() / 2);
    }

    public void showStaleMate(Graphics g) {
        super.paint(g);
        String s = "Stalemate";
        g.setFont(new Font("Arial", Font.ITALIC, 30));
        g.drawString(s, this.getWidth() / 2 - g.getFontMetrics().stringWidth(s) / 2, this.getHeight() / 2);
    }

    public void setRes(boolean res) {
        isRes = res;
    }

    /*Initializacni blok na zacatku hry*/
    private void initialization() {
        initCells(size);
        initPieces();
    }

    private void saveGame(int size) {
        List<Integer> index = new ArrayList<>();
        for (int i = 0; i < 64; i++) {
            if (cells.get(i).getPiece() != null) {
                index.add(i);
            }
        }
        resizeCells(size);
        resizePieces(size);
        for (int i = 0; i < index.size(); i++) {
            cells.get(index.get(i)).setPiece(pieces.get(i));
        }
    }

    private void resizeCells(int size) {
        cells = new ArrayList<>();
        int posX = startX;
        int posY = startY;
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                cells.add(new Cell(posX, posY, size / 8, size / 8, null));
                posX += size / 8;
            }
            posX = startX;
            posY += size / 8;
        }
    }

    private void resizePieces(int size) {
        for (int i = 0; i < 32; i++) {
            if (pieces.get(i) != null) {
                pieces.get(i).setWidth(size);
                pieces.get(i).setHeight(size);
            }
        }
    }

    public int getBoardSize() {
        return size;
    }

    /*Vykresluje sachovnici*/
    private void drawBoard(Graphics2D g2d) {
        AffineTransform at = g2d.getTransform();
        if (this.getHeight() != this.getWidth()) {
            if (this.getHeight() - this.getWidth() > 0) {
                startY = (this.getHeight() - this.getWidth()) / 2;
                startX = 0;
            } else {
                startX = (this.getWidth() - this.getHeight()) / 2;
                startY = 0;
            }
        } else {
            startX = 0;
            startY = 0;
        }
        g2d.translate(startX, startY);
        int posXBlack = size / 8;
        int posYBlack = 0;
        g2d.setColor(Color.GRAY);
        for (int j = 0; j < 8; j++) {
            for (int i = 0; i < 4; i++) {
                g2d.fillRect(posXBlack, posYBlack, size / 8, size / 8);
                posXBlack += size / 4;
            }
            posYBlack += size / 8;
            if (j % 2 == 0) {
                posXBlack = 0;
            } else posXBlack = size / 8;
        }

        g2d.setTransform(at);
    }

    public List<Cell> getCells() {
        return cells;
    }

    public void setStart(boolean start) {
        isStart = start;
    }

    /**
     * Initializace bunek (zmena jejich rozmeru po zmene velikosti okna) na zacatku hry.
     */
    public void initCells(int size) {
        cells = new LinkedList<>();
        int posX = 0;
        int posY = 0;
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                cells.add(new Cell(posX, posY, size / 8, size / 8, null));
                posX += size / 8;
            }
            posX = 0;
            posY += size / 8;
        }
    }

    public void animation(Piece p, int start, int end) {
        MovingCell mc = new MovingCell(p, cells.get(start),
                cells.get(end), getGraphics(), this);
        mc.paint();
    }

    /**
     * Initializace kamenu a jejich prirazeni slusne bunce na zacatku hry.
     */
    public void initPieces() {
        for (Cell cell : cells) {
            cell.setPiece(null);
        }
        blackRook0 = new Rook(Color.BLACK, size, size);
        blackHorse0 = new Horse(Color.BLACK, size, size);
        blackBishop0 = new Bishop(Color.BLACK, size, size);
        blackQueen = new Queen(Color.BLACK, size, size);
        blackKing = new King(Color.BLACK, size, size);
        blackBishop1 = new Bishop(Color.BLACK, size, size);
        blackHorse1 = new Horse(Color.BLACK, size, size);
        blackRook1 = new Rook(Color.BLACK, size, size);
        blackPawn0 = new Pawn(Color.BLACK, size, size);
        blackPawn1 = new Pawn(Color.BLACK, size, size);
        blackPawn2 = new Pawn(Color.BLACK, size, size);
        blackPawn3 = new Pawn(Color.BLACK, size, size);
        blackPawn4 = new Pawn(Color.BLACK, size, size);
        blackPawn5 = new Pawn(Color.BLACK, size, size);
        blackPawn6 = new Pawn(Color.BLACK, size, size);
        blackPawn7 = new Pawn(Color.BLACK, size, size);
        whiteRook0 = new Rook(Color.WHITE, size, size);
        whiteHorse0 = new Horse(Color.WHITE, size, size);
        whiteBishop0 = new Bishop(Color.WHITE, size, size);
        whiteQueen = new Queen(Color.WHITE, size, size);
        whiteKing = new King(Color.WHITE, size, size);
        whiteBishop1 = new Bishop(Color.WHITE, size, size);
        whiteHorse1 = new Horse(Color.WHITE, size, size);
        whiteRook1 = new Rook(Color.WHITE, size, size);
        whitePawn0 = new Pawn(Color.WHITE, size, size);
        whitePawn1 = new Pawn(Color.WHITE, size, size);
        whitePawn2 = new Pawn(Color.WHITE, size, size);
        whitePawn3 = new Pawn(Color.WHITE, size, size);
        whitePawn4 = new Pawn(Color.WHITE, size, size);
        whitePawn5 = new Pawn(Color.WHITE, size, size);
        whitePawn6 = new Pawn(Color.WHITE, size, size);
        whitePawn7 = new Pawn(Color.WHITE, size, size);

        pieces.add(blackRook0);
        pieces.add(blackHorse0);
        pieces.add(blackBishop0);
        pieces.add(blackQueen);
        pieces.add(blackKing);
        pieces.add(blackBishop1);
        pieces.add(blackHorse1);
        pieces.add(blackRook1);
        pieces.add(blackPawn0);
        pieces.add(blackPawn1);
        pieces.add(blackPawn2);
        pieces.add(blackPawn3);
        pieces.add(blackPawn4);
        pieces.add(blackPawn5);
        pieces.add(blackPawn6);
        pieces.add(blackPawn7);
        pieces.add(whitePawn0);
        pieces.add(whitePawn1);
        pieces.add(whitePawn2);
        pieces.add(whitePawn3);
        pieces.add(whitePawn4);
        pieces.add(whitePawn5);
        pieces.add(whitePawn6);
        pieces.add(whitePawn7);
        pieces.add(whiteRook0);
        pieces.add(whiteHorse0);
        pieces.add(whiteBishop0);
        pieces.add(whiteQueen);
        pieces.add(whiteKing);
        pieces.add(whiteBishop1);
        pieces.add(whiteHorse1);
        pieces.add(whiteRook1);


        cells.get(0).setPiece(blackRook0);
        cells.get(1).setPiece(blackHorse0);
        cells.get(2).setPiece(blackBishop0);
        cells.get(3).setPiece(blackQueen);
        cells.get(4).setPiece(blackKing);
        cells.get(5).setPiece(blackBishop1);
        cells.get(6).setPiece(blackHorse1);
        cells.get(7).setPiece(blackRook1);
        cells.get(8).setPiece(blackPawn0);
        cells.get(9).setPiece(blackPawn1);
        cells.get(10).setPiece(blackPawn2);
        cells.get(11).setPiece(blackPawn3);
        cells.get(12).setPiece(blackPawn4);
        cells.get(13).setPiece(blackPawn5);
        cells.get(14).setPiece(blackPawn6);
        cells.get(15).setPiece(blackPawn7);
        cells.get(56).setPiece(whiteRook0);
        cells.get(57).setPiece(whiteHorse0);
        cells.get(58).setPiece(whiteBishop0);
        cells.get(59).setPiece(whiteQueen);
        cells.get(60).setPiece(whiteKing);
        cells.get(61).setPiece(whiteBishop1);
        cells.get(62).setPiece(whiteHorse1);
        cells.get(63).setPiece(whiteRook1);
        cells.get(55).setPiece(whitePawn0);
        cells.get(54).setPiece(whitePawn1);
        cells.get(53).setPiece(whitePawn2);
        cells.get(52).setPiece(whitePawn3);
        cells.get(51).setPiece(whitePawn4);
        cells.get(50).setPiece(whitePawn5);
        cells.get(49).setPiece(whitePawn6);
        cells.get(48).setPiece(whitePawn7);
    }

    /**
     * Metoda pro "vynechani" kamenu
     */
    public void remove(int index) {
        cells.get(index).setPiece(null);
        repaint();
    }

    public void paintPiece(Piece piece, int x, int y) {
        getGraphics().translate(x, y);
        piece.paint(getGraphics());
        getGraphics().translate(-x, -y);
    }

    public void dragging(Graphics g2, int x, int y, Piece piece) {
        Cell cell = new Cell(x, y, size, size, piece);
        cell.getPiece().paint(g2);
    }

    public void setStaleMate(boolean staleMate) {
        isStaleMate = staleMate;
    }

    public List<Piece> getPieces() {
        return pieces;
    }
}
