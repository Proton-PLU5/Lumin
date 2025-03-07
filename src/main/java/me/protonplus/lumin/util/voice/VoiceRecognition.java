package me.protonplus.lumin.util.voice;

import ai.picovoice.cheetah.Cheetah;
import ai.picovoice.cheetah.CheetahException;
import ai.picovoice.cheetah.CheetahTranscript;
import ai.picovoice.porcupine.Porcupine;
import ai.picovoice.porcupine.PorcupineException;
import javafx.application.Platform;
import javafx.scene.media.AudioClip;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import me.protonplus.lumin.Lumin;
import me.protonplus.lumin.scenes.MainScene;
import me.protonplus.lumin.scenes.ScalableTextBoxV2Scene;
import me.protonplus.lumin.util.StageManager;

import javax.sound.sampled.*;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class VoiceRecognition {
    private static final String PICO_API_TOKEN = System.getenv("PICO_API_TOKEN");
    private static final String keywordPath = "/me/protonplus/lumin/data/lumine_en_windows_v3_0_0.ppn";

    public static Thread voiceThread;

    private static Cheetah cheetah;

    public static void start() {
        Lumin.LOGGER.info("Starting voice recognition...");
        voiceThread = new Thread(VoiceRecognition::startWakeListener);
        voiceThread.start();
    }

    private static String startListener(TargetDataLine micDataLine) throws LineUnavailableException {
        long totalBytesCaptured = 0;
        StringBuilder transcript = new StringBuilder();
        try {
            System.out.println("Now listening...");
            String readySoundEffect = "/me/protonplus/lumin/sounds/ready_sound_effect.mp3";
            Platform.runLater(() -> {
                AudioClip media = new AudioClip(VoiceRecognition.class.getResource(readySoundEffect).toString());
                media.setVolume(2);
                media.play();
            });

            // buffers for processing audio
            int frameLength = cheetah.getFrameLength();
            ByteBuffer captureBuffer = ByteBuffer.allocate(frameLength * 2);
            captureBuffer.order(ByteOrder.LITTLE_ENDIAN);
            short[] cheetahBuffer = new short[frameLength];
            int numBytesRead;
            while (true) {
                // read a buffer of audio
                numBytesRead = micDataLine.read(captureBuffer.array(), 0, captureBuffer.capacity());
                totalBytesCaptured += numBytesRead;

                // don't pass to cheetah if we don't have a full buffer
                if (numBytesRead != frameLength * 2) {
                    continue;
                }

                // copy into 16-bit buffer
                captureBuffer.asShortBuffer().get(cheetahBuffer);

                // process with cheetah
                CheetahTranscript transcriptObj = cheetah.process(cheetahBuffer);
                // System.out.print(transcriptObj.getTranscript());
                transcript.append(transcriptObj.getTranscript());
                if (transcriptObj.getIsEndpoint()) { Lumin.LOGGER.info("Processing Audio..."); CheetahTranscript endpointTranscriptObj = cheetah.flush();
                    transcript.append(endpointTranscriptObj.getTranscript()); break;
                }
                System.out.flush();
            }
        } catch (CheetahException e) {
            e.printStackTrace();
        }
        return transcript.toString();
    }

    private static Path extractResourceToTempFile(String resourcePath) throws IOException {
        try (InputStream resourceStream = Lumin.class.getResourceAsStream(resourcePath)) {
            if (resourceStream == null) {
                throw new IOException("Resource not found: " + resourcePath);
            }
            Path tempFile = Files.createTempFile("keyword", ".ppn");
            Files.copy(resourceStream, tempFile, StandardCopyOption.REPLACE_EXISTING);
            return tempFile;
        }
    }

    private static void startWakeListener() {
        try {
            Path keywordTempFile = extractResourceToTempFile(keywordPath);
            Porcupine porcupine = new Porcupine.Builder()
                    .setAccessKey(PICO_API_TOKEN)
                    .setKeywordPath(keywordTempFile.toString())
                    .build();

            cheetah = new Cheetah.Builder()
                    .setAccessKey(PICO_API_TOKEN)
                    .setEndpointDuration(1)
                    .build();

            AudioFormat format = new AudioFormat(16000f, 16, 1, true, false);
            DataLine.Info dataLineInfo = new DataLine.Info(TargetDataLine.class, format);
            TargetDataLine micDataLine;
            try {
                micDataLine = (TargetDataLine) AudioSystem.getLine(dataLineInfo);
                micDataLine.open(format);
            } catch (LineUnavailableException e) {
                System.err.println("Failed to get a valid audio capture device.");
                return;
            }

            // start audio capture
            micDataLine.start();
            // buffers for processing audio
            short[] porcupineBuffer = new short[porcupine.getFrameLength()];
            ByteBuffer porcupineCaptureBuffer = ByteBuffer.allocate(porcupine.getFrameLength() * 2);
            porcupineCaptureBuffer.order(ByteOrder.LITTLE_ENDIAN);

            int numBytesRead;
            boolean recordingCancelled = false;
            while (!recordingCancelled) {

                // read a buffer of audio
                numBytesRead = micDataLine.read(porcupineCaptureBuffer.array(), 0, porcupineCaptureBuffer.capacity());

                // don't pass to Picovoice if we don't have a full buffer
                if (numBytesRead != porcupine.getFrameLength() * 2) {
                    continue;
                }

                // copy into 16-bit buffer
                porcupineCaptureBuffer.asShortBuffer().get(porcupineBuffer);

                // Process with Porcupine
                int keywordIndex = porcupine.process(porcupineBuffer);
                if (keywordIndex == 0) {
                    Lumin.LOGGER.info("Wake word detected.");
                    try {
                        String output = startListener(micDataLine);

                        Platform.runLater(() -> {
                            Lumin.LOGGER.info("Processing Audio...");
                            Stage scalableTextStage = new Stage();
                            Stage mainStage = StageManager.getStage("main").get();
                            ScalableTextBoxV2Scene scalableTextBoxScene = new ScalableTextBoxV2Scene(output, 15);
                            scalableTextBoxScene.setFill(Color.TRANSPARENT);
                            scalableTextStage.setAlwaysOnTop(true);
                            scalableTextStage.initStyle(StageStyle.TRANSPARENT);
                            scalableTextStage.initOwner(mainStage);
                            scalableTextStage.setScene(scalableTextBoxScene);
                            scalableTextStage.show();

                            ((MainScene) mainStage.getScene()).addNewDialog(scalableTextStage);
                        });

                        // WitAPI.getIntent(output);
                    } catch (LineUnavailableException e) {
                        Lumin.LOGGER.warn("Could not grab microphone.");
                    }
                }
            }
        } catch (PorcupineException | CheetahException e) {
            e.printStackTrace();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
