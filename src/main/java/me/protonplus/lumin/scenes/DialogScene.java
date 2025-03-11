package me.protonplus.lumin.scenes;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import me.protonplus.lumin.util.LuminOperations;
import me.protonplus.lumin.util.StageManager;

import java.util.ArrayList;
import java.util.List;

public class DialogScene extends LuminScene {
    private double yOffset;
    private double xOffset;
    private GridPane gridPane;
    private Label dialogHeaderLabel;
    public List<String> previousMessages = new ArrayList<>();
    public List<String> responses = new ArrayList<>();
    private Boolean _isFirstMessage = true;

    public DialogScene(Group root) {
        super(root);
        this.setFill(Color.TRANSPARENT);

        // Create an outer shadow effect
        DropShadow outerShadow = new DropShadow();
        outerShadow.setRadius(10.0);
        outerShadow.setColor(Color.BLACK);

        // Apply the outer shadow effect to the root node
        root.setEffect(outerShadow);
        AnchorPane anchorPane = new AnchorPane();
        anchorPane.setStyle("-fx-background-color: #e8e5ea; -fx-background-radius: 10");
        anchorPane.setPrefSize(600, 350+300);

        Rectangle dragRectangle = new Rectangle(600, 40);
        dragRectangle.setFill(Color.TRANSPARENT);


        dragRectangle.setOnMousePressed((event) -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });

        dragRectangle.setOnMouseDragged((event) -> {
            this.getWindow().setX(event.getScreenX() - xOffset);
            this.getWindow().setY(event.getScreenY() - yOffset);
        });

        // Label
        dialogHeaderLabel = new Label("Lumin Chat");
        dialogHeaderLabel.setFont(Font.font("Inter", FontWeight.EXTRA_BOLD, 18));
        dialogHeaderLabel.setTranslateX((600-(600-10*2))/2);
        dialogHeaderLabel.setTranslateY(5*1.5);

        // Scroll Pane
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setPrefSize(600-10*2, 600-30);
        scrollPane.setTranslateY(45-(5*1.5));
        scrollPane.setTranslateX((600-(600-10*2))/2);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        // Grid Pane
        gridPane = new GridPane();
        gridPane.setTranslateX(5);
        gridPane.setTranslateY(5);
        gridPane.setVgap(5);
        gridPane.setPrefWidth(600-10*2);

        scrollPane.setContent(gridPane);

