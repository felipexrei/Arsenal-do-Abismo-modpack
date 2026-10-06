package dev.arsenal.item;

import dev.arsenal.Arsenal;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.block.Blocks;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;

/** Chave dimensional: usa para ir e usa de novo (dentro da dimensão) para voltar ao Overworld. */
public class KeyItem extends Item {
    private final RegistryKey<World> dim;

    public KeyItem(String dimId) {
        super(new FabricItemSettings().maxCount(1));
        this.dim = RegistryKey.of(RegistryKeys.WORLD, Arsenal.id(dimId));
    }

    @Override
    public TypedActionResult<ItemStack> use(World w, PlayerEntity p, Hand hand) {
        ItemStack s = p.getStackInHand(hand);
        if (!(w instanceof ServerWorld sw) || !(p instanceof ServerPlayerEntity sp)) return TypedActionResult.success(s);
        boolean back = sw.getRegistryKey().equals(dim);
        ServerWorld tw = sw.getServer().getWorld(back ? World.OVERWORLD : dim);
        if (tw == null) { sp.sendMessage(Text.literal("Dimensão não encontrada."), true); return TypedActionResult.fail(s); }

        BlockPos pos;
        if (back) {
            BlockPos spawn = sp.getSpawnPointPosition();
            pos = spawn != null ? spawn : tw.getSpawnPos();
        } else {
            tw.getChunk(0, 0);
            int y = tw.getTopY(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, 0, 0);
            if (y <= tw.getBottomY() + 2) y = 90;
            for (int dx = -2; dx <= 2; dx++) for (int dz = -2; dz <= 2; dz++) {
                tw.setBlockState(new BlockPos(dx, y - 1, dz), Blocks.OBSIDIAN.getDefaultState());
                for (int dy = 0; dy < 3; dy++) tw.setBlockState(new BlockPos(dx, y + dy, dz), Blocks.AIR.getDefaultState());
            }
            pos = new BlockPos(0, y, 0);
        }
        sp.teleport(tw, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, sp.getYaw(), sp.getPitch());
        sp.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOW_FALLING, 200));
        sp.getItemCooldownManager().set(this, 100);
        return TypedActionResult.success(s);
    }
}
