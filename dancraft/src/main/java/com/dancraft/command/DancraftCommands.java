package com.dancraft.command;

import com.dancraft.rpg.PlayerRpg;
import com.dancraft.rpg.RpgManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Locale;

/** Comandos de teste e administração: /dancraft ... (requer OP). */
public class DancraftCommands {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("dancraft")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("xp").then(Commands.literal("add")
                        .then(Commands.argument("amount", IntegerArgumentType.integer(1, 10_000_000))
                                .executes(ctx -> {
                                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                                    int amount = IntegerArgumentType.getInteger(ctx, "amount");
                                    RpgManager.addXpDirect(player, amount);
                                    RpgManager.sync(player);
                                    ctx.getSource().sendSuccess(() -> Component.literal("+" + amount + " XP"), false);
                                    return 1;
                                }))))
                .then(Commands.literal("excelia").then(Commands.literal("add")
                        .then(Commands.argument("amount", IntegerArgumentType.integer(1, 10_000_000))
                                .executes(ctx -> {
                                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                                    int amount = IntegerArgumentType.getInteger(ctx, "amount");
                                    PlayerRpg rpg = RpgManager.get(player);
                                    if (rpg == null) return 0;
                                    rpg.addExcelia(amount);
                                    RpgManager.sync(player);
                                    ctx.getSource().sendSuccess(() -> Component.literal("+" + amount + " Excelia"), false);
                                    return 1;
                                }))))
                .then(Commands.literal("falna")
                        .then(Commands.literal("grant").executes(ctx -> {
                            ServerPlayer player = ctx.getSource().getPlayerOrException();
                            RpgManager.grantFalna(player);
                            return 1;
                        }))
                        .then(Commands.literal("set")
                                .then(Commands.argument("level", IntegerArgumentType.integer(0, PlayerRpg.MAX_FALNA_LEVEL))
                                        .executes(ctx -> {
                                            ServerPlayer player = ctx.getSource().getPlayerOrException();
                                            int level = IntegerArgumentType.getInteger(ctx, "level");
                                            PlayerRpg rpg = RpgManager.get(player);
                                            if (rpg == null) return 0;
                                            rpg.setFalnaLevel(level);
                                            RpgManager.applyAttributes(player);
                                            RpgManager.sync(player);
                                            ctx.getSource().sendSuccess(() -> Component.literal("Falna nível " + level), false);
                                            return 1;
                                        }))))
                .then(Commands.literal("reset").executes(ctx -> {
                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                    PlayerRpg rpg = RpgManager.get(player);
                    if (rpg == null) return 0;
                    rpg.reset();
                    RpgManager.applyAttributes(player);
                    RpgManager.sync(player);
                    ctx.getSource().sendSuccess(() -> Component.literal("Dados de RPG zerados."), false);
                    return 1;
                }))
                .then(Commands.literal("info").executes(ctx -> {
                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                    PlayerRpg rpg = RpgManager.get(player);
                    if (rpg == null) return 0;
                    String text = String.format(Locale.ROOT, "Nv %d (%.1f/%d XP) | Excelia %d | Falna %d | Pontos %d",
                            rpg.getLevel(), rpg.getXp(), rpg.xpToNext(), rpg.getExcelia(),
                            rpg.getFalnaLevel(), rpg.getStatPoints());
                    ctx.getSource().sendSuccess(() -> Component.literal(text), false);
                    return 1;
                })));
    }
}
