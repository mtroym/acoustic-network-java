package SOUND;

import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.*;

import SOUND.core.Sender;


public class Task1 extends JFrame {
    private static Mixer mixer;
    private static Clip clip;
    private static AudioFormat format = Sender.getFormat();
    private static ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    private static final DataLine.Info infoTarget = new DataLine.Info(TargetDataLine.class, format);
    private static final DataLine.Info infoSource = new DataLine.Info(SourceDataLine.class, format);
    private static TargetDataLine targetDataLine;
    private static SourceDataLine sourceDataLine;
    private static int Replay = 1;
    private static File audioFile = new File("C:\\Users\\Yenene\\IdeaProjects\\CS120-Toy_1\\src\\SOUND\\treasure.wav");
    private static AudioInputStream audioStream;

    public Task1(){  //录音；
        super("Capture Sound Demo");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        Container content = getContentPane();
        final JButton task1But = new JButton("TASK1");
        final JButton task2But = new JButton("TASK2");
        task1But.setEnabled(true);
        task2But.setEnabled(true);
        ActionListener actionListener1 = e->{
            playTask(1,"treasure.wav",10);
        };
        ActionListener actionListener2 = e->{
            playTask(2,"treasure.wav",10);
        };
        task1But.addActionListener(actionListener1);
        content.add(task1But, BorderLayout.NORTH);
        task2But.addActionListener(actionListener2);
        content.add(task2But, BorderLayout.CENTER);
    }

    public static void main(String[] args) {
            JFrame jframe = new Task1();
            jframe.pack();
            jframe.show();
    }

    public class play implements Runnable{
        public void run()
        {// 播放；
            try {
                byte data[] = outputStream.toByteArray();
                final InputStream inputStream = new ByteArrayInputStream(data);
                audioStream = new AudioInputStream(inputStream, format,
                        data.length / format.getFrameSize());
                if(Replay == 0){
                    try {
                        audioStream = AudioSystem.getAudioInputStream(audioFile);
                        data = new byte[320];
                    }catch (Exception e){
                        System.out.println("Fail to load treasure.wav!\n");
                    }
                }
                sourceDataLine = (SourceDataLine) AudioSystem.getLine(infoSource);
                sourceDataLine.open(format);
                sourceDataLine.start();

                int count;
                try {
                    while ((count = audioStream.read(data, 0, data.length)) != -1) {
                        if (count > 0) {
                            sourceDataLine.write(data, 0, count);
                        }
                    }
                    sourceDataLine.drain();
                    sourceDataLine.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (LineUnavailableException e) {
                e.printStackTrace();
            }
        }
    }


    public class record implements Runnable{
        public void run() {
            try {
                targetDataLine = (TargetDataLine) AudioSystem.getLine(infoTarget);
                targetDataLine.open(format);
                targetDataLine.start();
                byte data[] = new byte[(int) (format.getSampleRate() * format.getFrameSize())];
                int readBytes;
                while (true) {
                    readBytes = targetDataLine.read(data, 0, data.length);
                    if (readBytes > 0) {
                        try {
                            outputStream.write(data, 0, readBytes);
                        } catch (Exception e) {
                            System.out.println("Fail to write recording!\n");
                        }
                    }
                }
            } catch (Exception e) {
                System.out.println(e);
            }
        }
    }


    public void playTask(int taskNum, String fileName, int seconds) {
        System.out.println("=> Start TASKs ...");
        outputStream = new ByteArrayOutputStream();     //  清零；
        if(taskNum == 1) {
            Thread recorder = new Thread(new record());
            Thread replayer = new Thread(new play());
            try {
                recorder.start();
                System.out.println("=> Start Recording ... ");
                replayer.sleep(seconds * 1000);
                recorder.interrupt();
                targetDataLine.close();
                System.out.println("=> Ended Recording Tasks ");
                System.out.println("=> Start Replaying ... ");
                replayer.start();
                recorder.sleep(seconds*1000);
                System.out.println("=> End Replaying ... ");
            }catch (Exception e){
                System.out.println(e);
            }
        }else if(taskNum == 2){
            Thread player = new Thread(new play());
            Thread recorder = new Thread(new record());
            Thread replayer = new Thread(new play());
            try {
                Replay = 0;
                player.start();
                recorder.start();
                System.out.println("=> Start Playing && Recording ... ");
                replayer.sleep(seconds * 1000);
                sourceDataLine.close();
                targetDataLine.close();
                player.stop();
                recorder.interrupt();
                System.out.println("=> End Playing && Recording ... ");
                Replay = 1;
                System.out.println("=> Start Replaying ... ");
                replayer.start();
                player.sleep(seconds*1000);
                System.out.println("=> End Replaying ... ");
            }catch (Exception e){
                System.out.println(e);
            }
        }
    }
}
