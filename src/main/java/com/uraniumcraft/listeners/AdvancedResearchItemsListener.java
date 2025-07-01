package com.uraniumcraft.listeners;

import com.uraniumcraft.UraniumPlugin;
import com.uraniumcraft.gui.TeleporterGUI;
import com.uraniumcraft.items.AdvancedResearchItems;
import com.uraniumcraft.radiation.RadiationManager;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class AdvancedResearchItemsListener implements Listener {
    
    private final UraniumPlugin plugin;
    private final Map<UUID, Long> cooldowns = new HashMap<>();
    private final Map<UUID, Integer> itemEnergy = new HashMap<>();
    
    public AdvancedResearchItemsListener(UraniumPlugin plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        
        if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) {
            return;
        }
        
        String displayName = item.getItemMeta().getDisplayName();
        Action action = event.getAction();
        
        if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
            handleItemUse(player, item, displayName, event);
        }
    }
    
    private void handleItemUse(Player player, ItemStack item, String displayName, PlayerInteractEvent event) {
        UUID playerId = player.getUniqueId();
        
        // Проверка кулдауна
        if (cooldowns.containsKey(playerId)) {
            long timeLeft = cooldowns.get(playerId) - System.currentTimeMillis();
            if (timeLeft > 0) {
                player.sendMessage(ChatColor.RED + "Подождите " + (timeLeft / 1000) + " секунд!");
                return;
            }
        }
        
        switch (displayName) {
            case ChatColor.LIGHT_PURPLE + "Квантовый процессор":
                handleQuantumProcessor(player, item);
                break;
                
            case ChatColor.DARK_PURPLE + "Квантовый телепорт":
                handleTeleporter(player, item);
                break;
                
            case ChatColor.GREEN + "Нано-лечебная камера":
                handleHealingChamber(player, item);
                break;
                
            case ChatColor.YELLOW + "Нейтрализатор радиации":
                handleRadiationNeutralizer(player, item);
                break;
                
            case ChatColor.RED + "Энергетический усилитель":
                handleEnergyAmplifier(player, item);
                break;
                
            case ChatColor.DARK_GREEN + "Био-сканер":
                handleBioScanner(player, item);
                break;
                
            case ChatColor.DARK_PURPLE + "Квантовый реактор":
                handleQuantumReactor(player, item, event);
                break;
                
            case ChatColor.GOLD + "Нано-ремонтный набор":
                handleNanoRepairKit(player, item);
                break;
                
            case ChatColor.AQUA + "Голографический дисплей":
                handleHolographicDisplay(player, item);
                break;
        }
    }
    
    private void handleQuantumProcessor(Player player, ItemStack item) {
        if (!consumeEnergy(player, item, 50)) return;
        
        // Ускорение процессов в радиусе 10 блоков
        Location loc = player.getLocation();
        
        // Эффекты для игрока
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 600, 1));
        player.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, 600, 2));
        
        // Визуальные эффекты
        loc.getWorld().spawnParticle(Particle.ENCHANTMENT_TABLE, loc, 30, 2, 2, 2, 0.1);
        player.playSound(loc, Sound.BLOCK_BEACON_ACTIVATE, 1.0f, 1.5f);
        
        player.sendMessage(ChatColor.LIGHT_PURPLE + "Квантовый процессор активирован!");
        player.sendMessage(ChatColor.GREEN + "Получены эффекты ускорения!");
        
        setCooldown(player.getUniqueId(), 30000); // 30 секунд
    }
    
    private void handleTeleporter(Player player, ItemStack item) {
        if (!consumeEnergy(player, item, 50)) return;
        
        TeleporterGUI.openTeleporterMenu(player);
        player.playSound(player.getLocation(), Sound.BLOCK_PORTAL_AMBIENT, 1.0f, 1.0f);
    }
    
    private void handleHealingChamber(Player player, ItemStack item) {
        if (!consumeEnergy(player, item, 100)) return;
        
        Location loc = player.getLocation();
        
        // Лечение игрока и ближайших союзников
        player.setHealth(Math.min(player.getMaxHealth(), player.getHealth() + 10));
        
        for (Entity entity : loc.getWorld().getNearbyEntities(loc, 5, 5, 5)) {
            if (entity instanceof Player && entity != player) {
                Player nearbyPlayer = (Player) entity;
                nearbyPlayer.setHealth(Math.min(nearbyPlayer.getMaxHealth(), nearbyPlayer.getHealth() + 8));
                nearbyPlayer.sendMessage(ChatColor.GREEN + "Вы получили лечение от нано-камеры!");
            }
        }
        
        // Удаление негативных эффектов
        player.removePotionEffect(PotionEffectType.POISON);
        player.removePotionEffect(PotionEffectType.WITHER);
        
        // Визуальные эффекты
        loc.getWorld().spawnParticle(Particle.HEART, loc.add(0, 1, 0), 20, 1, 1, 1, 0.1);
        loc.getWorld().spawnParticle(Particle.TOTEM, loc, 15, 1, 1, 1, 0.1);
        player.playSound(loc, Sound.BLOCK_BEACON_POWER_SELECT, 1.0f, 1.2f);
        
        player.sendMessage(ChatColor.GREEN + "Нано-лечебная камера активирована!");
        player.sendMessage(ChatColor.AQUA + "Здоровье восстановлено!");
        
        setCooldown(player.getUniqueId(), 45000); // 45 секунд
    }
    
    private void handleRadiationNeutralizer(Player player, ItemStack item) {
        if (!consumeEnergy(player, item, 200)) return;
        
        RadiationManager radiationManager = plugin.getRadiationManager();
        Location loc = player.getLocation();
        
        // Очистка радиации у игрока и в радиусе
        radiationManager.removeRadiation(player.getUniqueId(), 50);
        
        for (Entity entity : loc.getWorld().getNearbyEntities(loc, 10, 10, 10)) {
            if (entity instanceof Player) {
                Player nearbyPlayer = (Player) entity;
                radiationManager.removeRadiation(nearbyPlayer.getUniqueId(), 30);
                nearbyPlayer.sendMessage(ChatColor.YELLOW + "Радиация нейтрализована!");
            }
        }
        
        // Визуальные эффекты
        loc.getWorld().spawnParticle(Particle.END_ROD, loc.add(0, 2, 0), 50, 5, 5, 5, 0.1);
        loc.getWorld().spawnParticle(Particle.FIREWORKS_SPARK, loc, 30, 3, 3, 3, 0.2);
        player.playSound(loc, Sound.BLOCK_CONDUIT_ACTIVATE, 1.0f, 0.8f);
        
        player.sendMessage(ChatColor.YELLOW + "Нейтрализатор радиации активирован!");
        player.sendMessage(ChatColor.GREEN + "Территория очищена от радиации!");
        
        setCooldown(player.getUniqueId(), 60000); // 60 секунд
    }
    
    private void handleEnergyAmplifier(Player player, ItemStack item) {
        if (!consumeEnergy(player, item, 75)) return;
        
        Location loc = player.getLocation();
        
        // Усиление эффектов в радиусе
        for (Entity entity : loc.getWorld().getNearbyEntities(loc, 7, 7, 7)) {
            if (entity instanceof Player) {
                Player nearbyPlayer = (Player) entity;
                
                // Усиление существующих эффектов
                for (PotionEffect effect : nearbyPlayer.getActivePotionEffects()) {
                    if (effect.getType().equals(PotionEffectType.SPEED) || 
                        effect.getType().equals(PotionEffectType.HASTE) ||
                        effect.getType().equals(PotionEffectType.STRENGTH)) {
                        
                        nearbyPlayer.removePotionEffect(effect.getType());
                        nearbyPlayer.addPotionEffect(new PotionEffect(
                            effect.getType(), 
                            effect.getDuration() + 200, 
                            Math.min(effect.getAmplifier() + 1, 3)
                        ));
                    }
                }
                
                nearbyPlayer.sendMessage(ChatColor.RED + "Ваши способности усилены!");
            }
        }
        
        // Визуальные эффекты
        loc.getWorld().spawnParticle(Particle.REDSTONE, loc.add(0, 1, 0), 40, 2, 2, 2, 0.1);
        loc.getWorld().spawnParticle(Particle.CRIT_MAGIC, loc, 25, 1.5, 1.5, 1.5, 0.1);
        player.playSound(loc, Sound.BLOCK_REDSTONE_TORCH_BURNOUT, 1.0f, 0.5f);
        
        player.sendMessage(ChatColor.RED + "Энергетический усилитель активирован!");
        
        setCooldown(player.getUniqueId(), 40000); // 40 секунд
    }
    
    private void handleBioScanner(Player player, ItemStack item) {
        if (!consumeEnergy(player, item, 25)) return;
        
        Location loc = player.getLocation();
        RadiationManager radiationManager = plugin.getRadiationManager();
        
        player.sendMessage(ChatColor.DARK_GREEN + "=== БИО-СКАНЕР ===");
        
        // Сканирование ближайших существ
        boolean foundEntities = false;
        for (Entity entity : loc.getWorld().getNearbyEntities(loc, 20, 20, 20)) {
            if (entity instanceof LivingEntity && entity != player) {
                LivingEntity living = (LivingEntity) entity;
                double distance = loc.distance(entity.getLocation());
                
                String entityName = entity.getType().name();
                double health = living.getHealth();
                double maxHealth = living.getMaxHealth();
                
                player.sendMessage(ChatColor.WHITE + entityName + ChatColor.GRAY + " (" + 
                    String.format("%.1f", distance) + "м)");
                player.sendMessage(ChatColor.RED + "  Здоровье: " + 
                    String.format("%.1f/%.1f", health, maxHealth));
                
                if (entity instanceof Player) {
                    Player targetPlayer = (Player) entity;
                    int radiation = radiationManager.getPlayerRadiation(targetPlayer.getUniqueId());
                    player.sendMessage(ChatColor.YELLOW + "  Радиация: " + radiation + "%");
                }
                
                foundEntities = true;
            }
        }
        
        if (!foundEntities) {
            player.sendMessage(ChatColor.GRAY + "Живых существ не обнаружено");
        }
        
        // Визуальные эффекты
        loc.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, loc.add(0, 1, 0), 15, 1, 1, 1, 0.1);
        player.playSound(loc, Sound.BLOCK_NOTE_BLOCK_CHIME, 1.0f, 2.0f);
        
        setCooldown(player.getUniqueId(), 10000); // 10 секунд
    }
    
    private void handleQuantumReactor(Player player, ItemStack item, PlayerInteractEvent event) {
        if (event.getClickedBlock() == null) {
            player.sendMessage(ChatColor.RED + "Установите реактор на блок!");
            return;
        }
        
        Location loc = event.getClickedBlock().getLocation().add(0, 1, 0);
        
        // Проверка топлива в инвентаре
        boolean hasFuel = false;
        for (ItemStack invItem : player.getInventory().getContents()) {
            if (invItem != null && invItem.hasItemMeta() && 
                invItem.getItemMeta().getDisplayName().contains("Обогащённый уран")) {
                hasFuel = true;
                invItem.setAmount(invItem.getAmount() - 1);
                break;
            }
        }
        
        if (!hasFuel) {
            player.sendMessage(ChatColor.RED + "Нужно обогащённое урановое топливо!");
            return;
        }
        
        // Установка реактора
        loc.getBlock().setType(Material.RESPAWN_ANCHOR);
        
        // Запуск производства энергии
        new BukkitRunnable() {
            int duration = 300; // 15 секунд
            
            @Override
            public void run() {
                if (duration <= 0 || loc.getBlock().getType() != Material.RESPAWN_ANCHOR) {
                    loc.getBlock().setType(Material.AIR);
                    this.cancel();
                    return;
                }
                
                // Производство энергии для ближайших устройств
                for (Entity entity : loc.getWorld().getNearbyEntities(loc, 15, 15, 15)) {
                    if (entity instanceof Player) {
                        Player nearbyPlayer = (Player) entity;
                        // Зарядка энергетических предметов в инвентаре
                        chargeEnergyItems(nearbyPlayer);
                    }
                }
                
                // Эффекты
                if (duration % 20 == 0) { // Каждую секунду
                    loc.getWorld().spawnParticle(Particle.PORTAL, loc, 10, 0.5, 0.5, 0.5, 0.1);
                    loc.getWorld().playSound(loc, Sound.BLOCK_BEACON_AMBIENT, 0.5f, 1.5f);
                }
                
                duration--;
            }
        }.runTaskTimer(plugin, 0L, 1L);
        
        player.sendMessage(ChatColor.DARK_PURPLE + "Квантовый реактор запущен!");
        player.sendMessage(ChatColor.GREEN + "Производство энергии: 100 ед/сек");
        
        event.setCancelled(true);
    }
    
    private void handleNanoRepairKit(Player player, ItemStack item) {
        if (!consumeEnergy(player, item, 50)) return;
        
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        ItemStack offHand = player.getInventory().getItemInOffHand();
        
        ItemStack toRepair = null;
        if (mainHand != item && mainHand.getType().getMaxDurability() > 0) {
            toRepair = mainHand;
        } else if (offHand != item && offHand.getType().getMaxDurability() > 0) {
            toRepair = offHand;
        }
        
        if (toRepair == null) {
            player.sendMessage(ChatColor.RED + "Возьмите предмет для ремонта в другую руку!");
            return;
        }
        
        // Полный ремонт
        toRepair.setDurability((short) 0);
        
        // Визуальные эффекты
        Location loc = player.getLocation();
        loc.getWorld().spawnParticle(Particle.CRIT_MAGIC, loc.add(0, 1, 0), 20, 0.5, 0.5, 0.5, 0.1);
        player.playSound(loc, Sound.BLOCK_ANVIL_USE, 1.0f, 1.5f);
        
        player.sendMessage(ChatColor.GOLD + "Предмет полностью восстановлен!");
        
        setCooldown(player.getUniqueId(), 20000); // 20 секунд
    }
    
    private void handleHolographicDisplay(Player player, ItemStack item) {
        if (!consumeEnergy(player, item, 10)) return;
        
        RadiationManager radiationManager = plugin.getRadiationManager();
        
        // Отображение голографической информации
        player.sendTitle(
            ChatColor.AQUA + "ГОЛОГРАФИЧЕСКИЙ ДИСПЛЕЙ",
            ChatColor.WHITE + "Анализ окружающей среды...",
            10, 60, 10
        );
        
        new BukkitRunnable() {
            @Override
            public void run() {
                player.sendMessage(ChatColor.AQUA + "=== ГОЛОГРАФИЧЕСКИЕ ДАННЫЕ ===");
                player.sendMessage(ChatColor.WHITE + "Игрок: " + player.getName());
                player.sendMessage(ChatColor.RED + "Здоровье: " + 
                    String.format("%.1f/%.1f", player.getHealth(), player.getMaxHealth()));
                player.sendMessage(ChatColor.YELLOW + "Радиация: " + 
                    radiationManager.getPlayerRadiation(player.getUniqueId()) + "%");
                player.sendMessage(ChatColor.GREEN + "Координаты: " + 
                    player.getLocation().getBlockX() + ", " + 
                    player.getLocation().getBlockY() + ", " + 
                    player.getLocation().getBlockZ());
                player.sendMessage(ChatColor.BLUE + "Мир: " + player.getWorld().getName());
                player.sendMessage(ChatColor.LIGHT_PURPLE + "Время: " + 
                    player.getWorld().getTime());
            }
        }.runTaskLater(plugin, 20L);
        
        // Визуальные эффекты
        Location loc = player.getLocation();
        loc.getWorld().spawnParticle(Particle.END_ROD, loc.add(0, 2, 0), 15, 1, 1, 1, 0.05);
        player.playSound(loc, Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 2.0f);
        
        setCooldown(player.getUniqueId(), 15000); // 15 секунд
    }
    
    private boolean consumeEnergy(Player player, ItemStack item, int energyCost) {
        ItemMeta meta = item.getItemMeta();
        List<String> lore = meta.getLore();
        
        // Поиск строки с энергией
        for (int i = 0; i < lore.size(); i++) {
            String line = lore.get(i);
            if (line.contains("Энергия:")) {
                String[] parts = line.split("/");
                if (parts.length == 2) {
                    try {
                        int currentEnergy = Integer.parseInt(parts[0].replaceAll("[^0-9]", ""));
                        int maxEnergy = Integer.parseInt(parts[1].replaceAll("[^0-9]", ""));
                        
                        if (currentEnergy < energyCost) {
                            player.sendMessage(ChatColor.RED + "Недостаточно энергии! Нужно: " + energyCost);
                            return false;
                        }
                        
                        // Обновление энергии
                        int newEnergy = currentEnergy - energyCost;
                        lore.set(i, ChatColor.BLUE + "Энергия: " + newEnergy + "/" + maxEnergy);
                        meta.setLore(lore);
                        item.setItemMeta(meta);
                        
                        return true;
                    } catch (NumberFormatException e) {
                        player.sendMessage(ChatColor.RED + "Ошибка чтения энергии!");
                        return false;
                    }
                }
            }
        }
        
        return false;
    }
    
    private void chargeEnergyItems(Player player) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.hasItemMeta() && item.getItemMeta().hasLore()) {
                ItemMeta meta = item.getItemMeta();
                List<String> lore = meta.getLore();
                
                for (int i = 0; i < lore.size(); i++) {
                    String line = lore.get(i);
                    if (line.contains("Энергия:")) {
                        String[] parts = line.split("/");
                        if (parts.length == 2) {
                            try {
                                int currentEnergy = Integer.parseInt(parts[0].replaceAll("[^0-9]", ""));
                                int maxEnergy = Integer.parseInt(parts[1].replaceAll("[^0-9]", ""));
                                
                                if (currentEnergy < maxEnergy) {
                                    int newEnergy = Math.min(maxEnergy, currentEnergy + 10);
                                    lore.set(i, ChatColor.BLUE + "Энергия: " + newEnergy + "/" + maxEnergy);
                                    meta.setLore(lore);
                                    item.setItemMeta(meta);
                                }
                            } catch (NumberFormatException ignored) {}
                        }
                        break;
                    }
                }
            }
        }
    }
    
    private void setCooldown(UUID playerId, long milliseconds) {
        cooldowns.put(playerId, System.currentTimeMillis() + milliseconds);
    }
}
