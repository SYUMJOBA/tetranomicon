package com.syric.tetranomicon.compat;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;

import static com.syric.tetranomicon.registry.TetranomiconItems.NETHERITE_OPAL;

public class OresAboveDiamondsCompat {

    // Resolved by registry name rather than a compile-time dependency on Ores Above Diamonds, so the mod
    // still builds and runs without the OAD jar and only slots the netherite opal in when OAD is present.
    public static void buildOresAboveDiamondsCreativeModeTabs(BuildCreativeModeTabContentsEvent event) {
        ResourceKey<CreativeModeTab> tab = event.getTabKey();
        if (!tab.location().getNamespace().equals("oresabovediamonds")) {
            return;
        }
        Item blackOpal = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("oresabovediamonds", "black_opal"));
        // insertBefore throws if the anchor isn't in this tab, so only slot in where black opal actually is.
        boolean anchorPresent = blackOpal != Items.AIR
                && event.getParentEntries().stream().anyMatch(stack -> stack.getItem() == blackOpal);
        if (anchorPresent) {
            event.insertBefore(new ItemStack(blackOpal), new ItemStack(NETHERITE_OPAL.get()), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
        }
    }

}
