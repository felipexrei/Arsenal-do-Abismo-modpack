package dev.arsenal.item;

import dev.arsenal.ModEntities;
import dev.arsenal.entity.BossEntity;
import dev.arsenal.entity.BossKind;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Difficulty;
import net.minecraft.world.World;

/** Invoca um boss ao usar num bloco. */
public class SealItem extends Item {
    private final BossKind kind;

    public SealItem(BossKind kind) {
        super(new FabricItemSettings().maxCount(1));
        this.kind = kind;
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext ctx) {
        World w = ctx.getWorld();
        if (w.isClient) return ActionResult.SUCCESS;
        if (w.getDifficulty() == Difficulty.PEACEFUL) {
            if (ctx.getPlayer() != null) ctx.getPlayer().sendMessage(Text.literal("Os bosses não aparecem no Pacífico."), true);
            return ActionResult.FAIL;
        }
        BlockPos p = ctx.getBlockPos().offset(ctx.getSide());
        BossEntity b = ModEntities.BOSS.get(kind).create(w);
        if (b == null) return ActionResult.FAIL;
        b.refreshPositionAndAngles(p.getX() + 0.5, p.getY(), p.getZ() + 0.5, 0, 0);
        w.spawnEntity(b);
        ctx.getStack().decrement(1);
        return ActionResult.CONSUME;
    }
}
