package com.leon.saintsdragons.neoforge.platform;

import com.leon.saintsdragons.platform.RegistryHelper;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

/** NeoForge implementation of the shared deferred-registry abstraction. */
public final class NeoForgeRegistryHelper implements RegistryHelper {
    private final IEventBus modEventBus;

    public NeoForgeRegistryHelper(IEventBus modEventBus) {
        this.modEventBus = modEventBus;
    }

    @Override
    public <T> RegistryWrapper<T> create(ResourceKey<? extends Registry<T>> registryKey,
                                         Supplier<Registry<T>> backingRegistry,
                                         String modId) {
        return new Wrapper<>(DeferredRegister.create(registryKey, modId), modEventBus);
    }

    private static final class Wrapper<T> implements RegistryWrapper<T> {
        private final DeferredRegister<T> register;
        private final IEventBus modEventBus;
        private boolean attached;

        private Wrapper(DeferredRegister<T> register, IEventBus modEventBus) {
            this.register = register;
            this.modEventBus = modEventBus;
        }

        @Override
        public <I extends T> Supplier<I> register(String name, Supplier<I> supplier) {
            return register.register(name, supplier);
        }

        @Override
        public void register() {
            if (!attached) {
                register.register(modEventBus);
                attached = true;
            }
        }
    }
}
