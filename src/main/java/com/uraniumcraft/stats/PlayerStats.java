package com.uraniumcraft.stats;

import com.uraniumcraft.UraniumPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerStats {
    
    private final UraniumPlugin plugin;
    private final Map<UUID, PlayerStatsData> playerStats;
    private File dataFile;
    private FileConfiguration dataConfig;
    
    public PlayerStats(UraniumPlugin plugin) {
        this.plugin = plugin;
        this.playerStats = new HashMap<>();
        setupDataFile();
        loadData();
    }
    
    public PlayerStats() {
        this.plugin = null;
        this.playerStats = new HashMap<>();
    }
    
    private void setupDataFile() {
        if (plugin == null) return;
        dataFile = new File(plugin.getDataFolder(), "stats.yml");
        if (!dataFile.exists()) {
            try {
                dataFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Не удалось создать файл stats.yml");
            }
        }
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
    }
    
    public static PlayerStatsData getStats(UUID playerId) {
        UraniumPlugin plugin = UraniumPlugin.getInstance();
        if (plugin != null && plugin.getPlayerStats() != null) {
            return plugin.getPlayerStats().getPlayerStats(playerId);
        }
        return new PlayerStatsData();
    }
    
    public PlayerStatsData getPlayerStats(UUID playerId) {
        return playerStats.computeIfAbsent(playerId, k -> new PlayerStatsData());
    }
    
    public void addUraniumMined(UUID playerId, int amount) {
        PlayerStatsData stats = getPlayerStats(playerId);
        stats.addStat(StatType.URANIUM_MINED, amount);
    }
    
    public void addPillsTaken(UUID playerId, int amount) {
        PlayerStatsData stats = getPlayerStats(playerId);
        stats.addStat(StatType.PILLS_TAKEN, amount);
    }
    
    public void addRadiationReceived(UUID playerId, int amount) {
        PlayerStatsData stats = getPlayerStats(playerId);
        stats.addStat(StatType.RADIATION_RECEIVED, amount);
    }
    
    public void updateAllStats() {
        // Обновление статистики всех игроков
        saveData();
    }
    
    public void saveData() {
        if (plugin == null || dataConfig == null) return;
        
        for (Map.Entry<UUID, PlayerStatsData> entry : playerStats.entrySet()) {
            String path = "stats." + entry.getKey().toString();
            PlayerStatsData stats = entry.getValue();
            
            for (StatType type : StatType.values()) {
                dataConfig.set(path + "." + type.name().toLowerCase(), stats.getStat(type));
            }
        }
        
        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Ошибка сохранения статистики: " + e.getMessage());
        }
    }
    
    private void loadData() {
        if (plugin == null || dataConfig == null) return;
        
        if (dataConfig.getConfigurationSection("stats") != null) {
            for (String key : dataConfig.getConfigurationSection("stats").getKeys(false)) {
                try {
                    UUID playerId = UUID.fromString(key);
                    PlayerStatsData stats = new PlayerStatsData();
                    
                    for (StatType type : StatType.values()) {
                        int value = dataConfig.getInt("stats." + key + "." + type.name().toLowerCase(), 0);
                        stats.setStat(type, value);
                    }
                    
                    playerStats.put(playerId, stats);
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Неверный UUID в статистике: " + key);
                }
            }
        }
    }
    
    public enum StatType {
        URANIUM_MINED,
        RADIATION_RECEIVED,
        MAX_RADIATION_LEVEL,
        PILLS_TAKEN,
        CENTRIFUGE_USES,
        RESEARCH_COMPLETED,
        LABORATORIES_BUILT,
        ADVANCED_ITEMS_CRAFTED,
        TIME_IN_RADIATION
    }
    
    public static class PlayerStatsData {
        private final Map<StatType, Integer> stats = new HashMap<>();
        
        public int getStat(StatType type) {
            return stats.getOrDefault(type, 0);
        }
        
        public void setStat(StatType type, int value) {
            stats.put(type, value);
        }
        
        public void addStat(StatType type, int amount) {
            stats.put(type, getStat(type) + amount);
        }
    }
}
