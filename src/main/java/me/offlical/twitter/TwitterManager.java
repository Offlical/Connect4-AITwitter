package me.offlical.twitter;

import me.offlical.Connect4Bot;
import me.offlical.connect4.Connect4Game;
import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;

public class TwitterManager {

    private Twitter twitterInstance;

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
        ResponseList<Status> list = twitterInstance.getUserTimeline("Connect4GameBot",new Paging(1,1));

        return list.get(0);
    }

    public Twitter getTwitterInstance() {
        return twitterInstance;
    }
}
