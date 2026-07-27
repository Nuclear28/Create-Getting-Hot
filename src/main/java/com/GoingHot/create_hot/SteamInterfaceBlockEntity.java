package com.GoingHot.create_hot;

// ПРАВИЛЬНЫЙ ИМПОРТ ДЛЯ 1.21.1
import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
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

    public int steamDrainedThisTick = 0;
    private int currentPressure = 0;

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

        if (be.temperature >= 100.0f) {
            be.processFluidDrain();
        }

        if (level.getGameTime() % 15 == 0 && be.temperature > 100) {
            be.spawnSmokeParticles(level, pos);
        }

        be.steamDrainedThisTick = 0;
    }

    // --- ЛОГИКА ОЧКОВ ИНЖЕНЕРА ---
    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        // Заголовок "Информация об устройстве"
        tooltip.add(Component.translatable("create_hot.tooltip.temperature_header")
            .withStyle(ChatFormatting.YELLOW));

        // Выбираем цвет в зависимости от нагрева
        ChatFormatting color = ChatFormatting.WHITE;
        if (temperature > 100) color = ChatFormatting.GOLD;
        if (temperature > 500) color = ChatFormatting.RED;
        if (temperature > 800) color = ChatFormatting.LIGHT_PURPLE;

        tooltip.add(Component.literal(" ")
            .append(Component.translatable("create_hot.tooltip.temperature_value"))
            .append(String.format(" %.1f°C", temperature))
            .withStyle(color));

        // Строка с давлением успешно удалена!
        return true;
    }

    private void updateTemperatureFromNeighbors(Level level, BlockPos pos) {
        float maxHeatFound = 20.0f;
        for (Direction dir : Direction.values()) {
            BlockPos neighborPos = pos.relative(dir);
            BlockEntity neighborBE = level.getBlockEntity(neighborPos);
            if (neighborBE instanceof FluidTankBlockEntity) {
                BlockPos underTankPos = neighborPos.below();
                BlockState heatSource = level.getBlockState(underTankPos);
                float currentSourceTemp = getTemperatureFromBlock(heatSource);
                if (currentSourceTemp > maxHeatFound) maxHeatFound = currentSourceTemp;
            }
        }
        this.targetTemperature = maxHeatFound;
    }

    private float getTemperatureFromBlock(BlockState state) {
        if (state.is(Blocks.LAVA)) return 300.0f;
        if (state.is(Blocks.MAGMA_BLOCK)) return 150.0f;
        if (state.is(Blocks.CAMPFIRE)) return 400.0f;
        if (state.hasProperty(BlazeBurnerBlock.HEAT_LEVEL)) {
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
        float efficiency = Mth.clamp((temperature - 100) / 450f, 0.1f, 3.0f);
        int transferAmount = (int) (100 * efficiency);

        for (Direction dir : Direction.values()) {
            BlockPos inputPos = worldPosition.relative(dir);
            IFluidHandler inputHandler = level.getCapability(Capabilities.FluidHandler.BLOCK, inputPos, dir.getOpposite());
            if (inputHandler != null) {
                FluidStack waterAvailable = inputHandler.drain(transferAmount, IFluidHandler.FluidAction.SIMULATE);
                if (!waterAvailable.isEmpty() && waterAvailable.getFluid().isSame(net.minecraft.world.level.material.Fluids.WATER)) {
                    BlockPos outputPos = this.worldPosition.relative(dir.getOpposite());
                    IFluidHandler outputHandler = level.getCapability(Capabilities.FluidHandler.BLOCK, outputPos, dir);
                    if (outputHandler != null) {
                        var steamFluid = ModFluids.STEAM_SOURCE.get(); 
                        FluidStack steamStack = new FluidStack(steamFluid, waterAvailable.getAmount());
                        int accepted = outputHandler.fill(steamStack, IFluidHandler.FluidAction.SIMULATE);
                        if (accepted > 0) {
                            inputHandler.drain(accepted, IFluidHandler.FluidAction.EXECUTE);
                            outputHandler.fill(new FluidStack(steamFluid, accepted), IFluidHandler.FluidAction.EXECUTE);
                            this.steamDrainedThisTick += accepted;
                            if (this.currentPressure > 0) this.currentPressure--;
                        } else {
                            this.currentPressure++;
                            if (this.currentPressure >= 100) {
                                level.explode(null, worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5, 4.0F, Level.ExplosionInteraction.TNT);
                                return;
                            }
                        }
                    }
                }
            }
        }
    }

    private void spawnSmokeParticles(Level level, BlockPos pos) {
        if (level instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, pos.getX() + 0.5, pos.getY() + 1.1, pos.getZ() + 0.5, (int) (temperature / 150) + 1, 0.1, 0.1, 0.1, 0.03);
        }
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag, registries);
        return tag;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putFloat("Temperature", temperature);
        tag.putInt("Pressure", currentPressure);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.temperature = tag.getFloat("Temperature");
        this.currentPressure = tag.getInt("Pressure");
    }
}
