package com.uraniumcraft.items;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.ChatColor;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.NamespacedKey;
import java.util.Arrays;
import java.util.List;

public class AdvancedItems {
    
    // Силовая броня с режимами
    public static ItemStack createPowerArmorHelmet() {
        ItemStack helmet = new ItemStack(Material.NETHERITE_HELMET);
        ItemMeta meta = helmet.getItemMeta();
        meta.setDisplayName(ChatColor.AQUA + "Шлем силовой брони");
        meta.setLore(Arrays.asList(
            ChatColor.GRAY + "Продвинутая защита от радиации",
            ChatColor.GRAY + "Встроенный HUD дисплей",
            ChatColor.GREEN + "Защита от радиации: 90%",
            ChatColor.YELLOW + "ПКМ - переключение режима",
            ChatColor.BLUE + "Режим: Стандартный"
        ));
        meta.addEnchant(Enchantment.PROTECTION, 5, true);
        meta.addEnchant(Enchantment.UNBREAKING, 10, true);
        
        // Добавляем NBT данные для режимов
        NamespacedKey modeKey = new NamespacedKey("uraniumcraft", "armor_mode");
        meta.getPersistentDataContainer().set(modeKey, PersistentDataType.STRING, "standard");
        
        helmet.setItemMeta(meta);
        return helmet;
    }
    
    public static ItemStack createPowerArmorChestplate() {
        ItemStack chestplate = new ItemStack(Material.NETHERITE_CHESTPLATE);
        ItemMeta meta = chestplate.getItemMeta();
        meta.setDisplayName(ChatColor.AQUA + "Нагрудник силовой брони");
        meta.setLore(Arrays.asList(
            ChatColor.GRAY + "Энергетический щит",
            ChatColor.GRAY + "Встроенный реактор",
            ChatColor.GREEN + "Защита от радиации: 95%",
            ChatColor.YELLOW + "ПКМ - переключение режима",
            ChatColor.BLUE + "Режим: Защита",
            ChatColor.GOLD + "Энергия: 10000/10000"
        ));
        meta.addEnchant(Enchantment.PROTECTION, 6, true);
        meta.addEnchant(Enchantment.UNBREAKING, 10, true);
        
        NamespacedKey modeKey = new NamespacedKey("uraniumcraft", "armor_mode");
        NamespacedKey energyKey = new NamespacedKey("uraniumcraft", "energy");
        meta.getPersistentDataContainer().set(modeKey, PersistentDataType.STRING, "protection");
        meta.getPersistentDataContainer().set(energyKey, PersistentDataType.INTEGER, 10000);
        
        chestplate.setItemMeta(meta);
        return chestplate;
    }
    
    public static ItemStack createPowerArmorLeggings() {
        ItemStack leggings = new ItemStack(Material.NETHERITE_LEGGINGS);
        ItemMeta meta = leggings.getItemMeta();
        meta.setDisplayName(ChatColor.AQUA + "Поножи силовой брони");
        meta.setLore(Arrays.asList(
            ChatColor.GRAY + "Усиленная мобильность",
            ChatColor.GRAY + "Встроенные сервоприводы",
            ChatColor.GREEN + "Защита от радиации: 85%",
            ChatColor.YELLOW + "ПКМ - переключение режима",
            ChatColor.BLUE + "Режим: Скорость"
        ));
        meta.addEnchant(Enchantment.PROTECTION, 5, true);
        meta.addEnchant(Enchantment.UNBREAKING, 10, true);
        
        NamespacedKey modeKey = new NamespacedKey("uraniumcraft", "armor_mode");
        meta.getPersistentDataContainer().set(modeKey, PersistentDataType.STRING, "speed");
        
        leggings.setItemMeta(meta);
        return leggings;
    }
    
