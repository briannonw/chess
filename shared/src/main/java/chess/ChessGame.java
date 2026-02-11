package chess;

import java.util.ArrayList;
import java.util.Collection;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {

    private TeamColor teamTurn;
    private ChessBoard board;

    public ChessGame() {
        this.teamTurn = TeamColor.WHITE;
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return teamTurn;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        if (team == TeamColor.WHITE) {
            teamTurn = TeamColor.BLACK;
        } else {
            teamTurn = TeamColor.WHITE;
        }
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        ChessPiece piece = getBoard().getPiece(startPosition);
        if (piece == null) {
            return null;
        }
        Collection<ChessMove> possibleMoves = piece.pieceMoves(getBoard(), startPosition);
        Collection<ChessMove> validMoves = new ArrayList<>();

        for (ChessMove possibleMove : possibleMoves) {
            ChessBoard newBoard = getBoard();
            try {
                makeMove(possibleMove);
                if (!isInCheck(piece.getTeamColor())) {
                    validMoves.add(possibleMove);
                }
                setBoard(newBoard);
            } catch (InvalidMoveException exception) {
                // skip
            }
        }
        return validMoves;
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to perform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        ChessPosition startPosition = move.getStartPosition();
        ChessPosition endPosition = move.getEndPosition();
        ChessPiece piece = getBoard().getPiece(startPosition);
        Collection<ChessMove> validMoves = validMoves(startPosition);

        if (piece == null || piece.getTeamColor() != teamTurn || !validMoves.contains(move) || isInCheck(piece.getTeamColor())) {
            throw new InvalidMoveException();
        }

        board.addPiece(endPosition, piece);
        board.addPiece(startPosition, null);

        setTeamTurn(piece.getTeamColor());
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        Collection<ChessMove> enemyMoves = new ArrayList<>();
        ChessPosition kingSquare = null;
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                ChessPosition square = new ChessPosition(i, j);
                ChessPiece piece = board.getPiece(square);
                if (piece.getTeamColor() == teamColor && piece.getPieceType() == ChessPiece.PieceType.KING) {
                    kingSquare = square;
                } else if (piece.getTeamColor() != teamColor) {
                    enemyMoves.addAll(validMoves(square));
                }
            }
        }
        for (ChessMove enemyMove : enemyMoves) {
            if (enemyMove.getEndPosition() == kingSquare) {
                return true;
            }
        }
        return false;
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        if (!isInCheck(teamColor)) {
            return false;
        }
        Collection<ChessMove> teamMoves = getTeamMoves(teamColor);
        for (ChessMove teamMove : teamMoves) {
            ChessBoard testBoard = board;
            try {
                makeMove(teamMove);
                if (!isInCheck(teamColor)) {
                    setBoard(testBoard);
                    return false;
                }
            } catch (InvalidMoveException exception) {
                // skip
            }
            setBoard(testBoard);
        }
        return true;
    }

    private Collection<ChessMove> getTeamMoves(TeamColor teamColor) {
        Collection<ChessMove> teamMoves = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                ChessPosition square = new ChessPosition(i, j);
                ChessPiece piece = board.getPiece(square);
                if (piece != null && piece.getTeamColor() == teamColor) {
                    teamMoves.addAll(validMoves(square));
                }
            }
        }
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves while not in check.
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        if (isInCheck(teamColor)) {
            return false;
        };
        Collection<ChessMove> teamMoves = getTeamMoves(teamColor);
        if (teamMoves != null) {
            return false;
        }
        return true;
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        this.board = board;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return board;
    }
}
