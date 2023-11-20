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


        RS485Util rfid = new RS485Util(context);
        rfid.setCOM("/dev/ttyS7");
        rfid.start();
        return true;
    }

}