package plugin;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.util.Log;
import android.widget.Toast;

import com.digitalpersona.uareu.Fmd;
import com.digitalpersona.uareu.Reader;
import com.digitalpersona.uareu.UareUException;
import com.digitalpersona.uareu.UareUGlobal;
import java.util.Arrays;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.logging.Logger;

import uareu.FmdConverter;
import uareu.UareUImpl;


/**
 *
 */
public class WebdeskKioskFingerprintPlugin extends CordovaPlugin {
    private static final String TAG = "WebdeskKioskFingerprintPlugin";
    Logger logger = Logger.getLogger(getClass().getName());

    static final String NO_RFID_ERROR = "[NO_RFID_ERROR]: No rfid installed";
    private static final String ACTION_USB_PERMISSION = "com.digitalpersona.uareu.dpfpddusbhost.USB_PERMISSION";
    private static final String RECEIVER_EXPORTED = "com.digitalpersona.uareu.dpfpddusbhost.RECEIVER_EXPORTED";
    private static final String RECEIVER_NOT_EXPORTED = "com.digitalpersona.uareu.dpfpddusbhost.RECEIVER_NOT_EXPORTED";
    Context context;
    UareUImpl reader = new UareUImpl();
    
    CallbackContext listener = null;

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);

        // This is how we get the current activity in Cordova
        this.context = cordova.getActivity().getApplicationContext();
    }

    @Override
    public boolean execute(String action, JSONArray data, CallbackContext callbackContext) {
        switch (action) {
            case "init":
                return init(callbackContext);
            case "reconnectReader":
                return reconnectReader(callbackContext);
            case "checkIsReady":
                return checkIsReady(callbackContext);
            case "addListener":
                return addListener(callbackContext);
            case "readCard":
                return readCard(callbackContext);
            case "sendReaderCommand":
                return sendReaderCommand(callbackContext, data);
            case "setListenerInterval":
                return setListenerInterval(callbackContext, data);
            default:
                callbackContext.error("[execute]: No action found");
                return false;
        }
    }

    private boolean readCard(CallbackContext callbackContext) {
       try {
           Reader.CaptureResult res = reader.capture();

           String result = reader.getImageAsBase64(res);
           Log.d(TAG,"[readCard] Read success");

           JSONObject jsonResult = new JSONObject();
           jsonResult.put("base64Image", result);

           try {
               Fmd fmd = UareUGlobal.GetEngine().CreateFmd(res.image, Fmd.Format.ANSI_378_2004);
               Log.d(TAG, Arrays.toString(fmd.getData()));
               JSONObject fmdObject = FmdConverter.fmdToJson(fmd);
               jsonResult.put("fmd", fmdObject);
           } catch (Exception e) {
               Log.e(TAG, e.toString());
           }

           callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, jsonResult));
           callbackContext.success();
           return true;
       } catch (Exception e) {
           callbackContext.error(e.toString());
           return false;
       }
    }

    private boolean checkIsReady(CallbackContext callbackContext) {
        try{
            reader.prepare(cordova.getActivity());
            reader.getCapabilities();
            callbackContext.success();
            return true;
        } catch (Exception e) {
            callbackContext.error(e.toString());
            return false;
        }
    }

    private boolean addListener(CallbackContext callbackContext) {
        this.listener = callbackContext;
        return true;
    }

    private boolean reconnectReader(CallbackContext callbackContext) {
       try {
           reader.prepare(cordova.getActivity());
           callbackContext.success();
       }
       catch (Exception e) {
            callbackContext.error("[reconnectReader] A problem occurred while attempting a reconnection");
       }

       return true;
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    private boolean init(CallbackContext callbackContext) {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_USB_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        cordova.getActivity().registerReceiver(broadcastReceiver, filter);

        callbackContext.success();
        return true;
    }

    private boolean sendReaderCommand(CallbackContext callbackContext, JSONArray data) {
        try {
            Reader.ParamId id = Reader.ParamId.values()[data.getInt(0)];
            byte[] bytes = data.getString(1).getBytes();
            reader.getReader().SetParameter(id, bytes);
            boolean res = true;
            callbackContext.success(data + " " + res);
            return res;
        } catch (Exception e) {
            callbackContext.error(e.toString());
            e.printStackTrace();
            return false;
        }
    }

    private boolean setListenerInterval(CallbackContext callbackContext, JSONArray data) {
        try {
            Log.w(TAG, "setListenerInterval is not supported");
            callbackContext.success(data);
            return true;
        } catch (Exception e) {
            callbackContext.error(e.toString());
            e.printStackTrace();
            return false;
        }
    }


    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver()
    {
        public void onReceive(Context context, Intent intent)
        {
            String action = intent.getAction();
            if (!ACTION_USB_PERMISSION.equals(action)){
                return;
            }

            synchronized (this) {
                UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                    if (device == null) {
                        Toast.makeText(context, "No device found", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    try {
                        Toast.makeText(context, device.getDeviceName(), Toast.LENGTH_SHORT).show();
                        reader.prepare(cordova.getActivity());
                        reader.checkDevice();
                    } catch (UareUException e) {
                        Toast.makeText(context, e.toString(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(context, "No permissions to manage USB", Toast.LENGTH_SHORT).show();
                }
            }
        }
    };
}