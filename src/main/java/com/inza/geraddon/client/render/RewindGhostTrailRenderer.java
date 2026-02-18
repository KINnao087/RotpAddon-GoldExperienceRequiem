package com.inza.geraddon.client.render;

import com.inza.geraddon.AddonMain;
import com.inza.geraddon.network.packet.RewindGhostTrailsS2CPacket;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mod.EventBusSubscriber(modid = AddonMain.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class  RewindGhostTrailRenderer {
    private static final Map<ResourceLocation, List<Trail>> TRAILS_BY_DIMENSION = new HashMap<>();
    private static final double OVERLAP_EPSILON = 0.12D;

    private RewindGhostTrailRenderer() {}

    public static void apply(RewindGhostTrailsS2CPacket packet) {
        List<Trail> trails = new ArrayList<>(packet.trails.size());
        for (RewindGhostTrailsS2CPacket.Trail trail : packet.trails) {
            trails.add(new Trail(trail.entityId, trail.poses));
        }
        TRAILS_BY_DIMENSION.put(packet.dimension, trails);
    }

    public static void clear(ResourceLocation dimension) {
        TRAILS_BY_DIMENSION.remove(dimension);
    }

    @SubscribeEvent
    public static void onRenderWorldLast(RenderWorldLastEvent event) {
        Minecraft mc = Minecraft.getInstance();
        ClientWorld world = mc.level;
        if (world == null || mc.getEntityRenderDispatcher() == null) {
            return;
        }
        List<Trail> trails = TRAILS_BY_DIMENSION.get(world.dimension().location());
        if (trails == null || trails.isEmpty()) {
            return;
        }

        Vector3d camera = mc.gameRenderer.getMainCamera().getPosition();
        MatrixStack matrixStack = event.getMatrixStack();
        IRenderTypeBuffer.Impl buffers = mc.renderBuffers().bufferSource();
        float partialTicks = event.getPartialTicks();

        trails.removeIf(trail -> trail.poses.isEmpty());
        for (Trail trail : trails) {
            Entity source = world.getEntity(trail.entityId);
            if (source == null || !source.isAlive()) {
                continue;
            }
            trail.removePassedBy(source);
            if (trail.poses.isEmpty()) {
                continue;
            }
            for (RewindGhostTrailsS2CPacket.Pose pose : trail.poses) {
                int light = WorldRenderer.getLightColor(world, new BlockPos(pose.x, pose.y, pose.z));
                matrixStack.pushPose();
                mc.getEntityRenderDispatcher().render(
                        source,
                        pose.x - camera.x,
                        pose.y - camera.y,
                        pose.z - camera.z,
                        pose.yRot,
                        partialTicks,
                        matrixStack,
                        buffers,
                        light
                );
                matrixStack.popPose();
            }
        }
        buffers.endBatch();
    }

    @SubscribeEvent
    public static void onClientWorldUnload(WorldEvent.Unload event) {
        if (event.getWorld().isClientSide()) {
            TRAILS_BY_DIMENSION.clear();
        }
    }

    private static final class Trail {
        private final int entityId;
        private final List<RewindGhostTrailsS2CPacket.Pose> poses;

        private Trail(int entityId, List<RewindGhostTrailsS2CPacket.Pose> poses) {
            this.entityId = entityId;
            this.poses = new ArrayList<>(poses);
        }

        private void removePassedBy(Entity source) {
            double sx = source.getX();
            double sy = source.getY();
            double sz = source.getZ();
            poses.removeIf(pose -> nearlyOverlapped(sx, sy, sz, pose));
        }

        private static boolean nearlyOverlapped(double sx, double sy, double sz, RewindGhostTrailsS2CPacket.Pose pose) {
            return Math.abs(sx - pose.x) <= OVERLAP_EPSILON
                    && Math.abs(sy - pose.y) <= OVERLAP_EPSILON
                    && Math.abs(sz - pose.z) <= OVERLAP_EPSILON;
        }
    }
}
