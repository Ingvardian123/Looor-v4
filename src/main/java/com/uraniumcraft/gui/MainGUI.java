package com.uraniumcraft.gui;

import com.uraniumcraft.UraniumPlugin;
import com.uraniumcraft.items.UraniumItems;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class MainGUI {
   
   public static void openMainMenu(Player player, UraniumPlugin plugin) {
       Inventory gui = Bukkit.createInventory(null, 54, ChatColor.DARK_GREEN + "UraniumCraft - Главное меню");
       
       // Заполняем границы
       ItemStack border = createBorderItem();
       for (int i = 0; i < 9; i++) {
           gui.setItem(i, border);
           gui.setItem(i + 45, border);
       }
       for (int i = 0; i < 54; i += 9) {
           gui.setItem(i, border);
           gui.setItem(i + 8, border);
       }
       
       // Основные разделы
       gui.setItem(10, createMenuItem(Material.EMERALD, ChatColor.GREEN + "Урановые материалы", 
           "Просмотр всех урановых предметов", "и их характеристик"));
       
       gui.setItem(12, createMenuItem(Material.BEACON, ChatColor.LIGHT_PURPLE + "Лаборатории", 
           "Создание и управление", "научными лабораториями"));
       
       gui.setItem(14, createMenuItem(Material.DISPENSER, ChatColor.AQUA + "Центрифуга", 
           "Обогащение урановых", "материалов"));
       
       gui.setItem(16, createMenuItem(Material.COMPASS, ChatColor.YELLOW + "Радиация", 
           "Мониторинг и защита", "от радиации"));
       
       gui.setItem(19, createMenuItem(Material.ENDER_PEARL, ChatColor.PURPLE + "Телепортация", 
           "Система телепортов", "и терминалов"));
       
       gui.setItem(21, createMenuItem(Material.MAP, ChatColor.GOLD + "Планшеты", 
           "Портативные устройства", "и модули"));
       
       gui.setItem(23, createMenuItem(Material.DIAMOND_SWORD, ChatColor.BLUE + "Продвинутые предметы", 
           "Высокотехнологичное", "оборудование"));
       
       gui.setItem(25, createMenuItem(Material.EXPERIENCE_BOTTLE, ChatColor.AQUA + "Достижения", 
           "Прогресс и награды", "за исследования"));
       
       gui.setItem(28, createMenuItem(Material.WRITABLE_BOOK, ChatColor.WHITE + "Руководство", 
           "Подробная инструкция", "по использованию"));
       
       gui.setItem(30, createMenuItem(Material.PLAYER_HEAD, ChatColor.YELLOW + "Статистика", 
           "Ваши показатели", "и достижения"));
       
       gui.setItem(32, createMenuItem(Material.CRAFTING_TABLE, ChatColor.BROWN + "Рецепты", 
           "Все рецепты крафта", "урановых предметов"));
       
       // Информационная панель
       gui.setItem(49, createInfoItem(player, plugin));
       
       player.openInventory(gui);
       player.playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 1.0f, 1.0f);
   }
   
   private static ItemStack createBorderItem() {
       ItemStack item = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
       ItemMeta meta = item.getItemMeta();
       meta.setDisplayName(" ");
       item.setItemMeta(meta);
       return item;
   }
   
   private static ItemStack createMenuItem(Material material, String name, String... lore) {
       ItemStack item = new ItemStack(material);
       ItemMeta meta = item.getItemMeta();
       meta.setDisplayName(name);
       meta.setLore(Arrays.asList(lore));
       item.setItemMeta(meta);
       return item;
   }
   
   private static ItemStack createInfoItem(Player player, UraniumPlugin plugin) {
       ItemStack item = new ItemStack(Material.NETHER_STAR);
       ItemMeta meta = item.getItemMeta();
       meta.setDisplayName(ChatColor.GOLD + "Информация о игроке");
       
       int radiation = plugin.getRadiationManager().getRadiationLevel(player.getUniqueId());
       boolean authorized = plugin.getEnhancedLaboratoryManager().isPlayerAuthorized(player.getUniqueId());
       
       meta.setLore(Arrays.asList(
           ChatColor.GRAY + "Игрок: " + ChatColor.WHITE + player.getName(),
           ChatColor.GRAY + "Радиация: " + getRadiationColor(radiation) + radiation + "%",
           ChatColor.GRAY + "Авторизация: " + (authorized ? ChatColor.GREEN + "Есть" : ChatColor.RED + "Нет"),
           "",
           ChatColor.YELLOW + "Нажмите для обновления"
       ));
       item.setItemMeta(meta);
       return item;
   }
   
   private static ChatColor getRadiationColor(int level) {
       if (level < 20) return ChatColor.GREEN;
       if (level < 40) return ChatColor.YELLOW;
       if (level < 60) return ChatColor.GOLD;
       if (level < 80) return ChatColor.RED;
       return ChatColor.DARK_RED;
   }
}
