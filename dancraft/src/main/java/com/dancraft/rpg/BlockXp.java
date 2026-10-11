package com.dancraft.rpg;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.Tags;

/** Decide quanto XP um bloco quebrado rende e de qual fonte. */
public class BlockXp {
    public record Gain(XpSource source, double amount) {}

    /** @return null se o bloco não rende XP. */
    public static Gain evaluate(BlockState state, BlockGetter level, BlockPos pos) {
        float hardness = state.getDestroySpeed(level, pos);
        if (hardness < 0) return null; // inquebrável

        if (state.is(Tags.Blocks.ORES)) {
            return new Gain(XpSource.MINING, 4.0 + hardness * 1.5);
        }
        if (state.is(BlockTags.LOGS)) {
            return new Gain(XpSource.WOODCUTTING, 2.0);
        }
        if (state.getBlock() instanceof CropBlock crop) {
            return crop.isMaxAge(state) ? new Gain(XpSource.FARMING, 2.5) : null;
        }
        if (state.is(BlockTags.MINEABLE_WITH_PICKAXE)) {
            return new Gain(XpSource.MINING, 0.5 + hardness * 0.2);
        }
        if (state.is(BlockTags.MINEABLE_WITH_SHOVEL)) {
            return new Gain(XpSource.DIGGING, 0.2);
        }
        return null;
    }
}
