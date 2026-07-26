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

    // Регистрируем наш блок интерфейса (исправлен MapColor на METAL)
    public static final DeferredHolder<Block, SteamInterfaceBlock> STEAM_INTERFACE = BLOCKS.register("steam_interface",
        () -> new SteamInterfaceBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(2.0f).sound(SoundType.COPPER)));

    public static final DeferredHolder<Item, BlockItem> STEAM_INTERFACE_ITEM = ITEMS.register("steam_interface",
        () -> new BlockItem(STEAM_INTERFACE.get(), new Item.Properties()));

    // Регистрируем его BlockEntity
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SteamInterfaceBlockEntity>> STEAM_INTERFACE_BE =
        BLOCK_ENTITIES.register("steam_interface", () -> BlockEntityType.Builder.of(SteamInterfaceBlockEntity::new, STEAM_INTERFACE.get()).build(null));

        public ExampleMod(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        BLOCK_ENTITIES.register(modEventBus);
        
        // Регистрируем жидкости из исправленного класса
        ModFluids.register(modEventBus);
        
        // Регистрируем кастомную креативную вкладку
        ModCreativeTabs.register(modEventBus);

                // Подключаем Ponder только на КЛИЕНТСКОЙ стороне, чтобы сервер не падал
        if (net.neoforged.fml.loading.FMLEnvironment.dist.isClient()) {
            modEventBus.addListener(this::onClientSetup);
        }
    }

    private void onClientSetup(final net.neoforged.fml.event.lifecycle.FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            // Временно отключаем вызов, пока не подключим либу Ponder в build.gradle
            // com.GoingHot.create_hot.infrastructure.ponder.ModPonderIndex.register();
        });
    }
}

