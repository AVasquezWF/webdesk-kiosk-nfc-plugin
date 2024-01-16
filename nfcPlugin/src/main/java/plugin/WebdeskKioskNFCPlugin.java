package plugin;

import android.content.Context;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;

import java.util.logging.Logger;

import elatec.RfidModuleUtil;

/**
 *
 */
public class WebdeskKioskNFCPlugin extends CordovaPlugin {
    Logger logger = Logger.getLogger(getClass().getName());

    static final String NO_RFID_ERROR = "[NO_RFID_ERROR]: No rfid installed";
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
            callbackContext.error(NO_RFID_ERROR);
            return false;
        }
        rfid.searchTag();
        logger.info("[CheckIsReady] Tag searched");
        rfid.readTag();
        logger.info("[CheckIsReady] Tag read");
        callbackContext.success();
        return true;
    }

    private boolean checkIsReady(CallbackContext callbackContext) {
        if (rfid == null) {
            callbackContext.error(NO_RFID_ERROR);
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

        rfid.getData(new RfidModuleUtil.onGetDataListener() {
            @Override
            public void onDataReceive(String cardType, String cardData) {
                if (listener == null){
                    return;
                }

                logger.info("[addListener.onGetDataListener]: Data was retrieved: " + cardType + " - " + cardData);

                if (cardType == null) {
                    logger.info("[addListener.onGetDataListener] Error found: " + cardData);
                    PluginResult result = new PluginResult(PluginResult.Status.ERROR, cardData);
                    result.setKeepCallback(true);
                    listener.sendPluginResult(result);

                } else {
                    logger.info("[addListener.onGetDataListener]" + cardType + " => " + cardData);
                    PluginResult result = new PluginResult(PluginResult.Status.OK, cardData);
                    result.setKeepCallback(true);
                    listener.sendPluginResult(result);
                }
            }

            @Override
            public void onTagDetached() {
                logger.info("[onTagDetached]");
            }

            @Override
            public void onTagAttached() {
                logger.info("[onTagAttached]");
            }
        });

        int result = rfid.init();

        if (result == 1){
            logger.info("[init] elatec.RfidModuleUtil initialized");
            rfid.start();
            logger.info("[init] elatec.RfidModuleUtil started");
            rfid.listenForTag();
            callbackContext.success();
        } else {
            callbackContext.error(result);
        }
        return true;
    }

    private boolean sendReaderCommand(CallbackContext callbackContext, JSONArray data) {
        if (rfid == null) {
            callbackContext.error(NO_RFID_ERROR);
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
            callbackContext.error(NO_RFID_ERROR);
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