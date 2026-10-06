package dev.arsenal;

import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.MapColor;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;

public final class ModBlocks {
    public static final Block ABYSS_ORE = Registry.register(Registries.BLOCK, Arsenal.id("abyss_ore"),
        new Block(FabricBlockSettings.create().mapColor(MapColor.DEEPSLATE_GRAY).strength(4.5f, 3.0f).requiresTool().sounds(BlockSoundGroup.DEEPSLATE)));

    private ModBlocks() {}
    public static void init() {}
}
