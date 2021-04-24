package com.offlical.net;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.offlical.net.ai.AIPlayer;
import com.offlical.net.connect4.Connect4Game;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;

import java.io.*;
import java.util.*;

public class Connect4Bot {

    private Twitter twitterInstance;

    private static String LIKE_EMOJI = "❤";
    private static String RT_EMOJI = "\uD83D\uDD01";

    private Status gameTweet;

    private PrintWriter writer;
    private int rtMove = 0;
    private int likeMove = 0;
    private Timer timer;

    private long UPDATE_TIME = 15000;

    private Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private int blueWins = 0,redWins = 0,gameNum = 1;
    private Connect4Game game;

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

        new Connect4Bot();
    }

    Connect4Bot() throws Exception {

        twitterInstance = getTwitterInstance();

        AIPlayer red = new AIPlayer(5,Connect4Game.RED_PLAYER_EMOJI);
        AIPlayer blue = new AIPlayer(5,Connect4Game.BLUE_PLAYER_EMOJI);

        game = new Connect4Game(red,blue);
        applyJSONData();



        sendGameTweet(game);


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
        },UPDATE_TIME,UPDATE_TIME);
    }

    private void applyJSONData() throws IOException, ParseException {

        File file = new File("game_data.json");

        if(!file.exists())
        {
            file.createNewFile();

            JSONObject object = new JSONObject();

            object.put("blue_wins",0);
            object.put("red_wins",0);
            object.put("games_count",1);


            JsonParser parser = new JsonParser();
            JsonElement je = parser.parse(object.toJSONString());

            writer = new PrintWriter(file);
            writer.write(gson.toJson(je));
            writer.flush();
            writer.close();

            gameNum = 1;
            blueWins = 0;
            redWins = 0;
        } else {

            JSONObject object = (JSONObject) new JSONParser().parse(new FileReader(file));

            long blueW = (long) object.getOrDefault("blue_wins",1);
            long redW = (long) object.getOrDefault("red_wins",0);
            long gNum = (long) object.getOrDefault("games_count",0);

            if(object.get("last_game_state") != null)
            {
                System.out.println("Loading game from file...");
                game.fromJSON((JSONObject) object.get("last_game_state"));
                System.out.println("Loaded successfully!");
            }

            gameNum = (int)gNum;
            redWins = (int)redW;
            blueWins = (int)blueW;
        }


    }

    void updateEndGameJSON() throws Exception {

        File file = new File("game_data.json");

        JSONObject object = (JSONObject) new JSONParser().parse(new FileReader(file));

        object.put("blue_wins",blueWins);
        object.put("red_wins",redWins);
        object.put("games_count",gameNum);
        object.put("last_game_state",null);

        JsonParser parser = new JsonParser();
        JsonElement je = parser.parse(object.toJSONString());

        writer = new PrintWriter(file);
        writer.write(gson.toJson(je));
        writer.flush();
        writer.close();
    }

    void saveGameState(Connect4Game game) throws Exception {

        File file = new File("game_data.json");

        JSONObject object = (JSONObject) new JSONParser().parse(new FileReader(file));

        LinkedHashMap<String,Object> values = new LinkedHashMap<String,Object>(3);

        values.put("turn",game.turn);
        values.put("currentPlayer", game.currentColor);
        values.put("grid",game.gridToJSONString());

        object.put("last_game_state",values);

        JsonParser parser = new JsonParser();
        JsonElement je = parser.parse(object.toJSONString());

        writer = new PrintWriter(file);
        writer.write(gson.toJson(je));
        writer.flush();
        writer.close();

    }

    void restart() {

        AIPlayer red = new AIPlayer(5,Connect4Game.RED_PLAYER_EMOJI);
        AIPlayer blue = new AIPlayer(5,Connect4Game.BLUE_PLAYER_EMOJI);

        Connect4Game game = new Connect4Game(red,blue);

        try {
            sendGameTweet(game);
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
        },UPDATE_TIME,UPDATE_TIME);
    }

    void gameUpdate(Connect4Game game) throws Exception {

        try {
            gameTweet = getLatestTweet();
        } catch (TwitterException e) {
            e.printStackTrace();
        }

        if(game.gameOver) {
            System.out.println("game ended!");
            return;
        }

        System.out.println("Game update!");
        if(gameTweet == null) {

            try {
                gameTweet = getLatestTweet();
            } catch (TwitterException e) {
                e.printStackTrace();
            }

        }

        int rts, likes;
        rts = gameTweet.getRetweetCount();
        likes = gameTweet.getFavoriteCount();


        if(likes < rts)
            game.play(rtMove);
        else if(likes > rts)
            game.play(likeMove);
        else{
            Random r = new Random();
            boolean likesPlay = r.nextBoolean();
            if(likesPlay)
                game.play(likeMove);
            else
                game.play(rtMove);
        }

        if(game.gameOver)
        {
            timer.cancel();
            timer = new Timer();
            tweetWinner(game);
            timer.schedule(new TimerTask() {
                @Override
                public void run() {

                    if(game.currentColor.equals("Blue"))
                        blueWins++;
                    else
                        redWins++;
                    gameNum++;
                    try {
                        updateEndGameJSON();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    restart();
                }
            },60000);
            return;
        }

        sendGameTweet(game);
        saveGameState(game);
    }

    void tweetWinner(Connect4Game game) {

        StringBuilder tweet = new StringBuilder();

        String bluePlus = "", redPlus = "";

        if(game.currentColor.equals("Blue"))
            bluePlus = " (+1)";
        else
            redPlus = " (+1)";

        tweet.append("Game #" + gameNum + " | Turn: " + game.turn + " \n\n Blue wins: " + blueWins + bluePlus + "\n Red wins: " + redWins + redPlus + " \n\n\n" + game.currentEmoji + " " + game.currentColor + "'s Won! \n\n")
                .append(game.gridToString(game.grid) + "\n Next game will start in 1 minute!");

        try {
            twitterInstance.updateStatus(tweet.toString());
        } catch (TwitterException e) {
            e.printStackTrace();
        }
    }

    public void sendGameTweet(Connect4Game game) throws TwitterException {

       int[] bestMoves = game.currentPlayer.bestMoves(game.grid);

       this.rtMove = bestMoves[0];
       this.likeMove = bestMoves[1];

       if(rtMove == likeMove) {

           likeMove += 1;
           if(likeMove > 6)
           {
               List<Integer> list = Arrays.asList(game.currentPlayer.getFreeSpots(game.grid));
               // if no other free spot is available
               if(list.size() == 1 && list.contains(rtMove))
                   likeMove = -1; // skip move
               else
                   for(int i : list) {
                       if(i != rtMove)
                       {
                           // get the first empty slot
                           likeMove = i;
                           break;
                       }
                   }
           }
       }

       StringBuilder tweet = new StringBuilder();

       tweet.append("Game #" + gameNum + " | Turn: " + game.turn + " \n\n Blue wins: " + blueWins + "\n Red wins: " + redWins + " \n\n\n" + game.currentEmoji + " " + game.currentColor + "'s Turn! Choose which move to do below! \n\n")
               .append(game.gridToString(game.grid) + "\n")
               .append(RT_EMOJI + game.numberToEmoji(rtMove) + "         " + LIKE_EMOJI + game.numberToEmoji(likeMove));


       gameTweet = twitterInstance.updateStatus(tweet.toString());
   }

   public Status getLatestTweet() throws TwitterException {
        ResponseList<Status> list = twitterInstance.getUserTimeline("Connect4GameBot",new Paging(1,1));

        return list.get(0);
   }

   public static Twitter getTwitterInstance() {
        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setDebugEnabled(true)
                .setOAuthConsumerKey(CONSUMER_KEY)
                .setOAuthConsumerSecret(CONSUMER_SECRET)
                .setOAuthAccessToken(ACCESS_TOKEN)
                .setOAuthAccessTokenSecret(ACCESS_TOKEN_SECRET);

        TwitterFactory tf = new TwitterFactory(cb.build());
        return tf.getInstance();
    }

}
