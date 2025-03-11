package UareU;

import com.digitalpersona.uareu.Reader;
import com.digitalpersona.uareu.UareUException;

public class UareUImpl {
    Reader reader;
    public Reader.Capabilities CheckDevice() throws UareUException {
        reader.Open(Reader.Priority.EXCLUSIVE);
        Reader.Capabilities cap = reader.GetCapabilities();
        reader.Close();
        return cap;
    }
}
