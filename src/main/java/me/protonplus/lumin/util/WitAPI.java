package me.protonplus.lumin.util;

import com.clivern.wit.Wit;
import com.clivern.wit.api.Message;
import com.clivern.wit.api.endpoint.MessageEndpoint;
import com.clivern.wit.exception.DataNotFound;
import com.clivern.wit.exception.DataNotValid;
import com.clivern.wit.util.Config;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.util.Pair;
import me.protonplus.lumin.util.voice.VoiceRecognition;

import java.io.IOException;

/*
    * This class is used to interact with the Wit API.
    * For intent recognition.
*/
public class WitAPI {
    private static Wit wit;

    public static enum INTENTS {
        GREETINGS,
        EXPLAIN,
        WEATHER
    }

    public static void initializeWit() {
        VoiceRecognition.LOGGER.info("Wit API Loading.");

        Config config = new Config();
        config.set("wit_api_id", LoadEnviromentalVariables.getVariable("WIT_API_ID"));
        config.set("wit_access_token", LoadEnviromentalVariables.getVariable("WIT_ACCESS_TOKEN"));
        config.set("logging_level", "info");

        wit = new Wit(config);

        VoiceRecognition.LOGGER.info("Wit API Loaded!");
    }

    public static Pair<INTENTS, String> getIntent(String msg) throws DataNotFound, DataNotValid, IOException {
        Message message = new Message(MessageEndpoint.GET);
        message.setQ(msg);

        String result = "";
        if (wit.send(message)) {
            result = wit.getResponse();
        } else {
            System.out.println(wit.getError());
        }

        // Process the JSON response
        JsonObject responseJson = (JsonObject) JsonParser.parseString(result);
        JsonObject entities = (JsonObject) responseJson.get("entities");

        String additionalInfo = "";

        System.out.println(result);
        String intent = entities.get("intent").getAsJsonArray().get(0).getAsJsonObject().get("value").getAsString();

        return switch (intent) {
            case "greeting" -> new Pair<>(INTENTS.GREETINGS, "");
            case "explain" -> new Pair<>(INTENTS.EXPLAIN, additionalInfo);
            case "get_weather" -> new Pair<>(INTENTS.WEATHER, additionalInfo);
            default -> null;
        };
    }

    public static void main(String[] args) throws DataNotFound, DataNotValid, IOException {
        initializeWit();
        Pair<INTENTS, String> pair = getIntent("Explain white holes.");
        System.out.println(pair.getKey().name());
        System.out.println(pair.getValue());
    }

}
