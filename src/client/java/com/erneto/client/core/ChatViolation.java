package com.erneto.client.core;

public record ChatViolation(long timestamp, String player, String type, String detail) {
}
