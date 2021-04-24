package com.offlical.net.ai;

import com.offlical.net.connect4.AIConnect4;
import com.offlical.net.connect4.Connect4Game;
import com.offlical.net.connect4.Connect4Player;
import com.offlical.net.connect4.MoveType;

import java.util.ArrayList;
import java.util.List;

public class AIPlayer extends Connect4Player {

    private int maxDepth;
    private String aiSymbol;
    private String opponentSymbol;

    private int bestColumn;
    private int secondBestColumn;

    public AIPlayer(int depth, String playerSymbol) {
        super();
        this.maxDepth = depth;
        this.aiSymbol = playerSymbol;

        this.opponentSymbol = !playerSymbol.equals(Connect4Game.BLUE_PLAYER_EMOJI) ? Connect4Game.BLUE_PLAYER_EMOJI : Connect4Game.RED_PLAYER_EMOJI;

        System.out.println("Player's symbol: " + opponentSymbol);
        // System.out.println("AI Symbol: " + this.getSymbol());
    }

    public int[] bestMoves(String[][] board) {

        long time = System.currentTimeMillis();
        // System.out.println("[BestMove] Getting best move...");

        bestColumn = 0;
        secondBestColumn = -1;

        minimax(board,0,true,0);

        int bestMove = bestColumn;

        time = System.currentTimeMillis() - time;
        // System.out.println("[BestMove] Found best move in " + time + "ms ");
        // System.out.println("bestMove: " + bestMove);
        // System.out.println("Player's symbol: " + opponentSymbol);
        // System.out.println("AI Symbol: " + this.getSymbol());


        System.out.println("Best move: " + bestMove);
        System.out.println("Second best move: " + secondBestColumn);
        return new int[]{bestMove,secondBestColumn};
    }
    /**
     *
     * @param board - Current board state
     * @param depth - How far in depth are we
     * @param isMaximizing - If true, we're playing for ourselves (the player) otherwise we're playing for the opponent
     * @return - Score of a certain move
     */
    public int minimax(String[][] board, int depth, boolean isMaximizing, int c) {

     //   System.out.println("[WinnerCheck] Checking for winner...");
     //   System.out.println("[MiniMax] DEPTH: " + depth + " MAXIMIZING? " + isMaximizing);

        if(depth == maxDepth) {
        //    System.out.println("[END] Reached max depth ");
            int row = getNextFreeRow(c,board);
            // System.out.println("Evaluating position: Row - " + row + " Column: " + c);
           // display(board);
            return evaluation(board);
        }
        long time = System.currentTimeMillis();
        if(isWinner(aiSymbol,board)) {
            time = System.currentTimeMillis() - time;
      //      System.out.println("[WinnerCheck] Took " + time + "ms ");
            return evaluation(board);
        }
        time = System.currentTimeMillis() - time;
 //       System.out.println("[WinnerCheck] Took " + time + "ms ");

        Integer[] freeSpots = getFreeSpots(board);
        // System.out.println("Free spots: " + freeSpots.length);

        int bestScore, secondBest;
        int secondMove = -1;
        if(isMaximizing) {
            bestScore = -Integer.MAX_VALUE;
            secondBest = -Integer.MAX_VALUE-1;
            for(int i : freeSpots) {

                // System.out.println("Looking at: " + i);
                String[][] clone = cloneBoard(board);
                for (int row = clone.length-1; row >= 0; row--){
                    if(clone[row][i].equalsIgnoreCase(Connect4Game.EMPTY_SLOT)){
                        clone[row][i] = aiSymbol;
                        break;
                    }
                }
                int score = minimax(clone,depth + 1, false,i);
                if(score > bestScore)
                {
                    bestScore = score;
                    bestColumn = i;
                }
                if(score > secondBest && score < bestScore && i != bestColumn) {
                    secondBest = score;
                    secondMove = i;
                }
            }
        } else {
            bestScore = Integer.MAX_VALUE;
            secondBest = Integer.MAX_VALUE-1;
            for(int i : freeSpots) {

                String[][] clone = cloneBoard(board);
                for (int row = clone.length-1; row >= 0; row--){
                    if(clone[row][i].equalsIgnoreCase(Connect4Game.EMPTY_SLOT)){
                        clone[row][i] = opponentSymbol;
                        break;
                    }
                }
                int score = minimax(clone,depth + 1, true,i);
                if(score < bestScore)
                {
                    bestScore = score;
                    bestColumn = i;
                }
                if(score < secondBest && score > bestScore && i != bestColumn) {
                    secondBest = score;
                    secondMove = i;
                }
            }
        }



        secondBestColumn = secondMove;
   //     System.out.println("[MiniMax] Finished minimax function at depth " + depth);
        return bestScore;
    }

