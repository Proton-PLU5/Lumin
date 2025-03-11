package me.protonplus.lumin.scenes;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import me.protonplus.lumin.Lumin;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static me.protonplus.lumin.Lumin.*;


public class SettingsScene extends LuminScene {
    private AnchorPane settingsScreen;
    private List<TogglePanelButtons> settingsPanelButtons = new ArrayList<>();
    private double yOffset;
    private double xOffset;

    Supplier<Rectangle> createLargeRectangle = new Supplier<Rectangle>() {
        @Override
        public Rectangle get() {
            Rectangle rect = new Rectangle(215*2, 40);
            rect.setFill(Color.web("#FFFFFF"));
            rect.setArcHeight(4);
            rect.setArcWidth(4);
            return rect;
        }
    };

    Supplier<Rectangle> createSmallRectangle = new Supplier<Rectangle>() {
        @Override
        public Rectangle get() {
            Rectangle rect = new Rectangle(120, 40);
            rect.setFill(Color.web("#FFFFFF"));
            rect.setArcHeight(4);
            rect.setArcWidth(4);
            return rect;
        }
    };

    class TogglePanelButtons {
        public Node settingPanelButton;
        public boolean toggled;

        public TogglePanelButtons(Node settingPanelButton, boolean toggled) {
            this.settingPanelButton = settingPanelButton;
            this.toggled = toggled;
        }
    }

    public SettingsScene(Group root) {
        super(root);
        this.setFill(Color.TRANSPARENT);
        // Create an outer shadow effect
        DropShadow outerShadow = new DropShadow();
        outerShadow.setRadius(10.0);
        outerShadow.setColor(Color.BLACK);

        // Apply the outer shadow effect to the root node
        root.setEffect(outerShadow);
        AnchorPane anchorPane = new AnchorPane();
        anchorPane.setStyle("-fx-background-color: #e8e5ea; -fx-background-radius: 20");
        anchorPane.setPrefSize(1000, 800);
        createSettingsPanel(anchorPane);

        root.getChildren().add(anchorPane);


        ImageView closeView = new ImageView("me/protonplus/lumin/icons/close.png");
        closeView.setFitHeight(25);
        closeView.setFitWidth(25);
        Rectangle closeRect = new Rectangle(25, 25);
        closeRect.setFill(Color.TRANSPARENT);
        StackPane closePane = new StackPane(closeView, closeRect);
        closePane.setOnMouseClicked((t) -> this.close());

        settingsScreen = new AnchorPane();
        settingsScreen.setPrefSize(600, 800);
        settingsScreen.setTranslateX(400);
        anchorPane.getChildren().add(settingsScreen);

        Platform.runLater(() -> {
            closePane.setTranslateX(this.getWindow().getWidth()-45);
            closePane.setTranslateY(10);
        });

        createGeneralSettingsScreen();

        Rectangle dragRectangle = new Rectangle(1000, 55);
        dragRectangle.setFill(Color.TRANSPARENT);


        dragRectangle.setOnMousePressed((event) -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });

        dragRectangle.setOnMouseDragged((event) -> {
            this.getWindow().setX(event.getScreenX() - xOffset);
            this.getWindow().setY(event.getScreenY() - yOffset);
        });

