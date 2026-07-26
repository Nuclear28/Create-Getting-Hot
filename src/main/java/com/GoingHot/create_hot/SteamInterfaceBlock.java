package com.GoingHot.create_hot;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class SteamInterfaceBlock extends Block implements EntityBlock {
    public SteamInterfaceBlock(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SteamInterfaceBlockEntity(pos, state);
    }

        @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        if (level.isClientSide()) return null;
        
        return blockEntityType == ExampleMod.STEAM_INTERFACE_BE.get() 
            ? (BlockEntityTicker<T>) (BlockEntityTicker<SteamInterfaceBlockEntity>) SteamInterfaceBlockEntity::tick 
            : null;
    }

}
