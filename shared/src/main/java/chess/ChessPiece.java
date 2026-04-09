package chess;

import dataaccess.DataAccessException;

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

    public String getLetter(PieceType type) {
        return switch (type) {
            case PieceType.ROOK -> "R";
            case PieceType.KNIGHT -> "N";
            case PieceType.BISHOP -> "B";
            case PieceType.KING -> "K";
            case PieceType.QUEEN -> "Q";
            case PieceType.PAWN -> "P";
        };
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
            getKingMoves(board, myPosition, possibleMoves, row, col);
        } else if (type == PieceType.QUEEN) {
            getDiagonalMoves(board, myPosition, possibleMoves, row, col);
            getHorizontalVerticalMoves(board, myPosition, possibleMoves, row, col);
        } else if (type == PieceType.BISHOP) {
            getDiagonalMoves(board, myPosition, possibleMoves, row, col);
        } else if (type == PieceType.KNIGHT) {
            getKnightMoves(board, myPosition, possibleMoves, row, col);
        } else if (type == PieceType.ROOK) {
            getHorizontalVerticalMoves(board, myPosition, possibleMoves, row, col);
        } else if (type == PieceType.PAWN) {
            getPawnMoves(board, myPosition, possibleMoves, row, col);
        }
        return possibleMoves;
    }

    private void getKingMoves(ChessBoard board, ChessPosition myPosition, Collection<ChessMove> possibleMoves, int row, int col) {
        int[] options = {-1, 0, 1};
        for (int r = 0; r <= 2; r++) {
            int possibleRow = row + options[r];
            for (int c = 0; c <= 2; c++) {
                int possibleCol = col + options[c];
                ChessPosition possiblePosition = new ChessPosition(possibleRow, possibleCol);
                if (!(possibleRow == 0 && possibleCol == 0)) {
                    if (1 <= possibleRow && possibleRow <= 8 && 1 <= possibleCol && possibleCol <= 8) {
                        ChessPiece otherPiece = board.getPiece(possiblePosition);
                        addPieces(otherPiece, possibleMoves, myPosition, possiblePosition);
                    }
                }
            }
        }
    }

    private void addPieces(ChessPiece otherPiece, Collection<ChessMove> possibleMoves, ChessPosition myPosition, ChessPosition possiblePosition) {
        if (otherPiece == null) {
            possibleMoves.add(new ChessMove(myPosition, possiblePosition, null));
        } else {
            if (otherPiece.pieceColor != pieceColor) {
                possibleMoves.add(new ChessMove(myPosition, possiblePosition, null));
            }
        }
    }

    private void getDiagonalMoves(ChessBoard board, ChessPosition myPosition, Collection<ChessMove> possibleMoves, int row, int col) {
        int[] rowOptions = {1, 1, -1, -1};
        int[] colOptions = {1, -1, 1, -1};

        for (int d = 0; d < 4; d++) {
            int r = row + rowOptions[d];
            int c = col + colOptions[d];
            while (1 <= r && r <= 8 && 1 <= c && c <= 8) {
                ChessPosition possiblePosition = new ChessPosition(r, c);
                ChessPiece otherPiece = board.getPiece(possiblePosition);
                if (otherPiece == null) {
                    possibleMoves.add(new ChessMove(myPosition, possiblePosition, null));
                } else {
                    if (otherPiece.pieceColor != pieceColor) {
                        possibleMoves.add(new ChessMove(myPosition, possiblePosition, null));
                    }
                    break;
                }
                r += rowOptions[d];
                c += colOptions[d];
            }
        }
    }

    private void getHorizontalVerticalMoves(ChessBoard board, ChessPosition myPosition, Collection<ChessMove> possibleMoves, int row, int col) {
        int[] options = {1, -1};

        for (int d = 0; d < 2; d++) {
            int r = row + options[d];
            while (1 <= r && r <= 8 && 1 <= col && col <= 8) {
                ChessPosition possiblePosition = new ChessPosition(r, col);
                ChessPiece otherPiece = board.getPiece(possiblePosition);
                if (otherPiece == null) {
                    possibleMoves.add(new ChessMove(myPosition, possiblePosition, null));
                } else {
                    if (otherPiece.pieceColor != pieceColor) {
                        possibleMoves.add(new ChessMove(myPosition, possiblePosition, null));
                    }
                    break;
                }
                r += options[d];
            }
        }
        for (int d = 0; d < 2; d++) {
            int c = col + options[d];
            while (1 <= row && row <= 8 && 1 <= c && c <= 8) {
                ChessPosition possiblePosition = new ChessPosition(row, c);
                ChessPiece otherPiece = board.getPiece(possiblePosition);
                if (otherPiece == null) {
                    possibleMoves.add(new ChessMove(myPosition, possiblePosition, null));
                } else {
                    if (otherPiece.pieceColor != pieceColor) {
                        possibleMoves.add(new ChessMove(myPosition, possiblePosition, null));
                    }
                    break;
                }
                c += options[d];
            }
        }
    }

    private void getKnightMoves(ChessBoard board, ChessPosition myPosition, Collection<ChessMove> possibleMoves, int row, int col) {
        int[] rowOptions = {1, 1, -1, -1, 2, 2, -2, -2};
        int[] colOptions = {2, -2, 2, -2, 1, -1, 1, -1};

        for (int d = 0; d < 8; d++) {
            int r = row + rowOptions[d];
            int c = col + colOptions[d];
            if (1 <= r && r <= 8 && 1 <= c && c <= 8) {
                ChessPosition possiblePosition = new ChessPosition(r, c);
                ChessPiece otherPiece = board.getPiece(possiblePosition);
                if (otherPiece == null) {
                    possibleMoves.add(new ChessMove(myPosition, possiblePosition, null));
                } else if (otherPiece.pieceColor != pieceColor) {
                    possibleMoves.add(new ChessMove(myPosition, possiblePosition, null));
                }
            }
        }
    }

    private void getPawnMoves(ChessBoard board, ChessPosition myPosition, Collection<ChessMove> possibleMoves, int row, int col) {
        if (getTeamColor() == ChessGame.TeamColor.BLACK && 2 <= row && row <= 7 && 1 <= col && col <= 8) {
            ChessPosition possiblePosition = new ChessPosition(row - 1, col);
            ChessPiece otherPiece = board.getPiece(possiblePosition);

            if (otherPiece == null) {
                addMoves(row, possibleMoves, myPosition, possiblePosition);
            }

            if (row == 7) {
                ChessPosition possiblePosition2 = new ChessPosition(row - 2, col);
                ChessPiece otherPiece2 = board.getPiece(possiblePosition2);
                if (otherPiece == null && otherPiece2 == null) {
                    possibleMoves.add(new ChessMove(myPosition, possiblePosition2, null));
                }
            }

            int[] options = {-1, 1};
            for (int d = 0; d < 2; d++) {
                int c = col + options[d];
                if (1 <= c && c <= 8) {
                    ChessPosition possiblePosition3 = new ChessPosition(row - 1, c);
                    ChessPiece otherPiece3 = board.getPiece(possiblePosition3);
                    if (otherPiece3 != null && otherPiece3.pieceColor != pieceColor) {
                        addMoves(row, possibleMoves, myPosition, possiblePosition3);
                    }
                }
            }
        } else if (getTeamColor() == ChessGame.TeamColor.WHITE && 2 <= row && row <= 7 && 1 <= col && col <= 8) {
            ChessPosition possiblePosition = new ChessPosition(row + 1, col);
            ChessPiece otherPiece = board.getPiece(possiblePosition);

            if (otherPiece == null) {
                addMoves(row, possibleMoves, myPosition, possiblePosition);
            }

            if (row == 2) {
                ChessPosition possiblePosition2 = new ChessPosition(row + 2, col);
                ChessPiece otherPiece2 = board.getPiece(possiblePosition2);
                if (otherPiece == null && otherPiece2 == null) {
                    possibleMoves.add(new ChessMove(myPosition, possiblePosition2, null));
                }
            }

            int[] options = {-1, 1};
            for (int d = 0; d < 2; d++) {
                int c = col + options[d];
                if (1 <= c && c <= 8) {
                    ChessPosition possiblePosition3 = new ChessPosition(row + 1, c);
                    ChessPiece otherPiece3 = board.getPiece(possiblePosition3);
                    if (otherPiece3 != null && otherPiece3.pieceColor != pieceColor) {
                        addMoves(row, possibleMoves, myPosition, possiblePosition3);
                    }
                }
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