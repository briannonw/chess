package client;

import static ui.EscapeSequences.*;

public class Board {

    public static void drawBoard(boolean isWhite) {
        String[] columns;
        int[] rows;
        String[] pieces;
        if (isWhite) {
            rows = new int[] {8, 7, 6, 5, 4, 3, 2, 1};
            columns = new String[] {"a", "b", "c", "d", "e", "f", "g", "h"};
            pieces = new String[] {"R", "N", "B", "Q", "K", "B", "N", "R"};
        } else {
            rows = new int[] {1, 2, 3, 4, 5, 6, 7, 8};
            columns = new String[] {"h", "g", "f", "e", "d", "c", "b", "a"};
            pieces = new String[] {"R", "N", "B", "K", "Q", "B", "N", "R"};
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
                        System.out.print(SET_TEXT_COLOR_BLUE  + " " + piece + " ");
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
                for (int col = 0; col < 8; col ++) {
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
