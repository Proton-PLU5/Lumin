package me.protonplus.lumin.scenes;

import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import me.protonplus.lumin.events.Events;
import me.protonplus.lumin.scenes.stickycards.StickyCardScene;
import me.protonplus.lumin.util.StageManager;

import java.awt.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static me.protonplus.lumin.Lumin.isLuminHidden;

public class MenuScene extends LuminScene {

    public MenuScene(Group root, Stage owner) {
        super(root);
        this.setFill(Color.TRANSPARENT);
        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(5));
        gridPane.setVgap(5);
        gridPane.setStyle("-fx-background-color: #e8e5ea; -fx-background-radius: 10");
        gridPane.setPrefSize(30, 250);
        gridPane.setBackground(Background.EMPTY);

        addMenuButtons(gridPane);

        // Set column constraint to evenly distribute the columns
        RowConstraints rowConstraints = new RowConstraints();
        rowConstraints.setPercentHeight(100 / 5); // Divide the width into 3 equal parts
        gridPane.getRowConstraints().addAll(rowConstraints, rowConstraints, rowConstraints, rowConstraints, rowConstraints);

        // Fade in animation
        FadeTransition fadeIn = new FadeTransition(Duration.seconds(0.5), root);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        fadeIn.play();

        root.getChildren().add(gridPane);
        try {
            Platform.runLater(MainScene::closeTextStages);
        } catch (Exception e) {
            e.printStackTrace();
        }


        Events.mousePressedListerners.add((t) -> close());
    }

    private void addMenuButtons(GridPane pane) {
        Supplier<Rectangle> iconBackground = () -> {
            Rectangle rect = new Rectangle(48, 48);
            rect.setFill(Color.web("#d1cfe2"));
            rect.setArcWidth(10);
            rect.setArcHeight(10);
            return rect;
        };

        // Ask Icon
        ImageView chatView = new ImageView("me/protonplus/lumin/icons/ask.png");
        chatView.setFitHeight(32);
        chatView.setFitWidth(32);

        Rectangle chatRect = iconBackground.get();
        StackPane chatPane = new StackPane(chatRect, chatView);
        chatPane.setOnMouseClicked((e) -> {
            Stage dialogStage = new Stage();
            DialogScene dialogScene = new DialogScene(new Group());
            dialogStage.setScene(dialogScene);
            dialogStage.initStyle(StageStyle.TRANSPARENT);
            dialogStage.setX(dialogStage.getX()+70+60);
            dialogStage.setAlwaysOnTop(false);
            dialogStage.show();
            close();
        });
        pane.add(chatPane, 0, 0);

        // Sticky Card Icon
        ImageView stickyView = new ImageView("me/protonplus/lumin/icons/sticky_card.png");
        stickyView.setFitHeight(32);
        stickyView.setFitWidth(32);

        Rectangle stickyRect = iconBackground.get();

        StackPane stickyPane = new StackPane(stickyRect, stickyView);
        stickyPane.setOnMouseClicked((e) -> {
            Stage stickyStage = new Stage();
            StickyCardScene stickyScene = new StickyCardScene(new Group());
            stickyStage.setScene(stickyScene);
            stickyStage.initStyle(StageStyle.TRANSPARENT);
            stickyStage.setX(e.getScreenX());
            stickyStage.setY(e.getScreenY());
            stickyStage.initOwner(StageManager.getStage("main").get());
            stickyStage.setAlwaysOnTop(true);

            stickyStage.show();
            close();
        });
        pane.add(stickyPane, 0, 1);

        // Visibility icon
        ImageView visibilityView = new ImageView("me/protonplus/lumin/icons/eye.png");
        visibilityView.setFitHeight(32);
        visibilityView.setFitWidth(32);

        Consumer<Integer> visibilityConsumer = new Consumer() {
            @Override
            public void accept(Object o) {
                if (isLuminHidden) {
                    visibilityView.setImage(new Image("me/protonplus/lumin/icons/eye_crossed.png"));
                } else {
                    visibilityView.setImage(new Image("me/protonplus/lumin/icons/eye.png"));
                }
            }
        };

        Rectangle visibilityRect = iconBackground.get();

        visibilityConsumer.accept(0);
        StackPane visibilityPane = new StackPane(visibilityRect, visibilityView);
        visibilityPane.setOnMouseClicked(event -> {
            isLuminHidden = !isLuminHidden;
            MainScene.toggleVisibility(isLuminHidden);
            visibilityConsumer.accept(0);
        });
        pane.add(visibilityPane, 0, 2);

        // Settings Icon
        ImageView searchView = new ImageView("me/protonplus/lumin/icons/search.png");
        searchView.setFitHeight(32);
        searchView.setFitWidth(32);

        Rectangle searchRect = iconBackground.get();

        StackPane searchPane = new StackPane(searchRect, searchView);
        searchPane.setOnMouseClicked(event -> {
            Stage searchStage = new Stage();
            SearchScene settingsScene = new SearchScene(new Group());
            searchStage.setScene(settingsScene);
            searchStage.initStyle(StageStyle.TRANSPARENT);
            searchStage.initOwner(StageManager.getStage("main").get());
            searchStage.setAlwaysOnTop(true);
            close();
            searchStage.show();

            searchStage.setX(Toolkit.getDefaultToolkit().getScreenSize().getWidth()/2 - searchStage.getWidth()/2);
            searchStage.setY(100);
        });
        pane.add(searchPane, 0, 3);

        // Settings Icon
        ImageView settingsView = new ImageView("me/protonplus/lumin/icons/settings.png");
        settingsView.setFitHeight(32);
        settingsView.setFitWidth(32);

        Rectangle settingsRect = iconBackground.get();
        StackPane settingsPane = new StackPane(settingsRect, settingsView);
        settingsPane.setOnMouseClicked(event -> {
            Stage settingsStage = new Stage();
            SettingsScene settingsScene = new SettingsScene(new Group());
            settingsStage.setScene(settingsScene);
            settingsStage.initStyle(StageStyle.TRANSPARENT);
            settingsStage.initOwner(StageManager.getStage("main").get());
            settingsStage.setAlwaysOnTop(true);
            close();
            settingsStage.show();

        });

        pane.add(settingsPane, 0, 4);
    }
}
