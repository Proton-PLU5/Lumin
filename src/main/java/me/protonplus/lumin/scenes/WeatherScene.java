package me.protonplus.lumin.scenes;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.util.Duration;
import me.protonplus.lumin.util.LuminOperations;
import me.protonplus.lumin.util.StageManager;
import me.protonplus.lumin.util.WeatherAPI;
import me.protonplus.lumin.util.voice.VoiceRecognition;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

public class WeatherScene extends Scene {

    private static WeatherAPI.WeatherData weatherData;
    private double xOffset;
    private double yOffset;

    static class WeatherComment {
        private String weather;
        private String comment;

        public WeatherComment(String weather, String comment) {
            this.weather = weather;
            this.comment = comment;
        }

        public String getWeather() {
            return weather;
        }

        public void setWeather(String weather) {
            this.weather = weather;
        }

        public String getComment() {
            return comment;
        }

        public void setComment(String comment) {
            this.comment = comment;
        }
    }


    private static List<WeatherComment> weatherDetails = Arrays.asList(
            new WeatherComment("Clouds", "Cloudy with a chance of meatballs!"), // When it is cloudy
            new WeatherComment("Thunderstorm", "Weathering the storm!"), // During a thunderstorm
            new WeatherComment("Drizzle", "Don't rain on my parade!"), // During Drizzle
            new WeatherComment("Snow", "Let's snow what you're made of!"), // On a snowy day
            new WeatherComment("Fog", "Feeling a little under the weather?"), // When it's foggy
            new WeatherComment("Clear", "Sunny side up!"), // On a clear sky day
            new WeatherComment("Mist", "Misty with a hint of enchantment!"), // Mist
            new WeatherComment("Smoke", "Feeling a bit smoky today!"), // Smoke
            new WeatherComment("Haze", "In a hazy state of mind!"), // Haze
            new WeatherComment("Fog", "Lost in the fog of thoughts!"), // Fog
            new WeatherComment("Sand", "Getting gritty with some sand!"), // Sand
            new WeatherComment("Dust", "Embracing the dusty atmosphere!"), // Dust
            new WeatherComment("Ash", "Covered in ash and mystery!"), // Volcanic Ash
            new WeatherComment("Squalls", "Hold on tight, squalls are coming!"), // Squalls
            new WeatherComment("Tornado", "A tornado of activities awaits!"), // Tornado
            new WeatherComment("Rain", "Lets dance in the rain!") // When it is raining
    );

    private Label weatherDetailsLabel;
    public Consumer<String> updateDescription = new Consumer<String>() {
        @Override
        public void accept(String description) {
            Platform.runLater(() -> {
                if (weatherDetailsLabel != null) {
                    weatherDetailsLabel.setText(description);
                }
            });
        }
    };

    public WeatherScene(Group root) {
        super(root);
        this.setFill(Color.TRANSPARENT);
        try {
            weatherData = WeatherAPI.getWeather();
        } catch (Exception e) {
            VoiceRecognition.LOGGER.error(e);
            ScalableTextBoxV2Scene.createExpirableTextBox("Failed to get weather information, please try again later", 5);
            return;
        }


        Rectangle backgroundRect = new Rectangle(500, 238);
        backgroundRect.setArcHeight(7);
        backgroundRect.setArcWidth(7);
        backgroundRect.setFill(Color.web("E8E5EA"));

        Rectangle backgroundOutlineRect = new Rectangle(508, 246);
        backgroundOutlineRect.setArcWidth(10);
        backgroundOutlineRect.setArcHeight(10);
        backgroundOutlineRect.setFill(Color.BLACK);

        StackPane backgroundPane = new StackPane(backgroundOutlineRect, backgroundRect);

        backgroundPane.setOnMousePressed((event) -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });

        backgroundPane.setOnMouseDragged((event) -> {
            this.getWindow().setX(event.getScreenX() - xOffset);
            this.getWindow().setY(event.getScreenY() - yOffset);
        });

        ImageView weatherImage = new ImageView(getImageUrl(weatherData.getWeather()));
        weatherImage.setFitHeight(100);
        weatherImage.setFitWidth(100);
        weatherImage.setTranslateX(380-2);
        weatherImage.setTranslateY(20+2);

        LocalDate currentDate = LocalDate.now();
        Month currentMonth = currentDate.getMonth();
        ImageView seasonImage = new ImageView("me/protonplus/lumin/images/weather/season_"+getSeason(currentMonth)+".png");
        seasonImage.setFitHeight(36);
        seasonImage.setFitWidth(36);
        seasonImage.setTranslateX(161);
        seasonImage.setTranslateY(20);

