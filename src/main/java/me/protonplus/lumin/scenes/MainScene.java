package me.protonplus.lumin.scenes;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.media.AudioClip;
import javafx.stage.Stage;
import javafx.util.Duration;
import me.protonplus.lumin.util.StageManager;

public class MainScene extends Scene {
    private double targetX, targetY;
    private double endX, endY;
    private boolean isDragging;

    public MainScene(Group root) {
        super(root);

        // Load the image from the resources folder
        String imagePath = "/me/protonplus/lumin/image.png";
        Image image = new Image(imagePath);
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(60);
        imageView.setFitHeight(60);
        StackPane stackPane = new StackPane(imageView);

        this.setOnKeyPressed(this::handleKeyPressed);
        this.setOnMousePressed(this::handleMousePressed);
        this.setOnMouseReleased(this::handleMouseReleased);
        this.setOnMouseDragged(this::handleMouseDrag);

        root.getChildren().add(stackPane);

        // Scale in animation
        ScaleTransition scaleIn = new ScaleTransition(Duration.seconds(0.25), imageView);
        scaleIn.setFromX(0);
        scaleIn.setFromY(0);
        scaleIn.setToX(1);
        scaleIn.setToY(1);
        scaleIn.setInterpolator(Interpolator.EASE_BOTH);

        scaleIn.setOnFinished(event -> {
            imageView.setOnMouseClicked(this::handleMouseClicked);
        });

        // Fade in animation
        FadeTransition fadeIn = new FadeTransition(Duration.seconds(0.5), imageView);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        ParallelTransition introAnimation = new ParallelTransition(scaleIn, fadeIn);
        introAnimation.play();

        Platform.runLater(() -> {
            Stage stage = StageManager.getStage("main").get();
            endX = stage.getX();
            endY = stage.getY();
        });


        AnimationTimer animationTimer = new AnimationTimer() {
            Stage stage = StageManager.getStage("main").get();
            @Override
            public void handle(long now) {
                if (isDragging) {

                    // Update the window position
                    double currentX = stage.getX();
                    double currentY = stage.getY();

                    double x = lerp(currentX, targetX, 0.1);
                    double y = lerp(currentY, targetY, 0.1);

                    stage.setX(x);
                    stage.setY(y);
                } else {
                    // Update the window position
                    double currentX = stage.getX();
                    double currentY = stage.getY();

                    double x = lerp(currentX, endX, 0.1);
                    double y = lerp(currentY, endY, 0.1);
                    stage.setX(x);
                    stage.setY(y);
                }
            }
        };
        animationTimer.start();
    }

    private double lerp(double start, double end, double t) {
        return start + t * (end - start);
    }

    private void handleMousePressed(MouseEvent event) {
        this.isDragging = true;
        targetX = event.getScreenX() - this.getWidth()/2;
        targetY = event.getScreenY() - this.getHeight()/2;
    }

    private void handleMouseReleased(MouseEvent event) {
        this.isDragging = false;
        endX = event.getScreenX() - this.getWidth()/2;
        endY = event.getScreenY() - this.getHeight()/2;
    }

    private void handleMouseClicked(MouseEvent event) {
        if (!event.isStillSincePress()) return;
        ScaleTransition scaleDown = new ScaleTransition(Duration.seconds(0.1), (ImageView) event.getSource());
        scaleDown.setToX(0.75);
        scaleDown.setToY(0.75);
        scaleDown.setAutoReverse(true);

        ScaleTransition scaleUp = new ScaleTransition(Duration.seconds(0.1), (ImageView) event.getSource());
        scaleUp.setToX(1);
        scaleUp.setToY(1);
        scaleUp.setAutoReverse(true);

        scaleDown.play();
        scaleDown.setOnFinished(e -> scaleUp.play());

        // Play soundeffect.
        String openMenuSoundEffect = "/me/protonplus/lumin/sounds/melancholy_ui_chime_pixabay.mp3";
        AudioClip media = new AudioClip(getClass().getResource(openMenuSoundEffect).toString());
        media.play();

        Stage stage = StageManager.getStage("main").get();
        double startX = stage.getX();
        double targetX = startX + this.getWidth() + 10; // Move the stage to the right

        DoubleProperty xProperty = new SimpleDoubleProperty(startX);
        KeyValue keyValue = new KeyValue(xProperty, targetX);

        Duration duration = Duration.seconds(0.1);
        KeyFrame keyFrame = new KeyFrame(duration, keyValue);

        Timeline timeline = new Timeline(keyFrame);
        timeline.play();
        xProperty.addListener((observable, oldValue, newValue) -> {
            stage.setX(newValue.doubleValue());
            endX = newValue.doubleValue();
        });
    }

    private void handleMouseDrag(MouseEvent event) {
        targetX = event.getScreenX() - this.getWidth()/2;
        targetY = event.getScreenY() - this.getHeight()/2;

    }

    private void handleKeyPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.ESCAPE) {
            Stage primaryStage = (Stage) ((Scene) event.getSource()).getWindow();
            primaryStage.close();
        }
    }
}
