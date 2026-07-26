package com.GoingHot.create_hot;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

public class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS = 
        DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ExampleMod.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> BASE_TAB = CREATIVE_TABS.register("create_hot_tab",
        () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.create_hot.base_tab"))
            .icon(() -> new ItemStack(ExampleMod.STEAM_INTERFACE_ITEM.get())) 
            .displayItems((parameters, output) -> {
                output.accept(ExampleMod.STEAM_INTERFACE_ITEM.get());
               
            })
            .build()
    );

    public static void register(IEventBus eventBus) {
        CREATIVE_TABS.register(eventBus);
    }
}
