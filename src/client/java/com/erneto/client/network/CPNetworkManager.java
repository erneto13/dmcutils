package com.erneto.client.network;

import com.erneto.client.core.CPEvent;
import com.erneto.client.core.CPEventParser;
import com.erneto.client.core.CPTimelineStore;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;

public final class CPNetworkManager {

    private static final String MOD_VERSION = "1.0.0";
    private static final String MOD_ID = "dmcutils";
    private static final int PROTOCOL = 1;

    private final CPTimelineStore store;
    private boolean registered = false;

    public CPNetworkManager(CPTimelineStore store) {
        this.store = store;
    }

    public boolean isRegistered() {
        return registered;
    }

    public void init() {
        PayloadTypeRegistry.playC2S().register(CPPayloads.HandshakeC2S.ID, CPPayloads.HandshakeC2S.CODEC);
        PayloadTypeRegistry.playS2C().register(CPPayloads.HandshakeS2C.ID, CPPayloads.HandshakeS2C.CODEC);
        PayloadTypeRegistry.playS2C().register(CPPayloads.DataS2C.ID, CPPayloads.DataS2C.CODEC);

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            registered = false;
            if (ClientPlayNetworking.canSend(CPPayloads.HandshakeC2S.ID)) {
                sender.sendPacket(new CPPayloads.HandshakeC2S(MOD_VERSION, MOD_ID, PROTOCOL));
            }
        });

        ClientPlayNetworking.registerGlobalReceiver(CPPayloads.HandshakeS2C.ID, (payload, context) -> {
            registered = payload.registered();
        });

        ClientPlayNetworking.registerGlobalReceiver(CPPayloads.DataS2C.ID, (payload, context) ->
                context.client().execute(() -> {
                    try {
                        CPEvent event = CPEventParser.parse(payload.type(), payload.raw());
                        store.add(event);
                    } catch (Exception e) {
                    }
                }));
    }
}