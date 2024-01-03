package acs.apdu.command.card;

import android.util.Log;

import acs.Util;
import acs.apdu.Result;
import acs.apdu.command.Base;
import acs.params.InitNTAGParams;
import acs.reader.ACRReader;
import acs.reader.ACRReaderException;
import acs.reader.OnDataListener;

/**
 * Created by kevin on 5/27/15.
 */
public abstract class CardCommand extends Base<InitNTAGParams> implements OnDataListener{

    public CardCommand(InitNTAGParams params) {
        super(params);
    }

    protected boolean transmit(byte[] sendBuffer, OnDataListener listener){
        Log.d(getTag(), Util.toHexString(sendBuffer));
        ACRReader acrReader = this.getParams().getReader().getReader();
        acrReader.transmit(0, sendBuffer, listener);
        return true;
    }

    protected boolean transmit(byte[] sendBuffer){
        return transmit(sendBuffer,this);
    }

    protected abstract String getTag();

    protected abstract String getCommandName();

    public Result.Checker getChecker(){
        return null;
    }

    @Override
    public boolean onData(byte[] bytes, int len) {
        Result result = new Result(getCommandName(), len, bytes);
        result.setProcessor(this);
        result.setChecker(getChecker());
        if(getStopSession() == null){
            result.setSendPlugin(false);
            if (this.getParams().getOnGetResultListener() != null) {
                this.getParams().getOnGetResultListener().onResult(result);
            }
        }else{
            getStopSession().setSendResult(result);
        }
        runTaskListener(result.isSuccess());
        return result.isSuccess();
    }

    @Override
    public boolean onError(ACRReaderException e) {
        Result result = new Result(getCommandName(), e);
        if (this.getParams().getOnGetResultListener() != null) {
            this.getParams().getOnGetResultListener().onResult(result);
        }
        return result.isSuccess();
    }
}
