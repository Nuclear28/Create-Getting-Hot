package com.GoingHot.create_hot;

import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.content.kinetics.base.GeneratingKineticBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import java.util.List;

public class SteamEngineBlockEntity extends GeneratingKineticBlockEntity implements IHaveGoggleInformation {

    public FluidTank steamTank = new FluidTank(4000);

    public SteamEngineBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type != null ? type : ExampleMod.STEAM_ENGINE_BE.get(), pos, state);
    }

    public float getSteamTemperature() {
        FluidStack steam = steamTank.getFluid();
        if (steam.isEmpty()) return 100.0f;
        var customData = steam.get(DataComponents.CUSTOM_DATA);
        return (customData != null) ? customData.copyTag().getFloat("Temperature") : 100.0f;
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        tooltip.add(Component.translatable("create_hot.tooltip.steam_engine_stats").withStyle(ChatFormatting.GOLD));
        
        float temp = getSteamTemperature();
        ChatFormatting color = temp > 600 ? ChatFormatting.RED : (temp > 300 ? ChatFormatting.GOLD : ChatFormatting.WHITE);
        tooltip.add(Component.literal("  ").append(Component.translatable("create_hot.tooltip.steam_temp")).append(String.format(" %.1f°C", temp)).withStyle(color));
        
        // Считаем итоговые SU для отображения (Capacity * Speed)
        float totalStress = calculateAddedStressCapacity() * getGeneratedSpeed();
        tooltip.add(Component.literal("  ").append(Component.translatable("create_hot.tooltip.stress_output")).append(String.format(" %.0f SU", totalStress)).withStyle(ChatFormatting.AQUA));
        
        return true;
    }

    @Override public float getGeneratedSpeed() { return (steamTank.getFluidAmount() > 0) ? 256.0f : 0.0f; }

    @Override public float calculateAddedStressCapacity() {
        if (steamTank.getFluidAmount() <= 0) return 0.0f;
        
        // 47.0f * 256 RPM ≈ 12032 SU при полном баке и макс температуре
        float baseStress = 47.0f; 
        float heatMultiplier = Mth.clamp(getSteamTemperature() / 300.0f, 1.0f, 3.0f);
        float fillRatio = (float) steamTank.getFluidAmount() / steamTank.getCapacity();
        
        return fillRatio * baseStress * heatMultiplier;
    }

    @Override public void tick() {
        super.tick();
        if (level != null && !level.isClientSide && steamTank.getFluidAmount() > 0) {
            steamTank.drain(2, IFluidHandler.FluidAction.EXECUTE);
            if (level.getGameTime() % 10 == 0) updateGeneratedRotation();
        }
    }

    public IFluidHandler getFluidHandler(Direction side) { return steamTank; }
    @Override protected void write(CompoundTag tag, HolderLookup.Provider reg, boolean cp) { super.write(tag, reg, cp); tag.put("Tank", steamTank.writeToNBT(reg, new CompoundTag())); }
    @Override protected void read(CompoundTag tag, HolderLookup.Provider reg, boolean cp) { super.read(tag, reg, cp); steamTank.readFromNBT(reg, tag.getCompound("Tank")); }
}
