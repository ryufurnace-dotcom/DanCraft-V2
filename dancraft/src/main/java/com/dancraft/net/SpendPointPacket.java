package com.dancraft.net;

import com.dancraft.rpg.PlayerRpg;
import com.dancraft.rpg.RpgAttribute;
import com.dancraft.rpg.RpgManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Cliente -> servidor: gastar um ponto em um atributo. */
public class SpendPointPacket {
    private final int attributeIndex;

    public SpendPointPacket(int attributeIndex) {
        this.attributeIndex = attributeIndex;
    }

    public static void encode(SpendPointPacket packet, FriendlyByteBuf buf) {
        buf.writeVarInt(packet.attributeIndex);
    }

    public static SpendPointPacket decode(FriendlyByteBuf buf) {
        return new SpendPointPacket(buf.readVarInt());
    }

    public static void handle(SpendPointPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;
            RpgAttribute[] attributes = RpgAttribute.values();
            if (packet.attributeIndex < 0 || packet.attributeIndex >= attributes.length) return;
            PlayerRpg rpg = RpgManager.get(player);
            if (rpg != null && rpg.spendPoint(attributes[packet.attributeIndex])) {
                RpgManager.applyAttributes(player);
                RpgManager.sync(player);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
