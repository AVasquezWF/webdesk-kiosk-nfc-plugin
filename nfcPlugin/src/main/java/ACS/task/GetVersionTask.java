package ACS.task;

import android.os.AsyncTask;

import ACS.apdu.command.GetVersion;
import ACS.params.BaseParams;

/**
 * Created by kevin on 6/2/15.
 */
public class GetVersionTask extends AsyncTask<BaseParams, Void, Boolean> {

    final private String TAG = "GetVersionTask";


    @Override
    protected Boolean doInBackground(BaseParams... paramses) {
        BaseParams params = paramses[0];
        if (params == null) {
            return false;
        }
        if(!params.getReader().isReady()){
            params.getReader().raiseNotReady(params.getOnGetResultListener());
            return false;
        }
        GetVersion getVersion = new GetVersion(params);
        return getVersion.run();
    }

}


