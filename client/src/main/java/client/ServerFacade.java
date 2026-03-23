package client;

import com.google.gson.Gson;
import dataaccess.DataAccessException;

import java.net.http.HttpClient;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;

import model.AuthData;
import model.GameData;
import service.CreateGameRequest;
import service.LoginRequest;
import service.RegisterRequest;


public class ServerFacade {
    private final HttpClient client = HttpClient.newHttpClient();
    private final String serverUrl;

    public ServerFacade(int port) {
        this.serverUrl = "http://localhost:" + port;
    }

    public AuthData register(String username, String password, String email) throws DataAccessException {
        var body = new RegisterRequest(username, password, email);

        var registerRequest = buildRequest("POST", "/user", body);
        var registerResponse = sendRequest(registerRequest);

        return handleResponse(registerResponse, AuthData.class);
    }

    public AuthData login(String username, String password) throws  DataAccessException {
        var body = new LoginRequest(username, password);

        var loginRequest = buildRequest("POST", "/session", body);
        var loginResponse = sendRequest(loginRequest);

        return handleResponse(loginResponse, AuthData.class);
    }

    public AuthData logout(String authToken) throws DataAccessException {
        var logoutRequest = buildRequest("DELETE", "/session", authToken);
        var logoutResponse = sendRequest(logoutRequest);

        return handleResponse(logoutResponse, AuthData.class);
    }

    public GameData listGame(String authToken) throws DataAccessException {
        var listGamesRequest = buildRequest("GET", "/game", authToken);
        var listGamesResponse = sendRequest(listGamesRequest);

        return handleResponse(listGamesResponse, GameData.class);
    }

    public GameData createGame(String authToken, String gameName) throws DataAccessException {
        var body = new CreateGameRequest(authToken, gameName);

        var createGameRequest = buildRequest("POST", "/game", body);
        var createGameResponse = sendRequest(createGameRequest);

        return handleResponse(createGameResponse, GameData.class);
    }

    // register(username, password, email)
    // login(username, password)
    // logout(authToken)
    // createGame(authToken, gameName)
    // listGames(authToken)
    // joinGame(authToken, gameID, color)
    // clear

    private HttpRequest buildRequest(String method, String path, Object body) {
        var request = HttpRequest.newBuilder()
                .uri(URI.create(serverUrl + path))
                .method(method, makeRequestBody(body));
        if (body != null) {
            request.setHeader("Content-Type", "application/json");
        }
        return request.build();
    }

    private BodyPublisher makeRequestBody(Object request) {
        if (request != null) {
            return BodyPublishers.ofString(new Gson().toJson(request));
        } else {
            return BodyPublishers.noBody();
        }
    }

    private HttpResponse<String> sendRequest(HttpRequest request) throws DataAccessException {
        try {
            return client.send(request, BodyHandlers.ofString());
        } catch (Exception ex) {
            throw new DataAccessException("Error: " + ex.getMessage(), ex);
        }
    }

    private <T> T handleResponse(HttpResponse<String> response, Class<T> responseClass) throws DataAccessException {
        var status = response.statusCode();

        if (!isSuccessful(status)) {
            String message = response.body();
            throw new DataAccessException("Error: " + message);
        }

        if (responseClass != null) {
            return new Gson().fromJson(response.body(), responseClass);
        }

        return null;
    }

    private boolean isSuccessful(int status) {
        return status / 100 == 2;
    }
}
