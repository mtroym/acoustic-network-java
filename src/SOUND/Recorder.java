package SOUND;

import javax.sound.sampled.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class Recorder {

    private static Mixer mixer;
    private static Clip clip;
    private static AudioFormat format = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 8000, 8, 1, 1, 8000, true);


    public static void main(String[] args) {
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

            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
            if (!AudioSystem.isLineSupported(info)) {
                System.err.println("[ERROR!] : line not supported");
            }
            final TargetDataLine targetLine = (TargetDataLine) AudioSystem.getLine(info);
            targetLine.open(format);
            System.out.println("=> Start Recording ... ");

            final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            Thread targetThread = new Thread(() -> {
                targetLine.start();
                byte data[] = new byte[(int) (format.getSampleRate() * format.getFrameSize())];
                int readBytes;
                while (true){
                    readBytes = targetLine.read(data,0,data.length);
                    if (readBytes > 0){ outputStream.write(data,0, readBytes); }
                }
               // System.out.println("=> Stopped Recording ... ");
            });

            Thread replayThread = new Thread(() -> {
                try{
                    byte data[] = outputStream.toByteArray();

                    final InputStream inputStream = new ByteArrayInputStream(data);
                    final AudioInputStream audioStream = new AudioInputStream(inputStream, format,
                            data.length / format.getFrameSize());
                    DataLine.Info replayInfo = new DataLine.Info(SourceDataLine.class, format);
                    final SourceDataLine sourceDataLine;
                    sourceDataLine = (SourceDataLine) AudioSystem.getLine(replayInfo);
                    sourceDataLine.open(format);
                    sourceDataLine.start();

                    int count;
                    try{
                        while( (count = audioStream.read(data,0,data.length) ) != -1 ){
                            if (count > 0 ){ sourceDataLine.write(data,0, count); }
                        }
                        sourceDataLine.drain();
                        sourceDataLine.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } catch (LineUnavailableException e) {
                    e.printStackTrace();
                }


            });

            clip.start();
            targetThread.start();
            Thread.sleep(seconds * 1000);
            targetLine.stop();
            targetLine.close();
            System.out.println("=> Ended Sound Tasks ");
            clip.stop();
            clip.close();
            targetThread.interrupt();

            if (isReplay) {
                System.out.println("=> Start replay Tasks ");
                replayThread.start();
                int i = 0;
                while (i < seconds){
                    Thread.sleep(1000);
                    System.out.println("=> Now is " + String.valueOf(i));
                    i ++;
                }
                System.out.println("=> Ended replay Tasks ");
            }
//            replayThread.interrupt();

            System.exit(0);

        } catch (LineUnavailableException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }



    }
}
