package SOUND.core;

public class CRC {

    public static void  main(String[] args){
        byte[] data = new byte[1];
        data[0] = 0x12;
        //data[1] = 0x11;
        System.out.println(String.valueOf(FindCRC(data)));
    }

    public static int FindCRC(byte[] data){ //0x07标准CRC8；
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