import java.util.ArrayList;

public class MCTS {

    static final int MAX_DEPTH = 3;
    boolean playingAsWhite;

    static final int[][] PAWN_TABLE = {
        { 0,  0,  0,  0,  0,  0,  0,  0},
        {50, 50, 50, 50, 50, 50, 50, 50},
        {10, 10, 20, 30, 30, 20, 10, 10},
        { 5,  5, 10, 25, 25, 10,  5,  5},
        { 0,  0,  0, 20, 20,  0,  0,  0},
        { 5, -5,-10,  0,  0,-10, -5,  5},
        { 5, 10, 10,-20,-20, 10, 10,  5},
        { 0,  0,  0,  0,  0,  0,  0,  0}
    };

    static final int[][] KNIGHT_TABLE = {
        {-50,-40,-30,-30,-30,-30,-40,-50},
        {-40,-20,  0,  0,  0,  0,-20,-40},
        {-30,  0, 10, 15, 15, 10,  0,-30},
        {-30,  5, 15, 20, 20, 15,  5,-30},
        {-30,  0, 15, 20, 20, 15,  0,-30},
        {-30,  5, 10, 15, 15, 10,  5,-30},
        {-40,-20,  0,  5,  5,  0,-20,-40},
        {-50,-40,-30,-30,-30,-30,-40,-50}
    };

    static final int[][] BISHOP_TABLE = {
        {-20,-10,-10,-10,-10,-10,-10,-20},
        {-10,  0,  0,  0,  0,  0,  0,-10},
        {-10,  0,  5, 10, 10,  5,  0,-10},
        {-10,  5,  5, 10, 10,  5,  5,-10},
        {-10,  0, 10, 10, 10, 10,  0,-10},
        {-10, 10, 10, 10, 10, 10, 10,-10},
        {-10,  5,  0,  0,  0,  0,  5,-10},
        {-20,-10,-10,-10,-10,-10,-10,-20}
    };

    static final int[][] ROOK_TABLE = {
        { 0,  0,  0,  0,  0,  0,  0,  0},
        { 5, 10, 10, 10, 10, 10, 10,  5},
        {-5,  0,  0,  0,  0,  0,  0, -5},
        {-5,  0,  0,  0,  0,  0,  0, -5},
        {-5,  0,  0,  0,  0,  0,  0, -5},
        {-5,  0,  0,  0,  0,  0,  0, -5},
        {-5,  0,  0,  0,  0,  0,  0, -5},
        { 0,  0,  0,  5,  5,  0,  0,  0}
    };

    static final int[][] QUEEN_TABLE = {
        {-20,-10,-10, -5, -5,-10,-10,-20},
        {-10,  0,  0,  0,  0,  0,  0,-10},
        {-10,  0,  5,  5,  5,  5,  0,-10},
        { -5,  0,  5,  5,  5,  5,  0, -5},
        {  0,  0,  5,  5,  5,  5,  0, -5},
        {-10,  5,  5,  5,  5,  5,  0,-10},
        {-10,  0,  5,  0,  0,  0,  0,-10},
        {-20,-10,-10, -5, -5,-10,-10,-20}
    };

    static final int[][] KING_TABLE = {
        {-30,-40,-40,-50,-50,-40,-40,-30},
        {-30,-40,-40,-50,-50,-40,-40,-30},
        {-30,-40,-40,-50,-50,-40,-40,-30},
        {-30,-40,-40,-50,-50,-40,-40,-30},
        {-20,-30,-30,-40,-40,-30,-30,-20},
        {-10,-20,-20,-20,-20,-20,-20,-10},
        { 20, 20,  0,  0,  0,  0, 20, 20},
        { 20, 30, 10,  0,  0, 10, 30, 20}
    };

    public MCTS(boolean playingAsWhite) {
        this.playingAsWhite = playingAsWhite;
    }

    public int[] getBestMove(Board board) {
        ArrayList<int[]> moves = getAllMoves(board, board.whiteTurn);
        if (moves.isEmpty()) return null;

        moves.sort((a, b) -> scoreMoveForOrdering(board, b) - scoreMoveForOrdering(board, a));

        int[] bestMove = moves.get(0);
        int bestScore = board.whiteTurn ? Integer.MIN_VALUE : Integer.MAX_VALUE;

        for (int[] move : moves) {
            Board copy = copyBoard(board);
            copy.makeMove(move[0], move[1], move[2], move[3]);

            int score = minimax(copy, MAX_DEPTH - 1, Integer.MIN_VALUE, Integer.MAX_VALUE, !board.whiteTurn);

            System.out.printf("Move [%d,%d -> %d,%d] score: %d%n",
                move[0], move[1], move[2], move[3], score);

            if (board.whiteTurn) {
                if (score > bestScore) { bestScore = score; bestMove = move; }
            } else {
                if (score < bestScore) { bestScore = score; bestMove = move; }
            }
        }

        System.out.printf("Best move: [%d,%d -> %d,%d] score: %d%n",
            bestMove[0], bestMove[1], bestMove[2], bestMove[3], bestScore);

        return bestMove;
    }

