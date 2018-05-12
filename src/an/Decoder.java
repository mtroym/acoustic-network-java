package an;

import javax.sound.sampled.TargetDataLine;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.PipedInputStream;

public class Decoder implements Runnable{
    DataOutputStream dataOutputStream = null;
    PipedInputStream pipedInputStream = null;

    @Override
    public void run() {

    }

}
