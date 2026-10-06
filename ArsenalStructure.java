package dev.arsenal.world;

import dev.arsenal.Arsenal;
import dev.arsenal.ModEntities;
import dev.arsenal.entity.MobKind;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.block.entity.MobSpawnerBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.util.FeatureContext;

/**
 * Estruturas geradas por código (sem arquivos .nbt): fáceis de editar.
 * Quatro variantes: bunker e acampamento (Overworld), torre (Abismo) e santuário (Céus).
 */
public class ArsenalStructure extends Feature<DefaultFeatureConfig> {
    public enum Variant {
        BUNKER("bunker"), MINE_CAMP("mine_camp"), ABYSS_TOWER("abyss_tower"), SKY_SHRINE("sky_shrine");
        public final String id;
        Variant(String id) { this.id = id; }
    }

    private final Variant variant;

    public ArsenalStructure(Variant v) {
        super(DefaultFeatureConfig.CODEC);
        this.variant = v;
    }

    @Override
    public boolean generate(FeatureContext<DefaultFeatureConfig> c) {
        StructureWorldAccess w = c.getWorld();
        BlockPos o = c.getOrigin();
        Random r = c.getRandom();
        if (o.getY() <= w.getBottomY() + 3) return false;
        switch (variant) {
            case BUNKER -> bunker(w, o, r);
            case MINE_CAMP -> mineCamp(w, o, r);
            case ABYSS_TOWER -> abyssTower(w, o, r);
            case SKY_SHRINE -> skyShrine(w, o, r);
        }
        return true;
    }

    // ---------- helpers ----------
    private static void set(StructureWorldAccess w, BlockPos o, int x, int y, int z, BlockState s) {
        w.setBlockState(o.add(x, y, z), s, Block.NOTIFY_LISTENERS);
    }
    private static void fill(StructureWorldAccess w, BlockPos o, int x1, int y1, int z1, int x2, int y2, int z2, BlockState s) {
        for (int x = x1; x <= x2; x++) for (int y = y1; y <= y2; y++) for (int z = z1; z <= z2; z++) set(w, o, x, y, z, s);
    }
    private static void chest(StructureWorldAccess w, BlockPos o, int x, int y, int z, Direction facing, String loot, Random r) {
        BlockPos p = o.add(x, y, z);
        w.setBlockState(p, Blocks.CHEST.getDefaultState().with(ChestBlock.FACING, facing), Block.NOTIFY_LISTENERS);
        LootableContainerBlockEntity.setLootTable(w, r, p, Arsenal.id("chests/" + loot));
    }
    private static void spawn(StructureWorldAccess w, EntityType<?> t, BlockPos p) {
        Entity e = t.create(w.toServerWorld());
        if (e == null) return;
        e.refreshPositionAndAngles(p.getX() + 0.5, p.getY(), p.getZ() + 0.5, 0, 0);
        if (e instanceof MobEntity m) {
            m.initialize(w, w.getLocalDifficulty(p), SpawnReason.STRUCTURE, null, null);
            m.setPersistent();
        }
        w.spawnEntityAndPassengers(e);
    }

    // ---------- Overworld: Bunker abandonado ----------
    private void bunker(StructureWorldAccess w, BlockPos o, Random r) {
        BlockState bricks = Blocks.STONE_BRICKS.getDefaultState();
        BlockState air = Blocks.AIR.getDefaultState();
        fill(w, o, -4, -4, -4, 4, -1, 4, Blocks.COBBLESTONE.getDefaultState());
        fill(w, o, -4, 0, -4, 4, 4, 4, bricks);
        fill(w, o, -3, 0, -3, 3, 3, 3, air);
        fill(w, o, 0, 0, 4, 0, 1, 4, air); // porta
        for (int i : new int[]{-4, 4}) set(w, o, i, 2, 0, Blocks.IRON_BARS.getDefaultState());
        set(w, o, 0, 2, -4, Blocks.IRON_BARS.getDefaultState());
        set(w, o, 0, 3, 0, Blocks.LANTERN.getDefaultState().with(LanternBlock.HANGING, true));
        set(w, o, 3, 0, -3, Blocks.BARREL.getDefaultState());
        set(w, o, -3, 0, 3, Blocks.CRAFTING_TABLE.getDefaultState());
        chest(w, o, -3, 0, -3, Direction.SOUTH, "bunker", r);
        spawn(w, ModEntities.MOB.get(MobKind.RENEGADE), o.add(1, 0, 1));
        spawn(w, ModEntities.MOB.get(MobKind.RENEGADE), o.add(-1, 0, 0));
    }

