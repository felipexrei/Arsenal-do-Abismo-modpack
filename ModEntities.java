package dev.arsenal;

import dev.arsenal.entity.*;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.SpawnRestriction;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.world.Heightmap;

import java.util.EnumMap;
import java.util.Map;

public final class ModEntities {
    public static EntityType<MinerEntity> MINER;
    public static final Map<BossKind, EntityType<BossEntity>> BOSS = new EnumMap<>(BossKind.class);
    public static final Map<MobKind, EntityType<ArsenalMob>> MOB = new EnumMap<>(MobKind.class);

    private ModEntities() {}

    public static void init() {
        MINER = Registry.register(Registries.ENTITY_TYPE, Arsenal.id("miner"),
            FabricEntityTypeBuilder.createMob()
                .spawnGroup(SpawnGroup.CREATURE)
                .entityFactory(MinerEntity::new)
                .spawnRestriction(SpawnRestriction.Location.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, (t, w, r, p, rnd) -> true)
                .dimensions(EntityDimensions.fixed(0.6f, 1.95f))
                .build());
        FabricDefaultAttributeRegistry.register(MINER, MinerEntity.attributes());

        for (MobKind k : MobKind.values()) {
            FabricEntityTypeBuilder.Mob<ArsenalMob> b = FabricEntityTypeBuilder.createMob()
                .spawnGroup(SpawnGroup.MONSTER)
                .entityFactory((EntityType<ArsenalMob> t, net.minecraft.world.World w) -> new ArsenalMob(t, w, k))
                .spawnRestriction(SpawnRestriction.Location.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES,
                    (t, w, r, p, rnd) -> HostileEntity.canSpawnInDark(t, w, r, p, rnd))
                .dimensions(EntityDimensions.fixed(0.6f, 1.95f));
            if (k.fireImmune) b.fireImmune();
            EntityType<ArsenalMob> type = Registry.register(Registries.ENTITY_TYPE, Arsenal.id(k.id), b.build());
            FabricDefaultAttributeRegistry.register(type, ArsenalMob.attributes(k));
            MOB.put(k, type);
        }

        for (BossKind k : BossKind.values()) {
            FabricEntityTypeBuilder.Mob<BossEntity> b = FabricEntityTypeBuilder.createMob()
                .spawnGroup(SpawnGroup.MONSTER)
                .entityFactory((EntityType<BossEntity> t, net.minecraft.world.World w) -> new BossEntity(t, w, k))
                .dimensions(EntityDimensions.fixed(0.6f * k.scale, 1.95f * k.scale))
                .trackRangeBlocks(128);
            if (k.fireImmune) b.fireImmune();
            EntityType<BossEntity> type = Registry.register(Registries.ENTITY_TYPE, Arsenal.id(k.entityId()), b.build());
            FabricDefaultAttributeRegistry.register(type, BossEntity.attributes(k));
            BOSS.put(k, type);
        }
    }
}
