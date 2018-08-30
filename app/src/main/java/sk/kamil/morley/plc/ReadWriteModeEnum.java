package sk.kamil.morley.plc;

import sk.kamil.morley.mokka7.S7;

public enum  ReadWriteModeEnum {

    PROCESS_INPUTS("Process Inputs", S7.S7AreaPE),
    PROCESS_OUTPUTS("Process Outputs", S7.S7AreaPA),
    MERKERS("Merkers", S7.S7AreaMK),
    DB("DB", S7.S7AreaDB),
    COUNTERS("Counters", S7.S7AreaDB),
    TIMERS("Timers", S7.S7AreaTM);

    private String name;
    private int value;

    ReadWriteModeEnum(String name, int value) {
        this.name = name;
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
