package com.syric.tetranomicon;

import com.syric.tetranomicon.registry.TetranomiconItems;
import com.syric.tetranomicon.registry.TetranomiconTiers;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(Tetranomicon.MODID)
public class Tetranomicon {
    public static final String MODID = "tetranomicon";
    public static final Logger LOGGER = LogManager.getLogger();

    public Tetranomicon(ModContainer container) {
        TetranomiconTiers.init();

        IEventBus modEventBus = container.getEventBus();
        if (ModList.get().isLoaded("oresabovediamonds") || ModList.get().isLoaded("betternether")) {
            TetranomiconItems.ITEMS.register(modEventBus);
        }
    }

}