    private int minimax(Board board, int depth, int alpha, int beta, boolean maximizing) {
        // Treat repeated positions as draws — prevents perpetual check when winning
        if (board.isThreefoldRepetition()) return 0;
        if (board.fiftyMoveCounter >= 100) return 0;
        if (board.isInsufficientMaterial()) return 0;

        ArrayList<int[]> moves = getAllMoves(board, board.whiteTurn);

        if (moves.isEmpty()) {
            if (board.isInCheck(board.whiteTurn)) {
                return board.whiteTurn ? (-100000 - depth) : (100000 + depth);
            }
            return 0; // Stalemate
        }

        if (depth == 0) return quiescence(board, alpha, beta, maximizing);

        moves.sort((a, b) -> scoreMoveForOrdering(board, b) - scoreMoveForOrdering(board, a));

        if (maximizing) {
            int best = Integer.MIN_VALUE;
            for (int[] move : moves) {
                Board copy = copyBoard(board);
                copy.makeMove(move[0], move[1], move[2], move[3]);
                int score = minimax(copy, depth - 1, alpha, beta, false);
                best = Math.max(best, score);
                alpha = Math.max(alpha, best);
                if (beta <= alpha) break;
            }
            return best;
        } else {
            int best = Integer.MAX_VALUE;
            for (int[] move : moves) {
                Board copy = copyBoard(board);
                copy.makeMove(move[0], move[1], move[2], move[3]);
                int score = minimax(copy, depth - 1, alpha, beta, true);
                best = Math.min(best, score);
                beta = Math.min(beta, best);
                if (beta <= alpha) break;
            }
            return best;
        }
    }

    private int quiescence(Board board, int alpha, int beta, boolean maximizing) {
        if (board.isThreefoldRepetition()) return 0;

        int standPat = evaluate(board);

        if (maximizing) {
            if (standPat >= beta) return beta;
            if (standPat > alpha) alpha = standPat;
        } else {
            if (standPat <= alpha) return alpha;
            if (standPat < beta) beta = standPat;
        }

        ArrayList<int[]> captures = new ArrayList<>();
        for (int[] move : getAllMoves(board, board.whiteTurn)) {
            int target = board.get(move[2], move[3]);
            boolean isCapture = maximizing ? target < 0 : target > 0;

            int piece = board.get(move[0], move[1]);
            boolean isEnPassant = (piece == 1 || piece == -1)
                && move[1] != move[3] && target == 0;

            if (isCapture || isEnPassant) captures.add(move);
        }

        if (captures.isEmpty()) return standPat;

        captures.sort((a, b) -> scoreMoveForOrdering(board, b) - scoreMoveForOrdering(board, a));

        if (maximizing) {
            int best = standPat;
            for (int[] move : captures) {
                Board copy = copyBoard(board);
                copy.makeMove(move[0], move[1], move[2], move[3]);
                int score = quiescence(copy, alpha, beta, false);
                best = Math.max(best, score);
                alpha = Math.max(alpha, best);
                if (beta <= alpha) break;
            }
            return best;
        } else {
            int best = standPat;
            for (int[] move : captures) {
                Board copy = copyBoard(board);
                copy.makeMove(move[0], move[1], move[2], move[3]);
                int score = quiescence(copy, alpha, beta, true);
                best = Math.min(best, score);
                beta = Math.min(beta, best);
                if (beta <= alpha) break;
            }
            return best;
        }
    }

