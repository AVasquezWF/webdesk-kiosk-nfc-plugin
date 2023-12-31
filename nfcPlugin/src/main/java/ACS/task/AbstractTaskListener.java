package acs.task;

import acs.apdu.Result;
import acs.apdu.command.card.StopSession;

/**
 * Created by kevin on 16/3/22.
 */
abstract class AbstractTaskListener implements TaskListener{

    final StopSession stopSession;

    public AbstractTaskListener(StopSession stopSession) {
        this.stopSession = stopSession;
    }

    public StopSession getStopSession() {
        return stopSession;
    }

    @Override
    public void onFailure() {
        stopSession.run();
    }

    @Override
    public void onException() {
        stopSession.run();
    }
}
