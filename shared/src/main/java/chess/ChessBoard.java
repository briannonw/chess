package chess;

import java.util.Arrays;
import java.util.Objects;

/**
 * A chessboard that can hold and rearrange chess pieces.
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessBoard {

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessBoard that = (ChessBoard) o;
        return Objects.deepEquals(board, that.board);
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(board);
    }

    private ChessPiece[][] board;

    public ChessBoard() {
        board = new ChessPiece[8][8];
    }

    /**
     * Adds a chess piece to the chessboard
     *
     * @param position where to add the piece to
     * @param piece    the piece to add
     */
    public void addPiece(ChessPosition position, ChessPiece piece) {
        int row = position.getRow() -1;
        int col = position.getColumn() - 1;
        board[row][col] = piece;
    }

    /**
     * Gets a chess piece on the chessboard
     *
     * @param position The position to get the piece from
     * @return Either the piece at the position, or null if no piece is at that
     * position
     */
    public ChessPiece getPiece(ChessPosition position) {
        int row = position.getRow() -1;
        int col = position.getColumn() - 1;
        return board[row][col];
    }

    /**
     * Sets the board to the default starting board
     * (How the game of chess normally starts)
     */
    public void resetBoard() {
        board = new ChessPiece[8][8];

        for (int col = 1; col <= 8; col++) {
            addPiece(new ChessPosition(2, col), new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN));
            addPiece(new ChessPosition(7, col), new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.PAWN));
        }

//        int[] direction = {1, -1};
//        int[] fbrow = {1, 8};
//        int[] column = {1, 8};
//        ChessGame.TeamColor[] colors = {
//                ChessGame.TeamColor.WHITE,
//                ChessGame.TeamColor.BLACK
//        };
//        for (int d = 0; d < 2; d++) {
//            int col = column[d];
//            addPiece(new ChessPosition(fbrow[d], col), new ChessPiece(colors[d], ChessPiece.PieceType.ROOK));
//            addPiece(new ChessPosition(fbrow[d], col), new ChessPiece(colors[d], ChessPiece.PieceType.KNIGHT));
//            addPiece(new ChessPosition(fbrow[d], col), new ChessPiece(colors[d], ChessPiece.PieceType.BISHOP));
//            addPiece(new ChessPosition(fbrow[d], col), new ChessPiece(colors[d], ChessPiece.PieceType.QUEEN));
//            addPiece(new ChessPosition(fbrow[d], col), new ChessPiece(colors[d], ChessPiece.PieceType.KING));
//            addPiece(new ChessPosition(fbrow[d], col), new ChessPiece(colors[d], ChessPiece.PieceType.BISHOP));
//            addPiece(new ChessPosition(fbrow[d], col), new ChessPiece(colors[d], ChessPiece.PieceType.KNIGHT));
//            addPiece(new ChessPosition(fbrow[d], col), new ChessPiece(colors[d], ChessPiece.PieceType.ROOK));
//            col += direction[d];
//        }

    }
}
