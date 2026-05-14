import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class Board{
    int [] squares = new int[64];
    boolean whiteTurn = true;
    Scanner scanner = new Scanner(System.in);

    int enPassantCol = -1;
    int enPassantRow = -1;

    int fiftyMoveCounter = 0;

    ArrayList<int[]> boardHistory = new ArrayList<>();
    //Castling stuff 
    boolean whiteKingMoved = false;
    boolean blackKingMoved = false;
    boolean whiteRookMovedLeft = false;
    boolean whiteRookMovedRight = false;
    boolean blackRookMovedLeft = false;
    boolean blackRookMovedRight = false;
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
        // //set Pawns
        // for(int i = 0; i<= 7; i++){
        //     set(1, i, PAWN);
        //     set(6, i, -PAWN);
        // }
        // //Set Rooks
        // set(0, 0, ROOK);
        // set(0, 7, ROOK);
        // set(7, 0, -ROOK);
        // set(7, 7, -ROOK);
        // //Set knight
        // set(0, 1, KNIGHT);
        // set(0, 6, KNIGHT);
        // set(7, 1, -KNIGHT);
        // set(7, 6, -KNIGHT);
        // //Set Bishop
        // set(0, 2, BISHOP);
        // set(0, 5, BISHOP);
        // set(7, 2, -BISHOP);
        // set(7, 5, -BISHOP);
        // //Set Queen
        // set(0, 3, QUEEN);
        // set(7, 3, -QUEEN);
        // //Set King
        // set(0, 4, KING);
        // set(7, 4, -KING);
        Arrays.fill(squares, EMPTY);
        set(0, 4, KING);
        set(7, 4, -KING);
        set(0, 0, ROOK);
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
    int[][] castling = castlingMoves(row, col);
    int[][] allMoves = new int[count + castling.length][2];
    for (int i = 0; i < count; i++) allMoves[i] = moves[i];
    for (int i = 0; i < castling.length; i++) allMoves[count + i] = castling[i];
    return allMoves;
}
public int[][] kingMovesOnly(int row, int col){
    int[] rowOffsets = {-1, -1, -1,  0,  0,  1,  1,  1};
    int[] colOffsets = {-1,  0,  1, -1,  1, -1,  0,  1};
    int[][] moves = new int[8][2];
    int count = 0;
    for(int i = 0; i < 8; i++){
        int newRow = row + rowOffsets[i];
        int newCol = col + colOffsets[i];
        if (newRow < 0 || newRow > 7 || newCol < 0 || newCol > 7) continue;
        if ((get(row, col) > 0) && (get(newRow, newCol) > 0)) continue;
        if ((get(row, col) < 0) && (get(newRow, newCol) < 0)) continue;
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

public int[][] queenMoves(int row, int col){
    int[][]moves = new int [27][2];
    int count = 0;
    int[][] directions = {{1,0},{-1,0},{0,1},{0,-1},{1,1},{1,-1},{-1,1},{-1,-1}};

    for (int d = 0; d < 8; d++){
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

public int[][] knightMoves(int row, int col){
    int[] rowOffsets = {-2, -2, -1, -1,  1,  1,  2,  2};
    int[] colOffsets = {-1,  1, -2,  2, -2,  2, -1,  1};
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

public int[][] pawnMoves(int row, int col){
    int[][] moves = new int[6][2];
    int count = 0;
    int direction;
    if(get(row, col) > 0){
        direction = 1;
    } else{
        direction = -1;
    }
    int newRow = row + direction;
    //Regular Pawn movement(One step forward)
    if (get(newRow, col) == EMPTY){
        moves[count][0] = newRow;
        moves[count][1] = col;
        count++;
    }
    //Pawn not moving(Can move two spaces forward)
    //White
    if(get(row, col) > 0 && row == 1){
        if(get(newRow, col) == EMPTY && get(row + 2, col) == EMPTY){
            moves[count][0] = row + 2;
            moves[count][1] = col;
            count++;
        }
    }
    //Black
    if(get(row, col) < 0 && row == 6){
        if(get(newRow, col) == EMPTY && get(row - 2, col) == EMPTY){
            moves[count][0] = row - 2;
            moves[count][1] = col;
            count++;
        }
    }
    //White captures diagonally
    if(get(row, col) > 0 && col + 1 <= 7 && get(newRow, col + 1) <0){
        moves[count][0] = newRow;
        moves[count][1] = col + 1;
        count++;
    }
    if (get(row, col) > 0 && col - 1 >= 0 && get(newRow, col - 1) <0 ){
        moves[count][0] = newRow;
        moves[count][1] = col - 1;
        count++;
    }
    //Black captures diagonally
    if(get(row, col) < 0 && col + 1 <= 7 && get(newRow, col + 1) >0){
        moves[count][0] = newRow;
        moves[count][1] = col + 1;
        count++;
    }
    if (get(row, col) < 0 && col - 1 >= 0 && get(newRow, col - 1) >0 ){
        moves[count][0] = newRow;
        moves[count][1] = col - 1;
        count++;
    }
    //En passant for white
    if(get(row, col) > 0 && enPassantRow != -1){
        if(row + 1 == enPassantRow && Math.abs(col - enPassantCol) == 1){
            moves[count][0] = enPassantRow;
            moves[count][1] = enPassantCol;
            count++;
        }
    }
    //En passant for black
    if(get(row, col) < 0 && enPassantRow != -1){
        if(row - 1 == enPassantRow && Math.abs(col - enPassantCol) == 1){
            moves[count][0] = enPassantRow;
            moves[count][1] = enPassantCol;
            count++;
        }
    }

    return Arrays.copyOf(moves, count);
}
//Checks if the move is a valid move
    public boolean makeMove(int fromRow, int fromCol, int toRow, int toCol){
        int piece = get(fromRow, fromCol);
        int [][] validMoves = new int[0][2];
        

        if(piece == PAWN || piece == -PAWN){
            validMoves = pawnMoves(fromRow, fromCol);
        }else if (piece == ROOK || piece == -ROOK) {
            validMoves = rookMoves(fromRow, fromCol);
        }else if (piece == KNIGHT || piece == -KNIGHT) {
            validMoves = knightMoves(fromRow, fromCol);
        }else if (piece == BISHOP || piece == -BISHOP) {
            validMoves = bishopMoves(fromRow, fromCol);
        }else if (piece == QUEEN || piece == -QUEEN) {
            validMoves = queenMoves(fromRow, fromCol);
        }else if (piece == KING || piece == -KING) {
            validMoves = kingMoves(fromRow, fromCol);
        }

        
        //Check for whos turn it is
        if (whiteTurn && piece < 0){
            return false;
        }
        if (!whiteTurn && piece > 0){
            return false;
        }


        for(int i = 0; i <validMoves.length; i++){
            //check if move is valid
            if(validMoves[i][0] == toRow && validMoves[i][1] == toCol){
                if(leavesKingInCheck(fromRow, fromCol, toRow, toCol)){
                    return false;
                }

                boolean isEnPassant = (piece == PAWN || piece == -PAWN) && toCol == enPassantCol && toRow == enPassantRow;

                int capturedPiece = get(toRow, toCol);

                set(toRow, toCol, get(fromRow, fromCol));
                set(fromRow, fromCol, EMPTY);
                //Castling stuff
                if (piece == KING && fromRow == 0 && fromCol == 4) {
                    if (toCol == 6) { set(0, 5, ROOK);  set(0, 7, EMPTY); }
                    else if (toCol == 2) { set(0, 3, ROOK);  set(0, 0, EMPTY); }
                }
                if (piece == -KING && fromRow == 7 && fromCol == 4) {
                    if (toCol == 6) { set(7, 5, -ROOK); set(7, 7, EMPTY); }
                    else if (toCol == 2) { set(7, 3, -ROOK); set(7, 0, EMPTY); }
                }


                // Remove the captured pawn if en passant
                if(isEnPassant){
                    set(fromRow, toCol, EMPTY);
                }

                if((piece == PAWN || piece == -PAWN) && Math.abs(toRow - fromRow) == 2){
                    enPassantRow = (fromRow + toRow) / 2;
                    enPassantCol = toCol;
                } else {
                    enPassantRow = -1;
                    enPassantCol = -1;
                }
                if (piece == KING)  whiteKingMoved = true;
                if (piece == -KING) blackKingMoved = true;
                if (piece == ROOK  && fromRow == 0 && fromCol == 0) whiteRookMovedLeft = true;
                if (piece == ROOK  && fromRow == 0 && fromCol == 7) whiteRookMovedRight = true;
                if (piece == -ROOK && fromRow == 7 && fromCol == 0) blackRookMovedLeft = true;
                if (piece == -ROOK && fromRow == 7 && fromCol == 7) blackRookMovedRight = true;

                if (piece == PAWN && toRow == 7) {
                    System.out.println("Promote pawn! Enter piece (Q, R, B, N):");
                    String choice = scanner.next().toUpperCase();
                    switch(choice) {
                        case "Q": set(toRow, toCol, QUEEN);  break;
                        case "R": set(toRow, toCol, ROOK);   break;
                        case "B": set(toRow, toCol, BISHOP); break;
                        case "N": set(toRow, toCol, KNIGHT); break;
                        default:  set(toRow, toCol, QUEEN);  break; // default to queen
                    }
                }
                if (piece == -PAWN && toRow == 0) {
                    System.out.println("Promote pawn! Enter piece (Q, R, B, N):");
                    String choice = scanner.next().toUpperCase();
                    switch(choice) {
                        case "Q": set(toRow, toCol, -QUEEN);  break;
                        case "R": set(toRow, toCol, -ROOK);   break;
                        case "B": set(toRow, toCol, -BISHOP); break;
                        case "N": set(toRow, toCol, -KNIGHT); break;
                        default:  set(toRow, toCol, -QUEEN);  break;
                    }
                }

                if (piece == PAWN || piece == -PAWN || capturedPiece != EMPTY) {
                    fiftyMoveCounter = 0;  // reset on pawn move or capture
                } else {
                    fiftyMoveCounter++;    // increment otherwise
                }

                boardHistory.add(getBoardSnapshot());

                whiteTurn = !whiteTurn;
                return true;
            }
            
        }
        return false;
    }

    public boolean isInCheck(boolean white){
        int kingRow = -1;
        int kingCol = -1;

        for(int row = 0; row < 8 ; row ++){
            for(int col = 0; col <8; col++){
                if(white && get(row, col) == KING){
                    kingRow = row;
                    kingCol = col;
                }
                if (!white && get(row, col) == -KING){
                    kingRow = row;
                    kingCol = col;
                }
            }
            
        }

        for (int row = 0; row < 8 ; row ++){
            for(int col = 0; col <8; col++){
                int piece = get(row, col);
                int[][] enemyMoves = new int[0][2];

                if(white && piece < 0 || !white && piece > 0){
                    if(piece == PAWN || piece == -PAWN){
                        enemyMoves = pawnMoves(row, col);
                    }else if (piece == ROOK || piece == -ROOK) {
                        enemyMoves = rookMoves(row, col);
                    }else if (piece == KNIGHT || piece == -KNIGHT) {
                        enemyMoves = knightMoves(row, col);
                    }else if (piece == BISHOP || piece == -BISHOP) {
                        enemyMoves = bishopMoves(row, col);
                    }else if (piece == QUEEN || piece == -QUEEN) {
                        enemyMoves = queenMoves(row, col);
                    }else if (piece == KING || piece == -KING) {
                        enemyMoves = kingMovesOnly(row, col);
                    }
                }
                for(int i = 0; i < enemyMoves.length; i++){
                    if(enemyMoves[i][0] == kingRow && enemyMoves[i][1] == kingCol){
                        return true;
                    }
                }
            }

        }
        return false;
    }

    public boolean leavesKingInCheck(int fromRow, int fromCol, int toRow, int toCol){
        int movingPiece = get(fromRow, fromCol);
        int capturedPiece = get(toRow, toCol);

        set(toRow, toCol, movingPiece);
        set(fromRow, fromCol, EMPTY);

        boolean inCheck = isInCheck(movingPiece > 0);

        set(fromRow, fromCol, movingPiece);
        set(toRow, toCol, capturedPiece);

        return inCheck;
    }

    public boolean isCheckmate(boolean white){
        if(!isInCheck(white)){
            return false;
        }

        for (int row = 0; row < 8; row++){
            for(int col = 0; col < 8; col++){
                int piece = get(row, col);

                if (white && piece <= 0) continue;
                if (!white && piece >= 0) continue;

                int[][] moves = new int[0][2];
                if (piece == PAWN || piece == -PAWN)     moves = pawnMoves(row, col);
                else if (piece == ROOK || piece == -ROOK)     moves = rookMoves(row, col);
                else if (piece == KNIGHT || piece == -KNIGHT) moves = knightMoves(row, col);
                else if (piece == BISHOP || piece == -BISHOP) moves = bishopMoves(row, col);
                else if (piece == QUEEN || piece == -QUEEN)   moves = queenMoves(row, col);
                else if (piece == KING || piece == -KING)     moves = kingMoves(row, col);

                for (int i = 0; i < moves.length; i++) {
                    if (!leavesKingInCheck(row, col, moves[i][0], moves[i][1])) {
                        return false;
                    }
                }
            }
        }

        return true;
    }
    public boolean isStalemate(boolean white){
        if (isInCheck(white)) return false;
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                int piece = get(row, col);
                if (white && piece <= 0) continue;
                if (!white && piece >= 0) continue;

                int[][] moves = new int[0][2];
                if (piece == PAWN || piece == -PAWN)         moves = pawnMoves(row, col);
                else if (piece == ROOK || piece == -ROOK)     moves = rookMoves(row, col);
                else if (piece == KNIGHT || piece == -KNIGHT) moves = knightMoves(row, col);
                else if (piece == BISHOP || piece == -BISHOP) moves = bishopMoves(row, col);
                else if (piece == QUEEN || piece == -QUEEN)   moves = queenMoves(row, col);
                else if (piece == KING || piece == -KING)     moves = kingMoves(row, col);

                for (int i = 0; i < moves.length; i++) {
                    if (!leavesKingInCheck(row, col, moves[i][0], moves[i][1])) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
    public int[][] castlingMoves(int row, int col) {
        int[][] moves = new int[2][2];
        int count = 0;
        boolean white = get(row, col) > 0;

        // Can't castle while in check
        if (isInCheck(white)) return new int[0][2];

        if (white && !whiteKingMoved) {
            // White kingside (right)
            if (!whiteRookMovedRight
                    && get(0, 5) == EMPTY
                    && get(0, 6) == EMPTY
                    && !isInCheck(true)
                    && !squareAttacked(0, 5, true)
                    && !squareAttacked(0, 6, true)) {
                moves[count][0] = 0;
                moves[count][1] = 6;
                count++;
            }
            // White queenside (left)
            if (!whiteRookMovedLeft
                    && get(0, 1) == EMPTY
                    && get(0, 2) == EMPTY
                    && get(0, 3) == EMPTY
                    && !squareAttacked(0, 2, true)
                    && !squareAttacked(0, 3, true)) {
                moves[count][0] = 0;
                moves[count][1] = 2;
                count++;
            }
        }

        if (!white && !blackKingMoved) {
            // Black kingside (right)
            if (!blackRookMovedRight
                    && get(7, 5) == EMPTY
                    && get(7, 6) == EMPTY
                    && !isInCheck(false)
                    && !squareAttacked(7, 5, false)
                    && !squareAttacked(7, 6, false)) {
                moves[count][0] = 7;
                moves[count][1] = 6;
                count++;
            }
            // Black queenside (left)
            if (!blackRookMovedLeft
                    && get(7, 1) == EMPTY
                    && get(7, 2) == EMPTY
                    && get(7, 3) == EMPTY
                    && !squareAttacked(7, 2, false)
                    && !squareAttacked(7, 3, false)) {
                moves[count][0] = 7;
                moves[count][1] = 2;
                count++;
            }
        }

        return Arrays.copyOf(moves, count);
    }

    public boolean squareAttacked(int targetRow, int targetCol, boolean white) {
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                int piece = get(row, col);
                if (white && piece >= 0) continue;  // skip friendly/empty
                if (!white && piece <= 0) continue;

                int[][] moves = new int[0][2];
                if (piece == PAWN || piece == -PAWN)         moves = pawnMoves(row, col);
                else if (piece == ROOK || piece == -ROOK)     moves = rookMoves(row, col);
                else if (piece == KNIGHT || piece == -KNIGHT) moves = knightMoves(row, col);
                else if (piece == BISHOP || piece == -BISHOP) moves = bishopMoves(row, col);
                else if (piece == QUEEN || piece == -QUEEN)   moves = queenMoves(row, col);
                else if (piece == KING || piece == -KING)     moves = kingMovesOnly(row, col);

                for (int i = 0; i < moves.length; i++) {
                    if (moves[i][0] == targetRow && moves[i][1] == targetCol) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    public boolean isInsufficientMaterial(){
        int whiteBishops = 0, whiteKnights = 0, whitePawns = 0;
        int blackBishops = 0, blackKnights = 0, blackPawns = 0;
        int whiteOther = 0, blackOther = 0;
        int whiteBishopColor = -1, blackBishopColor = -1;

        for (int row = 0; row <8; row++){
            for (int col = 0; col < 8; col++){
                int piece = get(row, col);
                int squareColor = (row + col) % 2; // 0 or 1

                switch (piece) {
                    case PAWN:    whitePawns++;   break;
                    case KNIGHT:  whiteKnights++; break;
                    case BISHOP:  whiteBishops++; whiteBishopColor = squareColor; break;
                    case ROOK: case QUEEN: whiteOther++; break;
                    case -PAWN:   blackPawns++;   break;
                    case -KNIGHT: blackKnights++; break;
                    case -BISHOP: blackBishops++; blackBishopColor = squareColor; break;
                    case -ROOK: case -QUEEN: blackOther++; break;    
                }
            }
        }
        // If anyone has pawns, rooks, or queens — not insufficient
        if (whitePawns > 0 || blackPawns > 0) return false;
        if (whiteOther > 0 || blackOther > 0) return false;

        // King vs King
        if (whiteKnights == 0 && whiteBishops == 0 && blackKnights == 0 && blackBishops == 0) return true;

        // King + Knight vs King
        if (whiteKnights == 1 && whiteBishops == 0 && blackKnights == 0 && blackBishops == 0) return true;
        if (blackKnights == 1 && blackBishops == 0 && whiteKnights == 0 && whiteBishops == 0) return true;

        // King + Bishop vs King
        if (whiteBishops == 1 && whiteKnights == 0 && blackKnights == 0 && blackBishops == 0) return true;
        if (blackBishops == 1 && blackKnights == 0 && whiteKnights == 0 && whiteBishops == 0) return true;

        // King + Bishop vs King + Bishop (same color bishops)
        if (whiteBishops == 1 && blackBishops == 1 && whiteKnights == 0 && blackKnights == 0) {
            if (whiteBishopColor == blackBishopColor) return true;
        }

        return false;
    }
    public int[] getBoardSnapshot() {
        return Arrays.copyOf(squares, squares.length);
    }

    public boolean isThreefoldRepetition(){
        int[] current = getBoardSnapshot();
        int count = 0;
        for (int[] snapshot : boardHistory){
            if(Arrays.equals(snapshot, current)){
                count++;
            }
        }
        return count >= 3;
    }

    public static void main(String[] args){
        Board b = new Board();
        b.initBoard();
        b.boardHistory.add(b.getBoardSnapshot());
        
        if (b.isInsufficientMaterial()) {
            System.out.println("Draw! Insufficient material!"); 
            return;
        }
    
        while(true){
            b.printBoard();
            System.out.println(b.whiteTurn ? "White's turn" : "Black's turn");
            System.out.println("Enter move (fromRow fromCol toRow toCol):");
            int fromRow = b.scanner.nextInt();
            int fromCol = b.scanner.nextInt();
            int toRow = b.scanner.nextInt();
            int toCol = b.scanner.nextInt();
        
            boolean success = b.makeMove(fromRow, fromCol, toRow, toCol);
            if(success){
                System.out.println("Move made!");
                if(b.isInCheck(true))  System.out.println("White is in check!");
                if(b.isInCheck(false)) System.out.println("Black is in check!");

                if (b.isCheckmate(true))  { System.out.println("Checkmate! Black wins!"); break; }
                if (b.isCheckmate(false)) { System.out.println("Checkmate! White wins!"); break; }
                if (b.isStalemate(true)) {System.out.println("Stalemate! Its a draw!"); break; }
                if (b.isStalemate(false)) {System.out.println("Stalemate! Its a draw!"); break; }
                if (b.fiftyMoveCounter >= 100) { System.out.println("Draw! Fifty move rule!"); break; }
                if (b.isThreefoldRepetition()) { System.out.println("Draw! Threefold repetition!"); break; }
            } else {
                System.out.println("Invalid move!");
            }
        }
    }
}

