import java.util.ArrayList;

public class MCTS {

    static final int SIMULATIONS = 1000;
    boolean playingAsWhite;

    public MCTS(boolean playingAsWhite) {
        this.playingAsWhite = playingAsWhite;
    }

    public int[] getBestMove(Board board) {
        MCTSNode root = new MCTSNode(null, null, board.getBoardSnapshot(), board.whiteTurn);

        for (int i = 0; i < SIMULATIONS; i++) {
            Board simBoard = copyBoard(board);
            MCTSNode node = select(root, simBoard);
            MCTSNode expanded = expand(node, simBoard);
            double result = simulate(simBoard);
            backpropagate(expanded, result);
        }

        // Pick the child with the most visits
        MCTSNode best = null;
        for (MCTSNode child : root.children) {
            if (best == null || child.visits > best.visits) best = child;
        }

        System.out.println("Engine is thinking...");
        for (MCTSNode child : root.children) {
            if (child.move != null) {
                System.out.printf("Move [%d,%d -> %d,%d] visits: %d wins: %.0f winrate: %.1f%%%n",
                    child.move[0], child.move[1], child.move[2], child.move[3],
                    child.visits, child.wins,
                    child.visits > 0 ? (child.wins / child.visits) * 100 : 0);
            }
        }
        if (best != null) {
            System.out.printf("Best move: [%d,%d -> %d,%d] with %d visits%n",
                best.move[0], best.move[1], best.move[2], best.move[3], best.visits);
        }

        return best != null ? best.move : null;
    }

    public Board copyBoard(Board original) {
        Board copy = new Board();
        copy.squares = java.util.Arrays.copyOf(original.squares, original.squares.length);
        copy.whiteTurn = original.whiteTurn;
        copy.enPassantCol = original.enPassantCol;
        copy.enPassantRow = original.enPassantRow;
        copy.fiftyMoveCounter = original.fiftyMoveCounter;
        copy.whiteKingMoved = original.whiteKingMoved;
        copy.blackKingMoved = original.blackKingMoved;
        copy.whiteRookMovedLeft = original.whiteRookMovedLeft;
        copy.whiteRookMovedRight = original.whiteRookMovedRight;
        copy.blackRookMovedLeft = original.blackRookMovedLeft;
        copy.blackRookMovedRight = original.blackRookMovedRight;
        copy.promotionChoice = original.promotionChoice;
        return copy;
    }

    public MCTSNode select(MCTSNode node, Board board) {
        while (node.children.size() > 0) {
            MCTSNode best = null;
            for (MCTSNode child : node.children) {
                if (best == null || child.getUCB() > best.getUCB()) best = child;
            }
            node = best;
            board.makeMove(node.move[0], node.move[1], node.move[2], node.move[3]);
        }
        return node;
    }

    public MCTSNode expand(MCTSNode node, Board board) {
        ArrayList<int[]> allMoves = getAllMoves(board, board.whiteTurn);
    
        if (allMoves.size() == 0) return node;

        // If children already exist, just pick the unvisited one with highest UCB
        if (node.children.size() > 0) {
            MCTSNode selected = null;
            for (MCTSNode child : node.children) {
                if (selected == null || child.getUCB() > selected.getUCB()) selected = child;
            }
            board.makeMove(selected.move[0], selected.move[1], selected.move[2], selected.move[3]);
            return selected;
        }

        // First time visiting this node — add ALL moves as children
        for (int[] move : allMoves) {
            MCTSNode child = new MCTSNode(node, move, board.getBoardSnapshot(), board.whiteTurn);
            node.children.add(child);
        }

        // Pick a random child to simulate from first
        MCTSNode selected = node.children.get((int)(Math.random() * node.children.size()));
        board.makeMove(selected.move[0], selected.move[1], selected.move[2], selected.move[3]);
        return selected;
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

    public double simulate(Board board) {
        int maxMoves = 200;
        int moveCount = 0;

        while (moveCount < maxMoves) {
            // Check if game is over
            if (board.isCheckmate(true))  return playingAsWhite ? 0.0 : 1.0;
            if (board.isCheckmate(false)) return playingAsWhite ? 1.0 : 0.0;
            if (board.isStalemate(true))  return 0.5;
            if (board.isStalemate(false)) return 0.5;
            if (board.fiftyMoveCounter >= 100) return 0.5;
            if (board.isInsufficientMaterial()) return 0.5;

            // Get all legal moves and pick a random one
            ArrayList<int[]> moves = getAllMoves(board, board.whiteTurn);
            if (moves.size() == 0) return 0.5;

            moves.sort((a, b) -> scoreMove(board, b) - scoreMove(board, a));
            int topN = Math.max(1, moves.size() / 3);
            int[] move = moves.get((int)(Math.random() * topN));
            board.makeMove(move[0], move[1], move[2], move[3]);
            moveCount++;
        } 

        return 0.5; // if game goes too long call it a draw
    }

    public void backpropagate(MCTSNode node, double result) {
        while (node != null) {
            node.visits++;
            node.wins += result;
            node = node.parent;
        }
    }

    public int scoreMove(Board board, int[] move) {
        int score = 0;
        int target = board.get(move[2], move[3]);

        // Reward captures based on piece value
        switch (Math.abs(target)) {
            case 1: score += 10;  break; // pawn
            case 2: score += 30;  break; // knight
            case 3: score += 30;  break; // bishop
            case 4: score += 50;  break; // rook
            case 5: score += 90;  break; // queen
            case 6: score += 900; break; // king
        }

        // Reward moving pawns forward
        int piece = board.get(move[0], move[1]);
        if (piece == 1  && move[2] > move[0]) score += 2; // white pawn forward
        if (piece == -1 && move[2] < move[0]) score += 2; // black pawn forward

        // Reward controlling the center
        int toRow = move[2];
        int toCol = move[3];
        if (toRow >= 3 && toRow <= 4 && toCol >= 3 && toCol <= 4) score += 5;

        return score;
    }

}