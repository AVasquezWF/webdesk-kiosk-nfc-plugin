package acs.apdu.command;

import android.util.Log;

import acs.Util;
import acs.apdu.Result;
import acs.task.TaskListener;
import acs.params.ClearLCDParams;
import acs.reader.ACRReader;
import acs.reader.ACRReaderException;
import acs.reader.OnDataListener;

/**
 * Created by kevin on 5/27/15.
 */
public class ClearCLD extends Base<ClearLCDParams> implements OnDataListener {

    private static final String TAG = "ClearCLD";

    public ClearCLD(ClearLCDParams params) {
        super(params);
    }

    public boolean run(TaskListener listener) {
//       FF 00 60 00 00
        byte[] sendBuffer = new byte[]{(byte) 0xFF, (byte) 0x00, (byte) 0x60, (byte) 0x00, (byte) 0x00};
        byte[] receiveBuffer = new byte[16];
        Log.d(TAG, Util.toHexString(sendBuffer));
        ACRReader acrReader = this.getParams().getReader().getReader();
        acrReader.control(0, sendBuffer, this);
//        try {
//            int byteCount = acrReader.control(0, Reader.IOCTL_CCID_ESCAPE, sendBuffer, sendBuffer.length, receiveBuffer, receiveBuffer.length);
//            result = new Result("ClearCLD", byteCount, receiveBuffer);
//        } catch (ACRReaderException e) {
//            result = new Result("ClearCLD", e);
//            e.printStackTrace();
//        }
//
//        if (this.getParams().getOnGetResultListener() != null) {
//            this.getParams().getOnGetResultListener().onResult(result);
//        }
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
