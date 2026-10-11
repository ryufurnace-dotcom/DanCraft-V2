package com.dancraft.item;

import com.dancraft.Dancraft;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, Dancraft.MODID);

    public static final RegistryObject<Item> FALNA_SCROLL = ITEMS.register("falna_scroll",
            () -> new FalnaScrollItem(new Item.Properties().stacksTo(1).rarity(Rarity.RARE)));
}
