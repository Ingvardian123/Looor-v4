package com.uraniumcraft.listeners;

import com.uraniumcraft.UraniumPlugin;
import com.uraniumcraft.achievements.AchievementManager;
import com.uraniumcraft.items.UraniumItems;
import com.uraniumcraft.stats.PlayerStats;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

public class NaturalGameplayListener implements Listener {
    private final UraniumPlugin plugin;
    private final AchievementManager achievementManager;
    private final Random random = new Random();
    
    public NaturalGameplayListener(UraniumPlugin plugin, AchievementManager achievementManager) {
        this.plugin = plugin;
        this.achievementManager = achievementManager;
    }
    
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Material blockType = event.getBlock().getType();

        // Обновляем статистику
        PlayerStats.incrementBlocksMined(player.getUniqueId());
        
        // Проверяем на урановую пыль при добыче различных руд
        if (isOreBlock(blockType)) {
            handleOreBreaking(player, blockType, event);
        }
        
        // Специальная обработка для камня (базовый материал)
        if (blockType == Material.STONE || blockType == Material.DEEPSLATE) {
            handleStoneBreaking(player, event);
        }
        
        // Обработка блоков Нижнего мира
        if (isNetherBlock(blockType)) {
            handleNetherBlockBreaking(player, blockType, event);
        }
    }
    
    private void handleOreBreaking(Player player, Material blockType, BlockBreakEvent event) {
        double dustChance = getDustChance(blockType);
        
        if (random.nextDouble() < dustChance) {
            ItemStack uraniumDust = UraniumItems.getItem("uranium_dust");
            if (uraniumDust != null) {
                // Добавляем урановую пыль в инвентарь
                player.getInventory().addItem(uraniumDust);
                
                // Эффекты находки
                player.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, 
                    event.getBlock().getLocation().add(0.5, 1, 0.5), 10, 0.5, 0.5, 0.5, 0.1);
                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.8f, 1.5f);
                
                // Сообщение игроку
                player.sendMessage(ChatColor.GREEN + "🔍 Найдена урановая пыль!");
                
                // Обновляем статистику
                PlayerStats.incrementComponentsFound(player.getUniqueId());
                
                // Проверяем достижения
                achievementManager.checkAchievement(player, "first_uranium");
                
                if (PlayerStats.getComponentsFound(player.getUniqueId()) >= 10) {
                    achievementManager.checkAchievement(player, "uranium_collector");
                }
            }
        }
    }
    
    private void handleStoneBreaking(Player player, BlockBreakEvent event) {
        // Очень низкий шанс найти урановую пыль в обычном камне
        if (random.nextDouble() < 0.01) { // 1% шанс
            ItemStack uraniumDust = UraniumItems.getItem("uranium_dust");
            if (uraniumDust != null) {
                player.getInventory().addItem(uraniumDust);
                
                player.getWorld().spawnParticle(Particle.CRIT, 
                    event.getBlock().getLocation().add(0.5, 1, 0.5), 5, 0.3, 0.3, 0.3, 0.1);
                player.playSound(player.getLocation(), Sound.BLOCK_STONE_BREAK, 1.0f, 1.5f);
                
                player.sendMessage(ChatColor.GRAY + "🔍 В камне найдена урановая пыль!");
                
                PlayerStats.incrementComponentsFound(player.getUniqueId());
            }
        }
    }
    
    private void handleNetherBlockBreaking(Player player, Material blockType, BlockBreakEvent event) {
        double dustChance = getNetherDustChance(blockType);
        
        if (random.nextDouble() < dustChance) {
            ItemStack uraniumDust = UraniumItems.getItem("uranium_dust");
            if (uraniumDust != null) {
                player.getInventory().addItem(uraniumDust);
                
                // Специальные эффекты для Нижнего мира
                player.getWorld().spawnParticle(Particle.FLAME, 
                    event.getBlock().getLocation().add(0.5, 1, 0.5), 15, 0.5, 0.5, 0.5, 0.1);
                player.getWorld().spawnParticle(Particle.LAVA, 
                    event.getBlock().getLocation().add(0.5, 1, 0.5), 5, 0.3, 0.3, 0.3, 0.1);
                player.playSound(player.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 0.8f, 1.2f);
                
                player.sendMessage(ChatColor.RED + "🔥 Найдена адская урановая пыль!");
                
                PlayerStats.incrementComponentsFound(player.getUniqueId());
                achievementManager.checkAchievement(player, "nether_explorer");
            }
        }
    }
    
    private boolean isOreBlock(Material material) {
        return material == Material.IRON_ORE || material == Material.DEEPSLATE_IRON_ORE ||
               material == Material.GOLD_ORE || material == Material.DEEPSLATE_GOLD_ORE ||
               material == Material.DIAMOND_ORE || material == Material.DEEPSLATE_DIAMOND_ORE ||
               material == Material.EMERALD_ORE || material == Material.DEEPSLATE_EMERALD_ORE ||
               material == Material.LAPIS_ORE || material == Material.DEEPSLATE_LAPIS_ORE ||
               material == Material.REDSTONE_ORE || material == Material.DEEPSLATE_REDSTONE_ORE ||
               material == Material.COAL_ORE || material == Material.DEEPSLATE_COAL_ORE ||
               material == Material.COPPER_ORE || material == Material.DEEPSLATE_COPPER_ORE;
    }
    
    private boolean isNetherBlock(Material material) {
        return material == Material.NETHERRACK || material == Material.NETHER_QUARTZ_ORE ||
               material == Material.NETHER_GOLD_ORE || material == Material.ANCIENT_DEBRIS ||
               material == Material.BLACKSTONE || material == Material.BASALT ||
               material == Material.SOUL_SAND || material == Material.SOUL_SOIL;
    }
    
    private double getDustChance(Material blockType) {
        switch (blockType) {
            case DIAMOND_ORE:
            case DEEPSLATE_DIAMOND_ORE:
                return 0.15; // 15% шанс
            case EMERALD_ORE:
            case DEEPSLATE_EMERALD_ORE:
                return 0.12; // 12% шанс
            case GOLD_ORE:
            case DEEPSLATE_GOLD_ORE:
                return 0.08; // 8% шанс
            case IRON_ORE:
            case DEEPSLATE_IRON_ORE:
                return 0.06; // 6% шанс
            case REDSTONE_ORE:
            case DEEPSLATE_REDSTONE_ORE:
                return 0.05; // 5% шанс
            case LAPIS_ORE:
            case DEEPSLATE_LAPIS_ORE:
                return 0.04; // 4% шанс
            case COPPER_ORE:
            case DEEPSLATE_COPPER_ORE:
                return 0.03; // 3% шанс
            default:
                return 0.02; // 2% шанс для остальных
        }
    }
    
    private double getNetherDustChance(Material blockType) {
        switch (blockType) {
            case ANCIENT_DEBRIS:
                return 0.25; // 25% шанс
            case NETHER_QUARTZ_ORE:
                return 0.10; // 10% шанс
            case NETHER_GOLD_ORE:
                return 0.08; // 8% шанс
            case BLACKSTONE:
                return 0.05; // 5% шанс
            case BASALT:
                return 0.04; // 4% шанс
            case SOUL_SAND:
            case SOUL_SOIL:
                return 0.03; // 3% шанс
            default:
                return 0.02; // 2% шанс для незеррака
        }
    }
}
