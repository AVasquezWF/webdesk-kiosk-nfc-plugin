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
    RfidModuleUtil rfid;

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
                return Init(callbackContext);
            case "checkIsReady":
                return CheckIsReady(callbackContext);
            case "addListener":
                return AddListener(callbackContext);
            case "poolReadTag":
                return PoolReadTag(callbackContext);
            default:
                callbackContext.error("[execute]: No action found");
                return false;
        }
    }

    private boolean PoolReadTag(CallbackContext callbackContext) {
        if (rfid == null) {
            callbackContext.error("[PoolReadTag]: No rfid installed");
            return false;
        }
        this.AddListener(callbackContext);
        System.out.println(rfid);
        rfid.searchTag();
        System.out.println("[CheckIsReady] Tag searched");
        rfid.readTag();
        System.out.println("[CheckIsReady] Tag read");
        return true;
    }

    private boolean CheckIsReady(CallbackContext callbackContext) {
        if (rfid == null) {
            callbackContext.error("[CheckIsReady]: No rfid installed");
            return false;
        }
        callbackContext.success();
        return true;
    }

    private boolean AddListener(CallbackContext callbackContext) {
        if (rfid == null) {
            callbackContext.error("[AddListener]: No rfid installed");
            return false;
        }

        rfid.getData((RfidModuleUtil.OnGetDataListener) (cardType, var1) -> {
            callbackContext.error("[OnGetDataListener]: Data was retrieved");
            System.out.println(cardType + "--->" + var1);

            if ("unknown".equals(var1)) {
                System.out.println(cardType + "--->" + "unknown");
            } else if ("Unsupported card type".equals(var1)) {
                System.out.println(cardType + "--->" + "Unsupported card type");
            } else if ("No tag".equals(var1)) {
                System.out.println(cardType + "--->" + "No tag");
            } else if ("Success".equals(var1)) {
                System.out.println(cardType + "--->" + "Success");
            } else {
                callbackContext.success(var1);
            }
        });
        System.out.println("[AddListener] Listener added");
        return true;
    }

    private boolean Init(CallbackContext callbackContext) {
        rfid = new RfidModuleUtil(context);
        rfid.setBeep(true);
        rfid.init();
        System.out.println("[Init] RfidModuleUtil initialized");
        rfid.start();
        System.out.println("[Init] RfidModuleUtil started");
        callbackContext.success();
        return true;
    }

}