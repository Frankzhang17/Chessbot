import java.util.ArrayList;

public class MCTS {

    static final int MAX_DEPTH = 4;
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

    // -------------------------------------------------------
    // Main entry point — same signature as before so chessGUI
    // doesn't need any changes
    // -------------------------------------------------------
    public int[] getBestMove(Board board) {
        ArrayList<int[]> moves = getAllMoves(board, board.whiteTurn);
        if (moves.isEmpty()) return null;

        // Order moves so the search sees good moves first (better pruning)
        moves.sort((a, b) -> scoreMoveForOrdering(board, b) - scoreMoveForOrdering(board, a));

        int[] bestMove = null;
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

        if (bestMove != null) {
            System.out.printf("Best move: [%d,%d -> %d,%d] score: %d%n",
                bestMove[0], bestMove[1], bestMove[2], bestMove[3], bestScore);
        }

        return bestMove;
    }

    // -------------------------------------------------------
    // Minimax with alpha-beta pruning
    // maximizing = true  → white is choosing (wants highest score)
    // maximizing = false → black is choosing (wants lowest score)
    // -------------------------------------------------------
    private int minimax(Board board, int depth, int alpha, int beta, boolean maximizing) {

        // Draw checks
        if (board.fiftyMoveCounter >= 100) return 0;
        if (board.isInsufficientMaterial()) return 0;

        ArrayList<int[]> moves = getAllMoves(board, board.whiteTurn);

        // Terminal node: no moves available
        if (moves.isEmpty()) {
            if (board.isInCheck(board.whiteTurn)) {
                // Checkmate — penalise heavily; prefer faster mates with depth bonus
                return board.whiteTurn ? (-100000 - depth) : (100000 + depth);
            }
            return 0; // Stalemate
        }

        // Reached search horizon — return static evaluation
        if (depth == 0) return quiescence(board, alpha, beta);

        // Order moves for better pruning at every level
        moves.sort((a, b) -> scoreMoveForOrdering(board, b) - scoreMoveForOrdering(board, a));

        if (maximizing) {
            int best = Integer.MIN_VALUE;
            for (int[] move : moves) {
                Board copy = copyBoard(board);
                copy.makeMove(move[0], move[1], move[2], move[3]);
                int score = minimax(copy, depth - 1, alpha, beta, false);
                best = Math.max(best, score);
                alpha = Math.max(alpha, best);
                if (beta <= alpha) break; // beta cutoff
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
                if (beta <= alpha) break; // alpha cutoff
            }
            return best;
        }
    }

    // -------------------------------------------------------
    // Static evaluation — returns a score in centipawns
    // positive = good for white, negative = good for black
    // -------------------------------------------------------
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
                    case 1:
                        materialScore   = 100;
                        positionalScore = PAWN_TABLE[tableRow][col];
                        break;
                    case 2:
                        materialScore   = 320;
                        positionalScore = KNIGHT_TABLE[tableRow][col];
                        break;
                    case 3:
                        materialScore   = 330;
                        positionalScore = BISHOP_TABLE[tableRow][col];
                        break;
                    case 4:
                        materialScore   = 500;
                        positionalScore = ROOK_TABLE[tableRow][col];
                        break;
                    case 5:
                        materialScore   = 900;
                        positionalScore = QUEEN_TABLE[tableRow][col];
                        break;
                    case 6:
                        positionalScore = KING_TABLE[tableRow][col];
                        break;
                }

                int total = materialScore + positionalScore;
                score += isWhite ? total : -total;
            }
        }
        return score;
    }

    // -------------------------------------------------------
    // Move ordering score — higher = search this move first
    // Uses MVV-LVA (Most Valuable Victim, Least Valuable Attacker)
    // -------------------------------------------------------
    public int scoreMoveForOrdering(Board board, int[] move) {
        int score = 0;
        int attacker = Math.abs(board.get(move[0], move[1]));
        int victim   = Math.abs(board.get(move[2], move[3]));

        // MVV-LVA: reward capturing valuable pieces with cheap pieces
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

    // -------------------------------------------------------
    // Helpers — identical to before so nothing else breaks
    // -------------------------------------------------------
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

    private int quiescence(Board board, int alpha, int beta) {
        // Get a "stand pat" score — what's the position worth right now
        int standPat = evaluate(board);

        // Beta cutoff — position is already too good for opponent
        if (standPat >= beta) return beta;

        // Update alpha if standing pat is better
        if (standPat > alpha) alpha = standPat;

        // Only look at captures
        ArrayList<int[]> moves = getAllMoves(board, board.whiteTurn);
        moves.removeIf(move -> board.get(move[2], move[3]) == 0); // remove non-captures
        moves.sort((a, b) -> scoreMoveForOrdering(board, b) - scoreMoveForOrdering(board, a));

        for (int[] move : moves) {
            Board copy = copyBoard(board);
            copy.makeMove(move[0], move[1], move[2], move[3]);
            int score = -quiescence(copy, -beta, -alpha);

            if (score >= beta) return beta;
            if (score > alpha) alpha = score;
        }

        return alpha;
    }   

    public Board copyBoard(Board original) {
        Board copy = new Board();
        copy.squares          = java.util.Arrays.copyOf(original.squares, original.squares.length);
        copy.whiteTurn        = original.whiteTurn;
        copy.enPassantCol     = original.enPassantCol;
        copy.enPassantRow     = original.enPassantRow;
        copy.fiftyMoveCounter = original.fiftyMoveCounter;
        copy.whiteKingMoved   = original.whiteKingMoved;
        copy.blackKingMoved   = original.blackKingMoved;
        copy.whiteRookMovedLeft  = original.whiteRookMovedLeft;
        copy.whiteRookMovedRight = original.whiteRookMovedRight;
        copy.blackRookMovedLeft  = original.blackRookMovedLeft;
        copy.blackRookMovedRight = original.blackRookMovedRight;
        copy.promotionChoice  = original.promotionChoice;
        return copy;
    }
}