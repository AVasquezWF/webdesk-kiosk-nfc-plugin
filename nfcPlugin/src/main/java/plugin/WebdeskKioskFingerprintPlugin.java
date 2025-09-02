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

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Arrays;

import uareu.FmdConverter;
import uareu.FmdFactory;
import uareu.FmdImpl;
import uareu.UareUImpl;


/**
 *
 */
public class WebdeskKioskFingerprintPlugin extends CordovaPlugin {
    private static final String TAG = "WebdeskKioskFingerprintPlugin";
    private static final String ACTION_USB_PERMISSION = "com.digitalpersona.uareu.dpfpddusbhost.USB_PERMISSION";
    Context context;
    UsbDevice device;
    UareUImpl uareu = new UareUImpl();
    
    CallbackContext listenerContext = null;

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
            case "identify":
                return identify(callbackContext, data);
            case "mapBase64ToFMD":
                return mapBase64ToFMD(callbackContext, data);
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
            JSONObject jsonResult = uareu.read();
            callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, jsonResult));
            callbackContext.success();
            return true;
        } catch (Exception e) {
            callbackContext.error(e.toString());
            return false;
        }
    }

    private boolean mapBase64ToFMD(CallbackContext callbackContext, JSONArray data){
        try {
            JSONArray jsonArray = data.getJSONArray(0);

            if(jsonArray.length() == 0){
                throw new Exception("[mapBase64ToFMD]: Provided JSON array is empty");
            }

            JSONArray candidates = new JSONArray();
            for (int i = 0; i < jsonArray.length(); i++) {
                candidates.put(FmdConverter.fmdToJson(FmdFactory.fromBase64(jsonArray.getString(i))));
            }

            JSONObject jsonResult = new JSONObject();
            jsonResult.put("fmd", (candidates));
            callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, jsonResult));
            callbackContext.success();
            return true;
        } catch (Exception e) {
            callbackContext.error(e.toString());
            return false;
        }
    }
    private boolean identify(CallbackContext callbackContext, JSONArray data) {
        try {
            JSONArray jsonArray = data.getJSONArray(1);

            if(jsonArray.length() == 0 || data.getString(0) == null){
                throw new Exception("[identify]: No candidates were provided");
            }

            Fmd target = FmdFactory.fromJsonString(data.getString(0));
            Fmd[] candidates = new Fmd[jsonArray.length()];
            for (int i = 0; i < jsonArray.length(); i++) {
                candidates[i] = FmdFactory.fromBase64(jsonArray.getString(i));
            }

            Log.d(TAG, target.toString());
            Log.d(TAG, Arrays.toString(candidates));

            JSONObject jsonResult = uareu.identify(target, candidates);
            callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, jsonResult));
            callbackContext.success();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            callbackContext.error(e.toString());
            return false;
        }
    }

    private boolean checkIsReady(CallbackContext callbackContext) {
        try{
            uareu.prepare(cordova.getActivity());
            Reader.Capabilities cap = uareu.getCapabilities();
            if(this.listenerContext != null) this.listenerContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, cap.toString()));
            callbackContext.success();
            return true;
        } catch (Exception e) {
            callbackContext.error(e.toString());
            return false;
        }
    }

    private boolean addListener(CallbackContext callbackContext) {
        this.listenerContext = callbackContext;
        uareu.getCapturePollingThread().setOnCapturedCallback(() -> {
            JSONObject result = uareu.read();
            PluginResult readResult = new PluginResult(PluginResult.Status.OK, result);
            readResult.setKeepCallback(true);
            this.listenerContext.sendPluginResult(readResult);
        });
        return true;
    }

    private boolean reconnectReader(CallbackContext callbackContext) {
       try {
           uareu.close();
           uareu.prepare(cordova.getActivity());
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
            uareu.getReader().SetParameter(id, bytes);
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
            uareu.setInterval(data.getLong(0));
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
                device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                    if (device == null) {
                        Toast.makeText(context, "No device found", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    try {
                        Toast.makeText(context, device.getDeviceName(), Toast.LENGTH_SHORT).show();
                        uareu.prepare(cordova.getActivity());
                        uareu.checkDevice();
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