package SOUND;

import javax.sound.sampled.*;
import java.io.ByteArrayOutputStream;

public class SourceDataLine {

    public static void main(String[] args) {
        int ALL_TIME = 5000;
        System.out.println("Start testing Sound.....");
        AudioFormat format = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 44100, 16, 2, 4, 44100, false);

        try {
            DataLine.Info info = new DataLine.Info(javax.sound.sampled.SourceDataLine.class, format);
            final javax.sound.sampled.SourceDataLine sourceLine = (javax.sound.sampled.SourceDataLine) AudioSystem.getLine(info);
            sourceLine.open();

            info = new DataLine.Info(TargetDataLine.class, format);
            final TargetDataLine targetLine = (TargetDataLine) AudioSystem.getLine(info);
            targetLine.open();

            final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            Thread sourceThread = new Thread(() -> {
                while (true) {
                    sourceLine.write(outputStream.toByteArray(), 0, outputStream.size());
                }
            });

            Thread targetThread = new Thread(() -> {
                targetLine.start();
                byte[] data = new byte[targetLine.getBufferSize() / 5];
                int readBytes;
                while (true) {
                    readBytes = targetLine.read(data, 0, data.length);
                    outputStream.write(data, 0, readBytes);
                }

            });

            targetThread.start();
            System.out.println("start recording ...");
            Thread.sleep(ALL_TIME);
            targetLine.stop();
            targetLine.close();
            System.out.println("end recording ...");


            System.out.println("start playback ...");
            sourceThread.start();
            Thread.sleep(ALL_TIME);
            sourceLine.stop();
            sourceLine.close();
            System.out.println("Ended playback ...");

        } catch (LineUnavailableException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
