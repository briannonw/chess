package server;

import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import dataaccess.MemoryDataAccess;
import io.javalin.*;
import io.javalin.http.Context;
import service.*;

import java.util.Map;

public class Server {

    private final Javalin javalin;
    private final ClearService cService = new ClearService(new MemoryDataAccess());

    public Server() {
        javalin = Javalin.create(config -> config.staticFiles.add("web"));

        javalin.post("/user", this::register);
        javalin.post("/session", this::login);
        javalin.delete("/session", this::logout);
        javalin.get("/game", this::listGames);
        javalin.post("/game", this::createGame);
        javalin.put("/game", this::joinGame);
        javalin.delete("/db", this::clear);
        // Register your endpoints and exception handlers here.

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
                message = "Error: " + message;
                break;
        }

        ctx.json(Map.of("message", message));
    }

    private void register(Context ctx) throws DataAccessException {
        // register
    }

    private void login(Context ctx) throws DataAccessException {
        // login
    }
    private void logout(Context ctx) throws DataAccessException {
        // logout
    }
    private void listGames(Context ctx) throws DataAccessException {
        // listGames
    }
    private void createGame(Context ctx) throws DataAccessException {
        // createGame
    }
    private void joinGame(Context ctx) throws DataAccessException {
        // joinGame
    }
    private void clear(Context ctx) throws DataAccessException {
        cService.clear();
        ctx.status(200);
        ctx.json(Map.of());
    }
}
