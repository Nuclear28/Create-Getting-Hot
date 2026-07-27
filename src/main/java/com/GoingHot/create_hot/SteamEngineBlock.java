package com.GoingHot.create_hot;

import com.simibubi.create.content.kinetics.base.HorizontalKineticBlock;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class SteamEngineBlock extends HorizontalKineticBlock implements IBE<SteamEngineBlockEntity> {
    public SteamEngineBlock(Properties properties) { super(properties); }

    @Override
    public Direction.Axis getRotationAxis(BlockState state) { return state.getValue(HORIZONTAL_FACING).getAxis(); }

    @Override
    public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) { return face.getAxis() == getRotationAxis(state); }

    @Override
    public Class<SteamEngineBlockEntity> getBlockEntityClass() { return SteamEngineBlockEntity.class; }

    @Override
    public BlockEntityType<? extends SteamEngineBlockEntity> getBlockEntityType() { return ExampleMod.STEAM_ENGINE_BE.get(); }
}
