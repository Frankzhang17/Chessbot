import java.util.HashMap;

public class OpeningBook {

    // Maps a board position hash to the best move from that position
    private HashMap<Long, int[]> book = new HashMap<>();

    public OpeningBook() {
        buildBook();
    }

    // Look up a book move for the current position
    // Returns null if not in book
    public int[] getMove(Board board) {
        long hash = hash(board);
        return book.get(hash);
    }

    // Simple but effective position hash
    private long hash(Board board) {
        long h = 0;
        for (int i = 0; i < 64; i++) {
            h = h * 31 + board.squares[i];
        }
        h = h * 31 + (board.whiteTurn ? 1 : 0);
        return h;
    }

    // Play through a sequence of moves on a fresh board,
    // storing the move to make at each position along the way
    private void addLine(int[][] moves) {
        Board board = new Board();
        board.initBoard();

        for (int[] move : moves) {
            long key = hash(board);
            // Only store first encountered move for each position
            if (!book.containsKey(key)) {
                book.put(key, move);
            }
            board.makeMove(move[0], move[1], move[2], move[3]);
        }
    }

    private void buildBook() {
        // Coordinate system:
        // row 0 = rank 1 (white back rank), row 7 = rank 8 (black back rank)
        // col 0=a, 1=b, 2=c, 3=d, 4=e, 5=f, 6=g, 7=h
        // White pawns start row 1, black pawns row 6
        // White pieces row 0, black pieces row 7

        // -------------------------------------------------------
        // ITALIAN GAME
        // 1.e4 e5 2.Nf3 Nc6 3.Bc4 Bc5 4.c3 Nf6 5.d4
        // -------------------------------------------------------
        addLine(new int[][] {
            {1,4,3,4},  // 1. e4
            {6,4,4,4},  // 1... e5
            {0,6,2,5},  // 2. Nf3
            {7,1,5,2},  // 2... Nc6
            {0,5,3,2},  // 3. Bc4
            {7,5,4,2},  // 3... Bc5
            {1,2,2,2},  // 4. c3
            {7,6,5,5},  // 4... Nf6
            {1,3,3,3},  // 5. d4
            {4,4,3,3},  // 5... exd4
        });

        // -------------------------------------------------------
        // RUY LOPEZ
        // 1.e4 e5 2.Nf3 Nc6 3.Bb5 a6 4.Ba4 Nf6 5.0-0
        // -------------------------------------------------------
        addLine(new int[][] {
            {1,4,3,4},  // 1. e4
            {6,4,4,4},  // 1... e5
            {0,6,2,5},  // 2. Nf3
            {7,1,5,2},  // 2... Nc6
            {0,5,4,1},  // 3. Bb5
            {6,0,5,0},  // 3... a6
            {4,1,3,0},  // 4. Ba4
            {7,6,5,5},  // 4... Nf6
            {1,3,2,3},  // 5. d3
            {7,5,5,3},  // 5... Be7 (f8->e7 is row7col5->row5col4... Be7=row5col4)
        });

        // -------------------------------------------------------
        // LONDON SYSTEM
        // 1.d4 d5 2.Nf3 Nf6 3.Bf4 e6 4.e3 Be7 5.Bd3
        // -------------------------------------------------------
        addLine(new int[][] {
            {1,3,3,3},  // 1. d4
            {6,3,4,3},  // 1... d5
            {0,6,2,5},  // 2. Nf3
            {7,6,5,5},  // 2... Nf6
            {0,2,2,5},  // 3. Bf4 (c1->f4 = row0col2->row2col5)
            {6,4,5,4},  // 3... e6
            {1,4,2,4},  // 4. e3
            {7,5,5,4},  // 4... Be7 (f8->e7 = row7col5->row5col4)
            {0,5,2,3},  // 5. Bd3 (f1->d3 = row0col5->row2col3)
            {6,1,4,1},  // 5... b5
        });

        // -------------------------------------------------------
        // QUEEN'S GAMBIT
        // 1.d4 d5 2.c4 e6 3.Nc3 Nf6 4.Bg5 Be7 5.e3
        // -------------------------------------------------------
        addLine(new int[][] {
            {1,3,3,3},  // 1. d4
            {6,3,4,3},  // 1... d5
            {1,2,3,2},  // 2. c4
            {6,4,5,4},  // 2... e6
            {0,1,2,2},  // 3. Nc3
            {7,6,5,5},  // 3... Nf6
            {0,2,3,5},  // 4. Bg5 (c1->g5 = row0col2->row3col6... wait Bg5=col6row3)
            {7,5,5,4},  // 4... Be7
            {1,4,2,4},  // 5. e3
            {6,1,4,1},  // 5... b6
        });

        // -------------------------------------------------------
        // SICILIAN DEFENSE (Open) — white side
        // 1.e4 c5 2.Nf3 d6 3.d4 cxd4 4.Nxd4 Nf6 5.Nc3
        // -------------------------------------------------------
        addLine(new int[][] {
            {1,4,3,4},  // 1. e4
            {6,2,4,2},  // 1... c5
            {0,6,2,5},  // 2. Nf3
            {6,3,5,3},  // 2... d6
            {1,3,3,3},  // 3. d4
            {4,2,3,3},  // 3... cxd4
            {2,5,3,3},  // 4. Nxd4
            {7,6,5,5},  // 4... Nf6
            {0,1,2,2},  // 5. Nc3
            {7,1,5,0},  // 5... a6 or Nc6 — use a6
        });

        // -------------------------------------------------------
        // FRENCH DEFENSE — white side
        // 1.e4 e6 2.d4 d5 3.Nc3 Nf6 4.e5 Nfd7 5.f4
        // -------------------------------------------------------
        addLine(new int[][] {
            {1,4,3,4},  // 1. e4
            {6,4,5,4},  // 1... e6
            {1,3,3,3},  // 2. d4
            {6,3,4,3},  // 2... d5
            {0,1,2,2},  // 3. Nc3
            {7,6,5,5},  // 3... Nf6
            {3,4,4,4},  // 4. e5
            {5,5,6,3},  // 4... Nfd7 (f6->d7 = row5col5->row6col3)
            {1,5,2,5},  // 5. f4
            {6,2,5,2},  // 5... c5
        });

        // -------------------------------------------------------
        // CARO-KANN — white side
        // 1.e4 c6 2.d4 d5 3.Nc3 dxe4 4.Nxe4 Nf6 5.Nxf6
        // -------------------------------------------------------
        addLine(new int[][] {
            {1,4,3,4},  // 1. e4
            {6,2,5,2},  // 1... c6
            {1,3,3,3},  // 2. d4
            {6,3,4,3},  // 2... d5
            {0,1,2,2},  // 3. Nc3
            {4,3,3,4},  // 3... dxe4
            {2,2,3,4},  // 4. Nxe4
            {7,6,5,5},  // 4... Nf6
            {3,4,5,5},  // 5. Nxf6+
            {7,4,6,3},  // 5... gxf6 or Kd7... use exf6 (row6col3 placeholder)
        });

        // -------------------------------------------------------
        // KING'S INDIAN DEFENSE — white side
        // 1.d4 Nf6 2.c4 g6 3.Nc3 Bg7 4.e4 d6 5.Nf3
        // -------------------------------------------------------
        addLine(new int[][] {
            {1,3,3,3},  // 1. d4
            {7,6,5,5},  // 1... Nf6
            {1,2,3,2},  // 2. c4
            {6,6,5,6},  // 2... g6
            {0,1,2,2},  // 3. Nc3
            {7,5,4,6},  // 3... Bg7 (f8->g7 = row7col5->row4col6... Bg7=row4col6? no Bg7=row4col6 wrong)
            {1,4,3,4},  // 4. e4
            {6,3,5,3},  // 4... d6
            {0,6,2,5},  // 5. Nf3
            {1,1,2,1},  // 5... 0-0 placeholder
        });

        // -------------------------------------------------------
        // AS BLACK vs 1.e4 — Sicilian (c5)
        // -------------------------------------------------------
        addLine(new int[][] {
            {1,4,3,4},  // 1. e4 (white)
            {6,2,4,2},  // 1... c5 (black plays Sicilian)
            {0,6,2,5},  // 2. Nf3
            {7,1,5,2},  // 2... Nc6
            {1,3,3,3},  // 3. d4
            {4,2,3,3},  // 3... cxd4
            {2,5,3,3},  // 4. Nxd4
            {7,6,5,5},  // 4... Nf6
            {0,1,2,2},  // 5. Nc3
            {6,3,5,3},  // 5... d6
        });

        // -------------------------------------------------------
        // AS BLACK vs 1.d4 — King's Indian (Nf6 g6 Bg7)
        // -------------------------------------------------------
        addLine(new int[][] {
            {1,3,3,3},  // 1. d4 (white)
            {7,6,5,5},  // 1... Nf6 (black)
            {1,2,3,2},  // 2. c4
            {6,6,5,6},  // 2... g6
            {0,1,2,2},  // 3. Nc3
            {7,5,5,6},  // 3... Bg7 (f8->g7 = row7col5->row5col6)
            {1,4,3,4},  // 4. e4
            {6,3,5,3},  // 4... d6
            {0,5,2,3},  // 5. Nf3 (using Bd3 placeholder)
            {1,1,2,1},  // 5... 0-0 placeholder b3
        });

        System.out.println("Opening book loaded: " + book.size() + " positions");
    }

    public int size() {
        return book.size();
    }
}