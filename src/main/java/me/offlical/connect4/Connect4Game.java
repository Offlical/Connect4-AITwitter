package me.offlical.connect4;

import me.offlical.ai.AIPlayer;
import me.offlical.twitter.TwitterManager;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.Arrays;
import java.util.Random;

public class Connect4Game {

    public String[][] grid;
    private final AIPlayer bluePlayer;
    private final AIPlayer redPlayer;

    public final static String BLUE_PLAYER_EMOJI = "\uD83D\uDD34";
    public final static String RED_PLAYER_EMOJI = "\uD83D\uDD35";
    public final static String EMPTY_SLOT = "\u2B1B";
    public final static String WIN_MOVE = "\uD83D\uDFE1";

    private boolean gameOver = false;
    private boolean isTie = false;

    public int turn = 1;

    public AIPlayer currentPlayer;
    public String currentColor;
    public String currentEmoji;

    public Connect4Game(AIPlayer bluePlayer, AIPlayer redPlayer) {

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
        currentColor = (currentPlayer.equals(this.bluePlayer)) ? "Blue" : "Red";
    }



    /**
     *  Prints to console a debug message of the current state of the game
     */
    public void debugDisplay() {
        System.out.println(currentColor + "'s Turn");
        System.out.println(" 0 1 2 3 4 5 6");
        System.out.println("---------------");
        for (int row = 0; row < grid.length; row++) {
            System.out.print("|");
            for (int col = 0; col < grid[0].length; col++) {
                System.out.print(grid[row][col]);
                System.out.print("|");
            }
            System.out.println();
            System.out.println("---------------");
        }
        System.out.println(" 0 1 2 3 4 5 6");
        System.out.println();
    }

    public String gridToString(String[][] grid) {

        StringBuilder builder = new StringBuilder();

        for (String str : TwitterManager.getNumberEmojis())
            builder.append(str);
        builder.append("\n");

        for (String[] rows : grid) {
            for (int col = 0; col < grid[0].length; col++) {
                builder.append(rows[col]);
            }
            builder.append("\n");
        }

        return builder.toString();
    }

    public void fromJSON(JSONObject object) {

        turn = Integer.parseInt(String.valueOf(object.get("turn")));
        String color = String.valueOf(object.get("currentPlayer"));
        gridFromJSON(String.valueOf(object.get("grid")));

        if (!color.equals(currentColor)) {
            this.nextTurn();
        }
    }

    public String gridToJSONString() {

        StringBuilder builder = new StringBuilder();

        JSONArray rows = new JSONArray();
        for (String[] row : grid) {

            JSONArray col = new JSONArray();
            col.addAll(Arrays.asList(row));

            rows.add(col);
        }


        return rows.toJSONString();
    }

    public void gridFromJSON(String json) {
        JSONParser parser = new JSONParser();
        try {
            JSONArray array = (JSONArray) parser.parse(json);

            for (int row = 0; row < array.size(); row++) {

                JSONArray columnArr = (JSONArray) array.get(row);
                for (int col = 0; col < columnArr.size(); col++) {
                    grid[row][col] = (String) columnArr.get(col);
                }

            }

        } catch (ParseException e) {
            e.printStackTrace();
        }
        debugDisplay();
    }

    public void play(int input) {


        if (turn <= 42 && !gameOver) {

            if (isWinner(currentEmoji, grid)) {

                System.out.println("Win! " + currentColor);
                this.reset();
                return;
            }

            if (input == -1) {
                turn++;
                this.nextTurn();
                return;
            }

            if (!validate(input, grid)) {
                System.out.println("Invalid Move? How did this get here? Playing random move");
                play(this.currentPlayer.getFreeSpots(grid)[0]);
                return;
            }

            for (int row = grid.length - 1; row >= 0; row--) {
                if (grid[row][input].equalsIgnoreCase(EMPTY_SLOT)) {
                    grid[row][input] = currentEmoji;
                    break;
                }
            }

            if (isWinner(currentEmoji, grid)) {

                System.out.println("Win! " + currentColor);
                this.reset();
                return;
            }

            if (turn >= 42) {
                tie();
            }
            turn++;
            this.nextTurn();
        } else {
            tie();
        }

    }


