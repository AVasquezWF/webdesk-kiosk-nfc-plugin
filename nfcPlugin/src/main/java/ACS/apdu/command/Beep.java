package acs.apdu.command;

import acs.apdu.Result;
import acs.params.BaseParams;
import acs.reader.ACRReader;
import acs.reader.ACRReaderException;
import acs.reader.OnDataListener;
import acs.task.TaskListener;

public class Beep extends Base<BaseParams> implements OnDataListener {
    private final String TAG = "Beep";

    public Beep(BaseParams params) {
        super(params);
    }

    public boolean run() {
        byte[] sendBuffer = new byte[]{(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x28, (byte) 0x01, (byte) 0x01};

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
