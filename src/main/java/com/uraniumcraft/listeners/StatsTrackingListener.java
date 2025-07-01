package com.uraniumcraft.listeners;

import com.uraniumcraft.UraniumPlugin;
import com.uraniumcraft.stats.PlayerStats;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class StatsTrackingListener implements Listener {
    private final UraniumPlugin plugin;
    
    public StatsTrackingListener(UraniumPlugin plugin) {
        this.plugin = plugin;
        startRadiationTimeTracking();
    }
    
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        
        // Отслеживаем добычу урановой руды
        if (event.getBlock().getType() == Material.EMERALD_ORE) {
            // Проверяем, что это действительно урановая руда по названию
            PlayerStats.incrementStat(player.getUniqueId(), PlayerStats.StatType.URANIUM_MINED);
        }
    }
    
    @EventHandler
    public void onPlayerItemConsume(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        
        if (event.getItem().hasItemMeta() && event.getItem().getItemMeta().hasDisplayName()) {
            String displayName = event.getItem().getItemMeta().getDisplayName();
            
            if (displayName.contains("Таблетки от радиации")) {
                PlayerStats.incrementStat(player.getUniqueId(), PlayerStats.StatType.PILLS_TAKEN);
            }
        }
    }
    
    private void startRadiationTimeTracking() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : plugin.getServer().getOnlinePlayers()) {
                    int radiation = plugin.getRadiationManager().getPlayerRadiation(player.getUniqueId());
                    
                    if (radiation > 0) {
                        PlayerStats.incrementStat(player.getUniqueId(), PlayerStats.StatType.TIME_IN_RADIATION);
                        PlayerStats.incrementStat(player.getUniqueId(), PlayerStats.StatType.RADIATION_RECEIVED);
                        
                        // Обновляем максимальный уровень радиации
                        int currentMax = PlayerStats.getStats(player.getUniqueId()).getStat(PlayerStats.StatType.MAX_RADIATION_LEVEL);
                        if (radiation > currentMax) {
                            PlayerStats.setStat(player.getUniqueId(), PlayerStats.StatType.MAX_RADIATION_LEVEL, radiation);
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 1200L); // Каждую минуту
    }
}
