package acs.task;

import android.os.AsyncTask;

import acs.apdu.command.Display;
import acs.params.DisplayParams;

/**
 * Created by kevin on 6/2/15.
 */
public class DisplayTask extends AsyncTask<DisplayParams, Void, Boolean> {
    final private String TAG = "DisplayTask";

    @Override
    protected Boolean doInBackground(DisplayParams... paramses) {
        DisplayParams params = paramses[0];
        if (params == null) {
            return false;
        }
        if(!params.getReader().isReady()){
            params.getReader().raiseNotReady(params.getOnGetResultListener());
            return false;
        }
        Display display = new Display(params);
        return display.run();
    }
}
