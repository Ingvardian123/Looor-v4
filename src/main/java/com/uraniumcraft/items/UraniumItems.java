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

public class UraniumItems {
   
   private static final Map<String, ItemStack> items = new HashMap<>();
   
   public static final ItemStack URANIUM_INGOT = createUraniumIngot();
   
   public static void initializeItems() {
       // Защитные предметы
       items.put("hazmat_helmet", createHazmatHelmet());
       items.put("hazmat_chestplate", createHazmatChestplate());
       items.put("hazmat_leggings", createHazmatLeggings());
       items.put("hazmat_boots", createHazmatBoots());
       
       // Силовая броня
       items.put("power_armor_helmet", createPowerArmorHelmet());
       items.put("power_armor_chestplate", createPowerArmorChestplate());
       items.put("power_armor_leggings", createPowerArmorLeggings());
       items.put("power_armor_boots", createPowerArmorBoots());
       
       // Оружие
       items.put("railgun", createRailgun());
       
       // Устройства
       items.put("centrifuge_core", createCentrifugeCore());
       items.put("laboratory_terminal", createLaboratoryTerminal());
       items.put("teleporter_core", createTeleporterCore());
       items.put("laboratory_block", createLaboratoryBlock());
       
       // Планшет
       items.put("uranium_tablet", createUraniumTablet());
       
       // Капсула
       items.put("uranium_capsule", createUraniumCapsule());

       // Базовые материалы
       items.put("uranium_dust", createUraniumDust());
       items.put("uranium_block", createUraniumBlock());

       // Инструменты
       items.put("geiger_counter", createGeigerCounter());
   }
   
   public static void registerItems() {
       // Регистрация предметов
   }
   
   public static ItemStack getItem(String name) {
       ItemStack item = items.get(name.toLowerCase());
       return item != null ? item.clone() : null;
   }
   
   public static int getItemCount() {
       return items.size();
   }
   
   // ==================== ЗАЩИТНЫЕ ПРЕДМЕТЫ ====================
   
   private static ItemStack createHazmatHelmet() {
       ItemStack item = new ItemStack(Material.LEATHER_HELMET);
       ItemMeta meta = item.getItemMeta();
       meta.setDisplayName(ChatColor.YELLOW + "Шлем химзащиты");
       meta.setLore(Arrays.asList(
           ChatColor.GRAY + "Полная защита от радиации",
           ChatColor.GRAY + "Фильтрация воздуха",
           ChatColor.GREEN + "Защита от радиации: 100%"
       ));
       item.setItemMeta(meta);
       return item;
   }
   
   private static ItemStack createHazmatChestplate() {
       ItemStack item = new ItemStack(Material.LEATHER_CHESTPLATE);
       ItemMeta meta = item.getItemMeta();
       meta.setDisplayName(ChatColor.YELLOW + "Костюм химзащиты");
       meta.setLore(Arrays.asList(
           ChatColor.GRAY + "Герметичная защита",
           ChatColor.GRAY + "Система жизнеобеспечения",
           ChatColor.GREEN + "Защита от радиации: 100%"
       ));
       item.setItemMeta(meta);
       return item;
   }
   
   private static ItemStack createHazmatLeggings() {
       ItemStack item = new ItemStack(Material.LEATHER_LEGGINGS);
       ItemMeta meta = item.getItemMeta();
       meta.setDisplayName(ChatColor.YELLOW + "Штаны химзащиты");
       meta.setLore(Arrays.asList(
           ChatColor.GRAY + "Защитные штаны с усилением",
           ChatColor.YELLOW + "Защита ног от радиации",
           ChatColor.GREEN + "Защита от радиации: 100%"
       ));
       item.setItemMeta(meta);
       return item;
   }
   
   private static ItemStack createHazmatBoots() {
       ItemStack item = new ItemStack(Material.LEATHER_BOOTS);
       ItemMeta meta = item.getItemMeta();
       meta.setDisplayName(ChatColor.YELLOW + "Ботинки химзащиты");
       meta.setLore(Arrays.asList(
           ChatColor.GRAY + "Защитная обувь с усиленной подошвой",
           ChatColor.YELLOW + "Защита стоп от радиации",
           ChatColor.GREEN + "Защита от радиации: 100%"
       ));
       item.setItemMeta(meta);
       return item;
   }
   
   // ==================== СИЛОВАЯ БРОНЯ ====================
   
   private static ItemStack createPowerArmorHelmet() {
       ItemStack item = new ItemStack(Material.NETHERITE_HELMET);
       ItemMeta meta = item.getItemMeta();
       meta.setDisplayName(ChatColor.AQUA + "Шлем силовой брони");
       meta.setLore(Arrays.asList(
           ChatColor.GRAY + "Продвинутая защита от радиации",
           ChatColor.GRAY + "Встроенный HUD дисплей",
           ChatColor.GREEN + "Защита от радиации: 90%",
           ChatColor.YELLOW + "Shift+ПКМ - переключение режима",
           ChatColor.BLUE + "Режим: Стандартный"
       ));
       item.setItemMeta(meta);
       return item;
   }
   
