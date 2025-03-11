package me.protonplus.lumin.events;

import javafx.application.Platform;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class Events {
    public static final List<Consumer<Integer>> pressListeners = new ArrayList<>();
    public static List<Consumer<Integer>> mouseListeners = new ArrayList<>();
    public static List<Consumer<Integer>> mousePressedListerners = new ArrayList<>();
    public static final List<Consumer<Integer>> luminDraggedListeners = new ArrayList<>();

    public static void onLuminPressed() {
        Platform.runLater(() -> {
            synchronized (pressListeners) {
                try {
                    for (Consumer<Integer> func: pressListeners) {
                        func.accept(0);
                        pressListeners.remove(func);
                    }
                } catch (Exception ignored) {}
            }
        });
    }

    public static void onLuminDragged() {
        Platform.runLater(() -> {
            synchronized (luminDraggedListeners) {
                try {
                    for (Consumer<Integer> func: luminDraggedListeners) {
                        func.accept(0);
                        luminDraggedListeners.remove(func);
                    }
                } catch (Exception ignored) {}
            }
        });
    }

}
