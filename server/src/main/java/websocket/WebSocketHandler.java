package websocket;

import chess.*;
import com.google.gson.Gson;
import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import io.javalin.websocket.WsContext;
import model.AuthData;
import model.GameData;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;
import websocket.messages.ServerMessage.ServerMessageType;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.*;

import io.javalin.websocket.WsConfig;

public class WebSocketHandler {

    private final Gson gson = new Gson();
    private final DataAccess dataAccess;
    private final Map<Integer, Set<io.javalin.websocket.WsContext>> gameConnections = new HashMap<>();
    private final Map<WsContext, String> roles = new HashMap<>();
    private final Set<Integer> finishedGames = new HashSet<>();

    public WebSocketHandler(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    public void register(WsConfig ws) {

        ws.onConnect(ctx -> {
            ctx.session.setIdleTimeout(java.time.Duration.ofMinutes(5));
        });

        ws.onClose(ctx -> {
            Integer gameID = ctx.attribute("gameID");
            roles.remove(ctx);
            if (gameID != null) {
                removeConnection(gameID, ctx);
            }
        });

        ws.onMessage(ctx -> {
            String message = ctx.message();

            try {
                UserGameCommand command = gson.fromJson(message, UserGameCommand.class);

                switch (command.getCommandType()) {

                    case CONNECT -> handleConnect(ctx, command);

                    case RESIGN -> handleResign(ctx, command);

                    case LEAVE -> handleLeave(ctx, command);

                    case MAKE_MOVE -> handleMove(ctx, command, message);

                    default -> System.out.println("Unknown command");
                }

            } catch (Exception e) {
                System.out.println("Error handling message: " + e.getMessage());
            }
        });
    }

    private void sendError(WsContext ctx, String msg) {
        ServerMessage error = new ServerMessage(ServerMessageType.ERROR);
        error.setErrorMessage(msg);
        send(ctx, error);
    }

    private void handleConnect(io.javalin.websocket.WsContext ctx, UserGameCommand command) throws DataAccessException {

        int gameID = command.getGameID();
        ctx.attribute("gameID", gameID);

        GameData gameData = dataAccess.getGame(gameID);
        if (gameData == null) {
            sendError(ctx, "Invalid game ID");
            return;
        }
        ChessGame game = gameData.game();

        AuthData auth = dataAccess.getAuth(command.getAuthToken());
        if (auth == null) {
            sendError(ctx, "Invalid auth token");
            return;
        }
        String username = auth.username();

        gameConnections.putIfAbsent(gameID, new HashSet<>());
        gameConnections.get(gameID).add(ctx);

        String role;
        ChessGame.TeamColor userColor = null;
        if (username.equals(gameData.whiteUsername())) {
            role = "PLAYER";
            userColor = ChessGame.TeamColor.WHITE;
        } else if (username.equals(gameData.blackUsername())) {
            role = "PLAYER";
            userColor = ChessGame.TeamColor.BLACK;
        } else {
            role = "OBSERVER";
        }
        roles.put(ctx, role);

        ServerMessage response = new ServerMessage(ServerMessageType.LOAD_GAME);
        response.setGame(game);
        send(ctx, response);

        if (gameConnections.get(gameID).size() > 1) {
            if (role.equals("PLAYER")) {
                broadcastNotification(gameID, ctx, username + " joined the game as " + userColor.toString().toLowerCase());
            } else {
                broadcastNotification(gameID, ctx,username + " observing the game");
            }
        }
    }

    private void handleResign(io.javalin.websocket.WsContext ctx, UserGameCommand command) throws DataAccessException {

        int gameID = command.getGameID();
        GameData gameData = dataAccess.getGame(gameID);
        if (gameData == null) {
            sendError(ctx, "Invalid game ID");
            return;
        }

        if (finishedGames.contains(gameID)) {
            sendError(ctx, "Game is over");
            return;
        }

        AuthData auth = dataAccess.getAuth(command.getAuthToken());
        if (auth == null) {
            sendError(ctx, "Invalid auth token");
            return;
        }
        String username = auth.username();

        String role = roles.get(ctx);
        if (!"PLAYER".equals(role)) {
            sendError(ctx, "Only players can resign");
            return;
        }

        finishedGames.add(gameID);

        ServerMessage msg = new ServerMessage(ServerMessageType.NOTIFICATION);
        msg.setMessage(username + " resigned from the game");
        send(ctx, msg);

        broadcastNotification(gameID, ctx, username + " resigned from the game");
    }

    private void handleLeave(io.javalin.websocket.WsContext ctx, UserGameCommand command) throws DataAccessException {

        int gameID = command.getGameID();
        GameData gameData = dataAccess.getGame(gameID);
        if (gameData == null) {
            sendError(ctx, "Invalid game ID");
            return;
        }

        AuthData auth = dataAccess.getAuth(command.getAuthToken());
        if (auth == null) {
            sendError(ctx, "Invalid auth token");
            return;
        }
        String username = auth.username();

        String role = roles.get(ctx);

        if ("PLAYER".equals(role)) {
            String whiteUser = gameData.whiteUsername();
            String blackUser = gameData.blackUsername();

            String newWhiteUser = whiteUser;
            String newBlackUser = blackUser;

            if (username.equals(whiteUser)) {
                newWhiteUser = null;
            } else if (username.equals(blackUser)) {
                newBlackUser = null;
            }

            GameData updated = new GameData(
                    gameData.gameID(),
                    newWhiteUser,
                    newBlackUser,
                    gameData.gameName(),
                    gameData.game()
            );

            dataAccess.updateGame(updated);
        }

        if ("PLAYER".equals(role)) {
            broadcastNotification(gameID, ctx, username + " left the game");
        } else {
            broadcastNotification(gameID, ctx, username + " stopped observing the game");
        }

        removeConnection(gameID, ctx);
        roles.remove(ctx);
    }

    private void handleMove(WsContext ctx, UserGameCommand command, String rawJson) throws DataAccessException {

        int gameID = command.getGameID();

        GameData gameData = dataAccess.getGame(gameID);
        if (gameData == null) {
            sendError(ctx, "Invalid game ID");
            return;
        }

        if (finishedGames.contains(gameID)) {
            sendError(ctx, "Game is over");
            return;
        }

        AuthData auth = dataAccess.getAuth(command.getAuthToken());
        if (auth == null) {
            sendError(ctx, "Invalid auth token");
            return;
        }
        String username = auth.username();

        String role = roles.get(ctx);
        if (!"PLAYER".equals(role)) {
            sendError(ctx, "Only players can make moves");
            return;
        }

        try {
            completeMove(ctx, rawJson, username, gameData, gameID);
        } catch (InvalidMoveException e) {
            sendError(ctx, "Invalid move");
        } catch (DataAccessException e) {
            sendError(ctx, "Server error");
        } catch (Exception e) {
            sendError(ctx, "Invalid request");
        }
    }

    private void completeMove(WsContext ctx, String rawJson, String username, GameData gameData, int gameID)
            throws DataAccessException, InvalidMoveException {
        JsonObject json = JsonParser.parseString(rawJson).getAsJsonObject();
        JsonObject moveObj = json.getAsJsonObject("move");
        JsonObject start = moveObj.getAsJsonObject("startPosition");
        JsonObject end = moveObj.getAsJsonObject("endPosition");

        int startRow = start.get("row").getAsInt();
        int startCol = start.get("col").getAsInt();
        int endRow = end.get("row").getAsInt();
        int endCol = end.get("col").getAsInt();

        ChessPosition startPos = new ChessPosition(startRow, startCol);
        ChessPosition endPos = new ChessPosition(endRow, endCol);
        ChessMove move = new ChessMove(startPos, endPos, null);
        ChessGame game = gameData.game();

        ChessGame.TeamColor opponent;
        if (game.getTeamTurn() == ChessGame.TeamColor.WHITE) {
            opponent = ChessGame.TeamColor.BLACK;
        } else {
            opponent = ChessGame.TeamColor.WHITE;
        }
        ChessGame.TeamColor userColor;
        if (username.equals(gameData.whiteUsername())) {
            userColor = ChessGame.TeamColor.WHITE;
        } else {
            userColor = ChessGame.TeamColor.BLACK;
        }

        ChessPiece piece = game.getBoard().getPiece(startPos);
        if (piece == null) {
            sendError(ctx, "No piece selected");
            return;
        } else if (piece.getTeamColor() != userColor) {
            sendError(ctx, "Cannot move opponent's piece");
            return;
        }

        if (game.getTeamTurn() != userColor) {
            sendError(ctx, "Not " + userColor.toString().toLowerCase() + "'s turn");
            return;
        }

        game.makeMove(move);
        GameData updated = new GameData(
                gameData.gameID(),
                gameData.whiteUsername(),
                gameData.blackUsername(),
                gameData.gameName(),
                game
        );
        dataAccess.updateGame(updated);

        ServerMessage response = new ServerMessage(ServerMessageType.LOAD_GAME);
        response.setGame(game);
        send(ctx, response);
        broadcast(gameID, ctx, response);

        String startString = toChessFormat(startRow, startCol);
        String endString = toChessFormat(endRow, endCol);
        broadcastNotification(gameID, ctx, username + " moved a piece from " + startString + " to " + endString);

        if (game.isInCheckmate(opponent) || game.isInStalemate(opponent)) {
            finishedGames.add(gameID);
            String msg;

            if (game.isInCheckmate(opponent)) {
                msg = "Checkmate! " + username + " won.";
            } else {
                msg = "Stalemate! Draw game.";
            }

            ServerMessage notification = new ServerMessage(ServerMessageType.NOTIFICATION);
            notification.setMessage(msg);
            send(ctx, notification);

            broadcastNotification(gameID, ctx, msg);
            return;
        }

        if (game.isInCheck(opponent)) {
            String opponentUsername;
            if (opponent == ChessGame.TeamColor.BLACK) {
                opponentUsername = gameData.blackUsername();
            } else {
                opponentUsername = gameData.whiteUsername();
            }
            String msg = opponentUsername + " is in check.";

            ServerMessage notification = new ServerMessage(ServerMessageType.NOTIFICATION);
            notification.setMessage(msg);
            send(ctx, notification);

            broadcastNotification(gameID, ctx, msg);
        }
    }

    private String toChessFormat(int row, int col) throws DataAccessException {
        String letters =  switch (col) {
            case 1 -> "a";
            case 2 -> "b";
            case 3 -> "c";
            case 4 -> "d";
            case 5 -> "e";
            case 6 -> "f";
            case 7 -> "g";
            case 8 -> "h";
            default -> throw new DataAccessException("Invalid column");
        };
        return letters + row;
    }

    private void send(io.javalin.websocket.WsContext ctx, ServerMessage message) {
        String json = gson.toJson(message);
        ctx.send(json);
    }

    private void broadcast(int gameID, WsContext exclude, ServerMessage message) {
        String json = gson.toJson(message);

        if (gameConnections.containsKey(gameID)) {
            Iterator<WsContext> iterator = gameConnections.get(gameID).iterator();

            while (iterator.hasNext()) {
                WsContext client = iterator.next();

                if (client.equals(exclude)) {
                    continue;
                }
                try {
                    client.send(json);
                } catch (Exception e) {
                    iterator.remove();
                }
            }
        }
    }

    private void broadcastNotification(int gameID, WsContext exclude, String text) {
        ServerMessage msg = new ServerMessage(ServerMessageType.NOTIFICATION);
        msg.setMessage(text);
        broadcast(gameID, exclude, msg);
    }

    private void removeConnection(int gameID, WsContext ctx) {
        Set<WsContext> set = gameConnections.get(gameID);
        if (set != null) {
            set.remove(ctx);
            if (set.isEmpty()) {
                gameConnections.remove(gameID);
            }
        }
    }
}