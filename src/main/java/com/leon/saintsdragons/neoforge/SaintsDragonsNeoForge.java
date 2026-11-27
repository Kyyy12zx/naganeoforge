package com.leon.saintsdragons.neoforge;

import com.leon.saintsdragons.common.SaintsDragonsCommon;
import com.leon.saintsdragons.common.config.SaintsDragonsConfig;
import com.leon.saintsdragons.common.config.ToolsArmorConfig;
import com.leon.saintsdragons.common.config.dragon.DragonAttributeConfigLoader;
import com.leon.saintsdragons.common.network.MessageCameraImpulse;
import com.leon.saintsdragons.common.network.MessageDragonPartAttack;
import com.leon.saintsdragons.common.network.MessageDragonAbilityDebugBox;
import com.leon.saintsdragons.common.network.MessageDragonMovingSound;
import com.leon.saintsdragons.common.network.MessageDragonlordFlightBoost;
import com.leon.saintsdragons.common.registry.ModAttributes;
import com.leon.saintsdragons.common.registry.ModMemoryTypes;
import com.leon.saintsdragons.common.registry.ModSounds;
import com.leon.saintsdragons.server.entity.variant.DragonVariantReloadListener;
import com.leon.saintsdragons.neoforge.platform.NeoForgePlatformHelper;
import com.leon.saintsdragons.platform.Services;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(SaintsDragonsNeoForge.MOD_ID)
public final class SaintsDragonsNeoForge {
    public static final String MOD_ID = "saintsdragons";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public SaintsDragonsNeoForge(IEventBus modEventBus, ModContainer modContainer) {
        NeoForgePlatformHelper.configure(modEventBus, modContainer);
        Services.PLATFORM.getNetworkHelper().registerClientbound(
                MessageCameraImpulse.class,
                SaintsDragonsCommon.rl("camera_impulse"),
                MessageCameraImpulse::encode,
                MessageCameraImpulse::decode,
                MessageCameraImpulse::handle
        );
        Services.PLATFORM.getNetworkHelper().registerClientbound(
                MessageDragonMovingSound.class,
                SaintsDragonsCommon.rl("dragon_moving_sound"),
                MessageDragonMovingSound::encode,
                MessageDragonMovingSound::decode,
                MessageDragonMovingSound::handle
        );
        Services.PLATFORM.getNetworkHelper().registerClientbound(
                MessageDragonlordFlightBoost.class,
                SaintsDragonsCommon.rl("dragonlord_flight_boost"),
                MessageDragonlordFlightBoost::encode,
                MessageDragonlordFlightBoost::decode,
                MessageDragonlordFlightBoost::handle
        );
        Services.PLATFORM.getNetworkHelper().registerClientbound(
                MessageDragonAbilityDebugBox.class,
                SaintsDragonsCommon.rl("dragon_ability_debug_box"),
                MessageDragonAbilityDebugBox::encode,
                MessageDragonAbilityDebugBox::decode,
                MessageDragonAbilityDebugBox::handle
        );
        Services.PLATFORM.getNetworkHelper().registerServerbound(
                MessageDragonPartAttack.class,
                SaintsDragonsCommon.rl("dragon_part_attack"),
                MessageDragonPartAttack::encode,
                MessageDragonPartAttack::decode,
                MessageDragonPartAttack::handle
        );
        ModAttributes.register(modEventBus);
        ModMemoryTypes.register(modEventBus);
        ModSounds.register(modEventBus);
        modEventBus.addListener(this::commonSetup);
        NeoForge.EVENT_BUS.addListener(this::addReloadListeners);
        LOGGER.info("Saint's Dragons NeoForge bootstrap loaded");
    }

    private void addReloadListeners(AddReloadListenerEvent event) {
        event.addListener(DragonAttributeConfigLoader.getInstance());
        event.addListener(DragonVariantReloadListener.getInstance());
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        SaintsDragonsConfig.bootstrap();
        ToolsArmorConfig.bootstrap();
        DragonAttributeConfigLoader.bootstrap();
        LOGGER.info("Saint's Dragons NeoForge common setup");
    }
}
