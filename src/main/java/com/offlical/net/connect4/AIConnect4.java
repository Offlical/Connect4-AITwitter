package com.offlical.net.connect4;

import java.util.Random;

public class AIConnect4 {

    public String[][] grid;
    private Connect4Player bluePlayer;
    private Connect4Player redPlayer;

    public final static String RED_PLAYER_EMOJI = "\u1F7E5";
    public final static String BLUE_PLAYER_EMOJI = "\u1F7E6";
    public final static String EMPTY_SLOT = "\u2B1B";



    public boolean gameOver = false;

    public int turn = 1;

    public Connect4Player currentPlayer;
    public String currentColor;
    public String currentEmoji;

    public AIConnect4(Connect4Player bluePlayer, Connect4Player redPlayer) {

        this.redPlayer = redPlayer;
        this.bluePlayer = bluePlayer;

        grid = new String[6][7];
        for (int row = 0; row < grid.length; row++) {
            for (int col = 0; col < grid[0].length; col++) {
                grid[row][col] = EMPTY_SLOT;
            }
        }

        int firstTurn = new Random().nextInt(2);

        // which person gets to do their first turn
        currentPlayer = (firstTurn == 0) ? this.redPlayer : this.bluePlayer;
        currentEmoji = currentPlayer.getSymbol();
        currentColor = (currentPlayer.equals(this.bluePlayer)) ? "Blue" : "RED";
    }


    public void display(String[][] grid){
        System.out.println(currentPlayer.getGameName() + "'s Turn");
        System.out.println(" 0 1 2 3 4 5 6");
        System.out.println("---------------");
        for (int row = 0; row < grid.length; row++){
            System.out.print("|");
            for (int col = 0; col < grid[0].length; col++){
                System.out.print(grid[row][col]);
                System.out.print("|");
            }
            System.out.println();
            System.out.println("---------------");
        }
        System.out.println(" 0 1 2 3 4 5 6");
        System.out.println();
    }

    public void play(int input) {


        if(turn <= 42 && !gameOver) {

            if(!validate(input,grid))
            {
                return;
            }

            for (int row = grid.length-1; row >= 0; row--){
                if(grid[row][input].equalsIgnoreCase(EMPTY_SLOT)){
                    grid[row][input] = currentEmoji;
                    break;
                }
            }

            turn++;
            if(isWinner(currentEmoji,grid)) {

                this.reset();
                display(grid);
                System.out.println(currentPlayer.getGameName() + " won!");
                return;
            }
            if(turn >= 42) {
                tie();
            }
        }else {
            tie();
        }

    }



    public boolean validate(int column, String[][] grid){
        //valid column?
        if (column < 0 || column > grid[0].length){
            return false;
        }

        //full column?
        return grid[0][column].equals(EMPTY_SLOT);
    }


    /**
     *
     * @param player - Player's emoji in the grid
     * @param grid - the game grid
     * @return - If the player won or not
     */
    public boolean isWinner(String player, String[][] grid){
        //check for 4 across
        for(int row = 0; row<grid.length; row++){
            for (int col = 0;col < grid[0].length - 3;col++){
                if (grid[row][col] == player   &&
                        grid[row][col+1] == player &&
                        grid[row][col+2] == player &&
                        grid[row][col+3] == player){
                    return true;
                }
            }
        }
        //check for 4 up and down
        for(int row = 0; row < grid.length - 3; row++){
            for(int col = 0; col < grid[0].length; col++){
                if (grid[row][col] == player   &&
                        grid[row+1][col] == player &&
                        grid[row+2][col] == player &&
                        grid[row+3][col] == player){
                    return true;
                }
            }
        }
        //check upward diagonal
        for(int row = 3; row < grid.length; row++){
            for(int col = 0; col < grid[0].length - 3; col++){
                if (grid[row][col] == player   &&
                        grid[row-1][col+1] == player &&
                        grid[row-2][col+2] == player &&
                        grid[row-3][col+3] == player){
                    return true;
                }
            }
        }
        //check downward diagonal
        for(int row = 0; row < grid.length - 3; row++){
            for(int col = 0; col < grid[0].length - 3; col++){
                if (grid[row][col] == player   &&
                        grid[row+1][col+1] == player &&
                        grid[row+2][col+2] == player &&
                        grid[row+3][col+3] == player){
                    return true;
                }
            }
        }
        return false;
    }


    private void tie() {
        this.reset();
        display(grid);
        System.out.println("Game ended!");
    }

    private void reset() {
        gameOver = true;
    }

    public void nextTurn() {

        currentPlayer = (currentPlayer.equals(redPlayer)) ? bluePlayer : redPlayer;
        currentEmoji = currentPlayer.getSymbol();
        currentColor = (currentPlayer.equals(this.bluePlayer)) ? "Blue" : "RED";

    }

}
