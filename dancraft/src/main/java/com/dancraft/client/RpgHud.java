package com.dancraft.client;

import com.dancraft.rpg.PlayerRpg;
import com.dancraft.rpg.RpgManager;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

/** Mini painel no canto superior esquerdo: nível, Falna e barra de XP. */
public class RpgHud {
    public static final IGuiOverlay OVERLAY = (gui, graphics, partialTick, screenWidth, screenHeight) -> {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui) return;
        PlayerRpg rpg = RpgManager.get(mc.player);
        if (rpg == null) return;

        int x = 6;
        int y = 6;
        int barWidth = 90;

        String label = "Nv " + rpg.getLevel() + (rpg.hasFalna() ? "  |  Falna " + rpg.getFalnaLevel() : "");
        graphics.drawString(mc.font, label, x, y, 0xFFE8E8E8, true);

        int filled = (int) (barWidth * rpg.xpProgress());
        graphics.fill(x, y + 11, x + barWidth, y + 15, 0xFF202020);
        graphics.fill(x, y + 11, x + filled, y + 15, 0xFFD02020);

        if (rpg.hasFalna() && rpg.getStatPoints() > 0) {
            graphics.drawString(mc.font, "+" + rpg.getStatPoints() + " pontos (K)", x, y + 18, 0xFFFFD34D, true);
        }
    };
}
