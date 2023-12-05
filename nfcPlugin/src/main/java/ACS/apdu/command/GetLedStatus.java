package ACS.apdu.command;

import ACS.Util;
import ACS.apdu.Result;
import ACS.task.TaskListener;
import ACS.params.BaseParams;
import ACS.reader.ACRReader;
import ACS.reader.ACRReaderException;
import ACS.reader.OnDataListener;

/**
 * Created by Kevin on 16/3/1.
 */
public class GetLedStatus extends Base<BaseParams> implements OnDataListener {

    private static final String TAG = "GetLedStatus";

    private boolean sendPlugin = true;

    public boolean isSendPlugin() {
        return sendPlugin;
    }

    public void setSendPlugin(boolean sendPlugin) {
        this.sendPlugin = sendPlugin;
    }

    public GetLedStatus(BaseParams params) {
        super(params);
    }

    public String toDataString(Result result) {
        byte[] data = new byte[1];
        System.arraycopy(result.getData().clone(), 5, data, 0, 1);
        return Util.toHexString(data);
    }

    public boolean run(TaskListener listener) {
        byte[] sendBuffer = new byte[]{(byte) 0xE0, (byte) 0x00, (byte) 0x00, (byte) 0x29, (byte) 0x00};

        ACRReader acrReader = this.getParams().getReader().getReader();
        acrReader.control(0, sendBuffer, this);
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
