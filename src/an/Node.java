package an;

import javax.xml.crypto.Data;
import java.io.IOException;
import java.io.PipedInputStream;
import java.util.Arrays;

public class Node {
    int ID = 0;
    Sender sender;
    Encoder encoder;
    PipedInputStream pipedInputStream;
    Thread threadReadFile;
    int src;
    int dst;

    public void setID(int ID){
        this.ID = ID;
        this.src = ID;
    }

    public void setGoal(int dst){
        this.dst = dst;
    }
    private void initTx(String name){
        pipedInputStream = new PipedInputStream();
        sender = new Sender();
        encoder = new Encoder(pipedInputStream, name + ".bin");
        threadReadFile = new Thread(encoder, "threadReadFile");
        sender.initLine();
    }

    private void startTx(){
        System.out.println("[SEND]=> Start Tx!");
        // TODO sent sourcedataline -> ji cheng
        sender.sourceDataLine.start();
        threadReadFile.start();
    }

    private void stopTx(){
        sender.stopLine();
        try {
            threadReadFile.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void sendFile() throws IOException {
        int numByteTotal = 0;
        int pkgNum = 0;
        sender.sendWN();
        sender.sendWN();
        sender.sendWN();
        long stamp0 = System.currentTimeMillis();
        System.out.println("[SEND]**********Start  Transfer*********");
        while (true){
            int numByte;
            byte [] dataRead = new byte[Config.PHY_PAYLOAD_LEN];
            numByte = pipedInputStream.read(dataRead, 0 , dataRead.length);
            if (numByte == -1) {
                break;
            }else{
                numByteTotal += numByte;
                // TODO: doing mac... ACK ...
                DataFrame df = new DataFrame(dataRead, numByte, pkgNum);
                df.setSrcDst(this.src, this.dst);
                df.setType(df.TYPE_NOM);
//                if (pkgNum == 0) {df.setType((byte)df.TYPE_NBG);}
//                df.printSelf();
                int ret = sender.sendFrame(df);
            }
            pkgNum += 1;
        }
        long stamp1 = System.currentTimeMillis();
        double time = ((stamp1-stamp0) / 1000.0);
        sender.sendWN();
        System.out.println("[SEND]**********End Transfer*********");
        System.out.println("[SEND]=> total Transferred : " + numByteTotal + " Bytes");
        System.out.printf("[SEND]=> Time used: %.04f s; Transfer Speed: %.04f kbps" , time , numByteTotal*8 / 1000.0 / time);
    }


    public static void send() {
        Node node = new Node();
        node.initTx("INPUT6250B");
        node.startTx();
        node.setGoal(1);
        try {
            node.sendFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        node.stopTx();
    }


    public static void receiver(){
        Receiver receiver = new Receiver();
        receiver.initLine();
        Thread rec = new Thread(receiver, "threadReceiverSignal");
        rec.start();
    }


    public static void main(String[] args) {
        receiver();
        send();
    }

}
