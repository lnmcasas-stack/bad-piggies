package com.example.pigbuilder.net;

import com.example.pigbuilder.client.ClientGameEvents;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Only ever invoked on the client. */
public final class ClientPayloadHandler {
    private ClientPayloadHandler() {}

    public static void handle(ModePayload payload, IPayloadContext ctx) {
        ClientGameEvents.setMode(payload.on());
    }
}
