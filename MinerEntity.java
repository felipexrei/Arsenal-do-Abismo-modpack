package dev.arsenal.entity;

import net.fabricmc.fabric.api.tag.convention.v1.ConventionalBlockTags;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.EnumSet;

/** Mineiro: NPC neutro que sai procurando minérios por perto e os quebra (os itens caem no chão). */
public class MinerEntity extends PathAwareEntity {
    public MinerEntity(EntityType<? extends PathAwareEntity> type, World world) { super(type, world); }

    public static DefaultAttributeContainer.Builder attributes() {
        return PathAwareEntity.createMobAttributes()
            .add(EntityAttributes.GENERIC_MAX_HEALTH, 30.0)
            .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.3)
            .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 3.0);
    }

    @Override
    protected void initGoals() {
        goalSelector.add(0, new SwimGoal(this));
        goalSelector.add(2, new MineOreGoal(this));
        goalSelector.add(5, new WanderAroundFarGoal(this, 0.8));
        goalSelector.add(6, new LookAtEntityGoal(this, PlayerEntity.class, 8.0f));
        goalSelector.add(6, new LookAroundGoal(this));
    }

    static class MineOreGoal extends Goal {
        private final MinerEntity m;
        private BlockPos target;
        private int timer, hits;

        MineOreGoal(MinerEntity m) { this.m = m; setControls(EnumSet.of(Control.MOVE, Control.LOOK)); }

        private BlockPos find() {
            BlockPos o = m.getBlockPos(), best = null;
            double bd = Double.MAX_VALUE;
            for (BlockPos p : BlockPos.iterateOutwards(o, 8, 4, 8)) {
                if (m.getWorld().getBlockState(p).isIn(ConventionalBlockTags.ORES)) {
                    double d = p.getSquaredDistance(o);
                    if (d < bd) { bd = d; best = p.toImmutable(); }
                }
            }
            return best;
        }

        @Override public boolean canStart() {
            if (m.getRandom().nextInt(40) != 0) return false;
            target = find();
            return target != null;
        }
        @Override public boolean shouldContinue() {
            return target != null && timer < 300 && m.getWorld().getBlockState(target).isIn(ConventionalBlockTags.ORES);
        }
        @Override public void start() { timer = 0; hits = 0; go(); }
        private void go() { m.getNavigation().startMovingTo(target.getX() + 0.5, target.getY(), target.getZ() + 0.5, 1.0); }

        @Override public void tick() {
            timer++;
            if (m.getBlockPos().isWithinDistance(target, 2.6)) {
                m.getNavigation().stop();
                m.getLookControl().lookAt(Vec3d.ofCenter(target));
                if (timer % 15 == 0) {
                    m.swingHand(Hand.MAIN_HAND);
                    if (++hits >= 4) { m.getWorld().breakBlock(target, true, m); target = null; }
                }
            } else if (timer % 20 == 0) go();
        }
        @Override public void stop() { target = null; }
    }
}
