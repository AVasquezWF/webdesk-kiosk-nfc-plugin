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

import java.util.Objects;

public class UareUImpl {
    Dpfpdd dpfpdd = new Dpfpdd();
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
    }

    void prepare(Activity activity) {
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
