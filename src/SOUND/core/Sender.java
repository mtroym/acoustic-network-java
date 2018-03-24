package SOUND.core;

import javax.sound.sampled.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static java.lang.StrictMath.round;

public class Sender {

    private static float SAMPLE_RATE = 44100;

    public Sender() {

    }

    private static byte[] task2data() {
        double time = 0.0;
        double amp = 60;
        float t = 10;
        int bufferSize = round(t * SAMPLE_RATE);
        byte[] data = new byte[bufferSize];
        double timeIncrement = 1 / SAMPLE_RATE;

        for (int i = 0; i < bufferSize; i++) {
            data[i] = (byte) (amp * (Math.sin(2 * Math.PI * 1000 * time) +
                Math.sin(2 * Math.PI * 10000 * time)));
            time += timeIncrement;
        }
        return data;
    }

    static void sendByte(byte[] audio) {
        try {
            final AudioFormat format = getFormat();
            InputStream inputStream = new ByteArrayInputStream(audio);
            final AudioInputStream audioInputStream = new AudioInputStream(inputStream,
                format, audio.length / format.getFrameSize());
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
            final SourceDataLine sourceDataLine = (SourceDataLine) AudioSystem.getLine(info);
            sourceDataLine.open(format);
            sourceDataLine.start();

            Thread sendThread = new Thread(() -> {
                int count;
                try {
                    while ((count = audioInputStream.read(audio, 0, audio.length)) != 1) {
                        if (count > 0) {
                            sourceDataLine.write(audio, 0, count);
                        }
                    }
                    sourceDataLine.drain();
                    sourceDataLine.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            sendThread.start();
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }
    }


    public static AudioFormat getFormat() {
        int SAMPLE_SIZE = 8;
        return new AudioFormat(SAMPLE_RATE,
            SAMPLE_SIZE, 1, true, true);
    }

    public static void main(String args[]) {
        byte[] data = task2data();
        System.out.println(data.length);
        sendByte(data);
    }
}