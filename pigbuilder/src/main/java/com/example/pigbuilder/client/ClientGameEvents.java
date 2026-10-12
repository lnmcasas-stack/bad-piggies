package com.example.pigbuilder.client;

import com.example.pigbuilder.entity.ContraptionEntity;
import com.example.pigbuilder.net.ControlPayload;
import com.example.pigbuilder.net.SmashPayload;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * 2.5D side view: the camera looks along +Z, the player is locked to one Z plane and moves along X.
 *  A / D        move left / right (or drive a contraption)
 *  Space        jump (or fire propellers while driving)
 *  Attack       smash / push in the facing direction
 *  W + Attack   smash above, S + Attack smash below
 * Mounting a contraption switches the side view on automatically.
 */
public final class ClientGameEvents {
    private static boolean active = false;
    private static boolean autoEnabled = false;
    private static double lockedZ;
    private static int facing = 1; // +1 = +X, -1 = -X
    private static boolean aimUp, aimDown;
    private static CameraType previousCamera = CameraType.FIRST_PERSON;

    private static int lastMove = 0;
    private static boolean lastUp = false;
    private static int sendTimer = 0;

    private ClientGameEvents() {}

    /** Called from the /sidescroll side command. */
    public static void setMode(boolean on) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player != null && on != active) {
            toggle(mc, player);
            autoEnabled = false;
        }
    }

    public static void onTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;

        while (ClientKeys.TOGGLE.consumeClick()) {
            if (player != null) {
                toggle(mc, player);
                autoEnabled = false;
            }
        }

        boolean riding = player != null && player.getVehicle() instanceof ContraptionEntity;
        if (player != null) {
            if (riding && !active) {
                toggle(mc, player);
                autoEnabled = true;
            } else if (!riding && active && autoEnabled) {
                toggle(mc, player);
                autoEnabled = false;
            }
        }

        boolean left = mc.options.keyLeft.isDown();
        boolean right = mc.options.keyRight.isDown();
        aimUp = mc.options.keyUp.isDown();
        aimDown = mc.options.keyDown.isDown();

        if (riding) {
            int move = (left ? 1 : 0) - (right ? 1 : 0);
            boolean up = mc.options.keyJump.isDown();
            if (move != lastMove || up != lastUp || ++sendTimer >= 5) {
                PacketDistributor.sendToServer(new ControlPayload(move, up));
                lastMove = move;
                lastUp = up;
                sendTimer = 0;
            }
        }

        if (!active) return;
        if (player == null) { active = false; return; }

        if (left != right) facing = left ? 1 : -1;

        player.setYRot(0f);
        player.yRotO = 0f;
        player.setXRot(0f);
        player.xRotO = 0f;

        float body = facing > 0 ? -90f : 90f;
        player.yBodyRot = body;
        player.yBodyRotO = body;
        player.yHeadRot = body;
        player.yHeadRotO = body;

        // W/S would walk along Z; pin the player to the plane instead.
        if (!player.isPassenger()) {
            if (Math.abs(player.getZ() - lockedZ) > 1.0E-4) {
                player.setPos(player.getX(), player.getY(), lockedZ);
            }
            Vec3 m = player.getDeltaMovement();
            player.setDeltaMovement(m.x, m.y, 0);
        }
    }

    public static void onInteract(InputEvent.InteractionKeyMappingTriggered event) {
        if (!active || !event.isAttack()) return;
        event.setCanceled(true);
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || player.isPassenger()) return; // contraptions bulldoze on their own
        int dy = aimUp ? 1 : (aimDown ? -1 : 0);
        int dx = dy != 0 ? 0 : facing;
        PacketDistributor.sendToServer(new SmashPayload(dx, dy));
    }

    public static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        active = false;
        autoEnabled = false;
    }

    private static void toggle(Minecraft mc, LocalPlayer player) {
        if (!active) {
            active = true;
            lockedZ = player.getZ();
            previousCamera = mc.options.getCameraType();
            mc.options.setCameraType(CameraType.THIRD_PERSON_BACK);
            player.displayClientMessage(Component.translatable("message.pigbuilder.on"), true);
        } else {
            active = false;
            mc.options.setCameraType(previousCamera);
            player.displayClientMessage(Component.translatable("message.pigbuilder.off"), true);
        }
    }
}
