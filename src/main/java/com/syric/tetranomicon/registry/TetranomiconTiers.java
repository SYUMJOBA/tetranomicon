package com.syric.tetranomicon.registry;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.SimpleTier;
import se.mickelus.tetra.TetraRegistries;
import se.mickelus.tetra.tools.HarvestTierRegistry;

import java.util.List;

public class TetranomiconTiers {

    public static void init() {
        // Tetranomicon adds four tiers above Tetra's maxed forge hammer, chained by the after/before DAG
        // (there is no harvest-level int in 1.21). Each tier's SimpleTier takes an "incorrect for drops"
        // block tag (the inverse of the old ForgeTier "needs this tier" tag); shipping those tags empty /
        // pointing at the forge hammer's incorrect set makes these top tiers mine everything it can and more.
        Tier eightTier = registerTier("tier_eight", "incorrect_for_tier_eight_tool",
                List.of(TetraRegistries.forgeHammerTier));

        Tier nineTier = registerTier("tier_nine", "incorrect_for_tier_nine_tool",
                List.of(eightTier));

        Tier tenTier = registerTier("tier_ten", "incorrect_for_tier_ten_tool",
                List.of(nineTier));

        registerTier("tier_eleven", "incorrect_for_tier_eleven_tool",
                List.of(tenTier));
    }

    private static Tier registerTier(String name, String incorrectTagPath, List<Tier> after) {
        TagKey<Block> incorrectTag = BlockTags.create(ResourceLocation.fromNamespaceAndPath("tetranomicon", incorrectTagPath));
        return HarvestTierRegistry.register(
                new SimpleTier(incorrectTag, 0, 0, 0, 0, () -> Ingredient.EMPTY),
                ResourceLocation.fromNamespaceAndPath("tetranomicon", name),
                after,
                List.of());
    }

}
