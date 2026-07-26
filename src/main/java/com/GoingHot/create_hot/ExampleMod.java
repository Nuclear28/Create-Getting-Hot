package com.GoingHot.create_hot;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

@Mod(ExampleMod.MODID)
public class ExampleMod {
    public static final String MODID = "create_hot";
    public static final String CREATE_MODID = "create";

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, MODID);

    public static final DeferredHolder<Block, SteamInterfaceBlock> STEAM_INTERFACE = BLOCKS.register("steam_interface",
        () -> new SteamInterfaceBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(2.0f).sound(SoundType.COPPER)));

    public static final DeferredHolder<Item, BlockItem> STEAM_INTERFACE_ITEM = ITEMS.register("steam_interface",
        () -> new BlockItem(STEAM_INTERFACE.get(), new Item.Properties()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SteamInterfaceBlockEntity>> STEAM_INTERFACE_BE =
        BLOCK_ENTITIES.register("steam_interface", () -> BlockEntityType.Builder.of(SteamInterfaceBlockEntity::new, STEAM_INTERFACE.get()).build(null));

    public ExampleMod(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        BLOCK_ENTITIES.register(modEventBus);

        ModFluids.register(modEventBus);
        ModCreativeTabs.register(modEventBus);

        if (net.neoforged.fml.loading.FMLEnvironment.dist.isClient()) {
            modEventBus.addListener(this::onClientSetup);
        }

        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(ExampleMod::onItemTooltip);
    }

    private void onClientSetup(final net.neoforged.fml.event.lifecycle.FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            // Регистрация Ponder отключена
        });
    }

        // Идеальное форматирование с переносом слишком длинных строк
    public static void onItemTooltip(net.neoforged.neoforge.event.entity.player.ItemTooltipEvent event) {
        if (event.getItemStack() != null && event.getItemStack().getItem() == STEAM_INTERFACE_ITEM.get()) {
            
            if (net.minecraft.client.gui.screens.Screen.hasShiftDown()) {
                event.getToolTip().add(net.minecraft.network.chat.Component.literal(" "));
                
                // Абзац 1: Описание прибора (разбито на 3 аккуратные строки)
                net.minecraft.network.chat.MutableComponent p1 = net.minecraft.network.chat.Component.literal("Многофункциональный интерфейс для\n")
                    .withStyle(net.minecraft.ChatFormatting.GOLD)
                    .append(net.minecraft.network.chat.Component.literal("Паровых двигателей").withStyle(net.minecraft.ChatFormatting.GRAY))
                    .append(net.minecraft.network.chat.Component.literal(" и ").withStyle(net.minecraft.ChatFormatting.GOLD))
                    .append(net.minecraft.network.chat.Component.literal("Бойлеров").withStyle(net.minecraft.ChatFormatting.GRAY))
                    .append(net.minecraft.network.chat.Component.literal(".\nПозволяет напрямую подключать трубы\nдля автоматизации давления.").withStyle(net.minecraft.ChatFormatting.GOLD));
                event.getToolTip().add(p1);

                event.getToolTip().add(net.minecraft.network.chat.Component.literal(" "));

                // Абзац 2: Предупреждение об опасности (разбито на 3 аккуратные строки)
                net.minecraft.network.chat.MutableComponent p2 = net.minecraft.network.chat.Component.literal("Внимание: ").withStyle(net.minecraft.ChatFormatting.RED)
                    .append(net.minecraft.network.chat.Component.literal("При переполнении конечного\nрезервуара избыточное давление жидкости\nили пара может привести к ").withStyle(net.minecraft.ChatFormatting.GOLD))
                    .append(net.minecraft.network.chat.Component.literal("критическому взрыву").withStyle(net.minecraft.ChatFormatting.RED))
                    .append(net.minecraft.network.chat.Component.literal("\nвсей конструкции.").withStyle(net.minecraft.ChatFormatting.GOLD));
                event.getToolTip().add(p2);
                
            } else {
                net.minecraft.network.chat.Component shiftPrompt = net.minecraft.network.chat.Component.literal("Удерживайте [Shift] для сводки")
                    .withStyle(net.minecraft.ChatFormatting.DARK_GRAY);
                event.getToolTip().add(1, shiftPrompt);
            }
        }
    }
}
