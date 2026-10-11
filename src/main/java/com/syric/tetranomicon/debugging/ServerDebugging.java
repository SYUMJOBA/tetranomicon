package com.syric.tetranomicon.debugging;

import com.syric.tetranomicon.Tetranomicon;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.event.ServerChatEvent;
import se.mickelus.tetra.ConfigHandler;
import se.mickelus.tetra.data.DataManager;
import se.mickelus.tetra.data.predicate.TetraItemPredicate;
import se.mickelus.tetra.module.data.MaterialData;

import java.util.ArrayList;
import java.util.Arrays;

@EventBusSubscriber(modid = Tetranomicon.MODID, bus = EventBusSubscriber.Bus.GAME)
public class ServerDebugging {

    private static String modId(Item item) {
        return BuiltInRegistries.ITEM.getKey(item).getNamespace();
    }

    private static String itemId(Item item) {
        return BuiltInRegistries.ITEM.getKey(item).toString();
    }

    @SubscribeEvent
    public static void checkMissingMaterials(ServerChatEvent event) {
        if (!ConfigHandler.development.get()) {
            return;
        }
        String rawText = event.getRawText();
        if (rawText.contains("tetranomicon check missing")) {
            Tetranomicon.LOGGER.debug("Tetranomicon: scanning for missing materials...");
            for (MaterialData materialData : DataManager.instance.materialData.getData().values()) {
                if (materialData.material.getApplicableItemStacks().length == 0) {
                    Tetranomicon.LOGGER.debug("Material missing item: " + materialData.key);
                }
            }
        }
    }

    @SubscribeEvent
    public static void checkCandidates(ServerChatEvent event) {
        if (!ConfigHandler.development.get()) {
            return;
        }
        String rawText = event.getRawText();
        if (rawText.contains("tetranomicon check candidates")) {
            Tetranomicon.LOGGER.debug("Tetranomicon: scanning for unused candidates...");

            BuiltInRegistries.ITEM.entrySet().stream()
                    .filter(x -> {
                        Item item = x.getValue();
                        ItemStack stack = item.getDefaultInstance();
                        String item_id = itemId(item);
                        String mod_id = modId(item);
                        boolean likelyCandidateTags = (stack.is(Tags.Items.STONES) || stack.is(ItemTags.PLANKS) || stack.is(Tags.Items.INGOTS) || stack.is(Tags.Items.GEMS));
                        boolean likelyCandidateID = (item_id.contains("planks") || item_id.contains("ingot")) || item_id.contains("vine") || item_id.contains("bone");
                        boolean minecraft = mod_id == null || mod_id.contains("minecraft");
                        return likelyCandidateID || likelyCandidateTags && !minecraft;
                    })
                    .filter(x -> DataManager.instance.materialData.getData().values().stream().noneMatch(y -> {
                        TetraItemPredicate predicate = y.material.getPredicate();
                        if (predicate != null) {
                            if (y.material.isTagged()) {
                                return false;
                            }
                            return predicate.matches(x.getValue().getDefaultInstance());
                        }
                        return false;
                    }))
                    .filter(x -> !itemId(x.getValue()).contains("vertical"))
                    .filter(x -> !itemId(x.getValue()).contains("dyed"))
                    .filter(x -> !itemId(x.getValue()).contains("polished"))
                    .filter(x -> !itemId(x.getValue()).contains("slab"))
                    .filter(x -> !itemId(x.getValue()).contains("stairs"))
                    .filter(x -> !itemId(x.getValue()).contains("painted"))
                    .filter(x -> !itemId(x.getValue()).contains("mossy"))
                    .filter(x -> !itemId(x.getValue()).contains("olivine"))
                    .forEach(x -> Tetranomicon.LOGGER.debug("Candidate that is not a material: " + modId(x.getValue()) + ":" + itemId(x.getValue())));
        }
    }

    @SubscribeEvent
    public static void checkMissingReplacements(ServerChatEvent event) {
        if (!ConfigHandler.development.get()) {
            return;
        }
        String rawText = event.getRawText();
        if (rawText.contains("tetranomicon missing replacements")) {
            Tetranomicon.LOGGER.debug("Tetranomicon: scanning for missing replacements...");
            DataManager.instance.replacementData.getData().values().forEach(replacementList -> Arrays.stream(replacementList)
                    .filter(replacement -> BuiltInRegistries.ITEM.entrySet().stream()
                            .noneMatch(item -> replacement.predicate.matches(item.getValue().getDefaultInstance())))
                    .forEach(replacement -> Tetranomicon.LOGGER.debug("Replacement missing item: " + replacement.itemStack.toString())));
        }
    }

    @SubscribeEvent
    public static void checkReplacementCandidates(ServerChatEvent event) {
        if (!ConfigHandler.development.get()) {
            return;
        }
        String rawText = event.getRawText();
        if (rawText.contains("tetranomicon check replacement candidates")) {
            Tetranomicon.LOGGER.debug("Tetranomicon: scanning for replacement candidates...");

            ArrayList<TagKey<Item>> tagKeys = new ArrayList<>(Arrays.asList(Tags.Items.TOOLS, ItemTags.AXES, ItemTags.PICKAXES, ItemTags.SHOVELS, ItemTags.HOES, ItemTags.SWORDS, Tags.Items.TOOLS_BOW));
            ArrayList<String> tool_names = new ArrayList<>(Arrays.asList("_axe", "_pickaxe", "_shovel", "_pick", "_hoe", "_sword", "_bow"));

            BuiltInRegistries.ITEM.entrySet().stream()
                    .filter(x -> {
                        Item item = x.getValue();
                        ItemStack stack = item.getDefaultInstance();
                        String item_id = itemId(item);
                        String mod_id = modId(item);

                        boolean likelyCandidateTags = tagKeys.stream().anyMatch(stack::is);
                        boolean likelyCandidateStrings = tool_names.stream().anyMatch(item_id::contains);
                        boolean minecraft = mod_id == null || mod_id.contains("minecraft");

                        return (likelyCandidateStrings || likelyCandidateTags) && !minecraft;
                    })
                    .filter(x -> DataManager.instance.replacementData.getData().values().stream().noneMatch(replacementList -> {
                        ItemStack stack = x.getValue().getDefaultInstance();
                        return Arrays.stream(replacementList).anyMatch(replacement -> replacement.predicate.matches(stack));
                    }))
                    .forEach(x -> Tetranomicon.LOGGER.debug("Candidate tool not replaced: " + modId(x.getValue()) + ":" + itemId(x.getValue())));
        }
    }

}
