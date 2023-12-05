package ACS.task;

import android.os.AsyncTask;
import android.util.Log;
import ACS.operate.DisconnectReader;
import ACS.params.BaseParams;
import ACS.params.DisconnectParams;

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
