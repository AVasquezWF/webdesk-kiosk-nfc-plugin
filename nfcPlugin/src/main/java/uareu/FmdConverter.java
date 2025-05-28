package uareu;

import com.digitalpersona.uareu.Fmd;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Base64;

public class FmdConverter {
    public static JSONObject fmdToJson(Fmd fmd) throws JSONException {
        JSONObject json = new JSONObject();

        json.put("cbeffId", fmd.getCbeffId());
        json.put("captureEquipmentCompliance", fmd.getCaptureEquipmentCompliance());
        json.put("captureEquipmentId", fmd.getCaptureEquipmentId());
        json.put("width", fmd.getWidth());
        json.put("height", fmd.getHeight());
        json.put("resolution", fmd.getResolution());
        json.put("viewCnt", fmd.getViewCnt());
        json.put("format", fmd.getFormat().name());
        json.put("dataBase64", fmd.getData() != null ? Base64.getEncoder().encodeToString(fmd.getData()) : null);

        JSONArray viewsArray = new JSONArray();
        for (Fmd.Fmv view : fmd.getViews()) {
            JSONObject viewJson = new JSONObject();
            viewJson.put("fingerPosition", view.getFingerPosition());
            viewJson.put("viewNumber", view.getViewNumber());
            viewJson.put("impressionType", view.getImpressionType());
            viewJson.put("quality", view.getQuality());
            viewJson.put("minutiaCnt", view.getMinutiaCnt());
            viewJson.put("dataBase64", view.getData() != null ? Base64.getEncoder().encodeToString(view.getData()) : null);
            viewJson.put("extBlockDataBase64", view.getExtBlockData() != null ? Base64.getEncoder().encodeToString(view.getExtBlockData()) : null);
            viewsArray.put(viewJson);
        }

        json.put("views", viewsArray);

        return json;
    }
}
