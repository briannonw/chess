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

import service.*;


public class ServerFacade {
    private final HttpClient client = HttpClient.newHttpClient();
    private final String serverUrl;

    public ServerFacade(int port) {
        this.serverUrl = "http://localhost:" + port;
    }

    public RegisterResult register(String username, String password, String email) throws DataAccessException {
        var body = new RegisterRequest(username, password, email);

        var registerRequest = buildRequest("POST", "/user", body, null);
        var registerResponse = sendRequest(registerRequest);

        return handleResponse(registerResponse, RegisterResult.class);
    }

    public LoginResult login(String username, String password) throws  DataAccessException {
        var body = new LoginRequest(username, password);

        var loginRequest = buildRequest("POST", "/session", body, null);
        var loginResponse = sendRequest(loginRequest);

        return handleResponse(loginResponse, LoginResult.class);
    }

    public void logout(String authToken) throws DataAccessException {
        var logoutRequest = buildRequest("DELETE", "/session", null, authToken);
        var logoutResponse = sendRequest(logoutRequest);
        handleResponse(logoutResponse, null);
    }

    public ListGamesResult listGames(String authToken) throws DataAccessException {
        var listGamesRequest = buildRequest("GET", "/game", null, authToken);
        var listGamesResponse = sendRequest(listGamesRequest);

        return handleResponse(listGamesResponse, ListGamesResult.class);
    }

    public CreateGameResult createGame(String authToken, String gameName) throws DataAccessException {
        var body = new CreateGameRequest(authToken, gameName);

        var createGameRequest = buildRequest("POST", "/game", body, authToken);
        var createGameResponse = sendRequest(createGameRequest);

        return handleResponse(createGameResponse, CreateGameResult.class);
    }

    public void joinGame(String authToken, String playerColor, int gameID) throws DataAccessException {
        var body = new JoinGameRequest(authToken, playerColor, gameID);

        var joinGameRequest = buildRequest("PUT", "/game", body, authToken);
        var joinGameResponse = sendRequest(joinGameRequest);
        handleResponse(joinGameResponse, null);
    }

    public void clear() throws DataAccessException {
        var clearRequest = buildRequest("DELETE", "/db", null, null);
        var clearResponse = sendRequest(clearRequest);
        handleResponse(clearResponse, null);
    }

    private HttpRequest buildRequest(String method, String path, Object body, String authToken) {
        var request = HttpRequest.newBuilder()
                .uri(URI.create(serverUrl + path))
                .method(method, makeRequestBody(body));
        if (body != null) {
            request.setHeader("Content-Type", "application/json");
        }
        if (authToken != null) {
            request.setHeader("authorization", authToken);
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

    private static class ErrorResponse {
        String message;
    }

    private <T> T handleResponse(HttpResponse<String> response, Class<T> responseClass) throws DataAccessException {
        var status = response.statusCode();

        if (!isSuccessful(status)) {
            var error = new Gson().fromJson(response.body(), ErrorResponse.class);
            throw new DataAccessException(error.message);
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
