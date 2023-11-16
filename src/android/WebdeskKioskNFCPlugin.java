package at.workflow;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;

public class WebdeskKioskNFCPlugin extends CordovaPlugin {

    @Override
    public boolean execute(String action, JSONArray data, CallbackContext callbackContext) {
        System.out.println(action);
        System.out.println(data);
        System.out.println(callbackContext);
        return true;
    }

}