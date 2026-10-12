package com.example.pigbuilder.world;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import com.example.pigbuilder.Config;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * First-join tutorial: a few chat messages spaced a few seconds apart.
 * "Seen" is stored as an entity tag on the player, so it is once per player per world.
 */
public final class Tutorial {
    private static final String TAG = "pigbuilder_tutorial_seen";
    private static final int FIRST_DELAY = 60;
    private static final int STEP_TICKS = 180;
    private static final int STEP_COUNT = 7;

    /** player -> {next step, ticks until it} */
    private static final Map<UUID, int[]> RUNNING = new HashMap<>();

    private Tutorial() {}

    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!Config.TUTORIAL_ENABLED.get()) return;
        if (player.getTags().contains(TAG)) return;
        start(player, true);
    }

    public static void start(ServerPlayer player, boolean giveKit) {
        if (giveKit) Kit.give(player);
        RUNNING.put(player.getUUID(), new int[] { 0, FIRST_DELAY });
        player.addTag(TAG);
    }

    public static void skip(ServerPlayer player) {
        RUNNING.remove(player.getUUID());
        player.addTag(TAG);
    }

    public static void onServerTick(ServerTickEvent.Post event) {
        if (RUNNING.isEmpty()) return;
        Iterator<Map.Entry<UUID, int[]>> it = RUNNING.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<UUID, int[]> entry = it.next();
            ServerPlayer player = event.getServer().getPlayerList().getPlayer(entry.getKey());
            if (player == null) {
                it.remove();
                continue;
            }
            int[] st = entry.getValue();
            if (--st[1] > 0) continue;
            send(player, st[0]);
            st[0]++;
            st[1] = STEP_TICKS;
            if (st[0] >= STEP_COUNT) it.remove();
        }
    }

    private static void send(ServerPlayer player, int step) {
        Component header = Component.translatable("tutorial.pigbuilder.header", step + 1, STEP_COUNT)
                .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD);
        Component body = switch (step) {
            case 1 -> Component.translatable("tutorial.pigbuilder.step1", Component.keybind("key.pigbuilder.toggle"));
            default -> Component.translatable("tutorial.pigbuilder.step" + step);
        };
        player.sendSystemMessage(header);
        player.sendSystemMessage(body.copy().withStyle(ChatFormatting.YELLOW));
        player.displayClientMessage(body, true);
        if (step == 0) {
            player.connection.send(new ClientboundSetTitlesAnimationPacket(10, 70, 20));
            player.connection.send(new ClientboundSetTitleTextPacket(
                    Component.translatable("tutorial.pigbuilder.title").withStyle(ChatFormatting.GOLD)));
        }
    }
}
