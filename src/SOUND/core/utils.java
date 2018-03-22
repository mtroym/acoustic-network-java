package SOUND.core;

import java.util.Arrays;

public class utils {

    public static int[] dec2Arr(int num, int base) {
        int ret[] = new int[base];
        for (int i =0; i < base;i++){
            ret[base - i - 1] = (num >> i & 0x0001);
        }
        return ret;
    }

    public static byte[] dec2byteArr(byte num, byte base) {
        byte ret[] = new byte[base];
        for (int i = 0; i < base; i++) {
            ret[base - i - 1] = (byte) (num >> i & 0x0001);
        }
        return ret;
    }

    public static byte[] addArray(byte[] pre, byte[] suc) {
        if (pre.length == 0) return suc;
        if (suc.length == 0) return pre;
        byte ret[] = new byte[pre.length + suc.length];
        System.arraycopy(pre, 0, ret, 0, pre.length);
        System.arraycopy(suc, 0, ret, pre.length, suc.length);
        return ret;
    }

    public static int[] addIntArray(int[] pre, int[] suc) {
        if (pre.length == 0) return suc;
        if (suc.length == 0) return pre;
        int ret[] = new int[pre.length + suc.length];
        System.arraycopy(pre, 0, ret, 0, pre.length);
        System.arraycopy(suc, 0, ret, pre.length, suc.length);
        return ret;
    }
    public static void main(String args[]) {
        System.out.print(Arrays.toString(dec2Arr(255, 8)));
    }
}
