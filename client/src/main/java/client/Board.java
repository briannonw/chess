package client;

import chess.ChessGame;
import chess.ChessPosition;

import java.util.HashSet;
import java.util.Set;

import static ui.EscapeSequences.*;

public class Board {
    private static ChessGame currentGame;

    public static void updateFromChessGame(ChessGame game) {
        currentGame = game;
    }
    private static Set<ChessPosition> highlightedSquares = new HashSet<>();
    private static ChessPosition selectedSquare = null;

    public static void drawBoard(boolean isWhite) {
        if (currentGame == null) {
            drawNewBoard(isWhite);
        } else {
            drawUpdatedBoard(isWhite);
        }
    }

    private static ChessPosition displayPosition(ChessPosition pos, boolean isWhite) {
        if (isWhite) return pos;

        return new ChessPosition(pos.getRow(), 9 - pos.getColumn());
    }

    public static ChessGame getCurrentGame() {
        return currentGame;
    }

    public static void setHighlightedSquares(Set<ChessPosition> squares) {
        highlightedSquares = squares;
    }

    public static void clearHighlightedSquares() {
        highlightedSquares.clear();
        selectedSquare = null;
    }

    public static void setSelectedSquare(ChessPosition pos) {
        selectedSquare = pos;
    }

    private static void drawUpdatedBoard(boolean isWhite) {
        String[] columns;
        int[] rows;
        if (isWhite) {
            rows = new int[]{8, 7, 6, 5, 4, 3, 2, 1};
            columns = new String[]{"a", "b", "c", "d", "e", "f", "g", "h"};
        } else {
            rows = new int[]{1, 2, 3, 4, 5, 6, 7, 8};
            columns = new String[]{"h", "g", "f", "e", "d", "c", "b", "a"};
        }

        System.out.print(SET_BG_COLOR_LIGHT_GREY + "   ");
        for (String column : columns) {
            System.out.print(" " + column + " ");
        }
        System.out.println("   " + RESET_BG_COLOR);

        boolean whiteSquare = true;
        for (int row : rows) {
            System.out.print(SET_BG_COLOR_LIGHT_GREY + " " + row + " " + RESET_BG_COLOR);

            for (int col = 0; col < 8; col++) {
                ChessPosition pos = new ChessPosition(row, col + 1);
                pos = displayPosition(pos, isWhite);
                boolean isHighlighted = highlightedSquares.contains(pos);
                boolean isSelected = selectedSquare != null && selectedSquare.equals(pos);

                if (isSelected) {
                    System.out.print(SET_BG_COLOR_MAGENTA);
                } else if (isHighlighted) {
                    System.out.print(SET_BG_COLOR_YELLOW);
                } else if (whiteSquare) {
                    System.out.print(SET_BG_COLOR_WHITE);
                } else {
                    System.out.print(SET_BG_COLOR_DARK_GREEN);
                }
                whiteSquare = !whiteSquare;

                chess.ChessPiece chessPiece = currentGame.getBoard().getPiece(pos);

                if (chessPiece != null) {
                    String letter = chessPiece.getLetter(chessPiece.getPieceType());
                    if (chessPiece.getTeamColor() == ChessGame.TeamColor.WHITE) {
                        System.out.print(SET_TEXT_COLOR_BLUE + " " + letter + " ");
                    } else {
                        System.out.print(SET_TEXT_COLOR_RED + " " + letter + " ");
                    }
                } else {
                    System.out.print("   ");
                }
            }

            whiteSquare = !whiteSquare;
            System.out.println(RESET_TEXT_COLOR + SET_BG_COLOR_LIGHT_GREY + " " + row + " " + RESET_BG_COLOR);
        }

        System.out.print(SET_BG_COLOR_LIGHT_GREY + "   ");
        for (String column : columns) {
            System.out.print(" " + column + " ");
        }
        System.out.println("   " + RESET_BG_COLOR);
    }

    private static void drawNewBoard(boolean isWhite) {
        String[] columns;
        int[] rows;
        String[] pieces;
        if (isWhite) {
            rows = new int[]{8, 7, 6, 5, 4, 3, 2, 1};
            columns = new String[]{"a", "b", "c", "d", "e", "f", "g", "h"};
            pieces = new String[]{"R", "N", "B", "Q", "K", "B", "N", "R"};
        } else {
            rows = new int[]{1, 2, 3, 4, 5, 6, 7, 8};
            columns = new String[]{"h", "g", "f", "e", "d", "c", "b", "a"};
            pieces = new String[]{"R", "N", "B", "K", "Q", "B", "N", "R"};
        }

        System.out.print(SET_BG_COLOR_LIGHT_GREY + "   ");
        for (String column : columns) {
            System.out.print(" " + column + " ");
        }
        System.out.println("   " + RESET_BG_COLOR);

        boolean whiteSquare = true;
        for (int row : rows) {
            System.out.print(SET_BG_COLOR_LIGHT_GREY + " " + row + " " + RESET_BG_COLOR);

            if (row == 1 || row == 8) {
                for (String piece : pieces) {
                    if (whiteSquare) {
                        System.out.print(SET_BG_COLOR_WHITE);
                    } else {
                        System.out.print(SET_BG_COLOR_DARK_GREEN);
                    }
                    whiteSquare = !whiteSquare;
                    if (row == 8) {
                        System.out.print(SET_TEXT_COLOR_BLUE + " " + piece + " ");
                    } else {
                        System.out.print(SET_TEXT_COLOR_RED + " " + piece + " ");
                    }
                }
            } else if (row == 2 || row == 7) {
                for (int i = 0; i < 8; i++) {
                    if (whiteSquare) {
                        System.out.print(SET_BG_COLOR_WHITE);
                    } else {
                        System.out.print(SET_BG_COLOR_DARK_GREEN);
                    }
                    whiteSquare = !whiteSquare;
                    if (row == 7) {
                        System.out.print(SET_TEXT_COLOR_BLUE + " P ");
                    } else {
                        System.out.print(SET_TEXT_COLOR_RED + " P ");
                    }
                }
            } else {
                for (int col = 0; col < 8; col++) {
                    if (whiteSquare) {
                        System.out.print(SET_BG_COLOR_WHITE + "   ");
                    } else {
                        System.out.print(SET_BG_COLOR_DARK_GREEN + "   ");
                    }
                    whiteSquare = !whiteSquare;
                }
                System.out.print(RESET_BG_COLOR);
            }

            whiteSquare = !whiteSquare;
            System.out.println(RESET_TEXT_COLOR + SET_BG_COLOR_LIGHT_GREY + " " + row + " " + RESET_BG_COLOR);
        }

        System.out.print(SET_BG_COLOR_LIGHT_GREY + "   ");
        for (String column : columns) {
            System.out.print(" " + column + " ");
        }
        System.out.println("   " + RESET_BG_COLOR);
    }
}
