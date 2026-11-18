package com.leon.saintsdragons.neoforge.platform;

import com.leon.saintsdragons.platform.ConfigHelper;
import com.leon.saintsdragons.platform.NetworkHelper;
import com.leon.saintsdragons.platform.PlatformHelper;
import com.leon.saintsdragons.platform.RegistryHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.MobBucketItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.bus.api.IEventBus;

import java.nio.file.Path;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * NeoForge service-provider implementation for shared platform hooks.
 *
 * Platform services are configured by the mod entry point before common setup
 * accesses Services.PLATFORM.
 */
public final class NeoForgePlatformHelper implements PlatformHelper {
    private static IEventBus modEventBus;
    private static ModContainer modContainer;
    private static NeoForgeNetworkHelper networkHelper;
    private static NeoForgeRegistryHelper registryHelper;
    private static NeoForgeConfigHelper configHelper;

    public NeoForgePlatformHelper() {
    }

    public static synchronized void configure(IEventBus eventBus, ModContainer container) {
        Objects.requireNonNull(eventBus, "eventBus");
        Objects.requireNonNull(container, "container");
        if (modEventBus != null && modEventBus != eventBus) {
            throw new IllegalStateException("NeoForge platform services were already configured");
        }
        modEventBus = eventBus;
        modContainer = container;
        if (networkHelper == null) {
            networkHelper = new NeoForgeNetworkHelper(eventBus);
        }
    }

    private static void requireConfigured() {
        if (modEventBus == null || modContainer == null) {
            throw new IllegalStateException("NeoForge platform services have not been configured");
        }
    }

    @Override
    public RegistryHelper getRegistryHelper() {
        requireConfigured();
        if (registryHelper == null) {
            registryHelper = new NeoForgeRegistryHelper(modEventBus);
        }
        return registryHelper;
    }

    @Override
    public NetworkHelper getNetworkHelper() {
        requireConfigured();
        return networkHelper;
    }

    @Override
    public ConfigHelper getConfigHelper() {
        requireConfigured();
        if (configHelper == null) {
            configHelper = new NeoForgeConfigHelper(modContainer);
        }
        return configHelper;
    }

    @Override
    public void runOnClient(Runnable runnable) {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            ClientExecutor.run(runnable);
        }
    }

    @Override
    public <T> T callOnClient(Supplier<T> supplier) {
        return FMLEnvironment.dist == Dist.CLIENT ? ClientExecutor.call(supplier) : null;
    }

    @Override
    public boolean isDevelopmentEnvironment() {
        return !FMLEnvironment.production;
    }

    @Override
    public String getPlatformId() {
        return "neoforge";
    }

    @Override
    public boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }

    @Override
    public boolean isGenericDiveLoopEnabled() {
        return true;
    }

    @Override
    public float getSwarmBattleMusicVolume() {
        return 1.0F;
    }

    @Override
    public Item createSpawnEgg(Supplier<? extends EntityType<? extends Mob>> entityType,
                               int primaryColor, int secondaryColor, Item.Properties properties) {
        throw new UnsupportedOperationException("Spawn egg factory will be enabled with the NeoForge item registry port");
    }

    @Override
    public Item createDraconianSwarmSpawnEgg(Supplier<? extends EntityType<? extends Mob>> displayEntityType,
                                             int primaryColor, int secondaryColor, Item.Properties properties) {
        throw new UnsupportedOperationException("Draconian Swarm spawn egg factory is not ported yet");
    }

    @Override
    public Item createDragonheartSword(Tier tier, int attackDamageModifier, float attackSpeedModifier,
                                       double entityReach, float criticalDamageBonus, Item.Properties properties) {
        throw new UnsupportedOperationException("Dragonheart sword factory is not ported yet");
    }

    @Override
    public Item createMobBucket(Supplier<? extends EntityType<? extends Mob>> entityType,
                                Fluid fluid, SoundEvent emptySound, Item.Properties properties) {
        return new MobBucketItem(entityType, () -> fluid, () -> emptySound, properties);
    }

    @Override
    public SimpleParticleType createSimpleParticle(boolean overrideLimiter) {
        return new SimpleParticleType(overrideLimiter);
    }

    @Override
    public Path getConfigDirectory() {
        return FMLPaths.CONFIGDIR.get();
    }

    @Override
    public double getPlayerAttackReach(Player player) {
        return player.getAttributeValue(Attributes.ENTITY_INTERACTION_RANGE);
    }

    @Override
    public boolean canDragonBreakBlock(ServerLevel level, LivingEntity dragon, BlockPos pos, BlockState state) {
        return level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)
                && state.canEntityDestroy(level, pos, dragon);
    }

    private static final class ClientExecutor {
        private ClientExecutor() {
        }

        private static void run(Runnable runnable) {
            net.minecraft.client.Minecraft.getInstance().execute(runnable);
        }

        private static <T> T call(Supplier<T> supplier) {
            net.minecraft.client.Minecraft minecraft = net.minecraft.client.Minecraft.getInstance();
            return minecraft.isSameThread() ? supplier.get() : minecraft.submit(supplier).join();
        }
    }
}
