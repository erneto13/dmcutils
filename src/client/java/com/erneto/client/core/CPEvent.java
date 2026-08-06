package com.erneto.client.core;

public sealed interface CPEvent permits CPEvent.BlockEvent, CPEvent.ContainerEvent,
        CPEvent.MessageEvent, CPEvent.SimpleEvent {

    long time();

    String user();

    record BlockEvent(long time, String selector, String user, String target, int amount,
                      int x, int y, int z, String world,
                      boolean rolledback, boolean isContainer, boolean added) implements CPEvent {
    }

    // Type 2 — carries coordinates, this is the one the heatmap/overlay relies on
    record ContainerEvent(long time, String selector, String user, int amount,
                          int x, int y, int z, String world) implements CPEvent {
    }

    record MessageEvent(long time, String user, String message, boolean sign) implements CPEvent {
    }

    record SimpleEvent(long time, String user, String target) implements CPEvent {
    }
}