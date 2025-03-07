package me.protonplus.lumin.events;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class Events {
    public static List<Consumer<Integer>> pressListeners = new ArrayList<>();
    public static List<Consumer<Integer>> mouseListeners = new ArrayList<>();
    public static List<Consumer<Integer>> mousePressedListerners = new ArrayList<>();
    public static List<Consumer<Integer>> luminDraggedListeners = new ArrayList<>();
    public static List<Consumer<Integer>> scalableTextBoxV2SceneCreatedListeners = new ArrayList<>();

    public static void onLuminPressed() {
        for (Consumer<Integer> func: pressListeners) {
            try {
                func.accept(0);
            } catch (Exception ignored) {}
            pressListeners.remove(func);
        }
    }

    public static void onMouseMoved() {
        for (Consumer<Integer> func: mouseListeners) {
            try {
                func.accept(0);
            } catch (Exception ignored) {}
            mouseListeners.remove(func);
        }
    }

    public static void onMouseClicked() {
        for (Consumer<Integer> func: mousePressedListerners) {
            try {
                func.accept(0);
            } catch (Exception ignored) {}
            mousePressedListerners.remove(func);
        }
    }

    public static void onLuminDragged() {
        for (Consumer<Integer> func: luminDraggedListeners) {
            try {
                func.accept(0);
            } catch (Exception ignored) {}
            luminDraggedListeners.remove(func);
        }
    }

    public static void onScalableTextBoxV2SceneCreated() {
        for (Consumer<Integer> func: scalableTextBoxV2SceneCreatedListeners) {
            try {
                func.accept(0);
            } catch (Exception ignored) {}
            scalableTextBoxV2SceneCreatedListeners.remove(func);
        }
    }
}
