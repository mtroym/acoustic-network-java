package an;
import javax.xml.bind.DatatypeConverter;
import java.awt.*;
import java.util.Arrays;

/*
 * |Preamble8|id8|LEN8|SRC8|DST8|Type8|PAYLOAD|CRCcly|
 * |Preamble8|id8|emp8|SRC8|DST8|Type8|SRC|
 * */
public class DataFrame {
    public static int FRAME_SIZE = 200;
    public static int CRC_SIZE = 0;
    private byte[] data = new byte[Config.DATA_PACKAGE_MAX_LEN];
    private int dataSize;
    private byte id;
    private int len;
    private byte crc;
    private byte src;
    private byte dst;
    private int idIndex = 0;
    private int dataIndex = 5;
    private int crcIndex;
    private int srcIndex = 2;
    private int dstIndex = 3;
    private int typeIndex = 4;
    private int lenIndex = 1;
    public byte TYPE_NOM = 0;
    public byte TYPE_ACK = 1;
    public byte TYPE_NCK = 2;
    public String[] types = {"TYPE_NOM","TYPE_ACK","TYPE_NCK"};

    public DataFrame(){

    }

    public DataFrame(byte[] dataArray, int dataSize ,int id){
        this.data[0] = (byte) id;
        this.id = (byte) id;
        this.data[1] = (byte) dataSize;
        this.len =  dataSize;
        this.dataSize = dataSize;
        setData(dataArray, dataSize);
    }

    public void reset(){
        this.data = new byte[Config.DATA_PACKAGE_MAX_LEN];
        this.id = -1;
        this.len = 0;
        setSrcDst(-1,-1);
        setCrc((byte) -1);

    }

    public int getId(){
        return this.data[0];
    }
    public void setId(byte id){
        this.data[idIndex] = id;
        this.id = id;
    }
    public void setLen(byte l){
        this.len = (int) l < 0 ? (int)l + 256 : (int)l;
        this.crcIndex = this.len + 5;
        this.data[lenIndex] = l;
    }
    public void setCrc(byte crc) {
        this.crc = crc;
        this.data[crcIndex] = crc;
    }
    public void setSrc(byte src) {
        this.src = src;
        this.data[srcIndex] = src;
    }

    public void setDst(byte dst) {
        this.dst = dst;
        this.data[dstIndex] = dst;
    }
    public byte getCrc(){
        return this.crc;
    }
    public byte[] getData() {
        return data;
    }

    public int getDataLen() {
        return this.len;
    }

    public void setSrcDst(int src, int dst){
        this.data[srcIndex] = (byte) src;
        this.data[dstIndex] = (byte) dst;
    }

    public void setDataFromIndex(byte data, int index) {
        this.data[index + dataIndex] = data;
    }

    public void setType(byte type){
        this.data[typeIndex] = type;
    }

    public String getType(){
        return types[(int)this.data[typeIndex]];
    }

    private void setData(byte[] dataArray, int dataSize){
        this.crcIndex = dataIndex + dataSize;
        if (this.crcIndex >= Config.DATA_PACKAGE_MAX_LEN){
            System.err.println("[ERROR!]: The data size out of size!");
        }
        for (int i = 0; i < dataSize; i++){
            this.data[dataIndex + i] = dataArray[i];
        }
    }

    public void printSelf(){
//        System.out.println("[SEND]=> Sent : Pkg {id:" + getId() + ", len:" + this.len + "}");
//        System.out.println(Arrays.toString(getData()));
    }
}
