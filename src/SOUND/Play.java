package SOUND;

import javax.sound.sampled.*;
import java.io.IOException;
import java.net.URL;


public class Play {
    private static Mixer mixer;
    private static Clip clip;


    public static void main(String[] args) {
        Mixer.Info[] mixInfos = AudioSystem.getMixerInfo();
//        for(Mixer.Info info : mixInfos){
//            System.out.println(info.getName() + " --- " + info.getDescription());
//        }
        mixer = AudioSystem.getMixer(mixInfos[0]);

        DataLine.Info dataInfo = new DataLine.Info(Clip.class, null);
        try {
            clip = (Clip)mixer.getLine(dataInfo);
        }catch (LineUnavailableException ex){
            ex.printStackTrace();
        }


        try {
            URL soundURL = Play.class.getResource("treasure.wav");
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(soundURL);
            clip.open(audioStream);
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (UnsupportedAudioFileException e) {
            e.printStackTrace();
        }
        clip.start();

        do {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } while (clip.isActive());
    }
}
