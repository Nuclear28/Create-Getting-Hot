package com.GoingHot.create_hot;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ExampleMod.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN_TAB = CREATIVE_TABS.register("create_hot",
        () -> CreativeModeTab.builder()
            .title(Component.translatable("item_group.create_hot.create_hot"))
            .icon(() -> new ItemStack(ExampleMod.STEAM_INTERFACE_ITEM.get())) // Иконка вкладки
            .displayItems((parameters, output) -> {
                // Добавляем наши предметы во вкладку
                output.accept(ExampleMod.STEAM_INTERFACE_ITEM.get());
                output.accept(ExampleMod.STEAM_ENGINE_ITEM.get()); // Добавили двигатель!
            })
            .build());

    public static void register(IEventBus modEventBus) {
        CREATIVE_TABS.register(modEventBus);
    }
}
