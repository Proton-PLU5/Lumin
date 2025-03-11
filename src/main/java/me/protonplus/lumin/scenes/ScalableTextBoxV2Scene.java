package me.protonplus.lumin.scenes;

import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Group;
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
import me.protonplus.lumin.events.Events;
import me.protonplus.lumin.util.StageManager;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Consumer;

public class ScalableTextBoxV2Scene extends LuminScene {
    private Timer timer;
    Group root;
    // Primary Constructor
    public ScalableTextBoxV2Scene(Group root, String text) {
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
        timer = new Timer();
        timer.schedule(new ExpiryTask(), timeUntilExpiry * 1000);
    }

    public ScalableTextBoxV2Scene(String text, boolean listenToMouseMove, boolean listenToMousePress, Integer timeUntilExpiry) {
        this(text, listenToMouseMove, listenToMousePress);
        timer = new Timer();
        timer.schedule(new ExpiryTask(), timeUntilExpiry * 1000);
    }

    // Quick access method
    public static void createExpirableTextBox(String response, int expiry) {
        Platform.runLater(() -> {
            ScalableTextBoxV2Scene scalableTextBoxV2Scene = new ScalableTextBoxV2Scene(response, expiry);
            Stage scalableTextBoxStage = new Stage();
            scalableTextBoxStage.setScene(scalableTextBoxV2Scene);
            scalableTextBoxStage.initOwner(StageManager.getStage("main").get());
            scalableTextBoxStage.initStyle(StageStyle.TRANSPARENT);

            scalableTextBoxStage.show();
            ((MainScene) StageManager.getStage("main").get().getScene()).addNewDialog(scalableTextBoxStage);


        });
    }

    // Function to add interactivity to the scene
    public static AnchorPane createInteractibleButtons(List<String> buttonNames, List<Consumer> buttonActions) {
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

        return buttons;
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
