package plugin;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.util.Log;

import com.acs.smartcard.Reader;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Timer;

import acs.ACRDevice;
import acs.NFCReader;
import acs.Util;
import acs.apdu.OnGetResultListener;
import acs.params.AuthParams;
import acs.params.BaseParams;
import acs.params.ClearLCDParams;
import acs.params.ConnectParams;
import acs.params.DisplayParams;
import acs.params.InitNTAGParams;
import acs.params.ReadParams;
import acs.params.SelectFileParams;
import acs.params.WriteParams;
import acs.reader.ACRReader;
import acs.reader.USBReader;
import acs.task.StopSessionTimerTask;

/**
 * This class echoes a string called from JavaScript.
 */
public class ACRNFCReaderPhoneGapPlugin extends CordovaPlugin {

    private static final String TAG = "ACR";
    private static final String LISTEN = "listen";
    private static final String ADD_LISTENER = "addListener";
    private static final String READ_UID = "readUID";
    private static final String READ_DATA = "readData";
    private static final String WRITE_DATA = "writeData";
    private static final String GET_VERSION = "getVersion";
    private static final String AUTHENTICATE_WITH_KEY_A = "authenticateWithKeyA";
    private static final String AUTHENTICATE_WITH_KEY_B = "authenticateWithKeyB";
    private static final String WRITE_AUTHENTICATE = "writeAuthenticate";
    private static final String SELECT_FILE = "selectFile";
    private static final String IS_READY = "isReady";
    private static final String DISPLAY = "display";
    private static final String CLEAR_LCD = "clearLCD";
    private static final String INIT_NTAG213 = "initNTAG213";
    private static final String INIT_READER = "initReader";
    private static final String GET_FIRMWARE_VERSION = "getFirmwareVersion";
    private static final String GET_RECEIVED_DATA = "getReceivedData";
    private static final String GET_LED_STATUS = "getLedStatus";
    private static final String GET_BATTERY_LEVEL = "getBatteryLevel";
    private static final String DISCONNECT_READER = "disconnectReader";
    private static final String CONNECT_READER = "connectReader";
    private static final String START_SCAN = "startScan";
    private static final String STOP_SCAN = "stopScan";
    private static final String RECONNECT_READER = "reconnectReader";
    private static final String ON_READY = "onReady";
    private static final String ON_ATTACH = "onAttach";
    private static final String ON_DETACH = "onDetach";
    private CordovaWebView webView;
    private NFCReader nfcReader;
    private UsbManager usbManager;
    private CallbackContext contextEmitter;

