package an;

import javax.xml.crypto.Data;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Arrays;

public class Node {
    int ID = 0;
    Sender sender;
    Encoder encoder;
    Receiver receiver;
    Decoder decoder;
    PipedInputStream pipedInputStream;
    PipedOutputStream pipedOutputStream;
    Thread threadReadFile;
    Thread threadWriteFile;
    Thread receer;
    int src;
    int dst;
    DataFrame [] dataFrames = new DataFrame[100];
    public long[] sendTime = new long[100];
    boolean noise = false;


    public Node(int ID){
        setID(ID);
    }

    private void setID(int ID){
        this.ID = ID;
        this.src = ID;
    }

    public void setGoal(int dst){
        this.dst = dst;
        sender.setGoal(dst);
    }
    private void initTx(String name){
        pipedInputStream = new PipedInputStream();
        sender = new Sender(this.ID);
        encoder = new Encoder(pipedInputStream, name + ".bin");
        threadReadFile = new Thread(encoder, "threadReadFile");
        sender.initLine();
    }

    private void initRx(String name){
        receiver = new Receiver(this.ID);
        receiver.initLine();
        receer = new Thread(receiver, "threadReceiverSignal");
        decoder = new Decoder(receiver.pipedOutputStream, name+".bin");
        threadWriteFile  = new Thread(decoder, "threadWriteFile");
        threadWriteFile.start();
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

    public void startRx(){
        receer.start();
    }


    public void stopRx(){
        try {
            threadWriteFile.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    private void lockIdle(){
        if (noise){
            while (!receiver.isIdle()){ }
        }
    }

    public void sendFile() throws IOException {
        int numByteTotal = 0;
        int pkgNum = 0;
        lockIdle();
        sender.sendFileBgn();
        long stamp0 = System.currentTimeMillis();
        System.out.println("[SEND]**********Start  Transfer*********");
        while (true){
            int numByte;
            byte [] dataRead = new byte[Config.PHY_PAYLOAD_LEN];
            numByte = pipedInputStream.read(dataRead, 0 , dataRead.length);
            if (numByte == -1) {
                lockIdle();
                receiver.fileSize = pkgNum;
                sender.setFileEnd(pkgNum);
                sender.sendFileEnd();
                break;
            }else{
                numByteTotal += numByte;
                DataFrame df = new DataFrame(dataRead, numByte, pkgNum);
                df.setSrcDst(this.src, this.dst);
                df.setType(df.TYPE_NOM);
                byte crc = sender.utility.updateCRC(df.getData(), 0,df.getDataLen() -1);
                df.setCrc(crc);

                // TODO : CHECK SUM
//                if (pkgNum == 0) {df.setType((byte)df.TYPE_NBG);}
//                df.printSelf();
                dataFrames[pkgNum] = df;
                lockIdle();
                sender.sendFrame(df);
                long stampnow = System.currentTimeMillis(); //TODO task1 -> comment out this...
                sendTime[pkgNum] = stampnow; //TODO task1 -> comment out this...
                sendACK(); //TODO task1 -> comment out this...
                checkLinkError(stampnow, pkgNum, false); //TODO task1 -> comment out this...
            }
            pkgNum += 1;
        }
        long stamp = System.currentTimeMillis(); //TODO task1 -> comment out this...
        checkACKandResend(stamp); //TODO task1 -> comment out this...
        long stamp1 = System.currentTimeMillis();
        double time = ((stamp1-stamp0) / 1000.0);
//        lockIdle();
        sender.sendWN();
        System.out.println("[SEND]**********End Transfer*********");
        System.out.println("[SEND]=> total Transferred : " + numByteTotal + " Bytes");
        System.out.printf("[SEND]=> Time used: %.04f s; Transfer Speed: %.04f kbps" , time , numByteTotal*8 / 1000.0 / time);
    }

    private void sendACK(){
        for (int i = 0; i < receiver.rec.length; i++){
            if(receiver.rec[i] == 1){
                sender.sendACK(i);
                receiver.rec[i] = 0;
            }
        }
    }


    private void checkLinkError(long stamp, int pkgnum, boolean ping){
        for (int i = 0; i < pkgnum; i++){
            if(receiver.ack[i] == 0){
                long time = stamp - sendTime[i];
                if(time > 5000){
                    System.err.println("Link Error");
                    return;
                }else if (time > 10000){
                    System.err.println("Link Error");
                    lockIdle();
                    sendTime[i] = stamp;
                    sender.sendFrame(dataFrames[i]);
                    return;
                }
            }else {
                long time = receiver.ackTime[i] - sendTime[i];
                if (ping){
                    System.out.println("[PING]=> RTT: "+time);
                }
            }
        }
    }


    private void checkACKandResend(long stamp0){
        System.out.println("[Send]=> check ack");
        int flag = 0;
        while (flag == 0){
            flag = 1;
//            System.out.print(receiver.fileSize);
            for (int i = 0; i< receiver.fileSize; i++){
                if (receiver.ack[i] == 0){
                    lockIdle();
                    sendTime[i] = System.currentTimeMillis();
                    sender.sendFrame(dataFrames[i]);
                    sendACK();
                    flag = 0;
                }
            }
        }
    }


    private void ping(int dst){
        // TODO
    }

    private void perf(int dst){
        // TODO
    }

    public static void go() {
        Node node = new Node(0);
        node.initTx("INPUT6250B");
        node.initRx("OUTPUT");
        node.setGoal(1);
        node.startRx();
        node.startTx();

        try {
            node.sendFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        node.stopTx();


        node.stopRx();
    }


//    public static void receiver(){
//        Receiver receiver = new Receiver();
//        receiver.initLine();
//        Thread rec = new Thread(receiver, "threadReceiverSignal");
//        rec.start();
//    }


    public static void main(String[] args) {
        go();
    }

}
