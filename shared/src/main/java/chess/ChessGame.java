package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessGame chessGame = (ChessGame) o;
        return teamTurn == chessGame.teamTurn && Objects.equals(board, chessGame.board);
    }

    @Override
    public int hashCode() {
        return Objects.hash(teamTurn, board);
    }

    private TeamColor teamTurn;
    private ChessBoard board;

    public ChessGame() {
        this.teamTurn = TeamColor.WHITE;
        this.board = new ChessBoard();
        board.resetBoard();
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
        teamTurn = team;
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
            return new ArrayList<>();
        }
        Collection<ChessMove> possibleMoves = piece.pieceMoves(getBoard(), startPosition);
        Collection<ChessMove> validMoves = new ArrayList<>();

        for (ChessMove possibleMove : possibleMoves) {
            ChessPosition endPosition = possibleMove.endPosition();

            ChessBoard originalBoard = board;
            ChessBoard tempBoard = makeCopy();
            setBoard(tempBoard);

            tempBoard.addPiece(endPosition, piece);
            tempBoard.addPiece(startPosition, null);

            if (!isInCheck(piece.getTeamColor())) {
                validMoves.add(possibleMove);
            }
            setBoard(originalBoard);
        }
        return validMoves;
    }

    private ChessBoard makeCopy() {
        ChessBoard tempBoard = new ChessBoard();
        for (int i = 1; i < 9; i++) {
            for (int j = 1; j < 9; j++) {
                ChessPosition square = new ChessPosition(i, j);
                ChessPiece piece = board.getPiece(square);
                tempBoard.addPiece(square, piece);
            }
        }
        return tempBoard;
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to perform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        ChessPosition startPosition = move.startPosition();
        ChessPosition endPosition = move.endPosition();
        ChessPiece piece = getBoard().getPiece(startPosition);
        Collection<ChessMove> validMoves = validMoves(startPosition);

        if (piece == null || piece.getTeamColor() != teamTurn || !validMoves.contains(move)) {
            throw new InvalidMoveException();
        }

        board.addPiece(endPosition, piece);
        board.addPiece(startPosition, null);

        if (move.promotionPiece() != null) {
            board.addPiece(endPosition, new ChessPiece(piece.getTeamColor(), move.promotionPiece()));
        }

        if (teamTurn == TeamColor.WHITE) {
            teamTurn = TeamColor.BLACK;
        } else {
            teamTurn = TeamColor.WHITE;
        }
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
        for (int i = 1; i < 9; i++) {
            for (int j = 1; j < 9; j++) {
                ChessPosition square = new ChessPosition(i, j);
                ChessPiece piece = board.getPiece(square);
                if (piece != null && piece.getTeamColor() == teamColor && piece.getPieceType() == ChessPiece.PieceType.KING) {
                    kingSquare = square;
                } else if (piece != null && piece.getTeamColor() != teamColor) {
                    enemyMoves.addAll(piece.pieceMoves(board, square));
                }
            }
        }
        for (ChessMove enemyMove : enemyMoves) {
            if (enemyMove.endPosition().equals(kingSquare)) {
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
            ChessPosition startPosition = teamMove.startPosition();
            ChessPosition endPosition = teamMove.endPosition();
            ChessPiece piece = getBoard().getPiece(startPosition);

            ChessBoard originalBoard = board;
            ChessBoard tempBoard = makeCopy();
            setBoard(tempBoard);

            tempBoard.addPiece(endPosition, piece);
            tempBoard.addPiece(startPosition, null);

                if (!isInCheck(teamColor)) {
                    setBoard(originalBoard);
                    return false;
                }
            setBoard(originalBoard);
        }
        return true;
    }

    private Collection<ChessMove> getTeamMoves(TeamColor teamColor) {
        Collection<ChessMove> teamMoves = new ArrayList<>();
        for (int i = 1; i < 9; i++) {
            for (int j = 1; j < 9; j++) {
                ChessPosition square = new ChessPosition(i, j);
                ChessPiece piece = board.getPiece(square);
                if (piece != null && piece.getTeamColor() == teamColor) {
                    teamMoves.addAll(validMoves(square));
                }
            }
        }
        return teamMoves;
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
        }
        Collection<ChessMove> teamMoves = getTeamMoves(teamColor);
        return teamMoves.isEmpty();
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
