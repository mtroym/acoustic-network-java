package SOUND.core;

import java.util.Arrays;

public class utils {

    public static byte[] dec2ByteArr(int num, int base){
        byte ret[] = new byte[base];
        for (int i =0; i < base;i++){
            ret[base - i - 1] = (byte) (num >> i & 0x0001);
        }
        return ret;
    }

    public static void main(String args[]) {
        System.out.print(Arrays.toString(dec2ByteArr(255, 8)));
    }
}
