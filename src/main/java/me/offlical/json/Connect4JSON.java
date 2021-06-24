package me.offlical.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import me.offlical.connect4.Connect4Game;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.util.LinkedHashMap;

public class Connect4JSON {

    private Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private Connect4Game game;
    private String path;

    public Connect4JSON(Connect4Game game, String path) {
        this.game = game;
        this.path = path;
    }

    public void saveGameState() throws IOException, ParseException {

        File file = new File(path + "game_data.json");

        JSONObject object = (JSONObject) new JSONParser().parse(new FileReader(file));

        LinkedHashMap<String,Object> values = new LinkedHashMap<String,Object>(3);

        values.put("turn",game.turn);
        values.put("currentPlayer", game.currentColor);
        values.put("grid",game.gridToJSONString());

        object.put("last_game_state",values);

        JsonParser parser = new JsonParser();
        JsonElement je = parser.parse(object.toJSONString());

        PrintWriter writer = new PrintWriter(file);
        writer.write(gson.toJson(je));
        writer.flush();
        writer.close();
    }

    public void clearGameState(int blueWins, int redWins, int gameNum) throws IOException, ParseException {

        File file = new File("game_data.json");

        JSONObject object = (JSONObject) new JSONParser().parse(new FileReader(file));

        object.put("blue_wins",blueWins);
        object.put("red_wins",redWins);
        object.put("games_count",gameNum);
        object.put("last_game_state",null);

        JsonParser parser = new JsonParser();
        JsonElement je = parser.parse(object.toJSONString());

        PrintWriter writer = new PrintWriter(file);
        writer.write(gson.toJson(je));
        writer.flush();
        writer.close();
    }

}
