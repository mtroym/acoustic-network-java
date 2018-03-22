package SOUND.core;

import SOUND.Recorder;
import SOUND.core.Sender.*;
import com.sun.prism.impl.Disposer;
import org.omg.IOP.TAG_RMI_CUSTOM_MAX_STREAM_FORMAT;

import java.io.*;
import java.util.Arrays;
import java.util.LinkedList;
import javax.sound.sampled.*;
import java.io.IOException;

public class Receiver {
    private static float SAMPLE_RATE = 44100;
    private static int FRAME_SIZE = 92;
    private static float CARRIER1_FREQ = 10000;
    private static float CARRIER0_FREQ = 5000;
    private static int PREAMBLE_SIZE = 440;
    private static double CUTOFF_1 = 2e3;
    private static double CUTOFF_2 = 10e3;
    private static double AMP_PREAMPLE = 127;
    private static int BIT_SAMPLE = 44;
    private static byte[] FRAME0 = new byte[BIT_SAMPLE];
    private static byte[] FRAME1 = new byte[BIT_SAMPLE];
    private static int STATE;   // 0: sync; 1: decode;

    public static byte[] Decorder(byte[] data){ //Suppose per block Input;
        int lastSample = 0;
        int curSample = 0;
        int edge = 0;
        int edgeIndex = 0;
        int index = 0;
        int lastEdge = 0;
        int lastEdgeIndex = 0;
        float dt = 1/SAMPLE_RATE;
        double freq;
        float MARGIN = (CARRIER1_FREQ-CARRIER0_FREQ)/2;
        int BAUDRATE = 300; //波特率；
        int bpts = (int)(1.00/BAUDRATE/dt);
        int ones = 0;
        int zeros = 0;
        int tmp = 0;
        boolean STARTBIT = true;
        byte out[] = new byte[data.length];

        for(int i=0; i<data.length; i++){
            curSample = (int)data[i];
            index++;
            if(curSample-lastSample>0.4){
                edge = 1;   //Rising edge;
            }else{
                edge = 0;   //Dropping edge;
            }
            edgeIndex = index;
            if((lastEdge != -1) && (edge != -1) && (lastEdge != edge)) {
                tmp = lastEdgeIndex - edgeIndex;
                if(Math.abs(tmp)<=1){
                    System.out.println("Unknown Error!\n");
                    break;
                }

                freq = 1.00 / 2 / (Math.abs(tmp) * dt);

                if(freq > CARRIER1_FREQ-MARGIN && freq < CARRIER1_FREQ+MARGIN) {
                    ones++;
                } else if(freq > CARRIER0_FREQ-MARGIN && freq < CARRIER0_FREQ+MARGIN) {
                    zeros++;
                }
            }

            //TODO: Find out Preamble;
            int startIndex = 0;
            int bitCounter = 0;
            int bits = 0;
            boolean ModemStatusReg = false;
            if((Math.abs(tmp) >= (bpts-6)) && (ModemStatusReg & STARTBIT)) {
                bitCounter++;
                if(bitCounter > 8 || bitCounter < 1){
                    System.out.println("Something wrong\n");
                    break;
                }
                if(bitCounter==8){
                    ModemStatusReg &= !STARTBIT;
                    out[index] = (byte) bits;
                    //fprintf(outfile, "0x%x, %c\n", bits, bits);
                }else{
                    if(ones >= zeros){
                        if(bitCounter >= 1 && bitCounter <=8){
                            bits |= (0x1 << (bitCounter-1));
                        }
                    }
                }
                zeros = 0;
                ones = 0;
                startIndex = index;
            }
        }
        lastEdge = edge;
        lastEdgeIndex = index;
        lastSample = curSample;


        return out;
    }

    public static byte[] Recorder(){
        AudioFormat audioFormat=Sender.getFormat();
        byte data[] = new byte[4];
        byte out[] = new byte[(int) (audioFormat.getSampleRate() * audioFormat.getFrameSize())];

        try{
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, audioFormat);
            TargetDataLine targetDataLine = (TargetDataLine) AudioSystem.getLine(info);
            targetDataLine.open(audioFormat);
            info = new DataLine.Info(SourceDataLine.class, audioFormat);
            SourceDataLine sourceDataLine = (SourceDataLine) AudioSystem.getLine(info);
            sourceDataLine.open(audioFormat);
            targetDataLine.start();
            sourceDataLine.start();
            int readBytes=0;
            while (readBytes!=-1){
                readBytes = targetDataLine.read(data, 0, data.length);
                sourceDataLine.write(data, 0, readBytes);
            }
            sourceDataLine.stop();
            targetDataLine.stop();
        }catch (Exception e){
            e.printStackTrace();
        }
        return data;
    }

    public static void main(String args[]) throws IOException{
        Recorder();
    }

    /* 写入Txt文件 */
//    File writename = new File(".\\result\\en\\output.txt"); // 相对路径，如果没有则要建立一个新的output。txt文件
//        writename.createNewFile(); // 创建新文件
//    BufferedWriter out = new BufferedWriter(new FileWriter(writename));
//        out.write("我会写入文件啦\r\n"); // \r\n即为换行
//        out.flush(); // 把缓存区内容压入文件
//        out.close(); // 最后记得关闭文件
//
}
