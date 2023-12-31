package acs;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by kevin on 8/3/15.
 */
public class ChipMeta {
    private String uid;
    private String type;
    private String name;
    private byte[] atr;
    private boolean mifare;

    private final String TAG = "ChipMeta";

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean needAuthentication() {
        return canGetVersion();
//        Log.d("ChipMeta.needAuthentication", (name != null && name == "NTAG213") ? "true" : "false");
//        return name != null && name == "NTAG213";
    }

    public boolean canGetVersion() {
        return type != null && (type == ATRHistorical.MIFARE_ULTRALIGHT_C);
    }

    public void parseATR(byte[] atr) {
        this.atr = atr;
        ATRHistorical atrHistorical = new ATRHistorical(atr);
        this.type = atrHistorical.getType();
    }

    public void parseBlock0(byte[] data) {
        if (data != null && data.length == 16) { // check the Capability Container (CC bytes)
            Log.d("ChipMeta.parseBlock0", Util.ByteArrayToHexString(data));
            if (bitCompare(data[12], (byte) 0xe1)
                    && bitCompare(data[13], (byte) 0x10)
                    && bitCompare(data[14], (byte) 0x12)
                    ) {
                this.type = ATRHistorical.MIFARE_ULTRALIGHT_C;
            }
        }
    }

    public boolean bitCompare(byte a, byte b) {
        Byte ba = new Byte(a);
        Byte bb = new Byte(b);
        return ba.compareTo(bb) == 0;
    }

//
//    public boolean bitCompare(byte a, byte b) {
//        return (a & b) == b;
//    }

    public void setUID(byte[] data) {
        this.uid = Util.toHexString(data);
    }


    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        try {
            if (this.type != null) {
                json.put("type", this.type.toString().toLowerCase().replace(" ", "_"));
            }
            json.put("name", this.name);
            json.put("uid", this.uid);
            json.put("atr", Util.toHexString(this.atr));
            return json;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean isMifare() {
        return this.type != null && (this.type.equals(ATRHistorical.MIFARE_ULTRALIGHT) || this.type.equals(ATRHistorical.MIFARE_ULTRALIGHT_C));
    }

//    public boolean isMifare_C() {
//        return this.type != null && this.type.equals(ATRHistorical.MIFARE_ULTRALIGHT_C);
//    }
}
