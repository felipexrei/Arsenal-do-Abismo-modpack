package dev.arsenal.item;

import dev.arsenal.ModItems;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Tecla F: troca o slot selecionado com a faca do inventário; F de novo devolve a arma. */
public final class KnifeHandler {
    private static final Map<UUID, Integer> BACK = new HashMap<>();
    private KnifeHandler() {}

    public static void toggle(ServerPlayerEntity p) {
        PlayerInventory inv = p.getInventory();
        int sel = inv.selectedSlot;
        if (inv.getStack(sel).isOf(ModItems.KNIFE)) {
            Integer back = BACK.remove(p.getUuid());
            if (back != null && back != sel) swap(inv, sel, back);
            return;
        }
        int k = -1;
        for (int i = 0; i < inv.main.size(); i++) if (i != sel && inv.getStack(i).isOf(ModItems.KNIFE)) { k = i; break; }
        if (k < 0) { p.sendMessage(Text.literal("Você não tem uma faca."), true); return; }
        swap(inv, sel, k);
        BACK.put(p.getUuid(), k);
        p.getWorld().playSound(null, p.getBlockPos(), SoundEvents.ITEM_ARMOR_EQUIP_IRON, SoundCategory.PLAYERS, 1f, 1.4f);
    }

    private static void swap(PlayerInventory inv, int a, int b) {
        ItemStack x = inv.getStack(a), y = inv.getStack(b);
        inv.setStack(a, y);
        inv.setStack(b, x);
    }
}
