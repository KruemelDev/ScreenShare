package com.kruemel.screenshare.client;

public class Util {

    public static String floatToPercentage(float value) {
        int percentage = Math.round(value * 100);
        return percentage + "%";
    }

}
