package com.uraniumcraft.items;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class AdvancedResearchItems {
    
    private static final Map<String, ItemStack> items = new HashMap<>();
    
    public static void initializeItems() {
        items.put("quantum_processor", createQuantumProcessor());
        items.put("teleporter", createTeleporter());
    }
    
    public static ItemStack get(String name) {
        ItemStack item = items.get(name.toLowerCase());
        return item != null ? item.clone() : null;
    }
    
    public static ItemStack createQuantumProcessor() {
        ItemStack item = new ItemStack(Material.NETHER_STAR);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.LIGHT_PURPLE + "Квантовый процессор");
        meta.setLore(Arrays.asList(
            ChatColor.GRAY + "Продвинутый вычислительный блок",
            ChatColor.GRAY + "для квантовых технологий"
        ));
        meta.addEnchant(Enchantment.DURABILITY, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        item.setItemMeta(meta);
        return item;
    }
    
    public static ItemStack createTeleporter() {
        ItemStack item = new ItemStack(Material.END_PORTAL_FRAME);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.DARK_PURPLE + "Квантовый телепорт");
        meta.setLore(Arrays.asList(
            ChatColor.GRAY + "Устройство для мгновенного",
            ChatColor.GRAY + "перемещения в пространстве"
        ));
        meta.addEnchant(Enchantment.DURABILITY, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        item.setItemMeta(meta);
        return item;
    }
}
