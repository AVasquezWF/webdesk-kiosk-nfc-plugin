package acs.apdu.command;

import acs.apdu.Result;
import acs.params.PICCOperatingParameterParams;
import acs.reader.ACRReader;
import acs.reader.ACRReaderException;
import acs.reader.OnDataListener;
import acs.task.TaskListener;

public class AutoStartPolling extends Base<PICCOperatingParameterParams> implements OnDataListener {
    private static final String TAG = "AutoStartingPolling";

    public AutoStartPolling(PICCOperatingParameterParams params) {
        super(params);
    }

    public boolean run(TaskListener taskListener) {
        super.run(taskListener);
        byte[] sendBuffer = new byte[]{(byte) 0xE0, (byte) 0x00, (byte) 0x00, (byte) 0x40, (byte) 0x01};

        ACRReader reader = this.getParams().getReader().getReader();
        reader.control(0, sendBuffer, this);
        return true;
    }

    @Override
    public boolean onData(byte[] bytes, int len) {
        Result result = Result.buildSuccessInstance(TAG);
        result.setData(bytes, len);
        if (this.getParams().getOnGetResultListener() != null) {
            result.setProcessor(this);
            this.getParams().getOnGetResultListener().onResult(result);
        }
        runTaskListener(result.isSuccess());
        return result.isSuccess();
    }

    @Override
    public boolean onError(ACRReaderException e) {
        e.printStackTrace();
        Result result = new Result(TAG, e);
        if (this.getParams().getOnGetResultListener() != null) {
            this.getParams().getOnGetResultListener().onResult(result);
        }
        return false;
    }
}
