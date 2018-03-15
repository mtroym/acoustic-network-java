package SOUND;

import java.io.*;
import java.net.URL;
import javax.sound.sampled.*;
import java.io.ByteArrayOutputStream;
import java.security.spec.ECField;

public class test_dac_adc {
        public static int HEADER_LEN=44;
        public static double PI=Math.PI;
        public static int TIME_LEN=20;
        public static double FREQ=1000;
        public static int SAMPLE_RATE=44100;
        public static int CHANNELS=1;

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
                SAMPLE_RATE,
                16,
                CHANNELS,
                CHANNELS*2,
                SAMPLE_RATE,
                false
        );
        try{
            byte buf[]=new byte[TIME_LEN*8];
            DataLine.Info dataLineInfo=new DataLine.Info(SourceDataLine.class,audioFormat,AudioSystem.NOT_SPECIFIED);
            SourceDataLine sourceDataLine = (SourceDataLine) AudioSystem.getLine(dataLineInfo);
            //增加监听；
            sourceDataLine.addLineListener(new LineListener() {
                @Override
                public void update(LineEvent event) {
                    if (event.getType() == LineEvent.Type.STOP) {
                        synchronized(sourceDataLine) {
                            sourceDataLine.notify();
                        }
                    }
                }
            });
            sourceDataLine.open(audioFormat);
            sourceDataLine.start();
            //字节写入；
            double angle=(2*PI*FREQ)/SAMPLE_RATE;
            for(int i=0; i<TIME_LEN*8; i++){
                angle=(2*PI*FREQ*i)/(SAMPLE_RATE);
                buf[i]=(byte)(Math.sin(angle)*127);   //10000 is amp;
                sourceDataLine.write(buf,0,buf.length);
            }
            //保证音频结束；
            synchronized (sourceDataLine){
                sourceDataLine.wait();
            }
            //sourceDataLine.write(buf,0,buf.length);
            sourceDataLine.close();
        }catch (Exception e){
            System.err.println("Exception: "+e.getMessage());
        }
    }
    public static void main(String[] args){
        File file=new File("Y:\\\\yy\\\\8.32.0.0\\\\wave\\\\voice.wav");
        //reader(file);
        sender();
    }
}