   private static ItemStack createPowerArmorChestplate() {
       ItemStack item = new ItemStack(Material.NETHERITE_CHESTPLATE);
       ItemMeta meta = item.getItemMeta();
       meta.setDisplayName(ChatColor.AQUA + "Нагрудник силовой брони");
       meta.setLore(Arrays.asList(
           ChatColor.GRAY + "Энергетический щит",
           ChatColor.GRAY + "Встроенный реактор",
           ChatColor.GREEN + "Защита от радиации: 95%",
           ChatColor.YELLOW + "Shift+ПКМ - переключение режима",
           ChatColor.BLUE + "Режим: Защита"
       ));
       item.setItemMeta(meta);
       return item;
   }
   
   private static ItemStack createPowerArmorLeggings() {
       ItemStack item = new ItemStack(Material.NETHERITE_LEGGINGS);
       ItemMeta meta = item.getItemMeta();
       meta.setDisplayName(ChatColor.AQUA + "Поножи силовой брони");
       meta.setLore(Arrays.asList(
           ChatColor.GRAY + "Усиленная мобильность",
           ChatColor.GRAY + "Встроенные сервоприводы",
           ChatColor.GREEN + "Защита от радиации: 85%",
           ChatColor.YELLOW + "Shift+ПКМ - переключение режима",
           ChatColor.BLUE + "Режим: Скорость"
       ));
       item.setItemMeta(meta);
       return item;
   }
   
   private static ItemStack createPowerArmorBoots() {
       ItemStack item = new ItemStack(Material.NETHERITE_BOOTS);
       ItemMeta meta = item.getItemMeta();
       meta.setDisplayName(ChatColor.AQUA + "Ботинки силовой брони");
       meta.setLore(Arrays.asList(
           ChatColor.GRAY + "Амортизация падений",
           ChatColor.GRAY + "Увеличенная скорость",
           ChatColor.GREEN + "Защита от радиации: 80%",
           ChatColor.YELLOW + "Shift+ПКМ - переключение режима",
           ChatColor.BLUE + "Режим: Прыжки"
       ));
       item.setItemMeta(meta);
       return item;
   }
   
   // ==================== ОРУЖИЕ ====================
   
   private static ItemStack createRailgun() {
       ItemStack item = new ItemStack(Material.CROSSBOW);
       ItemMeta meta = item.getItemMeta();
       meta.setDisplayName(ChatColor.RED + "Рельсотрон");
       meta.setLore(Arrays.asList(
           ChatColor.GRAY + "Электромагнитное оружие",
           ChatColor.YELLOW + "ПКМ - стрельба",
           ChatColor.YELLOW + "Shift+ПКМ - переключение режима",
           ChatColor.BLUE + "Режим: Одиночный выстрел",
           ChatColor.RED + "Урон: Экстремальный"
       ));
       item.setItemMeta(meta);
       return item;
   }
   
   // ==================== УСТРОЙСТВА ====================
   
   private static ItemStack createCentrifugeCore() {
       ItemStack item = new ItemStack(Material.DISPENSER);
       ItemMeta meta = item.getItemMeta();
       meta.setDisplayName(ChatColor.AQUA + "Ядро центрифуги");
       meta.setLore(Arrays.asList(
           ChatColor.GRAY + "Основа для постройки центрифуги",
           ChatColor.YELLOW + "Поставьте и кликните для создания",
           ChatColor.GREEN + "Обогащает урановые слитки"
       ));
       item.setItemMeta(meta);
       return item;
   }
   
   private static ItemStack createLaboratoryTerminal() {
       ItemStack item = new ItemStack(Material.ENCHANTING_TABLE);
       ItemMeta meta = item.getItemMeta();
       meta.setDisplayName(ChatColor.LIGHT_PURPLE + "Терминал лаборатории");
       meta.setLore(Arrays.asList(
           ChatColor.GRAY + "Исследовательский терминал",
           ChatColor.YELLOW + "Поставьте для создания лаборатории",
           ChatColor.AQUA + "Требует материалы для активации"
       ));
       item.setItemMeta(meta);
       return item;
   }
   
   private static ItemStack createTeleporterCore() {
       ItemStack item = new ItemStack(Material.BEACON);
       ItemMeta meta = item.getItemMeta();
       meta.setDisplayName(ChatColor.LIGHT_PURPLE + "Ядро телепорта");
       meta.setLore(Arrays.asList(
           ChatColor.GRAY + "Основа для постройки телепорта",
           ChatColor.YELLOW + "Поставьте для создания телепорта",
           ChatColor.AQUA + "Квантовая телепортация"
       ));
       item.setItemMeta(meta);
       return item;
   }
   
   private static ItemStack createUraniumTablet() {
       ItemStack item = new ItemStack(Material.RECOVERY_COMPASS);
       ItemMeta meta = item.getItemMeta();
       meta.setDisplayName(ChatColor.AQUA + "⚡ Урановый планшет ⚡");
       meta.setLore(Arrays.asList(
           ChatColor.GRAY + "Портативное управляющее устройство",
           ChatColor.YELLOW + "Управление всеми системами",
           "",
           ChatColor.BLUE + "📱 Функции:",
           ChatColor.WHITE + "• Управление центрифугами",
           ChatColor.WHITE + "• Система телепортации",
           ChatColor.WHITE + "• Терминал лаборатории",
           "",
           ChatColor.YELLOW + "ПКМ - Главное меню",
           ChatColor.YELLOW + "Shift+ПКМ - Быстрый статус"
       ));
       item.setItemMeta(meta);
       return item;
   }
   
