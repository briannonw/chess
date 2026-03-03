package service;

import model.GameData;
import model.ListGamesData;

import java.util.List;

public record ListGamesResult(
        List<ListGamesData> games
) {}
