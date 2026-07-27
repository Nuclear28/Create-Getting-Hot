package com.GoingHot.create_hot;

import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import com.simibubi.create.content.fluids.tank.FluidTankBlockEntity;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock.HeatLevel;
import java.util.List;

public class SteamInterfaceBlockEntity extends BlockEntity implements IHaveGoggleInformation {

    private float temperature = 20.0f;
    private float targetTemperature = 20.0f;

    public SteamInterfaceBlockEntity(BlockPos pos, BlockState state) {
        super(ExampleMod.STEAM_INTERFACE_BE.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, SteamInterfaceBlockEntity be) {
        if (level.isClientSide()) return;
        be.updateTemperatureFromNeighbors(level, pos);
        float oldTemp = be.temperature;
        be.temperature += (be.targetTemperature - be.temperature) * 0.05f;
        if (Math.abs(be.temperature - oldTemp) > 0.5f) {
            be.setChanged();
            level.sendBlockUpdated(pos, state, state, 3);
        }
        if (be.temperature >= 100.0f) be.processFluidDrain();
        if (level.getGameTime() % 15 == 0 && be.temperature > 100) be.spawnSmokeParticles(level, pos);
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        tooltip.add(Component.translatable("create_hot.tooltip.temperature_header").withStyle(ChatFormatting.YELLOW));
        tooltip.add(Component.literal(" ").append(Component.translatable("create_hot.tooltip.temperature_value"))
            .append(String.format(" %.1f°C", temperature)).withStyle(temperature > 500 ? ChatFormatting.RED : ChatFormatting.GOLD));
        return true;
    }

    private void updateTemperatureFromNeighbors(Level level, BlockPos pos) {
        float maxHeatFound = 20.0f;
        for (Direction dir : Direction.values()) {
            BlockEntity be = level.getBlockEntity(pos.relative(dir));
            if (be instanceof FluidTankBlockEntity) {
                float t = getTemperatureFromBlock(level.getBlockState(pos.relative(dir).below()));
                if (t > maxHeatFound) maxHeatFound = t;
            }
        }
        this.targetTemperature = maxHeatFound;
    }

    private float getTemperatureFromBlock(BlockState state) {
        if (state.is(Blocks.LAVA)) return 300.0f;
        if (state.is(Blocks.MAGMA_BLOCK)) return 150.0f;
        if (state.is(Blocks.CAMPFIRE)) return 400.0f;
        if (state.getBlock() instanceof BlazeBurnerBlock) {
            HeatLevel heat = state.getValue(BlazeBurnerBlock.HEAT_LEVEL);
            return switch (heat) {
                case NONE -> 20.0f;
                case SMOULDERING -> 200.0f;
                case KINDLED -> 600.0f;
                case SEETHING -> 1000.0f;
                default -> 100.0f;
            };
        }
        return 20.0f;
    }

    private void processFluidDrain() {
        if (level == null) return;
        int amount = (int) (100 * Mth.clamp((temperature - 100) / 450f, 0.1f, 3.0f));
        for (Direction dir : Direction.values()) {
            IFluidHandler in = level.getCapability(Capabilities.FluidHandler.BLOCK, worldPosition.relative(dir), dir.getOpposite());
            if (in != null) {
                FluidStack water = in.drain(amount, IFluidHandler.FluidAction.SIMULATE);
                if (!water.isEmpty() && water.getFluid().isSame(net.minecraft.world.level.material.Fluids.WATER)) {
                    IFluidHandler out = level.getCapability(Capabilities.FluidHandler.BLOCK, worldPosition.relative(dir.getOpposite()), dir);
                    if (out != null) {
                        FluidStack steam = new FluidStack(ModFluids.STEAM_SOURCE.get(), water.getAmount());
                        CompoundTag tag = new CompoundTag();
                        tag.putFloat("Temperature", this.temperature);
                        steam.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
                        if (out.fill(steam, IFluidHandler.FluidAction.SIMULATE) > 0) {
                            in.drain(water.getAmount(), IFluidHandler.FluidAction.EXECUTE);
                            out.fill(steam, IFluidHandler.FluidAction.EXECUTE);
                        }
                    }
                }
            }
        }
    }

    private void spawnSmokeParticles(Level level, BlockPos pos) {
        if (level instanceof ServerLevel sl) sl.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, pos.getX() + 0.5, pos.getY() + 1.1, pos.getZ() + 0.5, 2, 0.1, 0.1, 0.1, 0.03);
    }
    
    @Override public ClientboundBlockEntityDataPacket getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }
    @Override public CompoundTag getUpdateTag(HolderLookup.Provider reg) { CompoundTag tag = new CompoundTag(); saveAdditional(tag, reg); return tag; }
    @Override protected void saveAdditional(CompoundTag tag, HolderLookup.Provider reg) { super.saveAdditional(tag, reg); tag.putFloat("Temp", temperature); }
    @Override protected void loadAdditional(CompoundTag tag, HolderLookup.Provider reg) { super.loadAdditional(tag, reg); this.temperature = tag.getFloat("Temp"); }
}
