package com.dancraft.rpg;

import com.dancraft.Dancraft;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.common.util.LazyOptional;

public class RpgCapability {
    public static final Capability<PlayerRpg> PLAYER_RPG = CapabilityManager.get(new CapabilityToken<>() {});
    public static final ResourceLocation ID = new ResourceLocation(Dancraft.MODID, "player_rpg");

    /** Registrado no barramento do mod (ver Dancraft.java). */
    public static void register(RegisterCapabilitiesEvent event) {
        event.register(PlayerRpg.class);
    }

    public static class Provider implements ICapabilitySerializable<CompoundTag> {
        private final PlayerRpg rpg = new PlayerRpg();
        private final LazyOptional<PlayerRpg> optional = LazyOptional.of(() -> rpg);

        @Override
        public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
            return cap == PLAYER_RPG ? optional.cast() : LazyOptional.empty();
        }

        @Override
        public CompoundTag serializeNBT() {
            return rpg.save();
        }

        @Override
        public void deserializeNBT(CompoundTag tag) {
            rpg.load(tag);
        }

        public void invalidate() {
            optional.invalidate();
        }
    }
}
