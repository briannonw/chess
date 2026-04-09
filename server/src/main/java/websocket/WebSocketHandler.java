package websocket;

import chess.InvalidMoveException;
import com.google.gson.Gson;
import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import dataaccess.MySqlDataAccess;
import io.javalin.websocket.WsContext;
import model.AuthData;
import model.GameData;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;
import websocket.messages.ServerMessage.ServerMessageType;
import chess.ChessGame;
import java.util.*;

import io.javalin.websocket.WsConfig;

public class WebSocketHandler {

    private final Gson gson = new Gson();
    private final DataAccess dataAccess;
    private final Map<Integer, Set<io.javalin.websocket.WsContext>> gameConnections = new HashMap<>();

    public WebSocketHandler(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    public void register(WsConfig ws) {

        ws.onConnect(ctx -> {
            ctx.session.setIdleTimeout(java.time.Duration.ofMinutes(5));
            System.out.println("Client connected");
        });

        ws.onClose(ctx -> {
            System.out.println("Client disconnected");
            Integer gameID = ctx.attribute("gameID");
            if (gameID != null) {
                removeConnection(gameID, ctx);
            }
        });

        ws.onMessage(ctx -> {
            String message = ctx.message();
            System.out.println("Received: " + message);

            try {
                UserGameCommand command = gson.fromJson(message, UserGameCommand.class);

                switch (command.getCommandType()) {

                    case CONNECT -> handleConnect(ctx, command);

                    case RESIGN -> handleResign(ctx, command);

                    case LEAVE -> handleLeave(ctx, command);

                    case MAKE_MOVE -> handleMove(ctx, command);

                    default -> System.out.println("Unknown command");
                }

            } catch (Exception e) {
                System.out.println("Error handling message: " + e.getMessage());
            }
        });
    }

    private void handleConnect(io.javalin.websocket.WsContext ctx, UserGameCommand command) throws DataAccessException {
        int gameID = command.getGameID();
        ctx.attribute("gameID", gameID);

        gameConnections.putIfAbsent(gameID, new HashSet<>());
        gameConnections.get(gameID).add(ctx);

        GameData gameData = dataAccess.getGame(gameID);
        ChessGame game = gameData.game();

        AuthData auth = dataAccess.getAuth(command.getAuthToken());
        String username = auth.username();

        ServerMessage response = new ServerMessage(ServerMessageType.LOAD_GAME);
        response.setGame(game);
        response.setMessage("Game loaded");

        send(ctx, response);

        broadcastNotification(gameID, username + " joined the game");
    }

    private void handleResign(io.javalin.websocket.WsContext ctx, UserGameCommand command) throws DataAccessException {

        System.out.println("Handling RESIGN");

        int gameID = command.getGameID();
        GameData gameData = dataAccess.getGame(gameID);
        ChessGame game = gameData.game();

        AuthData auth = dataAccess.getAuth(command.getAuthToken());
        String username = auth.username();

        ServerMessage response = new ServerMessage(ServerMessageType.LOAD_GAME);
        response.setGame(game);
        response.setMessage("Player resigned");
        send(ctx, response);

        broadcastNotification(gameID, username + " resigned from the game");
    }

    private void handleLeave(io.javalin.websocket.WsContext ctx, UserGameCommand command) throws DataAccessException {

        System.out.println("Handling LEAVE");

        int gameID = command.getGameID();
        GameData gameData = dataAccess.getGame(gameID);
        ChessGame game = gameData.game();

        AuthData auth = dataAccess.getAuth(command.getAuthToken());
        String username = auth.username();

        ServerMessage response = new ServerMessage(ServerMessageType.LOAD_GAME);
        response.setGame(game);
        response.setMessage("Player left the game");
        send(ctx, response);
        removeConnection(gameID, ctx);
        ctx.closeSession();

        broadcastNotification(gameID, username + " left the game");
    }

    private void handleMove(io.javalin.websocket.WsContext ctx, UserGameCommand command) throws DataAccessException, InvalidMoveException {

        System.out.println("Handling MOVE");

        int gameID = command.getGameID();
        GameData gameData = dataAccess.getGame(gameID);
        ChessGame game = gameData.game();

        try {
            game.makeMove(command.getMove());
            GameData updatedGame = new GameData(
                    gameData.gameID(),
                    gameData.whiteUsername(),
                    gameData.blackUsername(),
                    gameData.gameName(),
                    game
            );
            dataAccess.updateGame(updatedGame);

            ServerMessage response = new ServerMessage(ServerMessageType.LOAD_GAME);
            response.setGame(game);
            response.setMessage("Move received");
            broadcast(gameID, response);
        } catch (InvalidMoveException ex) {
            ServerMessage error = new ServerMessage(ServerMessageType.ERROR);
            error.setMessage("Error: Invalid move");
            send(ctx, error);

        } catch (DataAccessException ex) {
            ServerMessage error = new ServerMessage(ServerMessageType.ERROR);
            error.setMessage("Invalid move");
            send(ctx, error);
        }
    }

    private void send(io.javalin.websocket.WsContext ctx, ServerMessage message) {
        String json = gson.toJson(message);
        System.out.println("Sending: " + json);
        ctx.send(json);
    }

    private void broadcast(int gameID, ServerMessage message) {
        String json = gson.toJson(message);
        System.out.println("Broadcasting: " + json);

        if (gameConnections.containsKey(gameID)) {
            Iterator<WsContext> iterator = gameConnections.get(gameID).iterator();

            while (iterator.hasNext()) {
                WsContext client = iterator.next();
                try {
                    client.send(json);
                } catch (Exception e) {
                    iterator.remove();
                }
            }
        }
    }

    private void broadcastNotification(int gameID, String text) {
        ServerMessage msg = new ServerMessage(ServerMessageType.NOTIFICATION);
        msg.setMessage(text);
        broadcast(gameID, msg);
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