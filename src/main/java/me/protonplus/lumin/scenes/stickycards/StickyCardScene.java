package me.protonplus.lumin.scenes.stickycards;

import com.google.gson.JsonObject;
import javafx.animation.AnimationTimer;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextFormatter;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import me.protonplus.lumin.util.StageManager;
import me.protonplus.lumin.util.stickycards.StickyCardColor;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import static me.protonplus.lumin.util.stickycards.StickyCardColor.*;


public class StickyCardScene extends Scene {
    private double initialY;
    private boolean resizing = false;
    private double defaultHeight;
    double xOffset = 0;
    double yOffset = 0;

    public StickyCardColor theme = YELLOW_THEME;
    
    public TextArea titleArea;
    public TextArea contentArea;
    public UUID id;

    public String title = "Stickynote Title";
    public String content = "Stickynote Content.";

    public String imagePath = "null";

    private AnchorPane anchorPane;
    private Rectangle imageBackground;

    public StickyCardScene(Group root, StickyCardColor theme, String title, String content, UUID id) {
        super(root);
        this.theme = theme;
        this.title = title;
        this.content = content;
        this.id = id;
        anchorPane = new AnchorPane();
        setUpBody(anchorPane, false, true);
        anchorPane.setPrefHeight(350);
        this.setFill(theme.getSecondaryColor());
        root.getChildren().add(anchorPane);

        FadeTransition fadeTransition = new FadeTransition(Duration.seconds(0.5), root);
        fadeTransition.setFromValue(0);
        fadeTransition.setInterpolator(Interpolator.EASE_BOTH);
        fadeTransition.setToValue(1);

        fadeTransition.play();
    }

    public StickyCardScene(Group root) {
        super(root);
        this.id = UUID.randomUUID();
        AnchorPane anchorPane = new AnchorPane();
        anchorPane.setPadding(new Insets(2));
        setUpBody(anchorPane, false, false);
        anchorPane.setPrefHeight(350);
        this.setFill(theme.getSecondaryColor());
        root.getChildren().add(anchorPane);

        FadeTransition fadeTransition = new FadeTransition(Duration.seconds(0.5), root);
        fadeTransition.setFromValue(0);
        fadeTransition.setInterpolator(Interpolator.EASE_BOTH);
        fadeTransition.setToValue(1);

        fadeTransition.play();
    }



    public StickyCardScene(Group root, StickyCardColor theme, String title, String content, UUID id, String imagePath) {
        super(root);
        this.theme = theme;
        this.title = title;
        this.content = content;
        this.id = id;
        this.imagePath = imagePath;

        AnchorPane anchorPane = new AnchorPane();
        setUpBody(anchorPane, true, true);
        setUpCoverImage(anchorPane, imagePath);
        anchorPane.setPrefHeight(350);
        this.setFill(theme.getSecondaryColor());
        root.getChildren().add(anchorPane);

        FadeTransition fadeTransition = new FadeTransition(Duration.seconds(0.5), root);
        fadeTransition.setFromValue(0);
        fadeTransition.setInterpolator(Interpolator.EASE_BOTH);
        fadeTransition.setToValue(1);

        fadeTransition.play();
    }

    private void close() {
        FadeTransition fadeTransition = new FadeTransition(Duration.seconds(0.5), this.getRoot());
        fadeTransition.setFromValue(1);
        fadeTransition.setInterpolator(Interpolator.EASE_BOTH);
        fadeTransition.setToValue(0);

        ParallelTransition parallelTransition = new ParallelTransition(fadeTransition);
        parallelTransition.setOnFinished((e) -> {
            Stage window = ((Stage)this.getWindow());

            File file = new File(
                    System.getProperty("user.home"), ".lumin/stickycards/"+id+".json");
            try {
                file.getParentFile().mkdirs();
                file.createNewFile();
            } catch (IOException ex) {
                ex.printStackTrace();
            }

            try (FileWriter writer = new FileWriter(file)) {
                JsonObject jsonData = getJsonObject();;
                // Write the JSON data to the file
                writer.write(jsonData.toString());
            } catch (IOException ex) {
                ex.printStackTrace();
            }

            window.close();
        });
        parallelTransition.play();
    }

    private @NotNull JsonObject getJsonObject() {
        JsonObject jsonData = new JsonObject();

        // Add data to the `jsonData` object
        jsonData.addProperty("title", titleArea.getText());
        jsonData.addProperty("content", contentArea.getText());
        String strTheme = "YELLOW";
        if (this.theme == YELLOW_THEME) {jsonData.addProperty("theme", strTheme);}
        else if (this.theme == BLUE_THEME) {jsonData.addProperty("theme", "BLUE");}
        else if (this.theme == PINK_THEME) {jsonData.addProperty("theme", "PINK");}
        jsonData.addProperty("x", this.getWindow().getX());
        jsonData.addProperty("y", this.getWindow().getY());
        jsonData.addProperty("height", this.getWindow().getHeight());
        jsonData.addProperty("imagePath", this.imagePath);
        return jsonData;
    }

