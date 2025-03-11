package me.protonplus.lumin.scenes;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.media.AudioClip;
import javafx.stage.Stage;
import javafx.util.Duration;
import me.protonplus.lumin.util.LogoAnimationUtils;
import me.protonplus.lumin.util.StageManager;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class MainScene extends Scene {
    private double targetX, targetY;
    private double endX, endY;
    private boolean isDragging;
    private AudioClip media;
    private StackPane logoPane;

    private static List<Stage> dialogStages = new ArrayList<>();
    private static double totalDialogStagesHeight = 0;

    public static void closeTextStages() {
        Platform.runLater(() -> {
            for (Stage dialog : dialogStages) {
                if (dialog.getScene() instanceof ScalableTextBoxV2Scene scene) {
                    scene.close();
                }
            }
        });
    }

    public void addNewDialog(Stage stage) {
        Platform.runLater(() -> {
            if (dialogStages.contains(stage)) {
                return; // If the stage is already in the list, don't do anything.
            } else {

                if (dialogStages.size() > 5) {
                    Stage removedStage = dialogStages.remove(0);
                    totalDialogStagesHeight -= (removedStage.getHeight() + 10);
                }

                if (dialogStages.size() == 0) {
                    dialogStages.add(stage);
                    dialogStages.get(0).setX(this.getWindow().getX()+this.getWidth()+10);
                    dialogStages.get(0).setY(this.getWindow().getY() + this.getHeight()/2 - stage.getHeight() / 2 + totalDialogStagesHeight);
                    totalDialogStagesHeight += (stage.getHeight() - stage.getHeight()/2 + 10); // Padding of 10;
                    return;
                }

                dialogStages.add(stage);
                dialogStages.get(dialogStages.size()-1).setX(this.getWindow().getX() +this.getWidth()+10);
                dialogStages.get(dialogStages.size()-1).setY(this.getWindow().getY() + this.getHeight()/2 + totalDialogStagesHeight);
                totalDialogStagesHeight += (stage.getHeight() + 10); // Padding of 10;
            }
        });

    }

    public void removeDialog(Stage stage) {
        if (dialogStages.contains(stage)) {
            dialogStages.remove(stage);
            totalDialogStagesHeight = 0;
            for (Stage dialog:
                    dialogStages) {
                if (totalDialogStagesHeight == 0) {
                    dialog.setX(this.getWindow().getX()+this.getWidth()+10);
                    dialog.setY(this.getWindow().getY() + this.getHeight()/2 - dialog.getHeight() / 2 + totalDialogStagesHeight);
                    totalDialogStagesHeight += (dialog.getHeight() - dialog.getHeight()/2 + 10); // Padding of 10;
                } else {
                    dialog.setX(this.getWindow().getX()+this.getWidth()+10);
                    dialog.setY(this.getWindow().getY() + this.getHeight()/2 + totalDialogStagesHeight);
                    totalDialogStagesHeight += (dialog.getHeight() + 10); // Padding of 10;
                }
            }
        }
    }

    public MainScene(Group root) {
        super(root);
        this.setUpLogo(root);
        // Load media into the memory.
        Platform.runLater(() -> {
            String openMenuSoundEffect = "/me/protonplus/lumin/sounds/melancholy_ui_chime_pixabay.mp3";
            this.media = new AudioClip(this.getClass().getResource(openMenuSoundEffect).toString());
        });

//        // Load the image from the resources folder
//        String imagePath = "/me/protonplus/lumin/image.png";
//        Image image = new Image(this.getClass().getResource(imagePath).toString());
//        ImageView imageView = new ImageView(this.getClass().getResource(image);
//        imageView.setFitWidth(60);
//        imageView.setFitHeight(60);
//        StackPane stackPane = new StackPane(imageView);

        this.setOnKeyPressed(this::handleKeyPressed);
        this.setOnMousePressed(this::handleMousePressed);
        this.setOnMouseReleased(this::handleMouseReleased);
        this.setOnMouseDragged(this::handleMouseDrag);

//        root.getChildren().add(stackPane);

//        // Scale in animation
//        ScaleTransition scaleIn = new ScaleTransition(Duration.seconds(0.25), imageView);
//        scaleIn.setFromX(0);
//        scaleIn.setFromY(0);
//        scaleIn.setToX(1);
//        scaleIn.setToY(1);
//        scaleIn.setInterpolator(Interpolator.EASE_BOTH);

//        scaleIn.setOnFinished(event -> {
//            imageView.setOnMouseClicked(this::handleMouseClicked);
//        });

//        // Fade in animation
//        FadeTransition fadeIn = new FadeTransition(Duration.seconds(0.5), imageView);
//        fadeIn.setFromValue(0);
//        fadeIn.setToValue(1);
//
//        ParallelTransition introAnimation = new ParallelTransition(scaleIn, fadeIn);
//        introAnimation.play();

        Platform.runLater(() -> {
            Stage stage = StageManager.getStage("main").get();
            endX = stage.getX();
            endY = stage.getY();
        });


        AnimationTimer animationTimer = new AnimationTimer() {
            Stage stage = StageManager.getStage("main").get();
            @Override
            public void handle(long now) {
                if (isDragging) {

                    // Update the window position
                    double currentX = stage.getX();
                    double currentY = stage.getY();

                    double x = lerp(currentX, targetX, 0.1);
                    double y = lerp(currentY, targetY, 0.1);

                    stage.setX(x);
                    stage.setY(y);
                } else {
                    // Update the window position
                    double currentX = stage.getX();
                    double currentY = stage.getY();

                    double x = lerp(currentX, endX, 0.1);
                    double y = lerp(currentY, endY, 0.1);
                    stage.setX(x);
                    stage.setY(y);
                }
            }
        };
        animationTimer.start();

        Platform.runLater(() -> {this.media.play();});
    }

    private void setUpLogo(Group root) {
        ImageView luminLogoBody = new ImageView(this.getClass().getResource("/me/protonplus/lumin/images/logo/LuminLogoBody.png").toExternalForm());
        luminLogoBody.setFitWidth(53.6);
        luminLogoBody.setFitHeight(53.6);

        ImageView luminLogoBodyOutline = new ImageView(this.getClass().getResource("/me/protonplus/lumin/images/logo/LuminLogoBody.png").toExternalForm());
        luminLogoBodyOutline.setFitWidth(luminLogoBody.getFitWidth());
        luminLogoBodyOutline.setFitHeight(luminLogoBody.getFitHeight());
        luminLogoBodyOutline.setStyle("-fx-effect: innershadow(gaussian, rgba(0,0,0,1), 10, 1.0, 0, 0); fx-opacity: 1.0;");
        luminLogoBodyOutline.setScaleX(1.1);
        luminLogoBodyOutline.setScaleY(1.1);

        ImageView luminLogoLeftEar = new ImageView(this.getClass().getResource("/me/protonplus/lumin/images/logo/LuminLogoEar.png").toExternalForm());
        luminLogoLeftEar.setFitWidth(6.43);
        luminLogoLeftEar.setFitHeight(17.14);
        luminLogoLeftEar.setTranslateX(30-6.43/2);
        //luminLogoLeftEar.setY((53.6/2)-(60-53.6)/2);

        ImageView luminLogoLeftEarOutline = new ImageView(this.getClass().getResource("/me/protonplus/lumin/images/logo/LuminLogoEar.png").toExternalForm());
        luminLogoLeftEarOutline.setFitWidth(luminLogoLeftEar.getFitWidth());
        luminLogoLeftEarOutline.setFitHeight(luminLogoLeftEar.getFitHeight());
        luminLogoLeftEarOutline.setStyle("-fx-effect: innershadow(gaussian, rgba(0,0,0,1), 10, 1.0, 0, 0); fx-opacity: 1.0;");
        luminLogoLeftEarOutline.setScaleX(2);
        luminLogoLeftEarOutline.setScaleY(1.2);

        ImageView luminLogoRightEar = new ImageView(this.getClass().getResource("/me/protonplus/lumin/images/logo/LuminLogoEar.png").toExternalForm());
        luminLogoRightEar.setFitWidth(6.43);
        luminLogoRightEar.setFitHeight(17.14);
        luminLogoRightEar.setTranslateX(-30+6.43/2);
        //luminLogoRightEar.setY((53.6/2)-(60-53.6)/2);

        ImageView luminLogoRightEarOutline = new ImageView(this.getClass().getResource("/me/protonplus/lumin/images/logo/LuminLogoEar.png").toExternalForm());
        luminLogoRightEarOutline.setFitWidth(luminLogoRightEar.getFitWidth());
        luminLogoRightEarOutline.setFitHeight(luminLogoRightEar.getFitHeight());
        luminLogoRightEarOutline.setStyle("-fx-effect: innershadow(gaussian, rgba(0,0,0,1), 10, 1.0, 0, 0); fx-opacity: 1.0;");
        luminLogoRightEarOutline.setScaleX(2);
        luminLogoRightEarOutline.setScaleY(1.2);

        ImageView luminLogoFace = new ImageView(this.getClass().getResource("/me/protonplus/lumin/images/logo/LuminLogoFace.png").toExternalForm());
        luminLogoFace.setFitWidth(47.14);
        luminLogoFace.setFitHeight(32.57);
        luminLogoFace.setTranslateY((53.6/2)-(60-53.6)/2 - 32.57/2);

        double leftEyeOffset = 8.57;
        double leftEyeYOffset = (53.6/2)-(60-53.6)/2 - 32.57/2;

        ImageView luminLogoLeftEye = new ImageView(this.getClass().getResource("/me/protonplus/lumin/images/logo/LuminLogoEye0.png").toExternalForm());
        luminLogoLeftEye.setFitHeight(13.71);
        luminLogoLeftEye.setFitWidth(4.29);
        luminLogoLeftEye.setTranslateY(leftEyeYOffset);
        luminLogoLeftEye.setTranslateX(leftEyeOffset);

        double rightEyeOffset = -8.57;
        double rightEyeYOffset = (53.6/2)-(60-53.6)/2 - 32.57/2;

        ImageView luminLogoRightEye = new ImageView(this.getClass().getResource("/me/protonplus/lumin/images/logo/LuminLogoEye0.png").toExternalForm());
        luminLogoRightEye.setFitHeight(13.71);
        luminLogoRightEye.setFitWidth(4.29);
        luminLogoRightEye.setTranslateY(rightEyeYOffset);
        luminLogoRightEye.setTranslateX(rightEyeOffset);

        logoPane = new StackPane();
        logoPane.setPrefSize(65, 60);
        logoPane.getChildren().addAll(luminLogoBodyOutline, luminLogoLeftEarOutline, luminLogoLeftEar, luminLogoRightEarOutline, luminLogoRightEar, luminLogoBody , luminLogoFace, luminLogoLeftEye, luminLogoRightEye);

        // To ensure the correct size of the stage.
        BorderPane SizePane = new BorderPane();
        SizePane.setMinSize(65, 60);

        root.getChildren().add(logoPane);
        root.getChildren().add(SizePane);

        // Scale in animation
        ScaleTransition scaleIn = new ScaleTransition(Duration.seconds(0.25), logoPane);
        scaleIn.setFromX(0);
        scaleIn.setFromY(0);
        scaleIn.setToX(1);
        scaleIn.setToY(1);
        scaleIn.setInterpolator(Interpolator.EASE_BOTH);

        scaleIn.setOnFinished(event -> {
            root.setOnMouseClicked(this::handleMouseClicked);
        });

        // Fade in animation
        FadeTransition fadeIn = new FadeTransition(Duration.seconds(0.5), logoPane);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        ParallelTransition introAnimation = new ParallelTransition(scaleIn, fadeIn);
        introAnimation.play();

        Platform.runLater(() -> {
            Stage stage = StageManager.getStage("main").get();
            endX = stage.getX();
            endY = stage.getY();
        });

        AnimationTimer animationTimer = new AnimationTimer() {
            float frameDelay = 300;
            float blinkFrames = 100;
            double eyeSize = 1;
            @Override
            public void handle(long now) {
                PointerInfo pointerInfo = MouseInfo.getPointerInfo();
                Point point = pointerInfo.getLocation();

                double rootX = root.getScene().getWindow().getX();
                double rootY = root.getScene().getWindow().getY();

                int screenWidth = (int) Toolkit.getDefaultToolkit().getScreenSize().getWidth();
                int screenHeight = (int) Toolkit.getDefaultToolkit().getScreenSize().getHeight();

                double xDiff = rootX + 30 - point.getX();
                double yDiff = rootY + 30 - point.getY();

                double horizontalScalingValue = xDiff * 1/screenWidth;
                double verticalScalingValue = yDiff * 1/screenHeight;

                luminLogoLeftEye.setTranslateX(rightEyeOffset-9*horizontalScalingValue);
                luminLogoLeftEye.setTranslateY(rightEyeYOffset-9*verticalScalingValue);
                luminLogoLeftEye.setScaleY(1-Math.abs(verticalScalingValue/2));
                luminLogoLeftEye.setScaleX(1-Math.abs(horizontalScalingValue/3));

                luminLogoRightEye.setTranslateX(leftEyeOffset-9*horizontalScalingValue);
                luminLogoRightEye.setTranslateY(rightEyeYOffset-9*verticalScalingValue);
                luminLogoRightEye.setScaleY(1-Math.abs(verticalScalingValue/2));
                luminLogoRightEye.setScaleX(1-Math.abs(horizontalScalingValue/3));

                luminLogoFace.setScaleX(1-Math.abs(horizontalScalingValue/3));
                luminLogoFace.setTranslateX(9*-horizontalScalingValue);

                luminLogoLeftEar.setTranslateX(30-6.43/2 + Math.min(0,8*horizontalScalingValue));
                luminLogoLeftEarOutline.setTranslateX(luminLogoLeftEar.getTranslateX());
                luminLogoRightEar.setTranslateX(-30+6.43/2 + Math.max(0,8*horizontalScalingValue));
                luminLogoRightEarOutline.setTranslateX(luminLogoRightEar.getTranslateX());

                if (frameDelay>0) {frameDelay--;}
                else {
                    eyeSize = Math.abs(Math.cos(blinkFrames/20));
                    blinkFrames++;
                    luminLogoLeftEye.setScaleY(luminLogoLeftEye.getScaleY()*eyeSize);
                    luminLogoRightEye.setScaleY(luminLogoRightEye.getScaleY()*eyeSize);
                    if (blinkFrames>63) {
                        blinkFrames=0;
                        frameDelay=300;
                    }
                }
                //Lumin.LOGGER.info("Horizontal Value: " +horizontalScalingValue + ", Vertical Value: " + verticalScalingValue);
            }
        };
        animationTimer.start();
        Platform.runLater(() -> {
            LogoAnimationUtils.setEyeEmotion(LogoAnimationUtils.Emotion.HAPPY, luminLogoLeftEye, luminLogoRightEye);
        });
    }

    private double lerp(double start, double end, double t) {
        return start + t * (end - start);
    }

    private void handleMousePressed(MouseEvent event) {
        this.isDragging = true;
        targetX = event.getScreenX() - this.getWidth()/2;
        targetY = event.getScreenY() - this.getHeight()/2;
    }

    private void handleMouseReleased(MouseEvent event) {
        this.isDragging = false;
        endX = event.getScreenX() - this.getWidth()/2;
        endY = event.getScreenY() - this.getHeight()/2;
    }

    private void handleMouseClicked(MouseEvent event) {
        if (!event.isStillSincePress()) return;
        ScaleTransition scaleDown = new ScaleTransition(Duration.seconds(0.1), logoPane);
        scaleDown.setToX(0.75);
        scaleDown.setToY(0.75);
        scaleDown.setAutoReverse(true);

        ScaleTransition scaleUp = new ScaleTransition(Duration.seconds(0.1), logoPane);
        scaleUp.setToX(1);
        scaleUp.setToY(1);
        scaleUp.setAutoReverse(true);

        scaleDown.play();
        scaleDown.setOnFinished(e -> scaleUp.play());

        // Play soundeffect.
        String openMenuSoundEffect = "/me/protonplus/lumin/sounds/melancholy_ui_chime_pixabay.mp3";
        AudioClip media = new AudioClip(getClass().getResource(openMenuSoundEffect).toString());
        media.play();

        Stage stage = StageManager.getStage("main").get();
        double startX = stage.getX();
        double targetX;
        double screenWidth = Toolkit.getDefaultToolkit().getScreenSize().getWidth();

        // Check if Lumin is within screen bounds.
        if (screenWidth-200 < startX+this.getWidth()) {
            targetX = startX - this.getWidth() - 10; // Move the stage to the left
        } else {
            targetX = startX + this.getWidth() + 10; // Move the stage to the right
        }

        DoubleProperty xProperty = new SimpleDoubleProperty(startX);
        KeyValue keyValue = new KeyValue(xProperty, targetX);

        Duration duration = Duration.seconds(0.1);
        KeyFrame keyFrame = new KeyFrame(duration, keyValue);

        Timeline timeline = new Timeline(keyFrame);
        timeline.play();
        xProperty.addListener((observable, oldValue, newValue) -> {
            stage.setX(newValue.doubleValue());
            endX = newValue.doubleValue();
        });
    }

    private void handleMouseDrag(MouseEvent event) {
        targetX = event.getScreenX() - this.getWidth()/2;
        targetY = event.getScreenY() - this.getHeight()/2;

    }

    private void handleKeyPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.ESCAPE) {
            Stage primaryStage = (Stage) ((Scene) event.getSource()).getWindow();
            primaryStage.close();
        }
    }
}
