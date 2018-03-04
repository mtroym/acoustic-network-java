package SOUND;

import javax.sound.sampled.*;
import javax.xml.crypto.Data;
import java.io.*;
import java.net.URL;

public class Recorder {

    private static Mixer mixer;
    private static Clip clip;
    private static AudioFormat format = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 8000f, 8, 1, 1, 8000f, false);


    public static void main(String[] args) {
        record(true, true,"treasure.wav", "record.wav", 3);
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

            DataLine.Info replayInfo = new DataLine.Info(javax.sound.sampled.SourceDataLine.class, format);
            final SourceDataLine sourceDataLine = (SourceDataLine) AudioSystem.getLine(replayInfo);
            sourceDataLine.open(format);

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


            // File audioFile = new File(saveFileName);
            // try {
            //     AudioSystem.write(audioStream, AudioFileFormat.Type.WAVE, audioFile);
            // } catch (IOException e) {
            //     e.printStackTrace();
            // }
            // TODO copy codes. from line 91 at Capture.java

            Thread replayThread = new Thread(() -> {
                byte data[] = outputStream.toByteArray();
                InputStream inputStream = new ByteArrayInputStream(data);
                AudioInputStream audioStream = new AudioInputStream(inputStream, format,
                        data.length / format.getFrameSize());




                sourceDataLine.start();




                while (true){
                   sourceDataLine.write(outputStream.toByteArray(),0, outputStream.size());
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
                System.out.println("=> start playback");
                replayThread.start();
                System.out.println("=> start playback 1 ");
                Thread.sleep(seconds * 1000);
                System.out.println("=> start playback 2 ");
                sourceDataLine.stop();
                System.out.println("=> start playback 3 ");
                sourceDataLine.close();
                System.out.println("=> Ended playback 4 ");
            }
            replayThread.interrupt();

            System.exit(0);

        } catch (LineUnavailableException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }



    }
}
