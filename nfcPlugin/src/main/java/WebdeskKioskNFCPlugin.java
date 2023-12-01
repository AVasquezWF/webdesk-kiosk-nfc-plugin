import android.content.Context;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;

/**
 *
 */
public class WebdeskKioskNFCPlugin extends CordovaPlugin {
    Context context;
    RfidModuleUtil rfid = null;

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
        if (rfid == null) {
            callbackContext.error("[addListener]: No rfid installed");
            return false;
        }
        rfid.getData((cardType, cardData) -> {
            System.out.println("[addListener.onGetDataListener]: Data was retrieved: " + cardType + " - " + cardData);
            if (cardType == null) {
                System.out.println("[addListener.onGetDataListener] Error found: " + cardData);
                callbackContext.error(cardData);
            } else {
                System.out.println("[addListener.onGetDataListener]" + cardType + " => " + cardData);
                callbackContext.success(cardData);
            }
        });
        rfid.searchTag();
        rfid.readTag();
        System.out.println("[addListener] Listener added");
        return true;
    }

    private boolean init(CallbackContext callbackContext) {
        //if (rfid != null) {
        //   rfid.stop();
        //}

        rfid = new RfidModuleUtil(context);
        rfid.setBeep(false);

        int result = rfid.init();

        if (result == 1){
            System.out.println("[init] RfidModuleUtil initialized");
            rfid.start();
            System.out.println("[init] RfidModuleUtil started");
            callbackContext.success();
        } else {
            callbackContext.error(result);
        }
        return true;
    }
}