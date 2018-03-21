package SOUND.core;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.LinkedList;

public class Decoder {

    private static float SAMPLE_RATE = 44100;
    private static int FRAME_SIZE = 8;
    private static float CARRIER_FREQ = 1000;

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
        return new byte[0];
    }


    public static void main(String args[]) {
        LinkedList dataList = getFile("/Users/tony/IdeaProjects/CS120-Toy/text/input.txt");
        int len = dataList.size();
        System.out.println(len);
        while (dataList.size() != 0) {
            System.out.print(dataList.pop());
        }
    }


}
