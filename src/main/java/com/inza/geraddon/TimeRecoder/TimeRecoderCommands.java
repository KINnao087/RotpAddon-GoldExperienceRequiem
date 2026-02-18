package com.inza.geraddon.TimeRecoder;

import com.inza.geraddon.AddonMain;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = AddonMain.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class TimeRecoderCommands {
    private static final int DEFAULT_RADIUS = 1;
    private static final int MAX_RADIUS = 8;

    @SubscribeEvent
    public static void registerCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSource> dispatcher = event.getDispatcher();
        dispatcher.register(Commands.literal("timerecoder")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("record")
                        .executes(context -> executeRecord(context, DEFAULT_RADIUS))
                        .then(Commands.argument("radius", IntegerArgumentType.integer(0, MAX_RADIUS))
                                .executes(context -> executeRecord(
                                        context, IntegerArgumentType.getInteger(context, "radius")))))
                .then(Commands.literal("rewind")
                        .executes(TimeRecoderCommands::executeRewindLatest)
                        .then(Commands.argument("tick", LongArgumentType.longArg(0))
                                .executes(context -> executeRewindToTick(
                                        context, LongArgumentType.getLong(context, "tick")))))
                .then(Commands.literal("debug")
                        .executes(TimeRecoderCommands::executeDebug)));

        dispatcher.register(Commands.literal("recordnow")
                .requires(source -> source.hasPermission(2))
                .executes(context -> executeRecord(context, DEFAULT_RADIUS))
                .then(Commands.argument("radius", IntegerArgumentType.integer(0, MAX_RADIUS))
                        .executes(context -> executeRecord(
                                context, IntegerArgumentType.getInteger(context, "radius")))));

        dispatcher.register(Commands.literal("rewindtime")
                .requires(source -> source.hasPermission(2))
                .executes(TimeRecoderCommands::executeRewindLatest)
                .then(Commands.argument("tick", LongArgumentType.longArg(0))
                        .executes(context -> executeRewindToTick(
                                context, LongArgumentType.getLong(context, "tick")))));

        dispatcher.register(Commands.literal("trdebug")
                .requires(source -> source.hasPermission(2))
                .executes(TimeRecoderCommands::executeDebug));
    }

    private static int executeRecord(CommandContext<CommandSource> context, int radius) {
        CommandSource source = context.getSource();
        ServerWorld world = source.getLevel();
        BlockPos center = new BlockPos(source.getPosition());
        BytecodeRewindManager.Mark mark = BytecodeRewindManager.mark(world, center, radius);

        source.sendSuccess(new StringTextComponent(String.format(
                "Marked tick=%d radius=%d centerChunk=(%d,%d) [bytecode log]",
                mark.tick,
                mark.chunkRadius,
                mark.center.getX() >> 4,
                mark.center.getZ() >> 4
        )), true);
        return 1;
    }

    private static int executeRewindLatest(CommandContext<CommandSource> context) {
        CommandSource source = context.getSource();
        ServerWorld world = source.getLevel();
        Entity entity = source.getEntity();
        UUID initiator = entity instanceof ServerPlayerEntity ? entity.getUUID() : null;
        BytecodeRewindManager.RewindResult result = BytecodeRewindManager.rewindToLatestMark(world, initiator);
        if (!result.success) {
            source.sendFailure(new StringTextComponent(result.message));
            return 0;
        }
        source.sendSuccess(new StringTextComponent(result.message + " tick=" + result.targetTick
                + " revertedBlocks=" + result.revertedBlocks), true);
        return 1;
    }

    private static int executeRewindToTick(CommandContext<CommandSource> context, long targetTick) {
        CommandSource source = context.getSource();
        ServerWorld world = source.getLevel();
        Entity entity = source.getEntity();
        UUID initiator = entity instanceof ServerPlayerEntity ? entity.getUUID() : null;
        BytecodeRewindManager.RewindResult result = BytecodeRewindManager.rewindToTick(world, targetTick, initiator);
        if (!result.success) {
            source.sendFailure(new StringTextComponent(result.message));
            return 0;
        }
        source.sendSuccess(new StringTextComponent(result.message + " tick=" + result.targetTick
                + " revertedBlocks=" + result.revertedBlocks), true);
        return 1;
    }

    private static int executeDebug(CommandContext<CommandSource> context) {
        CommandSource source = context.getSource();
        ServerWorld world = source.getLevel();
        BytecodeRewindManager.DebugInfo debug = BytecodeRewindManager.debugInfo(world);
        source.sendSuccess(new StringTextComponent(String.format(
                "TimeRecoderDebug dim=%s ops=%d marks=%d latestMarkTick=%d rewinding=%s recording=%s",
                world.dimension().location(),
                debug.blockOps,
                debug.marks,
                debug.latestMarkTick,
                debug.rewinding,
                debug.recording
        )), false);
        return 1;
    }
}
