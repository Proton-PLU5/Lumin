package me.protonplus.lumin.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.application.Platform;
import me.protonplus.lumin.Lumin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class LuminOperations {
    protected static String luminInitialContext = "You are a Personal Desktop Assistant called Lumin. You are currently" +
            " only on the Windows Platform. As Lumin, your purpose is to help people who are not tech savvy to use their" +
            " computers. Here is the user's prompt: ";

    public static String explain(String explainText) {
        String prompt = "Explain what the user wants explained to your very best ability. Here is the user's question: "
                + explainText;
        String intent = getAIResponse(luminInitialContext + prompt);
        assert intent != null;
        return intent;
    }

    public static String conversation(String conversationText) {
        String prompt = "You are in conversation mode, So keep it simple and short and do not use any symbols or " +
                "characters that are outside the UTF-8 encoding. You are allowed to sound casual to better suite the" +
                " user. Here is the user's prompt: ";
        String intent = getAIResponse(luminInitialContext+prompt+conversationText);
        assert intent != null;
        return intent;
    }

    public static void generateWithoutBlocking(String text, Consumer<String> consumer, Boolean casual) {
        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> conversation(text));

        future.thenAccept((result) -> {
            Platform.runLater(()->consumer.accept(result));
        });
    }

    public static String getAIResponse(String prompt) {
        try {
            String apiURL = "https://luminrestapi.onrender.com/generate?prompt=" + URLEncoder.encode(prompt);
            // Create a URL object
            URL url = new URL(apiURL);
            Lumin.LOGGER.info("Attempting to communicate with the Lumin REST API for LLM access.");
            // Open a connection to the URL
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("GET");

            // Get the response code
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // Get the output from the LLM
                return parseOutputJson(connection);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    // The generated text is returned in JSON format, this function extracts the output field from the JSON.
    private static String parseOutputJson(HttpURLConnection connection) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;

        // Read the response line by line
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();

        String jsonResponse = response.toString();

        // Parse the JSON string
        JsonParser parser = new JsonParser();
        JsonObject jsonObject = (JsonObject) parser.parse(jsonResponse);

        // Get the "output" field from the JSON object
        String output = jsonObject.get("output").getAsString();
        return output;
    }
}
