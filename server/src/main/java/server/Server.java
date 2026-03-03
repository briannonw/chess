package server;

import com.google.gson.Gson;

import dataaccess.DataAccessException;
import dataaccess.MemoryDataAccess;
import io.javalin.*;
import io.javalin.http.Context;
import service.*;
import model.*;

import java.util.Map;

public class Server {

    private final Javalin javalin;
    private final MemoryDataAccess dataAccess = new MemoryDataAccess();
    private final ClearService clearService = new ClearService(dataAccess);
    private final UserService userService = new UserService(dataAccess);
    private final GameService gameService = new GameService(dataAccess);

    public Server() {
        javalin = Javalin.create(config -> config.staticFiles.add("web"));

        javalin.post("/user", this::register);
        javalin.post("/session", this::login);
        javalin.delete("/session", this::logout);
        javalin.get("/game", this::listGames);
        javalin.post("/game", this::createGame);
        javalin.put("/game", this::joinGame);
        javalin.delete("/db", this::clear);

        javalin.exception(DataAccessException.class, this::exceptionHandler);
    }

    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }

    private void exceptionHandler(DataAccessException ex, Context ctx) {
        String message = ex.getMessage();

        switch (message) {
            case "Bad request":
                ctx.status(400);
                break;
            case "Unauthorized":
                ctx.status(401);
                break;
            case "Already taken":
                ctx.status(403);
                break;
            default:
                ctx.status(500);
                break;
        }

        ctx.json(Map.of("message","Error: " + message));
    }

    private void register(Context ctx) throws DataAccessException {
        RegisterRequest request = new Gson().fromJson(ctx.body(), RegisterRequest.class);
        RegisterResult result = userService.register(request);
        ctx.status(200);
        ctx.json(result);
    }

    private void login(Context ctx) throws DataAccessException {
        LoginRequest request = new Gson().fromJson(ctx.body(), LoginRequest.class);
        LoginResult result = userService.login(request);
        ctx.status(200);
        ctx.json(result);
    }

    private void logout(Context ctx) throws DataAccessException {
        String authToken = ctx.header("authorization");
        LogoutRequest request = new LogoutRequest(authToken);
        userService.logout(request);
        ctx.status(200);
        ctx.json(Map.of());
    }
    private void listGames(Context ctx) throws DataAccessException {
        String authToken = ctx.header("authorization");
        ListGamesRequest request = new ListGamesRequest(authToken);
        ListGamesResult result = gameService.listGames(request);
        ctx.status(200);
        ctx.json(result);
    }

    private void createGame(Context ctx) throws DataAccessException {
        String authToken = ctx.header("authorization");
        CreateGameRequest body = new Gson().fromJson(ctx.body(), CreateGameRequest.class);
        CreateGameRequest request = new CreateGameRequest(authToken, body.gameName());
        CreateGameResult result = gameService.createGame(request);
        ctx.status(200);
        ctx.json(result);
    }

    private void joinGame(Context ctx) throws DataAccessException {
        String authToken = ctx.header("authorization");
        JoinGameRequest body = new Gson().fromJson(ctx.body(), JoinGameRequest.class);
        JoinGameRequest request = new JoinGameRequest(authToken, body.playerColor(), body.gameID());
        gameService.joinGame(request);
        ctx.status(200);
        ctx.json(Map.of());
    }

    private void clear(Context ctx) throws DataAccessException {
        clearService.clear();
        ctx.status(200);
        ctx.json(Map.of());
    }
}
