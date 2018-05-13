package an;

import javax.sound.sampled.AudioFormat;
import java.util.Arrays;

public class Utility {

    // 16 sample -> 2 bit
    protected short[] Preamble = new short[Config.PHY_PRE_SIZE*Config.PHY_SAMPLES_PER_BYTE];
    protected short[] carrier0 = new short[Config.PHY_SAMPLES_PER_SYMBOL];
    protected short[] carrier1 = new short[Config.PHY_SAMPLES_PER_SYMBOL];
    protected short[] carrier0Neg = new short[Config.PHY_SAMPLES_PER_SYMBOL];
    protected short[] carrier1Neg = new short[Config.PHY_SAMPLES_PER_SYMBOL];
    protected short[] heatUp = new short[2*Config.PHY_SAMPLES_PER_BYTE];
    CRC crcUtility = new CRC();



    public Utility(){
        initWaves();
        crcUtility.reset();
    }

    public byte updateCRC(byte[] data, int from ,int len){
        crcUtility.reset();
        crcUtility.update(data,from,len+5);
        return (byte) crcUtility.get();
    }


    private void initWaves(){
        calPreamble(Preamble);
        calWave(10,0,heatUp,false);
        calWave(Config.PHY_CARRIER0_FREQ, 0, carrier0, false);
        calWave(Config.PHY_CARRIER0_FREQ, 0, carrier0Neg, true);
        calWave(Config.PHY_CARRIER1_FREQ, 0, carrier1,false);
        calWave(Config.PHY_CARRIER1_FREQ, 0, carrier1Neg,true);
    }

    private static short[] calWave(int f, double pha, short[] wave, boolean neg) {
        int sign = neg ? -1 : 1;
        for (int i = 0; i < wave.length; i++) {
            wave[i] = (short) (sign * Config.PHY_AMP * Math.sin(2 * Math.PI * f * i / Config.PHY_SAMPLING_RATE + pha));
        }
        return wave;
    }

    private static short[] calPreamble(short[] preamble) {
//        int preambleSize = Config.PHY_PRE_SIZE*Config.PHY_SAMPLES_PER_SYMBOL;
//        short[] preamble = new short[preambleSize];
        int preambleSize = preamble.length;
        double phaseIncre = (Config.PHY_PRE_FREQ_1 - Config.PHY_PRE_FREQ_0) / (preambleSize / 2 - 1);
        for (int i = 0; i < preambleSize >> 1; i++) {
            double phase = ((double) i / Config.PHY_SAMPLING_RATE) * (i * phaseIncre + Config.PHY_PRE_FREQ_1);
            short signal = (short) (Config.PHY_AMP * (Math.sin(2 * Math.PI * phase)));
            preamble[i] = signal;
            preamble[preambleSize - i - 1] = preamble[i];
        }
        return preamble;
    }

    public static AudioFormat getFormat() {
        return new AudioFormat(Config.PHY_SAMPLING_RATE, 16, 1, true, true);
    }


    int fff(double[] buffer){
        int index = 0;
        int maxIndex= 0;
        double maxValue = 0;
        for (double b: buffer){
            if (index <= buffer.length /2){
                if (Math.abs(b) > maxValue){
                    maxValue = Math.abs(b);
                    maxIndex =  index;
                }
            }
            index ++;
        }
        System.out.println("{" + maxIndex+"}");
        return maxIndex == 2 ? 1 : 0;
    }

    public double calCorr(double[] buffer, short [] tobe){
        double sum = 0;
        for (int i = 0; i < buffer.length; i ++){
            sum += buffer[i]*(double)tobe[i];
        }
        return sum;
    }


    private int decodeBit(double[] buffer){
        double [] comp = new double[buffer.length];
        int bit2 = -1;
        FFT.transform(buffer.clone(), comp);
        int f = fff(comp);
        if (f == 0) {
            double corr = calCorr(buffer, carrier0);
            System.out.print(corr);
            bit2 = (corr > 0) ? 0 : 1;
        }else{
            double corr = calCorr(buffer, carrier1);
            System.out.print(corr);
            bit2 = (corr > 0) ? 2 : 3;
        }
        return bit2;
    }

    public static void main(String args[]) {
        Utility utility = new Utility();
        utility.initWaves();
        double[] comp = new double[utility.carrier0.length];
        double[] signal = new double[utility.carrier0.length];
        for (int i = 0; i< comp.length; i++){
            signal[i] = (double) utility.carrier1[i];
        }
        System.out.println(Arrays.toString(signal));
        FFT.transform(signal.clone(), comp);
        System.out.println(Arrays.toString(comp));

        System.out.println(Arrays.toString(signal));
        System.out.print(utility.decodeBit(signal));
//        System.out.print(Arrays.toString(utility.carrier0));

    }
}

