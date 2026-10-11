package com.dancraft.client;

import com.dancraft.Dancraft;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.OptionsScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.gui.ModListScreen;

/**
 * Tela inicial do Dancraft. A arte é desenhada ajustada à altura da janela e centralizada;
 * as laterais ficam pretas, combinando com o fundo da imagem.
 */
public class DancraftTitleScreen extends Screen {
    private static final ResourceLocation BACKGROUND =
            new ResourceLocation(Dancraft.MODID, "textures/gui/title_background.png");
    /** Lado da imagem (quadrada). Se trocar a arte por outra resolução, ajuste aqui. */
    private static final int TEXTURE_SIZE = 736;

    public DancraftTitleScreen() {
        super(Component.literal("Dancraft"));
    }

    @Override
    protected void init() {
        int buttonWidth = Math.max(100, Math.min(200, (width - height) / 2 - 24));
        int x = 20;
        int y = height / 2 - 20;

        addRenderableWidget(Button.builder(Component.literal("Um jogador"),
                        b -> minecraft.setScreen(new SelectWorldScreen(this)))
                .bounds(x, y, buttonWidth, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Multijogador"),
                        b -> minecraft.setScreen(new JoinMultiplayerScreen(this)))
                .bounds(x, y + 24, buttonWidth, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Mods"),
                        b -> minecraft.setScreen(new ModListScreen(this)))
                .bounds(x, y + 48, buttonWidth, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Opções"),
                        b -> minecraft.setScreen(new OptionsScreen(this, minecraft.options)))
                .bounds(x, y + 72, buttonWidth, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Sair"),
                        b -> minecraft.stop())
                .bounds(x, y + 96, buttonWidth, 20).build());
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        g.fill(0, 0, width, height, 0xFF000000);

        int size = height;
        int imageX = (width - size) / 2;
        g.blit(BACKGROUND, imageX, 0, size, size, 0f, 0f, TEXTURE_SIZE, TEXTURE_SIZE, TEXTURE_SIZE, TEXTURE_SIZE);

        // Logo provisório em texto (troque por uma textura quando tiver o logo pronto)
        g.pose().pushPose();
        g.pose().translate(20, height / 2 - 78, 0);
        g.pose().scale(3.0f, 3.0f, 1.0f);
        g.drawString(font, "DANCRAFT", 0, 0, 0xFFE02020, true);
        g.pose().popPose();
        g.drawString(font, "A Lenda da Dungeon", 22, height / 2 - 46, 0xFFD0D0D0, true);

        super.render(g, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }
}
