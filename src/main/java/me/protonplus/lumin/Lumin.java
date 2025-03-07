package me.protonplus.lumin;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import me.protonplus.lumin.scenes.MainScene;
import me.protonplus.lumin.util.StageManager;

public class Lumin extends Application {

    private Scene scene;
    private Group root;

    public static void main(String[] args) {
        launch(args);
    }

    private double lerp(double start, double end, double t) {
        return start + t * (end - start);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        StageManager.setStage("main", primaryStage);

        root = new Group();
        scene = new MainScene(root);
        scene.setFill(Color.TRANSPARENT);

        primaryStage.initStyle(StageStyle.TRANSPARENT);
        primaryStage.setTitle("Lumin");

        primaryStage.setScene(scene);
        primaryStage.setAlwaysOnTop(true);
        primaryStage.show();
    }

    public static void launchApplication() {
        Lumin.launch();
    }

    private void addNewScene() {
        StackPane sceneContent = new StackPane();
        sceneContent.getChildren().add(new Button("New Scene Button"));

        Scene newScene = new Scene(sceneContent, 200, 150);
        newScene.setFill(Color.TRANSPARENT);

        Stage newStage = new Stage();

        newStage.setTitle("New Scene");
        newStage.setScene(newScene);
        newStage.show();
    }
}
