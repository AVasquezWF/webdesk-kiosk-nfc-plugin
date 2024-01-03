package acs.apdu.command;

import android.util.Log;

import acs.Util;
import acs.apdu.Result;
import acs.task.TaskListener;
import acs.params.AuthParams;
import acs.reader.ACRReader;
import acs.reader.ACRReaderException;

/**
 * Created by kevin on 5/27/15.
 */
public class LoadAuthentication extends Base<AuthParams> {
    private static final String TAG = "LoadAuthentication";

    public LoadAuthentication(AuthParams params) {
        super(params);
    }


    public boolean run(TaskListener listener) {
//        FF 82 00 00 06 FF FF FF FF FF FF
        byte[] sendBuffer = new byte[]{(byte) 0xFF, (byte) 0x82, (byte) 0x0, (byte) 0x0, (byte) 0x06,
                (byte) 0x0, (byte) 0x0, (byte) 0x0, (byte) 0x0, (byte) 0x0, (byte) 0x0
        };
        String pwdString = this.getParams().isA() ? this.getParams().getKeyA() : this.getParams().getKeyB();
        byte[] pwd = Util.convertHexAsciiToByteArray(pwdString, 6);
        System.arraycopy(pwd, 0, sendBuffer, 5, 6);
        byte[] receiveBuffer = new byte[16];
        Log.d(TAG, Util.toHexString(sendBuffer));
        ACRReader acrReader = this.getParams().getReader().getReader();
        Result result;
        int byteCount;
        try {
            byteCount = acrReader.transmit(this.getParams().getSlotNumber(), sendBuffer, sendBuffer.length, receiveBuffer, receiveBuffer.length);
            result = new Result("LoadAuthentication", byteCount, receiveBuffer);
        } catch (ACRReaderException e) {
            result = new Result("LoadAuthentication", e);
            e.printStackTrace();
        }
        if (this.getParams().getOnGetResultListener() != null) {
            this.getParams().getOnGetResultListener().onResult(result);
        }
        return result.isSuccess();
    }

}
