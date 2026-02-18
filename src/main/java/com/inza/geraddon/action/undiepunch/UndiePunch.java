package com.inza.geraddon.action.undiepunch;

import com.github.standobyte.jojo.action.stand.StandEntityHeavyAttack;
import com.github.standobyte.jojo.action.stand.punch.StandEntityPunch;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.util.mc.damage.StandEntityDamageSource;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;

public class UndiePunch extends StandEntityHeavyAttack {
    public static final String TAG = "rtz_finisher";

    public UndiePunch(StandEntityHeavyAttack.Builder builder) {
        super(builder);
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
