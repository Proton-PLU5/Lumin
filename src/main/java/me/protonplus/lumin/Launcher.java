package me.protonplus.lumin;

import me.protonplus.lumin.util.WitAPI;
import me.protonplus.lumin.util.voice.VoiceRecognition;

public class Launcher {

    public static void main(String[] args) {
        // Initialize Wit API
        WitAPI.initializeWit();


        Thread luminThread = new Thread(() -> {
            try {
                Lumin.launchApplication();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        luminThread.start();
        VoiceRecognition.start();
    }
}