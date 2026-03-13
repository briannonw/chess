package service;

import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import model.*;

import java.util.UUID;

public class UserService {
    private final DataAccess dataAccess;

    public UserService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    public RegisterResult register(RegisterRequest registerRequest) throws DataAccessException {
        if (registerRequest.username() == null || registerRequest.password() == null || registerRequest.email() == null ||
                registerRequest.username().isBlank() || registerRequest.password().isBlank() || registerRequest.email().isBlank()) {
            throw new DataAccessException("Bad request");
        }
        UserData user = new UserData(registerRequest.username(), registerRequest.password(), registerRequest.email());
        dataAccess.createUser(user);
        String authToken = UUID.randomUUID().toString();
        dataAccess.createAuth(new AuthData(authToken, user.username()));
        return new RegisterResult(user.username(), authToken);
    }

    public LoginResult login(LoginRequest loginRequest) throws DataAccessException {
        if (loginRequest.username() == null || loginRequest.username().isBlank() ||
                loginRequest.password() == null || loginRequest.password().isBlank()) {
            throw new DataAccessException("Bad request");
        }

        UserData user = dataAccess.getUser(loginRequest.username());
        if (user == null || !dataAccess.verifyUser(loginRequest.username(), loginRequest.password())) {
            throw new DataAccessException("Unauthorized");
        }
        String authToken = UUID.randomUUID().toString();
        dataAccess.createAuth(new AuthData(authToken, loginRequest.username()));
        return new LoginResult(loginRequest.username(), authToken);
    }

    public void logout(LogoutRequest logoutRequest) throws DataAccessException {
        AuthData auth = dataAccess.getAuth(logoutRequest.authToken());
        if (auth == null) {
            throw new DataAccessException("Unauthorized");
        }
        dataAccess.deleteAuth(logoutRequest.authToken());
    }
}
