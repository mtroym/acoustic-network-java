package SOUND.core;

import java.math.BigDecimal;
import java.math.BigInteger;

public class SignalTools {
    private static long A;
    private static long B;
    private static int Arest;
    private static int Brest;

    public static void main(String[] args){
        double a = 11.12001;
        double b = 101.10111;
        double res1 = mulDouble(a,b);
        double res2 = divDouble(a,b);
        System.out.println(a*b);
        System.out.println(a/b);
        System.out.println(res1);
        System.out.println(res2);
    }

    private static int doubleToLongA(double a){
        String str;
        str = String.valueOf(a);
        Arest = str.indexOf(".");
        int Asmall_len = str.length()-Arest-1;
        if(Asmall_len>8){
            str = str.substring(0, str.length()-(Asmall_len-8));
            Asmall_len=8;
        }
        str = str.substring(0, str.indexOf(".")) + str.substring(Arest + 1);
        A = Long.parseLong(str);
        return Asmall_len;
    }

    private static int doubleToLongB(double b){
        String str;
        str = String.valueOf(b);
        Brest = str.indexOf(".");
        int Bsmall_len = str.length()-Brest-1;
        if(Bsmall_len>3){
            str = str.substring(0, str.length()-(Bsmall_len-3));
            Bsmall_len=3;
        }
        str = str.substring(0, str.indexOf(".")) + str.substring(Brest + 1);
        B = Long.parseLong(str);
        return Bsmall_len;
    }

    public static double divDouble(double a, double b){
        boolean flag = false;
        if(b < 0){flag = true; b=-b; if(a<0){a=-a; flag=false;}}
        int Asmall_len = doubleToLongA(a);
        int Bsmall_len = doubleToLongB(b);

        long c = 0;
        while(A >= B){
            A = A-B;
            c++;
        }
        String str = String.valueOf(c);
        int tmp = Arest - Brest;
        if(tmp<0){
            str = "0."+str;
            tmp++;
            while(tmp<0){
                str = str.substring(0,2)+ "0" + str.substring(2);
                tmp++;
            }
        }else if((tmp>0) &&(str.length()-tmp<=0)) {
            while (str.length() - tmp < 0) {
                str = str + "0";
                tmp--;
            }
        }else if((tmp>0) && ((str.length()-tmp>=1))){
            str = str.substring(0,tmp)+"."+str.substring(tmp);
        }else{
            if(a<b){
                str = "0."+str;
            }else{
                str = str.substring(0,1) +"."+str.substring(0);
            }
        }
        if(flag) {
            return (-Double.valueOf(str));
        }
        return Double.valueOf(str);
    }

    public static double mulDouble(double a, double b){
        boolean flag = false;
        if(b < 0){flag = true; b=-b; if(a<0){a=-a; flag=false;}}
        int Asmall_len = doubleToLongA(a);
        int Bsmall_len = doubleToLongB(b);

        long ANS = 0;
        int c = 0;
        while(B!= 0){  //B作为乘数；
            if((B & 1)==1){
                ANS += (A<<c);
            }
            B = B>>1;
            ++c;
        }
        String str = String.valueOf(ANS);
        String part1 = str.substring(0,str.length()-Bsmall_len-Asmall_len);
        String part2 = str.substring(str.length()-Bsmall_len-Asmall_len);
        String ans = part1+"."+part2;

        if(flag) {
            return (-Double.valueOf(ans));
        }
        return Double.valueOf(ans);
    }


    public static double[] smooth(double[] in, double[] out, int N) {
        int i = 0;
        if (N < 5) { // 五点平滑；
            System.arraycopy(in, 0, out, 0, in.length);
        } else {
            // 头两个；
            out[0] = (3.0 * in[0] + 2.0 * in[1] + in[2] - in[4]) / 5.0;
            out[1] = (4.0 * in[0] + 3.0 * in[1] + 2 * in[2] + in[3]) / 10.0;
            for (i = 2; i <= N - 3; i++) {
                out[i] = (in[i - 2] + in[i - 1] + in[i] + in[i + 1] + in[i + 2]) / 5.0; // 平均;
            }
            // 最后两个；
            out[N - 2] = (4.0 * in[N - 1] + 3.0 * in[N - 2] + 2 * in[N - 3] + in[N - 4]) / 10.0;
            out[N - 1] = (3.0 * in[N - 1] + 2.0 * in[N - 2] + in[N - 3] - in[N - 5]) / 5.0;
        }
        return out;
    }

    public static double[] bandPassFilter(double[] data, float[] para1, float[] para2) {  // IRR Filter: y[n]+sum^N_1{para1[k]y[n-k]} = sum^M_0{para2[r]x[n-r]};
        double[] result = new double[data.length];
        double[] out = new double[para1.length - 1];
        double[] in = new double[para2.length];
        float y = 0;

        for (int i = 0; i < data.length; i++) {
            System.arraycopy(in, 0, in, 1, in.length - 1);
            in[0] = data[1];
            for (int j = 0; j < para2.length; j++) {
                y += para2[j] * in[j];
            }
            for (int j = 0; j < para1.length - 1; j++) {
                y -= para1[j + 1] * out[j];
            }
            System.arraycopy(out, 0, out, 1, out.length);
            out[0] = y;
            result[i] = y;
        }
        return result;
    }
}
