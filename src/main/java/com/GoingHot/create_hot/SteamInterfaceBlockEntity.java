package com.GoingHot.create_hot;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.capabilities.Capabilities;
import org.jetbrains.annotations.NotNull;

public class SteamInterfaceBlockEntity extends BlockEntity {

    public SteamInterfaceBlockEntity(BlockPos pos, BlockState state) {
        super(ExampleMod.STEAM_INTERFACE_BE.get(), pos, state);
    }

    public IFluidHandler getFluidHandler() {
        return new IFluidHandler() {
            @Override public int getTanks() { return 1; }

            @NotNull
            @Override
            public FluidStack getFluidInTank(int tank) {
                // Помпа видит, что внутри интерфейса плещется готовый пар
                return new FluidStack(ModFluids.STEAM_SOURCE, 1000);
            }

            @Override public int getTankCapacity(int tank) { return 4000; }
            @Override public boolean isFluidValid(int tank, @NotNull FluidStack stack) { return false; }
            @Override public int fill(FluidStack resource, FluidAction action) { return 0; }

            @NotNull
            @Override
            public FluidStack drain(FluidStack resource, FluidAction action) {
                if (resource.getFluid().isSame(ModFluids.STEAM_SOURCE.get())) {
                    return drain(resource.getAmount(), action);
                }
                return FluidStack.EMPTY;
            }

            @NotNull
            @Override
            public FluidStack drain(int maxDrain, FluidAction action) {
                if (level == null) return FluidStack.EMPTY;

                // Ищем бак Create вокруг нашего интерфейса
                for (Direction dir : Direction.values()) {
                    BlockPos boilerPos = worldPosition.relative(dir);
                    IFluidHandler boilerHandler = level.getCapability(Capabilities.FluidHandler.BLOCK, boilerPos, dir.getOpposite());
                    
                    if (boilerHandler != null) {
                        FluidStack waterStack = new FluidStack(Fluids.WATER.builtInRegistryHolder(), maxDrain);
                        FluidStack drainedWater = boilerHandler.drain(waterStack, FluidAction.SIMULATE);
                        
                        if (drainedWater.getAmount() > 0) {
                            if (action.execute()) {
                                // Выкачиваем воду из котла Create прямо в момент тика помпы
                                boilerHandler.drain(drainedWater, FluidAction.EXECUTE);
                                CreateBoilerTicker.registerSteamDrain(drainedWater.getAmount());
                            }
                            return new FluidStack(ModFluids.STEAM_SOURCE, drainedWater.getAmount());
                        }
                    }
                }
                return FluidStack.EMPTY;
            }
        };
    }
}
