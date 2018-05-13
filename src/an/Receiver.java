package an;

import com.sun.tools.internal.xjc.model.SymbolSpace;
import sun.awt.Symbol;

import javax.sound.sampled.*;
import java.io.PipedInputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.ListIterator;

public class Receiver implements Runnable{

    boolean runing = false;
    int state = Config.PHY_STATE_SYNC;
    private byte[] hwbuffer = new byte[2*Config.PHY_LINEBUFFER_SIZE];
    private int bufferlen;
    short[] sampleBuffer = new short[Config.PHY_LINEBUFFER_SIZE];
    private TargetDataLine targetDataLine;
    private LinkedList<Double> syncFIFO = new LinkedList<>();
    double[] decodebuffer = new double[Config.PHY_SAMPLES_PER_BYTE];
    PipedInputStream pipedOutputStream;
    private DataFrame[] dataFrames = new DataFrame[100];
    public int[] ack = new int[100];
    public long[] ackTime = new long[100];
    public int[] rec = new int[100];
    public int id;
    public int fileSize;

    Utility utility;

    public Receiver(int id){
        this.id = id;
        utility = new Utility();
        pipedOutputStream = new PipedInputStream();
        for (int i= 0;i < ack.length; i++){
            ack[i] = 0;
            rec[i] = 0;
        }
    }

