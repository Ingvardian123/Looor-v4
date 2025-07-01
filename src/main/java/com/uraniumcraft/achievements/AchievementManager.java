package com.uraniumcraft.achievements;

import com.uraniumcraft.UraniumPlugin;
import com.uraniumcraft.stats.PlayerStats;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class AchievementManager {
    private final UraniumPlugin plugin;
    private final Map<UUID, Set<String>> playerAchievements;
    private final Map<String, Achievement> achievements;
    
    public AchievementManager(UraniumPlugin plugin) {
        this.plugin = plugin;
        this.playerAchievements = new HashMap<>();
        this.achievements = new HashMap<>();
        
        initializeAchievements();
    }
    
    private void initializeAchievements() {
        // Базовые достижения
        achievements.put("first_uranium", new Achievement(
            "first_uranium",
            "Первый уран",
            "Добудьте свою первую урановую руду",
            "uranium_mined >= 1"
        ));
        
        achievements.put("uranium_miner", new Achievement(
            "uranium_miner",
            "Урановый шахтёр",
            "Добудьте 100 урановой руды",
            "uranium_mined >= 100"
        ));
        
        achievements.put("radiation_survivor", new Achievement(
            "radiation_survivor",
            "Выживший в радиации",
            "Проведите 1 час в радиации",
            "time_in_radiation >= 3600000"
        ));
        
        achievements.put("pill_consumer", new Achievement(
            "pill_consumer",
            "Любитель таблеток",
            "Примите 50 таблеток от радиации",
            "pills_taken >= 50"
        ));
        
        achievements.put("first_laboratory", new Achievement(
            "first_laboratory",
            "Первая лаборатория",
            "Постройте свою первую лабораторию",
            "laboratories_built >= 1"
        ));
        
        achievements.put("researcher", new Achievement(
            "researcher",
            "Исследователь",
            "Завершите 10 исследований",
            "research_completed >= 10"
        ));
        
        achievements.put("centrifuge_master", new Achievement(
            "centrifuge_master",
            "Мастер центрифуги",
            "Используйте центрифугу 25 раз",
            "centrifuge_uses >= 25"
        ));
        
        achievements.put("uranium_lord", new Achievement(
            "uranium_lord",
            "Урановый лорд",
            "Добудьте 1000 урановой руды",
            "uranium_mined >= 1000"
        ));
    }
    
    public void checkAchievement(Player player, String achievementId) {
        if (!achievements.containsKey(achievementId)) {
            return;
        }
        
        UUID playerId = player.getUniqueId();
        Set<String> playerAchs = playerAchievements.computeIfAbsent(playerId, k -> new HashSet<>());
        
        if (playerAchs.contains(achievementId)) {
            return; // Уже получено
        }
        
        Achievement achievement = achievements.get(achievementId);
        PlayerStats stats = plugin.getPlayerStats(playerId);
        
        if (checkAchievementCondition(achievement, stats)) {
            grantAchievement(player, achievement);
        }
    }
    
    public void checkAllAchievements(Player player) {
        for (String achievementId : achievements.keySet()) {
            checkAchievement(player, achievementId);
        }
    }
    
    private boolean checkAchievementCondition(Achievement achievement, PlayerStats stats) {
        String condition = achievement.getCondition();
        
        // Простая система проверки условий
        if (condition.contains("uranium_mined")) {
            int required = extractNumber(condition);
            return stats.getUraniumMined() >= required;
        } else if (condition.contains("pills_taken")) {
            int required = extractNumber(condition);
            return stats.getPillsTaken() >= required;
        } else if (condition.contains("time_in_radiation")) {
            long required = extractNumber(condition);
            return stats.getTimeInRadiation() >= required;
        } else if (condition.contains("laboratories_built")) {
            int required = extractNumber(condition);
            return stats.getLaboratoriesBuilt() >= required;
        } else if (condition.contains("research_completed")) {
            int required = extractNumber(condition);
            return stats.getResearchCompleted() >= required;
        } else if (condition.contains("centrifuge_uses")) {
            int required = extractNumber(condition);
            return stats.getCentrifugeUses() >= required;
        }
        
        return false;
    }
    
    private int extractNumber(String condition) {
        String[] parts = condition.split(">=");
        if (parts.length == 2) {
            try {
                return Integer.parseInt(parts[1].trim());
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return 0;
    }
    
    private void grantAchievement(Player player, Achievement achievement) {
        UUID playerId = player.getUniqueId();
        Set<String> playerAchs = playerAchievements.computeIfAbsent(playerId, k -> new HashSet<>());
        
        playerAchs.add(achievement.getId());
        plugin.getPlayerStats(playerId).incrementAchievements();
        
        // Уведомление игрока
        player.sendMessage(ChatColor.GOLD + "═══════════════════════════════");
        player.sendMessage(ChatColor.YELLOW + "🏆 ДОСТИЖЕНИЕ ПОЛУЧЕНО!");
        player.sendMessage(ChatColor.AQUA + achievement.getName());
        player.sendMessage(ChatColor.GRAY + achievement.getDescription());
        player.sendMessage(ChatColor.GOLD + "═══════════════════════════════");
        
        // Звуковые и визуальные эффекты
        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.8f, 1.2f);
        
        // Эффекты частиц
        player.getWorld().spawnParticle(org.bukkit.Particle.TOTEM, 
            player.getLocation().add(0, 1, 0), 20, 0.5, 0.5, 0.5, 0.1);
        player.getWorld().spawnParticle(org.bukkit.Particle.FIREWORKS_SPARK, 
            player.getLocation().add(0, 1, 0), 15, 0.3, 0.3, 0.3, 0.1);
    }
    
    public Set<String> getPlayerAchievements(UUID playerId) {
        return playerAchievements.getOrDefault(playerId, new HashSet<>());
    }
    
    public Map<String, Achievement> getAllAchievements() {
        return new HashMap<>(achievements);
    }
    
    public boolean hasAchievement(UUID playerId, String achievementId) {
        return playerAchievements.getOrDefault(playerId, new HashSet<>()).contains(achievementId);
    }
    
    // Внутренний класс для достижений
    public static class Achievement {
        private final String id;
        private final String name;
        private final String description;
        private final String condition;
        
        public Achievement(String id, String name, String description, String condition) {
            this.id = id;
            this.name = name;
            this.description = description;
            this.condition = condition;
        }
        
        public String getId() { return id; }
        public String getName() { return name; }
        public String getDescription() { return description; }
        public String getCondition() { return condition; }
    }
}
