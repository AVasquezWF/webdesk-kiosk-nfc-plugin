package UareU;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.digitalpersona.uareu.Fid;
import com.digitalpersona.uareu.Quality;
import com.digitalpersona.uareu.Reader;
import com.digitalpersona.uareu.ReaderCollection;
import com.digitalpersona.uareu.UareUException;
import com.digitalpersona.uareu.jni.Dpfpdd;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.util.Base64;
import java.io.ByteArrayOutputStream;

import java.util.Arrays;
import java.util.Objects;

import acsimpl.apdu.Result;

public class UareUImpl {
    Dpfpdd dpfpdd = new Dpfpdd();
    Reader reader;
    int DPI;
    String deviceName;


    public Reader.Capabilities getCapabilities() throws UareUException {
        Reader.Capabilities cap = reader.GetCapabilities();
        Log.e("Capabilities --- ", cap.toString());
        reader.Close();
        return cap;
    }

    public Reader.CaptureResult capture() throws  UareUException {
         Reader.CaptureResult result =
                reader.Capture(
                        Fid.Format.ANSI_381_2004,
                        Globals.DefaultImageProcessing,
                        DPI,
                        -1);

        reader.Close();
        return result;
    }

    /**
     * Captures one frame from the fingerprint stream and returns it as a BufferedImage.
     *
     * @return String image as base64.
     * @throws UareUException if capture fails.
     */
    public String captureStreamImage() throws UareUException {
        Fid fid = reader.GetStreamImage(Fid.Format.ANSI_381_2004, Reader.ImageProcessing.IMG_PROC_DEFAULT, 500).image;
        Fid.Fiv view = fid.getViews()[0];
        byte[] rawImageData = view.getImageData();
        int width = view.getWidth();
        int height = view.getHeight();

        Log.d("UareU", Arrays.toString(rawImageData));
        return encodeFingerprintImageToBase64(rawImageData, width, height);
    }

    public static String encodeFingerprintImageToBase64(byte[] imageData, int width, int height) {
        Bitmap bitmap = Bitmap.createBitmap(width, height, Config.ALPHA_8);

        // Fill bitmap pixel-by-pixel
        int index = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int gray = imageData[index++] & 0xFF;
                bitmap.setPixel(x, y, 0xFF000000 | (gray << 16) | (gray << 8) | gray);
            }
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);

        byte[] pngBytes = baos.toByteArray();
        return Base64.encodeToString(pngBytes, Base64.NO_WRAP);
    }

    public Reader.CaptureResult checkDevice() throws UareUException {
        Reader.CaptureResult result =
                reader.Capture(
                        Fid.Format.ANSI_381_2004,
                        Globals.DefaultImageProcessing,
                        DPI,
                        -1);

        Log.e("Reader --- ", result.toString());
        Reader.Capabilities cap = reader.GetCapabilities();
        Log.e("Capabilities --- ", cap.toString());

        reader.Close();
        return result;
    }

    public void prepare(Activity activity) {
        try
		{
            Context applicationContext = activity.getApplicationContext();
            dpfpdd.init(applicationContext, null);
            ReaderCollection readerCollection = Globals.getInstance().getReaders(applicationContext);
            Log.e("Reader collection --- ", readerCollection.toString());
            deviceName = readerCollection.get(0).GetDescription().name;
            Log.e("DeviceName --- ", deviceName);
            Globals.DefaultImageProcessing = Reader.ImageProcessing.IMG_PROC_DEFAULT;
            reader = Globals.getInstance().getReader(deviceName, applicationContext);
            if (reader == null) throw new Exception("[prepare]: No reader assigned");
			reader.Open(Reader.Priority.EXCLUSIVE);
			DPI = Globals.GetFirstDPI(reader);
        } catch (Exception e) {
			Log.w("UareUSampleJava", e);
			deviceName = "";
		}
    }
}
