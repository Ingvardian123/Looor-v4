package com.uraniumcraft.radiation;

import com.uraniumcraft.UraniumPlugin;
import com.uraniumcraft.items.UraniumItems;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RadiationManager {
    
    private final UraniumPlugin plugin;
    private final Map<UUID, Integer> playerRadiation;
    private File dataFile;
    private FileConfiguration dataConfig;
    
    public RadiationManager(UraniumPlugin plugin) {
        this.plugin = plugin;
        this.playerRadiation = new HashMap<>();
        setupDataFile();
        loadData();
    }
    
    private void setupDataFile() {
        dataFile = new File(plugin.getDataFolder(), "radiation.yml");
        if (!dataFile.exists()) {
            try {
                dataFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Не удалось создать файл radiation.yml");
            }
        }
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
    }
    
    public void addRadiation(Player player, int amount) {
        UUID playerId = player.getUniqueId();
        int currentRadiation = playerRadiation.getOrDefault(playerId, 0);
        int newRadiation = Math.min(100, currentRadiation + amount);
        playerRadiation.put(playerId, newRadiation);
        
        // Уведомляем игрока
        if (amount > 0) {
            player.sendMessage(ChatColor.RED + "☢ Получена радиация: +" + amount + " (Всего: " + newRadiation + ")");
            
            // Эффекты радиации
            if (newRadiation >= 80) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 200, 1));
                player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 200, 1));
                player.sendMessage(ChatColor.DARK_RED + "⚠ КРИТИЧЕСКИЙ УРОВЕНЬ РАДИАЦИИ!");
            } else if (newRadiation >= 60) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 100, 0));
                player.sendMessage(ChatColor.RED + "⚠ Высокий уровень радиации!");
            } else if (newRadiation >= 40) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, 100, 0));
                player.sendMessage(ChatColor.GOLD + "⚠ Повышенная радиация!");
            }
            
            // Визуальные эффекты
            player.spawnParticle(Particle.REDSTONE, player.getLocation().add(0, 1, 0), 
                10, 0.5, 0.5, 0.5, 0, new Particle.DustOptions(org.bukkit.Color.RED, 1.0f));
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.5f, 0.5f);
        }
    }
    
    public void removeRadiation(Player player, int amount) {
        UUID playerId = player.getUniqueId();
        int currentRadiation = playerRadiation.getOrDefault(playerId, 0);
        int newRadiation = Math.max(0, currentRadiation - amount);
        playerRadiation.put(playerId, newRadiation);
        
        if (amount > 0) {
            player.sendMessage(ChatColor.GREEN + "✓ Радиация снижена: -" + amount + " (Всего: " + newRadiation + ")");
            player.spawnParticle(Particle.HEART, player.getLocation().add(0, 1, 0), 5, 0.3, 0.3, 0.3, 0);
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.5f);
        }
    }
    
    public int getRadiation(Player player) {
        return getRadiation(player.getUniqueId());
    }
    
    public int getRadiation(UUID playerId) {
        return playerRadiation.getOrDefault(playerId, 0);
    }

    // Добавить метод для совместимости
    public int getRadiationLevel(UUID playerId) {
        return getRadiation(playerId);
    }
    
    public void updateAllPlayers() {
        Collection<? extends Player> players = Bukkit.getOnlinePlayers();
        if (players.isEmpty()) return;
        
        for (Player player : players) {
            try {
                updatePlayerRadiation(player);
            } catch (Exception e) {
                plugin.getLogger().warning("Ошибка при обновлении радиации игрока " + player.getName() + ": " + e.getMessage());
            }
        }
    }
    
    private void updatePlayerRadiation(Player player) {
        UUID playerId = player.getUniqueId();
        
        // Проверяем радиацию от предметов в инвентаре
        int inventoryRadiation = calculateInventoryRadiation(player);
        
        // Проверяем защиту от брони
        int protection = calculateRadiationProtection(player);
        
        // Рассчитываем итоговую радиацию
        int finalRadiation = Math.max(0, inventoryRadiation - protection);
        
        if (finalRadiation > 0) {
            addRadiation(player, 1); // Медленное накопление
        } else if (protection > 0) {
            int currentRadiation = getRadiation(playerId);
            if (currentRadiation > 0) {
                // Если есть защита, медленно снижаем радиацию
                removeRadiation(player, 1);
            }
        }
        
        // Показываем ActionBar с уровнем радиации только если есть радиация
        int currentRadiation = getRadiation(playerId);
        if (currentRadiation > 0) {
            String radiationBar = createRadiationBar(currentRadiation);
            player.sendActionBar(ChatColor.RED + "☢ Радиация: " + radiationBar + " " + currentRadiation + "%");
        }
    }
    
    private int calculateInventoryRadiation(Player player) {
        int totalRadiation = 0;
        ItemStack[] contents = player.getInventory().getContents();
        
        for (ItemStack item : contents) {
            if (item != null && item.getAmount() > 0) {
                int itemRadiation = UraniumItems.getItemRadiation(item);
                if (itemRadiation > 0) {
                    totalRadiation += itemRadiation * item.getAmount();
                }
            }
        }
        
        return totalRadiation;
    }
    
    private int calculateRadiationProtection(Player player) {
        int protection = 0;
        
        ItemStack helmet = player.getInventory().getHelmet();
        ItemStack chestplate = player.getInventory().getChestplate();
        ItemStack leggings = player.getInventory().getLeggings();
        ItemStack boots = player.getInventory().getBoots();
        
        if (isHazmatArmor(helmet)) protection += 25;
        if (isHazmatArmor(chestplate)) protection += 40;
        if (isHazmatArmor(leggings)) protection += 20;
        if (isHazmatArmor(boots)) protection += 15;
        
        if (isPowerArmor(helmet)) protection += 22;
        if (isPowerArmor(chestplate)) protection += 38;
        if (isPowerArmor(leggings)) protection += 17;
        if (isPowerArmor(boots)) protection += 13;
        
        return Math.min(100, protection);
    }
    
    private boolean isHazmatArmor(ItemStack item) {
        return item != null && item.hasItemMeta() && 
               item.getItemMeta().hasDisplayName() &&
               item.getItemMeta().getDisplayName().contains("химзащиты");
    }
    
    private boolean isPowerArmor(ItemStack item) {
        return item != null && item.hasItemMeta() && 
               item.getItemMeta().hasDisplayName() &&
               item.getItemMeta().getDisplayName().contains("силовой брони");
    }
    
    private String createRadiationBar(int radiation) {
        int bars = radiation / 5; // 20 символов максимум
        StringBuilder bar = new StringBuilder();
        
        for (int i = 0; i < 20; i++) {
            if (i < bars) {
                if (radiation >= 80) bar.append(ChatColor.DARK_RED + "█");
                else if (radiation >= 60) bar.append(ChatColor.RED + "█");
                else if (radiation >= 40) bar.append(ChatColor.GOLD + "█");
                else if (radiation >= 20) bar.append(ChatColor.YELLOW + "█");
                else bar.append(ChatColor.GREEN + "█");
            } else {
                bar.append(ChatColor.GRAY + "░");
            }
        }
        
        return bar.toString();
    }

    public void updateRadiation() {
        updateAllPlayers();
    }

    public void saveData() {
        saveAllData();
    }
    
    public void saveAllData() {
        for (Map.Entry<UUID, Integer> entry : playerRadiation.entrySet()) {
            dataConfig.set("radiation." + entry.getKey().toString(), entry.getValue());
        }
        
        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Не удалось сохранить данные радиации");
        }
    }
    
    private void loadData() {
        if (dataConfig.getConfigurationSection("radiation") != null) {
            for (String key : dataConfig.getConfigurationSection("radiation").getKeys(false)) {
                try {
                    UUID playerId = UUID.fromString(key);
                    int radiation = dataConfig.getInt("radiation." + key);
                    playerRadiation.put(playerId, radiation);
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Неверный UUID в файле радиации: " + key);
                }
            }
        }
    }
}
