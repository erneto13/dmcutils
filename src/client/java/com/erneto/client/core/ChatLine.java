package com.erneto.client.core;

public record ChatLine(long timestamp, String sender, String content, net.minecraft.text.Text text) {
}
