package com.uraniumcraft.listeners;

import com.uraniumcraft.UraniumPlugin;
import com.uraniumcraft.gui.*;
import com.uraniumcraft.items.UraniumItems;
import com.uraniumcraft.laboratory.*;
import com.uraniumcraft.radiation.RadiationManager;
import com.uraniumcraft.achievements.AchievementManager;
import com.uraniumcraft.stats.PlayerStats;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Particle;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class UraniumListeners implements Listener {
    private final UraniumPlugin plugin;
    private final LaboratoryManager laboratoryManager;
    private final RadiationManager radiationManager;
    private final AchievementManager achievementManager;
    private final Random random = new Random();
    
    // Делегируем события специализированным слушателям
    private final TeleporterListener teleporterListener;
    private final LaboratoryListener laboratoryListener;
    private final RadiationListener radiationListener;
    private final AchievementListener achievementListener;
    
    public UraniumListeners(UraniumPlugin plugin, LaboratoryManager laboratoryManager, 
                           RadiationManager radiationManager, AchievementManager achievementManager) {
        this.plugin = plugin;
        this.laboratoryManager = laboratoryManager;
        this.radiationManager = radiationManager;
        this.achievementManager = achievementManager;
        
        // Создаём и регистрируем специализированных слушателей
        this.teleporterListener = new TeleporterListener(plugin);
        this.laboratoryListener = new LaboratoryListener(plugin, laboratoryManager);
        this.radiationListener = new RadiationListener(plugin, radiationManager);
        this.achievementListener = new AchievementListener(plugin, achievementManager);
        
        // Регистрируем всех слушателей
        plugin.getServer().getPluginManager().registerEvents(teleporterListener, plugin);
        plugin.getServer().getPluginManager().registerEvents(laboratoryListener, plugin);
        plugin.getServer().getPluginManager().registerEvents(radiationListener, plugin);
        plugin.getServer().getPluginManager().registerEvents(achievementListener, plugin);
    }
    
    // Геттеры для доступа к специализированным слушателям
    public TeleporterListener getTeleporterListener() {
        return teleporterListener;
    }
    
    public LaboratoryListener getLaboratoryListener() {
        return laboratoryListener;
    }
    
    public RadiationListener getRadiationListener() {
        return radiationListener;
    }
    
    public AchievementListener getAchievementListener() {
        return achievementListener;
    }
    
    // ==================== LABORATORY EVENTS ====================
    
    // Методы для обработки событий лаборатории были перемещены в LaboratoryListener
    
    // ==================== GUI EVENTS ====================
    
    // Методы для обработки событий GUI были перемещены в соответствующие слушатели GUI
    
    // ==================== NATURAL GAMEPLAY EVENTS ====================
    
    // Методы для обработки событий игры были перемещены в соответствующие слушатели
    
    // ==================== UTILITY METHODS ====================
    
    // Вспомогательные методы остались без изменений
}
