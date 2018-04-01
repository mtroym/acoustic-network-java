package SOUND.core;

import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.Arrays;


public class Receiver extends JFrame {
    private static float SAMPLE_RATE = Encoder.SAMPLE_RATE;
    private static int FRAME_SIZE = Encoder.FRAME_SIZE;
    private static float CARRIER1_FREQ = Encoder.CARRIER1_FREQ;
    private static float CARRIER0_FREQ = Encoder.CARRIER0_FREQ;
    private static int PREAMBLE_SIZE = Encoder.PREAMBLE_SIZE;
    private static double CUTOFF_1 = Encoder.PREAMBLE_SIZE;
    private static double CUTOFF_2 = Encoder.PREAMBLE_SIZE;
    private static int BIT_SAMPLE = Encoder.BIT_SAMPLE;
    private static byte[] FRAME0 = new byte[BIT_SAMPLE];
    private static byte[] FRAME1 = new byte[BIT_SAMPLE];
    private static int STATE;   // 0: sync; 1: decode;
    private static int BUFF_SIZE = 320;
    private static boolean inReading = true;
    private static ByteArrayOutputStream audioData;
    private static int CRC_SIZE = Encoder.CRC_SIZE;



    public Receiver() {
        super("Capture Sound Demo");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        Container content = getContentPane();
        final JButton capture = new JButton("BeginReceive");
        final JButton stop = new JButton("Stop");
        final JButton send = new JButton("send");
        capture.setEnabled(true);
        stop.setEnabled(false);
        send.setEnabled(true);

        ActionListener captureListener = e -> {
            capture.setEnabled(false);
            stop.setEnabled(true);
            send.setEnabled(true);
            inReading = true;
            captureDecodeAudio();
        };
        capture.addActionListener(captureListener);
        content.add(capture, BorderLayout.NORTH);


        ActionListener sendListener = e -> {
            stop.setEnabled(true);
            send.setEnabled(false);
            inReading = true;
            try {
                Sender.sendByte(Encoder.genSoundtrack());
            } catch (IOException e1) {
                e1.printStackTrace();
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
        };
        send.addActionListener(sendListener);
        content.add(send, BorderLayout.SOUTH);

        ActionListener stopListener = e -> {
            capture.setEnabled(true);
            stop.setEnabled(false);
            send.setEnabled(true);
            inReading = false;
//            playAudio();
//            try {
//                System.out.println("=> Opening....");
//                Decoder.decodeAudio(byteToDouble(audioData.toByteArray()));
//                System.out.println("=> End of Decode");
//            } catch (IOException e1) {
//                e1.printStackTrace();
//            }
        };
        stop.addActionListener(stopListener);
        content.add(stop, BorderLayout.CENTER);
    }

    public static double[] byteToDouble(byte[] byteArray) {
        double[] doubleArray = new double[(int) byteArray.length / 2];
        byte lowByte;
        byte highByte;
        try {
            for (int i = 0; i < doubleArray.length; i++) {
                lowByte = byteArray[i * 2];
                highByte = byteArray[i * 2 + 1];
                short tmp = (short) ((highByte & 0x00FF) << 8 | (lowByte & 0x00FF));
                if (Math.abs(tmp / 32768f) < 0.01) {
                    doubleArray[i] = 0;
                } else {
                    doubleArray[i] = tmp / 32768f;
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to turn ByteArray to DoubleArray: " + e);
            System.exit(-1);
        }
        return doubleArray;
    }

    private void captureAudio() {
        try {
            final AudioFormat format = Sender.getFormat();
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
            final TargetDataLine targetDataLine = (TargetDataLine) AudioSystem.getLine(info);
            targetDataLine.open(format);
            targetDataLine.start();

            Runnable runner = new Runnable() {
                int bufferSize = (int) format.getSampleRate() * format.getFrameSize() / 100;
                byte buffer[] = new byte[bufferSize];

                public void run() {
                    audioData = new ByteArrayOutputStream();
                    inReading = true;
                    try {
                        while (inReading) {
                            int count = targetDataLine.read(buffer, 0, buffer.length);
                            if (count > 0) {
//                                System.out.println(Arrays.toString(buffer));
                                // TODO: Decode buffer.
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


    private void captureDecodeAudio() {
        try {
            StringBuilder message = new StringBuilder();
            String current = new java.io.File(".").getCanonicalPath();
            FileWriter OUT = new FileWriter(new File(current + "/text/output.txt"));
            final AudioFormat format = Sender.getFormat();
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
            final TargetDataLine targetDataLine = (TargetDataLine) AudioSystem.getLine(info);
            targetDataLine.open(format);
            targetDataLine.start();

            Runnable runner = new Runnable() {
                int bufferSize = (int) format.getSampleRate() * format.getFrameSize();
                byte buffer[] = new byte[bufferSize];
                double[] preamble = Encoder.getDoublePreamble();
                boolean isDecoding = false;
                double[] tmpSignal;
                double[] decodeFIFO;
                double power = 0;
                int startIndex = 0;
                double[] syncFIFO = new double[PREAMBLE_SIZE];
                double syncLocalMax = 0;
                int decodeLen = 0;

                public void run() {
                    inReading = true;
                    while (inReading) {
                        int count = targetDataLine.read(buffer, 0, buffer.length);
                        if (count > 0) {
                            // TODO
                            tmpSignal = byteToDouble(buffer);
                            for (int i = 0; i < bufferSize / 2; i++) {
                                double curr = tmpSignal[i];
                                power = power * (1 - 1.0 / 64) + curr * curr / 64.0;
                                double powerDebug = utils.sumOfPointProduct(syncFIFO, preamble) / 200.0;

                                if (!isDecoding) {
                                    //SYNC
                                    syncFIFO = utils.shiftDouble(syncFIFO, curr);
                                    if (powerDebug > power * 2 && powerDebug > syncLocalMax && powerDebug > 0.05) {
                                        syncLocalMax = powerDebug;
                                        startIndex = i;
                                    } else if (i - startIndex > PREAMBLE_SIZE && startIndex != 0) {
                                        syncLocalMax = 0;
                                        syncFIFO = new double[PREAMBLE_SIZE];
                                        isDecoding = true;
                                        decodeFIFO = Arrays.copyOfRange(tmpSignal, startIndex + 1, i);
                                        decodeLen = i - startIndex;
                                    }
                                } else {
                                    //DECODING
                                    decodeLen += 1;
                                    decodeFIFO = utils.appendDoubleArray(decodeFIFO, curr);
                                    if (decodeLen == BIT_SAMPLE * (FRAME_SIZE + 8 + CRC_SIZE)) {
//                                        System.out.println("=> DECODING...");
                                        message.append(Decoder.decodeFIFOArrayStr(decodeFIFO, OUT));
                                        isDecoding = false;
                                        startIndex = 0;
                                        decodeFIFO = new double[BIT_SAMPLE * (FRAME_SIZE + 8 + CRC_SIZE)];
                                    }
                                }
                            }


                        }
                    }
                    targetDataLine.drain();
                    targetDataLine.close();
                    System.out.println(message.toString());
                }
            };
            Thread captureThread = new Thread(runner);
            captureThread.start();
        } catch (LineUnavailableException e) {
            System.err.println("Line unavailable: " + e);
            System.exit(-2);
        } catch (IOException e) {
            e.printStackTrace();
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
                int bufferSize = (int) format.getSampleRate() * format.getFrameSize() / 100 /*44100 / 100*/;
                byte buffer[] = new byte[bufferSize];

                public void run() {
                    try {
                        int count;
                        while ((count = ais.read(buffer, 0, buffer.length)) != -1) {
                            if (count > 0) {
//                                System.out.println(Arrays.toString(buffer));
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

    public static void main(String args[]) throws IOException, InterruptedException {
        JFrame frame = new Receiver();
        frame.pack();
        frame.show();
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
