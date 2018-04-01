package SOUND.core;

public class DoubleCal {
    private static long A;
    private static long B;
    private static int Arest;
    private static int Brest;

    public static void main(String[] args) {
        double a = 11.12001;
        double b = 101.10111;
        double res1 = mulDouble(a, b);
        double res2 = divDouble(a, b);
        System.out.println(a * b);
        System.out.println(a / b);
        System.out.println(res1);
        System.out.println(res2);
    }

    private static int doubleToLongA(double a) {
        String str;
        str = String.valueOf(a);
        Arest = str.indexOf(".");
        int Asmall_len = str.length() - Arest - 1;
        if (Asmall_len > 8) {
            str = str.substring(0, str.length() - (Asmall_len - 8));
            Asmall_len = 8;
        }
        str = str.substring(0, str.indexOf(".")) + str.substring(Arest + 1);
        A = Long.parseLong(str);
        return Asmall_len;
    }

    private static int doubleToLongB(double b) {
        String str;
        str = String.valueOf(b);
        Brest = str.indexOf(".");
        int Bsmall_len = str.length() - Brest - 1;
        if (Bsmall_len > 3) {
            str = str.substring(0, str.length() - (Bsmall_len - 3));
            Bsmall_len = 3;
        }
        str = str.substring(0, str.indexOf(".")) + str.substring(Brest + 1);
        B = Long.parseLong(str);
        return Bsmall_len;
    }

    public static double divDouble(double a, double b) {
        boolean flag = false;
        if (b < 0) {
            flag = true;
            b = -b;
            if (a < 0) {
                a = -a;
                flag = false;
            }
        }
        int Asmall_len = doubleToLongA(a);
        int Bsmall_len = doubleToLongB(b);

        long c = 0;
        while (A >= B) {
            A = A - B;
            c++;
        }
        String str = String.valueOf(c);
        int tmp = Arest - Brest;
        if (tmp < 0) {
            str = "0." + str;
            tmp++;
            while (tmp < 0) {
                str = str.substring(0, 2) + "0" + str.substring(2);
                tmp++;
            }
        } else if ((tmp > 0) && (str.length() - tmp <= 0)) {
            while (str.length() - tmp < 0) {
                str = str + "0";
                tmp--;
            }
        } else if ((tmp > 0) && ((str.length() - tmp >= 1))) {
            str = str.substring(0, tmp) + "." + str.substring(tmp);
        } else {
            if (a < b) {
                str = "0." + str;
            } else {
                str = str.substring(0, 1) + "." + str.substring(0);
            }
        }
        if (flag) {
            return (-Double.valueOf(str));
        }
        return Double.valueOf(str);
    }

    public static double mulDouble(double a, double b) {
        boolean flag = false;
        if (b < 0) {
            flag = true;
            b = -b;
            if (a < 0) {
                a = -a;
                flag = false;
            }
        }
        int Asmall_len = doubleToLongA(a);
        int Bsmall_len = doubleToLongB(b);

        long ANS = 0;
        int c = 0;
        while (B != 0) {  //B作为乘数；
            if ((B & 1) == 1) {
                ANS += (A << c);
            }
            B = B >> 1;
            ++c;
        }
        String str = String.valueOf(ANS);
        String part1 = str.substring(0, str.length() - Bsmall_len - Asmall_len);
        String part2 = str.substring(str.length() - Bsmall_len - Asmall_len);
        String ans = part1 + "." + part2;

        if (flag) {
            return (-Double.valueOf(ans));
        }
        return Double.valueOf(ans);
    }

}