    public int evaluate(Board board) {
        int score = 0;

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                int piece = board.get(row, col);
                if (piece == 0) continue;

                boolean isWhite = piece > 0;
                int tableRow = isWhite ? (7 - row) : row;

                int materialScore = 0;
                int positionalScore = 0;

                switch (Math.abs(piece)) {
                    case 1: { // pawn
                        materialScore   = 100;
                        positionalScore = PAWN_TABLE[tableRow][col];

                        // Passed pawn bonus — no enemy pawns blocking on same or adjacent files
                        if (isWhite) {
                            boolean passed = true;
                            for (int r = row + 1; r < 8 && passed; r++) {
                                if (col > 0 && board.get(r, col - 1) == -1) passed = false;
                                if (board.get(r, col) == -1)                passed = false;
                                if (col < 7 && board.get(r, col + 1) == -1) passed = false;
                            }
                            if (passed) positionalScore += 20 + (row * 5); // bigger bonus closer to promotion
                        } else {
                            boolean passed = true;
                            for (int r = row - 1; r >= 0 && passed; r--) {
                                if (col > 0 && board.get(r, col - 1) == 1) passed = false;
                                if (board.get(r, col) == 1)                passed = false;
                                if (col < 7 && board.get(r, col + 1) == 1) passed = false;
                            }
                            if (passed) positionalScore += 20 + ((7 - row) * 5);
                        }
                        break;
                    }
                    case 2: // knight
                        materialScore   = 320;
                        positionalScore = KNIGHT_TABLE[tableRow][col];
                        break;
                    case 3: // bishop
                        materialScore   = 330;
                        positionalScore = BISHOP_TABLE[tableRow][col];
                        break;
                    case 4: // rook
                        materialScore   = 500;
                        positionalScore = ROOK_TABLE[tableRow][col];
                        break;
                    case 5: // queen
                        materialScore   = 900;
                        positionalScore = QUEEN_TABLE[tableRow][col];
                        break;
                    case 6: // king
                        positionalScore = KING_TABLE[tableRow][col];
                        break;
                }

                int total = materialScore + positionalScore;
                score += isWhite ? total : -total;
            }
        }

        // Reward castling rights — encourages king safety
        if (!board.whiteKingMoved) score += 15;
        if (!board.blackKingMoved) score -= 15;

        // Discourage draws when winning — if score is clearly positive for white,
        // returning a slightly worse score for repeated positions makes the engine
        // avoid perpetual check when it has a winning advantage
        if (board.isThreefoldRepetition()) {
            return score > 0 ? score - 50 : score + 50;
        }

        return score;
    }

    public int scoreMoveForOrdering(Board board, int[] move) {
        int score = 0;
        int attacker = Math.abs(board.get(move[0], move[1]));
        int victim   = Math.abs(board.get(move[2], move[3]));

        // MVV-LVA
        if (victim != 0) {
            score += pieceValue(victim) * 10 - pieceValue(attacker);
        }

        // Reward promotions
        int piece = board.get(move[0], move[1]);
        if ((piece == 1  && move[2] == 7) ||
            (piece == -1 && move[2] == 0)) {
            score += 900;
        }

        // Reward center control
        int toRow = move[2];
        int toCol = move[3];
        if (toRow >= 3 && toRow <= 4 && toCol >= 3 && toCol <= 4) score += 10;

        return score;
    }

    private int pieceValue(int pieceType) {
        switch (pieceType) {
            case 1: return 100;
            case 2: return 320;
            case 3: return 330;
            case 4: return 500;
            case 5: return 900;
            case 6: return 20000;
            default: return 0;
        }
    }

    public ArrayList<int[]> getAllMoves(Board board, boolean white) {
        ArrayList<int[]> allMoves = new ArrayList<>();

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                int piece = board.get(row, col);
                if (white  && piece <= 0) continue;
                if (!white && piece >= 0) continue;

                int[][] moves = new int[0][2];
                if      (piece == 1  || piece == -1) moves = board.pawnMoves(row, col);
                else if (piece == 2  || piece == -2) moves = board.knightMoves(row, col);
                else if (piece == 3  || piece == -3) moves = board.bishopMoves(row, col);
                else if (piece == 4  || piece == -4) moves = board.rookMoves(row, col);
                else if (piece == 5  || piece == -5) moves = board.queenMoves(row, col);
                else if (piece == 6  || piece == -6) moves = board.kingMoves(row, col);

                for (int[] move : moves) {
                    if (!board.leavesKingInCheck(row, col, move[0], move[1])) {
                        allMoves.add(new int[]{row, col, move[0], move[1]});
                    }
                }
            }
        }

        return allMoves;
    }

    public Board copyBoard(Board original) {
        Board copy = new Board();
        copy.squares             = java.util.Arrays.copyOf(original.squares, original.squares.length);
        copy.whiteTurn           = original.whiteTurn;
        copy.enPassantCol        = original.enPassantCol;
        copy.enPassantRow        = original.enPassantRow;
        copy.fiftyMoveCounter    = original.fiftyMoveCounter;
        copy.whiteKingMoved      = original.whiteKingMoved;
        copy.blackKingMoved      = original.blackKingMoved;
        copy.whiteRookMovedLeft  = original.whiteRookMovedLeft;
        copy.whiteRookMovedRight = original.whiteRookMovedRight;
        copy.blackRookMovedLeft  = original.blackRookMovedLeft;
        copy.blackRookMovedRight = original.blackRookMovedRight;
        copy.promotionChoice     = 5; // always queen for engine
        // Copy board history so repetition detection works during search
        copy.boardHistory        = new ArrayList<>(original.boardHistory);
        return copy;
    }
}