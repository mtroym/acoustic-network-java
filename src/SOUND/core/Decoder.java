package SOUND.core;

public class Decoder {

    private static float SAMPLE_RATE = 44100;
    private static int FRAME_SIZE = 100;
    private static float CARRIER1_FREQ = 3000;
    private static float CARRIER0_FREQ = 1000;
    private static int PREAMBLE_SIZE = 440;
    private static double CUTOFF_1 = 2e3;
    private static double CUTOFF_2 = 10e3;
    private static int BIT_SAMPLE = 88;
    private static int INTERVAL_BIT = 440; // ~0.01s


}