    private void setUpBody(AnchorPane anchorPane, boolean hasCover, boolean savedStickyNote) {
        Line resizeLine = new Line();
        resizeLine.setEndX(50);
        resizeLine.setStrokeWidth(4);
        resizeLine.setCursor(Cursor.N_RESIZE);
        resizeLine.setTranslateX(340/2-50/2);

        resizeLine.setOnMousePressed(this::startResize);
        resizeLine.setOnMouseReleased(this::stopResize);
        resizeLine.setOnMouseDragged(this::resizeRectangle);

        titleArea = new TextArea(title);

        titleArea.setWrapText(true);
        titleArea.setStyle("-fx-background-color: transparent;");
        titleArea.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        titleArea.setTranslateY(25);
        titleArea.setTranslateX(5);
        titleArea.setPrefWidth(332);
        titleArea.setPrefHeight(25);

        int CHARACTER_LIMIT = 80;
        titleArea.setTextFormatter(new TextFormatter<String>(change ->
                change.getControlNewText().length() <= CHARACTER_LIMIT ? change : null));

        this.getStylesheets().add("/me/protonplus/lumin/stylesheets/transparent-text-area.css");

        Line titleDividerLine = new Line();
        titleDividerLine.setEndX(322);
        titleDividerLine.setStrokeWidth(2);
        titleDividerLine.setTranslateX(8);
        titleDividerLine.setTranslateY(55);

        contentArea = new TextArea(content);

        contentArea.setWrapText(true);
        contentArea.setStyle("-fx-background-color: transparent;");
        contentArea.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
        contentArea.setTranslateY(60);
        contentArea.setTranslateX(5);
        contentArea.setPrefWidth(332);
        contentArea.setPrefHeight(125);
        contentArea.addEventFilter(ScrollEvent.ANY, Event::consume);

        if (!savedStickyNote) {
            titleArea.setPromptText(title);
            contentArea.setPromptText(content);
        }

        AnchorPane bodyPane = new AnchorPane();
        bodyPane.getChildren().add(titleArea);
        bodyPane.getChildren().add(titleDividerLine);
        bodyPane.getChildren().add(contentArea);

        if (hasCover) {
            bodyPane.setTranslateY(150);
        }

        ImageView closeView = new ImageView("me/protonplus/lumin/icons/close.png");
        closeView.setFitHeight(25);
        closeView.setFitWidth(25);
        Rectangle closeRect = new Rectangle(25, 25);
        closeRect.setFill(Color.TRANSPARENT);
        StackPane closePane = new StackPane(closeView, closeRect);
        closePane.setOnMouseClicked((t) -> this.close());

        ImageView deleteView = new ImageView("me/protonplus/lumin/icons/trash.png");
        deleteView.setFitHeight(25);
        deleteView.setFitWidth(25);
        Rectangle deleteRect = new Rectangle(25, 25);
        deleteRect.setFill(Color.TRANSPARENT);
        StackPane deletePane = new StackPane(deleteView, deleteRect);
        deletePane.setOnMouseClicked((t) -> ((Stage)this.getWindow()).close());
        
        ImageView settingsView = new ImageView("me/protonplus/lumin/icons/settings.png");
        settingsView.setFitHeight(25);
        settingsView.setFitWidth(25);
        Rectangle settingsRect = new Rectangle(25, 25);
        settingsRect.setFill(Color.TRANSPARENT);
        StackPane settingsPane = new StackPane(settingsView, settingsRect);
        settingsPane.setOnMouseClicked((t) -> {
            Stage settingsStage = new Stage();
            settingsStage.initOwner(this.getWindow());
            settingsStage.initStyle(StageStyle.TRANSPARENT);

            if (hasCover) {
                settingsStage.setScene(new StickyCardSettingsScene(new Group(), this, true));
            } else settingsStage.setScene(new StickyCardSettingsScene(new Group(), this));

            settingsStage.setX(this.getWindow().getX());
            settingsStage.setY(this.getWindow().getY()+this.getWindow().getHeight()+5);
            settingsStage.show();
            settingsStage.setAlwaysOnTop(true);
        });


        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                resizeLine.setTranslateY(resizeLine.getScene().getWindow().getHeight()-10);
                contentArea.setPrefHeight(contentArea.getScene().getWindow().getHeight()-60-25);
                double width = closePane.getScene().getWindow().getWidth();
                double height = closePane.getScene().getWindow().getHeight();
                settingsPane.setTranslateX(5);
                settingsPane.setTranslateY(height-30);
                closePane.setTranslateX(width-30);
                closePane.setTranslateY(height-30);
                deletePane.setTranslateX(width-(35+25));
                deletePane.setTranslateY(height-30);
            }
        };
        timer.start();

        anchorPane.getChildren().add(bodyPane);
        anchorPane.getChildren().add(resizeLine);
        anchorPane.getChildren().add(closePane);
        anchorPane.getChildren().add(deletePane);
        anchorPane.getChildren().add(settingsPane);

        anchorPane.setOnMousePressed((event) -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });

        anchorPane.setOnMouseDragged((event) -> {
            if (!this.resizing) {
                this.getWindow().setX(event.getScreenX() - xOffset);
                this.getWindow().setY(event.getScreenY() - yOffset);
            }
        });
    }

    private void setUpCoverImage(AnchorPane pane, String imagePath) {
        imageBackground = new Rectangle(340, 160);
        imageBackground.setFill(theme.getPrimaryColor());

        ImageView coverImage = new ImageView(imagePath);
        coverImage.setFitWidth(310);
        coverImage.setFitHeight(130);
        coverImage.setTranslateX(0);
        coverImage.setTranslateY(0);

        StackPane coverImagePane = new StackPane(imageBackground, coverImage);
        pane.getChildren().add(coverImagePane);

        coverImagePane.setOnMousePressed((event) -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });

        coverImagePane.setOnMouseDragged((event) -> {
            this.getWindow().setX(event.getScreenX() - xOffset);
            this.getWindow().setY(event.getScreenY() - yOffset);
        });
    }

    private void startResize(MouseEvent event) {
        if (defaultHeight == 0) {
            this.defaultHeight = this.getWindow().getHeight();
        }
        this.resizing = true;
        initialY = event.getSceneY();
    }

    private void stopResize(MouseEvent event) {
        this.resizing = false;
        // Update the initialY to prevent jumps when resizing after releasing the mouse
        initialY = event.getSceneY();
    }

    private void resizeRectangle(MouseEvent event) {
        double deltaY = event.getSceneY() - initialY;
        double newHeight = this.getWindow().getHeight() + deltaY;

        // Ensure the height doesn't become negative and doesn't go below the minimum height
        if (newHeight > 350) {
            this.getWindow().setHeight(newHeight);
        }

        // Update the initialY for the next drag event
        initialY = event.getSceneY();
    }

    public void updateTheme() {
        this.setFill(theme.getSecondaryColor());
        if (this.imageBackground != null) {
            this.imageBackground.setFill(theme.getPrimaryColor());
        }
    }
}

