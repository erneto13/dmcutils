package com.erneto.client.core;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class CPTimelineStore {

    private final Map<String, List<CPEvent>> byPlayer = new ConcurrentHashMap<>();
    private final List<CPEvent.ContainerEvent> locatable = Collections.synchronizedList(new ArrayList<>());

    public void add(CPEvent event) {
        byPlayer.computeIfAbsent(event.user().toLowerCase(Locale.ROOT), k -> new ArrayList<>()).add(event);
        if (event instanceof CPEvent.ContainerEvent ce) {
            locatable.add(ce);
        }
    }

    public List<CPEvent> timelineFor(String user) {
        List<CPEvent> list = byPlayer.getOrDefault(user.toLowerCase(Locale.ROOT), List.of());
        return list.stream()
                .sorted(Comparator.comparingLong(CPEvent::time).reversed())
                .toList();
    }

    public List<CPEvent.ContainerEvent> allLocatable() {
        return List.copyOf(locatable);
    }

    public void clear() {
        byPlayer.clear();
        locatable.clear();
    }
}