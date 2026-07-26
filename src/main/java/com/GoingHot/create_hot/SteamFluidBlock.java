package com.example.examplemod;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;

public class SteamFluidBlock extends LiquidBlock {

    public SteamFluidBlock(FlowingFluid fluid, Properties properties) {
        super(fluid, properties);
    }

    // Этот метод вызывается в самый первый миг, когда пар пытается разлиться в мире
    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        if (!level.isClientSide()) {
            if (level instanceof ServerLevel serverLevel) {
                // 1. Спавним красивое плотное облако пара на месте разлива
                serverLevel.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, 
                        pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 
                        8, 0.3, 0.3, 0.3, 0.05);
                
                // 2. И принудительно УНИЧТОЖАЕМ блок жидкости, заменяя его на воздух!
                serverLevel.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
                return;
            }
        }
        super.onPlace(state, level, pos, oldState, isMoving);
    }
}
