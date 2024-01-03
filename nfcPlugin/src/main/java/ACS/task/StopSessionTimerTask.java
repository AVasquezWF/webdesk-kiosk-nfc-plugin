package acs.task;

import android.util.Log;

import acs.NFCReader;
import acs.Util;
import acs.apdu.OnGetResultListener;
import acs.apdu.Result;
import acs.apdu.command.card.StopSession;

import acs.params.InitNTAGParams;

import java.util.TimerTask;

/**
 * Created by kevin on 9/9/15.
 */
public class StopSessionTimerTask extends TimerTask {
    private NFCReader reader;
    private final String TAG = "StopSessionTimerTask";

    public StopSessionTimerTask(NFCReader reader) {
        this.reader = reader;
    }

    @Override
    public void run() {
//        Log.d(TAG, "Check running session");
        if (this.reader.getSessionStartedAt() > 0 &&  this.reader.getSessionStartedAt() < System.currentTimeMillis() - (1000 * 6)) {
            Log.w(TAG, "Found running session");
            InitNTAGParams params = new InitNTAGParams(0);
            params.setReader(reader);
            params.setOnGetResultListener(new OnGetResultListener() {
                @Override
                public void onResult(Result result) {
                    Log.d(TAG, "==========" + result.getCommand() + "==========");
                    Log.d(TAG, "Code: " + result.getCodeString());
                    if (result.getData() != null) {
                        Log.d(TAG, "Data: " + Util.ByteArrayToHexString(result.getData()));
                    }
                    Log.d(TAG, "====================");
                }
            });
            StopSession stopSession = new StopSession(params);
            stopSession.run();
        }
    }
}
