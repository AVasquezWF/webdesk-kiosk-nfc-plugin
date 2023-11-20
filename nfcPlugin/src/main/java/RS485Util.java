import android.content.Context;
import android.serialport.SerialPort;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

public class RS485Util {
    private static RS485Util rs232 = null;
    private OnGetDataListener onDataListener = null;
    private SerialReadThread thread = null;
    private SerialPort serialPort = null;
    private String COM = "/dev/ttyS4";
    private long sleepTime = 500L;
    private int baudrate = 9600;
    private Context mContext;

    public RS485Util(Context context) {
        mContext = context;
    }

    public void setCOM(String com){
        this.COM = com;
    }

    public void getData(OnGetDataListener dataListener) {
        this.onDataListener = dataListener;
    }

    public static RS485Util init(Context context) {
        if (rs232 == null) {
            rs232 = new RS485Util(context);
        }
        return rs232;
    }

    public void start() {
        try {
            this.serialPort = new SerialPort(new File(this.COM), this.baudrate, 0);
            Toast.makeText(mContext,"Serial port opened successfully",Toast.LENGTH_SHORT).show();
        } catch (IOException var2) {
            var2.printStackTrace();
            Toast.makeText(mContext,"Failed to open serial port",Toast.LENGTH_SHORT).show();
        } catch (SecurityException var3) {
            var3.printStackTrace();
            Toast.makeText(mContext,"Failed to open serial port: no read / write permission",Toast.LENGTH_SHORT).show();
        }

        this.thread = new SerialReadThread();
        SerialReadThread.isStop = false;
        this.thread.setSerialPort(this.serialPort);
        this.thread.setSleepTime(this.sleepTime);
        this.thread.start();
        this.thread.setOnDataReceiveListener(new SerialReadThread.OnDataReceiveListener() {
            public void onDataReceive(byte[] buffer, int size) {
                if (onDataListener != null) {
                    onDataListener.onDataReceive(buffer, size);
                }
            }
        });
    }

    public void stop() {
        if (this.thread != null) {
            this.thread.shutdownThread();
            if (serialPort != null) {
                Toast.makeText(mContext, "Serial port closed successfully", Toast.LENGTH_SHORT).show();
                serialPort = null;
            }
        }

    }

    public void sendData(String str){
        if (this.thread != null){
            this.thread.sendCmds(HexUtil.HexToByteArr(str));
        }
    }

    public interface OnGetDataListener {
        void onDataReceive(byte[] buffer, int size);
    }
}
