package com.uraniumcraft.gui;

import com.uraniumcraft.stats.PlayerStats;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.Map;

public class StatsGUI {
    
    public static void openStatsMenu(Player player) {
        Inventory gui = Bukkit.createInventory(null, 54, ChatColor.GOLD + "Статистика игрока");
        
        PlayerStats.PlayerStatsData stats = PlayerStats.getStats(player.getUniqueId());
        
        // Общая информация
        ItemStack playerInfo = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta playerMeta = playerInfo.getItemMeta();
        playerMeta.setDisplayName(ChatColor.AQUA + "Статистика " + player.getName());
        playerMeta.setLore(Arrays.asList(
            ChatColor.GRAY + "Ваши достижения в UraniumCraft",
            ChatColor.YELLOW + "Отслеживание прогресса",
            "",
            ChatColor.GREEN + "Продолжайте исследования!"
        ));
        playerInfo.setItemMeta(playerMeta);
        gui.setItem(4, playerInfo);
        
        // Добыча урана
        addStatItem(gui, 19, Material.EMERALD_ORE, "Добыто урана", 
            stats.getStat(PlayerStats.StatType.URANIUM_MINED), "блоков");
        
        // Радиация
        addStatItem(gui, 20, Material.REDSTONE, "Получено радиации", 
            stats.getStat(PlayerStats.StatType.RADIATION_RECEIVED), "единиц");
        
        // Максимальная радиация
        addStatItem(gui, 21, Material.REDSTONE_BLOCK, "Максимальная радиация", 
            stats.getStat(PlayerStats.StatType.MAX_RADIATION_LEVEL), "%");
        
        // Таблетки
        addStatItem(gui, 22, Material.SUGAR, "Принято таблеток", 
            stats.getStat(PlayerStats.StatType.PILLS_TAKEN), "штук");
        
        // Центрифуга
        addStatItem(gui, 23, Material.DISPENSER, "Использований центрифуги", 
            stats.getStat(PlayerStats.StatType.CENTRIFUGE_USES), "раз");
        
        // Исследования
        addStatItem(gui, 24, Material.ENCHANTING_TABLE, "Завершено исследований", 
            stats.getStat(PlayerStats.StatType.RESEARCH_COMPLETED), "штук");
        
        // Лаборатории
        addStatItem(gui, 25, Material.BEACON, "Построено лабораторий", 
            stats.getStat(PlayerStats.StatType.LABORATORIES_BUILT), "штук");
        
        // Продвинутые предметы
        addStatItem(gui, 28, Material.NETHERITE_CHESTPLATE, "Создано продвинутых предметов", 
            stats.getStat(PlayerStats.StatType.ADVANCED_ITEMS_CRAFTED), "штук");
        
        // Время в радиации
        int timeInRadiation = stats.getStat(PlayerStats.StatType.TIME_IN_RADIATION);
        addStatItem(gui, 29, Material.CLOCK, "Время в радиации", 
            timeInRadiation / 60, "минут");
        
        // Достижения
        addAchievements(gui, stats);
        
        fillEmptySlots(gui);
        player.openInventory(gui);
    }
    
    private static void addStatItem(Inventory gui, int slot, Material material, String name, int value, String unit) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.YELLOW + name);
        meta.setLore(Arrays.asList(
            ChatColor.GRAY + "Значение: " + ChatColor.WHITE + value + " " + unit,
            ChatColor.DARK_GRAY + "Статистика UraniumCraft"
        ));
        item.setItemMeta(meta);
        gui.setItem(slot, item);
    }
    
    private static void addAchievements(Inventory gui, PlayerStats.PlayerStatsData stats) {
        // Достижение: Первый контакт
        boolean firstUranium = stats.getStat(PlayerStats.StatType.URANIUM_MINED) > 0;
        addAchievement(gui, 37, Material.EMERALD, "Первый контакт", 
            "Добыть первый урановый блок", firstUranium);
        
        // Достижение: Радиоактивный
        boolean highRadiation = stats.getStat(PlayerStats.StatType.MAX_RADIATION_LEVEL) >= 50;
        addAchievement(gui, 38, Material.REDSTONE_BLOCK, "Радиоактивный", 
            "Достичь 50% радиации", highRadiation);
        
        // Достижение: Учёный
        boolean researcher = stats.getStat(PlayerStats.StatType.RESEARCH_COMPLETED) >= 3;
        addAchievement(gui, 39, Material.ENCHANTING_TABLE, "Учёный", 
            "Завершить 3 исследования", researcher);
        
        // Достижение: Инженер
        boolean engineer = stats.getStat(PlayerStats.StatType.LABORATORIES_BUILT) >= 1;
        addAchievement(gui, 40, Material.BEACON, "Инженер", 
            "Построить лабораторию", engineer);
        
        // Достижение: Мастер центрифуги
        boolean centrifugeMaster = stats.getStat(PlayerStats.StatType.CENTRIFUGE_USES) >= 10;
        addAchievement(gui, 41, Material.DISPENSER, "Мастер центрифуги", 
            "Использовать центрифугу 10 раз", centrifugeMaster);
        
        // Достижение: Выживший
        boolean survivor = stats.getStat(PlayerStats.StatType.PILLS_TAKEN) >= 5;
        addAchievement(gui, 42, Material.SUGAR, "Выживший", 
            "Принять 5 таблеток от радиации", survivor);
        
        // Достижение: Технолог
        boolean technologist = stats.getStat(PlayerStats.StatType.ADVANCED_ITEMS_CRAFTED) >= 5;
        addAchievement(gui, 43, Material.NETHERITE_CHESTPLATE, "Технолог", 
            "Создать 5 продвинутых предметов", technologist);
    }
    
    private static void addAchievement(Inventory gui, int slot, Material material, String name, String description, boolean achieved) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName((achieved ? ChatColor.GREEN : ChatColor.GRAY) + name);
        meta.setLore(Arrays.asList(
            ChatColor.GRAY + description,
            "",
            achieved ? ChatColor.GREEN + "✓ Достигнуто!" : ChatColor.RED + "✗ Не достигнуто"
        ));
        item.setItemMeta(meta);
        gui.setItem(slot, item);
    }
    
    private static void fillEmptySlots(Inventory gui) {
        ItemStack filler = new ItemStack(Material.YELLOW_STAINED_GLASS_PANE);
        ItemMeta fillerMeta = filler.getItemMeta();
        fillerMeta.setDisplayName(" ");
        filler.setItemMeta(fillerMeta);
        
        for (int i = 0; i < gui.getSize(); i++) {
            if (gui.getItem(i) == null) {
                gui.setItem(i, filler);
            }
        }
    }
}
