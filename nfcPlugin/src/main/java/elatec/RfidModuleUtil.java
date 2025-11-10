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

/**
 * Utility class for interacting with an ELATEC RFID reader module.
 *
 * <p>The reader can output data in two different string formats:
 * <ul>
 *     <li>a short plain UID (e.g., {@code 05400860C0})</li>
 *     <li>an extended protocol frame (e.g., {@code 000140280505400860C0})</li>
 * </ul>
 * Both may appear within the same serial response.
 * Only the protocol frame ("0001...") contains structured information
 * that allows you to determine tag technology and UID length.
 * </p>
 */
public class RfidModuleUtil {

    private static final Logger LOGGER = Logger.getLogger(RfidModuleUtil.class.getName());

    private static final String DEFAULT_COM = "/dev/ttyACM1";
    private static final int DEFAULT_BAUDRATE = 9600;
    private static final long DEFAULT_SLEEP = 250L;

    private final Context context;
    private final List<String> comList = new ArrayList<>();

    private OnGetDataListener onDataListener;
    private SerialReadThread thread;
    private SerialPort serialPort;
    private SerialPortFinder finder;

    private String cardType = "";
    private String cardId = "";
    private String cardValue = "";
    private String prevCardId = "";

    private boolean beepStatus = true;
    private long sleepTime = DEFAULT_SLEEP;
    private int ret = -1;

    public RfidModuleUtil(Context context) {
        this.context = context;
    }

    public void getData(OnGetDataListener dataListener) {
        this.onDataListener = dataListener;
    }

    public void setSleepTime(long time) {
        this.sleepTime = time;
        if (thread != null) {
            thread.setSleepTime(time);
        }
    }

    /** Opens the serial connection to the ELATEC module if the device is present. */
    public int init() {
        if (finder == null) {
            finder = new SerialPortFinder();
        }

        comList.clear();
        comList.addAll(Arrays.asList(finder.getAllDevicesPath()));

        if (!comList.contains(DEFAULT_COM)) {
            Toast.makeText(context, "Please connect the module first", Toast.LENGTH_SHORT).show();
            return -1;
        }

        try {
            serialPort = new SerialPort(new File(DEFAULT_COM), DEFAULT_BAUDRATE, 0);
            ret = 1;
        } catch (IOException e) {
            Toast.makeText(context, "Failed to open serial port", Toast.LENGTH_SHORT).show();
            LOGGER.warning(e.getMessage());
        } catch (SecurityException e) {
            Toast.makeText(context,
                    "Failed to open serial port: no read / write permission",
                    Toast.LENGTH_SHORT).show();
            LOGGER.warning(e.getMessage());
        }
        return ret;
    }

    private SerialPort connectSerialPort() {
        try {
            return new SerialPort(new File(DEFAULT_COM), DEFAULT_BAUDRATE, 0);
        } catch (IOException | SecurityException e) {
            Toast.makeText(context, "Failed to open serial port", Toast.LENGTH_SHORT).show();
            LOGGER.warning(e.getMessage());
            return null;
        }
    }

    public boolean reconnectSerialPort() {
        SerialPort newSerialPort = connectSerialPort();
        if (newSerialPort == null || thread == null) {
            return false;
        }
        thread.shutdownThread();
        serialPort = newSerialPort;
        thread.setSerialPort(serialPort);
        thread.start();
        return true;
    }

    /**
     * Extracts readable tag information (type, UID, optional value) from the reader's
     * native hexadecimal string frame.
     *
     * <p>ELATEC sends a multi‑byte frame like {@code 000140280505400860C0}
     * where:
     * <ul>
     *     <li>{@code 0001} = header</li>
     *     <li>{@code 40} = technology (EM410x family)</li>
     *     <li>{@code 28} = bit length (0x28 = 40 bits)</li>
     *     <li>rest = raw UID bytes</li>
     * </ul>
     * Hence, we strip off the header bytes and interpret the rest according to type code.</p>
     */
    private String extractTagInformation(String str)
            throws JSONException, StringIndexOutOfBoundsException {

        JSONObject tagInformation = new JSONObject();
        cardValue = "";

        final String trimmed = str.trim();
        if (!trimmed.startsWith("0001")) {
            // Non‑protocol data (e.g. plain UID line) — ignore here.
            throw new JSONException("Unsupported card type: " + trimmed);
        }

        String typeHex = trimmed.substring(4, 6);
        switch (typeHex) {
            case "80":
                cardType = Constant.HFTAG_MIFARE;
                break;
            case "84":
                cardType = Constant.HFTAG_HIDICLASS;
                break;
            case "40":
                cardType = Constant.LFTAG_EM4102;
                break;
            case "49":
                cardType = Constant.LFTAG_HIDPROX;
                break;
            case "83":
                cardType = Constant.LFTAG_LEGICPRIME;
                break;
            case "41":
                cardType = Constant.LFTAG_HITAGS;
                break;
            default:
                // Unrecognized type; may still contain UID
                break;
        }

        // UID begins at position 10 — after header/type/bit fields (0001 + 40 + 28 + len).
        cardId = trimmed.substring(10);

        String bit = trimmed.substring(6, 8);
        int bitTen = Integer.parseInt(bit, 16);
        LOGGER.info("bit ---> " + bitTen);
        LOGGER.info("cardId ---> " + cardId);

        if (!TextUtils.isEmpty(cardId)) {
            try {
                // Convert hex→binary and truncate to the number of bits reported in field "bitTen".
                // Some readers pad data, so we trim exactly 40 bits for EM‑type tags.
                String binary = HexUtil.hex2bin(cardId.trim());
                cardId = HexUtil.bin2Hex(binary.substring(0, bitTen));
            } catch (NumberFormatException e) {
                LOGGER.warning(e.getMessage());
            }
        }

        if (!TextUtils.isEmpty(cardValue)) {
            tagInformation.put("value", cardValue);
        }
        tagInformation.put("type", cardType);
        tagInformation.put("cardId", cardId);

        return tagInformation.toString();
    }

