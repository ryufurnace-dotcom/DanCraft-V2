package com.dancraft.item;

import com.dancraft.rpg.PlayerRpg;
import com.dancraft.rpg.RpgManager;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Item provisório que concede a Falna. Será substituído pelos deuses (NPCs) na fase da campanha.
 */
public class FalnaScrollItem extends Item {
    public FalnaScrollItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            PlayerRpg rpg = RpgManager.get(serverPlayer);
            if (rpg == null) return InteractionResultHolder.fail(stack);
            if (rpg.hasFalna()) {
                serverPlayer.displayClientMessage(Component.literal("§cVocê já possui a Falna."), true);
                return InteractionResultHolder.fail(stack);
            }
            RpgManager.grantFalna(serverPlayer);
            if (!serverPlayer.getAbilities().instabuild) stack.shrink(1);
            return InteractionResultHolder.success(stack);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
}
