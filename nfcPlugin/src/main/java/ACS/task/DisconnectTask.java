package acs.task;

import android.os.AsyncTask;
import android.util.Log;
import acs.operate.DisconnectReader;
import acs.params.BaseParams;
import acs.params.DisconnectParams;

/**
 * Created by kevin on 16/3/25.
 */
public class DisconnectTask extends AsyncTask<BaseParams, Void, Boolean> {
    @Override
    protected Boolean doInBackground(BaseParams... paramses) {
        BaseParams params = paramses[0];
        if (params == null) {
            return false;
        }
//        if(!params.getReader().isReady()){
//            params.getReader().raiseNotReady(params.getOnGetResultListener());
//            return false;
//        }
        params.getReader().getReader().disconnectReader();
        return true;
    }
}