class StickyCardSettingsScene extends Scene {
    private final ImageView imageView;
    public StickyCardSettingsScene(Group root, StickyCardScene owner) {
        super(root);
        this.setFill(Color.TRANSPARENT);
        GridPane gridPane = new GridPane();
        //gridPane.setPrefSize(100, 40);
        gridPane.setPadding(new Insets(5));
        gridPane.setHgap(5);
        gridPane.setStyle("-fx-background-color: #e8e5ea; -fx-background-radius: 10");

        FadeTransition fadeTransition = new FadeTransition(Duration.seconds(0.25), this.getRoot());
        fadeTransition.setFromValue(0);
        fadeTransition.setInterpolator(Interpolator.EASE_BOTH);
        fadeTransition.setToValue(1);

        fadeTransition.play();

        Platform.runLater(() -> {
            this.getWindow().focusedProperty().addListener(new ChangeListener<Boolean>()
            {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov, Boolean onHidden, Boolean onShown)
                {
                    close();
                }
            });
        });

        imageView = new ImageView("me/protonplus/lumin/icons/image.png");
        imageView.setFitWidth(25);
        imageView.setFitHeight(25);
        Rectangle imageRect = new Rectangle(25, 25);
        imageRect.setFill(Color.TRANSPARENT);
        imageRect.setOnMousePressed((e) -> {
            chooseImage(owner);
        });

        StackPane imagePane = new StackPane(imageView, imageRect);

        ImageView paletteView = new ImageView("me/protonplus/lumin/icons/palette.png");
        paletteView.setFitWidth(25);
        paletteView.setFitHeight(25);
        Rectangle paletteRect = new Rectangle(25, 25);
        paletteRect.setFill(Color.TRANSPARENT);
        paletteRect.setOnMousePressed((e) -> {
            if (owner.theme.equals(YELLOW_THEME)) {
                owner.theme = BLUE_THEME;
            } else if (owner.theme.equals(BLUE_THEME)) {
                owner.theme = PINK_THEME;
            } else if (owner.theme.equals(PINK_THEME)) {
                owner.theme = YELLOW_THEME;
            }
            owner.updateTheme();
        });

        StackPane palettePane = new StackPane(paletteView, paletteRect);

