package com.uraniumcraft.listeners;

import com.uraniumcraft.UraniumPlugin;
import com.uraniumcraft.achievements.AchievementManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;

public class AchievementListener implements Listener {
    
    private final UraniumPlugin plugin;
    private final AchievementManager achievementManager;
    
    public AchievementListener(UraniumPlugin plugin, AchievementManager achievementManager) {
        this.plugin = plugin;
        this.achievementManager = achievementManager;
    }
    
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Material blockType = event.getBlock().getType();
        
        // Проверка добычи урановой руды
        if (blockType == Material.EMERALD_ORE) { // Урановая руда = изумрудная руда
            achievementManager.checkAchievements(player, "mine_uranium");
        }
    }
    
    @EventHandler
    public void onCraftItem(CraftItemEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        
        Player player = (Player) event.getWhoClicked();
        ItemStack result = event.getRecipe().getResult();
        
        if (result.hasItemMeta() && result.getItemMeta().hasDisplayName()) {
            String displayName = result.getItemMeta().getDisplayName();
            
            // Проверка крафта различных предметов
            if (displayName.contains("Защитный костюм")) {
                achievementManager.checkAchievements(player, "wear_hazmat");
            } else if (displayName.contains("Центрифуга")) {
                achievementManager.checkAchievements(player, "create_centrifuge");
            } else if (displayName.contains("Лаборатория")) {
                achievementManager.checkAchievements(player, "build_laboratory");
            } else if (displayName.contains("Квантовый")) {
                achievementManager.checkAchievements(player, "craft_quantum");
            }
        }
    }
    
    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        
        Player player = (Player) event.getEntity();
        
        // Проверка радиационного урона
        if (event.getCause() == EntityDamageEvent.DamageCause.POISON) {
            achievementManager.checkAchievements(player, "radiation_damage");
        }
    }
    
    @EventHandler
    public void onPlayerItemConsume(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        
        if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
            String displayName = item.getItemMeta().getDisplayName();
            
            // Проверка использования таблеток от радиации
            if (displayName.contains("Таблетки от радиации")) {
                achievementManager.checkAchievements(player, "clean_radiation", 20);
            }
        }
    }
}
