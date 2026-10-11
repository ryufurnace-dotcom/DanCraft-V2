package com.dancraft;

import com.dancraft.command.DancraftCommands;
import com.dancraft.item.ModItems;
import com.dancraft.net.ModNetwork;
import com.dancraft.rpg.RpgCapability;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(Dancraft.MODID)
public class Dancraft {
    public static final String MODID = "dancraft";

    public Dancraft() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModItems.ITEMS.register(modBus);
        modBus.addListener(RpgCapability::register);
        modBus.addListener(this::commonSetup);
        modBus.addListener(this::addCreativeTabItems);

        MinecraftForge.EVENT_BUS.addListener(this::registerCommands);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(ModNetwork::register);
    }

    private void addCreativeTabItems(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(ModItems.FALNA_SCROLL);
        }
    }

    private void registerCommands(RegisterCommandsEvent event) {
        DancraftCommands.register(event.getDispatcher());
    }
}
