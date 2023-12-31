package acs.params;

/**
 * Created by kevin on 6/8/15.
 */
public class ReadParams extends Params {
    private int slotNumber = 0;
    private int block = 4;
    private String password = "";

    public ReadParams(int slotNumber,int block) {
        this.slotNumber = slotNumber;
        this.block = block;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getBlock() {
        return block;
    }

    public void setBlock(int block) {
        this.block = block;
    }

    public int getSlotNumber() {
        return slotNumber;
    }

    public void setSlotNumber(int slotNumber) {
        this.slotNumber = slotNumber;
    }

}
