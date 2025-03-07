package me.protonplus.lumin.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import me.protonplus.lumin.Lumin;


import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

public class LogoAnimationUtils {

    public static Emotion currentEmotion = Emotion.NORMAL;
    static ClassLoader classLoader = LogoAnimationUtils.class.getClassLoader();
    private static Timer timer;
    private static ImageView luminLogoLeftEye;
    private static ImageView luminLogoRightEye;
    public enum Emotion {
        NORMAL,
        HAPPY,
        SAD
    }

    public static void setEyeEmotion(Emotion Emotion, ImageView _luminLogoLeftEye, ImageView _luminLogoRightEye) {
        currentEmotion = Emotion;
        luminLogoLeftEye = _luminLogoLeftEye;
        luminLogoRightEye = _luminLogoRightEye;

        if (currentEmotion != LogoAnimationUtils.Emotion.NORMAL) {
            timer = new Timer();
            timer.schedule(new UpdateToNormalEmotion(), 30*1000);
        }

        Image luminEye = new Image("/me/protonplus/lumin/images/logo/LuminLogoEye" + currentEmotion.ordinal() + ".png");
        JsonParser parser = new JsonParser();
        String resourcePath = "/me/protonplus/lumin/images/meta/LuminLogoEye" + currentEmotion.ordinal() + ".json";
        try (InputStream inputStream = Lumin.class.getResourceAsStream(resourcePath);
             InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
             BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {

            JsonObject object = (JsonObject) parser.parse(bufferedReader);
            double fitWidth = (double) object.get("fitWidth").getAsDouble();
            double fitHeight = (double) object.get("fitHeight").getAsDouble();
            luminLogoLeftEye.setImage(luminEye);
            luminLogoLeftEye.setFitHeight(fitHeight);
            luminLogoLeftEye.setFitWidth(fitWidth);
            luminLogoRightEye.setImage(luminEye);
            luminLogoRightEye.setFitHeight(fitHeight);
            luminLogoRightEye.setFitWidth(fitWidth);
        } catch (IOException | JsonParseException e) {
            e.printStackTrace();
            Lumin.LOGGER.error("Couldn't load metadata for eye animation.");
        }
    }

    public static void setEyeEmotion(Emotion Emotion) {
        if (luminLogoLeftEye != null && luminLogoRightEye != null) {
            setEyeEmotion(Emotion, luminLogoLeftEye, luminLogoRightEye);
        }
    }

    static class UpdateToNormalEmotion extends TimerTask {
        @Override
        public void run() {
            setEyeEmotion(Emotion.NORMAL, luminLogoLeftEye, luminLogoRightEye);
            timer.cancel();
        }
    }
}
