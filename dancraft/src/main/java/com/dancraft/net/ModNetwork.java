package com.dancraft.net;

import com.dancraft.Dancraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class ModNetwork {
    private static final String PROTOCOL = "1";

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(Dancraft.MODID, "main"),
            () -> PROTOCOL, PROTOCOL::equals, PROTOCOL::equals);

    public static void register() {
        int id = 0;
        CHANNEL.registerMessage(id++, SyncRpgPacket.class,
                SyncRpgPacket::encode, SyncRpgPacket::decode, SyncRpgPacket::handle);
        CHANNEL.registerMessage(id++, SpendPointPacket.class,
                SpendPointPacket::encode, SpendPointPacket::decode, SpendPointPacket::handle);
        CHANNEL.registerMessage(id++, FalnaUpdatePacket.class,
                FalnaUpdatePacket::encode, FalnaUpdatePacket::decode, FalnaUpdatePacket::handle);
    }
}
