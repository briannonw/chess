package model;

public record ListGamesData(
        int gameID,
        String whiteUsername,
        String blackUsername,
        String gameName
) {}
