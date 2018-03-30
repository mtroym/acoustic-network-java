package SOUND.core;

import java.util.Arrays;

import static java.lang.System.exit;

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

    public static double[] addDoubleArray(double[] pre, double[] suc) {
        if (pre.length == 0) return suc;
        if (suc.length == 0) return pre;
        double ret[] = new double[pre.length + suc.length];
        System.arraycopy(pre, 0, ret, 0, pre.length);
        System.arraycopy(suc, 0, ret, pre.length, suc.length);
        return ret;
    }

    public static double[] appendDoubleArray(double[] pre, double tail) {
        double ret[] = new double[pre.length + 1];
        System.arraycopy(pre, 0, ret, 0, pre.length);
        ret[pre.length] = tail;
        return ret;
    }


    public static double[] shiftDouble(double[] pre, double tail) {
        return utils.appendDoubleArray(Arrays.copyOfRange(pre, 1, pre.length), tail);
    }

    public static double sumDoubleArray(double[] arr) {
        double sum = 0;
        for (double i : arr) {
            sum += i;
        }
        return sum;
    }

    public static double[] pointProduct(double[] pre, double[] suc) {
        if (pre.length != suc.length) exit(-1);
        double ret[] = new double[pre.length];
        for (int i = 0; i < pre.length; i++) {
            ret[i] = pre[i] * suc[i];
        }
        return ret;
    }

    public static double sumOfPointProduct(double[] arr, double[] suc) {
        return sumDoubleArray(pointProduct(arr, suc));
    }

    public static void main(String args[]) {
        double[] a = new double[10];


        System.out.print(Arrays.toString(shiftDouble(a, 1.1)));
    }
}
