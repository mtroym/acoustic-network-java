package an;

import javax.sound.sampled.TargetDataLine;
import java.io.*;

public class Decoder implements Runnable{
    String inputFileName = null;
    boolean runing = true;
    byte []SingleDataFrame = new byte[128];
    DataOutputStream dataOutputStream = null;
    PipedInputStream pipedInputStream = null;


    public Decoder(PipedOutputStream outputStream, String fileName){
        System.out.println("=> Encoder INITED!");
        try {
            pipedInputStream = new PipedInputStream(outputStream);
            FileOutputStream fileOutputStream = new FileOutputStream("./file/" +fileName);
            dataOutputStream = new DataOutputStream(fileOutputStream);
            inputFileName = fileName;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {

        while (runing) {
            try {
                int numBytesRead = pipedInputStream.read(SingleDataFrame,
                        0, SingleDataFrame.length);
                if (numBytesRead == -1) {
                    continue;
                } else {
                    dataOutputStream.write(SingleDataFrame, 0,
                            numBytesRead);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            dataOutputStream.flush();
            dataOutputStream.close();
            pipedInputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
