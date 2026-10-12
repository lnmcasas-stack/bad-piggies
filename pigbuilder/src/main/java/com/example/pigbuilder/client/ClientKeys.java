package com.example.pigbuilder.client;

import com.example.pigbuilder.PigBuilderMod;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

public final class ClientKeys {
    /** Shown in Controls as "key.category.pigbuilder.main". */
    public static final KeyMapping.Category CATEGORY =
            new KeyMapping.Category(Identifier.fromNamespaceAndPath(PigBuilderMod.MODID, "main"));

    public static final KeyMapping TOGGLE = new KeyMapping(
            "key.pigbuilder.toggle", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_G, CATEGORY);

    private ClientKeys() {}
}
