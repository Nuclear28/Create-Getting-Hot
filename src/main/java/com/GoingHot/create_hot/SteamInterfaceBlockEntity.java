package com.GoingHot.create_hot;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.particles.ParticleTypes;
import com.simibubi.create.content.fluids.tank.FluidTankBlockEntity;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.FluidStack;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

public class SteamInterfaceBlockEntity extends BlockEntity {
    
    public int steamDrainedThisTick = 0;
    private int currentPressure = 0; 
    private static Method getControllerBEMethod = null;
    private static Method getTankInventoryMethod = null;
    private static Method getBlockPosMethod = null;

    public SteamInterfaceBlockEntity(BlockPos pos, BlockState state) {
        super(ExampleMod.STEAM_INTERFACE_BE.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, SteamInterfaceBlockEntity blockEntity) {
        if (level.isClientSide()) return;

        // Всасываем воду и генерируем пар каждый тик
        blockEntity.processFluidDrain();

        // Проверку дыма делаем раз в секунду (20 тиков)
        if (level.getGameTime() % 20 != 0) return;

        if (blockEntity.steamDrainedThisTick == 0) {
            for (Direction dir : Direction.values()) {
                BlockPos boilerPos = pos.relative(dir);
                BlockEntity targetBE = level.getBlockEntity(boilerPos);
                
                if (targetBE instanceof FluidTankBlockEntity tank) {
                    if (tank.boiler != null && tank.boiler.isActive()) {
                        if (level instanceof ServerLevel serverLevel) {
                            serverLevel.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE,
                                pos.getX() + 0.5, pos.getY() + 1.2, pos.getZ() + 0.5,
                                5, 0.1, 0.1, 0.1, 0.05);
                        }
                    }
                }
            }
        }
        
        blockEntity.steamDrainedThisTick = 0;
    }

    private void processFluidDrain() {
        if (level == null) return;

        Set<BlockPos> processedControllers = new HashSet<>();

        for (Direction dir : Direction.values()) {
            BlockPos inputPos = this.worldPosition.relative(dir);
            BlockEntity inputBE = level.getBlockEntity(inputPos);

            if (inputBE instanceof FluidTankBlockEntity inputTank) {
                try {
                    if (getControllerBEMethod == null) getControllerBEMethod = FluidTankBlockEntity.class.getMethod("getControllerBE");
                    if (getTankInventoryMethod == null) getTankInventoryMethod = FluidTankBlockEntity.class.getMethod("getTankInventory");
                    if (getBlockPosMethod == null) getBlockPosMethod = BlockEntity.class.getMethod("getBlockPos");

                    Object inputControllerObj = getControllerBEMethod.invoke(inputTank);
                    Object inputController = (inputControllerObj != null) ? inputControllerObj : inputTank;
                    
                    BlockPos inputControllerPos = (BlockPos) getBlockPosMethod.invoke(inputController);
                    if (processedControllers.contains(inputControllerPos)) continue;

                    Object inputFluidTankObj = getTankInventoryMethod.invoke(inputController);

                    if (inputFluidTankObj instanceof IFluidHandler inputHandler) {
                        FluidStack availableFluid = inputHandler.drain(100, IFluidHandler.FluidAction.SIMULATE);
                        
                        // Всасываем только воду
                        if (!availableFluid.isEmpty() && availableFluid.getFluid().isSame(net.minecraft.world.level.material.Fluids.WATER)) {
                            
                            Direction oppositeDir = dir.getOpposite();
                            BlockPos outputPos = this.worldPosition.relative(oppositeDir);
                            BlockEntity outputBE = level.getBlockEntity(outputPos);

                            if (outputBE instanceof FluidTankBlockEntity outputTank) {
                                Object outControllerObj = getControllerBEMethod.invoke(outputTank);
                                Object outputController = (outControllerObj != null) ? outControllerObj : outputTank;
                                BlockPos outputControllerPos = (BlockPos) getBlockPosMethod.invoke(outputController);

                                if (inputControllerPos.equals(outputControllerPos)) continue;

                                Object outFluidTankObj = getTankInventoryMethod.invoke(outputController);

                                if (outFluidTankObj instanceof IFluidHandler outHandler) {
                                    
                                    net.minecraft.world.level.material.Fluid mySteam = ModFluids.STEAM_SOURCE.get();

                                    if (mySteam != null) {
                                        FluidStack steamStack = new FluidStack(mySteam, availableFluid.getAmount());

                                        // Симулируем залив твоего пара в правый бак
                                        int acceptedSteamAmount = outHandler.fill(steamStack, IFluidHandler.FluidAction.SIMULATE);

                                        if (acceptedSteamAmount > 0) {
                                            // Сбрасываем давление, если бак принимает пар
                                            if (this.currentPressure > 0) this.currentPressure--;

                                            // Переносим жидкость
                                            inputHandler.drain(acceptedSteamAmount, IFluidHandler.FluidAction.EXECUTE);
                                            FluidStack finalSteamToPour = new FluidStack(mySteam, acceptedSteamAmount);
                                            int realPoured = outHandler.fill(finalSteamToPour, IFluidHandler.FluidAction.EXECUTE);

                                            if (realPoured > 0) {
                                                this.steamDrainedThisTick += realPoured;
                                                processedControllers.add(inputControllerPos);
                                                processedControllers.add(outputControllerPos);
                                            }
                                        } else {
                                            // Бак переполнен, поднимаем давление
                                            this.currentPressure++;

                                            // Спавним аварийный дым предупреждения
                                            if (level instanceof ServerLevel serverLevel && level.getGameTime() % 5 == 0) {
                                                serverLevel.sendParticles(ParticleTypes.SMOKE,
                                                    this.worldPosition.getX() + 0.5, this.worldPosition.getY() + 1.1, this.worldPosition.getZ() + 0.5,
                                                    3, 0.1, 0.1, 0.1, 0.02);
                                            }

                                            // Бабах через 5 секунд перегрузки (100 тиков)
                                            if (this.currentPressure >= 100) {
                                                level.explode(null, this.worldPosition.getX() + 0.5, this.worldPosition.getY() + 0.5, this.worldPosition.getZ() + 0.5, 
                                                    4.0F, Level.ExplosionInteraction.TNT);
                                                return;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    // Игнорируем любые сбои рефлексии
                }
            }
        }
    }
}
