package ACS.apdu.command;

import com.acs.smartcard.Reader;
import ACS.NFCReader;
import ACS.apdu.Result;
import ACS.task.TaskListener;
import ACS.params.BaseParams;
import ACS.reader.ACRReaderException;
import ACS.reader.OnDataListener;

/**
 * Created by kevin on 5/27/15.
 */
public class Reset extends Base<BaseParams> implements OnDataListener {

    private NFCReader reader;

    public Reset(BaseParams params) {
        super(params);
    }

    private boolean sendPlugin = true;

    public boolean isSendPlugin() {
        return sendPlugin;
    }

    public void setSendPlugin(boolean sendPlugin) {
        this.sendPlugin = sendPlugin;
    }

    public String toDataString(Result result) {
        return result.getMeta().getUid();
    }

    public boolean run(TaskListener listener) {
        super.run(listener);
        reader = getParams().getReader();
        try {
            reader.getReader().power(getParams().getSlotNumber(), Reader.CARD_WARM_RESET, this);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    @Override
    public boolean onData(byte[] bytes, int len) {
        Result result = Result.buildSuccessInstance("Reset");
        result.setData(bytes);
        reader.getChipMeta().parseATR(bytes);
        reader.getReader().setProtocol(this.getParams().getSlotNumber(), Reader.PROTOCOL_T0 | Reader.PROTOCOL_T1);
        result.setSendPlugin(isSendPlugin());
        if (getParams().getOnGetResultListener() != null) {
            getParams().getOnGetResultListener().onResult(result);
        }
        runTaskListener(result.isSuccess());
        return result.isSuccess();
    }

    @Override
    public boolean onError(ACRReaderException e) {
        Result result = new Result("Reset", e);
        result.setSendPlugin(isSendPlugin());
        if (getParams().getOnGetResultListener() != null) {
            getParams().getOnGetResultListener().onResult(result);
        }
        return false;
    }
}