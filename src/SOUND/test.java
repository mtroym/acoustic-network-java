package SOUND;

import sun.audio.AudioPlayer;
import sun.audio.AudioStream;

import java.io.IOException;
import java.io.File;
import java.net.URL;

import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;

public class test{
    public static void main(String[] args) throws Exception, IOException {
        AudioInputStream audioInputStream;  //文件输入流；
        AudioFormat audioFormat;    //格式；
        SourceDataLine sourceDataLine;  //输出设备；
        File file=new File("Y:\\\\yy\\\\8.32.0.0\\\\wave\\\\voice.wav"); //文件路径；

        audioInputStream=AudioSystem.getAudioInputStream(file); //指定输入流；
        audioFormat=audioInputStream.getFormat();   //获取格式信息；

        //格式纠错；
        if(audioFormat.getEncoding()!=AudioFormat.Encoding.PCM_SIGNED){
            audioFormat=new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,audioFormat.getSampleRate(),16,audioFormat.getChannels(),audioFormat.getChannels()*2,audioFormat.getSampleRate(),false);
            audioInputStream=AudioSystem.getAudioInputStream(audioFormat,audioInputStream);
        }

        //打开设备；
        DataLine.Info dataLineInfo=new DataLine.Info(SourceDataLine.class,audioFormat,AudioSystem.NOT_SPECIFIED);
        sourceDataLine=(SourceDataLine)AudioSystem.getLine(dataLineInfo);
        sourceDataLine.open(audioFormat);
        sourceDataLine.start();

        //读取数据；
        byte tmpBuf[]=new byte[320];
        try {
            int c;  //counter;
            while ((c = audioInputStream.read(tmpBuf, 0,
                    tmpBuf.length)) != -1) {
                if (c > 0) {
                    // 写入缓存；
                    sourceDataLine.write(tmpBuf, 0, c);
                }
            }
            sourceDataLine.drain();
            sourceDataLine.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
    }
}
