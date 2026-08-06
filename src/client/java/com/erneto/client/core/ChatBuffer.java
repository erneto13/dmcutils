package com.erneto.client.core;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//Keeps a rolling in-memory chat log used by the search screen and the spam detector
public final class ChatBuffer {

    private record LastSeen(String content, long timestamp, int repeats) {
    }

    private static final int MAX_LINES = 500;
    private static final long SPAM_WINDOW_MS = 8000;

    private static final Deque<ChatLine> LINES = new ArrayDeque<>();
    private static final Map<String, LastSeen> LAST_BY_SENDER = new HashMap<>();

    private ChatBuffer() {
    }

    public static void add(ChatLine line) {
        LINES.addLast(line);
        if (LINES.size() > MAX_LINES) LINES.removeFirst();
    }

    public static List<ChatLine> all() {
        return new ArrayList<>(LINES);
    }

    public static List<ChatLine> search(String query) {
        String q = query.toLowerCase();
        List<ChatLine> out = new ArrayList<>();
        for (ChatLine l : LINES) {
            if (l.content().toLowerCase().contains(q) || l.sender().toLowerCase().contains(q)) {
                out.add(l);
            }
        }
        return out;
    }

    //Tracks identical consecutive messages from the same sender within a time window
    public static int trackRepeat(String sender, String content) {
        long now = System.currentTimeMillis();
        LastSeen prev = LAST_BY_SENDER.get(sender);

        int repeats = (prev != null
                && prev.content().equals(content)
                && (now - prev.timestamp()) <= SPAM_WINDOW_MS)
                ? prev.repeats() + 1
                : 1;

        LAST_BY_SENDER.put(sender, new LastSeen(content, now, repeats));
        return repeats;
    }

    public static void clear() {
        LINES.clear();
        LAST_BY_SENDER.clear();
    }
}
