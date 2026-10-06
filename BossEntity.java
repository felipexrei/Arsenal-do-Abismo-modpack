package dev.arsenal.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.VexEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.SmallFireballEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class BossEntity extends HostileEntity {
    public final BossKind kind;
    private final ServerBossBar bar;

    public BossEntity(EntityType<? extends HostileEntity> type, World world, BossKind kind) {
        super(type, world);
        this.kind = kind;
        this.bar = new ServerBossBar(Text.translatable("entity.arsenal." + kind.entityId()), kind.color, BossBar.Style.NOTCHED_10);
        this.experiencePoints = 250;
        this.setPersistent();
    }

    public static DefaultAttributeContainer.Builder attributes(BossKind k) {
        return HostileEntity.createHostileAttributes()
            .add(EntityAttributes.GENERIC_MAX_HEALTH, k.health)
            .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, k.speed)
            .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, k.damage)
            .add(EntityAttributes.GENERIC_ARMOR, k.armor)
            .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 48.0)
            .add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 0.9);
    }

    @Override
    protected void initGoals() {
        goalSelector.add(0, new SwimGoal(this));
        goalSelector.add(2, new MeleeAttackGoal(this, 1.2, false));
        goalSelector.add(5, new WanderAroundFarGoal(this, 0.8));
        goalSelector.add(6, new LookAtEntityGoal(this, PlayerEntity.class, 16.0f));
        goalSelector.add(6, new LookAroundGoal(this));
        targetSelector.add(1, new RevengeGoal(this));
        targetSelector.add(2, new ActiveTargetGoal<>(this, PlayerEntity.class, true));
    }

    @Override
    protected void mobTick() {
        super.mobTick();
        bar.setPercent(getHealth() / getMaxHealth());
    }

    @Override public void onStartedTrackingBy(ServerPlayerEntity p) { super.onStartedTrackingBy(p); bar.addPlayer(p); }
    @Override public void onStoppedTrackingBy(ServerPlayerEntity p) { super.onStoppedTrackingBy(p); bar.removePlayer(p); }

    private boolean every(int ticks) {
        int t = getHealth() < getMaxHealth() / 2 ? Math.max(20, ticks / 2) : ticks; // fase 2
        return age % t == 0;
    }

    @Override
    public void tickMovement() {
        super.tickMovement();
        if (getWorld().isClient) return;
        LivingEntity t = getTarget();
        if (t == null || !t.isAlive()) return;
        switch (kind) {
            case COLOSSUS -> { if (every(100)) slam(6, (float) (kind.damage * 0.6)); }
            case WRAITH -> {
                if (every(120)) {
                    Vec3d p = t.getPos().add((random.nextDouble() - 0.5) * 6, 0, (random.nextDouble() - 0.5) * 6);
                    requestTeleport(p.x, p.y, p.z);
                    t.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 60));
                    t.addStatusEffect(new StatusEffectInstance(StatusEffects.WITHER, 60));
                }
            }
            case DRAKE -> { if (every(70)) fireball(t); }
            case VOID -> {
                if (every(100)) slam(5, (float) (kind.damage * 0.5));
                if (every(140)) fireball(t);
                if (every(300)) summonVex();
            }
        }
    }

    private void slam(double radius, float dmg) {
        for (Entity e : getWorld().getOtherEntities(this, getBoundingBox().expand(radius, 2, radius), x -> x instanceof LivingEntity && !(x instanceof BossEntity))) {
            LivingEntity le = (LivingEntity) e;
            le.damage(getDamageSources().mobAttack(this), dmg);
            le.takeKnockback(1.2, getX() - le.getX(), getZ() - le.getZ());
            le.addVelocity(0, 0.5, 0);
        }
        ((ServerWorld) getWorld()).spawnParticles(ParticleTypes.EXPLOSION, getX(), getY() + 0.2, getZ(), 6, radius / 3, 0.1, radius / 3, 0.0);
        playSound(SoundEvents.ENTITY_GENERIC_EXPLODE, 1.5f, 0.6f);
    }

    private void fireball(LivingEntity t) {
        Vec3d d = t.getEyePos().subtract(getEyePos()).normalize();
        SmallFireballEntity f = new SmallFireballEntity(getWorld(), this, d.x, d.y, d.z);
        f.setPosition(getX() + d.x * 1.5, getEyeY() + d.y, getZ() + d.z * 1.5);
        getWorld().spawnEntity(f);
    }

    private void summonVex() {
        VexEntity v = net.minecraft.entity.EntityType.VEX.create(getWorld());
        if (v == null) return;
        v.refreshPositionAndAngles(getX(), getY() + 2, getZ(), 0, 0);
        getWorld().spawnEntity(v);
    }
}
