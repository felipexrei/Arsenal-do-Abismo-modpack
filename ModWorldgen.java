package dev.arsenal.world;

import dev.arsenal.Arsenal;
import dev.arsenal.ModEntities;
import dev.arsenal.entity.MobKind;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.feature.PlacedFeature;

public final class ModWorldgen {
    private ModWorldgen() {}

    private static RegistryKey<PlacedFeature> placed(String id) {
        return RegistryKey.of(RegistryKeys.PLACED_FEATURE, Arsenal.id(id));
    }

    public static void init() {
        for (ArsenalStructure.Variant v : ArsenalStructure.Variant.values())
            Registry.register(Registries.FEATURE, Arsenal.id(v.id), new ArsenalStructure(v));

        // Estruturas no Overworld (as das dimensões novas estão nos biomas JSON delas)
        var overworldSurface = BiomeSelectors.foundInOverworld().and(BiomeSelectors.excludeByKey(BiomeKeys.DEEP_DARK));
        BiomeModifications.addFeature(overworldSurface, GenerationStep.Feature.SURFACE_STRUCTURES, placed("bunker"));
        BiomeModifications.addFeature(overworldSurface, GenerationStep.Feature.SURFACE_STRUCTURES, placed("mine_camp"));

        // Minério abissal também no Deep Dark (de onde vêm os primeiros fragmentos)
        BiomeModifications.addFeature(BiomeSelectors.includeByKey(BiomeKeys.DEEP_DARK),
            GenerationStep.Feature.UNDERGROUND_ORES, placed("abyss_ore"));

        // Mobs em TODAS as dimensões (Abismo e Céus: ver data/arsenal/worldgen/biome/*.json)
        BiomeModifications.addSpawn(BiomeSelectors.foundInOverworld(), SpawnGroup.MONSTER, ModEntities.MOB.get(MobKind.RENEGADE), 40, 1, 3);
        BiomeModifications.addSpawn(BiomeSelectors.foundInOverworld(), SpawnGroup.CREATURE, ModEntities.MINER, 6, 1, 2);
        BiomeModifications.addSpawn(BiomeSelectors.foundInTheNether(), SpawnGroup.MONSTER, ModEntities.MOB.get(MobKind.STALKER), 25, 1, 2);
        BiomeModifications.addSpawn(BiomeSelectors.foundInTheEnd(), SpawnGroup.MONSTER, ModEntities.MOB.get(MobKind.SENTINEL), 15, 1, 2);
    }
}
