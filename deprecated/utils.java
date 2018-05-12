package useless;

import an.Encoder;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;

import static java.lang.System.exit;

public class utils {

    public static double arr2Dec(int[] p2){
        double dec = 0;
        for (int i = 0; i < p2.length; i++){
            dec += (1 << (p2.length - i - 1)) * p2[i];
        }
        return dec;
    }


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


    public static int maxIdxOfFFT(double[] pha) {
        double max = 0.0;
        int maxIdx = 0;
        for (int i = 0; i < pha.length / 2; i++) {
            if (Math.abs(pha[i]) > max) {
                maxIdx = i;
                max = Math.abs(pha[i]);
            }
        }
        return maxIdx;
    }


    public static double minOfArr(double[] arr) {
        double min = arr[0];
        for (int i = 1; i < arr.length; i++) {
            if (arr[i] < min) {
                min = arr[i];
            }
        }
        return min;
    }


    public static double maxOfArr(double[] arr) {
        double max = arr[0];
        for (int i = 1; i < arr.length; i++) {
            if (arr[i] > max) {
                max = arr[i];
            }
        }
        return max;
    }

    public static double avgOfArr(double[] arr) {
        double sum = arr[0];
        for (int i = 1; i < arr.length; i++) {
            sum += arr[i];
        }
        return sum / arr.length;
    }


    public static int bool2int(boolean bool) {
        if (bool) return 1;
        else return 0;
    }


    public static int[] normalizePha(double[] pha) {
        int[] normalized = new int[pha.length];
        double max = maxOfArr(pha);
        double min = minOfArr(pha);
        double mid = ( max + min ) / 2;
        for (int i = 0; i < pha.length; i++) {
            normalized[i] = bool2int(pha[i] > mid);
        }
        return normalized;
    }

    public static String ints2String(int[] code) {
        StringBuilder a = new StringBuilder();
        for (int c : code) {
            a.append(String.valueOf(c));
        }
        return a.toString();
    }

    public static double calculateErr(String src, String gen) {
        LinkedList dataList = Encoder.getFile(src);
        LinkedList checked = Encoder.getFile(gen);
        if (checked.size() < dataList.size()) {
            System.err.println("=> ERR not complete decoding...");
        }
        double err = 0;
        double total = dataList.size();
        while (dataList.size() != 0) {
            err += bool2int(dataList.pop() != checked.pop());
        }
        return err / total;
    }

    public static int sumInt(int[] input){
        int sum = 0;
        for (int i : input){
            sum += i;
        }
        return sum;
    }


    public static void ENDCHECK() throws IOException {
        String current = new java.io.File(".").getCanonicalPath();
        double err = calculateErr(current + "/text/input.txt", current + "/text/output.txt") * 100;
        System.out.println("=> Err rate is: " + String.valueOf(err) + " %");
    }

    public static void main(String args[]) throws IOException {
//        System.out.println(arr2Dec(dec2Arr(10, 8)));
//        System.out.println(ints2String(new int[0]));
        ENDCHECK();
    }
}
