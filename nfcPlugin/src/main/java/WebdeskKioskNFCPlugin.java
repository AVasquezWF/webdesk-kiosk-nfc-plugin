import android.content.Context;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;

import Elatec.RfidModuleUtil;

/**
 *
 */
public class WebdeskKioskNFCPlugin extends CordovaPlugin {
    Context context;
    RfidModuleUtil rfid = null;

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
        if (rfid == null) {
            callbackContext.error("[readCard]: No rfid installed");
            return false;
        }
        rfid.searchTag();
        System.out.println("[CheckIsReady] Tag searched");
        rfid.readTag();
        System.out.println("[CheckIsReady] Tag read");
        callbackContext.success();
        return true;
    }

    private boolean checkIsReady(CallbackContext callbackContext) {
        if (rfid == null) {
            callbackContext.error("[checkIsReady]: No rfid installed");
            return false;
        }
        callbackContext.success();
        return true;
    }

    private boolean addListener(CallbackContext callbackContext) {
        this.listener = callbackContext;
        return true;
    }

    private boolean init(CallbackContext callbackContext) {
        if (rfid != null) {
           rfid.stop();
        }

        rfid = new RfidModuleUtil(context);
        rfid.setBeep(false);

        rfid.getData(new RfidModuleUtil.OnGetDataListener() {
            @Override
            public void onDataReceive(String cardType, String cardData) {
                if (listener == null){
                    return;
                }

                System.out.println("[addListener.onGetDataListener]: Data was retrieved: " + cardType + " - " + cardData);

                if (cardType == null) {
                    System.out.println("[addListener.onGetDataListener] Error found: " + cardData);
                    PluginResult result = new PluginResult(PluginResult.Status.ERROR, cardData);
                    result.setKeepCallback(true);
                    listener.sendPluginResult(result);

                } else {
                    System.out.println("[addListener.onGetDataListener]" + cardType + " => " + cardData);
                    PluginResult result = new PluginResult(PluginResult.Status.OK, cardData);
                    result.setKeepCallback(true);
                    listener.sendPluginResult(result);
                }
            }

            @Override
            public void onTagDetached() {
                System.out.println("[onTagDetached]");
            }

            @Override
            public void onTagAttached() {
                System.out.println("[onTagAttached]");
            }
        });

        int result = rfid.init();

        if (result == 1){
            System.out.println("[init] Elatec.RfidModuleUtil initialized");
            rfid.start();
            System.out.println("[init] Elatec.RfidModuleUtil started");
            rfid.listenForTag();
            callbackContext.success();
        } else {
            callbackContext.error(result);
        }
        return true;
    }

    private boolean sendReaderCommand(CallbackContext callbackContext, JSONArray data) {
        if (rfid == null) {
            callbackContext.error("[checkIsReady]: No rfid installed");
            return false;
        }
        try {
            boolean res = rfid.sendCommand(data.getString(0));
            callbackContext.success(data + " " + res);
            return res;
        } catch (JSONException e) {
            callbackContext.error(e.toString());
            e.printStackTrace();
            return false;
        }
    }

    private boolean setListenerInterval(CallbackContext callbackContext, JSONArray data) {
        if (rfid == null) {
            callbackContext.error("[checkIsReady]: No rfid installed");
            return false;
        }
        try {
            rfid.setSleepTime(data.getLong(0));
            callbackContext.success(data);
            return true;
        } catch (JSONException e) {
            callbackContext.error(e.toString());
            e.printStackTrace();
            return false;
        }
    }
}