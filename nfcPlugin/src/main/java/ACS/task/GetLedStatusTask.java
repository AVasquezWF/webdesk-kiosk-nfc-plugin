package acs.task;

import android.os.AsyncTask;
import acs.apdu.command.GetLedStatus;
import acs.params.BaseParams;
/**
 * Created by Kevin on 16/3/1.
 */
public class GetLedStatusTask extends AsyncTask<BaseParams, Void, Boolean> {

    final private String TAG = "GetLedStatusTask";

    @Override
    protected Boolean doInBackground(BaseParams... paramses) {
        BaseParams params = paramses[0];
        if(params == null) {
            return false;
        }
        if(!params.getReader().isReady()) {
            params.getReader().raiseNotReady(params.getOnGetResultListener());
        }
        GetLedStatus ledStatus = new GetLedStatus(params);
        return ledStatus.run();
    }
}
