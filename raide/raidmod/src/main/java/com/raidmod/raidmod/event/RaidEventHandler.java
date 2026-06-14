package com.raidmod.raidmod.event;

import com.raidmod.raidmod.RaidMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Pillager;
import net.minecraft.world.entity.monster.Vindicator;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

import java.util.List;
import java.util.Optional;

public class RaidEventHandler {

    private static final long TICKS_PER_DAY = 24000L;

    private boolean warned49  = false;
    private boolean raided50  = false;
    private boolean warned99  = false;
    private boolean raided100 = false;

    @SubscribeEvent
    public void onLevelTick(LevelTickEvent.Post event) {
        Level level = event.getLevel();
        if (level.isClientSide()) return;
        if (!(level instanceof ServerLevel serverLevel)) return;
        if (!level.dimension().equals(Level.OVERWORLD)) return;

        List<ServerPlayer> players = serverLevel.players();
        if (players.isEmpty()) return;

        long dayTime    = serverLevel.getDayTime();
        long currentDay = (dayTime / TICKS_PER_DAY) + 1;
        long tickOfDay  = dayTime % TICKS_PER_DAY;

        if (currentDay == 49 && !warned49 && tickOfDay < 200) {
            warned49 = true;
            sendTitle(serverLevel,
                "§c§l⚠ UYARI! ⚠",
                "§eYarın yağmacılar baskın yapacak!",
                "§c§l⚠ UYARI! ⚠ §r§eYarın (50. gün) yağmacılar baskın yapacak! Hazırlanın!");
            RaidMod.LOGGER.info("[RaidMod] 49. gun uyarisi.");
        }

        if (currentDay == 50 && !raided50 && tickOfDay < 200) {
            raided50 = true;
            sendTitle(serverLevel,
                "§4§l☠ BASKIN! ☠",
                "§cYagmacilar saldiriyor!",
                "§4§l☠ BASKIN BASLADI! ☠ §r§cYagmacilar saldiriyor!");
            spawnArmy(serverLevel, false);
            RaidMod.LOGGER.info("[RaidMod] 50. gun baskini.");
        }

        if (currentDay == 99 && !warned99 && tickOfDay < 200) {
            warned99 = true;
            sendTitle(serverLevel,
                "§5§l✦ KARANLIK UYARI ✦",
                "§dYarin Kotu Kehanet V ordusu geliyor!",
                "§5§l✦ KARANLIK UYARI ✦ §r§dYarin (100. gun) seytani ordu geliyor! Kacmak imkansiz!");
            RaidMod.LOGGER.info("[RaidMod] 99. gun kotu kehanet uyarisi.");
        }

        if (currentDay == 100 && !raided100 && tickOfDay < 200) {
            raided100 = true;
            sendTitle(serverLevel,
                "§4§l☠ KOTU KEHANET V ☠",
                "§cSeytani ordu geliyor!",
                "§4§l☠☠ KOTU KEHANET V ORDUSU! ☠☠ §r§cKaranlik savascilar sizi ezecek!");
            spawnArmy(serverLevel, true);
            RaidMod.LOGGER.info("[RaidMod] 100. gun Kotu Kehanet V ordusu.");
        }
    }

    private void sendTitle(ServerLevel level, String title, String subtitle, String chat) {
        for (ServerPlayer player : level.players()) {
            player.sendSystemMessage(Component.literal(chat));
            player.connection.send(new ClientboundSetTitleTextPacket(Component.literal(title)));
            player.connection.send(new ClientboundSetSubtitleTextPacket(Component.literal(subtitle)));
            player.connection.send(new ClientboundSetTitlesAnimationPacket(10, 60, 20));
        }
    }

    private void spawnArmy(ServerLevel level, boolean elite) {
        for (ServerPlayer player : level.players()) {
            BlockPos pos = player.blockPosition();
            if (elite) {
                // Gün 100 - Kötü Kehanet V ordusu
                for (int i = 0; i < 8; i++)  spawnMob(level, EntityType.PILLAGER,   pos, i,      true);
                for (int i = 0; i < 5; i++)  spawnMob(level, EntityType.VINDICATOR, pos, i + 8,  true);
                for (int i = 0; i < 3; i++)  spawnMob(level, EntityType.WITCH,      pos, i + 13, true);
                for (int i = 0; i < 2; i++)  spawnMob(level, EntityType.RAVAGER,    pos, i + 16, true);
                for (int i = 0; i < 2; i++)  spawnMob(level, EntityType.EVOKER,     pos, i + 18, true);
            } else {
                // Gün 50 - Normal baskın
                for (int i = 0; i < 6; i++)  spawnMob(level, EntityType.PILLAGER,   pos, i,     false);
                for (int i = 0; i < 3; i++)  spawnMob(level, EntityType.VINDICATOR, pos, i + 6, false);
                spawnMob(level, EntityType.RAVAGER, pos, 9, false);
            }
        }
    }

    private void spawnMob(ServerLevel level, EntityType<?> type, BlockPos center,
                          int index, boolean elite) {
        int radius = elite ? (28 + index % 8) : (24 + index % 6);
        double angle = (2.0 * Math.PI * index) / 20.0;
        int dx = (int)(Math.cos(angle) * radius);
        int dz = (int)(Math.sin(angle) * radius);
        BlockPos pos = new BlockPos(center.getX() + dx, center.getY(), center.getZ() + dz);

        Entity raw = type.create(level);
        if (!(raw instanceof Mob mob)) return;

        mob.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
        mob.setPersistenceRequired();

        if (elite) {
            applyEliteEffects(level, mob, type);
        }

        level.addFreshEntity(mob);
    }

    private void applyEliteEffects(ServerLevel level, Mob mob, EntityType<?> type) {
        // Efektleri Holder ile al - NeoForge 1.21.1 uyumlu yol
        var registry = level.registryAccess().lookupOrThrow(Registries.MOB_EFFECT);

        applyEffect(registry, mob, "minecraft:strength",       3);
        applyEffect(registry, mob, "minecraft:speed",          1);
        applyEffect(registry, mob, "minecraft:health_boost",   3);
        applyEffect(registry, mob, "minecraft:fire_resistance", 0);

        if (type == EntityType.RAVAGER || type == EntityType.EVOKER) {
            applyEffect(registry, mob, "minecraft:absorption", 4);
        }

        // Pillager'a crossbow ekle
        if (mob instanceof Pillager pillager) {
            pillager.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.CROSSBOW));
        }

        // Vindicator'a diamond axe ekle
        if (mob instanceof Vindicator vindicator) {
            vindicator.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.DIAMOND_AXE));
        }
    }

    private void applyEffect(net.minecraft.core.Registry<MobEffect> registry,
                              Mob mob, String effectId, int amplifier) {
        Optional<Holder.Reference<MobEffect>> holder =
            registry.get(ResourceLocation.parse(effectId));
        holder.ifPresent(h ->
            mob.addEffect(new MobEffectInstance(h, Integer.MAX_VALUE, amplifier, false, false))
        );
    }
}
