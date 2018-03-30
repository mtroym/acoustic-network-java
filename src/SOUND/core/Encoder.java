package SOUND.core;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.LinkedList;

import static java.lang.System.exit;


public class Encoder {
    private static float SAMPLE_RATE = 44100;
    private static int FRAME_SIZE = 100;
    private static float CARRIER1_FREQ = 3000;
    private static float CARRIER0_FREQ = 1000;
    private static double CARRIER1_PHA = 0.5;
    private static double CARRIER0_PHA = 0;
    private static int PREAMBLE_SIZE = 440;
    private static double CUTOFF_1 = 2e3;
    private static double CUTOFF_2 = 10e3;
    private static int BIT_SAMPLE = 88;
    private static byte[] FRAME0 = new byte[BIT_SAMPLE];
    private static byte[] FRAME1 = new byte[BIT_SAMPLE];
    private static double AMPLE = 127;
    private static int INTERVAL_BIT = 440; // ~0.01s


    private static byte[] generateWave(int sample, float carrier, double phrase, int sizeAmp) {
        byte[] wave = new byte[sample * 2];
        for (int i = 0; i < sample; i++) {
            int data = (int) (/*carrier / CARRIER0_FREQ */ AMPLE * Math.sin(2 * Math.PI * carrier * i / SAMPLE_RATE));
            wave[i * 2 + 1] = (((byte) (data & 0x00FF)));
            wave[i * 2 + 0] = (((byte) ((data >> 8) & 0x00FF)));
        }
        return wave;
    }

    private static LinkedList getFile(String filePath) {
        LinkedList linkedList = new LinkedList();
        try {
            Reader r = new FileReader(filePath);
            char c = 0;
            while ((c = (char) (r.read())) != 65535) {
                if (c == (char) 10 || c == (char) 32 || c == (char) 9) {
                    continue;
                }
                assert ((c == '1') || (c == '0')) : "=> Cannot put 2 or more base";
                linkedList.add((int) c - 48);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return linkedList;
    }

    public static byte[] getPreamble() {
        byte[] preamble = new byte[PREAMBLE_SIZE * 2];
        double phaseIncre = (CUTOFF_2 - CUTOFF_1) / PREAMBLE_SIZE / 2;

        for (int i = 0; i < PREAMBLE_SIZE >> 1; i++) {
            double phase = ((double) i / SAMPLE_RATE) * (i * phaseIncre + CUTOFF_1);
            double signal = AMPLE * (Math.sin(2 * Math.PI * phase));
            preamble[i * 2] = (((byte) (((int) signal >> 8) & 0x00FF)));
            preamble[i * 2 + 1] = (((byte) ((int) signal & 0x00FF)));
            preamble[PREAMBLE_SIZE * 2 - i * 2 - 2] = preamble[i * 2 + 0];
            preamble[PREAMBLE_SIZE * 2 - i * 2 - 1] = preamble[i * 2 + 1];
        }
        return preamble;
//        byte single_pre[] = generateWave(440, 500, 0, 3);
//        return single_pre;
    }

    public static double[] getDoublePreamble() {
        double[] preamble = new double[PREAMBLE_SIZE];
        double phaseIncre = (CUTOFF_2 - CUTOFF_1) / PREAMBLE_SIZE / 2;

        for (int i = 0; i < PREAMBLE_SIZE >> 1; i++) {
            double phase = ((double) i / SAMPLE_RATE) * (i * phaseIncre + CUTOFF_1);
            double signal = AMPLE * (Math.sin(2 * Math.PI * phase));
            preamble[i] = signal;
            preamble[PREAMBLE_SIZE - i - 1] = preamble[i];
        }
        return preamble;
    }

    private static LinkedList packUp(LinkedList msg) {
        int totalSize = msg.size();
//        System.out.println(totalSize);
        int numPkg = (int) Math.ceil(totalSize / (double) FRAME_SIZE);
//        System.out.println(numPkg);
        if (numPkg > 255) {
            exit(-4);
        }
        LinkedList pkgs = new LinkedList();
        for (int i = 0; i < numPkg ; i ++){
            int[] pkg = new int[FRAME_SIZE];
            for (int j = 0; j < FRAME_SIZE; j++){
                if ((i*FRAME_SIZE + j) < totalSize){
                    pkg[j] = (int) msg.pop();
                } else {
                    pkg[j] = 0;
                }
            }
            pkg = utils.addIntArray(utils.dec2Arr(i, 8), pkg);
            // TODO: add CRC code
            pkgs.add(pkg);
//            System.out.println(Arrays.toString(pkg));
        }
        return pkgs;
    }

    public static void main(String args[]) throws IOException, InterruptedException {
        System.out.println("=> Setup carriers!!!!.....");
        FRAME0 = generateWave(BIT_SAMPLE, CARRIER0_FREQ, CARRIER0_PHA, 1);
        FRAME1 = generateWave(BIT_SAMPLE, CARRIER1_FREQ, CARRIER1_PHA, 1);
        String current = new java.io.File(".").getCanonicalPath();
        LinkedList dataList = getFile(current + "/text/input.txt");

        LinkedList pkgs = packUp(dataList);
        int len = dataList.size();
        System.out.println("=> The length of bits to be sent is " + len);


        // TODO : Send package by package.
        byte[] totalTrack = new byte[0];
        int numPkgs = pkgs.size();
        for (int i = 0; i < numPkgs; i++) {
            byte soundTrack[] = getPreamble();
            int[] singlePkg = (int[]) pkgs.pop();
            for (int bit : singlePkg) {
                if (bit == 0) {
                    soundTrack = utils.addArray(soundTrack, FRAME0);
                } else {
                    soundTrack = utils.addArray(soundTrack, FRAME1);
                }
            }
            totalTrack = utils.addArray(totalTrack, soundTrack);
//            System.out.println(Arrays.toString(soundTrack));
            byte[] zeros = new byte[INTERVAL_BIT];
            totalTrack = utils.addArray(totalTrack, zeros);
        }
//        System.out.println(Arrays.toString(totalTrack));
        Sender.sendByte(totalTrack);

        System.out.println("=> end debug");
//        System.out.println(Arrays.toString(totalTrack));
    }


}
