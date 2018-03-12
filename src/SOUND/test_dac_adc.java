package SOUND;

import java.io.*;
import java.net.URL;
import javax.sound.sampled.*;
import java.io.ByteArrayOutputStream;
import java.security.spec.ECField;

public class test_dac_adc {
        public static int HEADER_LEN=44;
        public static double PI=Math.PI;
        public static int TIME_LEN=10;
        public static int FREQ=1000;

        public static void reader(File file){
            try{
                FileInputStream fileInputStream=new FileInputStream(file); //获取文件输入流；
                BufferedInputStream bufferedInputStream=new BufferedInputStream(fileInputStream);   //获取buf流；

                int data=0;
                int byteRead=0; //只需要header的话用这个计数到44；

                while((data=bufferedInputStream.read())!=-1){
                    System.out.println("HEXDATA: "+Integer.toString(data, 16));
                    System.out.println(String.format("BINDATA: %08d", Integer.valueOf(Integer.toString(data, 2))));
                    //++byteRead;
                }
            }catch (Exception e){
                System.err.println("Exception: "+e.getMessage());
            }
    }
    public static void sender(){
        AudioFormat audioFormat=new AudioFormat(
                AudioFormat.Encoding.PCM_SIGNED,
                44100,
                16,
                2,
                4,
                44100,
                false
        );
        try{
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
            SourceDataLine sourceDataLine = (SourceDataLine) AudioSystem.getLine(info);
            sourceDataLine.start();
            //单字节写入；
            byte[] buf=new byte[1];
            for(int i=0; i<TIME_LEN; i++){
                double angle=(2*PI*i)/(44100/FREQ);
                buf[0]=(byte)(Math.sin(angle)*127);
                sourceDataLine.write(buf,0,1);
            }
        }catch (Exception e){
            System.err.println("Exception: "+e.getMessage());
        }
    }
    public static void main(String[] args){
        File file=new File("Y:\\\\yy\\\\8.32.0.0\\\\wave\\\\voice.wav");
        reader(file);
    }
}

