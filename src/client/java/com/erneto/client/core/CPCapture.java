package com.erneto.client.core;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CPCapture {

    private static final Pattern ENTRY_PATTERN = Pattern.compile(
            "Hace\\s+[\\d.]+/[mhds]+\\s+[+\\-]\\s+([a-zA-Z0-9_]{1,16})\\s+reco"
    );
    private static final Pattern COORD_PATTERN = Pattern.compile(
            "\\(x(-?\\d+)/y(-?\\d+)/z(-?\\d+)/([^)]+)\\)"
    );
    private static final Pattern TIME_PATTERN = Pattern.compile(
            "Hace\\s+([\\d.]+/[mhds]+)"
    );
    private static final Pattern PAGE_PATTERN = Pattern.compile(
            "P[aá]gina\\s+(\\d+)/(\\d+)"
    );

    private int targetPages = 10;

    private boolean recording = false;
    private int currentPage = 0;

    private final List<CPEntry> entries = new ArrayList<>();

    private String pendingUser = null;
    private String pendingTime = null;

    public int getTargetPages() { return targetPages; }

    public void setTargetPages(int pages) {
        this.targetPages = Math.max(1, pages);
    }

    public void startRecording() {
        recording = true;
        currentPage = 0;
        entries.clear();
        pendingUser = null;
        pendingTime = null;
    }

    public void startRecording(int pages) {
        setTargetPages(pages);
        startRecording();
    }

    public void stopRecording() {
        recording = false;
        pendingUser = null;
        pendingTime = null;
    }

    public boolean isRecording() { return recording; }

    public int getCurrentPage() { return currentPage; }

    public List<CPEntry> getEntries() { return List.copyOf(entries); }

    public boolean feedLine(String raw) {
        if (!recording) return false;

        String clean = raw.replaceAll("§[0-9a-fk-or]", "").trim();

        if (pendingUser != null) {
            Matcher coord = COORD_PATTERN.matcher(clean);
            if (coord.find()) {
                int x = Integer.parseInt(coord.group(1));
                int y = Integer.parseInt(coord.group(2));
                int z = Integer.parseInt(coord.group(3));
                String world = coord.group(4);
                entries.add(new CPEntry(pendingUser, x, y, z, world, pendingTime));
            }
            pendingUser = null;
            pendingTime = null;
        }

        Matcher entry = ENTRY_PATTERN.matcher(clean);
        if (entry.find()) {
            pendingUser = entry.group(1);
            Matcher time = TIME_PATTERN.matcher(clean);
            pendingTime = time.find() ? time.group(1) : "?";
            return false;
        }

        Matcher page = PAGE_PATTERN.matcher(clean);
        if (page.find()) {
            currentPage = Integer.parseInt(page.group(1));
            int total = Integer.parseInt(page.group(2));

            if (currentPage >= targetPages || currentPage >= total) {
                stopRecording();
                return true;
            }
        }

        return false;
    }
}