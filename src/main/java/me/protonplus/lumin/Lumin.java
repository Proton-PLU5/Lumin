package me.protonplus.lumin;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import me.protonplus.lumin.events.Events;
import me.protonplus.lumin.scenes.AnimatedGestureScene;
import me.protonplus.lumin.scenes.MainScene;
import me.protonplus.lumin.scenes.ScalableTextBoxV2Scene;
import me.protonplus.lumin.util.StageManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Lumin extends Application {

    public static final Logger LOGGER = LogManager.getLogger(Lumin.class);
    private MainScene scene;
    private Group root;

    private double lerp(double start, double end, double t) {
        return start + t * (end - start);
    }

    @Override
    public void start(Stage primaryStage) {
        Lumin.LOGGER.info("Starting Lumin!");

        primaryStage.setTitle("Lumin");
        primaryStage.initStyle(StageStyle.UTILITY);
        primaryStage.setOpacity(0);
        primaryStage.setHeight(0);
        primaryStage.setWidth(0);
        primaryStage.show();

        Stage mainStage = new Stage(StageStyle.TRANSPARENT);
        StageManager.setStage("main", mainStage);

        Group root = new Group();
        MainScene scene = new MainScene(root);
        scene.setFill(Color.TRANSPARENT);

        mainStage.initOwner(primaryStage);
        mainStage.setTitle("Lumin");
        mainStage.setScene(scene);
        mainStage.setAlwaysOnTop(true);
        mainStage.show();
    }

    public static void launchApplication() {
        Lumin.launch();
    }

    public static void showStartMessage(Stage mainStage, int expire) {
        try {
            String resourcePath = "/me/protonplus/lumin/data/startup_messages.txt";
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(Lumin.class.getResourceAsStream(resourcePath)));
            List<String> lines = new ArrayList<>();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                lines.add(line);
            }
            Random random = new Random();
            String startupText = lines.get(random.nextInt(lines.size()));
            Stage animatedGestureStage = new Stage();
            AnimatedGestureScene animatedGestureScene = new AnimatedGestureScene(new Group(), "/me/protonplus/lumin/images/animated/wave.gif", expire);
            animatedGestureStage.setScene(animatedGestureScene);
            animatedGestureStage.setAlwaysOnTop(true);
            animatedGestureStage.initStyle(StageStyle.TRANSPARENT);
            animatedGestureStage.initOwner(mainStage);
            animatedGestureStage.show();
            Events.luminDraggedListeners.add((t) -> animatedGestureScene.close());
            Events.pressListeners.add((t) -> animatedGestureScene.close());

            Stage scalableTextStage = new Stage();
            ScalableTextBoxV2Scene scalableTextBoxScene = new ScalableTextBoxV2Scene(startupText, expire);
            scalableTextBoxScene.setFill(Color.TRANSPARENT);
            scalableTextStage.setAlwaysOnTop(true);
            scalableTextStage.initStyle(StageStyle.TRANSPARENT);
            scalableTextStage.initOwner(mainStage);
            scalableTextStage.setScene(scalableTextBoxScene);
            scalableTextStage.show();

            ((MainScene) mainStage.getScene()).addNewDialog(animatedGestureStage);
            Platform.runLater(() -> {
                ((MainScene) mainStage.getScene()).addNewDialog(scalableTextStage);
            });

        } catch (IOException e) {
            Lumin.LOGGER.warn("Couldn't display start message.");
        }
    }
}