    // ---------- Overworld: Acampamento de mineiros ----------
    private void mineCamp(StructureWorldAccess w, BlockPos o, Random r) {
        BlockState plank = Blocks.OAK_PLANKS.getDefaultState();
        BlockState log = Blocks.OAK_LOG.getDefaultState();
        fill(w, o, -3, -3, -3, 3, -1, 3, Blocks.DIRT.getDefaultState());
        fill(w, o, -3, -1, -3, 3, -1, 3, plank);
        fill(w, o, -3, 0, -3, 3, 3, 3, Blocks.AIR.getDefaultState());
        for (int x : new int[]{-3, 3}) for (int z : new int[]{-3, 3}) fill(w, o, x, 0, z, x, 3, z, log);
        fill(w, o, -3, 4, -3, 3, 4, 3, Blocks.SPRUCE_PLANKS.getDefaultState());
        for (int i = -2; i <= 2; i++) { set(w, o, i, 1, -3, plank); set(w, o, i, 0, -3, plank); set(w, o, -3, 0, i, plank); set(w, o, 3, 0, i, plank); }
        set(w, o, 0, 3, 0, Blocks.LANTERN.getDefaultState().with(LanternBlock.HANGING, true));
        chest(w, o, 2, 0, -2, Direction.WEST, "mine_camp", r);
        set(w, o, -2, 0, -2, Blocks.FURNACE.getDefaultState());
        set(w, o, -2, 0, 2, Blocks.COAL_ORE.getDefaultState());
        set(w, o, -2, 1, 2, Blocks.IRON_ORE.getDefaultState());
        for (int z = 4; z <= 8; z++) set(w, o, 0, 0, z, Blocks.RAIL.getDefaultState());
        spawn(w, ModEntities.MINER, o.add(1, 0, 1));
        spawn(w, ModEntities.MINER, o.add(-1, 0, 1));
    }

    // ---------- Abismo: Torre ----------
    private void abyssTower(StructureWorldAccess w, BlockPos o, Random r) {
        BlockState wall = Blocks.DEEPSLATE_BRICKS.getDefaultState();
        BlockState air = Blocks.AIR.getDefaultState();
        fill(w, o, -3, -5, -3, 3, -1, 3, Blocks.DEEPSLATE.getDefaultState());
        fill(w, o, -3, 0, -3, 3, 14, 3, wall);
        fill(w, o, -2, 0, -2, 2, 14, 2, air);
        for (int y : new int[]{5, 10}) {
            fill(w, o, -2, y, -2, 2, y, 2, Blocks.POLISHED_DEEPSLATE.getDefaultState());
            set(w, o, 0, y, -2, air);
        }
        for (int y = 1; y <= 13; y++) set(w, o, 0, y, -2, Blocks.LADDER.getDefaultState().with(LadderBlock.FACING, Direction.SOUTH));
        fill(w, o, 0, 0, 3, 0, 2, 3, air); // porta
        for (int y : new int[]{3, 8, 12}) for (int d : new int[]{-3, 3}) { set(w, o, d, y, 0, air); set(w, o, 0, y, -3, air); }
        for (int x = -3; x <= 3; x += 2) for (int z : new int[]{-3, 3}) { set(w, o, x, 15, z, wall); set(w, o, z, 15, x, wall); }
        set(w, o, 1, 11, -1, Blocks.SOUL_LANTERN.getDefaultState());
        chest(w, o, 1, 11, 1, Direction.NORTH, "abyss_tower", r);
        BlockPos sp = o.add(0, 6, 0);
        w.setBlockState(sp, Blocks.SPAWNER.getDefaultState(), Block.NOTIFY_LISTENERS);
        BlockEntity be = w.getBlockEntity(sp);
        if (be instanceof MobSpawnerBlockEntity ms) ms.setEntityType(ModEntities.MOB.get(MobKind.STALKER), r);
        spawn(w, ModEntities.MOB.get(MobKind.STALKER), o.add(1, 0, 0));
        spawn(w, ModEntities.MOB.get(MobKind.STALKER), o.add(-1, 11, 0));
    }

    // ---------- Céus: Santuário flutuante ----------
    private void skyShrine(StructureWorldAccess w, BlockPos o, Random r) {
        BlockState brick = Blocks.END_STONE_BRICKS.getDefaultState();
        for (int dx = -5; dx <= 5; dx++) for (int dz = -5; dz <= 5; dz++) {
            int d2 = dx * dx + dz * dz;
            if (d2 > 25) continue;
            set(w, o, dx, -1, dz, brick);
            if (d2 <= 16) set(w, o, dx, -2, dz, brick);
            if (d2 <= 9) set(w, o, dx, -3, dz, Blocks.END_STONE.getDefaultState());
            if (d2 <= 4) set(w, o, dx, -4, dz, Blocks.END_STONE.getDefaultState());
            if (d2 <= 1) set(w, o, dx, -5, dz, Blocks.END_STONE.getDefaultState());
            for (int y = 0; y <= 5; y++) set(w, o, dx, y, dz, Blocks.AIR.getDefaultState());
        }
        for (int sx : new int[]{-4, 4}) for (int sz : new int[]{-4, 4}) {
            fill(w, o, sx, 0, sz, sx, 3, sz, Blocks.OBSIDIAN.getDefaultState());
            set(w, o, sx, 4, sz, Blocks.END_ROD.getDefaultState());
        }
        set(w, o, 0, 0, 0, Blocks.CRYING_OBSIDIAN.getDefaultState());
        chest(w, o, 0, 1, 0, Direction.SOUTH, "sky_shrine", r);
        spawn(w, ModEntities.MOB.get(MobKind.SENTINEL), o.add(2, 0, 2));
        spawn(w, ModEntities.MOB.get(MobKind.SENTINEL), o.add(-2, 0, -2));
    }
}
