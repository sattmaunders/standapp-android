package com.standapp.backend;

/**
 * Created by SINTAJ2 on 2/11/2015.
 */
public enum StandAppMessages {

    BREAK_START("break.start"),
    BREAK_END("break.end");

    private final String name;

    private StandAppMessages(String s) {
        name = s;
    }

    public boolean equalsName(String otherName) {
        return (otherName == null) ? false : name.equals(otherName);
    }

    public String toString() {
        return name;
    }
}
