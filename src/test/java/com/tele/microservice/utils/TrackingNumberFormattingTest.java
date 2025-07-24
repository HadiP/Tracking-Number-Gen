package com.tele.microservice.utils;

import static org.assertj.core.api.Assertions.*;

import com.tele.microservice.util.TrackingNumberFormatting;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

class TrackingNumberFormattingTest {

    @Test
    void formatWithBusinessLogic_generatesCorrectPattern() {
        LocalDate date = LocalDate.of(2025, 7, 25);
        String origin = "US";
        long seq = 1;

        String formatted = TrackingNumberFormatting.formatWithBusinessLogic(date, origin, seq);

        // length = 2 (origin) + 5 (base36 date) + 7 (zero-padded seq)
        assertThat(formatted).hasSize(14);
        assertThat(formatted).startsWith(origin);

        // date portion matches dynamic base-36 conversion
        String yyyyMMdd = date.format(java.time.format.DateTimeFormatter.BASIC_ISO_DATE);
        long dateNum = Long.parseLong(yyyyMMdd);
        String expectedDate36 = Long.toString(dateNum, 36).toUpperCase();
        String actualDate36 = formatted.substring(2, 2 + expectedDate36.length());
        assertThat(actualDate36).isEqualTo(expectedDate36);

        // sequence suffix is 7 characters of base-36
        String suffix = formatted.substring(formatted.length() - 7);
        assertThat(suffix).matches("[0-9A-Z]{7}");
    }
}