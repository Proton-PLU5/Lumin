package me.protonplus.lumin.scenes;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Duration;
import me.protonplus.lumin.events.Events;
import me.protonplus.lumin.util.StageManager;

public class LuminScene extends Scene {
    public LuminScene(Group group) {
        super(group);
        synchronized (Events.luminDraggedListeners) {
            Events.luminDraggedListeners.add(e -> {
                this.close();
            });
        }
        synchronized (Events.pressListeners) {
            Events.pressListeners.add(e -> {
                this.close();
            });
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
