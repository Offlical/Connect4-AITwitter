package com.offlical.net.connect4;

public class Connect4Player {

    private String gameName;
    private String symbol;

    public Connect4Player(String symbol, String gameName) {
        this.symbol = symbol;
        this.gameName = gameName;
    }

    public Connect4Player() {

    }

    public String getGameName() {
        return gameName;
    }

    public String getSymbol() {
        return symbol;
    }
}
