package me.protonplus.lumin.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.maxmind.geoip2.WebServiceClient;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.CityResponse;
import com.maxmind.geoip2.record.City;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;

public class WeatherAPI {
    protected static final String API_TOKEN = System.getenv("WEATHER_API_TOKEN");
    protected static final String MAXMIND_LICENSE_KEY = System.getenv("MAXMIND_LICENSE_KEY");
    protected static final int MAXMIND_ACCOUNT_ID = Integer.parseInt(System.getenv("MAXMIND_ACCOUNT_ID"));
    protected static final Logger LOGGER = LogManager.getLogger();

    public static class WeatherData {
        private String weather;
        private String description;
        private Double feelsLike;
        private Double temp;
        private Double speed;
        private String direction;
        private Double gustSpeed;
        private String humidity;

        public WeatherData(String weather, String description, Double feelsLike, Double temp, Double speed, String direction, Double gustSpeed, String humidity) {
            this.weather = weather;
            this.description = description;
            this.feelsLike = feelsLike;
            this.temp = temp;
            this.speed = speed;
            this.direction = direction;
            this.gustSpeed = gustSpeed;
            this.humidity = humidity;
        }

        public String getWeather() {
            return weather;
        }

        public String getDescription() {
            return description;
        }

        public Double getFeelsLike() {
            return feelsLike;
        }

        public Double getTemp() {
            return temp;
        }

        public Double getSpeed() {
            return speed;
        }

        public String getDirection() {
            return direction;
        }

        public String getHumidity() {
            return humidity;
        }

        public Double getGustSpeed() {
            return gustSpeed;
        }

        @Override
        public String toString() {
            return "Weather: " + weather + "\n" +
                    "Description: " + description + "\n" +
                    "Feels Like: " + feelsLike + "\n" +
                    "Temperature: " + temp + "\n" +
                    "Wind Speed: " + speed + "\n" +
                    "Wind Direction: " + direction + "\n" +
                    "Gust Speed: " + gustSpeed +
                    "Humidity: " + humidity;
        }
    }

    public static void main(String[] args) throws Exception {
        getWeather();
    }

    public static WeatherData getWeather() throws Exception {
        String ipAddress = getIPAddress();
        String city = getCity(ipAddress);
        String weatherResponse = getWeather(city);

        try {
            JsonParser parser = new JsonParser();
            JsonObject JsonObject = (JsonObject) parser.parse(weatherResponse);

            JsonArray weatherArray = (JsonArray) JsonObject.get("weather");
            JsonObject mainArray = (JsonObject) JsonObject.get("main");
            JsonObject windArray = (JsonObject) JsonObject.get("wind");

            JsonObject weatherObject = (JsonObject) weatherArray.get(0);
            Long deg = (Long) windArray.get("deg").getAsLong();
            String weather = (String) weatherObject.get("main").getAsString();
            String description = (String) weatherObject.get("description").getAsString();

            Double feelsLike = (Double) mainArray.get("feels_like").getAsDouble() - 273.15d;
            Double temp = (Double) mainArray.get("temp").getAsDouble() - 273.15d;
            Long humidity = (Long) mainArray.get("humidity").getAsLong();

            Double speed = (Double) windArray.get("speed").getAsDouble() * 3.6;
            String direction = degreesToCompassDirection(deg);
            Double gustSpeed = null;
            if (windArray.has("gust")) {
                gustSpeed = (Double) windArray.get("gust").getAsDouble() * 3.6;
            }


            return new WeatherData(weather, description, feelsLike, temp, speed, direction, gustSpeed, String.valueOf(humidity) + "%");
        } catch (JsonParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String degreesToCompassDirection(double degrees) {
        if (degrees >= 337.5 || degrees < 22.5) {
            return "N";
        } else if (degrees >= 22.5 && degrees < 67.5) {
            return "NE";
        } else if (degrees >= 67.5 && degrees < 112.5) {
            return "E";
        } else if (degrees >= 112.5 && degrees < 157.5) {
            return "SE";
        } else if (degrees >= 157.5 && degrees < 202.5) {
            return "S";
        } else if (degrees >= 202.5 && degrees < 247.5) {
            return "SW";
        } else if (degrees >= 247.5 && degrees < 292.5) {
            return "W";
        } else {
            return "NW";
        }
    }
    private static String getWeather(String cityName) throws Exception {
        try {
            String apiUrl = "https://api.openweathermap.org/data/2.5/weather?q=" + cityName + "&appid=" + API_TOKEN;

            URL url = new URL(apiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();

                while ((inputLine = reader.readLine()) != null) {
                    response.append(inputLine);
                }
                reader.close();

                String weatherData = response.toString();
                LOGGER.info(weatherData);
                return weatherData;
            } else {
                LOGGER.info("HTTP GET request failed with response code: " + responseCode);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        throw new Exception();
    }
    private static String getCity(String _ipAddress) throws Exception {
        WebServiceClient client = new WebServiceClient.Builder(MAXMIND_ACCOUNT_ID, MAXMIND_LICENSE_KEY).host("geolite.info").build();
        try {
            InetAddress ipAddress = InetAddress.getByName(_ipAddress);

            CityResponse response = client.city(ipAddress);

            City city = response.getCity();
            LOGGER.info(city.getName());
            return city.getName();
        } catch (IOException | GeoIp2Exception e) {
            e.printStackTrace();
            throw new Exception();
        }
    }
    private static String getIPAddress() throws Exception {
        String apiUrl = "https://httpbin.org/ip";

        // Create an HTTP client
        HttpClient client = HttpClients.createDefault();

        // Create an HTTP GET request to the public IP service
        HttpGet request = new HttpGet(apiUrl);

        // Execute the request
        // Get the response entity
        // Retrieve the response content as a JSON string
        // Handle other response codes (e.g., 403 Forbidden for invalid credentials)

        // Check the response status code (HTTP 200 OK indicates success)
        return client.execute(request, classicHttpResponse -> {
            // Get the response entity
            String responseBody = EntityUtils.toString(classicHttpResponse.getEntity());
            JsonObject JsonObject = (JsonObject) JsonParser.parseString(responseBody);

            int statusCode = classicHttpResponse.getCode();
            if (statusCode == 200) {
                // Retrieve the response content as a JSON string

                String publicIP = (String) JsonObject.get("origin").getAsString();

                LOGGER.info("Public IP Address: {}", publicIP);
                return publicIP;
            } else {
                // Handle other response codes (e.g., 403 Forbidden for invalid credentials)
                System.err.println("HTTP Request failed with status code: " + statusCode);
            }

            return "";
        });
    }
}
