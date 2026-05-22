import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;
import javax.imageio.ImageIO;
import javax.swing.*;

public class chessGUI {

    private static final int TILE_SIZE = 80;
    private static final Color LIGHT = new Color(240, 217, 181);
    private static final Color DARK  = new Color(181, 136, 99);

    
    static HashMap<Integer, BufferedImage> pieceImages = new HashMap<>();
    static Board board = new Board();
    static int selectedRow = -1;
    static int selectedCol = -1;
    static int[][] validMoves = new int[0][2];
    static boolean flipped = false;
    static MCTS engine = null;
    static boolean enginePlaysWhite = false;


    public static void main(String[] args) {
        String[] options = {"Play as White", "Play as Black"};
        int choice = JOptionPane.showOptionDialog(null, "Choose your side",
        "Chess", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE,
        null, options, options[0]);

        if (choice == 0) {
            // Human is white, engine is black
            engine = new MCTS(false);
            enginePlaysWhite = false;
        } else {
            // Human is black, engine is white
            engine = new MCTS(true);
            enginePlaysWhite = true;
        }

        loadImages();
        board.initBoard();

        final JFrame frame = new JFrame("Chess");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.setLayout(new BorderLayout());

        JPanel boardPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawBoard(g);
            }
        };

        boardPanel.setPreferredSize(new Dimension(TILE_SIZE * 8, TILE_SIZE * 8));
        boardPanel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                int col = e.getX() / TILE_SIZE;
                int row = 7 - (e.getY() / TILE_SIZE);
                if (selectedRow == -1) {
                    if (board.get(row, col) != 0) {
                        selectedRow = row;
                        selectedCol = col;
                        validMoves = getMovesFor(row, col);
                        boardPanel.repaint();
                    }
                } else {
                    if ((board.get(selectedRow, selectedCol) == 1 && row == 7) ||
                        (board.get(selectedRow, selectedCol) == -1 && row == 0)) {
                        String[] options = {"Queen", "Rook", "Bishop", "Knight"};
                        int choice = JOptionPane.showOptionDialog(frame, "Promote pawn!",
                            "Promotion", JOptionPane.DEFAULT_OPTION,
                            JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
                        int[] pieces = {5, 4, 3, 2};
                        board.promotionChoice = pieces[choice];
                    }
                    boolean moved = board.makeMove(selectedRow, selectedCol, row, col);
                    selectedRow = -1;
                    selectedCol = -1;
                    validMoves = new int[0][2];
                    boardPanel.repaint();

                    if (moved) {
                        if (board.isCheckmate(true))        JOptionPane.showMessageDialog(frame, "Checkmate! Black wins!");
                        else if (board.isCheckmate(false))  JOptionPane.showMessageDialog(frame, "Checkmate! White wins!");
                        else if (board.isStalemate(true))   JOptionPane.showMessageDialog(frame, "Stalemate! Its a draw!");
                        else if (board.isStalemate(false))  JOptionPane.showMessageDialog(frame, "Stalemate! Its a draw!");
                        else if (board.isInCheck(true))     JOptionPane.showMessageDialog(frame, "White is in check!");
                        else if (board.isInCheck(false))    JOptionPane.showMessageDialog(frame, "Black is in check!");
                        else if (engine != null && board.whiteTurn == enginePlaysWhite) {
                            int[] engineMove = engine.getBestMove(board);
                            if (engineMove != null) {
                                board.makeMove(engineMove[0], engineMove[1], engineMove[2], engineMove[3]);
                                boardPanel.repaint();
                            }
                        }
                    }
                }
            }

        });
        JButton flipBtn = new JButton("Flip Board");
        flipBtn.addActionListener(e -> {
            flipped = !flipped;
            boardPanel.repaint();
        });


        frame.add(boardPanel, BorderLayout.CENTER);
        frame.add(flipBtn, BorderLayout.SOUTH);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);



    }

    private static void drawBoard(Graphics g) {
        for (int displayRow = 0; displayRow < 8; displayRow++) {
            int row = flipped ? displayRow : 7 - displayRow;
            for (int col = 0; col < 8; col++) {
                boolean isLight = (row + col) % 2 == 0;
                g.setColor(isLight ? LIGHT : DARK);
                g.fillRect(col * TILE_SIZE, displayRow * TILE_SIZE, TILE_SIZE, TILE_SIZE);

                for (int[] move : validMoves) {
                    if (move[0] == row && move[1] == col) {
                        g.setColor(new Color(0, 255, 0, 100));
                        g.fillRect(col * TILE_SIZE, displayRow * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                    }
                }


                int piece = board.get(row, col);
                if (piece != 0) {
                    BufferedImage img = pieceImages.get(piece);
                    g.drawImage(img, col * TILE_SIZE, displayRow * TILE_SIZE, TILE_SIZE, TILE_SIZE, null);
                }
            }
        }
    } 
    

    

    static void loadImages() {
        String[][] pieces = {
            {"wK", "wQ", "wB", "wN", "wR", "wP"},
            {"bK", "bQ", "bB", "bN", "bR", "bP"}
        };
        int[][] values = {
            { 6,  5,  3,  2,  4,  1},
            {-6, -5, -3, -2, -4, -1}
        };
        for (int row = 0; row < 2; row++) {
            for (int col = 0; col < 6; col++) {
                try {
                    BufferedImage img = ImageIO.read(new File("chesspieces/" + pieces[row][col] + ".png"));
                    pieceImages.put(values[row][col], img);
                } catch (Exception e) {
                    System.out.println("Could not load: " + pieces[row][col]);
                }
            }
        }

    }
    
    static int[][] getMovesFor(int row, int col) {
        int piece = board.get(row, col);
        int[][] raw = new int[0][2];
        if      (piece == 1  || piece == -1)  raw = board.pawnMoves(row, col);
        else if (piece == 2  || piece == -2)  raw = board.knightMoves(row, col);
        else if (piece == 3  || piece == -3)  raw = board.bishopMoves(row, col);
        else if (piece == 4  || piece == -4)  raw = board.rookMoves(row, col);
        else if (piece == 5  || piece == -5)  raw = board.queenMoves(row, col);
        else if (piece == 6  || piece == -6)  raw = board.kingMoves(row, col);
        

        int[][] filtered = new int[raw.length][2];
        int count = 0;
        for (int[] move : raw) {
            if (!board.leavesKingInCheck(row, col, move[0], move[1])) {
                filtered[count++] = move;
            }
        }

        return java.util.Arrays.copyOf(filtered, count);
    }


}
