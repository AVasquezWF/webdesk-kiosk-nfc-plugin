package elatec;

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
import java.util.Objects;
import java.util.logging.Logger;

public class RfidModuleUtil {

    Logger logger = Logger.getLogger(getClass().getName());
    
    private onGetDataListener onDataListener = null;
    public SerialReadThread thread = null;
    private SerialPort serialPort = null;
    private String COM = "/dev/ttyACM1";
    private long sleepTime = 250L;
    private int baudrate = 9600;
    private boolean beepStatus = true;
    private String cardType = "";
    private String cardId = "";
    private String cardValue = "";
    private Context mContext;
    private SerialPortFinder finder;
    private List<String> comList = new ArrayList<>();
    private int ret = -1;

    String prevCardId = "";

    public RfidModuleUtil(Context context) {
        this.mContext = context;
    }

    public void getData(onGetDataListener dataListener) {
        this.onDataListener = dataListener;
    }

    public void setSleepTime(long time) {
        this.sleepTime = time;
        if (this.thread != null){
            this.thread.setSleepTime(time);
        }
    }

    public int init (){
        if (this.finder == null) {
            this.finder = new SerialPortFinder();
        }

        comList = Arrays.asList(finder.getAllDevicesPath());
        if (comList.contains(COM)){
            ret = 0;
        } else {
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

    private SerialPort connectSerialPort() {
        try {
            return new SerialPort(new File(this.COM), this.baudrate, 0);
        } catch (IOException ioException) {
            Toast.makeText(mContext, "Failed to open serial port", Toast.LENGTH_SHORT).show();
            ioException.printStackTrace();
        } catch (SecurityException securityException) {
            Toast.makeText(mContext, "Failed to open serial port: no read / write permission", Toast.LENGTH_SHORT).show();
            securityException.printStackTrace();
        }
        return null;
    }

    public boolean reconnectSerialPort() {
        SerialPort newSerialPort = connectSerialPort();
        if(newSerialPort == null) return false;

        this.thread.shutdownThread();

        this.serialPort = newSerialPort;
        this.thread.setSerialPort(this.serialPort);

        this.thread.start();
        return true;
    }

    private String extractTagInformation (String str) throws JSONException, StringIndexOutOfBoundsException {
        JSONObject tagInformation = new JSONObject();

        cardValue = "";
        if (!str.trim().startsWith("0001")) {
            throw new JSONException("Unsupported card type: " + str.trim());
        }

        switch (str.substring(4,6)) {
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
            case "83":
                cardType = Constant.LFTAG_LEGICPRIME;
                cardId = str.substring(10);
                break;
            case "41":
                cardType = Constant.LFTAG_HITAGS;
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

        int bitTen = Integer.parseInt(bit, 16);
        logger.info("bit ---> " + bitTen);
        logger.info("cardId ---> " + cardId);

        if (!TextUtils.isEmpty(cardId)) {
            try {
                String binary = HexUtil.hex2bin(cardId.trim());
                cardId = HexUtil.bin2Hex(binary.substring(0, bitTen));
            } catch (NumberFormatException e){
                e.printStackTrace();
            }
        }

        if (!TextUtils.isEmpty(cardValue)) {
            tagInformation.put("value", cardValue);
        }
        tagInformation.put("type", cardType);
        tagInformation.put("cardId", cardId);
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
                logger.info("[start] No dataListener defined");
                return;
            }

            cardId = "";
            String str = new String(buffer, 0, size);
            String error = "";
            logger.info(str.trim());

            if (str.length() > 10) {
                try {
                    String tagInformation = extractTagInformation(str);

                    if (beepStatus){
                        thread.sendCommand(Constant.BEEP.getBytes());
                    }

                    if (Objects.equals(cardId, prevCardId)){
                        logger.info("onDataListener.onTagEqual");
                    } else {
                        onDataListener.onDataReceive(cardType, tagInformation);
                        onDataListener.onTagAttached();
                    }
                    prevCardId = cardId;

                    return;
                } catch (JSONException e) {
                    error = e.toString();
                } catch (StringIndexOutOfBoundsException e) {
                    logger.info("[setOnDataReceiveListener] Caught StringIndexOutOfBoundsException: " + e.getMessage());
                    error = e.toString();
                }
            } else if (str.trim().equals("0000")) {
                error = "No tag read";
            } else if (str.trim().equals("0001")) {
                error = "Successful read, but no data was found";
            } else {
                error = "Unknown data";
            }

            if (Objects.equals(cardId, prevCardId)){
                logger.info("onDataListener.onTagEqual");
            } else {
                onDataListener.onTagDetached();
            }

            prevCardId = cardId;

            onDataListener.onDataReceive(null, error);
        });
    }

    public void stop() {
        if (this.thread != null) {
            this.thread.shutdownThread();
        }
    }

    public interface onGetDataListener {
        void onDataReceive(String cardType, String cardData);
        void onTagDetached();
        void onTagAttached();
    }

    public void setBeep(boolean beep){
        beepStatus = beep;
    }

    public void searchTag(){
        cardType = "";
        thread.sendCommand(Constant.SEARCH_TAG.getBytes());
    }

    public void readTag(){
        switch (cardType){
            case Constant.HFTAG_MIFARE:
                thread.sendCommand(Constant.MIFARE_LOGIN.getBytes());
                thread.sendCommand(Constant.MIFARE_READ.getBytes());
                break;
            case Constant.HFTAG_HIDICLASS:
                thread.sendCommand(Constant.ICLASS_READ.getBytes());
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
        thread.sendCommand(("0B0202"+hexData+"\r\n").getBytes());
    }

    public void listenForTag() {
        this.thread.setOnIterationExecute(() -> {
            try {
                this.searchTag();
                this.readTag();
            } catch (Exception exception){
                logger.info(exception.toString());
            }
        });
    }

    public boolean sendCommand(String hexData) {
        return thread.sendCommand((hexData+"\r\n").getBytes());
    }
}
