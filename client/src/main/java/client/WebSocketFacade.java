package client;

import com.google.gson.Gson;
import dataaccess.DataAccessException;
import jakarta.websocket.*;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class WebSocketFacade extends Endpoint {

    Session session;
    NotificationHandler notificationHandler;
    private final Gson gson = new Gson();

    public WebSocketFacade(String url, NotificationHandler notificationHandler) throws DataAccessException {
        try {
            url = url.replace("http", "ws");
            URI socketURI = new URI(url + "/ws");
            this.notificationHandler = notificationHandler;

            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            this.session = container.connectToServer(this, socketURI);

        } catch (DeploymentException | IOException | URISyntaxException ex) {
            throw new DataAccessException(ex.getMessage(), ex);
        }
    }

    @Override
    public void onOpen(Session session, EndpointConfig config) {
        System.out.println("Connected to server!");
        this.session = session;

        session.addMessageHandler(String.class, message -> {
            System.out.println("RAW MESSAGE FROM SERVER: " + message); // 🔥 add this
            if (notificationHandler instanceof ChessClient chessClient) {
                chessClient.onServerMessage(message);
            }
        });
    }

    public void send(Object message) throws DataAccessException {
        if (session == null || !session.isOpen()) {
            throw new DataAccessException("WebSocket connection is not open");
        }

        try {
            String json = gson.toJson(message);
            System.out.println("Sending WS message: " + json);
            session.getBasicRemote().sendText(json);
        } catch (IOException ex) {
            throw new DataAccessException("Failed to send notification");
        }
    }

    @Override
    public void onClose(Session session, CloseReason closeReason) {
        this.session = null;
        System.out.println("WebSocket closed: " + closeReason);
    }
}