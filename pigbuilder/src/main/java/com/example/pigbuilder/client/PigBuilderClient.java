package com.example.pigbuilder.client;

import com.example.pigbuilder.PigBuilderMod;
import com.example.pigbuilder.registry.ModEntities;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.common.NeoForge;

/** Client-only entry point. */
@Mod(value = PigBuilderMod.MODID, dist = Dist.CLIENT)
public class PigBuilderClient {
    public PigBuilderClient(IEventBus modBus, ModContainer container) {
        modBus.addListener(PigBuilderClient::registerKeys);
        modBus.addListener(PigBuilderClient::registerRenderers);

        NeoForge.EVENT_BUS.addListener(ClientGameEvents::onTick);
        NeoForge.EVENT_BUS.addListener(ClientGameEvents::onInteract);
        NeoForge.EVENT_BUS.addListener(ClientGameEvents::onLogout);
    }

    private static void registerKeys(RegisterKeyMappingsEvent event) {
        event.registerCategory(ClientKeys.CATEGORY);
        event.register(ClientKeys.TOGGLE);
    }

    private static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.CONTRAPTION.get(), ContraptionRenderer::new);
    }
}
