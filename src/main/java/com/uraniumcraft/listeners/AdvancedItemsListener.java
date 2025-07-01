package com.uraniumcraft.listeners;

import com.uraniumcraft.UraniumPlugin;
import com.uraniumcraft.gui.TabletGUI;
import com.uraniumcraft.items.UraniumTablet;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.Arrays;

public class AdvancedItemsListener implements Listener {
    private final UraniumPlugin plugin;
    
    public AdvancedItemsListener(UraniumPlugin plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        
        if (item == null || !item.hasItemMeta()) return;
        
        String displayName = item.getItemMeta().getDisplayName();
        
        // Обработка силовой брони
        if (displayName.contains("силовой брони")) {
            handlePowerArmorInteraction(player, item, event);
        }
        // Обработка рельсотрона
        else if (displayName.equals(ChatColor.RED + "Рельсотрон")) {
            handleRailgunInteraction(player, item, event);
        }
        // Обработка планшета
        else if (displayName.contains("планшет")) {
            handleTabletInteraction(player, item, event);
        }
        // Обработка дозиметра Гейгера
        else if (displayName.contains("Дозиметр Гейгера")) {
            handleGeigerCounterInteraction(player, item, event);
        }
    }
    
    private void handlePowerArmorInteraction(Player player, ItemStack item, PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        
        if (!player.isSneaking()) return; // Только с Shift
        
        ItemMeta meta = item.getItemMeta();
        NamespacedKey modeKey = new NamespacedKey(plugin, "armor_mode");
        String currentMode = meta.getPersistentDataContainer().getOrDefault(modeKey, PersistentDataType.STRING, "standard");
        
        String newMode = getNextArmorMode(currentMode);
        meta.getPersistentDataContainer().set(modeKey, PersistentDataType.STRING, newMode);
        
        // Обновляем описание
        updateArmorLore(meta, newMode);
        item.setItemMeta(meta);
        
        // Применяем эффекты режима
        applyArmorModeEffects(player, newMode);
        
        player.sendMessage(ChatColor.AQUA + "Режим силовой брони: " + ChatColor.YELLOW + newMode);
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.5f);
        event.setCancelled(true);
    }
    
    private void handleRailgunInteraction(Player player, ItemStack item, PlayerInteractEvent event) {
        ItemMeta meta = item.getItemMeta();
        NamespacedKey modeKey = new NamespacedKey(plugin, "weapon_mode");
        NamespacedKey energyKey = new NamespacedKey(plugin, "weapon_energy");
        NamespacedKey cooldownKey = new NamespacedKey(plugin, "weapon_cooldown");
        
        String currentMode = meta.getPersistentDataContainer().getOrDefault(modeKey, PersistentDataType.STRING, "single");
        int energy = meta.getPersistentDataContainer().getOrDefault(energyKey, PersistentDataType.INTEGER, 1000);
        long lastShot = meta.getPersistentDataContainer().getOrDefault(cooldownKey, PersistentDataType.LONG, 0L);
        
        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (player.isSneaking()) {
                // Переключение режима
                String newMode = getNextWeaponMode(currentMode);
                meta.getPersistentDataContainer().set(modeKey, PersistentDataType.STRING, newMode);
                updateRailgunLore(meta, newMode, energy);
                item.setItemMeta(meta);
                
                player.sendMessage(ChatColor.RED + "Режим рельсотрона: " + ChatColor.YELLOW + newMode);
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.2f);
            } else {
                // Стрельба
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastShot < 1000) { // Кулдаун 1 секунда
                    player.sendMessage(ChatColor.RED + "Перезарядка...");
                    return;
                }
                
                int energyCost = getWeaponEnergyCost(currentMode);
                if (energy < energyCost) {
                    player.sendMessage(ChatColor.RED + "Недостаточно энергии!");
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);
                    return;
                }
                
                // Стреляем
                fireRailgun(player, currentMode);
                
                // Обновляем энергию и кулдаун
                energy -= energyCost;
                meta.getPersistentDataContainer().set(energyKey, PersistentDataType.INTEGER, energy);
                meta.getPersistentDataContainer().set(cooldownKey, PersistentDataType.LONG, currentTime);
                updateRailgunLore(meta, currentMode, energy);
                item.setItemMeta(meta);
            }
            event.setCancelled(true);
        }
    }
    
    private void handleTabletInteraction(Player player, ItemStack item, PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        
        if (UraniumTablet.isUraniumTablet(item, plugin)) {
            if (player.isSneaking()) {
                // Быстрый статус
                showQuickStatus(player, item);
            } else {
                // Главное меню
                TabletGUI.openMainInterface(player, plugin);
            }
            event.setCancelled(true);
        }
    }

    private void handleGeigerCounterInteraction(Player player, ItemStack item, PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        int radiation = plugin.getRadiationManager().getRadiation(player.getUniqueId());

        player.sendMessage(ChatColor.BLUE + "=== ДОЗИМЕТР ГЕЙГЕРА ===");
        player.sendMessage(ChatColor.WHITE + "Текущий уровень радиации: " + getRadiationColor(radiation) + radiation + "%");

        String status;
        if (radiation == 0) {
            status = ChatColor.GREEN + "НОРМА - Радиационный фон в пределах нормы";
        } else if (radiation < 20) {
            status = ChatColor.YELLOW + "НИЗКИЙ - Слабое радиационное воздействие";
        } else if (radiation < 40) {
            status = ChatColor.GOLD + "СРЕДНИЙ - Умеренная радиация";
        } else if (radiation < 60) {
            status = ChatColor.RED + "ВЫСОКИЙ - Опасный уровень радиации";
        } else if (radiation < 80) {
            status = ChatColor.RED + "КРИТИЧЕСКИЙ - Очень опасно!";
        } else {
            status = ChatColor.DARK_RED + "СМЕРТЕЛЬНЫЙ - Немедленно покиньте зону!";
        }

        player.sendMessage(ChatColor.WHITE + "Статус: " + status);

        if (radiation > 0) {
            player.sendMessage(ChatColor.YELLOW + "Рекомендация: Используйте защитную экипировку");
        }

        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f + (radiation * 0.01f));
        event.setCancelled(true);
    }
    
    private void fireRailgun(Player player, String mode) {
        Location eyeLocation = player.getEyeLocation();
        Vector direction = eyeLocation.getDirection();
        
        // Звуковые и визуальные эффекты
        player.getWorld().playSound(eyeLocation, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 2.0f, 1.5f);
        player.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, eyeLocation, 20, 0.5, 0.5, 0.5, 0.1);
        player.getWorld().spawnParticle(Particle.FLASH, eyeLocation, 5);
        
        switch (mode) {
            case "single":
                fireSingleShot(player, eyeLocation, direction);
                break;
            case "burst":
                fireBurstShot(player, eyeLocation, direction);
                break;
            case "piercing":
                firePiercingShot(player, eyeLocation, direction);
                break;
        }
    }
    
    private void fireSingleShot(Player player, Location start, Vector direction) {
        // Создаем мощный снаряд
        Arrow arrow = player.getWorld().spawnArrow(start, direction, 3.0f, 0.0f);
        arrow.setDamage(25.0); // 12.5 сердец урона
        arrow.setPierceLevel(0);
        arrow.setCritical(true);
        arrow.setShooter(player);
        
        // Эффекты полета
        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (arrow.isDead() || arrow.isOnGround() || ticks > 100) {
                    cancel();
                    return;
                }
                
                Location loc = arrow.getLocation();
                arrow.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, loc, 3, 0.1, 0.1, 0.1, 0.05);
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
    
    private void fireBurstShot(Player player, Location start, Vector direction) {
        // Стреляем 3 снаряда с небольшой задержкой
        for (int i = 0; i < 3; i++) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    Vector spreadDirection = direction.clone();
                    spreadDirection.add(new Vector(
                        (Math.random() - 0.5) * 0.2,
                        (Math.random() - 0.5) * 0.2,
                        (Math.random() - 0.5) * 0.2
                    ));
                    
                    Arrow arrow = player.getWorld().spawnArrow(start, spreadDirection, 2.5f, 0.1f);
                    arrow.setDamage(15.0); // 7.5 сердец урона
                    arrow.setShooter(player);
                    
                    player.getWorld().playSound(start, Sound.ENTITY_FIREWORK_ROCKET_BLAST, 1.0f, 1.8f);
                }
            }.runTaskLater(plugin, i * 3L);
        }
    }
    
    private void firePiercingShot(Player player, Location start, Vector direction) {
        // Создаем пробивающий снаряд
        Arrow arrow = player.getWorld().spawnArrow(start, direction, 2.8f, 0.0f);
        arrow.setDamage(20.0); // 10 сердец урона
        arrow.setPierceLevel(10); // Пробивает много целей
        arrow.setShooter(player);
        
        // Специальные эффекты для пробивающего режима
        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (arrow.isDead() || arrow.isOnGround() || ticks > 150) {
                    cancel();
                    return;
                }
                
                Location loc = arrow.getLocation();
                arrow.getWorld().spawnParticle(Particle.DRAGON_BREATH, loc, 5, 0.2, 0.2, 0.2, 0.02);
                arrow.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, loc, 2, 0.1, 0.1, 0.1, 0.05);
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
    
    private void showQuickStatus(Player player, ItemStack tablet) {
        UraniumTablet.TabletType type = UraniumTablet.getTabletType(tablet, plugin);
        int energy = UraniumTablet.getTabletEnergy(tablet, plugin);
        int radiation = plugin.getRadiationManager().getRadiation(player.getUniqueId());
        
        player.sendMessage(ChatColor.AQUA + "=== СТАТУС ПЛАНШЕТА ===");
        player.sendMessage(ChatColor.WHITE + "Энергия: " + ChatColor.YELLOW + energy + "/2000");
        player.sendMessage(ChatColor.WHITE + "Радиация: " + getRadiationColor(radiation) + radiation + "%");
        player.sendMessage(ChatColor.WHITE + "Тип: " + (type != null ? type.getDisplayName() : "Стандартный"));
        
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.8f, 1.5f);
    }
    
    // Вспомогательные методы для переключения режимов
    private String getNextArmorMode(String currentMode) {
        switch (currentMode) {
            case "standard": return "protection";
            case "protection": return "speed";
            case "speed": return "jump";
            case "jump": return "standard";
            default: return "standard";
        }
    }
    
    private String getNextWeaponMode(String currentMode) {
        switch (currentMode) {
            case "single": return "burst";
            case "burst": return "piercing";
            case "piercing": return "single";
            default: return "single";
        }
    }
    
    private int getWeaponEnergyCost(String mode) {
        switch (mode) {
            case "single": return 100;
            case "burst": return 200;
            case "piercing": return 150;
            default: return 100;
        }
    }
    
    // Методы для применения эффектов
    private void applyArmorModeEffects(Player player, String mode) {
        // Убираем старые эффекты
        player.removePotionEffect(PotionEffectType.DAMAGE_RESISTANCE);
        player.removePotionEffect(PotionEffectType.SPEED);
        player.removePotionEffect(PotionEffectType.JUMP);
        
        switch (mode) {
            case "protection":
                player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, Integer.MAX_VALUE, 2, false, false));
                break;
            case "speed":
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 2, false, false));
                break;
            case "jump":
                player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, Integer.MAX_VALUE, 2, false, false));
                break;
        }
    }
    
    private ChatColor getRadiationColor(int level) {
        if (level < 20) return ChatColor.GREEN;
        if (level < 40) return ChatColor.YELLOW;
        if (level < 60) return ChatColor.GOLD;
        if (level < 80) return ChatColor.RED;
        return ChatColor.DARK_RED;
    }
    
    // Методы для обновления описаний предметов
    private void updateArmorLore(ItemMeta meta, String mode) {
        meta.setLore(Arrays.asList(
            ChatColor.GRAY + "Продвинутая защита от радиации",
            ChatColor.GRAY + "Встроенный HUD дисплей",
            ChatColor.GREEN + "Защита от радиации: 95%",
            ChatColor.YELLOW + "Shift+ПКМ - переключение режима",
            ChatColor.BLUE + "Режим: " + mode,
            ChatColor.GOLD + "Энергия: 10000/10000"
        ));
    }
    
    private void updateRailgunLore(ItemMeta meta, String mode, int energy) {
        meta.setLore(Arrays.asList(
            ChatColor.GRAY + "Электромагнитное оружие",
            ChatColor.YELLOW + "ПКМ - стрельба",
            ChatColor.YELLOW + "Shift+ПКМ - переключение режима",
            ChatColor.BLUE + "Режим: " + mode,
            ChatColor.RED + "Урон: Экстремальный",
            ChatColor.GOLD + "Энергия: " + energy + "/1000"
        ));
    }

    public static class VisualEffects {
        public static void playPlayerNotificationEffect(Player player) {
            if (player != null && player.isOnline()) {
                player.spawnParticle(Particle.VILLAGER_HAPPY, player.getLocation().add(0, 2, 0), 10, 1, 1, 1, 0.1);
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.5f);
            }
        }
        
        public static void playTeleporterActivation(Location location) {
            if (location.getWorld() != null) {
                location.getWorld().spawnParticle(Particle.PORTAL, location, 30, 2, 2, 2, 0.5);
                location.getWorld().playSound(location, Sound.BLOCK_PORTAL_TRIGGER, 1.0f, 1.0f);
            }
        }
        
        public static void playTeleporterDeactivation(Location location) {
            if (location.getWorld() != null) {
                location.getWorld().spawnParticle(Particle.SMOKE_LARGE, location, 20, 1, 1, 1, 0.1);
                location.getWorld().playSound(location, Sound.BLOCK_FIRE_EXTINGUISH, 1.0f, 0.5f);
            }
        }
        
        public static void playTeleporterAmbient(Location location) {
            if (location.getWorld() != null) {
                location.getWorld().spawnParticle(Particle.PORTAL, location.clone().add(0, 1, 0), 3, 0.5, 0.5, 0.5, 0.1);
            }
        }
        
        public static void playTeleportCountdown(Location location, int countdown) {
            if (location.getWorld() != null) {
                location.getWorld().spawnParticle(Particle.ENCHANTMENT_TABLE, location, countdown * 2, 0.5, 0.5, 0.5, 0.1);
            }
        }
        
        public static void playTeleportationEffect(Location location) {
            if (location.getWorld() != null) {
                location.getWorld().spawnParticle(Particle.PORTAL, location, 50, 2, 2, 2, 1.0);
                location.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, location, 20, 1, 1, 1, 0.5);
            }
        }
    }
}
