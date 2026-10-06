package dev.arsenal.item;

import dev.arsenal.Arsenal;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

/** Segure o acessório numa mão e a arma na outra, depois clique com o botão direito. */
public class AttachmentItem extends Item {
    public final Attachment att;

    public AttachmentItem(Attachment att) {
        super(new FabricItemSettings().maxCount(16));
        this.att = att;
    }

    @Override
    public TypedActionResult<ItemStack> use(World w, PlayerEntity p, Hand hand) {
        ItemStack self = p.getStackInHand(hand);
        ItemStack other = p.getStackInHand(hand == Hand.MAIN_HAND ? Hand.OFF_HAND : Hand.MAIN_HAND);
        if (!(other.getItem() instanceof GunItem g)) return TypedActionResult.pass(self);
        if (!w.isClient) {
            NbtCompound n = other.getOrCreateNbt();
            String key = "att_" + att.slot.name();
            Attachment old = Attachment.byId(n.getString(key));
            n.putString(key, att.id);
            self.decrement(1);
            if (old != null) p.getInventory().offerOrDrop(new ItemStack(Registries.ITEM.get(Arsenal.id(old.id))));
            n.putInt("ammo", Math.min(n.getInt("ammo"), g.magSize(other)));
        }
        return TypedActionResult.success(self);
    }
}
