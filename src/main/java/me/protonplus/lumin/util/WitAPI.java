package me.protonplus.lumin.util;

import com.clivern.wit.Wit;
import com.clivern.wit.api.Message;
import com.clivern.wit.api.endpoint.MessageEndpoint;
import com.clivern.wit.exception.DataNotFound;
import com.clivern.wit.exception.DataNotValid;
import com.clivern.wit.util.Config;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.util.Pair;
import me.protonplus.lumin.util.voice.VoiceRecognition;

import java.io.IOException;
import java.util.Iterator;

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
        config.set("wit_api_id", System.getenv("wit_api_id"));
        config.set("wit_access_token", System.getenv("wit_access_token"));
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

        String highestConfidenceIntent = null;
        double confidence = 0;
        String additionalInfo = "";

        // Determine which intent has the highest confidence.
        for (Iterator<String> it = entities.keySet().iterator(); it.hasNext(); ) {
            String key = it.next();
            JsonObject innerObject = (JsonObject) ((JsonArray) entities.get(key)).get(0);
            if (highestConfidenceIntent == null) {
                highestConfidenceIntent = key;
                confidence = innerObject.get("confidence").getAsDouble();

                // Preprocessing
                // Extract information from intents that provide them
                // ...
                if (innerObject.has("value")) {
                    additionalInfo = innerObject.get("value").getAsString();
                }
            } else if (innerObject.get("confidence").getAsDouble() > confidence) {
                confidence = innerObject.get("confidence").getAsDouble();
                highestConfidenceIntent = key;

                // Preprocessing
                // Extract information from intents that provide them
                // ...
                if (innerObject.has("value")) {
                    additionalInfo = innerObject.get("value").getAsString();
                }
            }
        }


        System.out.println(result);




        return switch (highestConfidenceIntent) {
            case "greetings" -> new Pair<>(INTENTS.GREETINGS, "");
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