    /** Starts the background thread to continuously read serial data from the reader. */
    public void start() {
        if (ret != 1) {
            return;
        }
        thread = new SerialReadThread();
        SerialReadThread.isStop = false;
        thread.setSerialPort(serialPort);
        thread.setSleepTime(sleepTime);
        thread.start();

        thread.setOnDataReceiveListener((buffer, size) -> {
            if (onDataListener == null) {
                LOGGER.info("[start] No dataListener defined");
                return;
            }

            cardId = "";
            String str = new String(buffer, 0, size);
            LOGGER.info(str.trim());

            if (str.length() > 10) {
                // The reader often returns a combined message containing both UID and full frame.
                handleIncomingData(str.trim());
                return;
            }

            String error;
            switch (str.trim()) {
                case "0000":
                    error = "No tag read";
                    break;
                case "0001":
                    error = "Successful read, but no data was found";
                    break;
                default:
                    error = "Unknown data";
                    break;
            }

            if (!Objects.equals(cardId, prevCardId)) {
                onDataListener.onTagDetached();
                prevCardId = cardId;
            }
            onDataListener.onDataReceive(null, error);
        });
    }

    /**
     * Parses the string that may contain both plain UID and protocol data.
     * We split it by carriage returns and process only the lines that start with "0001"
     * because those correspond to the protocol frames from the reader.
     */
    private void handleIncomingData(String raw) {
        String[] lines = raw.split("\r");
        for (String line : lines) {
            line = line.trim();

            // Some readers output both a short UID and the protocol frame; we only parse the latter.
            if (!line.startsWith("0001")) {
                continue;
            }

            try {
                String tagInformation = extractTagInformation(line);

                // Optionally play reader beep feedback.
                if (beepStatus && thread != null) {
                    thread.sendCommand(Constant.BEEP.getBytes());
                }

                // Compare current UID with previous to prevent duplicate notifications.
                if (Objects.equals(cardId, prevCardId)) {
                    LOGGER.info("onDataListener.onTagEqual");
                } else {
                    onDataListener.onDataReceive(cardType, tagInformation);
                    onDataListener.onTagAttached();
                    prevCardId = cardId;
                }
            } catch (JSONException | StringIndexOutOfBoundsException e) {
                LOGGER.warning("Error parsing tag: " + e.getMessage());
            }
        }
    }

    public void stop() {
        if (thread != null) {
            thread.shutdownThread();
        }
    }

    /** Listener returning tag events to the caller. */
    public interface OnGetDataListener {
        void onDataReceive(String cardType, String cardData);
        void onTagDetached();
        void onTagAttached();
    }

    public void setBeep(boolean beep) {
        this.beepStatus = beep;
    }

    public void searchTag() {
        cardType = "";
        if (thread != null) {
            thread.sendCommand(Constant.SEARCH_TAG.getBytes());
        }
    }

    /** Sends the proper read commands depending on detected card technology. */
    public void readTag() {
        if (thread == null) {
            return;
        }

        switch (cardType) {
            case Constant.HFTAG_MIFARE:
                thread.sendCommand(Constant.MIFARE_LOGIN.getBytes());
                thread.sendCommand(Constant.MIFARE_READ.getBytes());
                break;
            case Constant.HFTAG_HIDICLASS:
                thread.sendCommand(Constant.ICLASS_READ.getBytes());
                break;
            default:
                // 125 kHz LF tags like EM410x/HID Prox are read‑only by design.
                break;
        }
    }

    public void writeTag(String hexData) {
        if (thread != null) {
            thread.sendCommand(("0B0202" + hexData + "\r\n").getBytes());
        }
    }

    /** Sets periodic tag‑scan commands on the reader thread. */
    public void listenForTag() {
        if (thread != null) {
            thread.setOnIterationExecute(() -> {
                try {
                    searchTag();
                    readTag();
                } catch (Exception e) {
                    LOGGER.warning(e.toString());
                }
            });
        }
    }

    /** Sends a custom command frame directly to the reader. */
    public boolean sendCommand(String hexData) {
        return thread != null && thread.sendCommand((hexData + "\r\n").getBytes());
    }
}