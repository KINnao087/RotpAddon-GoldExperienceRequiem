package com.inza.geraddon.network;

import com.inza.geraddon.AddonMain;
import com.inza.geraddon.network.packet.ClearRewindGhostTrailsS2CPacket;
import com.inza.geraddon.network.packet.RewindGhostTrailsS2CPacket;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.simple.SimpleChannel;

import java.util.Optional;

public final class NetworkHandler {
    private static final String PROTOCOL_VERSION = "1";
    private static final SimpleChannel CHANNEL = NetworkRegistry.ChannelBuilder
            .named(new ResourceLocation(AddonMain.MOD_ID, "main"))
            .networkProtocolVersion(() -> PROTOCOL_VERSION)
            .clientAcceptedVersions(PROTOCOL_VERSION::equals)
            .serverAcceptedVersions(PROTOCOL_VERSION::equals)
            .simpleChannel();
    private static int packetId = 0;
    private static boolean initialized = false;

    private NetworkHandler() {}

    public static void init() {
        if (initialized) {
            return;
        }
        initialized = true;

        CHANNEL.registerMessage(
                packetId++,
                RewindGhostTrailsS2CPacket.class,
                RewindGhostTrailsS2CPacket::encode,
                RewindGhostTrailsS2CPacket::decode,
                RewindGhostTrailsS2CPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT)
        );
        CHANNEL.registerMessage(
                packetId++,
                ClearRewindGhostTrailsS2CPacket.class,
                ClearRewindGhostTrailsS2CPacket::encode,
                ClearRewindGhostTrailsS2CPacket::decode,
                ClearRewindGhostTrailsS2CPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT)
        );
    }

    public static void sendToDimension(ServerWorld world, Object packet) {
        CHANNEL.send(PacketDistributor.DIMENSION.with(world::dimension), packet);
    }
}
