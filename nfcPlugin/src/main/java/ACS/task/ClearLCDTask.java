package acs.task;

import android.os.AsyncTask;

import acs.apdu.command.ClearCLD;
import acs.params.ClearLCDParams;

/**
 * Created by kevin on 6/2/15.
 */
public class ClearLCDTask extends AsyncTask<ClearLCDParams, Void, Boolean> {
    final private String TAG = "DisplayTask";

    @Override
    protected Boolean doInBackground(ClearLCDParams... paramses) {
        ClearLCDParams params = paramses[0];
        if (params == null) {
            return false;
        }
        if(!params.getReader().isReady()){
            params.getReader().raiseNotReady(params.getOnGetResultListener());
            return false;
        }
        ClearCLD clear = new ClearCLD(params);
        return clear.run();
    }
}
