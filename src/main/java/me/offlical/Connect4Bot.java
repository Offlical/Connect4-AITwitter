package me.offlical;

import me.offlical.ai.AIPlayer;
import me.offlical.connect4.Connect4Game;
import me.offlical.json.Connect4JSON;
import me.offlical.twitter.TwitterManager;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import twitter4j.*;

import java.io.*;
import java.util.*;

public class Connect4Bot {

    private final TwitterManager twitterManager;
    private final Connect4JSON json;

    private int rtMove = 0;
    private int likeMove = 0;
    private Timer timer;

    private final long UPDATE_TIME = 1800000L;
    private int blueWins = 0, redWins, gameNum = 1;
    private Connect4Game game;

    public static void main(String[] args) throws Exception {

        File file = new File("credentials.json");

        JSONObject object = (JSONObject) new JSONParser().parse(new FileReader(file));

        String ACCESS_TOKEN = (String) object.get("api_key");
        String ACCESS_TOKEN_SECRET = (String) object.get("api_secret");
        String CONSUMER_KEY = (String) object.get("consumer_key");
        String CONSUMER_SECRET = (String) object.get("consumer_secret");

        TwitterManager twitterManager = new TwitterManager(CONSUMER_KEY, CONSUMER_SECRET, ACCESS_TOKEN, ACCESS_TOKEN_SECRET);
        new Connect4Bot(twitterManager);
    }

    Connect4Bot(TwitterManager twitterManager) throws Exception {

        this.twitterManager = twitterManager;
        this.json = new Connect4JSON("");

        AIPlayer red = new AIPlayer(5, Connect4Game.RED_PLAYER_EMOJI);
        AIPlayer blue = new AIPlayer(5, Connect4Game.BLUE_PLAYER_EMOJI);

        game = new Connect4Game(red, blue);

        int[] stats = json.readStats();
        this.blueWins = stats[0];
        this.redWins = stats[1];
        this.gameNum = stats[2];
        json.readAndApplyGameState(game);
        if (game.turn > 1) {
            game.checkForEndState();
        }

        resetMoves();
        if (game.isGameOver()) {
            gameNum += 1;
            restart();
            return;
        }
        twitterManager.sendGameTweet(this);

        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    gameUpdate(game);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, UPDATE_TIME, UPDATE_TIME);
    }

    public void gameUpdate(Connect4Game game) throws Exception {

        Status gameTweet = null;
        try {
            gameTweet = twitterManager.getLatestTweet();
        } catch (TwitterException e) {
            e.printStackTrace();
        }


        System.out.println("Game update!");
        if (gameTweet == null) {

            System.out.println("Null Tweet! No Tweets found?");
            return;
        }

        int rts, likes;
        rts = gameTweet.getRetweetCount();
        likes = gameTweet.getFavoriteCount();


        if (likes < rts)
            game.play(rtMove);
        else if (likes > rts)
            game.play(likeMove);
        else {
            Random r = new Random();
            boolean likesPlay = r.nextBoolean();
            if (likesPlay)
                game.play(likeMove);
            else
                game.play(rtMove);
        }

        if (game.isGameOver()) {
            timer = new Timer();
            twitterManager.tweetEndState(game, gameNum, blueWins, redWins);
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if (game.currentColor.equals("Blue"))
                        blueWins++;
                    else
                        redWins++;
                    gameNum++;
                    try {
                        json.updateStats(gameNum, blueWins, redWins);
                        json.clearGameState();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    restart();
                }
            }, 60000);
            return;
        }

        resetMoves();
        twitterManager.sendGameTweet(this);
        json.saveGameState(game);
    }

    public void restart() {

        AIPlayer red = new AIPlayer(5, Connect4Game.RED_PLAYER_EMOJI);
        AIPlayer blue = new AIPlayer(5, Connect4Game.BLUE_PLAYER_EMOJI);

        game = new Connect4Game(red, blue);

        try {
            resetMoves();
            twitterManager.sendGameTweet(this);
        } catch (TwitterException e) {
            e.printStackTrace();
        }

        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    gameUpdate(game);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, UPDATE_TIME, UPDATE_TIME);
    }

    /**
     * Resets the moves for likes & retweets, calls the current aiplayer to calculate the 2 best moves.
     */
    public void resetMoves() {
        if (game.isGameOver()) {
            rtMove = -1;
            likeMove = -1;
            return;
        }
        int[] bestMoves = game.currentPlayer.bestMoves(game.grid);

        this.rtMove = bestMoves[0];
        this.likeMove = bestMoves[1];

        if (rtMove == likeMove) {

            List<Integer> list = Arrays.asList(game.currentPlayer.getFreeSpots(game.grid));
            // if no other free spot is available
            if (list.size() == 1 && list.contains(rtMove))
                likeMove = -1; // skip move
            else
                likeMove = list.get(0);
        }
    }

    public int getBlueWins() {
        return blueWins;
    }

    public int getRedWins() {
        return redWins;
    }

    public int getGameNum() {
        return gameNum;
    }

    public Connect4Game getGame() {
        return game;
    }

    public int getRetweetMove() {
        return rtMove;
    }

    public int getLikeMove() {
        return likeMove;
    }
}