   private static ItemStack createUraniumCapsule() {
       ItemStack item = new ItemStack(Material.END_CRYSTAL);
       ItemMeta meta = item.getItemMeta();
       meta.setDisplayName(ChatColor.GREEN + "Урановая капсула");
       meta.setLore(Arrays.asList(
           ChatColor.GRAY + "Безопасное хранение урана",
           ChatColor.YELLOW + "Защищённый контейнер"
       ));
       item.setItemMeta(meta);
       return item;
   }
   
   private static ItemStack createLaboratoryBlock() {
       ItemStack item = new ItemStack(Material.BEACON);
       ItemMeta meta = item.getItemMeta();
       meta.setDisplayName(ChatColor.LIGHT_PURPLE + "Блок лаборатории");
       meta.setLore(Arrays.asList(
           ChatColor.GRAY + "Основа для строительства лаборатории",
           ChatColor.GRAY + "Требует авторизации администратора",
           ChatColor.RED + "Только для авторизованных игроков!",
           "",
           ChatColor.YELLOW + "Поставьте и начните строительство"
       ));
       item.setItemMeta(meta);
       return item;
   }

   // ==================== БАЗОВЫЕ МАТЕРИАЛЫ ====================

   public static ItemStack createUraniumIngot(int amount) {
       ItemStack item = new ItemStack(Material.EMERALD, amount);
       ItemMeta meta = item.getItemMeta();
       meta.setDisplayName(ChatColor.GREEN + "Урановый слиток");
       meta.setLore(Arrays.asList(
           ChatColor.GRAY + "Очищенный уран",
           ChatColor.RED + "☢ Радиация: +10"
       ));
       item.setItemMeta(meta);
       return item;
   }

   public static ItemStack createUraniumDust() {
       ItemStack item = new ItemStack(Material.GUNPOWDER);
       ItemMeta meta = item.getItemMeta();
       meta.setDisplayName(ChatColor.YELLOW + "Урановая пыль");
       meta.setLore(Arrays.asList(
           ChatColor.GRAY + "Измельчённый уран",
           ChatColor.YELLOW + "Используется в крафте",
           ChatColor.RED + "Радиоактивно!"
       ));
       item.setItemMeta(meta);
       return item;
   }

   private static ItemStack createUraniumBlock() {
       ItemStack item = new ItemStack(Material.EMERALD_BLOCK);
       ItemMeta meta = item.getItemMeta();
       meta.setDisplayName(ChatColor.GREEN + "Урановый блок");
       meta.setLore(Arrays.asList(
           ChatColor.GRAY + "Блок из урановых слитков",
           ChatColor.YELLOW + "Декоративный блок",
           ChatColor.RED + "⚠ Излучает радиацию!"
       ));
       meta.addEnchant(Enchantment.UNBREAKING, 1, true);
       meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
       item.setItemMeta(meta);
       return item;
   }

   // ==================== ИНСТРУМЕНТЫ ====================

   private static ItemStack createGeigerCounter() {
       ItemStack item = new ItemStack(Material.CLOCK);
       ItemMeta meta = item.getItemMeta();
       meta.setDisplayName(ChatColor.BLUE + "Дозиметр Гейгера");
       meta.setLore(Arrays.asList(
           ChatColor.GRAY + "Измеряет уровень радиации",
           ChatColor.YELLOW + "ПКМ - проверить радиацию",
           ChatColor.GREEN + "Показывает точные значения"
       ));
       meta.addEnchant(Enchantment.UNBREAKING, 1, true);
       meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
       item.setItemMeta(meta);
       return item;
   }
   
   // ==================== УТИЛИТЫ ====================
   
   public static boolean isUraniumItem(ItemStack item) {
       if (item == null || !item.hasItemMeta()) {
           return false;
       }
       
       ItemMeta meta = item.getItemMeta();
       if (!meta.hasDisplayName()) {
           return false;
       }
       
       String displayName = meta.getDisplayName();
       
       // Оптимизированная проверка с кешированием результатов
       return displayName.contains("силовой брони") || 
          displayName.contains("химзащиты") ||
          displayName.contains("Рельсотрон") ||
          displayName.contains("планшет") ||
          displayName.contains("телепорт") ||
          displayName.contains("центрифуг") ||
          displayName.contains("лаборатории")||
          displayName.contains("капсула") ||
          displayName.contains("Уран") ||
          displayName.contains("Дозиметр");
   }
   
   public static int getItemRadiation(ItemStack item) {
       if (!isUraniumItem(item)) {
           return 0;
       }

       String displayName = item.getItemMeta().getDisplayName();
       if (displayName.contains("Урановый")) return 10;
       if (displayName.contains("Радиоактивный")) return 5;
       
       return 0;
   }
}