    public static ItemStack createPowerArmorBoots() {
        ItemStack boots = new ItemStack(Material.NETHERITE_BOOTS);
        ItemMeta meta = boots.getItemMeta();
        meta.setDisplayName(ChatColor.AQUA + "Ботинки силовой брони");
        meta.setLore(Arrays.asList(
            ChatColor.GRAY + "Амортизация падений",
            ChatColor.GRAY + "Увеличенная скорость",
            ChatColor.GREEN + "Защита от радиации: 80%",
            ChatColor.YELLOW + "ПКМ - переключение режима",
            ChatColor.BLUE + "Режим: Прыжки"
        ));
        meta.addEnchant(Enchantment.PROTECTION, 5, true);
        meta.addEnchant(Enchantment.FEATHER_FALLING, 10, true);
        meta.addEnchant(Enchantment.UNBREAKING, 10, true);
        
        NamespacedKey modeKey = new NamespacedKey("uraniumcraft", "armor_mode");
        meta.getPersistentDataContainer().set(modeKey, PersistentDataType.STRING, "jump");
        
        boots.setItemMeta(meta);
        return boots;
    }
    
    // Рельсотрон с режимами
    public static ItemStack createRailgun() {
        ItemStack railgun = new ItemStack(Material.CROSSBOW);
        ItemMeta meta = railgun.getItemMeta();
        meta.setDisplayName(ChatColor.RED + "Рельсотрон");
        meta.setLore(Arrays.asList(
            ChatColor.GRAY + "Электромагнитное оружие",
            ChatColor.YELLOW + "ПКМ - переключение режима",
            ChatColor.BLUE + "Режим: Одиночный выстрел",
            ChatColor.RED + "Урон: Экстремальный",
            ChatColor.GOLD + "Энергия: 1000/1000",
            "",
            ChatColor.DARK_GRAY + "Режимы:",
            ChatColor.WHITE + "• Одиночный - высокий урон",
            ChatColor.WHITE + "• Очередь - быстрая стрельба",
            ChatColor.WHITE + "• Пробивной - проходит сквозь блоки"
        ));
        meta.addEnchant(Enchantment.QUICK_CHARGE, 5, true);
        meta.addEnchant(Enchantment.MULTISHOT, 1, true);
        
        NamespacedKey modeKey = new NamespacedKey("uraniumcraft", "weapon_mode");
        NamespacedKey energyKey = new NamespacedKey("uraniumcraft", "energy");
        meta.getPersistentDataContainer().set(modeKey, PersistentDataType.STRING, "single");
        meta.getPersistentDataContainer().set(energyKey, PersistentDataType.INTEGER, 1000);
        
        railgun.setItemMeta(meta);
        return railgun;
    }
    
    // Костюм химзащиты
    public static ItemStack createHazmatHelmet() {
        ItemStack helmet = new ItemStack(Material.LEATHER_HELMET);
        ItemMeta meta = helmet.getItemMeta();
        meta.setDisplayName(ChatColor.YELLOW + "Шлем химзащиты");
        meta.setLore(Arrays.asList(
            ChatColor.GRAY + "Полная защита от радиации",
            ChatColor.GRAY + "Фильтрация воздуха",
            ChatColor.GREEN + "Защита от радиации: 100%"
        ));
        meta.addEnchant(Enchantment.PROTECTION, 3, true);
        helmet.setItemMeta(meta);
        return helmet;
    }
    
    public static ItemStack createHazmatSuit() {
        ItemStack suit = new ItemStack(Material.LEATHER_CHESTPLATE);
        ItemMeta meta = suit.getItemMeta();
        meta.setDisplayName(ChatColor.YELLOW + "Костюм химзащиты");
        meta.setLore(Arrays.asList(
            ChatColor.GRAY + "Герметичная защита",
            ChatColor.GRAY + "Система жизнеобеспечения",
            ChatColor.GREEN + "Защита от радиации: 100%"
        ));
        meta.addEnchant(Enchantment.PROTECTION, 3, true);
        suit.setItemMeta(meta);
        return suit;
    }
    
