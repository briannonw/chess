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
        UserData user = new UserData(registerRequest.username(), registerRequest.password(), registerRequest.email());
        dataAccess.createUser(user);
        String authToken = UUID.randomUUID().toString();
        dataAccess.createAuth(new AuthData(authToken, user.username()));
        return new RegisterResult(user.username(), authToken);
    }

    public LoginResult login(LoginRequest loginRequest) throws DataAccessException {
        UserData user = dataAccess.getUser(loginRequest.username());
        if (user == null) {
            throw new DataAccessException("User doesn't exist");
        }
        if (!user.password().equals(loginRequest.password())) {
            throw new DataAccessException("Passwords don't match");
        }
        String authToken = UUID.randomUUID().toString();
        dataAccess.createAuth(new AuthData(authToken, user.username()));
        return new LoginResult(user.username(), authToken);
    }

    public void logout(LogoutRequest logoutRequest) throws DataAccessException {
        AuthData auth = dataAccess.getAuth(logoutRequest.authToken());
        if (auth == null) {
            throw new DataAccessException("Unauthorized");
        }
        dataAccess.deleteAuth(logoutRequest.authToken());
    }
}
