package com.uraniumcraft.items;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class UniversalItems {
    
    public static ItemStack createUraniumOre() {
        ItemStack item = new ItemStack(Material.EMERALD_ORE);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.GREEN + "Урановая руда");
        meta.setLore(Arrays.asList(
            ChatColor.GRAY + "Радиоактивная руда",
            ChatColor.RED + "☢ Радиация: +5"
        ));
        item.setItemMeta(meta);
        return item;
    }
    
    public static ItemStack createRadioactiveComponent() {
        ItemStack item = new ItemStack(Material.IRON_INGOT);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.YELLOW + "Радиоактивный компонент");
        meta.setLore(Arrays.asList(
            ChatColor.GRAY + "Компонент для создания устройств",
            ChatColor.RED + "☢ Радиация: +2"
        ));
        item.setItemMeta(meta);
        return item;
    }
}
