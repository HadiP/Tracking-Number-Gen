package com.tele.microservice.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public final class TrackingNumberFormatting {

    /**
     * Tracking Number standard format.
     * Composition:
     * base36(date) + origin + base36(seq)
     *
     * @param date
     * @param origin
     * @param seq
     * @return String
     */
    public static String formatWithBusinessLogic(LocalDate date, String origin, long seq) {
        StringBuilder builder = new StringBuilder();
        builder.append(Base36.encode(date.atStartOfDay()
                .format(DateTimeFormatter.ofPattern("yyyyMMdd"))));
        builder.append(origin);
        builder.append(Base36.encode(seq));
        return builder.toString();
    }

}
