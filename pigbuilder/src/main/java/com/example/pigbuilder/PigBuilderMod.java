package com.example.pigbuilder;

import com.example.pigbuilder.net.ClientPayloadHandler;
import com.example.pigbuilder.net.ControlPayload;
import com.example.pigbuilder.net.ModePayload;
import com.example.pigbuilder.net.SmashPayload;
import com.example.pigbuilder.registry.ModBlocks;
import com.example.pigbuilder.registry.ModEntities;
import com.example.pigbuilder.registry.ModItems;
import com.example.pigbuilder.registry.ModTabs;
import com.example.pigbuilder.world.ModCommands;
import com.example.pigbuilder.world.Tutorial;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@Mod(PigBuilderMod.MODID)
public class PigBuilderMod {
    public static final String MODID = "pigbuilder";

    public PigBuilderMod(IEventBus modBus, ModContainer container) {
        container.registerConfig(ModConfig.Type.SERVER, Config.SPEC);

        ModBlocks.BLOCKS.register(modBus);
        ModItems.ITEMS.register(modBus);
        ModEntities.ENTITIES.register(modBus);
        ModTabs.TABS.register(modBus);
        modBus.addListener(this::registerPayloads);

        NeoForge.EVENT_BUS.addListener(ModCommands::register);
        NeoForge.EVENT_BUS.addListener(Tutorial::onLogin);
        NeoForge.EVENT_BUS.addListener(Tutorial::onServerTick);
    }

    private void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToServer(SmashPayload.TYPE, SmashPayload.STREAM_CODEC, SmashPayload::handle);
        registrar.playToServer(ControlPayload.TYPE, ControlPayload.STREAM_CODEC, ControlPayload::handle);
        registrar.playToClient(ModePayload.TYPE, ModePayload.STREAM_CODEC, ClientPayloadHandler::handle);
    }
}
