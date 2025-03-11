package me.protonplus.lumin.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class LoadEnviromentalVariables {

    public static String getVariable(String variable) {
        File environmentFile = new File(
                System.getProperty("user.home"), ".lumin/.env");
        if (environmentFile.exists()) {
            try (JsonReader reader = new JsonReader(new FileReader(environmentFile))) {
                JsonObject jsonData = (JsonObject) JsonParser.parseReader(reader);
                return jsonData.get(variable).getAsString();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            return null;
        }
    }
}
