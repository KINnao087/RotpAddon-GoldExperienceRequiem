package com.inza.geraddon.network.packet;

import com.inza.geraddon.client.render.RewindGhostTrailRenderer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class ClearRewindGhostTrailsS2CPacket {
    public final ResourceLocation dimension;

    public ClearRewindGhostTrailsS2CPacket(ResourceLocation dimension) {
        this.dimension = dimension;
    }

    public static void encode(ClearRewindGhostTrailsS2CPacket packet, PacketBuffer buf) {
        buf.writeResourceLocation(packet.dimension);
    }

    public static ClearRewindGhostTrailsS2CPacket decode(PacketBuffer buf) {
        return new ClearRewindGhostTrailsS2CPacket(buf.readResourceLocation());
    }

    public static void handle(ClearRewindGhostTrailsS2CPacket packet, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> RewindGhostTrailRenderer.clear(packet.dimension)));
        ctx.setPacketHandled(true);
    }
}
