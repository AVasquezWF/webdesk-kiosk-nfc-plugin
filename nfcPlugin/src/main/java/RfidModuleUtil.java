import android.content.Context;
import android.serialport.SerialPort;
import android.serialport.SerialPortFinder;
import android.text.TextUtils;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RfidModuleUtil {
    private OnGetDataListener onDataListener = null;
    private SerialReadThread thread = null;
    private SerialPort serialPort = null;
    private String COM = "/dev/ttyACM1";
    private long sleepTime = 500L;
    private int baudrate = 9600;
    private boolean beepStatus = true;
    private String cardType = "";
    private String cardId = "";
    private String cardValue = "";
    private Context mContext;
    private SerialPortFinder finder;
    private List<String> comList = new ArrayList<>();
    private int ret = -1;

    public RfidModuleUtil(Context context) {
        this.mContext = context;
    }

    public void getData(OnGetDataListener dataListener) {
        this.onDataListener = dataListener;
    }

    public int init() {
        finder = new SerialPortFinder();
        comList = Arrays.asList(finder.getAllDevicesPath());
        if (comList.contains(COM)){
            ret = 0;
        }else {
            Toast.makeText(mContext, "Please connect the module first", Toast.LENGTH_SHORT).show();
            ret = -1;
        }
        if (ret == 0) {
            try {
                this.serialPort = new SerialPort(new File(this.COM), this.baudrate, 0);
                ret = 1;
            } catch (IOException ioException) {
                Toast.makeText(mContext, "Failed to open serial port", Toast.LENGTH_SHORT).show();
                ioException.printStackTrace();
            } catch (SecurityException securityException) {
                Toast.makeText(mContext, "Failed to open serial port: no read / write permission", Toast.LENGTH_SHORT).show();
                securityException.printStackTrace();
            }
        }
        return ret;
    }

    private String extractTagInformation (String str) throws JSONException {
        JSONObject tagInformation = new JSONObject();
        cardValue = "";
        if (!str.trim().startsWith("0001")) {
            return "Unsupported card type";
        }

        switch (str.substring(4,6)){
            case "80":
                cardType = Constant.HFTAG_MIFARE;
                cardId = str.substring(10);
                break;
            case "84":
                cardType = Constant.HFTAG_HIDICLASS;
                cardId = str.substring(10);
                break;
            case "40":
                cardType = Constant.LFTAG_EM4102;
                cardId = str.substring(10);
                break;
            case "49":
                cardType = Constant.LFTAG_HIDPROX;
                cardId = str.substring(10);
                break;
            default:
                if (cardType.equals(Constant.HFTAG_MIFARE)){
                    cardValue = str.substring(9);
                } else if (cardType.equals(Constant.HFTAG_HIDICLASS)){
                    cardValue = str.substring(8);
                }
                break;
        }

        String bit = str.substring(6,8);

        int bit_ten = Integer.parseInt(bit, 16);
        System.out.println("bit ---> "+bit_ten);
        System.out.println("cardId ---> "+cardId);

        if (!TextUtils.isEmpty(cardId)) {
            String binary = HexUtil.hex2bin(cardId.trim());
            cardId = HexUtil.bin2Hex(binary.substring(0, bit_ten));
        }

        if (!TextUtils.isEmpty(cardValue)) {
            tagInformation.put("value", cardValue);
        } 
        tagInformation.put("type", cardType);
        tagInformation.put("cardId",  cardId);

        if (beepStatus){
            thread.sendCmds(Constant.BEEP.getBytes());
        }

        return tagInformation.toString();
    }

    public void start() {
        if (ret != 1){
            return;
        }
        this.thread = new SerialReadThread();
        SerialReadThread.isStop = false;
        this.thread.setSerialPort(this.serialPort);
        this.thread.setSleepTime(this.sleepTime);
        this.thread.start();
        this.thread.setOnDataReceiveListener((buffer, size) -> {
            if (onDataListener == null) {
                System.out.println("[start] No dataListener defined");
                return;
            }
            String str = new String(buffer, 0, size);
            String error = "";
            System.out.println(str.trim());

            if (str.length() > 10) {
                try {
                    onDataListener.onDataReceive(cardType, extractTagInformation(str));
                } catch (JSONException e) {
                    error = e.toString();
                }
            } else if (str.trim().equals("0000")) {
                error = "No tag read";
            } else if (str.trim().equals("0001")) {
                error = "Successful read, but no data was found";
            } else {
                error = "Unknown data";
            }
            onDataListener.onDataReceive("", error);
        });
    }

    public void stop() {
        if (this.thread != null) {
            this.thread.shutdownThread();
        }
    }

    public interface OnGetDataListener {
        void onDataReceive(String cardType, String cardData);
    }

    public void setBeep(boolean beep){
        beepStatus = beep;
    }

    public void searchTag(){
        cardType = "";
        thread.sendCmds(Constant.SEARCH_TAG.getBytes());
    }

    public void readTag(){
        switch (cardType){
            case Constant.HFTAG_MIFARE:
                thread.sendCmds(Constant.MIFARE_LOGIN.getBytes());
                thread.sendCmds(Constant.MIFARE_READ.getBytes());
                break;
            case Constant.HFTAG_HIDICLASS:
                thread.sendCmds(Constant.ICLASS_READ.getBytes());
                break;
            case Constant.LFTAG_EM4102:
                break;
            case Constant.LFTAG_HIDPROX:
                break;
            default:
                break;
        }
    }

    public void writeTag(String hexData){
        thread.sendCmds(("0B0202"+hexData+"\r\n").getBytes());
    }
}
