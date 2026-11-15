package com.leon.saintsdragons.neoforge.platform;

import com.leon.saintsdragons.platform.NetworkHelper;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/** NeoForge custom-payload implementation of the shared packet abstraction. */
public final class NeoForgeNetworkHelper implements NetworkHelper {
    private final List<PayloadRegistration<?>> registrations = new ArrayList<>();
    private final Set<ResourceLocation> ids = new HashSet<>();
    private boolean registrationEventFired;

    public NeoForgeNetworkHelper(IEventBus modEventBus) {
        modEventBus.addListener(this::registerPayloads);
    }

    @Override
    public <T> void registerServerbound(Class<T> type, ResourceLocation id,
                                        PacketEncoder<T> encoder, PacketDecoder<T> decoder,
                                        ServerboundHandler<T> handler) {
        add(new PayloadRegistration<>(type, id, encoder, decoder,
                (message, context) -> handler.handle(message, (ServerPlayer) context.player()),
                null));
    }

    @Override
    public <T> void registerClientbound(Class<T> type, ResourceLocation id,
                                        PacketEncoder<T> encoder, PacketDecoder<T> decoder,
                                        ClientboundHandler<T> handler) {
        add(new PayloadRegistration<>(type, id, encoder, decoder, null,
                (message, context) -> handler.handle(message)));
    }

    private void add(PayloadRegistration<?> registration) {
        if (registrationEventFired) {
            throw new IllegalStateException("Packets must be registered before NeoForge payload registration");
        }
        if (!ids.add(registration.id)) {
            throw new IllegalArgumentException("Duplicate payload id: " + registration.id);
        }
        registrations.add(registration);
    }

    private void registerPayloads(RegisterPayloadHandlersEvent event) {
        registrationEventFired = true;
        PayloadRegistrar registrar = event.registrar("2");
        for (PayloadRegistration<?> registration : registrations) {
            registration.register(registrar);
        }
    }

    @Override
    public void sendToServer(Object message) {
        PacketDistributor.sendToServer(payloadFor(message));
    }

    @Override
    public void sendToPlayer(ServerPlayer player, Object message) {
        PacketDistributor.sendToPlayer(player, payloadFor(message));
    }

    @Override
    public void sendToTracking(Entity entity, Object message) {
        PacketDistributor.sendToPlayersTrackingEntity(entity, payloadFor(message));
    }

    @Override
    public void sendToDimension(Level level, Object message) {
        if (!(level instanceof ServerLevel serverLevel)) {
            throw new IllegalArgumentException("Dimension packets can only be sent from a ServerLevel");
        }
        PacketDistributor.sendToPlayersInDimension(serverLevel, payloadFor(message));
    }

    private CustomPacketPayload payloadFor(Object message) {
        for (PayloadRegistration<?> registration : registrations) {
            if (registration.messageType.isInstance(message)) {
                return registration.wrap(message);
            }
        }
        throw new IllegalArgumentException("Unregistered packet class: " + message.getClass().getName());
    }

    private static final class Payload<T> implements CustomPacketPayload {
        private final Type<Payload<T>> type;
        private final T message;

        private Payload(Type<Payload<T>> type, T message) {
            this.type = type;
            this.message = message;
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return type;
        }
    }

    private static final class PayloadRegistration<T> {
        private final Class<T> messageType;
        private final ResourceLocation id;
        private final PacketEncoder<T> encoder;
        private final PacketDecoder<T> decoder;
        private final ServerboundHandler<T> serverHandler;
        private final ClientboundHandler<T> clientHandler;
        private final Type<Payload<T>> payloadType;
        private final StreamCodec<RegistryFriendlyByteBuf, Payload<T>> codec;

        private PayloadRegistration(Class<T> messageType, ResourceLocation id,
                                    PacketEncoder<T> encoder, PacketDecoder<T> decoder,
                                    ServerboundHandler<T> serverHandler,
                                    ClientboundHandler<T> clientHandler) {
            this.messageType = messageType;
            this.id = id;
            this.encoder = encoder;
            this.decoder = decoder;
            this.serverHandler = serverHandler;
            this.clientHandler = clientHandler;
            this.payloadType = new Type<>(id);
            this.codec = StreamCodec.of(
                    (RegistryFriendlyByteBuf buffer, Payload<T> payload) -> encoder.encode(payload.message, buffer),
                    (RegistryFriendlyByteBuf buffer) -> new Payload<>(payloadType, decoder.decode(buffer))
            );
        }

        private Payload<T> wrap(Object message) {
            return new Payload<>(payloadType, messageType.cast(message));
        }

        private void register(PayloadRegistrar registrar) {
            registrar.playBidirectional(payloadType, codec, this::handle);
        }

        private void handle(Payload<T> payload, IPayloadContext context) {
            if (context.flow() == PacketFlow.SERVERBOUND && serverHandler != null) {
                serverHandler.handle(payload.message, context);
            } else if (context.flow() == PacketFlow.CLIENTBOUND && clientHandler != null) {
                clientHandler.handle(payload.message, context);
            }
        }
    }
}
