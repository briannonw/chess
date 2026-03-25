package client;

import dataaccess.DataAccessException;
import model.ListGamesData;
import service.ListGamesResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import static ui.EscapeSequences.*;

public class ChessClient {
    private final ServerFacade server;
    private String authToken = null;

    public ChessClient(int port) throws DataAccessException {
        server = new ServerFacade(port);
    }

    public void run() {
        System.out.println("Successfully running");
        System.out.print(help());

        Scanner scanner = new Scanner(System.in);
        var result = "";

        while(!result.equals("quit")) {
            printPrompt();
            String line = scanner.nextLine();

            try {
                result = eval(line);
                if (!result.equals("quit")) {
                    System.out.print(result);
                }
            } catch (Throwable e) {
                var msg = e.toString();
                System.out.print(msg);
            }
        }
        System.out.println();
    }

    private void printPrompt() {
        if (authToken == null) {
            System.out.print("\n[LOGGED_OUT] >>> ");
        } else {
            System.out.print("\n[LOGGED_IN] >>> ");
        }
    }

    public String eval(String input) {
        try {
            String[] tokens = input.split(" ");
            String cmd = (tokens.length > 0) ? tokens[0] : "help";
            String[] params = Arrays.copyOfRange(tokens, 1, tokens.length);
            return switch (cmd) {
                case "help" -> help();
                case "register" -> register(params);
                case "login" -> login(params);
                case "logout" -> logout(params);
                case "create" -> createGame(params);
                case "list" -> listGames(params);
                case "join" -> joinGame(params);
                case "observe" -> observeGame(params);
                case "quit" -> "quit";
                default -> help();
            };
        } catch (DataAccessException ex) {
            return SET_TEXT_COLOR_RED + ex.getMessage() + RESET_TEXT_COLOR;
        }
    }

    public String help() {
        if (authToken == null) {
            return SET_TEXT_COLOR_BLUE + """
                    register <username> <password> <email>
                    login <username> <password>
                    quit
                    help""" + RESET_TEXT_COLOR;
        } else {
            return SET_TEXT_COLOR_BLUE + """
                    create <gameName>
                    list
                    join <gameID> <WHITE|BLACK>
                    observe <gameID>
                    logout
                    quit
                    help""" + RESET_TEXT_COLOR;
        }
    }

    public String register(String[] params) throws DataAccessException {
        if (params.length == 3) {
            var result = server.register(params[0], params[1], params[2]);
            authToken = result.authToken();

            return "Registered and logged in as " + result.username();
        }
        throw new DataAccessException("Expected: register <username> <password> <email>");
    }

    public String login(String[] params) throws DataAccessException {
        if (params.length == 2) {
            var result = server.login(params[0], params[1]);
            authToken = result.authToken();

            return "Logged in as " + result.username();
        }
        throw new DataAccessException("Expected: login <username> <password>");
    }

    public String logout(String[] params) throws DataAccessException {
        if (params.length == 0) {
            server.logout(authToken);
            authToken = null;

            return "Logged out";
        }
        throw new DataAccessException("Expected: logout");
    }

    public String createGame(String[] params) throws DataAccessException {
        if (params.length == 1) {
            server.createGame(authToken, params[0]);

            return "Created game: " + params[0];
        }
        throw new DataAccessException("Expected: create <gameName>");
    }

    public String listGames(String[] params) throws DataAccessException {
        if (params.length == 0) {
            ListGamesResult gamesList = server.listGames(authToken);
            games = gamesList.games();

            var result = new StringBuilder();

            int i = 1;
            int size = gamesList.games().size();
            for (ListGamesData game : gamesList.games()) {
                result.append(i).append(". ").append(game.gameName()).append(" | WHITE: ");
                if (game.whiteUsername() == null) {
                    result.append("empty").append(" | BLACK: ");
                } else {
                    result.append(game.whiteUsername()).append(" | BLACK: ");
                }
                if (game.blackUsername() == null) {
                    result.append("empty");
                } else {
                    result.append(game.blackUsername());
                }
                if (i < size) {
                    result.append('\n');
                }
                i++;
            }
            return result.toString();
        }
        throw new DataAccessException("Expected: list");
    }

    private List<ListGamesData> games = new ArrayList<>();

    public String joinGame(String[] params) throws DataAccessException {
        if (params.length == 2) {
            int i;
            try {
                i = Integer.parseInt(params[0]);
            } catch (NumberFormatException e) {
                throw new DataAccessException("Error: Invalid game number");
            }

            if (i < 1 || i > games.size()) {
                throw new DataAccessException("Error:  Invalid game number");
            }

            ListGamesData game = games.get(i - 1);

            int gameID = game.gameID();
            String playerColor = params[1].toUpperCase();

            if (!playerColor.equals("WHITE") && !playerColor.equals("BLACK")) {
                throw new DataAccessException("Error: Invalid player color");
            }

            server.joinGame(authToken, playerColor, gameID);

            boolean isWhite = playerColor.equals("WHITE");
            Board.drawBoard(isWhite);

            return "Joined Game " + i + " as " + playerColor;
        }
        throw new DataAccessException("Expected: join <gameID> <WHITE|BLACK>");
    }

    public String observeGame(String[] params) throws DataAccessException {
        if (params.length == 1) {
            int i;
            try {
                i = Integer.parseInt(params[0]);
            } catch (NumberFormatException e) {
                throw new DataAccessException("Error: Invalid game number");
            }

            if (i < 1 || i > games.size()) {
                throw new DataAccessException("Error:  Invalid game number");
            }

            ListGamesData game = games.get(i - 1);
            int gameID = game.gameID();

//            server.observeGame(authToken, gameID);

            Board.drawBoard(true);

            return "Observing game " + i;
        }
        throw new DataAccessException("Expected: observe <gameID>");
    }
}