    PendingIntent mPermissionIntent;

    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG, action);
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    nfcReader.attach(intent);
                }
            } else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                synchronized (this) {
                    nfcReader.detach(intent);
                }
            }
        }
    };

    @Override
    public void initialize(CordovaInterface cordova, final CordovaWebView webView) {
        this.webView = webView;
        super.initialize(cordova, webView);

        Log.d(TAG, "Initializing...");

        useUsbReader(cordova, webView);
    }

    private Timer timer;

    private void setupTimer() {
        timer = new Timer();
        StopSessionTimerTask task = new StopSessionTimerTask(nfcReader);
        timer.schedule(task, 10000, 5000);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
    private void useUsbReader(CordovaInterface cordova, final CordovaWebView webView) {
        usbManager = (UsbManager) cordova.getActivity().getSystemService(Context.USB_SERVICE);
        ACRReader reader = new USBReader(usbManager);
        nfcReader = new NFCReader(reader);
        nfcReader.setOnStateChangeListener((slotNumber, previousState, currentState) -> {
            Log.d(TAG, "slotNumber " + slotNumber);
            Log.d(TAG, "previousState " + previousState);
            Log.d(TAG, "currentState " + currentState);

            if (slotNumber == 0 && currentState == Reader.CARD_PRESENT) {
                Log.d(TAG, "Ready to read!!!!");
                nfcReader.reset(slotNumber);
            } else {
                Log.d(TAG, "Card Lost");
                webView.sendJavascript("ACR.runCardAbsent();");
            }
        });

        nfcReader.setOnStatusChangeListener(new ACRReader.StatusChangeListener() {
            @Override
            public void onReady(ACRReader reader) {
                Log.d(TAG, ON_READY);
                initReader(null, null);
                emitPluginResult(ON_READY, null);
            }

            @Override
            public void onAttach(ACRDevice device) {
                Log.d(TAG, ON_ATTACH);
                emitPluginResult(ON_ATTACH, ((UsbDevice) device.getDevice()).getDeviceName());
             }

            @Override
            public void onDetach(ACRDevice device) {
                Log.d(TAG, ON_DETACH);
                emitPluginResult(ON_DETACH, ((UsbDevice) device.getDevice()).getDeviceName());
            }
        });

        // Register receiver for USB permission
        mPermissionIntent = PendingIntent.getBroadcast(getActivity(), 0, new Intent(ACTION_USB_PERMISSION), 0);
        nfcReader.setPermissionIntent(mPermissionIntent);
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_USB_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        getActivity().registerReceiver(broadcastReceiver, filter);
        setupTimer();
        nfcReader.start();
    }

    @Override
    public boolean execute(String action, JSONArray data, CallbackContext callbackContext) throws JSONException {

        Log.d(TAG, "Execute action: " + action);

        if (action.equalsIgnoreCase(ADD_LISTENER)) {
            return addListener(callbackContext);
        }else if (action.equalsIgnoreCase(LISTEN)) {
            listen(callbackContext);
        } else if (action.equalsIgnoreCase(READ_UID)) {
            readUID(callbackContext);
        } else if (action.equalsIgnoreCase(READ_DATA)) {
            readData(callbackContext, data);
        } else if (action.equalsIgnoreCase(WRITE_DATA)) {
            writeData(callbackContext, data);
        } else if (action.equalsIgnoreCase(AUTHENTICATE_WITH_KEY_A)) {
            authenticateWithKeyA(callbackContext, data);
        } else if (action.equalsIgnoreCase(AUTHENTICATE_WITH_KEY_B)) {
            authenticateWithKeyB(callbackContext, data);
        } else if (action.equalsIgnoreCase(WRITE_AUTHENTICATE)) {
            writeAuthenticate(callbackContext, data);
        } else if (action.equalsIgnoreCase(SELECT_FILE)) {
            selectFile(callbackContext, data);
        } else if (action.equalsIgnoreCase(DISPLAY)) {
            display(callbackContext, data);
        } else if (action.equalsIgnoreCase(CLEAR_LCD)) {
            clearLCD(callbackContext, data);
        } else if (action.equalsIgnoreCase(GET_VERSION)) {
            getVersion(callbackContext, data);
        } else if (action.equalsIgnoreCase(INIT_NTAG213)) {
            initNTAG213(callbackContext, data);
        } else if (action.equalsIgnoreCase(INIT_READER)) {
            initReader(callbackContext, data);
        } else if (action.equalsIgnoreCase(GET_FIRMWARE_VERSION)) {
            getFirmwareVersion(callbackContext);
        } else if (action.equalsIgnoreCase(GET_LED_STATUS)) {
            getLedStatus(callbackContext);
        } else if (action.equalsIgnoreCase(GET_RECEIVED_DATA)) {
            getReceivedData(callbackContext);
        } else if (action.equalsIgnoreCase(GET_BATTERY_LEVEL)) {
            getBatteryLevel(callbackContext);
        } else if (action.equalsIgnoreCase(CONNECT_READER)) {
            connectReader(callbackContext, data);
        } else if (action.equalsIgnoreCase(DISCONNECT_READER)) {
            disconnectReader();
        } else if (action.equalsIgnoreCase(START_SCAN)) {
            startScan(callbackContext);
        } else if (action.equalsIgnoreCase(STOP_SCAN)) {
            stopScan(callbackContext);
        } else if (action.equalsIgnoreCase(IS_READY)) {
            if (nfcReader != null && nfcReader.isReady()) {
                callbackContext.success();
            } else {
                callbackContext.error("Reader is not ready.");
            }
        } else {
            return false;
        }
        return true;
    }

    private boolean addListener(CallbackContext callbackContext) {
        this.contextEmitter = callbackContext;
        return true;
    }

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    private void emitPluginResult (String key, String payload){
        try {
            JSONObject jsonPayload = new JSONObject();
            jsonPayload.put("key", key);
            jsonPayload.put("payload", payload);

        boolean isEmptyResult = payload == null || payload.isEmpty();
        PluginResult result = isEmptyResult
                ? new PluginResult(PluginResult.Status.OK)
                : new PluginResult(PluginResult.Status.OK, jsonPayload);

        result.setKeepCallback(true);
        contextEmitter.sendPluginResult(result);

        } catch (JSONException e) {
            Log.d(e.toString(), e.toString());
        }
    }

    private void initReader(CallbackContext callbackContext, JSONArray data) {
        nfcReader.updatePICCOperatingParameter(generateResultListener(null));
        callbackContext.success();
    }

    private void initNTAG213(CallbackContext callbackContext, JSONArray data) {
        InitNTAGParams initNTAGParams = new InitNTAGParams(0);
        try {
            initNTAGParams.setOldPassword(data.getString(0));
            initNTAGParams.setPassword(data.getString(1));
            initNTAGParams.setOnGetResultListener(generateResultListener(callbackContext));
            nfcReader.initNTAGTask(initNTAGParams);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void getVersion(final CallbackContext callbackContext, JSONArray data) {
        BaseParams baseParams = new BaseParams(0);
        baseParams.setOnGetResultListener(generateResultListener(callbackContext));
        nfcReader.getVersion(baseParams);
    }

    private void getBatteryLevel(final CallbackContext callbackContext) {
        BaseParams baseParams = new BaseParams(0);
        baseParams.setOnGetResultListener(generateResultListener(callbackContext));
        callbackContext.success(nfcReader.getReader().getBatteryLevelValue());
    }

    public void connectReader(final CallbackContext callbackContext, JSONArray data) {
        if (nfcReader != null) {
            try {
                String mDeviceAddress = data.getString(0);
                Log.d("ACR", "mDevice data:" + data);
                ConnectParams connectParams = new ConnectParams(mDeviceAddress, callbackContext);
                connectParams.setOnGetResultListener(generateResultListener(callbackContext));
                nfcReader.connect(connectParams);
            } catch (JSONException e) {
                e.printStackTrace();
                callbackContext.error("Device Address error!");
            }
        }
    }

    private void disconnectReader() {
        nfcReader.disconnect();
    }

    private void startScan(final CallbackContext callbackContext) {
        nfcReader.startScan(callbackContext);
    }

    private void stopScan(final CallbackContext callbackContext) {
        nfcReader.stopScan();
    }

    private void selectFile(final CallbackContext callbackContext, JSONArray data) {
        try {
            SelectFileParams selectFileParams = new SelectFileParams(0, data.getString(0));
            selectFileParams.setOnGetResultListener(generateResultListener(callbackContext));
            nfcReader.selectFile(selectFileParams);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void readUID(final CallbackContext callbackContext) {
        BaseParams uidParams = new BaseParams(0);
        uidParams.setOnGetResultListener(generateResultListener(callbackContext));
        nfcReader.getUID(uidParams);
    }

    private void getFirmwareVersion(final CallbackContext callbackContext) {
        BaseParams firmwareVersionParams = new BaseParams(0);
        firmwareVersionParams.setOnGetResultListener(generateResultListener(callbackContext));
        nfcReader.getFirmwareVersion(firmwareVersionParams);
    }

    private void getReceivedData(final CallbackContext callbackContext) {
        BaseParams receivedDataParams = new BaseParams(0);
        receivedDataParams.setOnGetResultListener(generateResultListener(callbackContext));
        nfcReader.getReceivedData(receivedDataParams);
    }

    private void getLedStatus(final CallbackContext callbackContext) {
        BaseParams ledStatusParams = new BaseParams(0);
        ledStatusParams.setOnGetResultListener(generateResultListener(callbackContext));
        nfcReader.getLedStatus(ledStatusParams);
    }

    private void writeAuthenticate(final CallbackContext callbackContext, JSONArray data) {
        try {
            AuthParams authParams = new AuthParams(0, data.getInt(0));
            authParams.setKeyA(data.getString(1));
            authParams.setKeyB(data.getString(2));
            authParams.setOnGetResultListener(generateResultListener(callbackContext));
            nfcReader.writeAuthenticate(authParams);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void authenticateWithKeyB(final CallbackContext callbackContext, JSONArray data) {
        try {
            AuthParams authParams = new AuthParams(0, data.getInt(0));
            authParams.setKeyB(data.getString(1));
            authParams.setOnGetResultListener(generateResultListener(callbackContext));
            nfcReader.authenticateWithKeyB(authParams);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void authenticateWithKeyA(final CallbackContext callbackContext, JSONArray data) {
        try {
            AuthParams authParams = new AuthParams(0, data.getInt(0));
            authParams.setKeyA(data.getString(1));
            authParams.setOnGetResultListener(generateResultListener(callbackContext));
            nfcReader.authenticateWithKeyA(authParams);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void readData(final CallbackContext callbackContext, JSONArray data) {
        try {
            ReadParams readParams = new ReadParams(0, data.getInt(0));
            readParams.setPassword(data.getString(1));
            readParams.setOnGetResultListener(generateResultListener(callbackContext));
            nfcReader.readData(readParams);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void writeData(final CallbackContext callbackContext, JSONArray data) {
        try {
            WriteParams writeParams = new WriteParams(0, data.getInt(0), data.getString(1));
            writeParams.setPassword(data.getString(2));
            writeParams.setOnGetResultListener(generateResultListener(callbackContext));
            nfcReader.writeData(writeParams);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void clearLCD(final CallbackContext callbackContext, JSONArray data) {
        ClearLCDParams clearLCDParams = new ClearLCDParams();
        clearLCDParams.setOnGetResultListener(generateResultListener(callbackContext));
        nfcReader.clearLCD(clearLCDParams);
    }

    private void display(final CallbackContext callbackContext, JSONArray data) {
        try {
            // [msg, options.x, options.y, options.bold, options.font]
            DisplayParams displayParams = new DisplayParams(data.getString(0));
            displayParams.setX(data.getInt(1));
            displayParams.setY(data.getInt(2));
            displayParams.setBold(data.getBoolean(3));
            displayParams.setFont(data.getInt(4));
            displayParams.setOnGetResultListener(generateResultListener(callbackContext));
            nfcReader.display(displayParams);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void listen(final CallbackContext callbackContext) {
        Log.w(TAG, "ACR listen");
        nfcReader.listen(generateResultListener(callbackContext));
    }

    private OnGetResultListener generateResultListener(final CallbackContext callbackContext) {
        return result -> {
            Log.d(TAG, "==========" + result.getCommand() + "==========");
            Log.d(TAG, result.isSendPlugin() ? "Send to Plugin" : "Does not Send to Plugin");
            Log.d(TAG, "Success: " + result.isSuccess());
            Log.d(TAG, "Code: " + result.getCodeString());
            if (result.getData() != null) {
                Log.d(TAG, "Data: " + Util.ByteArrayToHexString(result.getData()));
            }
            Log.d(TAG, "====================");
            if (callbackContext != null && result.isSendPlugin()) {
                PluginResult pluginResult = new PluginResult(
                        result.isSuccess() ? PluginResult.Status.OK : PluginResult.Status.ERROR,
                        Util.resultToJSON(result));
                pluginResult.setKeepCallback(true);
                callbackContext.sendPluginResult(pluginResult);
            }
        };
    }

    private Activity getActivity() {
        return this.cordova.getActivity();
    }

    @Override
    public void onDestroy() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        super.onDestroy();
    }
}