package com.GoingHot.create_hot;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
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

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, MODID);

    // 1. Паровой интерфейс
    public static final DeferredHolder<Block, SteamInterfaceBlock> STEAM_INTERFACE = BLOCKS.register("steam_interface",
        () -> new SteamInterfaceBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(2.0f).sound(SoundType.COPPER)));
    public static final DeferredHolder<Item, BlockItem> STEAM_INTERFACE_ITEM = ITEMS.register("steam_interface",
        () -> new BlockItem(STEAM_INTERFACE.get(), new Item.Properties()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SteamInterfaceBlockEntity>> STEAM_INTERFACE_BE =
        BLOCK_ENTITIES.register("steam_interface", () -> BlockEntityType.Builder.of(SteamInterfaceBlockEntity::new, STEAM_INTERFACE.get()).build(null));

    // 2. Паровой двигатель
    public static final DeferredHolder<Block, SteamEngineBlock> STEAM_ENGINE = BLOCKS.register("steam_engine",
        () -> new SteamEngineBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(3.0f).sound(SoundType.NETHERITE_BLOCK).noOcclusion()));
    public static final DeferredHolder<Item, BlockItem> STEAM_ENGINE_ITEM = ITEMS.register("steam_engine",
        () -> new BlockItem(STEAM_ENGINE.get(), new Item.Properties()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SteamEngineBlockEntity>> STEAM_ENGINE_BE =
        BLOCK_ENTITIES.register("steam_engine", () -> BlockEntityType.Builder.of(
            (pos, state) -> new SteamEngineBlockEntity(null, pos, state), 
            STEAM_ENGINE.get()
        ).build(null));

    public ExampleMod(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        BLOCK_ENTITIES.register(modEventBus);

        ModFluids.register(modEventBus);
        ModCreativeTabs.register(modEventBus);

        modEventBus.addListener(this::registerCapabilities);
        
        // Подключаем подсказки к глобальной шине NeoForge
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(ExampleMod::onItemTooltip);
    }

    private void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
            Capabilities.FluidHandler.BLOCK,
            STEAM_ENGINE_BE.get(),
            (be, direction) -> be.getFluidHandler(direction)
        );
    }

    public static void onItemTooltip(net.neoforged.neoforge.event.entity.player.ItemTooltipEvent event) {
        if (event.getItemStack() == null) return;
        Item item = event.getItemStack().getItem();
        boolean shiftPressed = net.minecraft.client.gui.screens.Screen.hasShiftDown();

        if (item == STEAM_INTERFACE_ITEM.get()) {
            if (shiftPressed) {
                event.getToolTip().add(net.minecraft.network.chat.Component.literal(" "));
                event.getToolTip().add(net.minecraft.network.chat.Component.translatable("create_hot.tooltip.steam_interface.title").withStyle(net.minecraft.ChatFormatting.GOLD));
                event.getToolTip().add(net.minecraft.network.chat.Component.translatable("create_hot.tooltip.steam_interface.description").withStyle(net.minecraft.ChatFormatting.GRAY));
                event.getToolTip().add(net.minecraft.network.chat.Component.literal(" "));
                event.getToolTip().add(net.minecraft.network.chat.Component.translatable("create_hot.tooltip.warning").withStyle(net.minecraft.ChatFormatting.RED));
                event.getToolTip().add(net.minecraft.network.chat.Component.translatable("create_hot.tooltip.steam_interface.warning_text").withStyle(net.minecraft.ChatFormatting.DARK_RED));
            } else {
                event.getToolTip().add(net.minecraft.network.chat.Component.translatable("create_hot.tooltip.hold_shift").withStyle(net.minecraft.ChatFormatting.DARK_GRAY));
            }
        }

        if (item == STEAM_ENGINE_ITEM.get()) {
            if (shiftPressed) {
                event.getToolTip().add(net.minecraft.network.chat.Component.literal(" "));
                event.getToolTip().add(net.minecraft.network.chat.Component.translatable("create_hot.tooltip.steam_engine.title").withStyle(net.minecraft.ChatFormatting.GOLD));
                event.getToolTip().add(net.minecraft.network.chat.Component.translatable("create_hot.tooltip.steam_engine.description").withStyle(net.minecraft.ChatFormatting.GRAY));
                event.getToolTip().add(net.minecraft.network.chat.Component.literal(" "));
                event.getToolTip().add(net.minecraft.network.chat.Component.translatable("create_hot.tooltip.usage").withStyle(net.minecraft.ChatFormatting.BLUE));
                event.getToolTip().add(net.minecraft.network.chat.Component.translatable("create_hot.tooltip.steam_engine.usage_text").withStyle(net.minecraft.ChatFormatting.AQUA));
            } else {
                event.getToolTip().add(net.minecraft.network.chat.Component.translatable("create_hot.tooltip.hold_shift").withStyle(net.minecraft.ChatFormatting.DARK_GRAY));
            }
        }
    }
}
