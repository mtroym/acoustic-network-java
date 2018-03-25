package SOUND.core;

import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.Arrays;


public class Receiver extends JFrame {
    private static float SAMPLE_RATE = 44100;
    private static int FRAME_SIZE = 92;
    private static float CARRIER1_FREQ = 10000;
    private static float CARRIER0_FREQ = 5000;
    private static int PREAMBLE_SIZE = 440;
    private static double CUTOFF_1 = 2e3;
    private static double CUTOFF_2 = 10e3;
    private static double AMP_PREAMPLE = 127;
    private static int BIT_SAMPLE = 44;
    private static byte[] FRAME0 = new byte[BIT_SAMPLE];
    private static byte[] FRAME1 = new byte[BIT_SAMPLE];
    private static int STATE;   // 0: sync; 1: decode;
    private static int BUFF_SIZE = 320;
    private static boolean inReading = true;



    public static byte[] Decorder(byte[] data){ //Suppose per block Input;
        int lastSample = 0;
        int curSample = 0;
        int edge = 0;
        int edgeIndex = 0;
        int index = 0;
        int lastEdge = 0;
        int lastEdgeIndex = 0;
        float dt = 1/SAMPLE_RATE;
        double freq;
        float MARGIN = (CARRIER1_FREQ-CARRIER0_FREQ)/2;
        int BAUDRATE = 300; //波特率；
        int bpts = (int)(1.00/BAUDRATE/dt);
        int ones = 0;
        int zeros = 0;
        int tmp = 0;
        boolean STARTBIT = true;
        byte out[] = new byte[data.length];

        for(int i=0; i<data.length; i++){
            curSample = (int)data[i];
            index++;
            if(curSample-lastSample>0.4){
                edge = 1;   //Rising edge;
            }else{
                edge = 0;   //Dropping edge;
            }
            edgeIndex = index;
            if((lastEdge != -1) && (edge != -1) && (lastEdge != edge)) {
                tmp = lastEdgeIndex - edgeIndex;
                if(Math.abs(tmp)<=1){
                    System.out.println("Unknown Error!\n");
                    break;
                }

                freq = 1.00 / 2 / (Math.abs(tmp) * dt);

                if(freq > CARRIER1_FREQ-MARGIN && freq < CARRIER1_FREQ+MARGIN) {
                    ones++;
                } else if(freq > CARRIER0_FREQ-MARGIN && freq < CARRIER0_FREQ+MARGIN) {
                    zeros++;
                }
            }

            //TODO: Find out Preamble;
            int startIndex = 0;
            int bitCounter = 0;
            int bits = 0;
            boolean ModemStatusReg = false;
            if((Math.abs(tmp) >= (bpts-6)) && (ModemStatusReg & STARTBIT)) {
                bitCounter++;
                if(bitCounter > 8 || bitCounter < 1){
                    System.out.println("Something wrong\n");
                    break;
                }
                if(bitCounter==8){
                    ModemStatusReg &= !STARTBIT;
                    out[index] = (byte) bits;
                    //fprintf(outfile, "0x%x, %c\n", bits, bits);
                }else{
                    if(ones >= zeros){
                        if(bitCounter >= 1 && bitCounter <=8){
                            bits |= (0x1 << (bitCounter-1));
                        }
                    }
                }
                zeros = 0;
                ones = 0;
                startIndex = index;
            }
        }
        lastEdge = edge;
        lastEdgeIndex = index;
        lastSample = curSample;


        return out;
    }

    ByteArrayOutputStream out;


    public Receiver() {
        super("Capture Sound Demo");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        Container content = getContentPane();

        final JButton capture = new JButton("Capture");
        final JButton stop = new JButton("Stop");
//        final JButton play = new JButton("Play");

        capture.setEnabled(true);
        stop.setEnabled(false);
//        play.setEnabled(false);

        ActionListener captureListener = e -> {
            capture.setEnabled(false);
            stop.setEnabled(true);
            inReading = true;
            captureAudio();

//            play.setEnabled(false);
//            byte[] data = Recorder();
        };
        capture.addActionListener(captureListener);
        content.add(capture, BorderLayout.NORTH);

        ActionListener stopListener = e -> {
            capture.setEnabled(true);
            stop.setEnabled(false);
//            play.setEnabled(true);
            inReading = false;
//            System.out.println(out);
            System.out.println(Arrays.toString(out.toByteArray()));
            playAudio();
        };
        stop.addActionListener(stopListener);
        content.add(stop, BorderLayout.CENTER);

//    ActionListener playListener = e -> playAudio();
//        play.addActionListener(playListener);
//        content.add(play, BorderLayout.SOUTH);
    }

    public static byte[][] smooth(byte[][] data, byte[] carrier) {
        //TODO;

        return data;
    }

    public static byte sum(byte[][] data, int index0, int index1) {
        //TODO;
        byte[] ret = new byte[1];

        return ret[0];
    }

