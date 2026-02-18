package com.inza.geraddon.mixin;

import com.github.standobyte.jojo.capability.world.TimeStopHandler;
import com.inza.geraddon.entity.GoldExprienceRequiemEntity;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TimeStopHandler.class)
public class TimeStopHandlerMixin {
    @Inject(method = "updateEntityTimeStop(Lnet/minecraft/entity/Entity;ZZ)V", at = @At("HEAD"), cancellable = true, remap = false)
    private void ger$skipGerFreeze(Entity entity, boolean canMove, boolean updateState, CallbackInfo ci) {
        if (entity instanceof GoldExprienceRequiemEntity) {
            ci.cancel();
        }
    }
}
