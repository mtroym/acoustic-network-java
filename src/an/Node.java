package an;

import javax.xml.crypto.Data;
import java.io.IOException;
import java.io.PipedInputStream;
import java.util.Arrays;

public class Node {
    int ID = 0;
    Sender sender;
    Encoder encoder;
    Receiver receiver;
    PipedInputStream pipedInputStream;
    Thread threadReadFile;
    Thread receer;
    int src;
    int dst;
    DataFrame [] dataFrames = new DataFrame[100];
    public long[] sendTime = new long[100];

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

    private void lockIdle(){
//        while (!receiver.isIdle()){ }
    }

    public void sendFile() throws IOException {
        int numByteTotal = 0;
        int pkgNum = 0;
        lockIdle();
        sender.sendWN();
        lockIdle();
        sender.sendWN();
        lockIdle();
        sender.sendFileBgn();
        long stamp0 = System.currentTimeMillis();
        System.out.println("[SEND]**********Start  Transfer*********");
        while (true){
            int numByte;
            byte [] dataRead = new byte[Config.PHY_PAYLOAD_LEN];
            numByte = pipedInputStream.read(dataRead, 0 , dataRead.length);
            if (numByte == -1) {
//                while (!receiver.isIdle()){ }
                receiver.fileSize = pkgNum;
                sender.setFileEnd(pkgNum);
                sender.sendFileEnd();
                break;
            }else{
                numByteTotal += numByte;
                // TODO: doing mac... ACK ...
                DataFrame df = new DataFrame(dataRead, numByte, pkgNum);
                df.setSrcDst(this.src, this.dst);
                df.setType(df.TYPE_NOM);
//                if (pkgNum == 0) {df.setType((byte)df.TYPE_NBG);}
//                df.printSelf();
                dataFrames[pkgNum] = df;
//                lockIdle();
                sender.sendFrame(df);
                long stampnow = System.currentTimeMillis(); //TODO task1 -> comment out this...
                sendTime[pkgNum] = stampnow; //TODO task1 -> comment out this...
                sendACK(); //TODO task1 -> comment out this...
                checkLinkError(stampnow, pkgNum); //TODO task1 -> comment out this...
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


    private void checkLinkError(long stamp, int pkgnum){
        for (int i = 0; i < pkgnum; i++){
            if(receiver.ack[i] == 0){
                if(stamp - sendTime[i] > 1000){
                    System.err.println("LINK ERROR");
                    return;
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
//                    lockIdle();
                    sender.sendFrame(dataFrames[i]);
                    flag = 0;
                }
            }
            sendACK();
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
