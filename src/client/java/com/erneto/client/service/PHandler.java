package com.erneto.client.service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PHandler {
    private static final Pattern PATTERN = Pattern.compile(
            "El usuario\\s+([a-zA-Z0-9_]{3,16})\\s+fue\\s+(advertido|baneado de ip|baneado|expulsado|muteado)",
            Pattern.CASE_INSENSITIVE
    );
    private static final Pattern STAFF_PATTERN = Pattern.compile(
            "Staff:\\s+([a-zA-Z0-9_]{3,16})"
    );

    public record PData(String user, String type, String staff) {}

    public static String resolveType(String raw) {
        return switch (raw.toLowerCase()) {
            case "advertido"     -> "advertencia";
            case "baneado de ip" -> "banip";
            case "baneado"       -> "ban";
            case "expulsado"     -> "expulsion";
            case "muteado"       -> "mute";
            default              -> raw;
        };
    }

    public static PData parse(String message) {
        String clean = message.replace("\n", " ").trim();
        Matcher sancionMatcher = PATTERN.matcher(clean);
        Matcher staffMatcher = STAFF_PATTERN.matcher(clean);

        if (sancionMatcher.find() && staffMatcher.find()) {
            return new PData(
                    sancionMatcher.group(1),
                    resolveType(sancionMatcher.group(2)),
                    staffMatcher.group(1)
            );
        }
        return null;
    }
}
