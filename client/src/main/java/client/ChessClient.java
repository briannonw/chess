package client;

import dataaccess.DataAccessException;
import model.GameData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

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
        System.out.print("\n>>> ");
    }

    public String eval(String input) {
        try {
            String[] tokens = input.toLowerCase().split(" ");
            String cmd = (tokens.length > 0) ? tokens[0] : "help";
            String[] params = Arrays.copyOfRange(tokens, 1, tokens.length);
            return switch (cmd) {
                case "help" -> help();
                case "register" -> register(params);
                case "login" -> login(params);
                case "logout" -> logout();
                case "create" -> createGame(params);
                case "list" -> listGames();
                case "join" -> joinGame(params);
                case "observe" -> observeGame(params);
                case "quit" -> "quit";
                default -> help();
            };
        } catch (DataAccessException ex) {
            return ex.getMessage();
        }
    }

    public String help() {
        if (authToken == null) {
            return """
                    register <username> <password> <email>
                    login <username> <password>
                    quit
                    help
                    """;
        } else {
            return """
                    create <gameName>
                    list
                    join <gameName> <WHITE|BLACK>
                    observe <ID>
                    logout
                    quit
                    help
                    """;
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
        return "";
    }

    public String logout() throws DataAccessException {
        return "";
    }

    public String createGame(String[] params) throws DataAccessException {
        return "";
    }

    private List<GameData> games = new ArrayList<>();
    public String listGames() throws DataAccessException {
        return "";
    }

    public String joinGame(String[] params) throws DataAccessException {
        return "";
    }

    public String observeGame(String[] params) throws DataAccessException {
        return "";
    }
}
