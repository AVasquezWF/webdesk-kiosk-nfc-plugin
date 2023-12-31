package acs.apdu.command.card;

import acs.Util;
import acs.task.TaskListener;
import acs.params.InitNTAGParams;

/**
 * Created by kevin on 5/27/15.
 */
public class NTagAuth extends CardCommand {
    public NTagAuth(InitNTAGParams params) {
        super(params);
    }

    private byte[] sendBuffer = new byte[]{(byte) 0xFF, (byte) 0x0, (byte) 0x0, (byte) 0x0, (byte) 0x05,
            (byte) 0x1B, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF};


    public boolean initOldPassword() {
        if (this.getParams().getOldPassword() != null && !"".equals(this.getParams().getOldPassword())) // use custom password
        {
            byte[] pwd = Util.convertHexAsciiToByteArray(this.getParams().getOldPassword(), 4);
            System.arraycopy(pwd, 0, sendBuffer, 6, 4);
            return true;
        }
        return false;
    }

    public void initPassword() {
        if (this.getParams().getPassword() != null && !"".equals(this.getParams().getPassword())) // use custom password
        {

            byte[] pwd = Util.convertHexAsciiToByteArray(this.getParams().getPassword(), 4);
            System.arraycopy(pwd, 0, sendBuffer, 6, 4);
        }
    }

    public boolean run(TaskListener listener) {
        super.run(listener);
        return transmit(sendBuffer);
    }

    @Override
    protected String getTag() {
        return "NTagAuth";
    }

    @Override
    protected String getCommandName() {
        return "NTagAuth";
    }

}
