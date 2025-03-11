package me.protonplus.lumin.scenes;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import me.protonplus.lumin.util.StageManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Consumer;

public class ButtonBoxScene extends Scene {
    private Timer timer;
    Group root;

    // Primary Constructor
    public ButtonBoxScene(Group root, String text, List<String> buttonNames, List<Consumer> buttonActions) {
        super(root);
        this.root = root;
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

        double labelHeight = (label.getHeight()+10);

        anchorPane.getChildren().add(label);
        anchorPane.setPrefWidth(label.getWidth()+5);
        anchorPane.setPrefHeight(labelHeight);

        // Create buttons
        AnchorPane buttons = new AnchorPane();

        for (int i = 0; i < buttonNames.size(); i++) {
            Label buttonLabel = new Label(buttonNames.get(i));
            buttonLabel.setTextFill(Color.WHITE);
            buttonLabel.applyCss();
            buttonLabel.layout();

            Text theText = new Text(buttonLabel.getText());
            theText.setFont(buttonLabel.getFont());
            double width = theText.getBoundsInLocal().getWidth();
            double height = theText.getBoundsInLocal().getHeight();

            javafx.scene.shape.Rectangle buttonBackground = new Rectangle(50, 30);
            buttonBackground.setFill(Color.web("#373F51"));
            buttonBackground.setArcHeight(15);
            buttonBackground.setArcWidth(15);
            buttonBackground.setWidth(width + 10);
            buttonBackground.setHeight(height + 10);

            StackPane button = new StackPane(buttonBackground, buttonLabel);

            int finalI = i;
            button.setOnMouseClicked((e) -> buttonActions.get(finalI).accept(null));
            buttons.getChildren().add(button);
        }

        Platform.runLater(() -> {
            buttons.setTranslateY(anchorPane.getBoundsInLocal().getHeight() + 5);
            this.getWindow().setHeight(this.getWindow().getHeight()+buttons.getBoundsInLocal().getHeight()+5);
        });

        anchorPane.getChildren().add(buttons);

        // Fade in animation
        FadeTransition fadeIn = new FadeTransition(Duration.seconds(0.5), root);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();

        root.getChildren().add(anchorPane);
    }


    public ButtonBoxScene(String text, Integer timeUntilExpiry, List<String> buttonNames, List<Consumer> buttonActions) {
        this(new Group(), text, buttonNames, buttonActions);
        timer = new Timer();
        timer.schedule(new ExpiryTask(), timeUntilExpiry * 1000);
    }

    public static void createExpirableButtonBox(String text, int expiry, ArrayList buttons, List<Consumer> consumers) {
        ButtonBoxScene buttonBoxScene = new ButtonBoxScene(text, expiry, buttons, consumers);
        Stage buttonBoxStage = new Stage();
        buttonBoxStage.setScene(buttonBoxScene);
        buttonBoxStage.initOwner(StageManager.getStage("main").get());
        buttonBoxStage.initStyle(StageStyle.TRANSPARENT);
        ((MainScene) StageManager.getStage("main").get().getScene()).addNewDialog(buttonBoxStage);
        buttonBoxStage.show();
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

    class ExpiryTask extends TimerTask {
        @Override
        public void run() {
            // Code to expire the stage (e.g., close it)
            close();
            timer.cancel(); // Cancel the timer
        }
    }
}
