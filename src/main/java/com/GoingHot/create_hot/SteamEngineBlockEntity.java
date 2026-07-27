package com.GoingHot.create_hot;

import com.simibubi.create.content.kinetics.base.GeneratingKineticBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

public class SteamEngineBlockEntity extends GeneratingKineticBlockEntity {

    public FluidTank steamTank = new FluidTank(4000) {
        @Override
        protected void onContentsChanged() {
            super.onContentsChanged();
            setChanged();
            if (level != null && !level.isClientSide) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
        }
    };

    // Исправленный конструктор: берет правильный тип, даже если пришел null при регистрации
    public SteamEngineBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type != null ? type : ExampleMod.STEAM_ENGINE_BE.get(), pos, state);
    }

    @Override
    public float getGeneratedSpeed() {
        return (steamTank.getFluidAmount() > 0) ? 256.0f : 0.0f;
    }

    @Override
    public float calculateAddedStressCapacity() {
        if (steamTank.getFluidAmount() <= 0) return 0.0f;
        float fillRatio = (float) steamTank.getFluidAmount() / steamTank.getCapacity();
        return fillRatio * 163.84f;
    }

    @Override
    public void tick() {
        super.tick();
        if (level != null && !level.isClientSide) {
            if (steamTank.getFluidAmount() > 0) {
                steamTank.drain(2, FluidAction.EXECUTE);
            }
            if (level.getGameTime() % 10 == 0) {
                updateGeneratedRotation();
            }
        }
    }

    public IFluidHandler getFluidHandler(Direction side) {
        return steamTank;
    }

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        tag.put("SteamTank", steamTank.writeToNBT(registries, new CompoundTag()));
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        if (tag.contains("SteamTank")) steamTank.readFromNBT(registries, tag.getCompound("SteamTank"));
    }
}
