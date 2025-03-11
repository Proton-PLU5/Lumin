package me.protonplus.lumin.util;

import javafx.stage.Stage;
import me.protonplus.lumin.Lumin;

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

    public static boolean containsStage(String name) {
        return stages.stream().anyMatch((stringStageEntry -> {
            return Objects.equals(stringStageEntry.getKey(), name);
        }));
    }

    public static Optional<Stage> getStage(String name) {
        return stages.stream().filter((stringStageEntry) -> {
            return Objects.equals(stringStageEntry.getKey(), name);
        }).findFirst().map(Map.Entry::getValue);
    }

    public static void removeStage(Stage stage) {
        Iterator<Map.Entry<String, Stage>> iterator = stages.iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Stage> entry = iterator.next();
            if (Objects.equals(entry.getValue(), stage)) {
                iterator.remove();
                Lumin.LOGGER.info("Removed Stage: " + entry.getValue().getScene().getClass().getName());
                return;
            }
        }
    }

    public static void closeStage(String weather) {
        stages.stream().filter((stringStageEntry) ->
                Objects.equals(stringStageEntry.getKey(), weather))
                .findFirst().ifPresent((stringStageEntry) -> {
            stringStageEntry.getValue().close();
        });
    }
}
