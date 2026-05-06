import java.util.Arrays;

public class Board{
    int [] squares = new int[64];
    // Convert rank + file to a flat index
    public int index(int row, int col){
        return row * 8 + col;
    }

    // Get the piece at a given square
    public int get(int row, int col){
        return squares[index(row, col)];
    }
    // Place a piece on a square
    public void set(int row, int col, int piece){
        squares[index(row, col)] = piece;
    }

    static final int EMPTY = 0;
    static final int PAWN = 1;
    static final int  KNIGHT = 2;
    static final int BISHOP = 3;
    static final int ROOK = 4;
    static final int QUEEN = 5;
    static final int KING = 6;

    public void initBoard(){
        //set Pawns
        for(int i = 0; i<= 7; i++){
            set(1, i, PAWN);
            set(6, i, -PAWN);
        }
        //Set Rooks
        set(0, 0, ROOK);
        set(0, 7, ROOK);
        set(7, 0, -ROOK);
        set(7, 7, -ROOK);
        //Set knight
        set(0, 1, KNIGHT);
        set(0, 6, KNIGHT);
        set(7, 1, -KNIGHT);
        set(7, 6, -KNIGHT);
        //Set Bishop
        set(0, 2, BISHOP);
        set(0, 5, BISHOP);
        set(7, 2, -BISHOP);
        set(7, 5, -BISHOP);
        //Set Queen
        set(0, 3, QUEEN);
        set(7, 3, -QUEEN);
        //Set King
        set(0, 4, KING);
        set(7, 4, -KING);
    }
    public void printBoard(){
    for(int row = 7; row >= 0; row--){
        for(int col = 0; col <= 7; col++){
            int piece = get(row, col);
            String symbol;
            switch(piece){
                case  PAWN:   symbol = "P"; break;
                case -PAWN:   symbol = "p"; break;
                case  KNIGHT: symbol = "N"; break;
                case -KNIGHT: symbol = "n"; break;
                case  BISHOP: symbol = "B"; break;
                case -BISHOP: symbol = "b"; break;
                case  ROOK:   symbol = "R"; break;
                case -ROOK:   symbol = "r"; break;
                case  QUEEN:  symbol = "Q"; break;
                case -QUEEN:  symbol = "q"; break;
                case  KING:   symbol = "K"; break;
                case -KING:   symbol = "k"; break;
                default:      symbol = "."; break;
            }
            System.out.print(symbol + " ");
        }
        System.out.println();
    }
}


public int[][] kingMoves(int row, int col){
    int[] rowOffsets = {-1, -1, -1,  0,  0,  1,  1,  1};
    int[] colOffsets = {-1,  0,  1, -1,  1, -1,  0,  1};
    int[][] moves = new int[8][2];
    int count = 0;
    for(int i = 0; i < 8; i++){
        int newRow = row + rowOffsets[i];
        int newCol = col + colOffsets[i];
        //Checks if outside board
        if (newRow < 0 || newRow > 7 || newCol < 0 || newCol > 7){
            continue;
        }
        //Checks if its about to move into its own piece
        if ((get(row, col) > 0 ) && (get(newRow, newCol) > 0 && get(row, col) > 0)){
            continue;
        }
        if ((get(row, col) < 0 ) && (get(newRow, newCol) < 0 && get(row, col) < 0)){
            continue;
        }
        moves[count][0] = newRow;
        moves[count][1] = newCol;
        count++;
    }
    return Arrays.copyOf(moves, count);
}
public int[][] rookMoves(int row, int col){
    int[][]moves = new int [14][2];
    int count = 0;
    int [][] directions = {{1,0},{-1,0},{0,1},{0,-1}};

    for (int d = 0; d < 4; d++){
        for (int step = 1; step <=7; step++){
            int newRow = row + directions[d][0] * step;
            int newCol = col + directions[d][1] * step;
            if (newRow < 0 || newRow > 7 || newCol < 0 || newCol > 7){
                break;
            }
            if ((get(row, col) > 0 ) && (get(newRow, newCol) > 0 && get(row, col) > 0)){
                break;
            }
            if ((get(row, col) < 0 ) && (get(newRow, newCol) < 0 && get(row, col) < 0)){
                break;
            }
            if (get(row, col) > 0 && get(newRow, newCol) < 0){
                moves[count][0] = newRow;
                moves[count][1] = newCol;
                count++;
                break;
            }
            if (get(row, col) < 0 && get(newRow, newCol) > 0){
                moves[count][0] = newRow;
                moves[count][1] = newCol;
                count++;
                break;
            }
            moves[count][0] = newRow;
            moves[count][1] = newCol;
            count++;
        }
        
    }
    return Arrays.copyOf(moves, count);
}

public int[][] bishopMoves(int row, int col){
    int[][]moves = new int [14][2];
    int count = 0;
    int[][] directions = {{1,1},{1,-1},{-1,1},{-1,-1}};

    for (int d = 0; d < 4; d++){
        for (int step = 1; step <=7; step++){
            int newRow = row + directions[d][0] * step;
            int newCol = col + directions[d][1] * step;
            if (newRow < 0 || newRow > 7 || newCol < 0 || newCol > 7){
                break;
            }
            if ((get(row, col) > 0 ) && (get(newRow, newCol) > 0 && get(row, col) > 0)){
                break;
            }
            if ((get(row, col) < 0 ) && (get(newRow, newCol) < 0 && get(row, col) < 0)){
                break;
            }
            if (get(row, col) > 0 && get(newRow, newCol) < 0){
                moves[count][0] = newRow;
                moves[count][1] = newCol;
                count++;
                break;
            }
            if (get(row, col) < 0 && get(newRow, newCol) > 0){
                moves[count][0] = newRow;
                moves[count][1] = newCol;
                count++;
                break;
            }
            moves[count][0] = newRow;
            moves[count][1] = newCol;
            count++;
        }
        
    }
    return Arrays.copyOf(moves, count);
}
public static void main(String[] args){
        Board b = new Board();
        b.initBoard();
        b.printBoard();
        int[][] moves = b.kingMoves(0, 4);
        for(int i = 0; i < moves.length; i++){
            System.out.println("Move: " + moves[i][0] + ", " + moves[i][1]);
        }
    }
}