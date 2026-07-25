package com.GoingHot.create_hot;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import java.util.function.Consumer;

public class ModFluids {
    public static final String MODID = "create_hot";

    public static final DeferredRegister<FluidType> FLUID_TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.FLUID_TYPES, MODID);
    public static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(Registries.FLUID, MODID);
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(Registries.BLOCK, MODID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, MODID);

    // 1. Регистрация Типа жидкости (Встраиваем инициализатор текстур напрямую через NeoForge метод)
    public static final DeferredHolder<FluidType, FluidType> STEAM_TYPE = FLUID_TYPES.register("steam", () ->
        new FluidType(FluidType.Properties.create()
            .descriptionId("fluid.examplemod.steam")
            .canPushEntity(false)
            .canSwim(false)
            .canDrown(false)
            .density(-1000)
            .viscosity(500)
            .motionScale(0.007)) {
            
            @Override
            public void initializeClient(Consumer<net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions> consumer) {
                consumer.accept(new net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions() {
                    private static final ResourceLocation STILL = ResourceLocation.fromNamespaceAndPath("minecraft", "block/water_still");
                    private static final ResourceLocation FLOW = ResourceLocation.fromNamespaceAndPath("minecraft", "block/water_flow");

                    @Override
                    public ResourceLocation getStillTexture() { return STILL; }
                    @Override
                    public ResourceLocation getFlowingTexture() { return FLOW; }
                    @Override
                    public int getTintColor() { return 0x33FFFFFF; } // Полупрозрачный белый пар
                });
            }
        });

    // 2. Источник и текущая жидкость
    public static final DeferredHolder<Fluid, FlowingFluid> STEAM_SOURCE = FLUIDS.register("steam", () ->
        new BaseFlowingFluid.Source(ModFluids.STEAM_PROPERTIES));
    public static final DeferredHolder<Fluid, FlowingFluid> STEAM_FLOWING = FLUIDS.register("steam_flowing", () ->
        new BaseFlowingFluid.Flowing(ModFluids.STEAM_PROPERTIES));

    // 3. Блок жидкости в мире и ведро
    public static final DeferredHolder<Block, LiquidBlock> STEAM_BLOCK = BLOCKS.register("steam_block", () ->
        new LiquidBlock(STEAM_SOURCE.get(), BlockBehaviour.Properties.ofFullCopy(Blocks.WATER).noCollission().noLootTable()));
    public static final DeferredHolder<Item, BucketItem> STEAM_BUCKET = ITEMS.register("steam_bucket", () ->
        new BucketItem(STEAM_SOURCE.get(), new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)));

    // Физические свойства пара
    private static final BaseFlowingFluid.Properties STEAM_PROPERTIES = new BaseFlowingFluid.Properties(
        STEAM_TYPE, STEAM_SOURCE, STEAM_FLOWING)
        .slopeFindDistance(2)
        .levelDecreasePerBlock(2)
        .block(STEAM_BLOCK)
        .bucket(STEAM_BUCKET);

    public static void register(IEventBus eventBus) {
        FLUID_TYPES.register(eventBus);
        FLUIDS.register(eventBus);
        BLOCKS.register(eventBus);
        ITEMS.register(eventBus);
    }
}
