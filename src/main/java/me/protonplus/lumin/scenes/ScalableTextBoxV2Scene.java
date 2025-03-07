package me.protonplus.lumin.scenes.textbox;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;
import me.protonplus.lumin.events.Events;
import me.protonplus.lumin.scenes.MainScene;
import me.protonplus.lumin.util.StageManager;

import java.util.Timer;
import java.util.TimerTask;

public class ScalableTextBoxV2Scene extends Scene {
    private int timeUntilExpiry = -1;
    private Timer timer;
    public ScalableTextBoxV2Scene(Group root, String text) {
        super(root);
        this.setFill(Color.TRANSPARENT);

        // Main Chat
        AnchorPane anchorPane = new AnchorPane();
        anchorPane.setPadding(new Insets(5));
        anchorPane.setStyle("-fx-background-color: #e8e5ea; -fx-background-radius: 10");
        anchorPane.setPrefWidth(400);
        anchorPane.setBackground(Background.EMPTY);

        // Create and configure the label
        Label label = new Label(text);
        label.setMaxWidth(395);
        label.setWrapText(true); // Allow text to wrap to the next line
        label.setStyle("-fx-font-size: 14; -fx-font-weight: normal;"); // Customize label style
        label.setTranslateY(5);
        label.setTranslateX(5);

        anchorPane.getChildren().add(label);
        anchorPane.setPrefWidth(label.getWidth()+5);
        anchorPane.setPrefHeight(label.getHeight()+10);

        // Fade in animation
        FadeTransition fadeIn = new FadeTransition(Duration.seconds(0.5), root);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();

        root.getChildren().add(anchorPane);
        // Events.onScalableTextBoxV2SceneCreated();
    }

    public ScalableTextBoxV2Scene(String text, boolean listenToMouseMove, boolean listenToMousePress) {
        this(new Group(), text);
        if (listenToMouseMove) {
            Events.mouseListeners.add((t) -> close());
        } else if (listenToMousePress) {Events.mousePressedListerners.add((t)-> close());}
    }

    public ScalableTextBoxV2Scene(String text, Integer timeUntilExpiry) {
        this(new Group(), text);
        this.timeUntilExpiry = timeUntilExpiry;
        timer = new Timer();
        timer.schedule(new ExpiryTask(), timeUntilExpiry * 1000);
    }
    public ScalableTextBoxV2Scene(String text, boolean listenToMouseMove, boolean listenToMousePress, Integer timeUntilExpiry) {
        this(text, listenToMouseMove, listenToMousePress);
        timer = new Timer();
        timer.schedule(new ExpiryTask(), timeUntilExpiry * 1000);
    }

    class ExpiryTask extends TimerTask {
        @Override
        public void run() {
            // Code to expire the stage (e.g., close it)
            close();
            timer.cancel(); // Cancel the timer
        }
    }

    public void close() {
        FadeTransition fadeTransition = new FadeTransition(Duration.seconds(0.5), this.getRoot());
        fadeTransition.setFromValue(1);
        fadeTransition.setInterpolator(Interpolator.EASE_BOTH);
        fadeTransition.setToValue(0);

        ParallelTransition parallelTransition = new ParallelTransition(fadeTransition);
        parallelTransition.setOnFinished((e) -> {
            Stage window = ((Stage)this.getWindow());
            StageManager.removeStage(window);
            try {
                ((MainScene) StageManager.getStage("main").get().getScene()).removeDialog(window);
            } catch (Exception exception) {
                exception.printStackTrace();
            }
            window.close();
        });
        parallelTransition.play();
    }
}
