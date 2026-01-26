package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessPiece that = (ChessPiece) o;
        return pieceColor == that.pieceColor && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pieceColor, type);
    }

    private final ChessGame.TeamColor pieceColor;
    private final PieceType type;

    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.pieceColor = pieceColor;
        this.type = type;

    }

    /**
     * The various different chess piece options
     */
    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor() {
        return pieceColor;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return type;
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> possibleMoves = new ArrayList<>();
        int row = myPosition.getRow();
        int col = myPosition.getColumn();
        if (type == PieceType.KING) {
            int[] options = {-1, 0, 1};
            for (int rowOption : options) {
                for (int colOption : options) {
                    if (!(rowOption == 0 && colOption == 0)) {
                        int possibleRow = row + rowOption;
                        int possibleCol = col + colOption;
                        ChessPosition possiblePosition = new ChessPosition(possibleRow, possibleCol);
                        if (1 <= possibleRow && possibleRow <=8 && 1 <= possibleCol && possibleCol <= 8) {
                            ChessPiece otherPiece = board.getPiece(possiblePosition);
                            if (otherPiece == null) {
                                possibleMoves.add(new ChessMove(myPosition, possiblePosition, null));
                            } else if (otherPiece.getTeamColor() != pieceColor) {
                                possibleMoves.add(new ChessMove(myPosition, possiblePosition, null));
                            }
                        }
                    }
                }
            }
        } else if (type == PieceType.QUEEN) {

        } else if (type == PieceType.BISHOP) {

        } else if (type == PieceType.KNIGHT) {

        } else if (type == PieceType.ROOK) {

        } else if (type == PieceType.PAWN) {

        }
        // possibleMoves.add(new ChessMove(myPosition, possiblePosition));
        return possibleMoves;
    }
}
