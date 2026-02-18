package com.inza.geraddon.mixin;

import com.inza.geraddon.TimeRecoder.BytecodeRewindManager;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayDeque;
import java.util.Deque;

@Mixin(World.class)
public abstract class ServerWorldMixin {
    @Unique
    private static final ThreadLocal<Deque<BlockSetCapture>> GER_CAPTURES =
            ThreadLocal.withInitial(ArrayDeque::new);

    @Inject(method = "setBlock(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;I)Z", at = @At("HEAD"))
    private void ger$beforeSetBlock(BlockPos pos, BlockState newState, int flags, CallbackInfoReturnable<Boolean> cir) {
        World rawWorld = (World) (Object) this;
        if (!(rawWorld instanceof ServerWorld)) {
            return;
        }
        ServerWorld world = (ServerWorld) rawWorld;
        if (BytecodeRewindManager.isRewinding(world)) {
            return;
        }

        BlockState oldState = world.getBlockState(pos);
        CompoundNBT oldStateNbt = NBTUtil.writeBlockState(oldState);
        TileEntity tileEntity = world.getBlockEntity(pos);
        CompoundNBT oldBlockEntityNbt = tileEntity == null ? null : tileEntity.save(new CompoundNBT());

        GER_CAPTURES.get().push(new BlockSetCapture(pos.immutable(), oldStateNbt, oldBlockEntityNbt, world.getGameTime()));
    }

    @Inject(method = "setBlock(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;I)Z", at = @At("RETURN"))
    private void ger$afterSetBlock(BlockPos pos, BlockState newState, int flags, CallbackInfoReturnable<Boolean> cir) {
        World rawWorld = (World) (Object) this;
        if (!(rawWorld instanceof ServerWorld)) {
            return;
        }
        ServerWorld world = (ServerWorld) rawWorld;
        if (BytecodeRewindManager.isRewinding(world)) {
            return;
        }

        Deque<BlockSetCapture> captures = GER_CAPTURES.get();
        if (captures.isEmpty()) {
            return;
        }
        BlockSetCapture capture = captures.pop();

        if (cir.getReturnValue()) {
            BytecodeRewindManager.recordBlockChange(world, capture.tick, capture.pos, capture.oldStateNbt, capture.oldBlockEntityNbt);
        }

        if (captures.isEmpty()) {
            GER_CAPTURES.remove();
        }
    }

    @Unique
    private static final class BlockSetCapture {
        private final BlockPos pos;
        private final CompoundNBT oldStateNbt;
        private final CompoundNBT oldBlockEntityNbt;
        private final long tick;

        private BlockSetCapture(BlockPos pos, CompoundNBT oldStateNbt, CompoundNBT oldBlockEntityNbt, long tick) {
            this.pos = pos;
            this.oldStateNbt = oldStateNbt;
            this.oldBlockEntityNbt = oldBlockEntityNbt;
            this.tick = tick;
        }
    }
}
