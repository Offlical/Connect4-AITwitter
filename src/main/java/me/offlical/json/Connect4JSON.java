package me.offlical.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import me.offlical.Connect4Bot;
import me.offlical.connect4.Connect4Game;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.util.LinkedHashMap;

public class Connect4JSON {

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final String path;

    private final File gameData;

    public Connect4JSON(String path) {
        this.path = path;
        this.gameData = new File(path + "game_data.json");
    }

    /**
     * Saves the current game state of the board to the JSON file
     *
     * @param game - Game to save the current state of the board of
     * @throws IOException    - If it fails writing to the file, or other IOExceptions
     * @throws ParseException - If it fails to parse the JSON from the file
     */
    public void saveGameState(Connect4Game game) throws IOException, ParseException {

        File file = new File(path + "game_data.json");

        JSONObject object = (JSONObject) new JSONParser().parse(new FileReader(file));

        LinkedHashMap<String, Object> values = new LinkedHashMap<String, Object>(3);

        values.put("turn", game.turn);
        values.put("currentPlayer", game.currentColor);
        values.put("grid", game.gridToJSONString());

        object.put("last_game_state", values);

        JsonParser parser = new JsonParser();
        JsonElement je = parser.parse(object.toJSONString());

        PrintWriter writer = new PrintWriter(file);
        writer.write(gson.toJson(je));
        writer.flush();
        writer.close();
    }

    /**
     * Updates the stats in the JSON file
     *
     * @param gameNum  - # of the game
     * @param blueWins - Number of wins for the blue "team"
     * @param redWins  - Number of wins for the red "team"
     * @throws IOException    - If it fails writing to the file, or other IOExceptions
     * @throws ParseException - If it fails to parse the JSON from the file
     */
    public void updateStats(int gameNum, int blueWins, int redWins) throws IOException, ParseException {

        JSONObject object = (JSONObject) new JSONParser().parse(new FileReader(gameData));

        object.put("blue_wins", blueWins);
        object.put("red_wins", redWins);
        object.put("games_count", gameNum);

        JsonParser parser = new JsonParser();
        JsonElement je = parser.parse(object.toJSONString());

        PrintWriter writer = new PrintWriter(gameData);
        writer.write(gson.toJson(je));
        writer.flush();
        writer.close();
    }

    /**
     * Clears the game state saved in the JSON file, used at the end of "game rounds"
     *
     * @throws IOException    -  If it fails writing to the file, or not finding it
     * @throws ParseException - - If it fails to parse the JSON from the file
     */
    public void clearGameState() throws IOException, ParseException {

        JSONObject object = (JSONObject) new JSONParser().parse(new FileReader(gameData));

        object.put("last_game_state", null);

        JsonParser parser = new JsonParser();
        JsonElement je = parser.parse(object.toJSONString());

        PrintWriter writer = new PrintWriter(gameData);
        writer.write(gson.toJson(je));
        writer.flush();
        writer.close();
    }

    public int[] readStats() throws IOException, ParseException {
        JSONObject object = (JSONObject) new JSONParser().parse(new FileReader(gameData));

        int blueWins, redWins, gameNum;

        // java is stupid and for some god awful reason i need to put these into long variables before casting it into an int, god knows why
        long blueW = (long) object.getOrDefault("blue_wins", 0);
        long redW = (long) object.getOrDefault("red_wins", 0);
        long gNum = (long) object.getOrDefault("games_count", 1);

        gameNum = (int)gNum;
        redWins = (int)redW;
        blueWins = (int)blueW;

        return new int[]{blueWins, redWins, gameNum};
    }


    public void readAndApplyGameState(Connect4Game game) throws IOException, ParseException {
        JSONObject object = (JSONObject) new JSONParser().parse(new FileReader(gameData));
        if (object.get("last_game_state") != null) {
            System.out.println("Loading game from file...");
            game.fromJSON((JSONObject) object.get("last_game_state"));
            System.out.println("Loaded successfully!");
        }
    }

}
