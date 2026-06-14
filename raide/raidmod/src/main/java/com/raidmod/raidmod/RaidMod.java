package com.raidmod.raidmod;

import com.mojang.logging.LogUtils;
import com.raidmod.raidmod.event.RaidEventHandler;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;

@Mod(RaidMod.MOD_ID)
public class RaidMod {

    public static final String MOD_ID = "raidmod";
    public static final Logger LOGGER = LogUtils.getLogger();

    public RaidMod(IEventBus modEventBus, ModContainer modContainer) {
        // Register server-side event handler
        NeoForge.EVENT_BUS.register(new RaidEventHandler());
        LOGGER.info("[RaidMod] Baskın Modu yüklendi! 49/50 ve 99/100. gün olayları aktif.");
    }
}
