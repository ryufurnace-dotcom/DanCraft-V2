package com.dancraft.client;

import com.dancraft.net.FalnaUpdatePacket;
import com.dancraft.net.ModNetwork;
import com.dancraft.net.SpendPointPacket;
import com.dancraft.rpg.PlayerRpg;
import com.dancraft.rpg.RpgAttribute;
import com.dancraft.rpg.RpgManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.Locale;

/** Painel de status da Falna (tecla K). */
public class StatusScreen extends Screen {
    private static final int W = 250;
    private static final int H = 220;

    private final Button[] plusButtons = new Button[RpgAttribute.values().length];
    private Button updateButton;

    public StatusScreen() {
        super(Component.literal("Status da Falna"));
    }

    private PlayerRpg rpg() {
        Minecraft mc = Minecraft.getInstance();
        return mc.player == null ? null : RpgManager.get(mc.player);
    }

    @Override
    protected void init() {
        int left = (width - W) / 2;
        int top = (height - H) / 2;
        RpgAttribute[] attributes = RpgAttribute.values();

        for (int i = 0; i < attributes.length; i++) {
            final int index = i;
            plusButtons[i] = addRenderableWidget(Button.builder(Component.literal("+"),
                            button -> ModNetwork.CHANNEL.sendToServer(new SpendPointPacket(index)))
                    .bounds(left + W - 30, top + 92 + i * 18, 18, 16)
                    .build());
        }

        updateButton = addRenderableWidget(Button.builder(Component.literal("Atualizar Falna"),
                        button -> ModNetwork.CHANNEL.sendToServer(new FalnaUpdatePacket()))
                .bounds(left + 20, top + H - 30, W - 40, 20)
                .build());
    }

    @Override
    public void tick() {
        PlayerRpg rpg = rpg();
        if (rpg == null) return;
        RpgAttribute[] attributes = RpgAttribute.values();
        for (int i = 0; i < attributes.length; i++) {
            plusButtons[i].active = rpg.hasFalna() && rpg.getStatPoints() > 0
                    && rpg.getAttribute(attributes[i]) < PlayerRpg.MAX_ATTRIBUTE_VALUE;
        }
        updateButton.active = rpg.hasFalna() && rpg.getFalnaLevel() < PlayerRpg.MAX_FALNA_LEVEL;
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        renderBackground(g);
        int left = (width - W) / 2;
        int top = (height - H) / 2;

        g.fill(left, top, left + W, top + H, 0xE0101010);
        g.renderOutline(left, top, W, H, 0xFFB01818);
        g.drawCenteredString(font, "STATUS DA FALNA", left + W / 2, top + 8, 0xFFE03030);

        PlayerRpg rpg = rpg();
        if (rpg != null) {
            int x = left + 20;

            g.drawString(font, "Nível " + rpg.getLevel() + " / " + PlayerRpg.MAX_LEVEL, x, top + 24, 0xFFFFFFFF, false);

            int barWidth = W - 40;
            g.fill(x, top + 36, x + barWidth, top + 42, 0xFF303030);
            g.fill(x, top + 36, x + (int) (barWidth * rpg.xpProgress()), top + 42, 0xFFD02020);
            String xpText = rpg.getLevel() >= PlayerRpg.MAX_LEVEL
                    ? "Nível máximo"
                    : String.format(Locale.ROOT, "%.0f / %d XP", rpg.getXp(), rpg.xpToNext());
            g.drawString(font, xpText, x, top + 45, 0xFFB0B0B0, false);

            if (!rpg.hasFalna()) {
                g.drawString(font, "Sem Falna: encontre uma divindade.", x, top + 60, 0xFFFF8080, false);
            } else {
                g.drawString(font, "Excelia: " + rpg.getExcelia() + "   Falna nível " + rpg.getFalnaLevel(),
                        x, top + 58, 0xFFD070FF, false);
                String next = rpg.getFalnaLevel() >= PlayerRpg.MAX_FALNA_LEVEL
                        ? "Falna no nível máximo"
                        : "Próximo: " + rpg.nextFalnaExcelia() + " Excelia + Nv " + rpg.nextFalnaPlayerLevel();
                g.drawString(font, next, x, top + 69, 0xFF909090, false);
            }

            RpgAttribute[] attributes = RpgAttribute.values();
            int pointsColor = rpg.hasFalna() ? 0xFFFFD34D : 0xFF707070;
            g.drawString(font, "Pontos disponíveis: " + rpg.getStatPoints(), x, top + 82, pointsColor, false);
            for (int i = 0; i < attributes.length; i++) {
                int value = rpg.getAttribute(attributes[i]);
                int y = top + 96 + i * 18;
                g.drawString(font, attributes[i].label() + ": " + value, x, y, 0xFFFFFFFF, false);
                g.drawString(font, attributes[i].describe(value), x + 88, y, 0xFF909090, false);
            }
        }

        super.render(g, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
