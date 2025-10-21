package com.innowise.JavaCore.Skynet;

public enum Parts {
    HEAD(0),
    TORSO(1),
    HAND(2),
    FOOT(3);

    private final int code;

    Parts(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static Parts getName(int id) {
        for (Parts p : Parts.values()) {
            if (p.getCode() == id) return p;
        }
        return FOOT;
    }
}
