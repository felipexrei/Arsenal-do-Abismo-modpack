package dev.arsenal;

import net.minecraft.item.Items;
import net.minecraft.item.ToolMaterial;
import net.minecraft.recipe.Ingredient;

import java.util.function.Supplier;

public enum ModToolMaterial implements ToolMaterial {
    KNIFE(900, 6.0f, 1.0f, 2, 10, () -> Ingredient.ofItems(Items.IRON_INGOT)),
    STEEL(900, 7.0f, 2.0f, 3, 12, () -> Ingredient.ofItems(Items.IRON_INGOT)),
    ABYSS(2200, 8.5f, 4.0f, 4, 16, () -> Ingredient.ofItems(ModItems.ABYSS_INGOT)),
    VOID(4200, 10.0f, 5.0f, 5, 22, () -> Ingredient.ofItems(ModItems.CORE_PRIMORDIAL));

    private final int durability, level, enchant;
    private final float speed, damage;
    private final Supplier<Ingredient> repair;

    ModToolMaterial(int durability, float speed, float damage, int level, int enchant, Supplier<Ingredient> repair) {
        this.durability = durability; this.speed = speed; this.damage = damage;
        this.level = level; this.enchant = enchant; this.repair = repair;
    }

    @Override public int getDurability() { return durability; }
    @Override public float getMiningSpeedMultiplier() { return speed; }
    @Override public float getAttackDamage() { return damage; }
    @Override public int getMiningLevel() { return level; }
    @Override public int getEnchantability() { return enchant; }
    @Override public Ingredient getRepairIngredient() { return repair.get(); }
}
