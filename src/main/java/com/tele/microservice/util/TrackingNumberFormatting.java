package com.tele.microservice.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public final class TrackingNumberFormatting {

    /**
     * Tracking Number standard format.
     * Composition:
     * origin (2) + base36(date) (5) + base36(zero_padded_sequence) (7)
     *
     * @param date
     * @param origin
     * @param seq
     * @return String
     */
    public static String formatWithBusinessLogic(LocalDate date, String origin, long seq) {
        String yyyyMMdd   = date.format(DateTimeFormatter.BASIC_ISO_DATE);
        long   dateNum    = Long.parseLong(yyyyMMdd);
        String base36Date = Base36.encode(dateNum);

        String base36Seq  = Base36.encode(seq);
        String paddedSeq  = String.format("%7s", base36Seq).replace(' ', '0');

        return origin + base36Date + paddedSeq;
    }

}
