package ACS.task;

import android.os.AsyncTask;
import ACS.apdu.command.GetReceivedData;
import ACS.params.BaseParams;

/**
 * Created by kevin on 16/2/26.
 */
public class GetReceivedDataTask extends AsyncTask<BaseParams, Void, Boolean> {
    @Override
    protected Boolean doInBackground(BaseParams... paramses) {
        BaseParams params = paramses[0];
        if (params == null) {
            return false;
        }
        if(!params.getReader().isReady()){
            params.getReader().raiseNotReady(params.getOnGetResultListener());
        }

        GetReceivedData getReceivedData = new GetReceivedData(params);
        return getReceivedData.run();
    }
}
