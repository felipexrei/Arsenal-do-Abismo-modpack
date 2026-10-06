package dev.arsenal;

import dev.arsenal.entity.BossKind;
import dev.arsenal.entity.MobKind;
import dev.arsenal.item.*;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.*;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ModItems {
    public static final Map<String, Item> ALL = new LinkedHashMap<>();
    public static final List<GunItem> GUNS = new ArrayList<>();

    private static <T extends Item> T reg(String id, T item) {
        Registry.register(Registries.ITEM, Arsenal.id(id), item);
        ALL.put(id, item);
        return item;
    }
    private static FabricItemSettings s() { return new FabricItemSettings(); }

    // materiais
    public static final Item ABYSS_SHARD = reg("abyss_shard", new Item(s()));
    public static final Item ABYSS_INGOT = reg("abyss_ingot", new Item(s()));
    public static final Item CORE_COLOSSUS = reg("core_colossus", new Item(s().rarity(Rarity.RARE)));
    public static final Item CORE_WRAITH = reg("core_wraith", new Item(s().rarity(Rarity.RARE)));
    public static final Item CORE_DRAKE = reg("core_drake", new Item(s().rarity(Rarity.RARE)));
    public static final Item CORE_VOID = reg("core_void", new Item(s().rarity(Rarity.EPIC)));
    public static final Item CORE_PRIMORDIAL = reg("core_primordial", new Item(s().rarity(Rarity.EPIC)));
    public static final Item ABYSS_ORE = reg("abyss_ore", new BlockItem(ModBlocks.ABYSS_ORE, s()));

    // espadas
    public static final Item COMBAT_KNIFE = reg("combat_knife", new SwordItem(ModToolMaterial.KNIFE, 2, -1.6f, s()));
    public static final Item KNIFE = COMBAT_KNIFE;
    public static final Item STEEL_SWORD = reg("steel_sword", new SwordItem(ModToolMaterial.STEEL, 3, -2.4f, s()));
    public static final Item ABYSS_SWORD = reg("abyss_sword", new SwordItem(ModToolMaterial.ABYSS, 3, -2.3f, s()));
    public static final Item VOID_SWORD = reg("void_sword", new SwordItem(ModToolMaterial.VOID, 3, -2.2f, s().rarity(Rarity.EPIC)));

    // armaduras
    public static final Item ABYSS_HELMET = reg("abyss_helmet", new ArmorItem(ModArmorMaterial.ABYSS, ArmorItem.Type.HELMET, s()));
    public static final Item ABYSS_CHESTPLATE = reg("abyss_chestplate", new ArmorItem(ModArmorMaterial.ABYSS, ArmorItem.Type.CHESTPLATE, s()));
    public static final Item ABYSS_LEGGINGS = reg("abyss_leggings", new ArmorItem(ModArmorMaterial.ABYSS, ArmorItem.Type.LEGGINGS, s()));
    public static final Item ABYSS_BOOTS = reg("abyss_boots", new ArmorItem(ModArmorMaterial.ABYSS, ArmorItem.Type.BOOTS, s()));
    public static final Item VOID_HELMET = reg("void_helmet", new ArmorItem(ModArmorMaterial.VOID, ArmorItem.Type.HELMET, s().rarity(Rarity.EPIC)));
    public static final Item VOID_CHESTPLATE = reg("void_chestplate", new ArmorItem(ModArmorMaterial.VOID, ArmorItem.Type.CHESTPLATE, s().rarity(Rarity.EPIC)));
    public static final Item VOID_LEGGINGS = reg("void_leggings", new ArmorItem(ModArmorMaterial.VOID, ArmorItem.Type.LEGGINGS, s().rarity(Rarity.EPIC)));
    public static final Item VOID_BOOTS = reg("void_boots", new ArmorItem(ModArmorMaterial.VOID, ArmorItem.Type.BOOTS, s().rarity(Rarity.EPIC)));

    // munição
    public static final Item AMMO_LIGHT = reg("ammo_light", new Item(s().maxCount(64)));
    public static final Item AMMO_HEAVY = reg("ammo_heavy", new Item(s().maxCount(64)));

    // acessórios
    public static final Item SCOPE = reg("scope", new AttachmentItem(Attachment.SCOPE));
    public static final Item SUPPRESSOR = reg("suppressor", new AttachmentItem(Attachment.SUPPRESSOR));
    public static final Item GRIP = reg("grip", new AttachmentItem(Attachment.GRIP));
    public static final Item EXT_MAG = reg("ext_mag", new AttachmentItem(Attachment.EXT_MAG));

    // chaves e selos
    public static final Item KEY_ABYSS = reg("key_abyss", new KeyItem("abyss"));
    public static final Item KEY_SKYLANDS = reg("key_skylands", new KeyItem("skylands"));
    public static final Item SEAL_COLOSSUS = reg("seal_colossus", new SealItem(BossKind.COLOSSUS));
    public static final Item SEAL_WRAITH = reg("seal_wraith", new SealItem(BossKind.WRAITH));
    public static final Item SEAL_DRAKE = reg("seal_drake", new SealItem(BossKind.DRAKE));
    public static final Item SEAL_VOID = reg("seal_void", new SealItem(BossKind.VOID));

    // ovos
    public static final Item MINER_SPAWN_EGG = reg("miner_spawn_egg", new SpawnEggItem(ModEntities.MINER, 0x8a6a3a, 0xd9d9d9, s()));

    public static final ItemGroup GROUP = FabricItemGroup.builder()
        .icon(() -> new ItemStack(VOID_SWORD))
        .displayName(Text.translatable("itemGroup.arsenal"))
        .entries((ctx, entries) -> ALL.values().forEach(entries::add))
        .build();

    private ModItems() {}

    public static void init() {
        for (dev.arsenal.Balance.Gun g : dev.arsenal.Balance.Gun.values()) {
            GunItem gi = reg(g.id, new GunItem(g));
            GUNS.add(gi);
        }
        for (MobKind k : MobKind.values()) {
            reg("spawn_" + k.id, new SpawnEggItem(ModEntities.MOB.get(k), k.eggA, k.eggB, s()));
        }
        Registry.register(Registries.ITEM_GROUP, Arsenal.id("main"), GROUP);
    }
}
