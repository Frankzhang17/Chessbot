import java.util.ArrayList;

public class MCTSNode {
    int[] move;           // the move that got us here [fromRow, fromCol, toRow, toCol]
    MCTSNode parent;
    ArrayList<MCTSNode> children = new ArrayList<>();

    int visits = 0;
    double wins = 0;

    int[] boardState;     // snapshot of the board
    boolean whiteTurn;

    public MCTSNode(MCTSNode parent, int[] move, int[] boardState, boolean whiteTurn) {
        this.parent = parent;
        this.move = move;
        this.boardState = boardState;
        this.whiteTurn = whiteTurn;
    }

    public double getUCB() {
        if (visits == 0) return Double.MAX_VALUE;
        return (wins / visits) + Math.sqrt(2 * Math.log(parent.visits) / visits);
    }
}
