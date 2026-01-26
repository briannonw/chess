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
            getDiagonalMoves(board, myPosition, possibleMoves, row, col);
            getHorizontalVerticalMoves(board, myPosition, possibleMoves, row, col);
        } else if (type == PieceType.BISHOP) {
            getDiagonalMoves(board, myPosition, possibleMoves, row, col);
        } else if (type == PieceType.KNIGHT) {
            int[] rowOptions = {1, 1, -1, -1, 2, 2, -2, -2};
            int[] colOptions = {2, -2, 2, -2, 1, -1, 1, -1};

            for (int o = 0; o < 8; o++) {
                int r = row + rowOptions[o];
                int c = col + colOptions[o];
                if (1 <= r && r <= 8 && 1 <= c && c <= 8) {
                    ChessPosition possiblePosition = new ChessPosition(r, c);
                    ChessPiece otherPiece = board.getPiece(possiblePosition);
                    if (otherPiece == null) {
                        possibleMoves.add(new ChessMove(myPosition, possiblePosition, null));
                    } else if (otherPiece.getTeamColor() != pieceColor) {
                        possibleMoves.add(new ChessMove(myPosition, possiblePosition, null));
                    }
                }
            }

        } else if (type == PieceType.ROOK) {
            getHorizontalVerticalMoves(board, myPosition, possibleMoves, row, col);
        } else if (type == PieceType.PAWN) {
            // Change promotion piece part from null to ?
            if (pieceColor == ChessGame.TeamColor.BLACK) {
                if (row > 1) {
                    row--;
                    ChessPosition possiblePosition = new ChessPosition(row, col);
                    ChessPiece otherPiece = board.getPiece(possiblePosition);
                    if (otherPiece == null) {
                        possibleMoves.add(new ChessMove(myPosition, possiblePosition, null));
                    }
                    if (row == 6) {
                        int secondRow = row - 1;
                        ChessPosition secondPosition = new ChessPosition(secondRow, col);
                        ChessPiece secondOtherPiece = board.getPiece(secondPosition);
                        if (secondOtherPiece == null) {
                            possibleMoves.add(new ChessMove(myPosition, secondPosition, null));
                        }
                    }
                    int[] colOptions = {-1, 1};
                    for (int o = 0; o < 2; o++) {
                        int c = col + colOptions[o];
                        if (1 <= c && c <= 8) {
                            ChessPosition thirdPosition = new ChessPosition(row, c);
                            ChessPiece thirdOtherPiece = board.getPiece(thirdPosition);
                            if (thirdOtherPiece != null && thirdOtherPiece.getTeamColor() != pieceColor) {
                                possibleMoves.add(new ChessMove(myPosition, thirdPosition, null));
                            }
                        }
                    }
                }
            } else {
                if (row < 8) {
                    row++;
                    ChessPosition possiblePosition = new ChessPosition(row, col);
                    ChessPiece otherPiece = board.getPiece(possiblePosition);
                    if (otherPiece == null) {
                        possibleMoves.add(new ChessMove(myPosition, possiblePosition, null));
                    }
                    if (row == 3) {
                        int secondRow = row + 1;
                        ChessPosition secondPosition = new ChessPosition(secondRow, col);
                        ChessPiece secondOtherPiece = board.getPiece(secondPosition);
                        if (secondOtherPiece == null) {
                            possibleMoves.add(new ChessMove(myPosition, secondPosition, null));
                        }
                    }
                    int[] colOptions = {-1, 1};
                    for (int o = 0; o < 2; o++) {
                        int c = col + colOptions[o];
                        if (1 <= c && c <= 8) {
                            ChessPosition thirdPosition = new ChessPosition(row, c);
                            ChessPiece thirdOtherPiece = board.getPiece(thirdPosition);
                            if (thirdOtherPiece != null && thirdOtherPiece.getTeamColor() != pieceColor) {
                                possibleMoves.add(new ChessMove(myPosition, thirdPosition, null));
                            }
                        }
                    }
                }
            }
        }
        return possibleMoves;
    }

    private void getDiagonalMoves(ChessBoard board, ChessPosition myPosition, Collection<ChessMove> possibleMoves, int row, int col) {
        int[] rowOptions = {1, 1, -1, -1};
        int[] colOptions = {1, -1, 1, -1};

        for (int o = 0; o < 4; o++) {
            int r = row + rowOptions[o];
            int c = col + colOptions[o];
            while (1 <= r && r <= 8 && 1 <= c && c <= 8) {
                ChessPosition possiblePosition = new ChessPosition(r, c);
                ChessPiece otherPiece = board.getPiece(possiblePosition);
                if (otherPiece == null) {
                    possibleMoves.add(new ChessMove(myPosition, possiblePosition, null));
                } else if (otherPiece.getTeamColor() != pieceColor) {
                    possibleMoves.add(new ChessMove(myPosition, possiblePosition, null));
                    break;
                }
                r += rowOptions[o];
                c += colOptions[o];
            }
        }
    }

    private void getHorizontalVerticalMoves(ChessBoard board, ChessPosition myPosition, Collection<ChessMove> possibleMoves, int row, int col) {
        int[] options = {1, -1};

        for (int o = 0; o < 2; o++) {
            int r = row + options[o];
            while (1 <= r && r <= 8 && 1 <= col && col <= 8) {
                ChessPosition possiblePosition = new ChessPosition(r, col);
                ChessPiece otherPiece = board.getPiece(possiblePosition);
                if (otherPiece == null) {
                    possibleMoves.add(new ChessMove(myPosition, possiblePosition, null));
                } else if (otherPiece.getTeamColor() != pieceColor) {
                    possibleMoves.add(new ChessMove(myPosition, possiblePosition, null));
                    break;
                }
                r += options[o];
            }
        }
        for (int o = 0; o < 2; o++) {
            int c = col + options[o];
            while (1 <= row && row <= 8 && 1 <= c && c <= 8) {
                ChessPosition possiblePosition = new ChessPosition(row, c);
                ChessPiece otherPiece = board.getPiece(possiblePosition);
                if (otherPiece == null) {
                    possibleMoves.add(new ChessMove(myPosition, possiblePosition, null));
                } else if (otherPiece.getTeamColor() != pieceColor) {
                    possibleMoves.add(new ChessMove(myPosition, possiblePosition, null));
                    break;
                }
                c += options[o];
            }
        }
    }
}
