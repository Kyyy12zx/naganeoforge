package com.leon.saintsdragons.neoforge;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
@Mod(SaintsDragonsNeoForge.MOD_ID)
public final class SaintsDragonsNeoForge {
    public static final String MOD_ID = "saintsdragons";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public SaintsDragonsNeoForge(IEventBus modEventBus) {
        modEventBus.addListener(this::commonSetup);
        LOGGER.info("Saint's Dragons NeoForge bootstrap loaded");
    }
    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("Saint's Dragons NeoForge common setup");
    }
}
