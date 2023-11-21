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
        System.out.println(action);
        System.out.println(data);
        System.out.println(callbackContext);

        switch (action){
            case "init": return StartReader();
            case "checkIsReady": return CheckIsReady();
            case "addListener": return AddListener();
            default: return false;
        }
    }

    private boolean CheckIsReady() {
        if (rfid == null) {
            return false;
        }
        System.out.println(rfid);
        rfid.searchTag();
        rfid.readTag();
        return true;
    }

    private boolean AddListener() {
        if (rfid == null) {
            return false;
        }
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
        rfid.getData((RfidModuleUtil.OnGetDataListener) (cardType, var1) -> cordova.getActivity().runOnUiThread(() -> {
            System.out.println(cardType + "--->" + var1);

            if ("unknown".equals(var1)){
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
        }));
        return true;
    }

    private boolean StartReader() {
        rfid = new RfidModuleUtil(context);
        rfid.init();
        rfid.start();
        /* 
         RS485Util rfid = new RS485Util(context);

        rfid.setCOM("/dev/ttyS7");

        rfid.start();*/
        return true; 
    }


}