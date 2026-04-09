package websocket;

import com.google.gson.Gson;
import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import dataaccess.MySqlDataAccess;
import model.GameData;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;
import websocket.messages.ServerMessage.ServerMessageType;
import chess.ChessGame;

import io.javalin.websocket.WsConfig;

public class WebSocketHandler {

    private final Gson gson = new Gson();
    private final DataAccess dataAccess;

    public WebSocketHandler(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    public void register(WsConfig ws) {

        ws.onConnect(ctx -> {
            System.out.println("Client connected");
        });

        ws.onClose(ctx -> {
            System.out.println("Client disconnected");
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

        System.out.println("Handling CONNECT");

        int gameID = command.getGameID();
        GameData gameData = dataAccess.getGame(gameID);
        ChessGame game = gameData.game();

        ServerMessage response = new ServerMessage(ServerMessageType.LOAD_GAME);
        response.setGame(game);
        response.setMessage("Game loaded");

        send(ctx, response);
    }

    private void handleResign(io.javalin.websocket.WsContext ctx, UserGameCommand command) {

        System.out.println("Handling RESIGN");

        ServerMessage response = new ServerMessage(ServerMessageType.NOTIFICATION);
        response.setMessage("Player resigned");

        send(ctx, response);
    }

    private void handleLeave(io.javalin.websocket.WsContext ctx, UserGameCommand command) {

        System.out.println("Handling LEAVE");

        ServerMessage response = new ServerMessage(ServerMessageType.NOTIFICATION);
        response.setMessage("Player left the game");

        send(ctx, response);
    }

    private void handleMove(io.javalin.websocket.WsContext ctx, UserGameCommand command) throws DataAccessException {

        System.out.println("Handling MOVE");

        int gameID = command.getGameID();
        GameData gameData = dataAccess.getGame(gameID);
        ChessGame game = gameData.game();

        ServerMessage response = new ServerMessage(ServerMessageType.LOAD_GAME);
        response.setGame(game);
        response.setMessage("Move received");
        send(ctx, response);
    }

    private void send(io.javalin.websocket.WsContext ctx, ServerMessage message) {
        String json = gson.toJson(message);
        System.out.println("Sending: " + json);
        ctx.send(json);
    }
}