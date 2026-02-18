package com.inza.geraddon.action.undiepunch;

import com.github.standobyte.jojo.action.stand.StandEntityHeavyAttack;
import com.github.standobyte.jojo.action.stand.punch.StandEntityPunch;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntityTask;
import com.github.standobyte.jojo.entity.stand.StandRelativeOffset;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.util.mc.damage.StandEntityDamageSource;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;

public class UndiePunch extends StandEntityHeavyAttack {
    public static final String TAG = "rtz_finisher";

    public UndiePunch(StandEntityHeavyAttack.Builder builder) {
        super(builder);
    }
    
    @Override
    public StandRelativeOffset getOffsetFromUser(IStandPower standPower, StandEntity standEntity, StandEntityTask task) {
        StandRelativeOffset offset = standEntity.isArmsOnlyMode() ? userOffsetArmsOnly : userOffset;
        return offset != null ? offset : super.getOffsetFromUser(standPower, standEntity, task);
    }

    @Override
    public StandEntityPunch punchEntity(StandEntity stand, Entity target, StandEntityDamageSource dmgSource) {
        StandEntityPunch punch =  super.punchEntity(stand, target, dmgSource);
        if (!(target instanceof LivingEntity)) return punch;

        LivingEntity livingEntity = (LivingEntity) target;

        double damage = stand.getAttackDamage();

        if (livingEntity.getHealth() > damage) return punch;

        livingEntity.addTag(TAG);
        return punch;
    }
}
