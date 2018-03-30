package SOUND.core;

import sun.awt.FwDispatcher;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

import static java.lang.System.exit;
import static java.lang.System.in;

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
    private static int CRC_SIZE = 0;


    public static double decodeSegment(double signal[]){
        double data = utils.sumOfPointProduct(signal,Encoder.generateDoubleWave(BIT_SAMPLE,CARRIER0_FREQ));
        return data;
    }

    //    public static ByteArrayOutputStream outCoder;
    public static byte[] decodeAudio(double signal[]) throws IOException {
//        String current = new java.io.File(".").getCanonicalPath();
//        FileWriter FW = new FileWriter(new File(current + "/DECODE.log"));
        int lenAudio = signal.length;
//        FW.write(String.valueOf(lenAudio));
//        FW.write('\n');
        double power = 0;
        double[] powerDebug = new double[lenAudio];
        int startIndex = 0;
        int[] startIndexDebug = new int[lenAudio];
        double[] syncFIFO = new double[PREAMBLE_SIZE];
        double[] syncPowerDebug = new double[lenAudio];
        double syncLocalMax = 0;


        double[] decodeFIFO = new double[BIT_SAMPLE * (FRAME_SIZE + 8 + CRC_SIZE)];
        int decodeLen = 0;

        double[] preamble = Encoder.getDoublePreamble();
        double[] signalDebug = new double[0];
        boolean isDecode = false;
        for (int i = 0; i < lenAudio; i++) {
            double curr = signal[i];
            power = power * (1 - 1.0 / 64) + (curr * curr) / 64.0;
            powerDebug[i] = power;

            if (!isDecode) {
                // SYNC
                syncFIFO = utils.shiftDouble(syncFIFO, curr);
                syncPowerDebug[i] = utils.sumOfPointProduct(syncFIFO, preamble) / 200.0;

                if ((syncPowerDebug[i] > power * 2) && (syncPowerDebug[i] > syncLocalMax) && (syncPowerDebug[i] > 0.05)) {
                    syncLocalMax = syncPowerDebug[i];
                    startIndex = i;
                } else if (i - startIndex > 400 && startIndex != 0) {
                    System.out.println("=> GOtcha! Preamble!");
                    startIndexDebug[startIndex] = 10;
                    syncLocalMax = 0;
                    syncFIFO = new double[PREAMBLE_SIZE];
                    isDecode = true;
                    decodeFIFO = Arrays.copyOfRange(signal, startIndex + 1, i);
                    decodeLen = i - startIndex;
                }
            } else {
                decodeLen += 1;
                decodeFIFO = utils.appendDoubleArray(decodeFIFO, curr);
                if (decodeLen == BIT_SAMPLE * (FRAME_SIZE + 8 + CRC_SIZE)) {
                    System.out.println("=> DECODING...");
                    String current = new java.io.File(".").getCanonicalPath();
                    FileWriter FW = new FileWriter(new File(current + "/DECODE.log"));
                    double info[] = new double[BIT_SAMPLE * (FRAME_SIZE + 8 + CRC_SIZE)];
                    for (int decodeIdx = 0; decodeIdx < decodeLen; decodeIdx ++){
                        info[i] = decodeSegment(Arrays.copyOfRange(decodeFIFO,decodeIdx*FRAME_SIZE , (decodeIdx+1)*FRAME_SIZE));
                    }
                    FW.write(Arrays.toString(info));
                    FW.write('\n');
                    FW.write(String.valueOf(startIndex));
                    FW.write('\n');
                    FW.write(Arrays.toString(decodeFIFO));
                    FW.write('\n');
                    FW.write(Arrays.toString(signal));
                    FW.write('\n');
                    isDecode = false;
                    FW.close();
                    System.out.println("OK");
                }

            }


        }
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
