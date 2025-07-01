package com.uraniumcraft.achievements;

import org.bukkit.ChatColor;
import org.bukkit.Material;

public enum AchievementCategory {
    MINING("Добыча", ChatColor.GRAY, Material.IRON_PICKAXE),
    CRAFTING("Крафт", ChatColor.GOLD, Material.CRAFTING_TABLE),
    SURVIVAL("Выживание", ChatColor.RED, Material.GOLDEN_APPLE),
    TECHNOLOGY("Технологии", ChatColor.BLUE, Material.REDSTONE),
    RESEARCH("Исследования", ChatColor.LIGHT_PURPLE, Material.ENCHANTING_TABLE),
    COMBAT("Сражения", ChatColor.DARK_RED, Material.DIAMOND_SWORD),
    SPECIAL("Особые", ChatColor.AQUA, Material.NETHER_STAR);
    
    private final String displayName;
    private final ChatColor color;
    private final Material icon;
    
    AchievementCategory(String displayName, ChatColor color, Material icon) {
        this.displayName = displayName;
        this.color = color;
        this.icon = icon;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public ChatColor getColor() {
        return color;
    }
    
    public Material getIcon() {
        return icon;
    }
}