        anchorPane.getChildren().add(dragRectangle);
        anchorPane.getChildren().add(closePane);

    }

    private void createSettingsPanel(AnchorPane anchorPane){
        Rectangle panelBackgroundRect = new Rectangle(400, 800);
        panelBackgroundRect.setFill(Color.web("#F4F3F5"));
        panelBackgroundRect.setArcWidth(20);
        panelBackgroundRect.setArcHeight(20);

        Rectangle panelBackgroundClipRect = new Rectangle(30, 800);
        panelBackgroundClipRect.setFill(Color.web("#F4F3F5"));
        panelBackgroundClipRect.setTranslateX(185*2);

        // Settings Header
        Rectangle settingsHeaderRect = new Rectangle(400, 29*2);
        settingsHeaderRect.setFill(Color.web("#FFFFFF"));
        settingsHeaderRect.setArcWidth(20);
        settingsHeaderRect.setArcHeight(20);

        Rectangle settingsHeaderClip1Rect = new Rectangle(400, 20);
        settingsHeaderClip1Rect.setFill(Color.web("#FFFFFF"));
        settingsHeaderClip1Rect.setTranslateY(19*2);

        Rectangle settingsHeaderClip2Rect = new Rectangle(20, 29*2);
        settingsHeaderClip2Rect.setFill(Color.web("#FFFFFF"));
        settingsHeaderClip2Rect.setTranslateX(190*2);

        Label settingsHeaderLabel = new Label("Settings");
        settingsHeaderLabel.setFont(Font.font("Inter", FontWeight.EXTRA_BOLD, 24));
        settingsHeaderLabel.setTranslateX(76*2);
        settingsHeaderLabel.setTranslateY(8*2);

        // Settings Options
        GridPane settingsOptionPane = new GridPane();
        settingsOptionPane.setTranslateX(20);
        settingsOptionPane.setTranslateY(43*2);
        settingsOptionPane.setVgap(5);

        // Save Button
        Rectangle saveSettingOptionBox = createOptionRectangle();
        saveSettingOptionBox.setFill(Color.web("#67C468"));
        ImageView saveSettingOptionImage = new ImageView("me/protonplus/lumin/icons/save.png");
        saveSettingOptionImage.setFitHeight(26);
        saveSettingOptionImage.setFitWidth(26);
        saveSettingOptionImage.setTranslateX(-180+28);
        Label saveSettingOptionLabel = new Label("Save");
        saveSettingOptionLabel.setFont(Font.font("Inter", FontWeight.MEDIUM, 20));

        StackPane saveSettingOption = new StackPane(saveSettingOptionBox, saveSettingOptionImage, saveSettingOptionLabel);
        saveSettingOption.setOnMouseClicked(event -> {
            saveSettingOptionBox.setStyle("-fx-effect: innershadow(gaussian, #62A2F7, 50, 1.0, 0, 0); -fx-opacity: 1.0;");
            for (Node child : settingsOptionPane.getChildren()) {
                if (child instanceof StackPane stackPane && !child.equals(saveSettingOption)) {
                    stackPane.getChildren().get(0).setStyle("-fx-effect: innershadow(gaussian, #FFFFFF, 50, 1.0, 0, 0); -fx-opacity: 1.0;");
                }
            }
            saveSettings(saveSettingOptionBox);
        });
        saveSettingOption.setOnMouseEntered(event -> {
            if (saveSettingOptionBox.getStyle().contains("#62A2F7")) {return;}
            saveSettingOptionBox.setStyle("-fx-effect: innershadow(gaussian, #75DF76, 50, 1.0, 0, 0); -fx-opacity: 1.0;");
        });
        saveSettingOption.setOnMouseExited(event -> {
            if (saveSettingOptionBox.getStyle().contains("#62A2F7")) {return;}
            saveSettingOptionBox.setStyle("-fx-effect: innershadow(gaussian, #67C468, 50, 1.0, 0, 0); -fx-opacity: 1.0;");
        });

        Consumer<StackPane> mouseClickConsumer = stack -> {
            for (Node child : settingsOptionPane.getChildren()) {
                if (child instanceof StackPane stackPane && !child.equals(stack) && !child.equals(saveSettingOption)) {
                    stackPane.getChildren().get(0).setStyle("-fx-effect: innershadow(gaussian, #FFFFFF, 50, 1.0, 0, 0); -fx-opacity: 1.0;");
                }
                else if (child.equals(saveSettingOption)) {
                    saveSettingOptionBox.setStyle("-fx-effect: innershadow(gaussian, #67C468, 50, 1.0, 0, 0); -fx-opacity: 1.0;");
                }
            }
        };

        // General Settings
        ImageView generalSettingOptionBox = new ImageView("me/protonplus/lumin/images/settings/settings_option_trapezium_shape.png");
        ImageView generalSettingOptionImage = new ImageView("me/protonplus/lumin/icons/settings.png");
        generalSettingOptionImage.setFitHeight(26);
        generalSettingOptionImage.setFitWidth(26);
        generalSettingOptionImage.setTranslateX(-180+28);
        Label generalSettingOptionLabel = new Label("General Settings");
        generalSettingOptionLabel.setFont(Font.font("Inter", FontWeight.MEDIUM, 20));
        generalSettingOptionBox.setStyle("-fx-effect: innershadow(gaussian, #62A2F7, 50, 1.0, 0, 0); -fx-opacity: 1.0;");

        StackPane generalSettingOption = new StackPane(generalSettingOptionBox, generalSettingOptionImage, generalSettingOptionLabel);
        generalSettingOption.setOnMouseClicked(event -> {
            mouseClickConsumer.accept(generalSettingOption);
            createGeneralSettingsScreen();
            generalSettingOptionBox.setStyle("-fx-effect: innershadow(gaussian, #62A2F7, 50, 1.0, 0, 0); -fx-opacity: 1.0;");
        });
        generalSettingOption.setOnMouseEntered(event -> {
            if (generalSettingOptionBox.getStyle().contains("#62A2F7")) {return;}
            generalSettingOptionBox.setStyle("-fx-effect: innershadow(gaussian, #E3E3E3, 50, 1.0, 0, 0); -fx-opacity: 1.0;");
        });
        generalSettingOption.setOnMouseExited(event -> {
            if (generalSettingOptionBox.getStyle().contains("#62A2F7")) {return;}
            generalSettingOptionBox.setStyle("-fx-effect: innershadow(gaussian, #FFFFFF, 50, 1.0, 0, 0); -fx-opacity: 1.0;");
        });
        settingsOptionPane.add(generalSettingOption, 0, 0);

        // Voice Recognition Settings;
        Rectangle voiceRecognitionSettingOptionBox = createOptionRectangle();
        ImageView voiceRecognitionSettingOptionImage = new ImageView("me/protonplus/lumin/icons/voice.png");
        voiceRecognitionSettingOptionImage.setFitHeight(26);
        voiceRecognitionSettingOptionImage.setFitWidth(26);
        voiceRecognitionSettingOptionImage.setTranslateX(-180+28);
        Label voiceRecognitionSettingOptionLabel = new Label("Voice Recognition Settings");
        voiceRecognitionSettingOptionLabel.setFont(Font.font("Inter", FontWeight.MEDIUM, 20));

        StackPane voiceRecognitionSettingOption = new StackPane(voiceRecognitionSettingOptionBox, voiceRecognitionSettingOptionImage, voiceRecognitionSettingOptionLabel);
        voiceRecognitionSettingOption.setOnMouseClicked(event -> {

            voiceRecognitionSettingOptionBox.setStyle("-fx-effect: innershadow(gaussian, #62A2F7, 50, 1.0, 0, 0); -fx-opacity: 1.0;");
            mouseClickConsumer.accept(voiceRecognitionSettingOption);
        });
        voiceRecognitionSettingOption.setOnMouseEntered(event -> {
            if (voiceRecognitionSettingOptionBox.getStyle().contains("#62A2F7")) {return;}
            voiceRecognitionSettingOptionBox.setStyle("-fx-effect: innershadow(gaussian, #E3E3E3, 50, 1.0, 0, 0); -fx-opacity: 1.0;");
        });
        voiceRecognitionSettingOption.setOnMouseExited(event -> {
            if (voiceRecognitionSettingOptionBox.getStyle().contains("#62A2F7")) {return;}
            voiceRecognitionSettingOptionBox.setStyle("-fx-effect: innershadow(gaussian, #FFFFFF, 50, 1.0, 0, 0); -fx-opacity: 1.0;");
        });
        settingsOptionPane.add(voiceRecognitionSettingOption, 0, 1);

        // Sticky Card Settings;
        Rectangle stickyCardSettingOptionBox = createOptionRectangle();
        ImageView stickyCardSettingOptionImage = new ImageView("me/protonplus/lumin/icons/sticky_card.png");
        stickyCardSettingOptionImage.setFitHeight(26);
        stickyCardSettingOptionImage.setFitWidth(26);
        stickyCardSettingOptionImage.setTranslateX(-180+28);
        Label stickyCardSettingOptionLabel = new Label("Sticky Card Settings");
        stickyCardSettingOptionLabel.setFont(Font.font("Inter", FontWeight.MEDIUM, 20));

        StackPane stickyCardSettingOption = new StackPane(stickyCardSettingOptionBox, stickyCardSettingOptionImage, stickyCardSettingOptionLabel);
        stickyCardSettingOption.setOnMouseClicked(event -> {
            createStickyCardSettingsScreen();
            stickyCardSettingOptionBox.setStyle("-fx-effect: innershadow(gaussian, #62A2F7, 50, 1.0, 0, 0); -fx-opacity: 1.0;");
            mouseClickConsumer.accept(stickyCardSettingOption);
        });
        stickyCardSettingOption.setOnMouseEntered(event -> {
            if (stickyCardSettingOptionBox.getStyle().contains("#62A2F7")) {return;}
            stickyCardSettingOptionBox.setStyle("-fx-effect: innershadow(gaussian, #E3E3E3, 50, 1.0, 0, 0); -fx-opacity: 1.0;");
        });
        stickyCardSettingOption.setOnMouseExited(event -> {
            if (stickyCardSettingOptionBox.getStyle().contains("#62A2F7")) {return;}
            stickyCardSettingOptionBox.setStyle("-fx-effect: innershadow(gaussian, #FFFFFF, 50, 1.0, 0, 0); -fx-opacity: 1.0;");
        });
        settingsOptionPane.add(stickyCardSettingOption, 0, 2);

        // Gmail Settings;
        Rectangle gmailSettingOptionBox = createOptionRectangle();
        ImageView gmailSettingOptionImage = new ImageView("me/protonplus/lumin/icons/mail.png");
        gmailSettingOptionImage.setFitHeight(26);
        gmailSettingOptionImage.setFitWidth(26);
        gmailSettingOptionImage.setTranslateX(-180+28);
        Label gmailSettingOptionLabel = new Label("Gmail Settings");
        gmailSettingOptionLabel.setFont(Font.font("Inter", FontWeight.MEDIUM, 20));

        StackPane gmailSettingOption = new StackPane(gmailSettingOptionBox, gmailSettingOptionImage, gmailSettingOptionLabel);
        gmailSettingOption.setOnMouseClicked(event -> {
            createGmailSettingsScreen();
            gmailSettingOptionBox.setStyle("-fx-effect: innershadow(gaussian, #62A2F7, 50, 1.0, 0, 0); -fx-opacity: 1.0;");
            mouseClickConsumer.accept(gmailSettingOption);
        });
        gmailSettingOption.setOnMouseEntered(event -> {
            if (gmailSettingOptionBox.getStyle().contains("#62A2F7")) {return;}
            gmailSettingOptionBox.setStyle("-fx-effect: innershadow(gaussian, #E3E3E3, 50, 1.0, 0, 0); -fx-opacity: 1.0;");
        });
        gmailSettingOption.setOnMouseExited(event -> {
            if (gmailSettingOptionBox.getStyle().contains("#62A2F7")) {return;}
            gmailSettingOptionBox.setStyle("-fx-effect: innershadow(gaussian, #FFFFFF, 50, 1.0, 0, 0); -fx-opacity: 1.0;");
        });
        settingsOptionPane.add(gmailSettingOption, 0, 3);

        settingsOptionPane.add(saveSettingOption, 0, settingsOptionPane.getRowCount());

        anchorPane.getChildren().addAll(panelBackgroundRect, panelBackgroundClipRect);
        anchorPane.getChildren().addAll(settingsHeaderRect, settingsHeaderClip1Rect, settingsHeaderClip2Rect, settingsHeaderLabel);
        anchorPane.getChildren().add(settingsOptionPane);
    }

    private void saveSettings(Node node) {
        File file = new File(
                System.getProperty("user.home"), ".lumin/settings.json");
        try {
            file.getParentFile().mkdirs();
            file.createNewFile();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        try (FileWriter writer = new FileWriter(file)) {
            JsonObject jsonData = new JsonObject();

            // Add data to the `jsonData` object
            jsonData.addProperty("autoHideLumin", autoHideLumin);
            jsonData.addProperty("autoOpenStickyCards", autoOpenStickyCards);

            // Write the JSON data to the file
            writer.write(jsonData.getAsString());
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        node.setStyle("-fx-effect: innershadow(gaussian, #67C468, 50, 1.0, 0, 0); -fx-opacity: 1.0;");
    }

    public static void loadSettings() {
        File file = new File(
                System.getProperty("user.home"), ".lumin/settings.json");
        if (!file.exists()) {
            Lumin.LOGGER.warn("Couldn't locate the settings file. Is this the first launch?");
            return;
        }
        if (file.isFile()) {
            try (FileReader reader = new FileReader(file)) {
                JsonParser parser = new JsonParser();
                JsonObject jsonData = parser.parse(reader).getAsJsonObject();

                // Add data to the `jsonData` object
                Boolean _autoHideLumin = jsonData.get("autoHideLumin").getAsBoolean();
                Boolean _autoOpenStickyCards = jsonData.get("autoOpenStickyCards").getAsBoolean();

                if (_autoHideLumin != null) {autoHideLumin=_autoHideLumin;}
                if (_autoOpenStickyCards != null) {autoOpenStickyCards=_autoOpenStickyCards;}

            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    private Rectangle createOptionRectangle() {
        Rectangle rectangle = new Rectangle(360, 40);
        rectangle.setFill(Color.web("#FFFFFF"));
        rectangle.setArcHeight(4);
        rectangle.setArcWidth(4);
        return rectangle;
    }

    private void createGeneralSettingsScreen() {
        this.settingsScreen.getChildren().clear();
        Label headerLabel = new Label("General Settings");
        headerLabel.setFont(Font.font("Inter", FontWeight.BOLD, 20));
        headerLabel.setTranslateX(11*2);
        headerLabel.setTranslateY(8*2);
        this.settingsScreen.getChildren().add(headerLabel);

        GridPane settingGrid = new GridPane();
        settingGrid.setTranslateX(11*2);
        settingGrid.setTranslateY(29*2);
        settingGrid.setVgap(10);
        settingGrid.setHgap(10);

        Rectangle autoHideLuminSettingRect = createLargeRectangle.get();
        Label autoHideLuminSettingLabel = new Label("Auto hide Lumin on launch");
        autoHideLuminSettingLabel.setFont(Font.font("Inter", FontWeight.MEDIUM, 20));

        Rectangle autoHideLuminSettingToggleRect = createSmallRectangle.get();
        Label autoHideLuminSettingToggleLabel = new Label("No");
        if (autoHideLumin) {
            autoHideLuminSettingToggleLabel.setText("Yes");
        } else {
            autoHideLuminSettingToggleLabel.setText("No");
        }
        autoHideLuminSettingToggleLabel.setFont(Font.font("Inter", FontWeight.MEDIUM, 20));
        StackPane autoHideLuminSettingTogglePane = new StackPane(autoHideLuminSettingToggleRect, autoHideLuminSettingToggleLabel);

        StackPane autoHideLuminPane = new StackPane(autoHideLuminSettingRect, autoHideLuminSettingLabel);
        settingGrid.add(autoHideLuminPane, 0, 1);
        settingGrid.add(autoHideLuminSettingTogglePane, 1, 1);
        autoHideLuminSettingTogglePane.setOnMouseClicked(event -> {
            autoHideLumin = !autoHideLumin;
            if (autoHideLumin) {
                autoHideLuminSettingToggleLabel.setText("Yes");
            } else {
                autoHideLuminSettingToggleLabel.setText("No");
            }
        });

        this.settingsScreen.getChildren().add(settingGrid);
    }
    private void createGmailSettingsScreen() {
        this.settingsScreen.getChildren().clear();
        Label headerLabel = new Label("Gmail Settings");
        headerLabel.setFont(Font.font("Inter", FontWeight.BOLD, 20));
        headerLabel.setTranslateX(11*2);
        headerLabel.setTranslateY(8*2);
        this.settingsScreen.getChildren().add(headerLabel);

        GridPane settingGrid = new GridPane();
        settingGrid.setTranslateX(11*2);
        settingGrid.setTranslateY(29*2);
        settingGrid.setVgap(10);
        settingGrid.setHgap(10);

        this.settingsScreen.getChildren().add(settingGrid);
    }
    private void createStickyCardSettingsScreen() {
        this.settingsScreen.getChildren().clear();
        Label headerLabel = new Label("Sticky Card Settings");
        headerLabel.setFont(Font.font("Inter", FontWeight.BOLD, 20));
        headerLabel.setTranslateX(11*2);
        headerLabel.setTranslateY(8*2);
        this.settingsScreen.getChildren().add(headerLabel);

        GridPane settingGrid = new GridPane();
        settingGrid.setTranslateX(11*2);
        settingGrid.setTranslateY(29*2);
        settingGrid.setVgap(10);
        settingGrid.setHgap(10);

        Rectangle autoOpenStickyCardsSettingRect = createLargeRectangle.get();
        Label autoOpenStickyCardsSettingLabel = new Label("Auto Open Sticky Cards");
        autoOpenStickyCardsSettingLabel.setFont(Font.font("Inter", FontWeight.MEDIUM, 20));

        Rectangle autoOpenStickyCardsSettingToggleRect = createSmallRectangle.get();
        Label autoOpenStickyCardsSettingToggleLabel = new Label("Yes");
        if (autoOpenStickyCards) {
            autoOpenStickyCardsSettingToggleLabel.setText("Yes");
        } else {
            autoOpenStickyCardsSettingToggleLabel.setText("No");
        }
        autoOpenStickyCardsSettingToggleLabel.setFont(Font.font("Inter", FontWeight.MEDIUM, 20));
        StackPane autoOpenStickyCardsSettingTogglePane = new StackPane(autoOpenStickyCardsSettingToggleRect, autoOpenStickyCardsSettingToggleLabel);

        StackPane autoOpenStickyCardsPane = new StackPane(autoOpenStickyCardsSettingRect, autoOpenStickyCardsSettingLabel);
        settingGrid.add(autoOpenStickyCardsPane, 0, 1);
        settingGrid.add(autoOpenStickyCardsSettingTogglePane, 1, 1);
        autoOpenStickyCardsSettingTogglePane.setOnMouseClicked(event -> {
            autoOpenStickyCards = !autoOpenStickyCards;
            if (autoOpenStickyCards) {
                autoOpenStickyCardsSettingToggleLabel.setText("Yes");
            } else {
                autoOpenStickyCardsSettingToggleLabel.setText("No");
            }
        });

        this.settingsScreen.getChildren().add(settingGrid);
    }

}
