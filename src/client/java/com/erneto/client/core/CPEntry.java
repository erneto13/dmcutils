package com.erneto.client.core;

public record CPEntry(String user, int x, int y, int z, String world, String timeLabel) {

    public boolean isNegativeCoord() {
        return y < 0;
    }
}