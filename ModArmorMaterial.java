package dev.arsenal;

import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.recipe.Ingredient;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;

import java.util.function.Supplier;

/** ABYSS ≈ netherite+; VOID = a mais forte do mod (difícil de fazer, ver receitas). */
public enum ModArmorMaterial implements ArmorMaterial {
    //      nome     mult  cap pei  per  bota  encant tough  knockbackRes  reparo
    ABYSS("abyss",   40,   3,  8,   6,   3,    16,    2.5f,  0.05f, () -> Ingredient.ofItems(ModItems.ABYSS_INGOT)),
    VOID ("void",    60,   4,  9,   7,   4,    22,    4.0f,  0.15f, () -> Ingredient.ofItems(ModItems.CORE_PRIMORDIAL));

    private static final int[] BASE = {11, 16, 15, 13}; // capacete, peitoral, calça, bota
    private final String name;
    private final int mult, helmet, chest, legs, boots, enchant;
    private final float toughness, kb;
    private final Supplier<Ingredient> repair;

    ModArmorMaterial(String name, int mult, int helmet, int chest, int legs, int boots, int enchant, float toughness, float kb, Supplier<Ingredient> repair) {
        this.name = name; this.mult = mult; this.helmet = helmet; this.chest = chest; this.legs = legs; this.boots = boots;
        this.enchant = enchant; this.toughness = toughness; this.kb = kb; this.repair = repair;
    }

    @Override
    public int getDurability(ArmorItem.Type type) {
        return switch (type) {
            case HELMET -> BASE[0] * mult;
            case CHESTPLATE -> BASE[1] * mult;
            case LEGGINGS -> BASE[2] * mult;
            case BOOTS -> BASE[3] * mult;
        };
    }

    @Override
    public int getProtection(ArmorItem.Type type) {
        return switch (type) {
            case HELMET -> helmet;
            case CHESTPLATE -> chest;
            case LEGGINGS -> legs;
            case BOOTS -> boots;
        };
    }

    @Override public int getEnchantability() { return enchant; }
    @Override public SoundEvent getEquipSound() { return SoundEvents.ITEM_ARMOR_EQUIP_NETHERITE; }
    @Override public Ingredient getRepairIngredient() { return repair.get(); }
    @Override public String getName() { return Arsenal.ID + ":" + name; }
    @Override public float getToughness() { return toughness; }
    @Override public float getKnockbackResistance() { return kb; }
}
