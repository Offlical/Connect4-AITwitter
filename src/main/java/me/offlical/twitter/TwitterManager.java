package me.offlical.twitter;

import me.offlical.Connect4Bot;
import me.offlical.connect4.Connect4Game;
import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;

public class TwitterManager {

    private Twitter twitterInstance;
    private final String[] numberEmojis = new String[]{"\u0031\u20E3","\u0032\u20E3","\u0033\u20E3","\u0034\u20E3","\u0035\u20E3","\u0036\u20E3","\u0037\u20E3"};
    private final String LIKE_EMOJI = "❤";
    private final String RT_EMOJI = "\uD83D\uDD01";

    public TwitterManager(String CONSUMER_KEY, String CONSUMER_SECRET, String ACCESS_TOKEN, String ACCESS_TOKEN_SECRET) {
        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setDebugEnabled(true)
                .setOAuthConsumerKey(CONSUMER_KEY)
                .setOAuthConsumerSecret(CONSUMER_SECRET)
                .setOAuthAccessToken(ACCESS_TOKEN)
                .setOAuthAccessTokenSecret(ACCESS_TOKEN_SECRET);

        TwitterFactory tf = new TwitterFactory(cb.build());

        twitterInstance = tf.getInstance();
    }

    public Status getLatestTweet() throws TwitterException {
        ResponseList<Status> list = twitterInstance.getUserTimeline("Connect4GameBot",new Paging(1,10));
        for(Status status : list) {
            if(status.getText().startsWith("Game #") && !status.isRetweet() && !status.isRetweetedByMe())
                return status;
        }
        return list.get(0);
    }

    public void tweetWinner(Connect4Game game, int gameNum, int blueWins, int redWins) {

        StringBuilder tweet = new StringBuilder();

        String bluePlus = "", redPlus = "";

        if (game.currentColor.equals("Blue"))
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

    public void sendGameTweet(Connect4Bot bot) throws TwitterException {
        StringBuilder tweet = new StringBuilder();
        Connect4Game game = bot.getGame();

        tweet.append("Game #" + bot.getGameNum() + " | Turn: " + game.turn+ " \n\n Blue wins: " + bot.getBlueWins() + "\n Red wins: " + bot.getRedWins() + " \n\n\n" + game.currentEmoji + " " + game.currentColor + "'s Turn! Choose which move to do below! \n\n")
                .append(game.gridToString(game.grid) + "\n")
                .append(RT_EMOJI + game.numberToEmoji(bot.getRetweetMove()) + "         " + LIKE_EMOJI + game.numberToEmoji(bot.getLikeMove()));

        twitterInstance.updateStatus(tweet.toString());
    }

    public String numberToEmoji(int num) {
        if(num == -1)
            return " Skip Turn";
        return numberEmojis[num];
    }

    public Twitter getTwitterInstance() {
        return twitterInstance;
    }
}
