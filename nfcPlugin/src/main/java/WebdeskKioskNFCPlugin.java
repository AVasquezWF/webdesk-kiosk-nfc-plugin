import android.content.Context;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;
import org.json.JSONTokener;

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
            case "readCard":
                return readCard(callbackContext);
            default:
                callbackContext.error("[execute]: No action found");
                return false;
        }
    }

    private boolean readCard(CallbackContext callbackContext) {
        if (rfid == null) {
            callbackContext.error("[AddListener]: No rfid installed");
            return false;
        }
        rfid.getData((cardType, cardData) -> {
            System.out.println("[OnGetDataListener]: Data was retrieved");

            if (isValidJson(cardData)) {
                System.out.println(cardType + "--->" + cardData);
                callbackContext.success(cardData);
            } else {
                callbackContext.error(cardData);
            }
        });
        System.out.println("[AddListener] Listener added");
        rfid.searchTag();
        System.out.println("[CheckIsReady] Tag searched");
        rfid.readTag();
        System.out.println("[CheckIsReady] Tag read");
        return true;
    }

    private boolean checkIsReady(CallbackContext callbackContext) {
        if (rfid == null) {
            callbackContext.error("[CheckIsReady]: No rfid installed");
            return false;
        }
        callbackContext.success();
        return true;
    }

    private boolean init(CallbackContext callbackContext) {
        rfid = new RfidModuleUtil(context);
        rfid.setBeep(true);
        rfid.init();
        System.out.println("[Init] RfidModuleUtil initialized");
        rfid.start();
        System.out.println("[Init] RfidModuleUtil started");
        callbackContext.success();
        return true;
    }

    private boolean isValidJson(String jsonString) {
        try {
            // Use JSONTokener to check if the string is a valid JSON
            new JSONTokener(jsonString).nextValue();
            return true;
        } catch (Exception e) {
            // JSONTokener throws an exception if the string is not valid JSON
            return false;
        }
    }
}