    public static byte[] Recorder(){
        final AudioFormat audioFormat = Sender.getFormat();
       // audioFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,44100,16,1,2,44100,false);
        byte data[] = new byte[8];

        try{
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, audioFormat);
            final TargetDataLine targetDataLine = (TargetDataLine) AudioSystem.getLine(info);
            targetDataLine.open(audioFormat);
            info = new DataLine.Info(SourceDataLine.class, audioFormat);
            final SourceDataLine sourceDataLine = (SourceDataLine) AudioSystem.getLine(info);
            sourceDataLine.open(audioFormat);
            final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            String current = new java.io.File(".").getCanonicalPath();
            FileOutputStream FOS = new FileOutputStream(new File(current + "/text/1.pcm"));
//            FileOutputStream FOS = new FileOutputStream(new File("C:\\Users\\Yenene\\Desktop\\stu\\网络\\proj\\1.pcm"));
            targetDataLine.start();
            sourceDataLine.start();

            int readBytes = 0;
            byte[] tmp = new byte[BUFF_SIZE];
            while (inReading && readBytes != -1) {
                readBytes = sourceDataLine.write(tmp, 0, tmp.length);
                FOS.write(tmp,0,readBytes);
                targetDataLine.read(tmp, 0, readBytes);
            }
            sourceDataLine.drain();
            sourceDataLine.close();
            targetDataLine.drain();
            targetDataLine.close();
            FOS.close();
        }catch (Exception e){
            e.printStackTrace();
        }
        return data;    //TODO: 注意现在输出是PCM文件；
    }

    public static void main(String args[]) throws IOException, InterruptedException {
//        byte[] data = Recorder();
//        Thread.sleep(1000);
//        System.out.println(Arrays.toString(data));
        JFrame frame = new Receiver();
        frame.pack();
        frame.show();
        /*
        int[] power_debug = new int[data.length];
        byte curSample;
        int power = 0;
        byte[][] decodeFIFO;
        byte[][] decodeFIFO_removecarrier;
        byte[] carrier = new byte[0]; //TODO: 载波数据；
        int startIndex = 0;
        int[] startIndexDebug = new int[data.length];
        for(int i=0; i<data.length; i++){
            curSample = data[i];
            power = power*(1-1/64) + curSample ^2/64;
            power_debug[i] = power;
            if(STATE == 0){
                // Todo: when state == 0(sync):
            }else if(STATE == 1){ //decode;
                decodeFIFO = new byte[data.length][8];
                if(decodeFIFO.length == 44*108){
                    //decode:
                    decodeFIFO_removecarrier = smooth(decodeFIFO, carrier);
                    byte[] decodeFIFO_power_bit = new byte[108];
                    for(int j=0; j<107; i++){
                        decodeFIFO_power_bit[j+1] = sum(decodeFIFO_removecarrier, 10+j*44, 30+j*44);
                    }
                    //TODO: check CRC;
                    startIndex = 0;
                    STATE = 0;
                }
            }
        }*/

    }

    private void captureAudio() {
        try {
            final AudioFormat format = Sender.getFormat();
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
            final TargetDataLine line = (TargetDataLine) AudioSystem.getLine(info);
            line.open(format);
            line.start();
            Runnable runner = new Runnable() {
                int bufferSize = (int) format.getSampleRate() * format.getFrameSize();
                byte buffer[] = new byte[bufferSize];

                public void run() {
                    out = new ByteArrayOutputStream();
                    inReading = true;
                    try {
                        while (inReading) {
                            int count = line.read(buffer, 0, buffer.length);
                            if (count > 0) {
                                out.write(buffer, 0, count);
                            }
                        }
                        out.close();
                    } catch (IOException e) {
                        System.err.println("I/O problems: " + e);
                        System.exit(-1);
                    }
                }
            };
            Thread captureThread = new Thread(runner);
            captureThread.start();
        } catch (LineUnavailableException e) {
            System.err.println("Line unavailable: " + e);
            System.exit(-2);
        }
    }

    private void playAudio() {
        try {
            byte audio[] = out.toByteArray();
            InputStream input = new ByteArrayInputStream(audio);
            final AudioFormat format = Sender.getFormat();
            final AudioInputStream ais = new AudioInputStream(input, format,
                audio.length / format.getFrameSize());
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
            final SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info);
            line.open(format);
            line.start();

            Runnable runner = new Runnable() {
                int bufferSize = (int) format.getSampleRate() * format.getFrameSize();
                byte buffer[] = new byte[bufferSize];

                public void run() {
                    try {
                        int count;
                        while ((count = ais.read(buffer, 0, buffer.length)) != -1) {
                            if (count > 0) {
                                line.write(buffer, 0, count);
                            }
                        }
                        line.drain();
                        line.close();
                    } catch (IOException e) {
                        System.err.println("I/O problems: " + e);
                        System.exit(-3);
                    }
                }
            };
            Thread playThread = new Thread(runner);
            playThread.start();
        } catch (LineUnavailableException e) {
            System.err.println("Line unavailable: " + e);
            System.exit(-4);
        }
    }

    private AudioFormat getFormat() {
        float sampleRate = 44100;
        int sampleSizeInBits = 16;
        int channels = 1;
        boolean signed = true;
        boolean bigEndian = true;
        return new AudioFormat(sampleRate,
            sampleSizeInBits, channels, signed, bigEndian);
    }

    /* 写入Txt文件 */
//    File writename = new File(".\\result\\en\\output.txt"); // 相对路径，如果没有则要建立一个新的output。txt文件
//        writename.createNewFile(); // 创建新文件
//    BufferedWriter out = new BufferedWriter(new FileWriter(writename));
//        out.write("我会写入文件啦\r\n"); // \r\n即为换行
//        out.flush(); // 把缓存区内容压入文件
//        out.close(); // 最后记得关闭文件
//
}
