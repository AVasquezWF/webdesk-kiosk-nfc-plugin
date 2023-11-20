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
            case "checkIsReady": return StartReader();
            case "addListener": return StartReader();
            default: return false;
        }
    }

    private boolean StartReader() {
        String TYPE_ID = "02";
        String TYPE_IC = "01";

        RS485Util rfid = new RS485Util(context);
        rfid.setCOM("/dev/ttyS7");
        if (rfid == null) {
            return false;
        }

        rfid.start();
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
        });
        return true;

    }


}