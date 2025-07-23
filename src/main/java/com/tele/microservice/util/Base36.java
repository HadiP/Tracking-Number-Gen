package com.tele.microservice.util;

public final class Base36 {

    public static String encode(long val){
        return Long.toString(val, 36);
    }

    public static String encode(String val){
        long parsedVal = Long.parseLong(val);
        return encode(parsedVal);
    }

    public static long decode(String val){
        return Long.parseLong(val, 10);
    }

}
