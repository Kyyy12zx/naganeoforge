package com.leon.saintsdragons.common.block;

import com.leon.saintsdragons.server.entity.base.DragonGender;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * Shared persistent state for dragon eggs.
 *
 * Ported from Saint's Dragons 1.20.1 by LilRicefield:
 * https://github.com/LilRicefield/saints-dragons/blob/1.20.1/common/src/main/java/com/leon/saintsdragons/common/block/AbstractDragonEggBlockEntity.java
 */
public abstract class AbstractDragonEggBlockEntity extends BlockEntity {
    private static final double LEGACY_NORMAL_HATCH_TICKS = 18000.0D;

    private double hatchProgress;
    @Nullable private UUID ownerUUID;
    @Nullable private UUID hatchAdvancementOwnerUUID;
    @Nullable private DragonGender babyGender;
    private boolean pausedHatchingParticlesShown;

    protected AbstractDragonEggBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public double getHatchProgress() { return hatchProgress; }

    public void setHatchProgress(double hatchProgress) {
        this.hatchProgress = Math.max(0.0D, Math.min(1.0D, hatchProgress));
        setChanged();
    }

    @Nullable public UUID getOwnerUUID() { return ownerUUID; }

    public void setOwnerUUID(@Nullable UUID ownerUUID) {
        this.ownerUUID = ownerUUID;
        setChanged();
    }

    @Nullable public UUID getHatchAdvancementOwnerUUID() {
        return hatchAdvancementOwnerUUID != null ? hatchAdvancementOwnerUUID : ownerUUID;
    }

    public void setHatchAdvancementOwnerUUID(@Nullable UUID value) {
        hatchAdvancementOwnerUUID = value;
        setChanged();
    }

    @Nullable public DragonGender getBabyGender() { return babyGender; }

    public void setBabyGender(@Nullable DragonGender gender) {
        babyGender = gender;
        setChanged();
    }

    public boolean hasShownPausedHatchingParticles() { return pausedHatchingParticlesShown; }

    public void setPausedHatchingParticlesShown(boolean shown) {
        if (pausedHatchingParticlesShown != shown) {
            pausedHatchingParticlesShown = shown;
            setChanged();
        }
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putDouble("HatchProgress", hatchProgress);
        if (ownerUUID != null) tag.putUUID("OwnerUUID", ownerUUID);
        if (hatchAdvancementOwnerUUID != null) tag.putUUID("HatchAdvancementOwnerUUID", hatchAdvancementOwnerUUID);
        if (babyGender != null) tag.putByte("BabyGender", babyGender.getId());
        tag.putBoolean("PausedHatchingParticlesShown", pausedHatchingParticlesShown);
    }

    @Override
    public void loadAdditional(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("HatchProgress")) {
            hatchProgress = Math.max(0.0D, Math.min(1.0D, tag.getDouble("HatchProgress")));
        } else {
            double legacyTicks = Math.max(0, tag.getInt("HatchProgressTicks"));
            hatchProgress = Math.max(0.0D, Math.min(1.0D, legacyTicks / LEGACY_NORMAL_HATCH_TICKS));
        }
        if (tag.hasUUID("OwnerUUID")) ownerUUID = tag.getUUID("OwnerUUID");
        if (tag.hasUUID("HatchAdvancementOwnerUUID")) hatchAdvancementOwnerUUID = tag.getUUID("HatchAdvancementOwnerUUID");
        if (tag.contains("BabyGender")) babyGender = DragonGender.fromId(tag.getByte("BabyGender"));
        pausedHatchingParticlesShown = tag.getBoolean("PausedHatchingParticlesShown");
    }
}
