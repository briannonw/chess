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

        switch (type) {
            case KING -> addKingMoves(board, myPosition, possibleMoves);
            case QUEEN -> {
                addDiagonalMoves(board, myPosition, possibleMoves);
                addHorizontalVerticalMoves(board, myPosition, possibleMoves);
            }
            case BISHOP -> addDiagonalMoves(board, myPosition, possibleMoves);
            case ROOK -> addHorizontalVerticalMoves(board, myPosition, possibleMoves);
            case KNIGHT -> addKnightMoves(board, myPosition, possibleMoves);
            case PAWN -> addPawnMoves(board, myPosition, possibleMoves);
        }

        return possibleMoves;
    }

    private void addKingMoves(ChessBoard board, ChessPosition pos, Collection<ChessMove> moves) {
        int row = pos.getRow();
        int col = pos.getColumn();

        for (int rOffset = -1; rOffset <= 1; rOffset++) {
            for (int cOffset = -1; cOffset <= 1; cOffset++) {

                if (rOffset == 0 && cOffset == 0) continue;

                int r = row + rOffset;
                int c = col + cOffset;

                if (!inBounds(r, c)) continue;

                addIfValid(board, pos, new ChessPosition(r, c), moves);
            }
        }
    }

    private boolean inBounds(int r, int c) {
        return r >= 1 && r <= 8 && c >= 1 && c <= 8;
    }

    private void addIfValid(ChessBoard board, ChessPosition from,
                            ChessPosition to, Collection<ChessMove> moves) {

        ChessPiece target = board.getPiece(to);

        if (target == null || target.pieceColor != pieceColor) {
            moves.add(new ChessMove(from, to, null));
        }
    }

    private void addKnightMoves(ChessBoard board, ChessPosition pos, Collection<ChessMove> moves) {
        int row = pos.getRow();
        int col = pos.getColumn();

        int[] rowOffsets = {1,1,-1,-1,2,2,-2,-2};
        int[] colOffsets = {2,-2,2,-2,1,-1,1,-1};

        for (int i = 0; i < 8; i++) {
            int r = row + rowOffsets[i];
            int c = col + colOffsets[i];

            if (!inBounds(r, c)) continue;

            addIfValid(board, pos, new ChessPosition(r, c), moves);
        }
    }

    private void addPawnMoves(ChessBoard board, ChessPosition pos, Collection<ChessMove> moves) {
        int row = pos.getRow();
        int col = pos.getColumn();

        int direction = (pieceColor == ChessGame.TeamColor.WHITE) ? 1 : -1;
        int startRow = (pieceColor == ChessGame.TeamColor.WHITE) ? 2 : 7;

        int forwardRow = row + direction;
        if (inBounds(forwardRow, col) && board.getPiece(new ChessPosition(forwardRow, col)) == null) {
            addMoves(row, moves, pos, new ChessPosition(forwardRow, col));

            if (row == startRow) {
                int doubleRow = row + 2 * direction;
                if (board.getPiece(new ChessPosition(doubleRow, col)) == null) {
                    moves.add(new ChessMove(pos, new ChessPosition(doubleRow, col), null));
                }
            }
        }

        int[] captureCols = {-1, 1};
        for (int offset : captureCols) {
            int captureCol = col + offset;
            int captureRow = row + direction;

            if (!inBounds(captureRow, captureCol)) continue;

            ChessPosition capturePos = new ChessPosition(captureRow, captureCol);
            ChessPiece target = board.getPiece(capturePos);

            if (target != null && target.pieceColor != pieceColor) {
                addMoves(row, moves, pos, capturePos);
            }
        }
    }

    private void addDiagonalMoves(ChessBoard board, ChessPosition pos, Collection<ChessMove> moves) {

        int row = pos.getRow();
        int col = pos.getColumn();

        int[] rowOffsets = {1, 1, -1, -1};
        int[] colOffsets = {1, -1, 1, -1};

        for (int d = 0; d < 4; d++) {
            int r = row + rowOffsets[d];
            int c = col + colOffsets[d];

            while (inBounds(r, c)) {

                ChessPosition newPos = new ChessPosition(r, c);
                ChessPiece target = board.getPiece(newPos);

                if (target == null) {
                    moves.add(new ChessMove(pos, newPos, null));
                } else {
                    if (target.pieceColor != pieceColor) {
                        moves.add(new ChessMove(pos, newPos, null));
                    }
                    break;
                }

                r += rowOffsets[d];
                c += colOffsets[d];
            }
        }
    }

    private void addHorizontalVerticalMoves(ChessBoard board, ChessPosition pos, Collection<ChessMove> moves) {
        int row = pos.getRow();
        int col = pos.getColumn();

        int[] directions = {1, -1};

        for (int dir : directions) {
            int r = row + dir;

            while (inBounds(r, col)) {

                ChessPosition newPos = new ChessPosition(r, col);
                ChessPiece target = board.getPiece(newPos);

                if (target == null) {
                    moves.add(new ChessMove(pos, newPos, null));
                } else {
                    if (target.pieceColor != pieceColor) {
                        moves.add(new ChessMove(pos, newPos, null));
                    }
                    break;
                }

                r += dir;
            }
        }

        for (int dir : directions) {
            int c = col + dir;

            while (inBounds(row, c)) {

                ChessPosition newPos = new ChessPosition(row, c);
                ChessPiece target = board.getPiece(newPos);

                if (target == null) {
                    moves.add(new ChessMove(pos, newPos, null));
                } else {
                    if (target.pieceColor != pieceColor) {
                        moves.add(new ChessMove(pos, newPos, null));
                    }
                    break;
                }

                c += dir;
            }
        }
    }

    private void addMoves(int row, Collection<ChessMove> possibleMoves, ChessPosition myPosition, ChessPosition possiblePosition) {
        if ((row == 2 && pieceColor == ChessGame.TeamColor.BLACK) || (row == 7 && pieceColor == ChessGame.TeamColor.WHITE)) {
            possibleMoves.add(new ChessMove(myPosition, possiblePosition, PieceType.QUEEN));
            possibleMoves.add(new ChessMove(myPosition, possiblePosition, PieceType.ROOK));
            possibleMoves.add(new ChessMove(myPosition, possiblePosition, PieceType.BISHOP));
            possibleMoves.add(new ChessMove(myPosition, possiblePosition, PieceType.KNIGHT));
        } else {
            possibleMoves.add(new ChessMove(myPosition, possiblePosition, null));
        }
    }
}