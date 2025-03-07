package me.protonplus.lumin.util;

import javafx.stage.Stage;

import java.util.*;

public class StageManager {

    private static Collection<Map.Entry<String, Stage>> stages = new ArrayList<>();

    public static void setStage(String name, Stage stage) {
        if (stages.stream().noneMatch((stringStageEntry -> {
            if (Objects.equals(stringStageEntry.getKey(), name)) {return true;}
            else return false;
        }))) {
            StageManager.stages.add(new AbstractMap.SimpleEntry<>(name, stage));
        }
    }

    public static Optional<Stage> getStage(String name) {
        return stages.stream().filter((stringStageEntry) -> {
            return Objects.equals(stringStageEntry.getKey(), name);
        }).findFirst().map(Map.Entry::getValue);
    }
}
