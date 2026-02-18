package com.inza.geraddon.TimeRecoder;

import com.inza.geraddon.entity.GoldExprienceRequiemEntity;
import com.inza.geraddon.network.NetworkHandler;
import com.inza.geraddon.network.packet.ClearRewindGhostTrailsS2CPacket;
import com.inza.geraddon.network.packet.RewindGhostTrailsS2CPacket;
import net.minecraft.block.BlockState;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.DoubleNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class BytecodeRewindManager {
    private static final int MAX_BLOCK_OPS = 200_000;
    private static final int ENTITY_RECORD_INTERVAL_TICKS = 10;
    private static final int MAX_ENTITY_FRAMES = 1200;
    private static final String GHOST_TAG = "ger_rewind_ghost";
    private static final int MAX_GHOST_TRAIL_POINTS_PER_ENTITY = 120;
    private static final double GHOST_TRAIL_MIN_DISTANCE = 0.75D;
    private static final double GHOST_TRAIL_MIN_DISTANCE_SQR = GHOST_TRAIL_MIN_DISTANCE * GHOST_TRAIL_MIN_DISTANCE;
    private static final Map<RegistryKey<World>, WorldLog> LOGS = new ConcurrentHashMap<>();

    private BytecodeRewindManager() {}

    public static void recordBlockChange(ServerWorld world, long tick, BlockPos pos, CompoundNBT oldStateNbt, CompoundNBT oldBlockEntityNbt) {
        WorldLog log = LOGS.computeIfAbsent(world.dimension(), key -> new WorldLog());
        if (log.rewinding || !log.recording) {
            return;
        }
        BlockOp op = new BlockOp(pos.immutable(), oldStateNbt.copy(), oldBlockEntityNbt == null ? null : oldBlockEntityNbt.copy());
        log.opsByTick.computeIfAbsent(tick, k -> new ArrayList<>()).add(op);
        log.blockOpCount++;
        trimOldOps(log);
    }

    public static boolean isRewinding(ServerWorld world) {
        WorldLog log = LOGS.get(world.dimension());
        return log != null && log.rewinding;
    }

    public static void recordEntityPositions(ServerWorld world) {
        WorldLog log = LOGS.get(world.dimension());
        if (log == null || !log.recording || log.rewinding) {
            return;
        }
        long tick = world.getGameTime();
        if (tick % ENTITY_RECORD_INTERVAL_TICKS != 0) {
            return;
        }

        Map<UUID, Pose> frame = new HashMap<>();
        for (Entity entity : world.getAllEntities()) {
            if (!entity.isAlive()) {
                continue;
            }
            frame.put(entity.getUUID(), Pose.of(entity));
        }
        log.entityFramesByTick.put(tick, frame);
        while (log.entityFramesByTick.size() > MAX_ENTITY_FRAMES) {
            log.entityFramesByTick.pollFirstEntry();
        }
    }

    public static Mark mark(ServerWorld world, BlockPos center, int chunkRadius) {
        WorldLog log = LOGS.computeIfAbsent(world.dimension(), key -> new WorldLog());
        int radius = Math.max(0, chunkRadius);
        Mark mark = buildMark(world, world.getGameTime(), center, radius, false);
        log.marksByTick.put(mark.tick, mark);
        log.recording = true;
        return mark;
    }

    public static Mark startTimeStopSession(ServerWorld world, BlockPos center, int chunkRadius) {
        WorldLog log = LOGS.computeIfAbsent(world.dimension(), key -> new WorldLog());
        if (log.activeSessionMarkTick >= 0) {
            log.marksByTick.remove(log.activeSessionMarkTick);
        }
        int radius = Math.max(0, chunkRadius);
        long startTick = world.getGameTime();
        Mark mark = buildMark(world, startTick, center, radius, true);
        log.marksByTick.put(mark.tick, mark);
        log.recording = true;
        log.activeSessionStartTick = startTick;
        log.activeSessionMarkTick = mark.tick;
        return mark;
    }

    public static Optional<Mark> latestMark(ServerWorld world) {
        WorldLog log = LOGS.get(world.dimension());
        if (log == null || log.marksByTick.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(log.marksByTick.lastEntry().getValue());
    }

    // ===== CRUD for mark map (key=tick, value=mark) =====

    public static Optional<Mark> getMark(ServerWorld world, long tick) {
        WorldLog log = LOGS.get(world.dimension());
        if (log == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(log.marksByTick.get(tick));
    }

    public static NavigableMap<Long, Mark> getAllMarks(ServerWorld world) {
        WorldLog log = LOGS.get(world.dimension());
        if (log == null || log.marksByTick.isEmpty()) {
            return Collections.emptyNavigableMap();
        }
        return Collections.unmodifiableNavigableMap(new TreeMap<>(log.marksByTick));
    }

    public static Mark addMark(ServerWorld world, long tick, BlockPos center, int chunkRadius) {
        WorldLog log = LOGS.computeIfAbsent(world.dimension(), key -> new WorldLog());
        int radius = Math.max(0, chunkRadius);
        Mark mark = buildMark(world, tick, center, radius, false);
        log.marksByTick.put(tick, mark);
        log.recording = true;
        return mark;
    }

    public static Optional<Mark> updateMark(ServerWorld world, long tick, BlockPos newCenter, int newChunkRadius) {
        WorldLog log = LOGS.get(world.dimension());
        if (log == null || !log.marksByTick.containsKey(tick)) {
            return Optional.empty();
        }
        Mark old = log.marksByTick.get(tick);
        int radius = Math.max(0, newChunkRadius);
        Mark updated = buildMark(world, tick, newCenter, radius, old.oneShot);
        log.marksByTick.put(tick, updated);
        return Optional.of(updated);
    }

    public static boolean removeMark(ServerWorld world, long tick) {
        WorldLog log = LOGS.get(world.dimension());
        if (log == null) {
            return false;
        }
        Mark removed = log.marksByTick.remove(tick);
        if (removed == null) {
            return false;
        }
        if (log.returnToZeroMarkTick == tick) {
            log.returnToZeroMarkTick = -1;
        }
        if (log.activeSessionMarkTick == tick) {
            log.activeSessionMarkTick = -1;
            log.activeSessionStartTick = -1;
        }
        return true;
    }

    public static int clearMarks(ServerWorld world) {
        WorldLog log = LOGS.get(world.dimension());
        if (log == null) {
            return 0;
        }
        int size = log.marksByTick.size();
        log.marksByTick.clear();
        log.returnToZeroMarkTick = -1;
        log.activeSessionMarkTick = -1;
        log.activeSessionStartTick = -1;
        log.entityFramesByTick.clear();
        log.activeEntityRewindTask = null;
        log.lastRewindResult = null;
        cleanupGhosts(world, log, true);
        clearClientGhostTrails(world);
        return size;
    }

    public static boolean hasAvailableMark(World world) {
        WorldLog log = LOGS.get(world.dimension());
        return log != null && !log.marksByTick.isEmpty();
    }

    public static Mark setReturnToZeroMark(ServerWorld world, BlockPos center, int chunkRadius) {
        WorldLog log = LOGS.computeIfAbsent(world.dimension(), key -> new WorldLog());
        Mark mark = mark(world, center, chunkRadius);
        log.returnToZeroMarkTick = mark.tick;
        return mark;
    }

    public static boolean hasReturnToZeroMark(World world) {
        WorldLog log = LOGS.get(world.dimension());
        return log != null && log.returnToZeroMarkTick >= 0 && log.marksByTick.containsKey(log.returnToZeroMarkTick);
    }

    public static long getReturnToZeroMarkTick(ServerWorld world) {
        WorldLog log = LOGS.get(world.dimension());
        return log == null ? -1 : log.returnToZeroMarkTick;
    }

    public static RewindResult rewindToReturnToZeroMark(ServerWorld world) {
        return rewindToReturnToZeroMark(world, null);
    }

    public static RewindResult rewindToReturnToZeroMark(ServerWorld world, UUID initiatorUuid) {
        WorldLog log = LOGS.get(world.dimension());
        if (log == null || log.returnToZeroMarkTick < 0) {
            return new RewindResult(false, -1, 0, 0, 0, "No ReturnToZero mark set.");
        }
        Mark mark = log.marksByTick.get(log.returnToZeroMarkTick);
        if (mark == null) {
            log.returnToZeroMarkTick = -1;
            return new RewindResult(false, -1, 0, 0, 0, "ReturnToZero mark expired.");
        }
        return startRewindToTick(world, mark.tick, Optional.of(mark), initiatorUuid, false);
    }

    public static RewindResult rewindToLatestMark(ServerWorld world) {
        return rewindToLatestMark(world, null);
    }

    public static RewindResult rewindToLatestMark(ServerWorld world, UUID initiatorUuid) {
        Optional<Mark> markOpt = latestMark(world);
        if (!markOpt.isPresent()) {
            return new RewindResult(false, -1, 0, 0, 0, "No mark recorded in this dimension.");
        }
        Mark mark = markOpt.get();
        return startRewindToTick(world, mark.tick, Optional.of(mark), initiatorUuid, true);
    }

    public static RewindResult rewindActiveSession(ServerWorld world) {
        return rewindActiveSession(world, null);
    }

    public static RewindResult rewindActiveSession(ServerWorld world, UUID initiatorUuid) {
        WorldLog log = LOGS.get(world.dimension());
        if (log == null || log.activeSessionStartTick < 0) {
            return rewindToLatestMark(world, initiatorUuid);
        }
        Optional<Mark> mark = Optional.ofNullable(log.marksByTick.get(log.activeSessionMarkTick));
        return startRewindToTick(world, log.activeSessionStartTick, mark, initiatorUuid, false);
    }

    public static long getActiveSessionOldestTick(ServerWorld world) {
        WorldLog log = LOGS.get(world.dimension());
        return log == null ? -1 : log.activeSessionStartTick;
    }

    public static RewindResult rewindToTick(ServerWorld world, long targetTick) {
        return rewindToTick(world, targetTick, null);
    }

    public static RewindResult rewindToTick(ServerWorld world, long targetTick, UUID initiatorUuid) {
        WorldLog log = LOGS.get(world.dimension());
        Optional<Mark> mark = Optional.empty();
        if (log != null) {
            Map.Entry<Long, Mark> nearestMark = log.marksByTick.floorEntry(targetTick);
            if (nearestMark != null) {
                mark = Optional.of(nearestMark.getValue());
            }
        }
        return startRewindToTick(world, targetTick, mark, initiatorUuid, false);
    }

    public static RewindResult rewindToMark(ServerWorld world, long markTick) {
        return rewindToMark(world, markTick, null);
    }

    public static RewindResult rewindToMark(ServerWorld world, long markTick, UUID initiatorUuid) {
        WorldLog log = LOGS.get(world.dimension());
        if (log == null) {
            return new RewindResult(false, markTick, 0, 0, 0, "No marks in this dimension.");
        }
        Mark mark = log.marksByTick.get(markTick);
        if (mark == null) {
            return new RewindResult(false, markTick, 0, 0, 0, "Mark not found at tick=" + markTick);
        }
        return startRewindToTick(world, markTick, Optional.of(mark), initiatorUuid, false);
    }

    public static DebugInfo debugInfo(ServerWorld world) {
        WorldLog log = LOGS.get(world.dimension());
        if (log == null) {
            return new DebugInfo(0, 0, -1, false, false);
        }
        long latestMarkTick = log.marksByTick.isEmpty() ? -1 : log.marksByTick.lastKey();
        return new DebugInfo(log.blockOpCount, log.marksByTick.size(), latestMarkTick, log.rewinding, log.recording);
    }

    private static RewindResult startRewindToTick(ServerWorld world, long targetTick, Optional<Mark> scopedMark, UUID initiatorUuid, boolean removeScopedMarkOnComplete) {
        WorldLog log = LOGS.get(world.dimension());
        if (log == null) {
            return new RewindResult(false, targetTick, 0, 0, 0, "No block operations recorded.");
        }
        if (log.activeEntityRewindTask != null) {
            return new RewindResult(false, targetTick, 0, 0, 0, "Rewind already in progress.");
        }
        if (log.opsByTick.isEmpty() && !scopedMark.isPresent()) {
            return new RewindResult(false, targetTick, 0, 0, 0, "No block operations recorded.");
        }

        int revertedBlocks = 0;
        if (!log.opsByTick.isEmpty()) {
            NavigableMap<Long, List<BlockOp>> tail = log.opsByTick.tailMap(targetTick, false);
            List<Long> ticks = new ArrayList<>(tail.keySet());
            Collections.reverse(ticks);
            for (Long tick : ticks) {
                List<BlockOp> ops = log.opsByTick.get(tick);
                if (ops == null) {
                    continue;
                }
                for (int i = ops.size() - 1; i >= 0; i--) {
                    BlockOp op = ops.get(i);
                    if (scopedMark.isPresent() && !inChunkRadius(op.pos, scopedMark.get().center, scopedMark.get().chunkRadius)) {
                        continue;
                    }
                    applyOldState(world, op);
                    revertedBlocks++;
                }
            }
        }

        if (revertedBlocks == 0 && !scopedMark.isPresent()) {
            return new RewindResult(false, targetTick, 0, 0, 0, "No changes to rewind.");
        }

        if (!scopedMark.isPresent()) {
            pruneAfterTick(log, targetTick);
            log.recording = false;
            clearClientGhostTrails(world);
            log.rewinding = false;
            return new RewindResult(true, targetTick, revertedBlocks, 0, 0, "Rewind complete.");
        }

        Mark mark = scopedMark.get();
        EntityRewindTask task = buildEntityRewindTask(world, log, mark, targetTick, initiatorUuid, revertedBlocks);
        if (task.isEmpty()) {
            int restoredEntities = restoreEntities(world, mark, initiatorUuid);
            int movedPlayers = restorePlayerPositions(world, mark, initiatorUuid);
            restoreStandPositions(world, mark, initiatorUuid);
            world.setDayTime(mark.dayTime);
            pruneAfterTick(log, targetTick);
            if (removeScopedMarkOnComplete) {
                removeMarkKeepRecording(log, mark.tick);
            } else if (mark.oneShot) {
                consumeOneShotMark(log, mark);
            }
            log.recording = false;
            restartEntityTrajectoryRecording(world, log);
            clearClientGhostTrails(world);
            log.rewinding = false;
            return new RewindResult(true, targetTick, revertedBlocks, restoredEntities, movedPlayers, "Rewind complete.");
        }

        log.rewinding = true;
        log.activeEntityRewindTask = new EntityRewindTask(task.mark, task.targetTick, task.currentTick, task.initiatorUuid, task.revertedBlocks, task.paths, removeScopedMarkOnComplete);
        spawnPathGhostsForTask(world, log, task);
        return new RewindResult(true, targetTick, revertedBlocks, 0, 0, "Rewind started.");
    }

    public static void tickRewind(ServerWorld world) {
        WorldLog log = LOGS.get(world.dimension());
        if (log == null) {
            cleanupTaggedGhosts(world);
            return;
        }
        if (log.activeEntityRewindTask == null) {
            cleanupGhosts(world, log, true);
            return;
        }

        EntityRewindTask task = log.activeEntityRewindTask;
        long applyTick = task.currentTick - 1;
        if (applyTick < task.targetTick) {
            int restoredEntities = restoreEntities(world, task.mark, task.initiatorUuid);
            int movedPlayers = restorePlayerPositions(world, task.mark, task.initiatorUuid);
            restoreStandPositions(world, task.mark, task.initiatorUuid);
            world.setDayTime(task.mark.dayTime);

            pruneAfterTick(log, task.targetTick);
            if (task.removeScopedMarkOnComplete) {
                removeMarkKeepRecording(log, task.mark.tick);
            } else if (task.mark.oneShot) {
                consumeOneShotMark(log, task.mark);
            }
            log.recording = false;
            restartEntityTrajectoryRecording(world, log);
            cleanupGhosts(world, log, true);
            clearClientGhostTrails(world);
            log.rewinding = false;
            log.activeEntityRewindTask = null;
            log.lastRewindResult = new RewindResult(true, task.targetTick, task.revertedBlocks, restoredEntities, movedPlayers, "Rewind complete.");
            return;
        }

        for (EntityPath path : task.paths.values()) {
            Pose next = path.popNextPose();
            if (next == null) {
                continue;
            }
            Entity entity = resolveEntity(world, path.uuid);
            if (entity == null || !entity.isAlive()) {
                continue;
            }
            if (path.uuid.equals(task.initiatorUuid)) {
                continue;
            }

            resetRewindPhysics(entity);
            moveEntityToPose(world, entity, next);
            resetRewindPhysics(entity);
        }
        task.currentTick = applyTick;
    }

    public static Optional<RewindResult> pollLastCompletedRewind(ServerWorld world) {
        WorldLog log = LOGS.get(world.dimension());
        if (log == null || log.lastRewindResult == null) {
            return Optional.empty();
        }
        RewindResult result = log.lastRewindResult;
        log.lastRewindResult = null;
        return Optional.of(result);
    }

    public static void cleanupOrphanGhosts(ServerWorld world) {
        WorldLog log = LOGS.get(world.dimension());
        if (log == null || log.activeEntityRewindTask == null) {
            cleanupTaggedGhosts(world);
        }
    }

    private static boolean inChunkRadius(BlockPos pos, BlockPos center, int radius) {
        int chunkX = pos.getX() >> 4;
        int chunkZ = pos.getZ() >> 4;
        int centerChunkX = center.getX() >> 4;
        int centerChunkZ = center.getZ() >> 4;
        return Math.abs(chunkX - centerChunkX) <= radius && Math.abs(chunkZ - centerChunkZ) <= radius;
    }

    private static void applyOldState(ServerWorld world, BlockOp op) {
        BlockState oldState = NBTUtil.readBlockState(op.oldStateNbt);
        world.setBlock(op.pos, oldState, 3);
        if (op.oldBlockEntityNbt != null) {
            TileEntity tileEntity = world.getBlockEntity(op.pos);
            if (tileEntity != null) {
                CompoundNBT nbt = op.oldBlockEntityNbt.copy();
                nbt.putInt("x", op.pos.getX());
                nbt.putInt("y", op.pos.getY());
                nbt.putInt("z", op.pos.getZ());
                tileEntity.load(oldState, nbt);
                tileEntity.setChanged();
            }
        }
    }

    private static List<EntitySnapshot> captureEntities(ServerWorld world, BlockPos center, int chunkRadius) {
        AxisAlignedBB area = areaForChunkRadius(center, chunkRadius);
        List<EntitySnapshot> snapshots = new ArrayList<>();
        for (Entity entity : world.getEntities((Entity) null, area,
                entity -> !(entity instanceof PlayerEntity) && !(entity instanceof StandEntity))) {
            CompoundNBT entityNbt = new CompoundNBT();
            if (entity.save(entityNbt)) {
                snapshots.add(new EntitySnapshot(entity.getUUID(), entityNbt));
            }
        }
        return snapshots;
    }

    private static List<PlayerPosSnapshot> capturePlayerPositions(ServerWorld world, BlockPos center, int chunkRadius) {
        AxisAlignedBB area = areaForChunkRadius(center, chunkRadius);
        List<PlayerPosSnapshot> snapshots = new ArrayList<>();
        for (PlayerEntity player : world.getEntitiesOfClass(PlayerEntity.class, area, player -> true)) {
            snapshots.add(new PlayerPosSnapshot(
                    player.getUUID(),
                    player.getX(),
                    player.getY(),
                    player.getZ(),
                    player.yRot,
                    player.xRot
            ));
        }
        return snapshots;
    }

    private static List<StandPosSnapshot> captureStandPositions(ServerWorld world, BlockPos center, int chunkRadius) {
        AxisAlignedBB area = areaForChunkRadius(center, chunkRadius);
        List<StandPosSnapshot> snapshots = new ArrayList<>();
        for (StandEntity stand : world.getEntitiesOfClass(StandEntity.class, area, stand -> true)) {
            snapshots.add(new StandPosSnapshot(
                    stand.getUUID(),
                    stand.getX(),
                    stand.getY(),
                    stand.getZ(),
                    stand.yRot,
                    stand.xRot
            ));
        }
        return snapshots;
    }

    private static int restoreEntities(ServerWorld world, Mark mark) {
        return restoreEntities(world, mark, null);
    }

    private static int restoreEntities(ServerWorld world, Mark mark, UUID excludedUuid) {
        AxisAlignedBB area = areaForChunkRadius(mark.center, mark.chunkRadius);
        List<Entity> currentEntities = world.getEntities((Entity) null, area,
                entity -> !(entity instanceof PlayerEntity) && !(entity instanceof StandEntity));

        Map<UUID, Entity> currentByUuid = new HashMap<>();
        for (Entity entity : currentEntities) {
            currentByUuid.put(entity.getUUID(), entity);
        }

        Set<UUID> snapshotUuids = new HashSet<>();
        for (EntitySnapshot snapshot : mark.entities) {
            if (snapshot.uuid.equals(excludedUuid)) {
                continue;
            }
            snapshotUuids.add(snapshot.uuid);
        }

        // Remove entities created after mark (not present in snapshot)
        for (Entity entity : currentEntities) {
            if (!snapshotUuids.contains(entity.getUUID())) {
                entity.remove();
            }
        }

        int restored = 0;
        Set<UUID> spawned = new HashSet<>();
        for (EntitySnapshot snapshot : mark.entities) {
            if (snapshot.uuid.equals(excludedUuid)) {
                continue;
            }
            CompoundNBT snapshotNbt = snapshot.entityNbt.copy();
            if ("minecraft:player".equals(snapshotNbt.getString("id"))) {
                continue;
            }

            Entity existing = currentByUuid.get(snapshot.uuid);
            if (existing != null && existing.isAlive()) {
                // Keep existing instance and apply exact snapshot state/position.
                existing.load(snapshotNbt);
                restored++;
                continue;
            }

            if (!spawned.add(snapshot.uuid)) {
                continue;
            }
            sanitizeEntityNbtForSpawn(snapshotNbt);
            Entity loaded = EntityType.loadEntityRecursive(snapshotNbt, world, entity -> {
                world.addFreshEntity(entity);
                return entity;
            });
            if (loaded != null) {
                restored++;
            }
        }
        return restored;
    }

    private static int restorePlayerPositions(ServerWorld world, Mark mark) {
        return restorePlayerPositions(world, mark, null);
    }

    private static int restorePlayerPositions(ServerWorld world, Mark mark, UUID excludedUuid) {
        int moved = 0;
        for (PlayerPosSnapshot snapshot : mark.players) {
            if (snapshot.uuid.equals(excludedUuid)) {
                continue;
            }
            ServerPlayerEntity player = world.getServer().getPlayerList().getPlayer(snapshot.uuid);
            if (player == null) {
                continue;
            }
            if (player.level != world) {
                continue;
            }
            player.teleportTo(world, snapshot.x, snapshot.y, snapshot.z, snapshot.yRot, snapshot.xRot);
            moved++;
        }
        return moved;
    }

    private static void restoreStandPositions(ServerWorld world, Mark mark) {
        restoreStandPositions(world, mark, null);
    }

    private static void restoreStandPositions(ServerWorld world, Mark mark, UUID excludedUuid) {
        for (StandPosSnapshot snapshot : mark.stands) {
            if (snapshot.uuid.equals(excludedUuid)) {
                continue;
            }
            Entity entity = world.getEntity(snapshot.uuid);
            if (!(entity instanceof StandEntity)) {
                continue;
            }
            entity.moveTo(snapshot.x, snapshot.y, snapshot.z, snapshot.yRot, snapshot.xRot);
        }
    }

    private static EntityRewindTask buildEntityRewindTask(ServerWorld world, WorldLog log, Mark mark, long targetTick, UUID initiatorUuid, int revertedBlocks) {
        long startTick = world.getGameTime();
        Map<UUID, EntityPath> paths = new HashMap<>();
        Map<UUID, Pose> targetPoseByUuid = new HashMap<>();

        for (PlayerPosSnapshot snapshot : mark.players) {
            targetPoseByUuid.put(snapshot.uuid, new Pose(snapshot.x, snapshot.y, snapshot.z, snapshot.yRot, snapshot.xRot));
            if (snapshot.uuid.equals(initiatorUuid)) {
                continue;
            }
            Entity entity = resolveEntity(world, snapshot.uuid);
            if (entity == null) {
                continue;
            }
            EntityPath path = new EntityPath(snapshot.uuid);
            path.samples.put(startTick, Pose.of(entity));
            path.samples.put(targetTick, targetPoseByUuid.get(snapshot.uuid));
            paths.put(snapshot.uuid, path);
        }

        for (StandPosSnapshot snapshot : mark.stands) {
            targetPoseByUuid.put(snapshot.uuid, new Pose(snapshot.x, snapshot.y, snapshot.z, snapshot.yRot, snapshot.xRot));
            if (snapshot.uuid.equals(initiatorUuid)) {
                continue;
            }
            Entity entity = resolveEntity(world, snapshot.uuid);
            if (entity == null) {
                continue;
            }
            EntityPath path = new EntityPath(snapshot.uuid);
            path.samples.put(startTick, Pose.of(entity));
            path.samples.put(targetTick, targetPoseByUuid.get(snapshot.uuid));
            paths.put(snapshot.uuid, path);
        }

        for (EntitySnapshot snapshot : mark.entities) {
            if (snapshot.uuid.equals(initiatorUuid)) {
                continue;
            }
            Entity entity = resolveEntity(world, snapshot.uuid);
            if (entity == null || entity instanceof PlayerEntity || entity instanceof StandEntity) {
                continue;
            }
            Pose target = readPoseFromNbt(snapshot.entityNbt);
            if (target == null) {
                continue;
            }
            targetPoseByUuid.put(snapshot.uuid, target);
            EntityPath path = new EntityPath(snapshot.uuid);
            path.samples.put(startTick, Pose.of(entity));
            path.samples.put(targetTick, target);
            paths.put(snapshot.uuid, path);
        }

        NavigableMap<Long, Map<UUID, Pose>> frameRange = log.entityFramesByTick.subMap(targetTick, true, startTick, true);
        for (Map.Entry<Long, Map<UUID, Pose>> frame : frameRange.entrySet()) {
            long tick = frame.getKey();
            for (Map.Entry<UUID, Pose> entityFrame : frame.getValue().entrySet()) {
                UUID uuid = entityFrame.getKey();
                EntityPath path = paths.get(uuid);
                if (path == null) {
                    if (uuid.equals(initiatorUuid)) {
                        continue;
                    }
                    Entity entity = resolveEntity(world, uuid);
                    if (entity == null || !entity.isAlive()) {
                        continue;
                    }
                    path = new EntityPath(uuid);
                    path.samples.put(startTick, Pose.of(entity));
                    Pose targetPose = targetPoseByUuid.get(uuid);
                    if (targetPose != null) {
                        path.samples.put(targetTick, targetPose);
                    }
                    paths.put(uuid, path);
                }
                path.samples.put(tick, entityFrame.getValue());
            }
        }
        for (EntityPath path : paths.values()) {
            path.buildRewindStack(targetTick, startTick);
        }

        return new EntityRewindTask(mark, targetTick, startTick, initiatorUuid, revertedBlocks, paths, false);
    }

    private static Pose samplePose(NavigableMap<Long, Pose> samples, long tick) {
        Map.Entry<Long, Pose> floor = samples.floorEntry(tick);
        Map.Entry<Long, Pose> ceil = samples.ceilingEntry(tick);
        if (floor == null && ceil == null) {
            return null;
        }
        if (floor == null) {
            return ceil.getValue();
        }
        if (ceil == null) {
            return floor.getValue();
        }
        if (floor.getKey().equals(ceil.getKey())) {
            return floor.getValue();
        }
        double t = (double) (tick - floor.getKey()) / (double) (ceil.getKey() - floor.getKey());
        return Pose.lerp(floor.getValue(), ceil.getValue(), t);
    }

    private static void restartEntityTrajectoryRecording(ServerWorld world, WorldLog log) {
        log.entityFramesByTick.clear();
        if (!log.recording || log.rewinding) {
            return;
        }
        Map<UUID, Pose> frame = new HashMap<>();
        for (Entity entity : world.getAllEntities()) {
            if (!entity.isAlive()) {
                continue;
            }
            frame.put(entity.getUUID(), Pose.of(entity));
        }
        log.entityFramesByTick.put(world.getGameTime(), frame);
    }

    private static void clearClientGhostTrails(ServerWorld world) {
        NetworkHandler.sendToDimension(
                world,
                new ClearRewindGhostTrailsS2CPacket(world.dimension().location())
        );
    }

    private static Entity resolveEntity(ServerWorld world, UUID uuid) {
        ServerPlayerEntity player = world.getServer().getPlayerList().getPlayer(uuid);
        if (player != null && player.level == world) {
            return player;
        }
        return world.getEntity(uuid);
    }

    private static Pose readPoseFromNbt(CompoundNBT nbt) {
        if (!nbt.contains("Pos", 9)) {
            return null;
        }
        ListNBT pos = nbt.getList("Pos", 6);
        if (pos.size() < 3) {
            return null;
        }
        double x = pos.getDouble(0);
        double y = pos.getDouble(1);
        double z = pos.getDouble(2);
        float yRot = 0.0F;
        float xRot = 0.0F;
        if (nbt.contains("Rotation", 9)) {
            ListNBT rot = nbt.getList("Rotation", 5);
            if (rot.size() >= 2) {
                yRot = rot.getFloat(0);
                xRot = rot.getFloat(1);
            }
        }
        return new Pose(x, y, z, yRot, xRot);
    }

    private static void moveEntityToPose(ServerWorld world, Entity entity, Pose pose) {
        if (entity instanceof ServerPlayerEntity) {
            ((ServerPlayerEntity) entity).teleportTo(world, pose.x, pose.y, pose.z, pose.yRot, pose.xRot);
        } else {
            entity.moveTo(pose.x, pose.y, pose.z, pose.yRot, pose.xRot);
        }
    }

    private static void resetRewindPhysics(Entity entity) {
        entity.fallDistance = 0.0F;
        entity.setDeltaMovement(Vector3d.ZERO);
        entity.setOnGround(true);
    }

    private static void spawnPathGhostsForTask(ServerWorld world, WorldLog log, EntityRewindTask task) {
        List<RewindGhostTrailsS2CPacket.Trail> trails = new ArrayList<>();
        for (EntityPath path : task.paths.values()) {
            Entity source = resolveEntity(world, path.uuid);
            if (source == null || !source.isAlive()) {
                continue;
            }
            if (source instanceof GoldExprienceRequiemEntity) {
                continue;
            }
            List<RewindGhostTrailsS2CPacket.Pose> poses = buildFilteredTrailPoses(path, task.targetTick, task.currentTick);
            if (!poses.isEmpty()) {
                trails.add(new RewindGhostTrailsS2CPacket.Trail(source.getId(), poses));
            }
        }
        NetworkHandler.sendToDimension(
                world,
                new RewindGhostTrailsS2CPacket(world.dimension().location(), trails)
        );
        log.activeGhosts.clear();
    }

    private static List<RewindGhostTrailsS2CPacket.Pose> buildFilteredTrailPoses(EntityPath path, long targetTick, long currentTick) {
        List<Pose> sampled = new ArrayList<>();
        int added = 0;
        int tickStep = Math.max(1, ENTITY_RECORD_INTERVAL_TICKS);
        for (long tick = targetTick; tick < currentTick && added < MAX_GHOST_TRAIL_POINTS_PER_ENTITY; tick += tickStep) {
            Pose pose = samplePose(path.samples, tick);
            if (pose == null) {
                continue;
            }
            sampled.add(pose);
            added++;
        }
        Pose nearStart = samplePose(path.samples, currentTick - 1);
        if (nearStart != null && sampled.size() < MAX_GHOST_TRAIL_POINTS_PER_ENTITY) {
            sampled.add(nearStart);
        }
        List<Pose> overlapCompacted = compactOverlapKeepMiddle(sampled);

        List<RewindGhostTrailsS2CPacket.Pose> result = new ArrayList<>();
        Pose lastKept = null;
        for (Pose pose : overlapCompacted) {
            if (lastKept != null && distanceSqr(lastKept, pose) < GHOST_TRAIL_MIN_DISTANCE_SQR) {
                continue;
            }
            result.add(new RewindGhostTrailsS2CPacket.Pose(pose.x, pose.y, pose.z, pose.yRot, pose.xRot));
            lastKept = pose;
            if (result.size() >= MAX_GHOST_TRAIL_POINTS_PER_ENTITY) {
                break;
            }
        }
        return result;
    }

    private static List<Pose> compactOverlapKeepMiddle(List<Pose> poses) {
        if (poses.size() <= 2) {
            return poses;
        }
        List<Pose> compacted = new ArrayList<>();
        int i = 0;
        while (i < poses.size()) {
            int j = i;
            while (j + 1 < poses.size() && distanceSqr(poses.get(j), poses.get(j + 1)) < GHOST_TRAIL_MIN_DISTANCE_SQR) {
                j++;
            }
            if (j == i) {
                compacted.add(poses.get(i));
            } else {
                int mid = i + (j - i) / 2;
                compacted.add(poses.get(mid));
            }
            i = j + 1;
        }
        return compacted;
    }

    private static double distanceSqr(Pose a, Pose b) {
        double dx = a.x - b.x;
        double dy = a.y - b.y;
        double dz = a.z - b.z;
        return dx * dx + dy * dy + dz * dz;
    }

    private static void cleanupGhosts(ServerWorld world, WorldLog log, boolean force) {
        if (force) {
            cleanupTaggedGhosts(world);
            log.activeGhosts.clear();
            return;
        }
        if (log.activeGhosts.isEmpty()) {
            return;
        }
        long now = world.getGameTime();
        Iterator<Map.Entry<UUID, Long>> it = log.activeGhosts.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<UUID, Long> entry = it.next();
            if (entry.getValue() > now) {
                continue;
            }
            Entity entity = world.getEntity(entry.getKey());
            if (entity != null) {
                entity.remove();
            }
            it.remove();
        }
    }

    private static void cleanupTaggedGhosts(ServerWorld world) {
        for (Entity entity : world.getAllEntities()) {
            if (entity.getTags().contains(GHOST_TAG)) {
                entity.remove();
            }
        }
    }

    private static void sanitizeEntityNbtForSpawn(CompoundNBT entityNbt) {
        entityNbt.remove("UUID");
        entityNbt.remove("UUIDMost");
        entityNbt.remove("UUIDLeast");
        if (entityNbt.contains("Passengers", 9)) {
            ListNBT passengers = entityNbt.getList("Passengers", 10);
            for (INBT nbt : passengers) {
                if (nbt instanceof CompoundNBT) {
                    sanitizeEntityNbtForSpawn((CompoundNBT) nbt);
                }
            }
        }
    }

    private static AxisAlignedBB areaForChunkRadius(BlockPos center, int chunkRadius) {
        int centerChunkX = center.getX() >> 4;
        int centerChunkZ = center.getZ() >> 4;
        int minX = (centerChunkX - chunkRadius) << 4;
        int minZ = (centerChunkZ - chunkRadius) << 4;
        int maxX = (centerChunkX + chunkRadius + 1) << 4;
        int maxZ = (centerChunkZ + chunkRadius + 1) << 4;
        return new AxisAlignedBB(minX, 0, minZ, maxX, 256, maxZ);
    }

    private static Mark buildMark(ServerWorld world, long tick, BlockPos center, int chunkRadius, boolean oneShot) {
        return new Mark(
                tick,
                world.getDayTime(),
                center.immutable(),
                chunkRadius,
                captureEntities(world, center, chunkRadius),
                capturePlayerPositions(world, center, chunkRadius),
                captureStandPositions(world, center, chunkRadius),
                oneShot
        );
    }

    private static void trimOldOps(WorldLog log) {
        while (log.blockOpCount > MAX_BLOCK_OPS && !log.opsByTick.isEmpty()) {
            Map.Entry<Long, List<BlockOp>> first = log.opsByTick.pollFirstEntry();
            if (first == null) {
                break;
            }
            log.blockOpCount -= first.getValue().size();
            log.marksByTick.headMap(first.getKey(), true).clear();
            log.entityFramesByTick.headMap(first.getKey(), true).clear();
            if (log.returnToZeroMarkTick >= 0 && !log.marksByTick.containsKey(log.returnToZeroMarkTick)) {
                log.returnToZeroMarkTick = -1;
            }
        }
    }

    private static void pruneAfterTick(WorldLog log, long tick) {
        NavigableMap<Long, List<BlockOp>> tail = new TreeMap<>(log.opsByTick.tailMap(tick, false));
        for (Map.Entry<Long, List<BlockOp>> entry : tail.entrySet()) {
            log.blockOpCount -= entry.getValue().size();
            log.opsByTick.remove(entry.getKey());
        }
        log.entityFramesByTick.tailMap(tick, false).clear();
        log.marksByTick.tailMap(tick, false).clear();
        if (log.returnToZeroMarkTick > tick) {
            log.returnToZeroMarkTick = -1;
        }
    }

    private static void consumeOneShotMark(WorldLog log, Mark mark) {
        log.marksByTick.remove(mark.tick);
        log.recording = false;
        if (log.returnToZeroMarkTick == mark.tick) {
            log.returnToZeroMarkTick = -1;
        }
        if (log.activeSessionMarkTick == mark.tick) {
            log.activeSessionMarkTick = -1;
            log.activeSessionStartTick = -1;
        }
    }

    private static void removeMarkKeepRecording(WorldLog log, long tick) {
        Mark removed = log.marksByTick.remove(tick);
        if (removed == null) {
            return;
        }
        if (log.returnToZeroMarkTick == tick) {
            log.returnToZeroMarkTick = -1;
        }
        if (log.activeSessionMarkTick == tick) {
            log.activeSessionMarkTick = -1;
            log.activeSessionStartTick = -1;
        }
    }

    public static final class Mark {
        public final long tick;
        public final long dayTime;
        public final BlockPos center;
        public final int chunkRadius;
        public final List<EntitySnapshot> entities;
        public final List<PlayerPosSnapshot> players;
        public final List<StandPosSnapshot> stands;
        public final boolean oneShot;

        private Mark(long tick, long dayTime, BlockPos center, int chunkRadius, List<EntitySnapshot> entities,
                     List<PlayerPosSnapshot> players, List<StandPosSnapshot> stands, boolean oneShot) {
            this.tick = tick;
            this.dayTime = dayTime;
            this.center = center;
            this.chunkRadius = chunkRadius;
            this.entities = Collections.unmodifiableList(new ArrayList<>(entities));
            this.players = Collections.unmodifiableList(new ArrayList<>(players));
            this.stands = Collections.unmodifiableList(new ArrayList<>(stands));
            this.oneShot = oneShot;
        }
    }

    public static final class RewindResult {
        public final boolean success;
        public final long targetTick;
        public final int revertedBlocks;
        public final int restoredEntities;
        public final int movedPlayers;
        public final String message;

        private RewindResult(boolean success, long targetTick, int revertedBlocks, int restoredEntities, int movedPlayers, String message) {
            this.success = success;
            this.targetTick = targetTick;
            this.revertedBlocks = revertedBlocks;
            this.restoredEntities = restoredEntities;
            this.movedPlayers = movedPlayers;
            this.message = message;
        }
    }

    public static final class DebugInfo {
        public final int blockOps;
        public final int marks;
        public final long latestMarkTick;
        public final boolean rewinding;
        public final boolean recording;

        private DebugInfo(int blockOps, int marks, long latestMarkTick, boolean rewinding, boolean recording) {
            this.blockOps = blockOps;
            this.marks = marks;
            this.latestMarkTick = latestMarkTick;
            this.rewinding = rewinding;
            this.recording = recording;
        }
    }

    private static final class BlockOp {
        private final BlockPos pos;
        private final CompoundNBT oldStateNbt;
        private final CompoundNBT oldBlockEntityNbt;

        private BlockOp(BlockPos pos, CompoundNBT oldStateNbt, CompoundNBT oldBlockEntityNbt) {
            this.pos = pos;
            this.oldStateNbt = oldStateNbt;
            this.oldBlockEntityNbt = oldBlockEntityNbt;
        }
    }

    private static final class EntitySnapshot {
        private final UUID uuid;
        private final CompoundNBT entityNbt;

        private EntitySnapshot(UUID uuid, CompoundNBT entityNbt) {
            this.uuid = uuid;
            this.entityNbt = entityNbt.copy();
        }
    }

    private static final class PlayerPosSnapshot {
        private final UUID uuid;
        private final double x;
        private final double y;
        private final double z;
        private final float yRot;
        private final float xRot;

        private PlayerPosSnapshot(UUID uuid, double x, double y, double z, float yRot, float xRot) {
            this.uuid = uuid;
            this.x = x;
            this.y = y;
            this.z = z;
            this.yRot = yRot;
            this.xRot = xRot;
        }
    }

    private static final class StandPosSnapshot {
        private final UUID uuid;
        private final double x;
        private final double y;
        private final double z;
        private final float yRot;
        private final float xRot;

        private StandPosSnapshot(UUID uuid, double x, double y, double z, float yRot, float xRot) {
            this.uuid = uuid;
            this.x = x;
            this.y = y;
            this.z = z;
            this.yRot = yRot;
            this.xRot = xRot;
        }
    }

    private static final class Pose {
        private final double x;
        private final double y;
        private final double z;
        private final float yRot;
        private final float xRot;

        private Pose(double x, double y, double z, float yRot, float xRot) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.yRot = yRot;
            this.xRot = xRot;
        }

        private static Pose of(Entity entity) {
            return new Pose(entity.getX(), entity.getY(), entity.getZ(), entity.yRot, entity.xRot);
        }

        private static Pose lerp(Pose a, Pose b, double t) {
            double x = a.x + (b.x - a.x) * t;
            double y = a.y + (b.y - a.y) * t;
            double z = a.z + (b.z - a.z) * t;
            float yRot = (float) (a.yRot + (b.yRot - a.yRot) * t);
            float xRot = (float) (a.xRot + (b.xRot - a.xRot) * t);
            return new Pose(x, y, z, yRot, xRot);
        }
    }

    private static final class EntityPath {
        private final UUID uuid;
        private final NavigableMap<Long, Pose> samples = new TreeMap<>();
        private final Deque<Pose> rewindStack = new ArrayDeque<>();

        private EntityPath(UUID uuid) {
            this.uuid = uuid;
        }

        private void buildRewindStack(long targetTick, long startTick) {
            rewindStack.clear();
            for (long tick = targetTick; tick < startTick; tick++) {
                Pose pose = samplePose(samples, tick);
                if (pose == null) {
                    continue;
                }
                rewindStack.push(pose);
            }
        }

        private Pose popNextPose() {
            return rewindStack.isEmpty() ? null : rewindStack.pop();
        }
    }

    private static final class EntityRewindTask {
        private final Mark mark;
        private final long targetTick;
        private long currentTick;
        private final UUID initiatorUuid;
        private final int revertedBlocks;
        private final Map<UUID, EntityPath> paths;
        private final boolean removeScopedMarkOnComplete;

        private EntityRewindTask(Mark mark, long targetTick, long startTick, UUID initiatorUuid, int revertedBlocks, Map<UUID, EntityPath> paths, boolean removeScopedMarkOnComplete) {
            this.mark = mark;
            this.targetTick = targetTick;
            this.currentTick = startTick;
            this.initiatorUuid = initiatorUuid;
            this.revertedBlocks = revertedBlocks;
            this.paths = paths;
            this.removeScopedMarkOnComplete = removeScopedMarkOnComplete;
        }

        private boolean isEmpty() {
            return paths.isEmpty();
        }
    }

    private static final class WorldLog {
        private final NavigableMap<Long, List<BlockOp>> opsByTick = new TreeMap<>();
        private final NavigableMap<Long, Mark> marksByTick = new TreeMap<>();
        private final NavigableMap<Long, Map<UUID, Pose>> entityFramesByTick = new TreeMap<>();
        private final Map<UUID, Long> activeGhosts = new HashMap<>();
        private boolean rewinding = false;
        private boolean recording = false;
        private int blockOpCount = 0;
        private long activeSessionStartTick = -1;
        private long activeSessionMarkTick = -1;
        private long returnToZeroMarkTick = -1;
        private EntityRewindTask activeEntityRewindTask = null;
        private RewindResult lastRewindResult = null;
    }
}
