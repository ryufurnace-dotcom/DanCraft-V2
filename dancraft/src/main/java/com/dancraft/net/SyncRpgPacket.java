package com.dancraft.net;

import com.dancraft.client.ClientPacketHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Servidor -> cliente: estado de RPG do jogador. */
public class SyncRpgPacket {
    private final CompoundTag data;

    public SyncRpgPacket(CompoundTag data) {
        this.data = data;
    }

    public static void encode(SyncRpgPacket packet, FriendlyByteBuf buf) {
        buf.writeNbt(packet.data);
    }

    public static SyncRpgPacket decode(FriendlyByteBuf buf) {
        CompoundTag tag = buf.readNbt();
        return new SyncRpgPacket(tag == null ? new CompoundTag() : tag);
    }

    public static void handle(SyncRpgPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() ->
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientPacketHandler.handleSync(packet.data)));
        ctx.get().setPacketHandled(true);
    }
}