        // Typing Pane
        TextField typingField = new TextField();
        typingField.setPrefSize(600-10*2, 30-2.5*1.5);
        typingField.setStyle("-fx-background-radius: 10");
        typingField.setFont(Font.font("Arial", 14));
        typingField.setTranslateY(350-30-(5*1.5)+300);
        typingField.setTranslateX((600-(600-10*2))/2);
        typingField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                previousMessages.add(typingField.getText());
                Platform.runLater(() -> {
                    dialogHeaderLabel.setText("Generating Response...");
                });
                Thread requestThread = new Thread(() -> {

                    if (!this.previousMessages.isEmpty() && !this.responses.isEmpty()) {
                        StringBuilder previous = new StringBuilder();
                        List<String> _n = new ArrayList<>();
                        _n.addAll(this.previousMessages);
                        _n.remove(_n.size()-1);
                        for (int i = 0; i < _n.size(); i++) {
                            previous.append("USER: " + _n.get(i) + ". LUMIN: " + this.responses.get(i));
                        }
                        LuminOperations.generateWithoutBlocking(typingField.getText() + "; Here are previous user inputs: " + previous, this::createLabels, true);
                    } else {
                        LuminOperations.generateWithoutBlocking(typingField.getText(), this::createLabels, true);
                    }
                });
                requestThread.start();
            }
        });

        ImageView closeView = new ImageView("me/protonplus/lumin/icons/close.png");
        closeView.setFitHeight(25);
        closeView.setFitWidth(25);
        Rectangle closeRect = new Rectangle(25, 25);
        closeRect.setFill(Color.TRANSPARENT);
        StackPane closePane = new StackPane(closeView, closeRect);
        closePane.setOnMouseClicked((t) -> this.close());
        Platform.runLater(() -> {
            closePane.setTranslateX(this.getWindow().getWidth()-40);
            closePane.setTranslateY(5);
            this.getWindow().requestFocus();
            ((Stage) this.getWindow()).getIcons().add(new Image("me/protonplus/lumin/images/lumin.png"));
            ((Stage) this.getWindow()).setTitle("Lumin Chat");
        });

        anchorPane.getChildren().add(dialogHeaderLabel);
        anchorPane.getChildren().add(scrollPane);
        anchorPane.getChildren().add(typingField);
        anchorPane.getChildren().add(dragRectangle);
        anchorPane.getChildren().add(closePane);

        root.getChildren().add(anchorPane);
    }
    public DialogScene(String query) {
        this(new Group());

        Platform.runLater(() -> {
            this.getWindow().hide();
            Stage mainStage = StageManager.getStage("main").get();
            Stage animatedGestureStage = new Stage();
            AnimatedGestureScene animatedGestureScene = new AnimatedGestureScene(new Group(), "me/protonplus/lumin/images/animated/writing.gif", 10);
            animatedGestureStage.setScene(animatedGestureScene);
            animatedGestureStage.setAlwaysOnTop(true);
            animatedGestureStage.initStyle(StageStyle.TRANSPARENT);
            animatedGestureStage.initOwner(mainStage);
            animatedGestureStage.show();

            Stage scalableTextStage = new Stage();
            ScalableTextBoxV2Scene scalableTextBoxScene = new ScalableTextBoxV2Scene("Hang tight, I'm working on it!", false,true, 10);
            scalableTextBoxScene.setFill(Color.TRANSPARENT);
            scalableTextStage.setAlwaysOnTop(true);
            scalableTextStage.initStyle(StageStyle.TRANSPARENT);
            scalableTextStage.initOwner(mainStage);
            scalableTextStage.setScene(scalableTextBoxScene);
            scalableTextStage.show();

            ((MainScene) mainStage.getScene()).addNewDialog(animatedGestureStage);
            ((MainScene) mainStage.getScene()).addNewDialog(scalableTextStage);
        });
        this.previousMessages.add(query);
        Platform.runLater(() -> {
            dialogHeaderLabel.setText("Generating Response...");
        });
        System.out.println(this.previousMessages);
        Thread requestThread = new Thread(() -> {

            if (!this.previousMessages.isEmpty() && !this.responses.isEmpty()) {
                StringBuilder previous = new StringBuilder();
                List<String> _n = new ArrayList<>();
                _n.addAll(this.previousMessages);
                _n.remove(_n.size()-1);
                for (int i = 0; i < _n.size(); i++) {
                    previous.append("USER: " + _n.get(i) + ". LUMIN: " + this.responses.get(i));
                }
                LuminOperations.generateWithoutBlocking(query + "; Here are previous user inputs: " + previous, this::createLabels, true);
            } else {
                LuminOperations.generateWithoutBlocking(query, this::createLabels, true);
            }
        });

        requestThread.start();
    }

    public DialogScene(Group root, String clipboardString) {
        super(root);
        this.setFill(Color.TRANSPARENT);

        // Create an outer shadow effect
        DropShadow outerShadow = new DropShadow();
        outerShadow.setRadius(10.0);
        outerShadow.setColor(Color.BLACK);

        // Apply the outer shadow effect to the root node
        root.setEffect(outerShadow);
        AnchorPane anchorPane = new AnchorPane();
        anchorPane.setStyle("-fx-background-color: #e8e5ea; -fx-background-radius: 10");
        anchorPane.setPrefSize(600, 350+300);

        // Label
        dialogHeaderLabel = new Label("Lumin Chat");
        dialogHeaderLabel.setFont(Font.font("Inter", FontWeight.EXTRA_BOLD, 18));
        dialogHeaderLabel.setTranslateX((600-(600-10*2))/2);
        dialogHeaderLabel.setTranslateY(5*1.5);

        // Scroll Pane
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setPrefSize(600-10*2, 600-30);
        scrollPane.setTranslateY(45-(5*1.5));
        scrollPane.setTranslateX((600-(600-10*2))/2);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        // Grid Pane
        gridPane = new GridPane();
        gridPane.setTranslateX(5);
        gridPane.setTranslateY(5);
        gridPane.setVgap(5);
        gridPane.setPrefWidth(600-10*2);

        scrollPane.setContent(gridPane);

        // Typing Pane
        TextField typingField = new TextField();
        typingField.setPrefSize(600-10*2, 30-2.5*1.5);
        typingField.setStyle("-fx-background-radius: 10");
        typingField.setFont(Font.font("Arial", 14));
        typingField.setTranslateY(350-30-(5*1.5)+300);
        typingField.setTranslateX((600-(600-10*2))/2);
        typingField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                this.previousMessages.add(typingField.getText());
                Platform.runLater(() -> {
                    dialogHeaderLabel.setText("Generating Response...");
                });
                System.out.println(this.previousMessages);
                Thread requestThread = new Thread(() -> {
                    if (!this.previousMessages.isEmpty() && !this.responses.isEmpty()) {
                        StringBuilder previous = new StringBuilder();
                        List<String> _n = new ArrayList<>();
                        _n.addAll(this.previousMessages);
                        _n.remove(_n.size()-1);
                        for (int i = 0; i < _n.size(); i++) {
                            previous.append("USER: " + _n.get(i) + ". LUMIN: " + this.responses.get(i));
                        }
                        LuminOperations.generateWithoutBlocking(typingField.getText() + "; Here are previous user inputs: " + previous, this::createLabels, true);
                    } else {
                        LuminOperations.generateWithoutBlocking(typingField.getText(), this::createLabels, true);
                    }
                });
                requestThread.start();
            }
        });

        ImageView closeView = new ImageView("me/protonplus/lumin/icons/close.png");
        closeView.setFitHeight(25);
        closeView.setFitWidth(25);
        Rectangle closeRect = new Rectangle(25, 25);
        closeRect.setFill(Color.TRANSPARENT);
        StackPane closePane = new StackPane(closeView, closeRect);
        closePane.setOnMouseClicked((t) -> this.close());
        Platform.runLater(() -> {
            closePane.setTranslateX(600-40);
            closePane.setTranslateY(5);
            ((Stage) this.getWindow()).setTitle("Lumin Chat");
        });

        anchorPane.getChildren().add(dialogHeaderLabel);
        anchorPane.getChildren().add(scrollPane);
        anchorPane.getChildren().add(typingField);
        anchorPane.getChildren().add(closePane);
        root.getChildren().add(anchorPane);
        this.previousMessages.add("I have added the context for you! Just ask me what you want me to help you with!");
        createLabels("Context:\n" + clipboardString.replace("EVENTCALL ", ""));
    }

    public void createLabels(String response) {
        ((Stage) this.getWindow()).show();
        dialogHeaderLabel.setText("Lumin Chat");
        responses.add(response);
        this.gridPane.getChildren().clear();
        List<String> jointList = new ArrayList<>();
        jointList.addAll(previousMessages);
        jointList.addAll(responses);
        for (int i = 0; i < jointList.size(); i++) {
            if ((i % 2) != 0) {
                String message = responses.get(i/2);

                Label messageLabel = new Label(message);
                messageLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
                messageLabel.setStyle("-fx-background-color: #e3e3e3; -fx-background-radius: 5");
                messageLabel.setPadding(new Insets(5));
                messageLabel.setTextAlignment(TextAlignment.LEFT);
                messageLabel.setWrapText(true);

                StackPane pane = new StackPane(messageLabel);
                pane.setAlignment(Pos.CENTER_LEFT);
                pane.setMaxWidth(600-35-30);

                ImageView copyImage = new ImageView("me/protonplus/lumin/icons/clipboard.png");
                copyImage.setFitWidth(25);
                copyImage.setFitHeight(25);
                Rectangle copyRect = new Rectangle(25, 25);
                copyRect.setFill(Color.TRANSPARENT);
                StackPane copyPane = new StackPane(copyImage, copyRect);
                copyPane.setOnMouseClicked((t) -> {
                    Clipboard clipboard = Clipboard.getSystemClipboard();
                    ClipboardContent content = new ClipboardContent();
                    content.putString(messageLabel.getText());
                    clipboard.setContent(content);
                });
                Platform.runLater(() -> {
                    copyPane.setTranslateX(this.gridPane.getWidth()/2 - 20);
                    copyPane.setTranslateY(-pane.getHeight()/2+25/2);
                });


                int rows = this.gridPane.getRowCount();
                this.gridPane.add(pane, 0, rows);
                this.gridPane.add(copyPane, 0, rows);
            } else {
                if (previousMessages.isEmpty()) continue;
                String message = previousMessages.get((int) (i/2+0.5f));
                Label messageLabel = new Label(message);
                messageLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
                messageLabel.setStyle("-fx-background-color: #1DA1F2; -fx-background-radius: 5");
                messageLabel.setPadding(new Insets(5));
                messageLabel.setTextAlignment(TextAlignment.LEFT);
                messageLabel.setWrapText(true);

                StackPane pane = new StackPane(messageLabel);
                pane.setAlignment(Pos.CENTER_RIGHT);
                pane.setPrefWidth(600-35);

                this.gridPane.add(pane, 0, this.gridPane.getRowCount());

            }
        }
    }

    public void close() {
        FadeTransition fadeTransition = new FadeTransition(Duration.seconds(0.5), this.getRoot());
        fadeTransition.setFromValue(1);
        fadeTransition.setInterpolator(Interpolator.EASE_BOTH);
        fadeTransition.setToValue(0);

        ParallelTransition parallelTransition = new ParallelTransition(fadeTransition);
        parallelTransition.setOnFinished((e) -> {
            ((Stage)this.getWindow()).close();
        });
        parallelTransition.play();

    }
}
