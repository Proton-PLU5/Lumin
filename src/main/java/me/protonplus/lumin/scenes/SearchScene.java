package me.protonplus.lumin.scenes;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import me.protonplus.lumin.events.Events;
import me.protonplus.lumin.scenes.stickycards.StickyCardScene;
import me.protonplus.lumin.util.StageManager;
import me.protonplus.lumin.util.stickycards.StickyCardManager;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

public class SearchScene extends LuminScene {
    private int typeWriterAnimationFrames = 0;
    private int currentIndex = 0;
    private boolean defaultTextRemoved = false;
    private GridPane optionsPane;
    private String textToType = "What do you want to find?";

    class SearchOptionPair {
        public String name;
        public Runnable action;

        public SearchOptionPair(String name, Runnable action) {
            this.name = name;
            this.action = action;
        }
    }

    private List<SearchOptionPair> optionPairs = Arrays.asList(
            new SearchOptionPair("Create a new Sticky Card", () -> openScene(StickyCardScene.class)),
            new SearchOptionPair("Open settings", () -> openScene(SettingsScene.class)),
            new SearchOptionPair("Ask Lumin", () -> openScene(DialogScene.class)),
            new SearchOptionPair("Check the weather", () -> openScene(WeatherScene.class)),
            new SearchOptionPair("Load sticky cards", StickyCardManager::loadStickyNotes)
    );

    public SearchScene(Group root) {
        super(root);
        this.setFill(Color.TRANSPARENT);
        AnchorPane anchorPane = new AnchorPane();
        anchorPane.setPrefHeight(40);
        anchorPane.setPrefWidth(400);
        anchorPane.setStyle("-fx-background-color: #e8e5ea; -fx-background-radius: 10");

        DropShadow outerShadow = new DropShadow();
        outerShadow.setRadius(-10.0);
        outerShadow.setColor(Color.BLACK);
        anchorPane.setEffect(outerShadow);

        TextField searchTextField = new TextField("");
        searchTextField.setPrefWidth(390);
        searchTextField.setBackground(Background.EMPTY);
        searchTextField.setFont(Font.font("Arial", FontWeight.NORMAL, 16));

        AnimationTimer timer = createTypeWriterAnimation(searchTextField);
        timer.start();
        searchTextField.setOnKeyTyped(event -> {
            if (!defaultTextRemoved) {
                defaultTextRemoved = true;
                searchTextField.setText(event.getText());
                searchTextField.requestFocus();
                timer.stop();
            }
            this.showOptions(searchTextField);
        });

        optionsPane = new GridPane();
        optionsPane.setPrefWidth(400);
        optionsPane.setVgap(5);
        optionsPane.setTranslateY(45);
        Platform.runLater(() -> {
            searchTextField.setTranslateY(40 / 2 - searchTextField.getHeight() / 2);
        });

        anchorPane.getChildren().add(searchTextField);
        root.getChildren().add(anchorPane);
        root.getChildren().add(optionsPane);
        Events.luminDraggedListeners.add((e) -> close());
    }

    private void showOptions(TextField textField) {
        optionsPane.getChildren().clear();
        Supplier<Rectangle> createOptionRectangle = () -> {
            Rectangle rectangle = new Rectangle(396, 36);
            rectangle.setFill(Color.web("#E3E3E3"));
            rectangle.setArcHeight(10);
            rectangle.setArcWidth(10);
            return rectangle;
        };

        if (!Objects.equals(textField.getText(), "")) {
            for (SearchOptionPair optionPair : optionPairs) {
                if (optionPair.name.toLowerCase().contains(textField.getText().toLowerCase())) {
                    Rectangle rectangle = createOptionRectangle.get();
                    rectangle.setStrokeWidth(2);
                    rectangle.setStroke(Color.BLACK);
                    Label label = new Label(optionPair.name);
                    label.setTranslateX(10);
                    label.setFont(Font.font("Arial", FontWeight.NORMAL, 16));
                    StackPane pane = new StackPane(rectangle, label);
                    pane.setAlignment(Pos.CENTER_LEFT);
                    pane.setOnMouseClicked(event -> {
                        optionPair.action.run();
                        close();
                    });
                    optionsPane.add(pane, 0, optionsPane.getRowCount());
                }
            }
        }

        Platform.runLater(() -> {
            this.getWindow().setHeight(45 + optionsPane.getHeight());
        });
        Events.luminDraggedListeners.add((t) -> close());
    }

    private void openScene(Class<? extends Scene> sceneClass) {
        try {
            Constructor<? extends Scene> intArgConstructor = sceneClass.getConstructor(Group.class);
            Scene scene = intArgConstructor.newInstance(new Group());
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.initStyle(StageStyle.TRANSPARENT);
            stage.initOwner(StageManager.getStage("root").get());
            stage.setAlwaysOnTop(true);
            stage.show();
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    private AnimationTimer createTypeWriterAnimation(TextField typeWriterLabel) {
        return new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (typeWriterAnimationFrames > 0) {
                    typeWriterAnimationFrames--;
                } else if (currentIndex < textToType.length()) {
                    char nextChar = textToType.charAt(currentIndex);
                    typeWriterLabel.setText(typeWriterLabel.getText() + nextChar);
                    currentIndex++;
                    typeWriterAnimationFrames = 1; // Adjust the frame delay as needed
                } else {
                    stop();
                    typeWriterLabel.requestFocus();
                }
            }
        };
    }
}