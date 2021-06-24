package me.offlical;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import me.offlical.ai.AIPlayer;
import me.offlical.connect4.Connect4Game;
import me.offlical.json.Connect4JSON;
import me.offlical.twitter.TwitterManager;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;

import java.io.*;
import java.util.*;

public class Connect4Bot {


    private final TwitterManager twitterManager;
    private final Connect4JSON json;

    private static String LIKE_EMOJI = "❤";
    private static String RT_EMOJI = "\uD83D\uDD01";

    private PrintWriter writer;
    private int rtMove = 0;
    private int likeMove = 0;
    private Timer timer;

    private final long UPDATE_TIME = 1800000L;
    private Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private int blueWins = 0, redWins = 0, gameNum = 1;
    private final Connect4Game game;

    static String CONSUMER_KEY;
    static String CONSUMER_SECRET;
    static String ACCESS_TOKEN;
    static String ACCESS_TOKEN_SECRET;

    public static void main(String[] args) throws Exception {

        File file = new File("credentials.json");

        JSONObject object = (JSONObject) new JSONParser().parse(new FileReader(file));

        ACCESS_TOKEN = (String) object.get("api_key");
        ACCESS_TOKEN_SECRET = (String) object.get("api_secret");
        CONSUMER_KEY = (String) object.get("consumer_key");
        CONSUMER_SECRET = (String) object.get("consumer_secret");

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

        resetMoves();
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

    void restart() {

        AIPlayer red = new AIPlayer(5, Connect4Game.RED_PLAYER_EMOJI);
        AIPlayer blue = new AIPlayer(5, Connect4Game.BLUE_PLAYER_EMOJI);

        Connect4Game game = new Connect4Game(red, blue);

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

    void gameUpdate(Connect4Game game) throws Exception {

        Status gameTweet = null;
        try {
            gameTweet = twitterManager.getLatestTweet();
        } catch (TwitterException e) {
            e.printStackTrace();
        }

        if (game.gameOver) {
            System.out.println("game ended!");
            return;
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

        if (game.gameOver) {
            timer = new Timer();
            twitterManager.tweetWinner(game,gameNum,blueWins,redWins);
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if (game.currentColor.equals("Blue"))
                        blueWins++;
                    else
                        redWins++;
                    gameNum++;
                    try {
                        json.updateStats(gameNum,blueWins,redWins);
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

    /**
     *  Resets the moves for likes & retweets, calls the current aiplayer to calculate the 2 best moves.
     */
    public void resetMoves() {
        int[] bestMoves = game.currentPlayer.bestMoves(game.grid);

        this.rtMove = bestMoves[0];
        this.likeMove = bestMoves[1];

        if (rtMove == likeMove) {

            likeMove += 1;
            if (likeMove > 6) {
                List<Integer> list = Arrays.asList(game.currentPlayer.getFreeSpots(game.grid));
                // if no other free spot is available
                if (list.size() == 1 && list.contains(rtMove))
                    likeMove = -1; // skip move
                else
                    for (int i : list) {
                        if (i != rtMove) {
                            // get the first empty slot
                            likeMove = i;
                            break;
                        }
                    }
            }
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
