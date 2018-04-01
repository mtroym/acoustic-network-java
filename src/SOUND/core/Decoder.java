package SOUND.core;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

public class Decoder {

    private static float SAMPLE_RATE = Encoder.SAMPLE_RATE;
    private static int FRAME_SIZE = Encoder.FRAME_SIZE;
    private static int PREAMBLE_SIZE = Encoder.PREAMBLE_SIZE;
    private static int BIT_SAMPLE = Encoder.BIT_SAMPLE;
    private static int INTERVAL_BIT = Encoder.INTERVAL_BIT; // ~0.01s
    private static int CRC_SIZE = Encoder.CRC_SIZE;


    public static double decodeSegment(double signal[]){
        double[] tmpReal = signal;
        FFT.transform(tmpReal, new double[signal.length]);
        return (double) utils.maxIdxOfFFT(tmpReal);
    }

    public static byte[] decodeAudio(double signal[]) throws IOException {
        String current = new java.io.File(".").getCanonicalPath();
        FileWriter FW = new FileWriter(new File(current + "/DECODE.log"));
        FileWriter OUT = new FileWriter(new File(current + "/text/output.txt"));
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
                } else if (i - startIndex > PREAMBLE_SIZE && startIndex != 0) {
//                    System.out.println("=> GOtcha! Preamble!");
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
//                    System.out.println("=> DECODING...");
//                    String current = new java.io.File(".").getCanonicalPath();
//                    FileWriter FW = new FileWriter(new File(current + "/DECODE.log"));
                    double info[] = new double[(FRAME_SIZE + 8 + CRC_SIZE)];
                    for (int decodeIdx = 0; decodeIdx < info.length; decodeIdx++) {
                        info[decodeIdx] = decodeSegment(Arrays.copyOfRange(decodeFIFO, decodeIdx * BIT_SAMPLE, (decodeIdx + 1) * BIT_SAMPLE));
                    }
                    int code[] = utils.normalizePha(info);
                    if (utils.sumInt(code) != 0){
                        OUT.write(utils.ints2String(Arrays.copyOfRange(code, 8, code.length)));
                        System.out.println("=> Received pkg #" + String.valueOf(utils.arr2Dec(Arrays.copyOfRange(code,0,8))));
                    }
                    //FW.write(Arrays.toString(code));
//                    FW.write('\n');
//                    FW.write(String.valueOf(startIndex));
//                    FW.write('\n');
//                    FW.write(Arrays.toString(decodeFIFO));
//                    FW.write('\n');
                    // TODO CRC;
                    // TODO Check pkg
                    isDecode = false;
//                    System.out.println("OK");
                    startIndex = 0;
                    decodeFIFO = new double[BIT_SAMPLE * (FRAME_SIZE + 8 + CRC_SIZE)];
                }

            }
        }
        FW.write(Arrays.toString(signal));
        FW.write('\n');
        OUT.close();
        FW.close();
        utils.ENDCHECK();
        System.out.println("=> END");
        return new byte[0];
    }

    public static int[] decodeFIFOArray(double[] decodeFIFO, FileWriter OUT) {
        double info[] = new double[(FRAME_SIZE + 8 + CRC_SIZE)];
        for (int decodeIdx = 0; decodeIdx < info.length; decodeIdx++) {
            info[decodeIdx] = Decoder.decodeSegment(Arrays.copyOfRange(decodeFIFO, decodeIdx * BIT_SAMPLE, (decodeIdx + 1) * BIT_SAMPLE));
        }
        int code[] = utils.normalizePha(info);
        if (utils.sumInt(code) != 0) {
            double pkgnum = utils.arr2Dec(Arrays.copyOfRange(code, 0, 8));
            System.out.println("=> Received pkg #" + String.valueOf(pkgnum));
//            try {
//                OUT.write(utils.ints2String(Arrays.copyOfRange(code, 8, code.length)));
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            System.out.println("=> Writen pkg #" + String.valueOf(pkgnum));
            return Arrays.copyOfRange(code, 8, code.length);
        }
        return new int[0];
    }


    public static String decodeFIFOArrayStr(double[] decodeFIFO, FileWriter OUT) {
        int[] code = decodeFIFOArray(decodeFIFO, OUT);
        return utils.ints2String(code);
    }

    public static void main(String args[]) throws IOException, InterruptedException {
        System.out.println(Arrays.toString(Receiver.byteToDouble(Encoder.getPreamble())));
    }

}
