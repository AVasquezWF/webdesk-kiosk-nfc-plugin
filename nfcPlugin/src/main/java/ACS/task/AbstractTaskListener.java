package ACS.task;

import ACS.apdu.Result;
import ACS.apdu.command.card.StopSession;

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