        Label weatherLabel = new Label(weatherData.getWeather());
        weatherLabel.setFont(loadFont("Bold", 24));
        weatherLabel.setTranslateX(26);
        weatherLabel.setTranslateY(19);

        Label todayLabel = new Label("Today");
        todayLabel.setFont(loadFont("Bold", 12));
        todayLabel.setTranslateX(118);
        todayLabel.setTranslateY(19);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMM", Locale.ENGLISH);
        String formattedDate = currentDate.format(formatter);

        Label dateLabel = new Label(formattedDate);
        dateLabel.setFont(loadFont("Regular", 12));
        dateLabel.setTranslateX(118);
        dateLabel.setTranslateY(19+14.5);

        ImageView seperatorLine = new ImageView("me/protonplus/lumin/images/weather/separator_line.png");
        seperatorLine.setFitHeight(3);
        seperatorLine.setFitWidth(286);
        seperatorLine.setTranslateX(26);
        seperatorLine.setTranslateY(56);

        String jokeComment = "";
        for (WeatherComment comment:
             weatherDetails) {
            if (comment.getWeather().equalsIgnoreCase(weatherData.getWeather())) {
                jokeComment = comment.getComment();
                break;
            }
        }
        Label jokeLabel = new Label(jokeComment);
        jokeLabel.setFont(loadFont("Italic", 12));
        jokeLabel.setTranslateX(26);
        jokeLabel.setTranslateY(62);
        jokeLabel.setMaxWidth(286);
        jokeLabel.setTextFill(Color.web("#5E5E5E"));
        String gust = ", There are no gusts today";
        if (weatherData.getGustSpeed() != null) {
             gust = ", Gust Speed (In Kilometers/Hour): " + weatherData.getGustSpeed().intValue();
        }

