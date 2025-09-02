package uareu;

import com.digitalpersona.uareu.Fid;
import com.digitalpersona.uareu.Fmd;
import com.digitalpersona.uareu.UareUException;
import com.digitalpersona.uareu.UareUGlobal;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.Base64;

public class FmdFactory {
    public static Fmd fromJsonString(String jsonString) throws JSONException {
        JSONObject obj = new JSONObject(jsonString);

        FmdImpl fmd = new FmdImpl();
        fmd.cbeffId = obj.getInt("cbeffId");
        fmd.captureEquipmentCompliance = obj.getInt("captureEquipmentCompliance");
        fmd.captureEquipmentId = obj.getInt("captureEquipmentId");
        fmd.width = obj.getInt("width");
        fmd.height = obj.getInt("height");
        fmd.resolution = obj.getInt("resolution");
        fmd.viewCnt = obj.getInt("viewCnt");
        fmd.format = Fmd.Format.valueOf(obj.getString("format"));
        fmd.data = Base64.getDecoder().decode(obj.getString("dataBase64"));

        JSONArray viewsArray = obj.getJSONArray("views");
        FmdImpl.FmvImpl[] views = new FmdImpl.FmvImpl[viewsArray.length()];

        for (int i = 0; i < viewsArray.length(); i++) {
            JSONObject viewObj = viewsArray.getJSONObject(i);
            FmdImpl.FmvImpl view = new FmdImpl.FmvImpl();

            view.fingerPosition = viewObj.getInt("fingerPosition");
            view.viewNumber = viewObj.getInt("viewNumber");
            view.impressionType = viewObj.getInt("impressionType");
            view.quality = viewObj.getInt("quality");
            view.minutiaCnt = viewObj.getInt("minutiaCnt");
            view.data = Base64.getDecoder().decode(viewObj.getString("dataBase64"));
            views[i] = view;
        }

        fmd.views = views;
        return fmd;
    }

    public static Fmd fromBase64(String data) throws UareUException {
        // Decode the base64 string into byte array
        byte[] decodedBytes = Base64.getDecoder().decode(data);
        return UareUGlobal.GetImporter().ImportFmd(decodedBytes, Fmd.Format.ANSI_378_2004, Fmd.Format.ANSI_378_2004);
    }
    public static byte[] getBytesFromBase64(String data)  {
        // Decode the base64 string into byte array
        return Base64.getDecoder().decode(data);
    }
}

