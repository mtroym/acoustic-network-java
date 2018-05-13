package an;

public class Config {
    /*
     * configure for PHY
     * */

    public final static int PHY_PAYLOAD_LEN = 246; //Byte

    public final static int DATA_PACKAGE_MAX_LEN = PHY_PAYLOAD_LEN + 10; //Byte

    public final static int PHY_SAMPLING_RATE = 48000;

    public final static int PHY_CARRIER0_FREQ = 9000;
    public final static int PHY_CARRIER1_FREQ = 6000;

    public final static int PHY_SYMBOL_RATE = 3000;

    public final static int PHY_SAMPLES_PER_SYMBOL = PHY_SAMPLING_RATE/PHY_SYMBOL_RATE;

    public final static int PHY_SAMPLES_PER_BYTE = PHY_SAMPLES_PER_SYMBOL*4;

    public final static int PHY_PRE_SIZE = 4; //Bytes

    public final static int PHY_PRE_FREQ_0 = 2000;
    public final static int PHY_PRE_FREQ_1 = 5000;

    public final static int PHY_STATE_SYNC = 0;
    public final static int PHY_STATE_DECODE = 1;

    public final static double PHY_THRE = 3.5;
    public final static int PHY_LINEBUFFER_SIZE = 8;


    public final static int PHY_AMP = 3000;


    public static void main(String[] args){
        System.out.print(PHY_SAMPLES_PER_BYTE);
    }


}