        Thread responseThread = new Thread(() -> {
            String _gust = ", There are no gusts today";
            if (weatherData.getGustSpeed() != null) {
                _gust = ", Gust Speed (In Kilometers/Hour): " + weatherData.getGustSpeed().intValue();
            }
            String description = LuminOperations.getAIResponse("Write a description for the current weather make it sound casual the maximum words allowed is 35 and do not use any symbols only use the UTF-8 Character set, Here is the information you will need: " + weatherData.getWeather()
                    + ". Here is some additional details: " + "Description: " + weatherData.getDescription()
                    + ", Wind Speed (In Kilometers/Hour): " + weatherData.getSpeed().intValue()
                    + _gust
                    + ", Temperature (In Celsius): " + weatherData.getTemp().intValue()
                    + ", Feels Like (In Celsius): " + weatherData.getFeelsLike().intValue()
                    + ", Wind direction: " + weatherData.getDirection()
                    + ". Write it in the same structure as the following example: Cloudy periods with moderate rain. Easterly winds traveling at 25km/h, with strong gusts. The current temperature is 30C, but it feels like 27C. Overall its best to stay inside for today!");
            updateDescription.accept(description);
            try {
                Thread.currentThread().join(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        responseThread.start();
        weatherDetailsLabel = new Label("Loading weather information...");
        weatherDetailsLabel.setMaxWidth(286);
        weatherDetailsLabel.setWrapText(true);
        weatherDetailsLabel.setTranslateY(80);
        weatherDetailsLabel.setTranslateX(26);
        weatherDetailsLabel.setFont(loadFont("Regular", 14));

        ImageView windSpeedImage = new ImageView("me/protonplus/lumin/images/weather/wind.png");
        windSpeedImage.setFitHeight(30);
        windSpeedImage.setFitWidth(30);
        windSpeedImage.setTranslateX(26);
        windSpeedImage.setTranslateY(193);

        Label windSpeedLabel = new Label("Winds at " + weatherData.getSpeed().intValue() + "km/h. Direction: " + weatherData.getDirection() + gust);
        windSpeedLabel.setMaxWidth(126+50);
        windSpeedLabel.setWrapText(true);
        windSpeedLabel.setFont(loadFont("Regular", 12));
        windSpeedLabel.setTranslateY(193);
        windSpeedLabel.setTranslateX(64);

        ImageView humidityImage = new ImageView("me/protonplus/lumin/images/weather/humidity.png");
        humidityImage.setFitHeight(30);
        humidityImage.setFitWidth(30);
        humidityImage.setTranslateX(415);
        humidityImage.setTranslateY(193);

        Label humidityLabel = new Label(weatherData.getHumidity());
        humidityLabel.setMaxWidth(41);
        humidityLabel.setFont(loadFont("Regular", 16));
        humidityLabel.setTranslateY(199);
        humidityLabel.setTranslateX(449);
        humidityLabel.setTextAlignment(TextAlignment.RIGHT);

        ImageView closeView = new ImageView("me/protonplus/lumin/icons/close.png");
        closeView.setFitHeight(25);
        closeView.setFitWidth(25);
        Rectangle closeRect = new Rectangle(25, 25);
        closeRect.setFill(Color.TRANSPARENT);
        StackPane closePane = new StackPane(closeView, closeRect);
        closePane.setOnMouseClicked((t) -> this.close());
        Platform.runLater(() -> {
            closePane.setTranslateX(this.getWindow().getWidth()-30-2);
            closePane.setTranslateY(5+2);
        });

        root.getChildren().add(backgroundPane);
        root.getChildren().add(weatherImage);
        root.getChildren().add(seasonImage);
        root.getChildren().add(weatherLabel);
        root.getChildren().add(todayLabel);
        root.getChildren().add(dateLabel);
        root.getChildren().add(seperatorLine);
        root.getChildren().add(jokeLabel);
        root.getChildren().add(weatherDetailsLabel);
        root.getChildren().add(windSpeedImage);
        root.getChildren().add(windSpeedLabel);
        root.getChildren().add(humidityImage);
        root.getChildren().add(humidityLabel);
        root.getChildren().add(closePane);
    }



    private static String getSeason(Month month) {
        return switch (month) {
            case DECEMBER, JANUARY, FEBRUARY -> "summer";
            case MARCH, APRIL, MAY -> "autumn";
            case JUNE, JULY, AUGUST -> "winter";
            case SEPTEMBER, OCTOBER, NOVEMBER -> "spring";
        };
    }

    private String getImageUrl(String weather) {
        // Get the current time
        LocalTime currentTime = LocalTime.now();

        // Define the daytime range
        LocalTime startTime = LocalTime.of(6, 0);  // 6:00 AM
        LocalTime endTime = LocalTime.of(18, 0);  // 6:00 PM

        // Check if it's currently daytime
        boolean isDaytime = currentTime.isAfter(startTime) && currentTime.isBefore(endTime);
        weather = weather.toLowerCase();

        boolean dustWeatherCheck = weather.equalsIgnoreCase("sand") || weather.equalsIgnoreCase("ash") || weather.equalsIgnoreCase("dust");
        if (isDaytime) {
            String suffix = "";
            if (weather.equalsIgnoreCase("rain")) {
                suffix = "rain.png";
            } else if (weather.equalsIgnoreCase("fog")) {
                suffix = "fog.png";
            } else if (weather.equalsIgnoreCase("tornado")){
                suffix = "tornado.png";
            } else if (dustWeatherCheck) {
                suffix = "sandstorm.png";
            } else {
                suffix = weather+"_day.png";
            }

            return "me/protonplus/lumin/images/weather/"+suffix;
        } else {
            String suffix = "";
            if (weather.equalsIgnoreCase("rain")) {
                suffix = "rain.png";
            } else if (weather.equalsIgnoreCase("fog")) {
                suffix = "fog.png";
            } else if (weather.equalsIgnoreCase("tornado")){
                suffix = "tornado.png";
            } else if (dustWeatherCheck) {
                suffix = "sandstorm.png";
            } else {
                suffix = weather+"_night.png";
            }
            return "me/protonplus/lumin/images/weather/"+suffix;
        }
    }

    public Font loadFont(String type, int size) {
        InputStream fontStream = getClass().getResourceAsStream("/me/protonplus/lumin/fonts/Inter-"+type+".ttf");
        return Font.loadFont(fontStream, size);
    }

    public void close() {
        FadeTransition fadeTransition = new FadeTransition(Duration.seconds(0.5), this.getRoot());
        fadeTransition.setFromValue(1);
        fadeTransition.setInterpolator(Interpolator.EASE_BOTH);
        fadeTransition.setToValue(0);

        ParallelTransition parallelTransition = new ParallelTransition(fadeTransition);
        parallelTransition.setOnFinished((e) -> {
            ((MainScene) StageManager.getStage("main").get().getScene()).removeDialog(((Stage)this.getWindow()));
            ((Stage)this.getWindow()).close();
        });
        parallelTransition.play();

    }
}
