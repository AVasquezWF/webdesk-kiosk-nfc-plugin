package acs.task;

import android.os.AsyncTask;

import acs.apdu.command.Beep;
import acs.apdu.command.UpdateBinaryBlock;
import acs.apdu.command.card.StopSession;
import acs.params.BaseParams;
import acs.params.WriteParams;

/**
 * Created by kevin on 6/2/15.
 */
public class WriteTask extends AsyncTask<WriteParams, Void, Boolean> {
    final private String TAG = "UIDTask";

    @Override
    protected Boolean doInBackground(WriteParams... paramses) {
        WriteParams params = paramses[0];
        if (params == null) {
            return false;
        }
        if(!params.getReader().isReady()){
            params.getReader().raiseNotReady(params.getOnGetResultListener());
            return false;
        }
        final UpdateBinaryBlock update = new UpdateBinaryBlock(params);

        if (params.getReader().getChipMeta().needAuthentication()) {
            TaskWithPassword task = new TaskWithPassword("UpdateBinaryBlock",
                    params.getReader(),
                    params.getSlotNumber(),
                    params.getPassword()
            );
            task.setGetResultListener(params.getOnGetResultListener());
            task.setCallback(new TaskWithPassword.TaskCallback() {
                @Override
                public boolean run(TaskListener taskListener, StopSession stopSession) {
                    update.setStopSession(stopSession);
                    return update.run(taskListener);
                }
            });
            task.run();
        } else {
            BaseParams baseParams = new BaseParams(params.getSlotNumber());
            baseParams.setReader(params.getReader());
            final Beep beep = new Beep(baseParams);
            TaskListener readListener = new TaskListener() {
                @Override
                public void onSuccess() {
                    beep.run();
                }

                @Override
                public void onFailure() {
                    beep.run();
                }

                @Override
                public void onException() {

                }
            };
            return update.run(readListener);
        }
        return false;
    }
}
