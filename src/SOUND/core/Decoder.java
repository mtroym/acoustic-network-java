package SOUND.core;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

public class Decoder {

    private static float SAMPLE_RATE = 44100;
    private static int FRAME_SIZE = 100;
    private static float CARRIER1_FREQ = 3000;
    private static float CARRIER0_FREQ = 1000;
    private static int PREAMBLE_SIZE = 440;
    private static double CUTOFF_1 = 2e3;
    private static double CUTOFF_2 = 10e3;
    private static int BIT_SAMPLE = 88;
    private static int INTERVAL_BIT = 440; // ~0.01s

    //    public static ByteArrayOutputStream outCoder;
    public static byte[] decodeAudio(double signal[]) throws IOException {
        String current = new java.io.File(".").getCanonicalPath();
        FileWriter FW = new FileWriter(new File(current + "/DECODE.log"));
        int lenAudio = signal.length;
        FW.write(String.valueOf(lenAudio));
        FW.write('\n');
        for (int i = 0; i < lenAudio - PREAMBLE_SIZE; i++) {
            double[] potentialPreamble = Arrays.copyOfRange(signal, i * PREAMBLE_SIZE, (i + 1) * PREAMBLE_SIZE);
            double lastData = potentialPreamble[0];
            double[] deltas = new double[potentialPreamble.length - 1];
            int flag = 0;
            for (int j = 1; j < PREAMBLE_SIZE - 1; j++) {
                deltas[j - 1] = potentialPreamble[j] - lastData;
                if (Math.abs(deltas[j - 1]) < 0.1) {
                    System.out.println(String.valueOf(1));
                    flag = 1;
                    break;
                }
                lastData = potentialPreamble[j];
            }
            if (flag == 0) {
                FW.write(Arrays.toString(potentialPreamble));
                FW.write('\n');
                FW.write(Arrays.toString(deltas));
            }
//            if (i == 44100){
//                System.exit(0);
//            }
        }
        FW.close();
        System.out.println("=> END");
        return new byte[0];
    }

    public static void main(String args[]) throws IOException, InterruptedException {
        System.out.println(Arrays.toString(Receiver.byteToDouble(Encoder.getPreamble())));
    }

    private int decodeSingle(double signal[]) {
        return 1;
    }
}
