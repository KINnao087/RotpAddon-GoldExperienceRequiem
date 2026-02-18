package com.inza.geraddon.mixin;

import com.github.standobyte.jojo.action.stand.TimeStop;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.inza.geraddon.TimeRecoder.BytecodeRewindManager;
import net.minecraft.entity.LivingEntity;
import net.minecraft.world.World;
import com.github.standobyte.jojo.action.ActionTarget;
import net.minecraft.world.server.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TimeStop.class)
public class TimeStopMixin {
    @Inject(method = "perform(Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;Lcom/github/standobyte/jojo/power/impl/stand/IStandPower;Lcom/github/standobyte/jojo/action/ActionTarget;)V",
            at = @At("HEAD"),
            remap = false)
    private void ger$onTimeStopPerform(World world, LivingEntity user, IStandPower userPower, ActionTarget target, CallbackInfo ci) {
        if (world.isClientSide() || !(world instanceof ServerWorld)) {
            return;
        }
        // Start one-shot rewind session at time stop activation.
        BytecodeRewindManager.startTimeStopSession((ServerWorld) world, user.blockPosition(), 8);
    }
}
