package com.dancraft.client;

import com.dancraft.rpg.PlayerRpg;
import com.dancraft.rpg.RpgManager;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

/** Só é carregada no cliente (chamada via DistExecutor). */
public class ClientPacketHandler {
    public static void handleSync(CompoundTag tag) {
        Player player = Minecraft.getInstance().player;
        if (player == null) return;
        PlayerRpg rpg = RpgManager.get(player);
        if (rpg != null) rpg.fromSyncTag(tag);
    }
}
