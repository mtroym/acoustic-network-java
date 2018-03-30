package SOUND.core;

import java.util.Arrays;

public class CRC {
    private static int LEN = 100; //1 CRC byte per 100 bytes;

    public static void  main(String[] args){
        byte[] data = new byte[1];
        data[0] = 0x12;
        //data[1] = 0x11;
        System.out.println(String.valueOf(FindCRC(data)));
    }

    public static int[] checkCRC(byte[] data){
        byte[] curData = new byte[100];
        byte curCRC;
        int[] errorList = new int[data.length/101]; //
        int errorCounter = 0;
        int[] noCRCret = new int[1];
        if(data.length<LEN){
            noCRCret[0] = -1;
            return noCRCret;  // -1：没找到CRC（长度不够）；
        }
        for(int i=0; i<data.length/101; i++){
            System.arraycopy(data, i*100, curData, 0, 100);
            curCRC = data[i*100+1];
            int crcVal = FindCRC(curData);
            if(crcVal!=Integer.valueOf(String.valueOf(curCRC))){
                errorList[errorCounter] = i;
                errorCounter++;
            }
        }
        if(errorCounter == 0){
            return new int[0];  // return [0] shows well;
        }
        return errorList;
    }

    public static byte[] genCRC(byte[] data){
        byte[] out = new byte[data.length+(data.length/4)];
        byte[] cur = new byte[101];
        if(data.length<LEN){
            return data;
        }
        for(int i=0; i<(data.length/100); i++){
            System.arraycopy(data, i*100, cur,0,100);
            int crcVal = FindCRC(cur);
            byte[] crcByte = hexToByte(String.valueOf(crcVal));
            System.arraycopy(crcByte,0, cur, 100, 1);   //把crc字节放到末尾；
            System.arraycopy(cur, 0, out, i*(cur.length), cur.length);
        }
        System.arraycopy(data, 100*(data.length/100), out,100*(data.length/100)+data.length/100, data.length%100 );
        return out;
    }

    public static byte[] hexToByte(String hexStr){
        final char[] hexChar = {'0', '1', '2', '3', '4', '5',
                '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
        byte[] ret;
        if(hexStr.length()==0){
            return new byte[1];
        }else{
            ret = new byte[hexStr.length() / 2]; //2n String -> n Bytes;
            for(int i=0; i<hexStr.length()/2; i++) {
                String subStr = hexStr.substring(i*2, i*2+2);
                ret[i] = (byte) Integer.parseInt(subStr, 16);
            }
        }
        return ret;
    }

    protected static int FindCRC(byte[] data){ //0x07标准CRC8；
        int CRC = 0;
        int genPoly = 0x07;
        for(int i=0;i<data.length; i++){
            CRC ^= data[i];
            for(int j=0;j<8;j++){
                if((CRC & 0x80) != 0){
                    CRC = (CRC << 1) ^ genPoly;
                }else{
                    CRC <<= 1;
                }
            }
        }
        CRC &= 0xff;
        return CRC;
    }
}