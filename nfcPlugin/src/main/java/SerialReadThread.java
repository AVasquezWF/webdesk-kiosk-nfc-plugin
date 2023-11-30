import android.serialport.SerialPort;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class SerialReadThread extends Thread{
    private OnDataReceiveListener onDataReceiveListener = null;
    public static boolean isStop = false;
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

    public interface OnDataReceiveListener {
        void onDataReceive(byte[] buffer, int size);
    }

    public void setOnDataReceiveListener(OnDataReceiveListener dataReceiveListener) {
        onDataReceiveListener = dataReceiveListener;
    }

    @Override
    public void run() {
        super.run();
        while (!isStop) {
            if (serialPort == null){
                return;
            }
            System.out.println("[run] Thread running");
            inputStream = serialPort.getInputStream();
            outputStream =serialPort.getOutputStream();
            try {
                if (inputStream.available() > 0) {
                    //当接收到数据时，sleep 500毫秒（sleep时间自己把握）
                    Thread.sleep(sleepTime);
                    //sleep过后，再读取数据，基本上都是完整的数据
                    byte[] buffer = new byte[inputStream.available()];
                    int size = inputStream.read(buffer);
                    if (size > 0) {
                        if (null != onDataReceiveListener) {
                            onDataReceiveListener.onDataReceive(buffer, size);
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                shutdownThread();
            } catch (InterruptedException e) {
                e.printStackTrace();
                shutdownThread();
            }
        }
    }

    /**
     * 发送指令到串口
     *
     * @param cmd
     * @return
     */
    public boolean sendCmds(byte[] cmd) {
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
        int hexlen = inHex.length();
        if (isOdd(hexlen) == 1) {
            hexlen++;
            result = new byte[(hexlen / 2)];
            inHex = "0" + inHex;
        } else {
            result = new byte[(hexlen / 2)];
        }
        int j = 0;
        for (int i = 0; i < hexlen; i += 2) {
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
        isStop = true;
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