    public boolean validate(int column, String[][] grid) {
        //valid column?
        if (column < 0 || column > grid[0].length) {
            return false;
        }

        //full column?
        return grid[0][column].equals(EMPTY_SLOT);
    }


    /**
     * @param player - Player's emoji in the grid
     * @param grid   - the game grid
     * @return - If the player won or not
     */
    public boolean isWinner(String player, String[][] grid) {
        //check for 4 across
        for (int row = 0; row < grid.length; row++) {
            for (int col = 0; col < grid[0].length - 3; col++) {
                if (grid[row][col] == player &&
                        grid[row][col + 1] == player &&
                        grid[row][col + 2] == player &&
                        grid[row][col + 3] == player) {

                    grid[row][col] = WIN_MOVE;
                    grid[row][col + 1] = WIN_MOVE;
                    grid[row][col + 2] = WIN_MOVE;
                    grid[row][col + 3] = WIN_MOVE;

                    return true;
                }
            }
        }
        //check for 4 up and down
        for (int row = 0; row < grid.length - 3; row++) {
            for (int col = 0; col < grid[0].length; col++) {
                if (grid[row][col] == player &&
                        grid[row + 1][col] == player &&
                        grid[row + 2][col] == player &&
                        grid[row + 3][col] == player) {

                    grid[row][col] = WIN_MOVE;
                    grid[row + 1][col] = WIN_MOVE;
                    grid[row + 2][col] = WIN_MOVE;
                    grid[row + 3][col] = WIN_MOVE;


                    return true;
                }
            }
        }
        //check upward diagonal
        for (int row = 3; row < grid.length; row++) {
            for (int col = 0; col < grid[0].length - 3; col++) {
                if (grid[row][col] == player &&
                        grid[row - 1][col + 1] == player &&
                        grid[row - 2][col + 2] == player &&
                        grid[row - 3][col + 3] == player) {


                    grid[row][col] = WIN_MOVE;
                    grid[row - 1][col + 1] = WIN_MOVE;
                    grid[row - 2][col + 2] = WIN_MOVE;
                    grid[row - 3][col + 3] = WIN_MOVE;

                    return true;
                }
            }
        }
        //check downward diagonal
        for (int row = 0; row < grid.length - 3; row++) {
            for (int col = 0; col < grid[0].length - 3; col++) {
                if (grid[row][col] == player &&
                        grid[row + 1][col + 1] == player &&
                        grid[row + 2][col + 2] == player &&
                        grid[row + 3][col + 3] == player) {

                    grid[row][col] = WIN_MOVE;
                    grid[row + 1][col + 1] = WIN_MOVE;
                    grid[row + 2][col + 2] = WIN_MOVE;
                    grid[row + 3][col + 3] = WIN_MOVE;

                    return true;
                }
            }
        }
        return false;
    }


    private void tie() {
        isTie = true;
        this.reset();
        this.debugDisplay();
        System.out.println("Game ended! - TIE");
    }

    private void reset() {
        System.out.println("Reseting game");
        gameOver = true;
    }

    public void nextTurn() {

        currentPlayer = (currentPlayer.equals(redPlayer)) ? bluePlayer : redPlayer;
        currentEmoji = currentPlayer.getSymbol();
        currentColor = (currentPlayer.equals(this.bluePlayer)) ? "Blue" : "RED";

    }

    public void checkForEndState() {
        boolean fullGrid = true;
        for(int i = 0; i < 7; i++) {
            if(grid[0][i].equals(EMPTY_SLOT))
            {
                System.out.println("Empty Column: " + i);
                fullGrid = false;
                break;
            }
        }
        System.out.println("Grid full? " + fullGrid);
        if(isWinner(currentEmoji,grid) || fullGrid) {
            this.reset();
        }
        System.out.println("Current Color win?" + isWinner(currentEmoji,grid));
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public boolean isTie() {
        return isTie;
    }

}
