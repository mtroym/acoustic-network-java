package SOUND;

import javax.sound.sampled.*;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;

public class Recorder {

    private static Mixer mixer;
    private static Clip clip;


    public static void main(String[] args) {
        for (String arg : args){
            System.out.println(arg);
        }
        record(true, true,"treasure.wav", "record.wav", 10);
    }


    public static void record(boolean isPlay,boolean isReplay, String fileName, String saveFileName, int seconds) {

        System.out.println("=> Start TASKs ...");


        if (isPlay) {
            Mixer.Info[] mixInfos = AudioSystem.getMixerInfo();
            mixer = AudioSystem.getMixer(mixInfos[0]);

            DataLine.Info dataInfo = new DataLine.Info(Clip.class, null);
            try{
                clip = (Clip) mixer.getLine(dataInfo);
            } catch (LineUnavailableException e) {
                e.printStackTrace();
            }

            try {
                URL soundURL = Player.class.getResource(fileName);
                AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(soundURL);
                clip.open(audioInputStream);
            } catch (LineUnavailableException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (UnsupportedAudioFileException e) {
                e.printStackTrace();
            }

        }

        try {

            AudioFormat format = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 44100, 16, 2, 4, 44100, false);
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
            if (!AudioSystem.isLineSupported(info)) {
                System.err.println("[ERROR!] : line not supported");
            }
            TargetDataLine targetLine = (TargetDataLine) AudioSystem.getLine(info);
            targetLine.open();
            System.out.println("=> Start Recording ... ");
            targetLine.start();

            final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            Thread targetThread = new Thread(() -> {
                AudioInputStream audioStream = new AudioInputStream(targetLine);
                File audioFile = new File(saveFileName);
                try {
                    System.out.println(audioStream);
                    AudioSystem.write(audioStream, AudioFileFormat.Type.WAVE, audioFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                System.out.println("=> Stopped Recording ... ");
            });

            clip.start();
            targetThread.start();
            Thread.sleep(seconds * 1000);
            targetLine.stop();
            targetLine.close();

            System.out.println("=> Ended Sound Tasks ");
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