    // Электротранспорт с режимами
    public static ItemStack createElectricCar() {
        ItemStack car = new ItemStack(Material.MINECART);
        ItemMeta meta = car.getItemMeta();
        meta.setDisplayName(ChatColor.BLUE + "Электромобиль");
        meta.setLore(Arrays.asList(
            ChatColor.GRAY + "Экологически чистый транспорт",
            ChatColor.GRAY + "Работает на электричестве",
            ChatColor.GREEN + "Скорость: Высокая",
            ChatColor.YELLOW + "ПКМ - переключение режима",
            ChatColor.BLUE + "Режим: Стандартный",
            ChatColor.GOLD + "Энергия: 5000/5000",
            "",
            ChatColor.DARK_GRAY + "Режимы:",
            ChatColor.WHITE + "• Стандартный - обычная скорость",
            ChatColor.WHITE + "• Турбо - максимальная скорость",
            ChatColor.WHITE + "• Эко - экономия энергии"
        ));
        
        NamespacedKey modeKey = new NamespacedKey("uraniumcraft", "transport_mode");
        NamespacedKey energyKey = new NamespacedKey("uraniumcraft", "energy");
        meta.getPersistentDataContainer().set(modeKey, PersistentDataType.STRING, "standard");
        meta.getPersistentDataContainer().set(energyKey, PersistentDataType.INTEGER, 5000);
        
        car.setItemMeta(meta);
        return car;
    }
    
    // Автошахтёр с режимами
    public static ItemStack createAutoMiner() {
        ItemStack miner = new ItemStack(Material.DISPENSER);
        ItemMeta meta = miner.getItemMeta();
        meta.setDisplayName(ChatColor.GOLD + "Автошахтёр");
        meta.setLore(Arrays.asList(
            ChatColor.GRAY + "Автоматическая добыча ресурсов",
            ChatColor.GRAY + "Работает на энергии",
            ChatColor.GREEN + "Эффективность: Высокая",
            ChatColor.YELLOW + "ПКМ - переключение режима",
            ChatColor.BLUE + "Режим: Обычная добыча",
            ChatColor.GOLD + "Энергия: 2000/2000",
            "",
            ChatColor.DARK_GRAY + "Режимы:",
            ChatColor.WHITE + "• Обычная - добыча всех блоков",
            ChatColor.WHITE + "• Руды - только ценные руды",
            ChatColor.WHITE + "• Глубокая - добыча вниз"
        ));
        
        NamespacedKey modeKey = new NamespacedKey("uraniumcraft", "miner_mode");
        NamespacedKey energyKey = new NamespacedKey("uraniumcraft", "energy");
        meta.getPersistentDataContainer().set(modeKey, PersistentDataType.STRING, "normal");
        meta.getPersistentDataContainer().set(energyKey, PersistentDataType.INTEGER, 2000);
        
        miner.setItemMeta(meta);
        return miner;
    }
    
    // Капсула для урана
    public static ItemStack createUraniumCapsule() {
        ItemStack capsule = new ItemStack(Material.END_CRYSTAL);
        ItemMeta meta = capsule.getItemMeta();
        meta.setDisplayName(ChatColor.GREEN + "Урановая капсула");
        meta.setLore(Arrays.asList(
            ChatColor.GRAY + "Безопасное хранение урана",
            ChatColor.GRAY + "Защита от радиации",
            ChatColor.YELLOW + "Вместимость: 64 единицы урана",
            ChatColor.GREEN + "Хранится: 0/64",
            "",
            ChatColor.YELLOW + "ПКМ - поместить уран",
            ChatColor.YELLOW + "Shift+ПКМ - извлечь уран"
        ));
        
        NamespacedKey storageKey = new NamespacedKey("uraniumcraft", "uranium_storage");
        meta.getPersistentDataContainer().set(storageKey, PersistentDataType.INTEGER, 0);
        
        capsule.setItemMeta(meta);
        return capsule;
    }
    
    // Блок лаборатории
    public static ItemStack createLaboratoryBlock() {
        ItemStack labBlock = new ItemStack(Material.BEACON);
        ItemMeta meta = labBlock.getItemMeta();
        meta.setDisplayName(ChatColor.LIGHT_PURPLE + "Блок лаборатории");
        meta.setLore(Arrays.asList(
            ChatColor.GRAY + "Основа для строительства лаборатории",
            ChatColor.GRAY + "Требует авторизации администратора",
            ChatColor.RED + "Только для авторизованных игроков!",
            "",
            ChatColor.YELLOW + "Поставьте и начните строительство"
        ));
        labBlock.setItemMeta(meta);
        return labBlock;
    }
}
