package useless;

import an.Encoder;
import an.Sender;
import an.Utility;

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
        Utility utilit = new Utility();
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        Container content = getContentPane();
        final JButton capture = new JButton("BeginReceive");
        final JButton stop = new JButton("Stop");
        final JButton capture1 = new JButton("captureSignal");
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

        ActionListener capture1Listener = e -> {
            capture.setEnabled(false);
            capture1.setEnabled(false);
            stop.setEnabled(true);
            send.setEnabled(true);
            inReading = true;
            captureAudio();
        };
        capture1.addActionListener(capture1Listener);
        content.add(capture1, BorderLayout.WEST);


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
                    doubleArray[i] = (tmp / 32768f);
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
                int bufferSize = (int) format.getSampleRate() * format.getFrameSize();
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
            final AudioFormat format = Utility.getFormat();
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
            final TargetDataLine targetDataLine = (TargetDataLine) AudioSystem.getLine(info);
            targetDataLine.open(format);
            targetDataLine.start();

            Runnable runner = new Runnable() {
                int bufferSize = (int) format.getSampleRate() * format.getFrameSize();
                byte buffer[] = new byte[bufferSize];
                short[] preambl = utilit.Preamble;
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
//                            System.out.println("=> Now is buffer #:" + String.valueOf(whileCount));
                            tmpSignal = byteToDouble(buffer);
                            for (int i = 0; i < count >> 1; i++) {
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
                                        decodeLen = 0;
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

}
