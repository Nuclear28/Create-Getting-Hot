package com.GoingHot.create_hot;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.capabilities.Capabilities;

import java.util.HashSet;
import java.util.Set;

@EventBusSubscriber(modid = ExampleMod.MODID)
public class CreateBoilerTicker {

    private static int steamDrainedThisTick = 0;

    public static void registerSteamDrain(int amount) {
        steamDrainedThisTick += amount;
    }

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        Level level = event.getLevel();
        if (level.isClientSide()) return;

        if (level instanceof ServerLevel serverLevel) {
            steamDrainedThisTick = 0;

            if (serverLevel.getGameTime() % 20 != 0) return;

            Set<BlockPos> processedBlocks = new HashSet<>();
            
            for (ServerPlayer player : serverLevel.players()) {
                BlockPos playerPos = player.blockPosition();
                int chunkRadius = 4;
                
                int playerChunkX = playerPos.getX() >> 4;
                int playerChunkZ = playerPos.getZ() >> 4;
                
                for (int x = -chunkRadius; x <= chunkRadius; x++) {
                    for (int z = -chunkRadius; z <= chunkRadius; z++) {
                        LevelChunk chunk = serverLevel.getChunk(playerChunkX + x, playerChunkZ + z);
                        if (chunk == null) continue;
                        
                        for (BlockEntity blockEntity : chunk.getBlockEntities().values()) {
                            BlockPos pos = blockEntity.getBlockPos();
                            if (pos == null || blockEntity.isRemoved() || processedBlocks.contains(pos)) continue;
                            
                            processedBlocks.add(pos);
                            
                            IFluidHandler fluidHandler = serverLevel.getCapability(Capabilities.FluidHandler.BLOCK, pos, null);
                            
                            if (fluidHandler != null) {
                                // ИСПРАВЛЕНО: Убрали лишний метод .getServerLevel(), теперь читаем состояние напрямую из serverLevel
                                BlockState blockUnder = serverLevel.getBlockState(pos.below());
                                String blockId = BuiltInRegistries.BLOCK.getKey(blockUnder.getBlock()).toString().toLowerCase();

                                boolean isHeated = blockUnder.is(Blocks.LAVA) || 
                                                   blockUnder.is(Blocks.MAGMA_BLOCK) || 
                                                   blockId.contains("campfire") || 
                                                   blockId.contains("burner") || 
                                                   blockId.contains("fire");

                                if (isHeated) {
                                    int maxBoilAmount = 200;

                                    FluidStack waterTestStack = new FluidStack(Fluids.WATER.builtInRegistryHolder(), maxBoilAmount);
                                    FluidStack simulatedDrain = fluidHandler.drain(waterTestStack, IFluidHandler.FluidAction.SIMULATE);
                                    int waterToBoil = simulatedDrain.getAmount();

                                    if (waterToBoil > 0) {
                                        BlockPos foundInterfacePos = null;

                                        for (Direction dir : Direction.values()) {
                                            BlockPos interfacePos = pos.relative(dir);
                                            if (serverLevel.getBlockState(interfacePos).is(ExampleMod.STEAM_INTERFACE.get())) {
                                                foundInterfacePos = interfacePos;
                                                break;
                                            }
                                        }

                                        // Если помпа Create не высасывала пар, стравливаем излишки
                                        if (steamDrainedThisTick == 0) {
                                            FluidStack realWaterStack = new FluidStack(Fluids.WATER.builtInRegistryHolder(), waterToBoil);
                                            fluidHandler.drain(realWaterStack, IFluidHandler.FluidAction.EXECUTE);

                                            BlockPos particlePos = (foundInterfacePos != null) ? foundInterfacePos : pos;
                                            
                                            serverLevel.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, 
                                                    particlePos.getX() + 0.5, particlePos.getY() + 1.2, particlePos.getZ() + 0.5, 
                                                    6, 0.2, 0.1, 0.2, 0.05);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
