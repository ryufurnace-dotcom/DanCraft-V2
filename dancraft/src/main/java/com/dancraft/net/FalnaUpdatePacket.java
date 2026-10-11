package com.dancraft.net;

import com.dancraft.rpg.RpgManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Cliente -> servidor: pedir a atualização de status da Falna. */
public class FalnaUpdatePacket {
    public static void encode(FalnaUpdatePacket packet, FriendlyByteBuf buf) {
    }

    public static FalnaUpdatePacket decode(FriendlyByteBuf buf) {
        return new FalnaUpdatePacket();
    }

    public static void handle(FalnaUpdatePacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) RpgManager.tryUpdateFalna(player);
        });
        ctx.get().setPacketHandled(true);
    }
}
