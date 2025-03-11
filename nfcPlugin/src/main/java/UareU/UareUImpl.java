package UareU;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.digitalpersona.uareu.Fid;
import com.digitalpersona.uareu.Quality;
import com.digitalpersona.uareu.Reader;
import com.digitalpersona.uareu.UareUException;
import com.digitalpersona.uareu.jni.DpfjQuality;

import java.util.Objects;

public class UareUImpl {
    Reader reader;
    int DPI;
    String deviceName;


    public Reader.Capabilities getCapabilities(Activity activity) throws UareUException {
        prepare(activity);
        Reader.Capabilities cap = reader.GetCapabilities();
        Log.e("Capabilities --- ", cap.toString());
        reader.Close();
        return cap;
    }

    public void checkDevice(Activity activity) throws UareUException {
        prepare(activity);
        reader.Open(Reader.Priority.EXCLUSIVE);
        Reader.CaptureResult result =
                reader.Capture(
                        Fid.Format.ANSI_381_2004,
                        Globals.DefaultImageProcessing,
                        DPI,
                        -1);

        Log.e("Reader --- ",result.toString());
        Reader.Capabilities cap = reader.GetCapabilities();
        Log.e("Capabilities --- ", cap.toString());
        reader.Close();
    }

    void prepare(Activity activity) {
        try 
		{
            deviceName = Objects.requireNonNull(activity.getIntent().getExtras()).getString("device_name");
            Globals.DefaultImageProcessing = Reader.ImageProcessing.IMG_PROC_DEFAULT;
            Context applContext = activity.getApplicationContext();
			reader = Globals.getInstance().getReader(deviceName, applContext);
			reader.Open(Reader.Priority.EXCLUSIVE);
			DPI = Globals.GetFirstDPI(reader);
		} catch (Exception e) {
			Log.w("UareUSampleJava", "error during init of reader");
			deviceName = "";
		}
    }
}
