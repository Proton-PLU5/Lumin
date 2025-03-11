package me.protonplus.lumin.util.stickycards;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import javafx.scene.Group;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import me.protonplus.lumin.Lumin;
import me.protonplus.lumin.scenes.stickycards.StickyCardScene;
import me.protonplus.lumin.util.StageManager;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

public class StickyCardManager {

    public static void createNessesaryDirectories() {
        File stickycard_dir = new File(
                System.getProperty("user.home"), ".lumin/stickycards/");
        File stickycard_img_dir = new File(
                System.getProperty("user.home"), ".lumin/stickycards_images/");
        stickycard_dir.mkdirs();
        stickycard_img_dir.mkdirs();
    }

    public static void loadStickyNotes() {
        File directory = new File(
                System.getProperty("user.home"), ".lumin/stickycards/");

        // Check if the directory exists
        if (!directory.exists() || !directory.isDirectory()) {
            Lumin.LOGGER.warn("Couldn't locate the sticky notes directory. Is this the first launch?");
            return;
        }

        // List all files in the directory
        File[] files = directory.listFiles();

        // Loop through each file in the directory
        for (File file : files) {
            if (file.isFile()) {
                try (JsonReader reader = new JsonReader(new FileReader(file))) {
                    JsonObject jsonData = (JsonObject) JsonParser.parseReader(reader);

                    // Add data to the `jsonData` object
                    String title = (String) jsonData.get("title").getAsString();
                    String content = (String) jsonData.get("content").getAsString();
                    Double x = (Double) jsonData.get("x").getAsDouble();
                    Double y = (Double) jsonData.get("y").getAsDouble();
                    Double height = (Double) jsonData.get("height").getAsDouble();
                    String theme = (String) jsonData.get("theme").getAsString();
                    StickyCardColor color = StickyCardColor.YELLOW_THEME;
                    String imagePath = (String) jsonData.get("imagePath").getAsString();

                    if (Objects.equals(theme, "BLUE")) {
                        color = StickyCardColor.BLUE_THEME;
                    } else if (Objects.equals(theme, "PINK")) {
                        color = StickyCardColor.PINK_THEME;
                    }

                    Stage stickyNoteStage = new Stage();
                    StickyCardScene scene = null;

                    UUID uuid = UUID.fromString(file.getName().replace(".json", ""));

                    if (StageManager.getStage(uuid.toString()).isPresent()) {
                        StageManager.getStage(uuid.toString()).get().close();
                    }

                    if (Objects.equals(imagePath, "null")) {
                        scene = new StickyCardScene(new Group(), color, title, content, uuid);
                    } else {
                        scene = new StickyCardScene(new Group(), color, title, content, uuid, imagePath);
                    }

                    stickyNoteStage.setScene(scene);
                    stickyNoteStage.initOwner(StageManager.getStage("main").get());
                    stickyNoteStage.initStyle(StageStyle.TRANSPARENT);
                    stickyNoteStage.setAlwaysOnTop(true);
                    stickyNoteStage.show();
                    stickyNoteStage.setX(x);
                    stickyNoteStage.setY(y);
                    stickyNoteStage.setHeight(height);

                    StageManager.setStage(uuid.toString(), stickyNoteStage);

                } catch (IOException | JsonParseException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }
}
