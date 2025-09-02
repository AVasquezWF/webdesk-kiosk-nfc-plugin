package uareu;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.Base64;
import android.util.Log;

import com.digitalpersona.uareu.Engine;
import com.digitalpersona.uareu.Fid;
import com.digitalpersona.uareu.Fmd;
import com.digitalpersona.uareu.Reader;
import com.digitalpersona.uareu.ReaderCollection;
import com.digitalpersona.uareu.UareUException;
import com.digitalpersona.uareu.UareUGlobal;
import com.digitalpersona.uareu.jni.Dpfpdd;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;

public class UareUImpl {
    String TAG = "UareUImpl";
    Dpfpdd dpfpdd = new Dpfpdd();
    private Reader reader;
    int DPI;
    String deviceName;
    CapturePollingThread capturePollingThread = new CapturePollingThread();

    public Reader.Capabilities getCapabilities() {
        Reader.Capabilities cap = getReader().GetCapabilities();
        Log.e("Capabilities --- ", cap.toString());
        return cap;
    }

    public Reader.CaptureResult capture() throws UareUException {
        return getReader().Capture(
                Fid.Format.ANSI_381_2004,
                Globals.DefaultImageProcessing,
                DPI,
                -1);
    }

    public JSONObject read() {
        JSONObject jsonResult = new JSONObject();

        try {
            Reader.CaptureResult res = capture();

            String result = getImageAsBase64(res);
            Log.d(TAG,"[read] Capture success");
            jsonResult.put("base64Image", result);

            Fmd fmd = UareUGlobal.GetEngine().CreateFmd(res.image, Fmd.Format.ANSI_378_2004);
            Log.d(TAG, Arrays.toString(fmd.getData()));
            JSONObject fmdObject = FmdConverter.fmdToJson(fmd);
            jsonResult.put("fmd", fmdObject);
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }

        return jsonResult;
    }

    public JSONObject identify(Fmd target, Fmd[] candidates) {
        JSONObject jsonResult = new JSONObject();
        try {
            Engine.Candidate[] result = UareUGlobal.GetEngine().Identify(target, 0, candidates, 2147483, 1);
            Log.d(TAG, "Results: " + result.length);
            int candidateIndex = result[0].fmd_index;
            Fmd chosenCandidate = candidates[candidateIndex];
            Log.d(TAG, "candidateIndex: "+ candidateIndex);
            JSONObject fmdObject = FmdConverter.fmdToJson(chosenCandidate);
            Log.d(TAG, "identify: "+ Arrays.toString(chosenCandidate.getData()));
            jsonResult.put("fmd", fmdObject);
            jsonResult.put("candidateIndex", candidateIndex);
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }

        return jsonResult;
    }

    /**
     * Captures one frame from the fingerprint stream and returns it as a BufferedImage.
     *
     * @return String image as base64.
     */
    public String getImageAsBase64(Reader.CaptureResult res) {
        try {
            Fid fid = res.image;
            if (fid == null || fid.getViews() == null || fid.getViews().length == 0) {
                throw new UareUException(96076126);
            }

            Fid.Fiv view = fid.getViews()[0];
            byte[] rawImage = view.getData();
            int width = view.getWidth();
            int height = view.getHeight();

            Log.d(TAG, "Width " + width);
            Log.d(TAG, "Height " + height);
            Log.d(TAG, "Views " + fid.getViews().length);
            Log.d(TAG, "Image data length: " + rawImage.length);
            return encodeFingerprintImageToBase64(rawImage, width, height);

        } catch (Exception e) {
            Log.e(TAG, "Error capturing fingerprint: ", e);
            return null;
        }
    }

    public String encodeFingerprintImageToBase64(byte[] imageData, int width, int height) {
        if (imageData == null) {
            throw new IllegalArgumentException("Invalid image data, the image is empty");
        }
        if (imageData.length >= width * height) {
            Log.w(TAG,(width * height) + " is smaller than the provided " + imageData.length);
            imageData = Arrays.copyOf(imageData, width * height);
        } else {
            throw new IllegalArgumentException("Not enough image data");
        }

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        int index = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int gray = imageData[index++] & 0xFF;
                int pixel = 0xFF000000 | (gray << 16) | (gray << 8) | gray; // ARGB format
                bitmap.setPixel(x, y, pixel);
            }
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        boolean success = bitmap.compress(Bitmap.CompressFormat.WEBP, 80, baos);
        if (!success) {
            throw new RuntimeException("Failed to compress bitmap");
        }

        byte[] pngBytes = baos.toByteArray();
        return Base64.encodeToString(pngBytes, Base64.NO_WRAP);
    }

    public Reader.CaptureResult checkDevice() throws UareUException {
        Reader.CaptureResult result =
                getReader().Capture(
                        Fid.Format.ANSI_381_2004,
                        Globals.DefaultImageProcessing,
                        DPI,
                        -1);

        Log.d(TAG,"Reader --- "+ result.toString());
        Reader.Capabilities cap = getReader().GetCapabilities();
        Log.d(TAG,"Capabilities --- " + cap.toString());
        return result;
    }

    public void prepare(Activity activity) {
        if(reader != null) return;
        try
		{
            Context applicationContext = activity.getApplicationContext();
            dpfpdd.init(applicationContext, null);
            ReaderCollection readerCollection = Globals.getInstance().getReaders(applicationContext);
            Log.d(TAG,"Reader collection --- " + readerCollection.toString());
            deviceName = readerCollection.get(0).GetDescription().name;
            Log.d(TAG,"DeviceName --- " + deviceName);
            Globals.DefaultImageProcessing = Reader.ImageProcessing.IMG_PROC_DEFAULT;
            reader = Globals.getInstance().getReader(deviceName, applicationContext);
            if (getReader() == null) throw new UareUException(96075807);
            getReader().Open(Reader.Priority.EXCLUSIVE);
            DPI = Globals.GetFirstDPI(getReader());
            if (DPI <= 0) {
                DPI = 500;
                Log.w(TAG, "DPI invalid. Falling back to default 500 DPI.");
            }
            capturePollingThread.start();
        } catch (Exception e) {
			Log.w(TAG, e);
			deviceName = "";
		}
    }

    public void setInterval(long ms){
        capturePollingThread.setIntervalMs(ms);
    }

    public void close() throws UareUException {
        capturePollingThread.stop();
        getReader().Close();
        reader = null;
    }

    public CapturePollingThread getCapturePollingThread() {
        return capturePollingThread;
    }

    public Reader getReader() {
        if (reader == null) {
            throw new IllegalStateException("Reader is not initialized. Call prepare() first.");
        }
        return reader;
    }
}
