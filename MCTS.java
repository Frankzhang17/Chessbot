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

        return best != null ? best.move : null;
    }
}