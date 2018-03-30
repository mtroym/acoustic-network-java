package SOUND.core;

public class SignalTools {
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
