package me.offlical.json;

import me.offlical.connect4.Connect4Game;

public class Connect4JSON {

    private Connect4Game game;
    private String path;

    public Connect4JSON(Connect4Game game, String path) {
        this.game = game;
        this.path = path;
    }
}
