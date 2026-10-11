package com.dancraft.rpg;

import com.dancraft.net.ModNetwork;
import com.dancraft.net.SyncRpgPacket;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.network.PacketDistributor;

import java.util.Locale;
import java.util.UUID;

/** Lógica central do RPG (lado servidor). */
public class RpgManager {
    /** Multiplicador global de XP, para ajustar o ritmo do mod. */
    public static final double XP_RATE = 1.0;

    private static final UUID LEVEL_HEALTH_ID = UUID.fromString("5d1b2a10-0010-4c3e-9a11-d4a2c0de0010");

    /** Obtém os dados de RPG do jogador (null se ainda não anexados). */
    public static PlayerRpg get(Player player) {
        return player.getCapability(RpgCapability.PLAYER_RPG).orElse(null);
    }

    /** Jogadores em criativo/espectador e bots não progridem. */
    public static boolean eligible(Player player) {
        return player instanceof ServerPlayer
                && !(player instanceof FakePlayer)
                && !player.isCreative()
                && !player.isSpectator();
    }

    // ---------- XP e Excelia ----------

    /** Concede XP de uma atividade, aplicando a fadiga (anti-farm). */
    public static void awardXp(ServerPlayer player, XpSource source, double raw) {
        if (raw <= 0 || !eligible(player)) return;
        PlayerRpg rpg = get(player);
        if (rpg == null) return;
        double gained = raw * rpg.applyFatigue(source, raw) * XP_RATE;
        if (gained < 0.01) return;
        grantXp(player, rpg, gained);
    }

    /** Concede XP sem fadiga nem checagem de modo (comandos e depuração). */
    public static void addXpDirect(ServerPlayer player, double amount) {
        PlayerRpg rpg = get(player);
        if (rpg == null) return;
        grantXp(player, rpg, amount);
    }

    private static void grantXp(ServerPlayer player, PlayerRpg rpg, double gained) {
        int levels = rpg.addXp(gained);
        rpg.addPendingGain(gained);
        if (levels > 0) {
            player.sendSystemMessage(Component.literal("§6§lNível " + rpg.getLevel() + "!§r §e+"
                    + (levels * PlayerRpg.POINTS_PER_LEVEL) + " pontos de atributo"
                    + (rpg.hasFalna() ? "" : " §7(receba a Falna para usá-los)")));
            player.level().playSound(null, player.blockPosition(), SoundEvents.PLAYER_LEVELUP,
                    SoundSource.PLAYERS, 0.8f, 1.0f);
            applyAttributes(player);
        }
    }

    /** Concede Excelia (feitos significativos) e avisa o jogador. */
    public static void awardExcelia(ServerPlayer player, long amount, String reason) {
        if (amount <= 0 || !eligible(player)) return;
        PlayerRpg rpg = get(player);
        if (rpg == null) return;
        rpg.addExcelia(amount);
        player.sendSystemMessage(Component.literal("§d+" + amount + " Excelia §7— " + reason));
    }

    // ---------- Falna ----------

    public static void grantFalna(ServerPlayer player) {
        PlayerRpg rpg = get(player);
        if (rpg == null || rpg.hasFalna()) return;
        rpg.grantFalna();
        player.sendSystemMessage(Component.literal(
                "§6✦ Você recebeu a Falna. §eAbra o painel de status com a tecla K."));
        player.level().playSound(null, player.blockPosition(), SoundEvents.ENCHANTMENT_TABLE_USE,
                SoundSource.PLAYERS, 1.0f, 1.2f);
        applyAttributes(player);
        sync(player);
    }

    /** Pedido de atualização de status junto à divindade. */
    public static void tryUpdateFalna(ServerPlayer player) {
        PlayerRpg rpg = get(player);
        if (rpg == null) return;
        String reason = rpg.falnaBlockReason();
        if (reason != null) {
            player.sendSystemMessage(Component.literal("§cA Falna não pode evoluir: " + reason));
            return;
        }
        rpg.upgradeFalna();
        player.sendSystemMessage(Component.literal("§6§l✦ Sua Falna evoluiu para o nível " + rpg.getFalnaLevel()
                + "! §r§e+" + PlayerRpg.POINTS_PER_FALNA_LEVEL + " pontos de atributo"));
        player.level().playSound(null, player.blockPosition(), SoundEvents.UI_TOAST_CHALLENGE_COMPLETE,
                SoundSource.PLAYERS, 1.0f, 1.0f);
        applyAttributes(player);
        sync(player);
    }

