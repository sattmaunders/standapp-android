package com.standapp.backend;

/**
 * Created by SINTAJ2 on 2/11/2015.
 */
public enum SenderId {

    PHONE("phone"),
    CHROME_EXT("chrome");

    private final String name;

    private SenderId(String s) {
        name = s;
    }

    public boolean equalsName(String otherName) {
        return (otherName == null) ? false : name.equals(otherName);
    }

    public String toString() {
        return name;
    }
}
