package sk.kamil.morley.plc;


public class RwParams {

    private int dbBolockNumber = 0;
    private int start = 0;
    private int end = 0;
    private int bit = 0;
    private ReadWriteModeEnum readWriteMode;
    private DataTypeEnum dataType;
    private Object writeData;


    public int getDbBolockNumber() {
        return dbBolockNumber;
    }

    public void setDbBolockNumber(int dbBolockNumber) {
        this.dbBolockNumber = dbBolockNumber;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    public int getBit() {
        return bit;
    }

    public void setBit(int bit) {
        this.bit = bit;
    }

    public ReadWriteModeEnum getReadWriteMode() {
        return readWriteMode;
    }



    public void setReadWriteMode(ReadWriteModeEnum readWriteMode) {
        this.readWriteMode = readWriteMode;
    }

    public DataTypeEnum getDataType() {
        return dataType;
    }

    public void setDataType(DataTypeEnum dataType) {
        this.dataType = dataType;
    }


    public Object getWriteData() {
        return writeData;
    }

    public void setWriteData(Object writeData) {
        this.writeData = writeData;
    }

    @Override
    public String toString() {
        return "RwParams{" +
                "dbBolockNumber=" + dbBolockNumber +
                ", start=" + start +
                ", end=" + end +
                ", bit=" + bit +
                ", readWriteMode=" + readWriteMode +
                ", dataType=" + dataType +
                ", writeData=" + writeData +
                '}';
    }
}
