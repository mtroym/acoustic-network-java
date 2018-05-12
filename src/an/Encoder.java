package an;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.util.Arrays;

public class Encoder implements Runnable{
    String inputFileName = null;
    boolean runing = true;
    byte[] SingleDataFrame = new byte[246];
    DataInputStream dataInputStream = null;
    PipedOutputStream pipedOutputStream = null;


    public Encoder(PipedInputStream inputStream, String fileName){
        System.out.println("=> Encoder INITED!");
        try {
            pipedOutputStream = new PipedOutputStream(inputStream);
            FileInputStream fileInputStream = new FileInputStream("./file/" +fileName);
            dataInputStream = new DataInputStream(fileInputStream);
            inputFileName = fileName;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {

        while (runing) {
            try {
                int numBytesRead = dataInputStream.read(SingleDataFrame,
                        0, SingleDataFrame.length);
                if (numBytesRead == -1) {
                    runing = false;
                } else {
                    pipedOutputStream.write(SingleDataFrame, 0,
                            numBytesRead);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            dataInputStream.close();
            pipedOutputStream.flush();
            pipedOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
