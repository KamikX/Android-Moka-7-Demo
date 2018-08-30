package sk.kamil.morley.plc;


public class ConnectionParams {

    private String ipAddress = "";
    private int rack = 0; // Default 0 for S7300
    private int slot = 2; // Default 2 for S7300
    private byte connectionType;


    public ConnectionParams() {
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public int getRack() {
        return rack;
    }

    public void setRack(int rack) {
        this.rack = rack;
    }

    public int getSlot() {
        return slot;
    }

    public void setSlot(int slot) {
        this.slot = slot;
    }

    public byte getConnectionType() {
        return connectionType;
    }

    public void setConnectionType(byte connectionType) {
        this.connectionType = connectionType;
    }

    @Override
    public String toString() {
        return "ConnectionParams{" +
                "ipAddress='" + ipAddress + '\'' +
                ", rack=" + rack +
                ", slot=" + slot +
                ", connectionType=" + connectionType +
                '}';
    }
}
