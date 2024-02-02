package elatec;

import android.serialport.SerialPort;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.logging.Logger;

public class SerialReadThread extends Thread {

    Logger logger = Logger.getLogger(getClass().getName());
    
    private OnDataReceiveListener onDataReceiveListener = null;
    private OnIterationExecute onIterationExecute = null;
    private boolean isThreadStopped = false;
    private InputStream inputStream = null;
    private OutputStream outputStream = null;
    private long sleepTime = 500;
    private SerialPort serialPort = null;

    public long getSleepTime() {
        return sleepTime;
    }

    public void setSleepTime(long sleepTime) {
        this.sleepTime = sleepTime;
    }

    public void setSerialPort(SerialPort serialPort) {
        this.serialPort = serialPort;
    }

    public SerialPort getSerialPort() {
        return serialPort;
    }

    public boolean isThreadStopped() {
        return isThreadStopped;
    }
    public void setThreadStopped(boolean threadStopped) {
        isThreadStopped = threadStopped;
    }

    public interface OnDataReceiveListener {
        void onDataReceive(byte[] buffer, int size);
    }

    public interface OnIterationExecute {
        void executeEveryIteration();
    }

    public void setOnDataReceiveListener(OnDataReceiveListener dataReceiveListener) {
        onDataReceiveListener = dataReceiveListener;
    }

    public void setOnIterationExecute(OnIterationExecute executeOnIteration) {
        onIterationExecute = executeOnIteration;
    }

    @Override
    public void run() {
        super.run();
        while (true) {
            try {
                if (serialPort == null){
                    logger.info("[run] Ignore iteration as there is not a serialPort active");
                    continue;
                }

                logger.info("[run] Start thread iteration");
                sleep(sleepTime);
                if(isThreadStopped()){
                    logger.info("[run] Ignore iteration as thread is stopped ");
                    continue;
                }

                if (null != onIterationExecute) {
                    onIterationExecute.executeEveryIteration();
                }

                inputStream = serialPort.getInputStream();
                outputStream = serialPort.getOutputStream();

                if (inputStream.available() == 0) continue;

                logger.info("[run] inputStream available");

                byte[] buffer = new byte[inputStream.available()];
                int size = inputStream.read(buffer);

                if (size == 0) continue;

                logger.info("[run] Data was read");

                if (null != onDataReceiveListener) {
                    logger.info("[run] On receive data");
                    onDataReceiveListener.onDataReceive(buffer, size);
                }
            } catch (IOException e) {
                e.printStackTrace();
                this.serialPort = null;
            } catch (InterruptedException e) {
                e.printStackTrace();
                shutdownThread();
                return;
            }
        }
    }

    /**
     * 发送指令到串口
     *
     * @param cmd
     * @return
     */
    public boolean sendCommand(byte[] cmd) {
        logger.info("[sendCommand] Sending " + Arrays.toString(cmd));
        boolean result = true;
        try {
            if (outputStream != null) {
                outputStream.write(cmd);
            } else {
                result = false;
            }
        } catch (IOException e) {
            e.printStackTrace();
            result = false;
        }
        return result;
    }

    public static byte[] HexToByteArr(String inHex) {
        byte[] result;
        int hexLength = inHex.length();
        if (isOdd(hexLength) == 1) {
            hexLength++;
            result = new byte[(hexLength / 2)];
            inHex = "0" + inHex;
        } else {
            result = new byte[(hexLength / 2)];
        }
        int j = 0;
        for (int i = 0; i < hexLength; i += 2) {
            result[j] = HexToByte(inHex.substring(i, i + 2));
            j++;
        }
        return result;
    }

    public static int isOdd(int num) {
        return num & 1;
    }

    public static byte HexToByte(String inHex) {
        return (byte) Integer.parseInt(inHex, 16);
    }


    public boolean sendBuffer(byte[] mBuffer) {
        boolean result = true;
        String tail = "";
        byte[] tailBuffer = tail.getBytes();
        byte[] mBufferTemp = new byte[mBuffer.length+tailBuffer.length];
        System.arraycopy(mBuffer, 0, mBufferTemp, 0, mBuffer.length);
        System.arraycopy(tailBuffer, 0, mBufferTemp, mBuffer.length, tailBuffer.length);
        try {
            if (outputStream != null) {
                outputStream.write(mBufferTemp);
            } else {
                result = false;
            }
        } catch (IOException e) {
            e.printStackTrace();
            result = false;
        }
        return result;
    }

    public void shutdownThread(){
        setThreadStopped(true);
        this.interrupt();
        if (serialPort != null){
            try {
                serialPort.getInputStream().close();
                serialPort.close();
                serialPort = null;
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        if (inputStream != null){
            try {
                inputStream.close();
            }catch (Exception e){
                e.printStackTrace();
            }
            inputStream = null;
        }

        if (outputStream != null){
            try {
                outputStream.close();
            }catch (Exception e){
                e.printStackTrace();
            }
            outputStream = null;
        }
    }
}
