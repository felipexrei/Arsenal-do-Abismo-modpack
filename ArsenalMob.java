package dev.arsenal.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.RangedAttackMob;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.world.World;

public class ArsenalMob extends HostileEntity implements RangedAttackMob {
    public final MobKind kind;

    public ArsenalMob(EntityType<? extends HostileEntity> type, World world, MobKind kind) {
        super(type, world);
        this.kind = kind;
        this.experiencePoints = 10;
    }

    public static DefaultAttributeContainer.Builder attributes(MobKind k) {
        return HostileEntity.createHostileAttributes()
            .add(EntityAttributes.GENERIC_MAX_HEALTH, k.health)
            .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, k.speed)
            .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, k.damage)
            .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 32.0)
            .add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, k.kbRes);
    }

    @Override
    protected void initGoals() {
        goalSelector.add(0, new SwimGoal(this));
        if (kind.ranged) goalSelector.add(2, new ProjectileAttackGoal(this, 1.0, 30, 20.0f));
        else goalSelector.add(2, new MeleeAttackGoal(this, 1.15, false));
        goalSelector.add(5, new WanderAroundFarGoal(this, 0.8));
        goalSelector.add(6, new LookAtEntityGoal(this, PlayerEntity.class, 10.0f));
        goalSelector.add(6, new LookAroundGoal(this));
        targetSelector.add(1, new RevengeGoal(this));
        targetSelector.add(2, new ActiveTargetGoal<>(this, PlayerEntity.class, true));
    }

    @Override
    public void shootAt(LivingEntity target, float pullProgress) {
        ArrowEntity a = new ArrowEntity(getWorld(), this);
        double dx = target.getX() - getX();
        double dy = target.getBodyY(0.3333) - a.getY();
        double dz = target.getZ() - getZ();
        double h = Math.sqrt(dx * dx + dz * dz);
        a.setVelocity(dx, dy + h * 0.2, dz, 1.6f, 8.0f);
        a.setDamage(kind.damage * 0.5);
        getWorld().spawnEntity(a);
        playSound(SoundEvents.ENTITY_SKELETON_SHOOT, 1.0f, 1.0f);
    }
}