    public void initLine(){
        final AudioFormat format = Utility.getFormat();
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
        try {
            targetDataLine = (TargetDataLine) AudioSystem.getLine(info);
            targetDataLine.open(format, targetDataLine.getBufferSize());
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    public void stopLine(){
        targetDataLine.flush();
        targetDataLine.stop();
        targetDataLine.close();
    }


    private void initFIFO(LinkedList<Double> l , int len){
        while(!l.isEmpty()){
            l.poll();
        }
        for (int i = 0; i< len; i ++){
            l.offer(0.0);
        }
    }

    @Override
    public void run() {
        runing = true;
        System.out.println("Start Rx!");
        targetDataLine.start();

        long indexStart = targetDataLine.getLongFramePosition();
        long indexEnd;
        initFIFO(syncFIFO, Config.PHY_SAMPLES_PER_BYTE*Config.PHY_PRE_SIZE);
        int numByteRead;
        while (runing){
            numByteRead = targetDataLine.read(hwbuffer, 0, hwbuffer.length);
            bufferlen = numByteRead;
            if (numByteRead == -1){
                System.err.println("[ERROR Receiver.java]: cannot read something");
                break;
            }
            indexEnd = targetDataLine.getLongFramePosition();
            assert (indexEnd - indexStart - numByteRead/2 <= 10000);
            decode();
        }

        // TODO end receive
    }

    private void decode(){
        ByteBuffer.wrap(hwbuffer).order(ByteOrder.BIG_ENDIAN).asShortBuffer().get(sampleBuffer);
        for (short b: sampleBuffer){
            process(b);
        }
    }

    public boolean isIdle(){
        for (short b :sampleBuffer){
            if (Math.abs(b) >= 5500){
                return false;
            }
        }
        return true;
    }

    private double power = 0;
    private double syncLocalMax = 0;
    private int startIndex = 0;
    private int indexDecode = 0;
    private int decodebufferIndex = 0;
    int decodeByteCount = 0;
    int correctDataCount = 0;
    DataFrame df = new DataFrame();
    int [] decodedBits = new int [8];
    byte decodedByte = 0;
    private byte src = -1;
    private byte dst = -1;


    private void process(short b){
        double sample = (double) b;
        indexDecode ++;
//        System.out.print(sample);System.out.print(",");

//        power = power - power/64 + sample*sample/64;

        if (state == Config.PHY_STATE_SYNC){
            shiftSyncFIFO(sample);
            double powerDebug = preambleCorr() / 5e8;
//            System.out.print(powerDebug);System.out.print(",");
//            if (powerDebug > power*2 && powerDebug > syncLocalMax && powerDebug > 0.05){
//                syncLocalMax = powerDebug;
//                startIndex = indexDecode;
//            }else if (indexDecode - startIndex > Config.PHY_SAMPLES_PER_BYTE * Config.PHY_PRE_SIZE && startIndex != 0){
            if (powerDebug > Config.PHY_THRE){
//                System.out.println("];");
//                System.out.println("???");
//                syncLocalMax = 0;
                initFIFO(syncFIFO, Config.PHY_SAMPLES_PER_BYTE*Config.PHY_PRE_SIZE);
                state = Config.PHY_STATE_DECODE;
                df.reset();
                decodebufferIndex = 0;
                decodeByteCount = 0;
            }
        }else if (state == Config.PHY_STATE_DECODE){
            decodebuffer[decodebufferIndex] = sample;
//            System.out.print(decodebufferIndex + ",");
            decodebufferIndex++;
            if (decodebufferIndex == Config.PHY_SAMPLES_PER_BYTE){
                decodebufferIndex = 0;
//                System.out.print(Arrays.toString(decodebuffer)+";");
                byte aByte = getByte();

//                System.out.print((int)aByte);
//                System.out.print(",");
                if (decodeByteCount == 0){
//                    System.out.print("pkg{id:");
                    df.setId(aByte);
//                    System.out.print(df.getId());
//                    System.out.print(",");
                    decodeByteCount ++;
                }else if(decodeByteCount == 1){
                    df.setLen(aByte);

//                    System.out.print("len:");
//                    System.out.print(df.getDataLen());
//                    System.out.print(",");
                    decodeByteCount ++;
                }else if(decodeByteCount == 2){
                    decodeByteCount ++;
                    df.setSrc(aByte);
//                    System.out.print("src:");
//                    System.out.print(aByte);
//                    System.out.print(",");
                }else if(decodeByteCount == 3){
                    df.setDst(aByte);
//                    System.out.print("dst:");
//                    System.out.print(aByte);
//                    System.out.print(",");
                    decodeByteCount ++;
                }else if(decodeByteCount == 4){
                    df.setType(aByte);
                    if (aByte > 4 || aByte < 0){
                        resetSync();
                    }
                    decodeByteCount ++;
                    if (df.getType().equals(df.types[df.TYPE_FBG])){
                        System.out.println("[Rece]=> Found file! Starting receive.");
                        resetSync();
                    }else if(df.getType().equals(df.types[df.TYPE_FED])){
                        System.out.println("[Rece]=> end file! #pkg is "+df.getId());
                        resetSync();
                    }else if(df.getType().equals(df.types[df.TYPE_ACK])){
                        if (df.getId() > 100 || df.getId() < 0){
                            resetSync();
                        }
                        System.out.println("[SEND]=> rece ACK# " + df.getId());
                        ack[df.getId()] = 1;
                        ackTime[df.getId()] = System.currentTimeMillis();
                        resetSync();
                    }
//                    System.out.print("type:");
//                    System.out.print(df.getType());
//                    System.out.print(",DataBegin\n[");
                }else if(decodeByteCount < df.getDataLen() + 5){
                    df.setDataFromIndex(aByte, decodeByteCount - 5);
//                    System.out.printf("%d, ", aByte);
                    decodeByteCount ++;
                }else if ((decodeByteCount == df.getDataLen() + 5) && (decodeByteCount-1 <= Config.PHY_PAYLOAD_LEN + 5)){
                    df.setCrc(aByte);
                    decodeByteCount++;
//                    System.out.printf("]\n, crc:%X }\n", df.getCrc());
                    // TODO CRC, MAC
                    byte crc = utility.updateCRC(df.getData(),0,df.getDataLen() -1);
                    if (aByte != crc){
                        // crcCheck
                        System.err.println("[RECE]=> CHECK WRONG CRC ! pkg #" + df.getId());
                        resetSync();
                    }
//                    System.out.println();
                    if (!df.getType().equals(df.types[df.TYPE_NCK])){
//                        System.out.println(Arrays.toString(df.getData()));
                        System.out.println("[RECE]=> Rece pkg# "+ df.getId());
                        rec[df.getId()] = 1;
                    }
                    resetSync();
                }
            }
        }else{
            System.err.println( "[ReXX]=> Unkown state!");
        }

    }


    private void resetSync(){
        decodeByteCount = 0;
        state = Config.PHY_STATE_SYNC;
        decodebufferIndex = 0;
        df.reset();
        decodebuffer = new double [Config.PHY_SAMPLES_PER_BYTE];
    }

    private int[] getBit(){
        int [] outBit = new int[8];
        int symbolSize = Config.PHY_SAMPLES_PER_SYMBOL;
        double []tempBuffer;
        for (int i = 0; i < decodebuffer.length / symbolSize; i += 1){
            tempBuffer = Arrays.copyOfRange(decodebuffer, i*symbolSize, (i+1)*symbolSize-1);
            int bit2 = decodeBit(tempBuffer);
            if (bit2 == 0){
                outBit[i*2] = 0; outBit[i*2+1] = 0;
            }else if (bit2 == 1){
                outBit[i*2] = 0; outBit[i*2+1] = 1;
            }else if (bit2 == 2){
                outBit[i*2] = 1; outBit[i*2+1] = 0;
            }else if (bit2 == 3){
                outBit[i*2] = 1; outBit[i*2+1] = 1;
            }else {
                System.err.println("Error in getBit");
            }
        }
        return outBit;
    }





    private int decodeBit(double[] buffer){
        double [] comp = new double[buffer.length];
        int bit2 = -1;
        FFT.transform(buffer.clone(), comp);
        int f = extractFrequency(comp);
        if (f == 0) {
            double corr = utility.calCorr(buffer.clone(), utility.carrier0);
            bit2 = (corr > 0) ? 0 : 1;
        }else{
            double corr = utility.calCorr(buffer.clone(), utility.carrier1);
            bit2 = (corr > 0) ? 2 : 3;
        }
        return bit2;
    }


    private int extractFrequency(double[] buffer){
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
        return maxIndex == 2 ? 1 : 0;
    }


    private byte getByte(){
        int [] bitArray = getBit();
        int a = 0;
        for (int i=0; i<8; i++) {
            if(bitArray[i] == 1) {
                a += (1<<(7-i));
            }
        }
        return (byte) a;
    }

    private void shiftSyncFIFO(double sample){
        syncFIFO.poll();
        syncFIFO.offer(sample);
    }

    private double preambleCorr(){
        int sumPower = 0;
//        Double []sync = (Double[]) syncFIFO.toArray();
        int index= 0;
        for (ListIterator<Double> iterator = syncFIFO.listIterator(); iterator.hasNext();){
            Double integer = iterator.next();
            sumPower += integer*utility.Preamble[index];
            index ++;
        }
        return sumPower;
    }
}
