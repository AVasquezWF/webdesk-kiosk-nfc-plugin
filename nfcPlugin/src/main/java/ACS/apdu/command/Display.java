package acs.apdu.command;

import acs.Util;
import acs.apdu.Result;
import acs.task.TaskListener;
import acs.params.DisplayParams;
import acs.reader.ACRReader;
import acs.reader.ACRReaderException;
import acs.reader.OnDataListener;

/**
 * Created by kevin on 5/27/15.
 */
public class Display extends Base<DisplayParams> implements OnDataListener{

    private static final String TAG = "Display" ;

    public Display(DisplayParams params) {
        super(params);
    }

    public boolean run(TaskListener listener) {
//       FF 00 68 00 02 31 32
        byte[] sendBuffer = new byte[]{(byte) 0xFF, this.getParams().getOption(), (byte) 0x68, this.getParams().getXY(),
                (byte) 0x0F,//length
                (byte) 0x00, (byte) 0x0, (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x0, (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x0, (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x0, (byte) 0x00, (byte) 0x00
        };
            byte []  msg = Util.toNFCByte(this.getParams().getMessage(),16);

            System.arraycopy(msg, 0, sendBuffer, 5, 16);
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
