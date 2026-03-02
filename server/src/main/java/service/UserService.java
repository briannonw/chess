package service;

import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import model.*;

public class UserService {
    private final DataAccess dataAccess;

    public UserService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    public RegisterResult register(RegisterRequest registerRequest) throws DataAccessException {
        UserData user = new UserData(registerRequest.username(), registerRequest.password(), registerRequest.email());
        dataAccess.createUser(user);

    }

    public LoginResult login(LoginRequest loginRequest) {

    }

    public void logout(LogoutRequest logoutRequest) {

    }
}
