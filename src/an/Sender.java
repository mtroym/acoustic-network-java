package an;

import javax.sound.sampled.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Sender {
    private byte[] hwbuffer = new byte[2 * Config.PHY_LINEBUFFER_SIZE];
    private int bufferlen;
    public SourceDataLine sourceDataLine;
    Utility utility;
    DataFrame common;
    public int id;

    public Sender(int id) {
        this.id = id;
        utility = new Utility();
        common = new DataFrame();
        common.setType(common.TYPE_NCK);
    }
    public void setGoal(int dst){
        common.setSrcDst(id,dst);
    }

    public void initLine(){
        final AudioFormat format = Utility.getFormat();
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
        try {
            sourceDataLine = (SourceDataLine) AudioSystem.getLine(info);
            sourceDataLine.open(format, sourceDataLine.getBufferSize());
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }
    }


    public void stopLine(){
        sourceDataLine.flush();
        sourceDataLine.stop();
        sourceDataLine.close();
    }

    public int sendFrame(DataFrame df){
        return sendPackage(df.getData());
        // TODO CRCcly
    }

    private int sendPackage (byte[] byteData){
        int sendPreamble = sendPreamble();
        int sendDataNumByte = sendData(byteData);
        return sendDataNumByte + sendPreamble;
    }

    private int sendData (byte[] byteData) {
        int sentSample = 0;
        byte nowByte;
        int nowSymbol;
        for (byte aByteData : byteData) {
            nowByte = aByteData;
            for (int j = 3; j >= 0; j -= 1) {
                nowSymbol = ((nowByte >>> j*2) & 0X003);
                int sentTemp = sendSymbol(nowSymbol);
                if (sentTemp == -1) {
                    System.err.println("send Error");
                    return -1;
                }
                sentSample += sentTemp;
            }
        }
        return sentSample;
    }

    private int sendSymbol(int symbol){
        short [] symbolwav;
        if (symbol == 0){
            symbolwav = utility.carrier0;
        }else if (symbol == 1){
            symbolwav = utility.carrier0Neg;
        }else if (symbol == 2){
            symbolwav = utility.carrier1;
        }else{
            symbolwav = utility.carrier1Neg;
        }
        return sendWave(symbolwav);
    }

    private int sendPreamble(){
        short [] preamble = utility.Preamble;
        // 16 bit length = 4 byte
        return sendWave(preamble);
    }

    private int sendWave (short[] wave) {
        int sentSampleCount = 0;
        for (int i=0; i<wave.length; i+=Config.PHY_LINEBUFFER_SIZE) {
            ByteBuffer.wrap(hwbuffer).order(ByteOrder.BIG_ENDIAN).asShortBuffer().put(wave, i, hwbuffer.length/2);
            bufferlen = hwbuffer.length;
            int sendNum = sendBuffer();
            if (sendNum == -1) {
                System.err.println("SendBuffer Error");
                return -1;
            }
            sentSampleCount += sendNum;
        }
        return sentSampleCount;
    }


    public void sendWN(){
        common.setType(common.TYPE_NCK);
        sendFrame(common);
    }

    public void sendFileBgn(){
        common.setType(common.TYPE_FBG);
        sendFrame(common);
    }

    public void setFileEnd(int endid){
        common.setId((byte)endid);
    }

    public void sendFileEnd(){
        common.setType(common.TYPE_FED);
        sendFrame(common);
        common.setId((byte)0);
    }
    public void sendACK(int id){
        common.setId((byte)id);
        common.setType(common.TYPE_ACK);
        sendFrame(common);
        common.setId((byte)0);
    }
    private int sendBuffer(){
        return sourceDataLine.write(hwbuffer, 0, bufferlen);
    }


}