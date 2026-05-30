public class TTEntry {
    static final int EXACT = 0; // score is exact
    static final int ALPHA = 1; // score is an upper bound
    static final int BETA  = 2; // score is a lower bound

    int score;
    int depth;
    int flag;

    public TTEntry(int score, int depth, int flag) {
        this.score = score;
        this.depth = depth;
        this.flag  = flag;
    }
}