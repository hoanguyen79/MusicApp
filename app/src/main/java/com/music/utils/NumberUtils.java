package com.music.utils;

import androidx.annotation.NonNull;

import java.util.Map;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.TreeMap;

public final class NumberUtils {
    private static final NavigableMap<Long, String> suffixes = new TreeMap<>();

    static {
        suffixes.put(1_000L, "k");
        suffixes.put(1_000_000L, "M");
        suffixes.put(1_000_000_000L, "G");
        suffixes.put(1_000_000_000_000L, "T");
        suffixes.put(1_000_000_000_000_000L, "P");
        suffixes.put(1_000_000_000_000_000_000L, "E");
    }

    @NonNull
    public static String formatWithSuffix(long number) {
        if (number == Long.MIN_VALUE) {
            return formatWithSuffix(Long.MIN_VALUE + 1);
        }

        if (number < 0) {
            return "-" + formatWithSuffix(-number);
        }

        if (number < 1000) {
            return Long.toString(number);
        }

        Map.Entry<Long, String> e = Objects.requireNonNull(suffixes.floorEntry(number));
        Long divideBy = e.getKey();
        String suffix = e.getValue();

        long truncated = number / (divideBy / 10);
        boolean hasDecimal = truncated < 100 && (truncated / 10d) != (truncated / 10d);

        return hasDecimal ? (truncated / 10d) + suffix : (truncated / 10) + suffix;
    }
}
