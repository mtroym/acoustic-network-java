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
    private static ByteArrayOutputStream audioData;

    public static double[] smooth(double[] in, double[] out, int N){
        int i = 0;
        if(N < 5){ // 五点平滑；
            System.arraycopy(in,0,out,0,in.length);
        }else{
            // 头两个；
            out[0] = ( 3.0 * in[0] + 2.0 * in[1] + in[2] - in[4] ) / 5.0;
            out[1] = ( 4.0 * in[0] + 3.0 * in[1] + 2 * in[2] + in[3] ) / 10.0;
            for ( i=2; i <= N-3; i++ ){
                out[i] = ( in[i-2] + in[i-1] + in[i] + in[i+1] + in[i+2] ) / 5.0; // 平均;
            }
            // 最后两个；
            out[N-2] = ( 4.0 * in[N-1] + 3.0 * in[N-2] + 2 * in[N-3] + in[N-4] ) / 10.0;
            out[N-1] = ( 3.0 * in[N-1] + 2.0 * in[N-2] + in[N-3] - in[N-5] ) / 5.0;
        }
        return out;
    }

    public static double[] bandPassFilter(double[] data, float[] para1, float[] para2){  // IRR Filter: y[n]+sum^N_1{para1[k]y[n-k]} = sum^M_0{para2[r]x[n-r]};
        double[] result = new double[data.length];
        double[] out = new double[para1.length-1];
        double[] in = new double[para2.length];
        float y = 0;

        for(int i=0; i<data.length; i++){
            System.arraycopy(in,0,in,1,in.length-1);
            in[0] = data[1];
            for(int j=0; j<para2.length; j++){
                y += para2[j] * in[j];
            }
            for(int j=0; j<para1.length-1; j++){
                y -= para1[j+1] * out[j];
            }
            System.arraycopy(out,0,out,1,out.length);
            out[0] = y;
            result[i] = y;
        }
        return result;
    }

    public static double[] byteToDouble(byte[] byteArray){
        double[] doubleArray = new double[(int)Math.ceil(byteArray.length / 2)];
        byte lowByte;
        byte highByte;
        try {
            for(int i=0; i<doubleArray.length; i++){
                lowByte = byteArray[i*2];
                highByte = byteArray[i*2 + 1];
                short tmp = (short) ((lowByte & 0x00FF) << 8 | (highByte & 0x00FF));
                doubleArray[i] = tmp / 32768f;
                System.out.println(doubleArray[i]);
            }
        }catch (Exception e){
            System.err.println("Failed to turn ByteArray to DoubleArray: " + e);
            System.exit(-1);
        }
        return doubleArray;
    }


    public Receiver() {
        super("Capture Sound Demo");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        Container content = getContentPane();
        final JButton capture = new JButton("Capture");
        final JButton stop = new JButton("Stop");

        capture.setEnabled(true);
        stop.setEnabled(false);

        ActionListener captureListener = e -> {
            capture.setEnabled(false);
            stop.setEnabled(true);
            inReading = true;
            captureAudio();
        };
        capture.addActionListener(captureListener);
        content.add(capture, BorderLayout.NORTH);

        ActionListener stopListener = e -> {
            capture.setEnabled(true);
            stop.setEnabled(false);
            inReading = false;
            System.out.println(Arrays.toString(audioData.toByteArray()));
            playAudio();
        };
        stop.addActionListener(stopListener);
        content.add(stop, BorderLayout.CENTER);
    }

    private void captureAudio() {
        try {
            final AudioFormat format = Sender.getFormat();
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
            final TargetDataLine targetDataLine = (TargetDataLine) AudioSystem.getLine(info);
            targetDataLine.open(format);
            targetDataLine.start();

            Runnable runner = new Runnable() {
                int bufferSize = (int) format.getSampleRate() * format.getFrameSize();
                byte buffer[] = new byte[bufferSize];

                public void run() {
                    audioData = new ByteArrayOutputStream();
                    inReading = true;
                    try {
                        while (inReading) {
                            int count = targetDataLine.read(buffer, 0, buffer.length);
                            if (count > 0) {
                                audioData.write(buffer, 0, count);
                            }
                        }
                        audioData.close();
                        targetDataLine.drain();
                        targetDataLine.close();
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
            byte data[] = audioData.toByteArray();
            InputStream input = new ByteArrayInputStream(data);
            final AudioFormat format = Sender.getFormat();
            final AudioInputStream ais = new AudioInputStream(input, format,
                data.length / format.getFrameSize());
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
            final SourceDataLine sourceDataLine = (SourceDataLine) AudioSystem.getLine(info);
            sourceDataLine.open(format);
            sourceDataLine.start();

            Runnable runner = new Runnable() {
                int bufferSize = (int) format.getSampleRate() * format.getFrameSize();
                byte buffer[] = new byte[bufferSize];

                public void run() {
                    try {
                        int count;
                        while ((count = ais.read(buffer, 0, buffer.length)) != -1) {
                            if (count > 0) {
                                sourceDataLine.write(buffer, 0, count);
                            }
                        }
                        sourceDataLine.drain();
                        sourceDataLine.close();
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
        int frameSize = 2;
        int frameFreq = 44100;
        int channels = 1;
        boolean bigEndian = true;
        return new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, sampleRate,
            sampleSizeInBits, channels, frameSize, frameFreq, bigEndian);
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


    public static byte[] Decorder1(byte[] data){ //Suppose per block Input;
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
//            FileOutputStream FOS = new FileOutputStream(new File(current + "/text/1.pcm"));
            FileOutputStream FOS = new FileOutputStream(new File("C:\\Users\\Yenene\\Desktop\\stu\\网络\\proj\\1.pcm"));
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

}
