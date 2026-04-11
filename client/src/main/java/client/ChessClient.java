package client;

import chess.ChessGame;
import chess.ChessPosition;
import dataaccess.DataAccessException;
import model.ListGamesData;
import service.ListGamesResult;
import webSocketMessages.Notification;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.*;

import static ui.EscapeSequences.*;

public class ChessClient implements NotificationHandler {
    private final ServerFacade server;
    private String authToken = null;
    private String username = null;
    private String gameName = null;
    private WebSocketFacade ws;
    private boolean inGame = false;
    private boolean isWhitePlayer = true;
    private int currentGameID;
    private final Gson gson = new Gson();
    private boolean isObserver = false;

    @Override
    public void notify(Notification notification) {
        System.out.println(SET_TEXT_COLOR_MAGENTA + notification.message() + RESET_TEXT_COLOR);
    }

    public ChessClient(int port) {
        server = new ServerFacade(port);
    }

    public void run() {
        System.out.println("Successfully running");
        System.out.print(help());

        Scanner scanner = new Scanner(System.in);
        var result = "";

        while(!result.equals("quit")) {
            System.out.println();
            printPrompt();
            String line = scanner.nextLine();

            try {
                result = eval(line);
                if (!result.equals("quit") && !result.isEmpty()) {
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
        String state;

        if (inGame) {
            state = "[IN_GAME]";
        } else if (authToken == null) {
            state = "[LOGGED_OUT]";
        } else {
            state = "[LOGGED_IN]";
        }

        System.out.print(state + " >>> ");
    }

    public String eval(String input) {
        try {
            String[] tokens = input.split(" ");
            String cmd = (tokens.length > 0) ? tokens[0] : "help";
            String[] params = Arrays.copyOfRange(tokens, 1, tokens.length);
            if (!inGame) {
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
            } else {
                return switch (cmd) {
                    case "help" -> help();
                    case "redraw" -> redrawBoard(params);
                    case "leave" -> leaveGame(params);
                    case "move" -> makeMove(params);
                    case "resign" -> resignGame(params);
                    case "highlight" -> highlightMoves(params);
                    default -> help();
                };
            }
        } catch (DataAccessException | IOException ex) {
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
        } else if (inGame) {
            return SET_TEXT_COLOR_BLUE + """
                    redraw
                    leave
                    move <start> <end>
                    resign
                    highlight <position>
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
            username = result.username();

            return "Registered and logged in as " + username;
        }
        throw new DataAccessException("Expected: register <username> <password> <email>");
    }

    public String login(String[] params) throws DataAccessException {
        if (params.length == 2) {
            var result = server.login(params[0], params[1]);
            authToken = result.authToken();
            username = result.username();

            return "Logged in as " + username;
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
            games = server.listGames(authToken).games();
            gameName = params[0];

            return "Created game: " + gameName;
        }
        throw new DataAccessException("Expected: create <gameName>");
    }

    private List<ListGamesData> games = new ArrayList<>();

    public String listGames(String[] params) throws DataAccessException {
        if (params.length == 0) {
            ListGamesResult gamesList = server.listGames(authToken);
            games = gamesList.games();

            if (games.isEmpty()) {
                return "No games available. Create a game with '" + SET_TEXT_COLOR_BLUE + "create <gameName>" + RESET_TEXT_COLOR + "'";
            }

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

    public String joinGame(String[] params) throws DataAccessException {
        if (params.length == 2) {
            if (games.isEmpty()) {
                throw new DataAccessException("Error: No games listed. '" + SET_TEXT_COLOR_BLUE + "list" + SET_TEXT_COLOR_RED + "' first.");
            }

            int i;
            try {
                i = Integer.parseInt(params[0]);
            } catch (NumberFormatException e) {
                throw new DataAccessException("Error: Invalid game number");
            }

            if (i < 1 || i > games.size()) {
                throw new DataAccessException("Error: Invalid game number");
            }

            ListGamesData game = games.get(i - 1);

            currentGameID = game.gameID();
            String playerColor = params[1].toUpperCase();

            if (!playerColor.equals("WHITE") && !playerColor.equals("BLACK")) {
                throw new DataAccessException("Error: Invalid player color");
            }

            isWhitePlayer = playerColor.equals("WHITE");

            server.joinGame(authToken, playerColor, currentGameID);
            inGame = true;
            isObserver = false;

            ws = new WebSocketFacade("http://localhost:8080", this);

            UserGameCommand command = new UserGameCommand(
                    UserGameCommand.CommandType.CONNECT,
                    authToken,
                    currentGameID
            );
            ws.send(command);

            return "Joined Game " + i + " as " + playerColor.toLowerCase();
        }
        throw new DataAccessException("Expected: join <gameID> <WHITE|BLACK>");
    }

    public String observeGame(String[] params) throws DataAccessException {
        if (params.length == 1) {
            if (games.isEmpty()) {
                throw new DataAccessException("Error: No games listed. Use '" + SET_TEXT_COLOR_BLUE + "list" + SET_TEXT_COLOR_RED + "' first.");
            }

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
            currentGameID = gameID;
            inGame = true;
            isWhitePlayer = true;
            isObserver = true;
            ws = new WebSocketFacade("http://localhost:8080", this);

            UserGameCommand command = new UserGameCommand(
                    UserGameCommand.CommandType.CONNECT,
                    authToken,
                    currentGameID
            );
            ws.send(command);

            return "Observing game " + i;
        }
        throw new DataAccessException("Expected: observe <gameID>");
    }

    private String redrawBoard(String[] params) throws DataAccessException {
        if (params.length == 0) {
            Board.clearHighlightedSquares();
            Board.drawBoard(isWhitePlayer);
            return "Board redrawn";
        }
        throw new DataAccessException("Expected: redraw");
    }

    private String leaveGame(String[] params) throws DataAccessException, IOException {
        if (params.length == 0) {
            UserGameCommand command = new UserGameCommand(
                    UserGameCommand.CommandType.LEAVE,
                    authToken,
                    currentGameID
            );

            ws.send(command);
            inGame = false;
            currentGameID = 0;

            if (ws != null) {
                ws.close();
                ws = null;
            }

            return "You left the game";
        }
        throw new DataAccessException("Expected: leave");
    }

    private int colFromLetter(char c) throws DataAccessException {
        return switch (c) {
            case 'a' -> 1;
            case 'b' -> 2;
            case 'c' -> 3;
            case 'd' -> 4;
            case 'e' -> 5;
            case 'f' -> 6;
            case 'g' -> 7;
            case 'h' -> 8;
            default -> throw new DataAccessException("Invalid column");
        };
    }

    private String makeMove(String[] params) throws DataAccessException {
        if (params.length == 2) {
            if (isObserver) {
                throw new DataAccessException("Observers can not make moves");
            }

            String start = params[0];
            String end = params[1];

            int startCol = colFromLetter(start.charAt(0));
            int startRow = Character.getNumericValue(start.charAt(1));
            int endCol = colFromLetter(end.charAt(0));
            int endRow = Character.getNumericValue(end.charAt(1));

            chess.ChessPosition startPosition = new chess.ChessPosition(startRow, startCol);
            chess.ChessPosition endPosition = new chess.ChessPosition(endRow, endCol);
            chess.ChessMove move = new chess.ChessMove(startPosition, endPosition, null);
            Board.clearHighlightedSquares();

            UserGameCommand command = new UserGameCommand(
                    UserGameCommand.CommandType.MAKE_MOVE,
                    authToken,
                    currentGameID,
                    move
            );
            ws.send(command);
            return ("Moved from: " + start + " to " + end);
        }
        throw new DataAccessException("Expected: move <start> <end>");
    }

    private String resignGame(String[] params) throws DataAccessException {
        if (params.length == 0) {
            if (isObserver) {
                throw new DataAccessException("Observers can not resign");
            }

            System.out.println("Are you sure you want to resign? (yes/no): ");
            System.out.print(">>> ");
            Scanner scanner = new Scanner(System.in);
            String input = scanner.nextLine().trim().toLowerCase();

            if (!input.equals("yes")) {
                return "Resign cancelled";
            }

            UserGameCommand command = new UserGameCommand(
                    UserGameCommand.CommandType.RESIGN,
                    authToken,
                    currentGameID
            );

            ws.send(command);
            return "Resigned from Game " + currentGameID;
        }
        throw new DataAccessException("Expected: resign");
    }
    private String highlightMoves(String[] params) throws DataAccessException {
        if (params.length == 1) {

            String start = params[0];
            int col = colFromLetter(start.charAt(0));
            int row = Character.getNumericValue(start.charAt(1));
            ChessPosition position = new ChessPosition(row, col);

            ChessGame game = Board.getCurrentGame();

            if (game == null) {
                throw new DataAccessException("Game not loaded");
            }

            var moves = game.validMoves(position);

            Set<ChessPosition> highlightedMoves = new HashSet<>();
            for (chess.ChessMove move : moves) {
                highlightedMoves.add(move.getEndPosition());
            }

            Board.setSelectedSquare(position);
            Board.setHighlightedSquares(highlightedMoves);
            Board.drawBoard(isWhitePlayer);

            return "Highlighted moves for " + params[0];
        }
        throw new DataAccessException("Expected: highlight <position>");
    }

    public void onServerMessage(String message) {

        ServerMessage msg = gson.fromJson(message, ServerMessage.class);

        switch (msg.getServerMessageType()) {
            case LOAD_GAME -> handleLoadGame(msg);
            case NOTIFICATION -> handleNotification(msg);
            case ERROR -> handleError(msg);
        }
    }

    private void handleLoadGame(ServerMessage msg) {
        ChessGame game = (ChessGame) msg.getGame();

        if (game == null) {
            System.out.println(SET_TEXT_COLOR_RED + "Error: No game data received!" + RESET_TEXT_COLOR);
            return;
        }

        inGame = true;
        System.out.println();

        Board.updateFromChessGame(game);
        Board.drawBoard(isWhitePlayer);
        System.out.println("Board updated!");
        printPrompt();
    }

    private void handleNotification(ServerMessage msg) {
        System.out.println();
        Notification notification = new Notification(Notification.Type.GAME_UPDATE, msg.getMessage());
        notify(notification);
        printPrompt();
    }

    private void handleError(ServerMessage msg) {
        System.out.println();
        System.out.println(SET_TEXT_COLOR_RED + "Error: " + msg.getErrorMessage() + RESET_TEXT_COLOR);
        printPrompt();
    }
}
