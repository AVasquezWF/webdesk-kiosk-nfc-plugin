package acs.task;

import android.os.AsyncTask;
import acs.apdu.command.UID;
import acs.params.BaseParams;

/**
 * Created by kevin on 6/2/15.
 */
public class UIDTask extends AsyncTask<BaseParams, Void, Boolean> {

    final private String TAG = "UIDTask";

    @Override
    protected Boolean doInBackground(BaseParams... paramses) {
        BaseParams params = paramses[0];
        if (params == null) {
            return false;
        }
        if(!params.getReader().isReady()){
            params.getReader().raiseNotReady(params.getOnGetResultListener());
        }
        UID uid = new UID(params);
        return uid.run();

    }

}