        gridPane.add(imagePane, 0, 0);
        gridPane.add(palettePane, 1, 0);
        root.getChildren().add(gridPane);
    }

    public StickyCardSettingsScene(Group root, StickyCardScene owner, boolean removeImage) {
        super(root);
        this.setFill(Color.TRANSPARENT);
        GridPane gridPane = new GridPane();
        //gridPane.setPrefSize(100, 40);
        gridPane.setPadding(new Insets(5));
        gridPane.setHgap(5);
        gridPane.setStyle("-fx-background-color: #e8e5ea; -fx-background-radius: 10");

        FadeTransition fadeTransition = new FadeTransition(Duration.seconds(0.25), this.getRoot());
        fadeTransition.setFromValue(0);
        fadeTransition.setInterpolator(Interpolator.EASE_BOTH);
        fadeTransition.setToValue(1);

        fadeTransition.play();

        imageView = new ImageView("me/protonplus/lumin/icons/remove_image.png");
        imageView.setFitWidth(25);
        imageView.setFitHeight(25);
        Rectangle imageRect = new Rectangle(25, 25);
        imageRect.setFill(Color.TRANSPARENT);
        imageRect.setOnMousePressed((e) -> {
            removeImage(owner);
        });

        StackPane imagePane = new StackPane(imageView, imageRect);

        ImageView paletteView = new ImageView("me/protonplus/lumin/icons/palette.png");
        paletteView.setFitWidth(25);
        paletteView.setFitHeight(25);
        Rectangle paletteRect = new Rectangle(25, 25);
        paletteRect.setFill(Color.TRANSPARENT);
        paletteRect.setOnMousePressed((e) -> {
            if (owner.theme.equals(YELLOW_THEME)) {
                owner.theme = BLUE_THEME;
            } else if (owner.theme.equals(BLUE_THEME)) {
                owner.theme = PINK_THEME;
            } else if (owner.theme.equals(PINK_THEME)) {
                owner.theme = YELLOW_THEME;
            }
            owner.updateTheme();
        });

        StackPane palettePane = new StackPane(paletteView, paletteRect);

        gridPane.add(imagePane, 0, 0);
        gridPane.add(palettePane, 1, 0);
        root.getChildren().add(gridPane);
    }

    private void removeImage(StickyCardScene stickyScene) {
        double x = stickyScene.getWindow().getX();
        double y = stickyScene.getWindow().getY();
        StickyCardColor theme = stickyScene.theme;
        double height = stickyScene.getHeight();

        Stage stickyNoteStage = new Stage();
        StickyCardScene scene = new StickyCardScene(new Group(), theme, stickyScene.titleArea.getText(), stickyScene.contentArea.getText(), stickyScene.id);

        stickyNoteStage.setScene(scene);
        stickyNoteStage.initOwner(StageManager.getStage("main").get());
        stickyNoteStage.initStyle(StageStyle.TRANSPARENT);
        stickyNoteStage.setAlwaysOnTop(true);
        stickyNoteStage.setX(x);
        stickyNoteStage.setY(y);
        stickyNoteStage.setHeight(height);
        stickyNoteStage.show();

        ((Stage) stickyScene.getWindow()).close();
    }

    private void chooseImage(StickyCardScene stickyScene) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Image File");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        File selectedFile = fileChooser.showOpenDialog(stickyScene.getWindow());
        if (selectedFile != null) {
            Path source = Path.of((selectedFile.getPath()));
            Path destination = Path.of(System.getProperty("user.home")+"/.lumin/stickycards_images/", stickyScene.id.toString()+".png");

            try {
                Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                e.printStackTrace();
            }


            double x = stickyScene.getWindow().getX();
            double y = stickyScene.getWindow().getY();
            StickyCardColor theme = stickyScene.theme;
            double height = stickyScene.getHeight();

            Stage stickyNoteStage = new Stage();


            StickyCardScene scene = new StickyCardScene(new Group(), theme, stickyScene.titleArea.getText(), stickyScene.contentArea.getText(), stickyScene.id, destination.toString());

            stickyNoteStage.setScene(scene);
            stickyNoteStage.initOwner(StageManager.getStage("main").get());
            stickyNoteStage.initStyle(StageStyle.TRANSPARENT);
            stickyNoteStage.setAlwaysOnTop(true);
            stickyNoteStage.setX(x);
            stickyNoteStage.setY(y);
            stickyNoteStage.setHeight(height);
            stickyNoteStage.show();

            ((Stage) stickyScene.getWindow()).close();
        }
    }

    public void close() {
        FadeTransition fadeTransition = new FadeTransition(Duration.seconds(0.5), this.getRoot());
        fadeTransition.setFromValue(1);
        fadeTransition.setInterpolator(Interpolator.EASE_BOTH);
        fadeTransition.setToValue(0);

        fadeTransition.setOnFinished((e) -> {
            ((Stage)this.getWindow()).close();
        });
        fadeTransition.play();
    }

}
