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

    String TYPE_ID = "02";
    String TYPE_IC = "01";

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
        switch (action){
            case "init": return Init(callbackContext);
            case "checkIsReady": return CheckIsReady(callbackContext);
            case "addListener": return AddListener(callbackContext);
            default:
                callbackContext.error("[execute]: No action found");
                return false;
        }
    }

    private boolean CheckIsReady(CallbackContext callbackContext) {
        if (rfid == null) {
            callbackContext.error("[CheckIsReady]: No rfid installed");
            return false;
        }
        System.out.println(rfid);
        rfid.searchTag();
        System.out.println("[CheckIsReady] Tag searched");
        rfid.readTag();
        System.out.println("[CheckIsReady] Tag read");
        callbackContext.success();
        return true;
    }

    private boolean AddListener(CallbackContext callbackContext) {
        if (rfid == null) {
            callbackContext.error("[AddListener]: No rfid installed");
            return false;
        }

        rfid.getData((RfidModuleUtil.OnGetDataListener) (cardType, var1) -> {
            System.out.println(cardType + "--->" + var1);

            if ("unknown".equals(var1)) {
                System.out.println(cardType + "--->" + "unknown");
            }

            if ("Unsupported card type".equals(var1)) {
                System.out.println(cardType + "--->" + "Unsupported card type");
            }

            if ("No tag".equals(var1)) {
                System.out.println(cardType + "--->" + "No tag");
            }

            if ("Success".equals(var1)) {
                System.out.println(cardType + "--->" + "Success");
            }
        });
        System.out.println("[AddListener] Listener added");
        callbackContext.success();
        /*
        rfid.getData((buffer, size) -> {
            byte[] data = new byte[size];
            for (int i = 0; i < size; i++) {
                data[i] = buffer[i];
            }
            String hexData = HexUtil.ByteArrToHex(data).replace(" ", "");
            System.out.println(hexData);
            String cardType = hexData.substring(4, 6);
            String cardData = null;
            if (cardType.equals(TYPE_ID)) {
                cardData = hexData.substring(8, hexData.length() - 4);
            } else if (cardType.equals(TYPE_IC)) {
                cardData = hexData.substring(6, hexData.length() - 4);
            }
            System.out.println(cardData);
        });*/
        return true;
    }

    private boolean Init(CallbackContext callbackContext) {
        rfid = new RfidModuleUtil(context);
        rfid.setBeep(true);
        rfid.init();
        System.out.println("[Init] RfidModuleUtil initialized");
        rfid.start();
        System.out.println("[Init] RfidModuleUtil started");

        /*
        RS485Util rfid = new RS485Util(context);
        rfid.setCOM("/dev/ttyS7");
        rfid.start();*/
        callbackContext.success();
        return true; 
    }


}