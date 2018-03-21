package SOUND.core;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.LinkedList;

import static java.lang.System.exit;


public class Decoder {

    private static float SAMPLE_RATE = 44100;
    private static int FRAME_SIZE = 92;
    private static float CARRIER_FREQ = 1000;
    private static int PREAMBLE_SIZE = 440;
    private static double CUTOFF_1 = 2e3;
    private static double CUTOFF_2 = 10e3;
    private static double AMP_PREAMPLE = 127;


    public Decoder() {
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
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return linkedList;
    }

    private static byte[] getPreamble() {
        byte[] preamble = new byte[PREAMBLE_SIZE];
        double phaseIncre = (CUTOFF_2 - CUTOFF_1 )/ PREAMBLE_SIZE / 2;

        for (int i = 0; i < PREAMBLE_SIZE >> 1 ; i++){
            double phase = ((double) i / SAMPLE_RATE ) * (i * phaseIncre + CUTOFF_1);
            double signal = AMP_PREAMPLE * (Math.sin(2 * Math.PI * phase ));
            preamble[i] = (byte) signal;
            preamble[PREAMBLE_SIZE - i - 1] = preamble[i];
        }
        return preamble;
    }


    private static LinkedList packUp(LinkedList msg) {
        int totalSize = msg.size();
        int numPkg = (int) Math.ceil( totalSize / FRAME_SIZE);
        if (numPkg > 255) {
            exit(-4);
        }
        int skip = numPkg * FRAME_SIZE - msg.size();
        for (int i = 0; i < numPkg ; i ++){
            byte[] pkg = new byte[FRAME_SIZE + 8];
            for (int j = 0; j < FRAME_SIZE; j++){
                if ((i*FRAME_SIZE + j) < totalSize){
                    pkg[j] = (byte) msg.pop();
                    // TODO: decode as wav
                }
            }
        }
        return msg;
    }

    public static void main(String args[]) {
        LinkedList dataList = getFile("/Users/tony_mao/Develop/CS120-Toy/text/input.txt");
        int len = dataList.size();
        // TODO: Handle 10Mbit file
        System.out.println(len);
        while (dataList.size() != 0) {
            System.out.print(dataList.pop());
        }
        System.out.println("=> end debug");
        System.out.println((byte) 213.111);
        getPreamble();
    }


}
