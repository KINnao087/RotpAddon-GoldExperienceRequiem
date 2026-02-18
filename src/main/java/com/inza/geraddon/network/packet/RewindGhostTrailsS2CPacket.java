package com.inza.geraddon.network.packet;

import com.inza.geraddon.client.render.RewindGhostTrailRenderer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

public class RewindGhostTrailsS2CPacket {
    public final ResourceLocation dimension;
    public final List<Trail> trails;

    public RewindGhostTrailsS2CPacket(ResourceLocation dimension, List<Trail> trails) {
        this.dimension = dimension;
        this.trails = Collections.unmodifiableList(new ArrayList<>(trails));
    }

    public static void encode(RewindGhostTrailsS2CPacket packet, PacketBuffer buf) {
        buf.writeResourceLocation(packet.dimension);
        buf.writeVarInt(packet.trails.size());
        for (Trail trail : packet.trails) {
            buf.writeVarInt(trail.entityId);
            buf.writeVarInt(trail.poses.size());
            for (Pose pose : trail.poses) {
                buf.writeDouble(pose.x);
                buf.writeDouble(pose.y);
                buf.writeDouble(pose.z);
                buf.writeFloat(pose.yRot);
                buf.writeFloat(pose.xRot);
            }
        }
    }

    public static RewindGhostTrailsS2CPacket decode(PacketBuffer buf) {
        ResourceLocation dimension = buf.readResourceLocation();
        int trailCount = buf.readVarInt();
        List<Trail> trails = new ArrayList<>(trailCount);
        for (int i = 0; i < trailCount; i++) {
            int entityId = buf.readVarInt();
            int poseCount = buf.readVarInt();
            List<Pose> poses = new ArrayList<>(poseCount);
            for (int j = 0; j < poseCount; j++) {
                poses.add(new Pose(
                        buf.readDouble(),
                        buf.readDouble(),
                        buf.readDouble(),
                        buf.readFloat(),
                        buf.readFloat()
                ));
            }
            trails.add(new Trail(entityId, poses));
        }
        return new RewindGhostTrailsS2CPacket(dimension, trails);
    }

    public static void handle(RewindGhostTrailsS2CPacket packet, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> RewindGhostTrailRenderer.apply(packet)));
        ctx.setPacketHandled(true);
    }

    public static final class Trail {
        public final int entityId;
        public final List<Pose> poses;

        public Trail(int entityId, List<Pose> poses) {
            this.entityId = entityId;
            this.poses = Collections.unmodifiableList(new ArrayList<>(poses));
        }
    }

    public static final class Pose {
        public final double x;
        public final double y;
        public final double z;
        public final float yRot;
        public final float xRot;

        public Pose(double x, double y, double z, float yRot, float xRot) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.yRot = yRot;
            this.xRot = xRot;
        }
    }
}
