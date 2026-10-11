package com.syric.tetranomicon.registry;

import com.syric.tetranomicon.Tetranomicon;
import com.syric.tetranomicon.compat.BetterNetherCompat;
import com.syric.tetranomicon.compat.OresAboveDiamondsCompat;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

@EventBusSubscriber(
        modid = Tetranomicon.MODID,
        bus = EventBusSubscriber.Bus.MOD
)
public class TetranomiconItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(BuiltInRegistries.ITEM, Tetranomicon.MODID);
    public static final DeferredHolder<Item, Item> NETHERITE_OPAL = ITEMS.register("netherite_opal", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> CINCINNASITE_DIAMOND = ITEMS.register("cincinnasite_diamond", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> FLAMING_RUBY = ITEMS.register("flaming_ruby", () -> new Item(new Item.Properties()));

    public TetranomiconItems() {}

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }

    @SubscribeEvent
    public static void buildCreativeModeTabs(BuildCreativeModeTabContentsEvent event) {
        if (ModList.get().isLoaded("oresabovediamonds")) {
            OresAboveDiamondsCompat.buildOresAboveDiamondsCreativeModeTabs(event);
        }
        if (ModList.get().isLoaded("betternether")) {
            BetterNetherCompat.buildBetterNetherCreativeModeTabs(event);
        }
    }
}