    public String[][] cloneBoard(String[][] board) {
        String[][] instance = new String[6][7];
        for(int row  = 0; row < board.length ; row++)
            System.arraycopy(board[row], 0, instance[row], 0, board[0].length);

      return instance;
    }

    public void display(String[][] grid){
        // System.out.println(" 0 1 2 3 4 5 6");
        // System.out.println("---------------");
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

    public boolean isWinner(String player, String[][] grid) {
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

    public Integer[] getFreeSpots(String[][] board) {
        List<Integer> freeSpots = new ArrayList<>();

        for(int row = 0; row < board.length-1; row++) {
            for(int col = 0; col < board[0].length; col++) {
                if(board[row][col].equals(Connect4Game.EMPTY_SLOT) && !freeSpots.contains(col))
                    freeSpots.add(col);
            }
        }

        return freeSpots.toArray(new Integer[0]);
    }




    public int getNextFreeRow(int col, String[][] board) {
        for(int row = 0; row < board.length-1; row++) {
            if(board[row][col] == Connect4Game.EMPTY_SLOT)
                return row;
        }
        return 0;
    }

    public int evaluation(String[][] board) {

        int score = 0;

        int center_pieces = 0;
        // center
        for(int r = 0; r < board.length; r++) {
            if(board[r][board[0].length/2].equals(aiSymbol))
                center_pieces++;
        }
        score += center_pieces * 2;


        // horizontal
        for(int row = 0; row < board.length; row++){
            for (int col = 0;col < board[0].length - 3;col++){
                int computer = 0, player = 0, empty = 0;
                for(int i = 0; i < 4; i++)
                        if(board[row][col+i].equals(aiSymbol))
                            computer++;
                        else if(board[row][col+i].equals(opponentSymbol))
                            player++;
                        else
                            empty++;

                score += scoreRow(new int[]{computer, player, empty});
            }
        }

        // vertical
        for(int row = 0; row < board.length - 3; row++){
            for(int col = 0; col < board[0].length; col++){
                int computer = 0, player = 0, empty = 0;
                for(int i = 0; i < 4; i++)
                    if(board[row+i][col].equals(aiSymbol))
                        computer++;
                    else if(board[row+i][col].equals(opponentSymbol))
                        player++;
                    else
                        empty++;

                score += scoreRow(new int[]{computer, player, empty});
            }
        }

        // upward diagonal
        for(int row = 3; row < board.length; row++){
            for(int col = 0; col < board[0].length - 3; col++){
                int computer = 0, player = 0, empty = 0;
                for(int i = 0; i < 4; i++)
                    if(board[row-i][col+i].equals(aiSymbol))
                        computer++;
                    else if(board[row-i][col+i].equals(opponentSymbol))
                        player++;
                    else
                        empty++;

                score += scoreRow(new int[]{computer, player, empty});
            }
        }

        // downward diagonal
        for(int row = 0; row < board.length - 3; row++) {
            for (int col = 0; col < board[0].length - 3; col++) {
                int computer = 0, player = 0, empty = 0;
                for(int i = 0; i < 4; i++)
                    if(board[row+i][col+i].equals(aiSymbol))
                        computer++;
                    else if(board[row+i][col+i].equals(opponentSymbol))
                        player++;
                    else
                        empty++;

                    score += scoreRow(new int[]{computer, player, empty});
            }



        }


        return score;
    }

    public int scoreRow(int[] counters) {

        int score = 0;

        int computer = counters[0];
        int player = counters[1];
        int empty = counters[2];


        if(computer == 3 && empty == 1)
            score += 50;
        if(computer == 2 && empty == 2)
            score += 20;
        //      if(player == 3 && computer == 1)
        //         score += 3;
        if(player == 2 && empty == 2)
            score -= 20;
        if(computer == 4)
            score += 1000;
        if(player == 3 && empty == 1)
            score -= 1250;
        if(player == 4)
            score -= 2000;

        return score;
    }

    /*
  public int scoreRow(int[] counters) {

        int score = 0;

        int computer = counters[0];
        int player = counters[1];
        int empty = counters[2];

        if(computer == 3 && empty == 1)
            score += 150;
        if(computer == 2 && empty == 2)
            score += 15;
        //if(player == 3 && computer == 1)
        //    score += 175;
        if(player == 2 && empty == 2)
            score -= 20;
        if(computer == 4)
            score += 1100;
        //   if(player == 3 && computer == 1)
        //       score += 250;
        if(player == 3 && empty == 1)
            score -= 1350;
        if(player == 4)
            score -= 1500;


        /*if(computer == 4)
            score += 1350;
        if(player == 3 && empty == 1)
            score -= 1250;
        if(player == 4)
            score -= 1750;*/

        /*
        if(computer == 3 && empty == 1)
            score += 150;
        if(computer == 2 && empty == 2)
            score += 20;
        if(player == 3 && computer == 1)
            score += 30;
        if(player == 2 && empty == 2)
            score -= 20;
        if(computer == 4)
            score += 1100;
        if(player == 3 && computer == 1)
            score += 250;
        if(player == 3 && empty == 1)
            score -= 1350;
        if(player == 4)
            score -= 1500;
         */

     //   return score;
   // }

    public int scoreMoveFromMove(String[][] board, int col, int row, MoveType moveType) {

        int score = 0;


        int computer = 0, player = 0, empty = 0;

        String[][] clone = cloneBoard(board);

        switch(moveType) {
            case VERTICAL:
                if(board[row][col].equalsIgnoreCase(aiSymbol) && (row+4) < board.length) {

                    row += 1;

                    if(clone[row][col] != AIConnect4.EMPTY_SLOT)
                        break;
                    clone[row][col] = opponentSymbol;

                    System.out.println("vertical");
                    for (int i = 0; i < 4; i++)
                        if (clone[row + i][col].equals(aiSymbol))
                            computer++;
                        else if (clone[row + i][col].equals(opponentSymbol))
                            player++;
                        else
                            empty++;

                    score += scoreHorizontal(clone,row,col);
                }
                break;
            case HORIZONTAL:
                if(board[row][col].equals(aiSymbol) && (col+4) < board[0].length) {

                    col += 1;

                    if(clone[row][col] != AIConnect4.EMPTY_SLOT)
                        break;

                    clone[row][col] = opponentSymbol;
                    for (int i = 0; i < 4; i++)
                        if (board[row][col + i].equals(aiSymbol))
                            computer++;
                        else if (board[row][col + i].equals(opponentSymbol))
                            player++;
                        else
                            empty++;

                    score += scoreVertical(clone,row,col);
                }
                break;
            case UPWARDS_DIAGONAL:
                if(board[row][col].equals(aiSymbol) && (row - 4) >= 0 && (col+4) < board[0].length) {

                    row -= 1;
                    //col += 1;
                    if(clone[row][col] != AIConnect4.EMPTY_SLOT)
                        break;
                    clone[row][col] = opponentSymbol;

                    System.out.println("upwards diagonal");
                    for (int i = 0; i < 4; i++)
                        if (board[row - i][col + i].equals(aiSymbol))
                            computer++;
                        else if (board[row - i][col + i].equals(opponentSymbol))
                            player++;
                        else
                            empty++;

                    if(computer == 3 && empty == 1)
                        score += 50;
                    if(computer == 2 && empty == 2)
                        score += 10;
                    //  if(player == 3 && computer == 1)
                    //     score += 30;
                    if(player == 2 && empty == 2)
                        score -= 20;
                    if(computer == 4)
                        score += 1000;
                    //  if(player == 3 && computer == 1)
                    //      score += 250;
                    if(player == 3 && empty == 1)
                        score -= 1750;
                    if(player == 4)
                        score -= 1850;

                    score += scoreHorizontal(clone,row,col);
                    score += scoreVertical(clone,row,col);
                }
                break;
            case DOWNWARDS_DIAGONAL:
                if(board[row][col].equals(aiSymbol) && (row + 4) < board.length && (col+4) < board[0].length) {

                    System.out.println("checking downards diagonal");
                    row += 1;
                    //col += 1;

                    if(clone[row][col] != AIConnect4.EMPTY_SLOT)
                        break;
                    clone[row][col] = opponentSymbol;

                    for (int i = 0; i < 4; i++)
                        if (board[row + i][col + i].equals(aiSymbol))
                            computer++;
                        else if (board[row + i][col + i].equals(opponentSymbol))
                            player++;
                        else
                            empty++;

                    if(computer == 3 && empty == 1)
                        score += 50;
                    if(computer == 2 && empty == 2)
                        score += 10;
                    //  if(player == 3 && computer == 1)
                    //     score += 30;
                    if(player == 2 && empty == 2)
                        score -= 20;
                    if(computer == 4)
                        score += 1000;
                    //  if(player == 3 && computer == 1)
                    //      score += 250;
                    if(player == 3 && empty == 1)
                        score -= 1750;
                    if(player == 4)
                        score -= 1850;

                    score += scoreHorizontal(clone,row,col);
                    score += scoreVertical(clone,row,col);
                }
                break;
        }

        if(computer == 3 && empty == 1)
            score += 50;
        if(computer == 2 && empty == 2)
            score += 10;
        //  if(player == 3 && computer == 1)
        //     score += 30;
        if(player == 2 && empty == 2)
            score -= 20;
        if(computer == 4)
            score += 1000;
        //  if(player == 3 && computer == 1)
        //      score += 250;
        if(player == 3 && empty == 1)
            score -= 1750;
        if(player == 4)
            score -= 1850;


        /*if(computer == 4)
            score += 1350;
        if(player == 3 && empty == 1)
            score -= 1250;
        if(player == 4)
            score -= 1750;*/

        return score;
    }

    public int scoreHorizontal(String[][] board, int row, int col) {

        if(col+3 > board[0].length-1)
            return 0;
        int score = 0;

        int computer = 0, player = 0, empty = 0;

        for (int i = 0; i < 4; i++)
            if (board[row][col + i].equals(aiSymbol))
                computer++;
            else if (board[row][col + i].equals(opponentSymbol))
                player++;
            else
                empty++;

        if(computer == 3 && empty == 1)
            score += 50;
        if(computer == 2 && empty == 2)
            score += 10;
        //  if(player == 3 && computer == 1)
        //     score += 30;
        if(player == 2 && empty == 2)
            score -= 20;
        if(computer == 4)
            score += 1000;
        //  if(player == 3 && computer == 1)
        //      score += 250;
        if(player == 3 && empty == 1)
            score -= 1750;
        if(player == 4)
            score -= 1850;


        return score;
    }

    public int getBestColumn() {
        return bestColumn;
    }

    public int getSecondBestColumn() {
        return secondBestColumn;
    }

    public int scoreVertical(String[][] board, int row, int col) {

        if(row+3 > board.length-1)
            return 0;
        int score = 0;

        int computer = 0, player = 0, empty = 0;

        for (int i = 0; i < 4; i++)
            if (board[row + i][col].equals(aiSymbol))
                computer++;
            else if (board[row + i][col].equals(opponentSymbol))
                player++;
            else
                empty++;


        if(computer == 3 && empty == 1)
            score += 50;
        if(computer == 2 && empty == 2)
            score += 10;
        //  if(player == 3 && computer == 1)
        //     score += 30;
        if(player == 2 && empty == 2)
            score -= 20;
        if(computer == 4)
            score += 1000;
        //  if(player == 3 && computer == 1)
        //      score += 250;
        if(player == 3 && empty == 1)
            score -= 1750;
        if(player == 4)
            score -= 1850;

        return score;
    }

    public boolean validate(int column, String[][] grid){
        //valid column?
        if (column < 0 || column > grid[0].length){
            return false;
        }

        //full column?
        return true;
    }

    @Override
    public String getGameName() {
        return "AI Player";
    }

    @Override
    public String getSymbol() {
        return aiSymbol;
    }

    public String getOpponentSymbol() {
        return opponentSymbol;
    }

    public String getAiSymbol() {
        return aiSymbol;
    }

    public int getMaxDepth() {
        return maxDepth;
    }
}
