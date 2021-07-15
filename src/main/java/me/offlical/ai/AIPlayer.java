package me.offlical.ai;

import me.offlical.connect4.Connect4Game;

import java.util.ArrayList;
import java.util.List;

public class AIPlayer  {

    private final int maxDepth;
    private final String aiSymbol;
    private final String opponentSymbol;

    private int bestColumn;
    private int secondBestColumn;

    public AIPlayer(int depth, String playerSymbol) {
        super();
        this.maxDepth = depth;
        this.aiSymbol = playerSymbol;

        this.opponentSymbol = !playerSymbol.equals(Connect4Game.BLUE_PLAYER_EMOJI) ? Connect4Game.BLUE_PLAYER_EMOJI : Connect4Game.RED_PLAYER_EMOJI;

        System.out.println("Player's symbol: " + opponentSymbol);
    }

    public int[] bestMoves(String[][] board) {


        bestColumn = 0;
        secondBestColumn = -1;

        minimax(board, 0, true, 0);

        int bestMove = bestColumn;


        return new int[]{bestMove, secondBestColumn};
    }

    /**
     * @param board        - Current board state
     * @param depth        - How far in depth are we
     * @param isMaximizing - If true, we're playing for ourselves (the player) otherwise we're playing for the opponent
     * @return - Score of a certain move
     */
    public int minimax(String[][] board, int depth, boolean isMaximizing, int c) {

        if (depth == maxDepth) {
            return evaluation(board);
        }
        if (isWinner(aiSymbol, board)) {
            return evaluation(board);
        }

        Integer[] freeSpots = getFreeSpots(board);

        int bestScore, secondBest;
        int secondMove = -1;
        if (isMaximizing) {
            bestScore = -Integer.MAX_VALUE;
            secondBest = -Integer.MAX_VALUE - 1;
            for (int i : freeSpots) {

                String[][] clone = cloneBoard(board);
                for (int row = clone.length - 1; row >= 0; row--) {
                    if (clone[row][i].equalsIgnoreCase(Connect4Game.EMPTY_SLOT)) {
                        clone[row][i] = aiSymbol;
                        break;
                    }
                }
                int score = minimax(clone, depth + 1, false, i);
                if (score > bestScore) {
                    bestScore = score;
                    bestColumn = i;
                }
                if (score > secondBest && score < bestScore && i != bestColumn) {
                    secondBest = score;
                    secondMove = i;
                }
            }
        } else {
            bestScore = Integer.MAX_VALUE;
            secondBest = Integer.MAX_VALUE - 1;
            for (int i : freeSpots) {

                String[][] clone = cloneBoard(board);
                for (int row = clone.length - 1; row >= 0; row--) {
                    if (clone[row][i].equalsIgnoreCase(Connect4Game.EMPTY_SLOT)) {
                        clone[row][i] = opponentSymbol;
                        break;
                    }
                }
                int score = minimax(clone, depth + 1, true, i);
                if (score < bestScore) {
                    bestScore = score;
                    bestColumn = i;
                }
                if (score < secondBest && score > bestScore && i != bestColumn) {
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
        for (int row = 0; row < board.length; row++)
            System.arraycopy(board[row], 0, instance[row], 0, board[0].length);

        return instance;
    }

    public boolean isWinner(String player, String[][] grid) {
        //check for 4 across
        for (int row = 0; row < grid.length; row++) {
            for (int col = 0; col < grid[0].length - 3; col++) {
                if (grid[row][col] == player &&
                        grid[row][col + 1] == player &&
                        grid[row][col + 2] == player &&
                        grid[row][col + 3] == player) {
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
                    return true;
                }
            }
        }
        return false;
    }

    public Integer[] getFreeSpots(String[][] board) {
        List<Integer> freeSpots = new ArrayList<>();

        for (int row = 0; row < board.length - 1; row++) {
            for (int col = 0; col < board[0].length; col++) {
                if (board[row][col].equals(Connect4Game.EMPTY_SLOT) && !freeSpots.contains(col))
                    freeSpots.add(col);
            }
        }

        return freeSpots.toArray(new Integer[0]);
    }

    public int getNextFreeRow(int col, String[][] board) {
        for (int row = 0; row < board.length - 1; row++) {
            if (board[row][col] == Connect4Game.EMPTY_SLOT)
                return row;
        }
        return 0;
    }

    public int evaluation(String[][] board) {

        int score = 0;

        int center_pieces = 0;
        // center
        for (int r = 0; r < board.length; r++) {
            if (board[r][board[0].length / 2].equals(aiSymbol))
                center_pieces++;
        }
        score += center_pieces * 2;


        // horizontal
        for (int row = 0; row < board.length; row++) {
            for (int col = 0; col < board[0].length - 3; col++) {
                int computer = 0, player = 0, empty = 0;
                for (int i = 0; i < 4; i++)
                    if (board[row][col + i].equals(aiSymbol))
                        computer++;
                    else if (board[row][col + i].equals(opponentSymbol))
                        player++;
                    else
                        empty++;

                score += scoreRow(new int[]{computer, player, empty});
            }
        }

        // vertical
        for (int row = 0; row < board.length - 3; row++) {
            for (int col = 0; col < board[0].length; col++) {
                int computer = 0, player = 0, empty = 0;
                for (int i = 0; i < 4; i++)
                    if (board[row + i][col].equals(aiSymbol))
                        computer++;
                    else if (board[row + i][col].equals(opponentSymbol))
                        player++;
                    else
                        empty++;

                score += scoreRow(new int[]{computer, player, empty});
            }
        }

        // upward diagonal
        for (int row = 3; row < board.length; row++) {
            for (int col = 0; col < board[0].length - 3; col++) {
                int computer = 0, player = 0, empty = 0;
                for (int i = 0; i < 4; i++)
                    if (board[row - i][col + i].equals(aiSymbol))
                        computer++;
                    else if (board[row - i][col + i].equals(opponentSymbol))
                        player++;
                    else
                        empty++;

                score += scoreRow(new int[]{computer, player, empty});
            }
        }

        // downward diagonal
        for (int row = 0; row < board.length - 3; row++) {
            for (int col = 0; col < board[0].length - 3; col++) {
                int computer = 0, player = 0, empty = 0;
                for (int i = 0; i < 4; i++)
                    if (board[row + i][col + i].equals(aiSymbol))
                        computer++;
                    else if (board[row + i][col + i].equals(opponentSymbol))
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


        if (computer == 3 && empty == 1)
            score += 50;
        if (computer == 2 && empty == 2)
            score += 20;
        if (player == 2 && empty == 2)
            score -= 20;
        if (computer == 4)
            score += 1000;
        if (player == 3 && empty == 1)
            score -= 1250;
        if (player == 4)
            score -= 2000;

        return score;
    }

    public String getSymbol() {
        return aiSymbol;
    }

}
