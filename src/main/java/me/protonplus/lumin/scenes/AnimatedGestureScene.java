package me.protonplus.lumin.scenes;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import me.protonplus.lumin.events.Events;
import me.protonplus.lumin.util.StageManager;

import java.util.Timer;
import java.util.TimerTask;

public class AnimatedGestureScene extends Scene {
    private Timer timer;
    public AnimatedGestureScene(Group root, String animationLocation, int timeUntilExpiry) {
        super(root);
        this.setFill(Color.TRANSPARENT);

        // Main
        StackPane stackPane = new StackPane();
        stackPane.setPadding(new Insets(5));
        stackPane.setStyle("-fx-background-color: #d1cfe2; -fx-background-radius: 15");

        // Animated Image
        ImageView animatedImage = new ImageView(this.getClass().getResource(animationLocation).toExternalForm());
        animatedImage.setFitWidth(50);
        animatedImage.setFitHeight(50);

        stackPane.getChildren().add(animatedImage);

        // Fade in animation
        FadeTransition fadeIn = new FadeTransition(Duration.seconds(0.5), root);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();

        root.getChildren().add(stackPane);
        timer = new Timer();
        timer.schedule(new ExpiryTask(), timeUntilExpiry * 1000L);
        Events.pressListeners.add((p) -> close());
        Events.luminDraggedListeners.add((p) -> close());
    }

    public static void createExpirableAnimatedGesture(String location, int expiry) {
        Platform.runLater(() -> {
            Stage animatedGestureStage = new Stage();
            AnimatedGestureScene animatedGestureScene = new AnimatedGestureScene(new Group(), location, expiry);
            animatedGestureStage.setScene(animatedGestureScene);
            animatedGestureStage.setAlwaysOnTop(true);
            animatedGestureStage.initStyle(StageStyle.TRANSPARENT);
            animatedGestureStage.initOwner(StageManager.getStage("main").get());
            animatedGestureStage.show();
            ((MainScene) StageManager.getStage("main").get().getScene()).addNewDialog(animatedGestureStage);
        });
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
