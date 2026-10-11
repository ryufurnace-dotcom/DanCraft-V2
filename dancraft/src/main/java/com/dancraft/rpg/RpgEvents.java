package com.dancraft.rpg;

import com.dancraft.Dancraft;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.Tags;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.AdvancementEvent;
import net.minecraftforge.event.entity.player.ItemFishedEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

/** Ganchos de gameplay: quase tudo que o jogador faz rende XP. */
@Mod.EventBusSubscriber(modid = Dancraft.MODID)
public class RpgEvents {

    // ---------- Dados do jogador ----------

    @SubscribeEvent
    public static void attachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player) {
            RpgCapability.Provider provider = new RpgCapability.Provider();
            event.addCapability(RpgCapability.ID, provider);
            event.addListener(provider::invalidate);
        }
    }

    @SubscribeEvent
    public static void onClone(PlayerEvent.Clone event) {
        event.getOriginal().reviveCaps();
        PlayerRpg oldData = RpgManager.get(event.getOriginal());
        PlayerRpg newData = RpgManager.get(event.getEntity());
        if (oldData != null && newData != null) newData.copyFrom(oldData);
        event.getOriginal().invalidateCaps();
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            RpgManager.applyAttributes(player);
            RpgManager.sync(player);
        }
    }

    @SubscribeEvent
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            RpgManager.applyAttributes(player);
            player.setHealth(player.getMaxHealth());
            RpgManager.sync(player);
        }
    }

    @SubscribeEvent
    public static void onChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            RpgManager.applyAttributes(player);
            RpgManager.sync(player);
        }
    }

    // ---------- Tick: fadiga, exploração e sincronização ----------

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.side.isClient()) return;
        if (!(event.player instanceof ServerPlayer player)) return;
        PlayerRpg rpg = RpgManager.get(player);
        if (rpg == null) return;

        if (player.tickCount % 20 == 0) rpg.decayFatigue(1.0);

        if (player.tickCount % 40 == 0 && RpgManager.eligible(player)) {
            player.level().getBiome(player.blockPosition()).unwrapKey().ifPresent(key -> {
                if (rpg.markBiome(key.location().toString())) {
                    RpgManager.awardXp(player, XpSource.EXPLORATION, 25);
                    RpgManager.awardExcelia(player, 3, "novo bioma descoberto");
                }
            });
        }

        if (player.tickCount % 5 == 0) RpgManager.flush(player);
    }

    // ---------- Fontes de XP ----------

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (!(event.getPlayer() instanceof ServerPlayer player)) return;
        BlockXp.Gain gain = BlockXp.evaluate(event.getState(), event.getLevel(), event.getPos());
        if (gain != null) RpgManager.awardXp(player, gain.source(), gain.amount());
    }

    @SubscribeEvent
    public static void onCrafted(PlayerEvent.ItemCraftedEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        ItemStack result = event.getCrafting();
        double amount = result.isDamageableItem() ? 3.5 : 1.5; // ferramentas, armas e armaduras valem mais
        RpgManager.awardXp(player, XpSource.CRAFTING, amount);
    }

    @SubscribeEvent
    public static void onSmelted(PlayerEvent.ItemSmeltedEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        RpgManager.awardXp(player, XpSource.SMELTING, 0.5 * Math.max(1, event.getSmelting().getCount()));
    }

    @SubscribeEvent
    public static void onFished(ItemFishedEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        RpgManager.awardXp(player, XpSource.FISHING, 3.0 + event.getDrops().size());
    }

    @SubscribeEvent
    public static void onAdvancement(AdvancementEvent.AdvancementEarnEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (event.getAdvancement().getDisplay() == null) return; // ignora receitas
        RpgManager.awardXp(player, XpSource.ACHIEVEMENT, 40);
        RpgManager.awardExcelia(player, 2, "conquista");
    }

    @SubscribeEvent
    public static void onKill(LivingDeathEvent event) {
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) return;
        LivingEntity victim = event.getEntity();
        if (victim instanceof Player) return;

        boolean boss = isBoss(victim);
        if (boss) {
            PlayerRpg rpg = RpgManager.get(player);
            String id = String.valueOf(ForgeRegistries.ENTITY_TYPES.getKey(victim.getType()));
            if (rpg != null && RpgManager.eligible(player) && rpg.markFirstKill(id)) {
                RpgManager.awardXp(player, XpSource.ACHIEVEMENT, 300);
                RpgManager.awardExcelia(player, 100, "primeiro chefe derrotado");
            }
        }

        // Só monstros rendem XP de combate: abater animais não é caminho de progressão.
        if (victim instanceof Enemy || boss) {
            RpgManager.awardXp(player, XpSource.COMBAT, 1.0 + victim.getMaxHealth() * 0.2);
        }
    }

    private static boolean isBoss(LivingEntity entity) {
        EntityType<?> type = entity.getType();
        return type.is(Tags.EntityTypes.BOSSES)
                || type == EntityType.ENDER_DRAGON
                || type == EntityType.WITHER
                || type == EntityType.WARDEN
                || type == EntityType.ELDER_GUARDIAN;
    }
}
