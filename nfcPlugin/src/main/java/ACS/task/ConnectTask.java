package acs.task;

import android.os.AsyncTask;
import acs.operate.ConnectReader;
import acs.params.ConnectParams;

/**
 * Created by cain on 16/4/26.
 */
public class ConnectTask extends AsyncTask<ConnectParams, Void, Boolean> {
    @Override
    protected Boolean doInBackground(ConnectParams... connectParamses) {
        ConnectParams connectParams = connectParamses[0];
        if (connectParams == null) {
            return false;
        }
//        connectParams.getReader().getReader().connect(connectParams.getmDeviceAddress());
        ConnectReader connectReader = new ConnectReader(connectParams);
        connectReader.run();
        return true;
    }
}
