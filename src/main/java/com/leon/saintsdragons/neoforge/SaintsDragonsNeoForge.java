package com.leon.saintsdragons.neoforge;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import com.leon.saintsdragons.common.config.SaintsDragonsConfig;
import com.leon.saintsdragons.common.config.ToolsArmorConfig;
import com.leon.saintsdragons.common.config.dragon.DragonAttributeConfigLoader;
import com.leon.saintsdragons.common.registry.ModAttributes;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;
import com.leon.saintsdragons.neoforge.platform.NeoForgeConfigHelper;
import com.leon.saintsdragons.neoforge.platform.NeoForgeNetworkHelper;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
@Mod(SaintsDragonsNeoForge.MOD_ID)
public final class SaintsDragonsNeoForge {
    public static final String MOD_ID = "saintsdragons";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public SaintsDragonsNeoForge(IEventBus modEventBus, ModContainer modContainer) {
        new NeoForgeConfigHelper(modContainer);
        new NeoForgeNetworkHelper(modEventBus);
        ModAttributes.register(modEventBus);
        modEventBus.addListener(this::commonSetup);
        NeoForge.EVENT_BUS.addListener(this::addReloadListeners);
        LOGGER.info("Saint's Dragons NeoForge bootstrap loaded");
    }
    private void addReloadListeners(final AddReloadListenerEvent event) {
        event.addListener(DragonAttributeConfigLoader.getInstance());
    }
    private void commonSetup(final FMLCommonSetupEvent event) {
        SaintsDragonsConfig.bootstrap();
        ToolsArmorConfig.bootstrap();
        DragonAttributeConfigLoader.bootstrap();
        LOGGER.info("Saint's Dragons NeoForge common setup");
    }
}
