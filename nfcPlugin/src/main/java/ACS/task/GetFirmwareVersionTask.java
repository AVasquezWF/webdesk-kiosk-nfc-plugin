package acs.task;

import android.os.AsyncTask;
import acs.apdu.command.GetFirmwareVersion;
import acs.params.BaseParams;

/**
 * Created by kevin on 16/2/23.
 */
public class GetFirmwareVersionTask extends AsyncTask<BaseParams, Void, Boolean> {

    final private String TAG = "GetFirmwareVersionTask";

    @Override
    protected Boolean doInBackground(BaseParams... paramses) {
        BaseParams params = paramses[0];
        if (params == null) {
            return false;
        }
        if(!params.getReader().isReady()){
            params.getReader().raiseNotReady(params.getOnGetResultListener());
        }
        GetFirmwareVersion getFirmwareVersion = new GetFirmwareVersion(params);
        return getFirmwareVersion.run();
    }
}
