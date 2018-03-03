package SOUND;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

public class Recorder {

    public static void main(String[] args){

        System.out.println("Start testing Sound.....");

        try {
            AudioFormat format = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 44100 ,16 ,2 ,4 , 44100,false);
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
            if(!AudioSystem.isLineSupported(info)){
                System.err.println("line not supported");
            }
            TargetDataLine targetLine = (TargetDataLine)AudioSystem.getLine(info);
            targetLine.open();
            System.out.println("Start Recording ... ");
            targetLine.start();

            Thread thread;
            thread = new Thread(() -> {
                AudioInputStream audioStream = new AudioInputStream(targetLine);
                File audioFile = new File("record.wav");
                try {
                    System.out.println(audioStream);
                    AudioSystem.write(audioStream, AudioFileFormat.Type.WAVE, audioFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                System.out.println("Stopped Recording");
            });
            thread.start();
            Thread.sleep(5000);
            targetLine.stop();
            targetLine.close();

            System.out.println("Ended Sound Test! ");
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