    // ---------- Atributos do Minecraft ----------

    public static void applyAttributes(ServerPlayer player) {
        PlayerRpg rpg = get(player);
        if (rpg == null) return;

        int str = rpg.getAttribute(RpgAttribute.STRENGTH);
        int agi = rpg.getAttribute(RpgAttribute.AGILITY);
        int end = rpg.getAttribute(RpgAttribute.ENDURANCE);
        int dex = rpg.getAttribute(RpgAttribute.DEXTERITY);

        setModifier(player.getAttribute(Attributes.ATTACK_DAMAGE), RpgAttribute.STRENGTH.modifierId(),
                "dancraft_strength", str * RpgAttribute.DAMAGE_PER_STRENGTH, AttributeModifier.Operation.ADDITION);
        setModifier(player.getAttribute(Attributes.MOVEMENT_SPEED), RpgAttribute.AGILITY.modifierId(),
                "dancraft_agility", agi * RpgAttribute.SPEED_PER_AGILITY, AttributeModifier.Operation.MULTIPLY_BASE);
        setModifier(player.getAttribute(Attributes.MAX_HEALTH), RpgAttribute.ENDURANCE.modifierId(),
                "dancraft_endurance", end * RpgAttribute.HEALTH_PER_ENDURANCE, AttributeModifier.Operation.ADDITION);
        setModifier(player.getAttribute(Attributes.ATTACK_SPEED), RpgAttribute.DEXTERITY.modifierId(),
                "dancraft_dexterity", dex * RpgAttribute.ATTACK_SPEED_PER_DEXTERITY, AttributeModifier.Operation.MULTIPLY_BASE);
        setModifier(player.getAttribute(Attributes.LUCK), UUID.fromString("5d1b2a10-0011-4c3e-9a11-d4a2c0de0011"),
                "dancraft_luck", dex * RpgAttribute.LUCK_PER_DEXTERITY, AttributeModifier.Operation.ADDITION);

        // Nível do personagem e da Falna também dão vida, mesmo sem gastar pontos.
        double levelHealth = (rpg.getLevel() - 1) * 0.2 + Math.max(0, rpg.getFalnaLevel() - 1) * 2.0;
        setModifier(player.getAttribute(Attributes.MAX_HEALTH), LEVEL_HEALTH_ID,
                "dancraft_level_health", levelHealth, AttributeModifier.Operation.ADDITION);

        if (player.getHealth() > player.getMaxHealth()) {
            player.setHealth(player.getMaxHealth());
        }
    }

    private static void setModifier(AttributeInstance instance, UUID id, String name, double amount,
                                    AttributeModifier.Operation operation) {
        if (instance == null) return;
        if (instance.getModifier(id) != null) instance.removeModifier(id);
        if (amount != 0) instance.addTransientModifier(new AttributeModifier(id, name, amount, operation));
    }

    // ---------- Sincronização ----------

    public static void sync(ServerPlayer player) {
        PlayerRpg rpg = get(player);
        if (rpg == null) return;
        ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new SyncRpgPacket(rpg.toSyncTag()));
        rpg.setDirty(false);
    }

    /** Chamado a cada poucos ticks: envia mudanças e mostra o XP ganho na barra de ação. */
    public static void flush(ServerPlayer player) {
        PlayerRpg rpg = get(player);
        if (rpg == null || !rpg.isDirty()) return;
        double gain = rpg.takePendingGain();
        sync(player);
        if (gain >= 0.5) {
            player.displayClientMessage(Component.literal(
                    String.format(Locale.ROOT, "§e+%.1f XP", gain)), true);
        }
    }
}
