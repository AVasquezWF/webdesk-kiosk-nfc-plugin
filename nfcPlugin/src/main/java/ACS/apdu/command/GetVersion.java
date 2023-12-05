package ACS.apdu.command;

import android.util.Log;

import com.acs.smartcard.ReaderException;
import ACS.ATRHistorical;
import ACS.Chip;
import ACS.NFCReader;
import ACS.Util;
import ACS.apdu.Result;
import ACS.task.TaskListener;
import ACS.params.BaseParams;
import ACS.reader.ACRReaderException;
import ACS.reader.OnDataListener;

/**
 * Created by kevin on 5/27/15.
 */
public class GetVersion extends Base<BaseParams> implements OnDataListener {
    private static final String TAG = "GetVersion";

    private boolean sendPlugin = true;

    public boolean isSendPlugin() {
        return sendPlugin;
    }

    public void setSendPlugin(boolean sendPlugin) {
        this.sendPlugin = sendPlugin;
    }

    private NFCReader nfcReader;

    public GetVersion(BaseParams params) {
        super(params);
    }

    public String toDataString(Result result) {
        Chip chip = Chip.find(result.getData());
        if (chip != null) {
            return chip.getName();
        } else {
            return "UNKNOWN";
        }
    }

    public boolean run(TaskListener listener) {
        super.run(listener);
        byte[] sendBuffer = new byte[]{(byte) 0xFF, (byte) 0x0, (byte) 0x0, (byte) 0x0, (byte) 0x01, (byte) 0x60};
        Log.d(TAG, Util.toHexString(sendBuffer));
        nfcReader = this.getParams().getReader();
        nfcReader.getReader().transmit(0, sendBuffer, this);
        return true;
    }

    @Override
    public boolean onData(byte[] bytes, int len) {
        Result result = new Result(TAG, len, bytes);
        if (nfcReader.getChipMeta().canGetVersion()) {
            if (result.isSuccess()) {
                Chip chip = Chip.find(result.getData());
                if (chip != null) {
                    nfcReader.getChipMeta().setName(chip.getName());
                    nfcReader.getChipMeta().setType(chip.getType());
                } else {
                    nfcReader.getChipMeta().setName(ATRHistorical.UNKNOWN);
                    nfcReader.getChipMeta().setType(ATRHistorical.MIFARE_ULTRALIGHT_C);
                }
            } else {
                nfcReader.getChipMeta().setName(ATRHistorical.UNKNOWN);
                nfcReader.getChipMeta().setType(ATRHistorical.MIFARE_ULTRALIGHT);
                Util.sleep(800);
            }
        } else {
            result = new Result("GetVersion", new ReaderException("the chip does not support"));
        }
        result.setSendPlugin(this.isSendPlugin());
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
