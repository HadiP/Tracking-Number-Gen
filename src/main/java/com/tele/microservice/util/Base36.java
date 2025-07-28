package com.tele.microservice.util;

public final class Base36 {

    /**
     * Change decimal based number into base 36 (0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ)
     * @param val long
     * @return base 36
     */
    public static String encode(long val){
        return Long.toString(val, 36).toUpperCase();
    }

    /**
     * Change decimal based number (String) into base 36
     * @param val String
     * @return base 36
     */
    public static String encode(String val){
        long parsedVal = Long.parseLong(val);
        return encode(parsedVal);
    }

    /**
     * Change base 36 into decimal
     * @param val String
     * @return decimal
     */
    public static long decode(String val){
        return Long.parseLong(val, 36);
    }

}
