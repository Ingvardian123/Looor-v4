package com.uraniumcraft.items;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.*;

public class UraniumTablet {
    
    public enum TabletType {
        STANDARD("Стандартный урановый планшет", Material.RECOVERY_COMPASS, 2000, NamedTextColor.GREEN),
        ADVANCED("Продвинутый урановый планшет", Material.ECHO_SHARD, 5000, NamedTextColor.BLUE),
        QUANTUM("Квантовый урановый планшет", Material.NETHERITE_UPGRADE_SMITHING_TEMPLATE, 10000, NamedTextColor.DARK_PURPLE),
        REALITY("Планшет реальности", Material.NETHER_STAR, 25000, NamedTextColor.GOLD);
        
        private final String displayName;
        private final Material material;
        private final int maxEnergy;
        private final NamedTextColor color;
        
        TabletType(String displayName, Material material, int maxEnergy, NamedTextColor color) {
            this.displayName = displayName;
            this.material = material;
            this.maxEnergy = maxEnergy;
            this.color = color;
        }
        
        public String getDisplayName() { return displayName; }
        public Material getMaterial() { return material; }
        public int getMaxEnergy() { return maxEnergy; }
        public NamedTextColor getColor() { return color; }
    }
    
    public enum TabletModule {
        RADIATION_SCANNER("Радиационный сканер", "Улучшенное обнаружение радиации", 50),
        QUANTUM_PROCESSOR("Квантовый процессор", "Ускоренная обработка данных", 100),
        ENERGY_AMPLIFIER("Энергетический усилитель", "Увеличенная емкость батареи", 75),
        HOLOGRAPHIC_PROJECTOR("Голографический проектор", "3D проекции и визуализация", 125),
        TEMPORAL_STABILIZER("Временной стабилизатор", "Контроль временных аномалий", 200),
        MATTER_SYNTHESIZER("Синтезатор материи", "Создание предметов из энергии", 300),
        DIMENSIONAL_GATEWAY("Межмерный портал", "Доступ к параллельным измерениям", 500),
        REALITY_ANCHOR("Якорь реальности", "Стабилизация пространства-времени", 1000);
        
        private final String name;
        private final String description;
        private final int energyCost;
        
        TabletModule(String name, String description, int energyCost) {
            this.name = name;
            this.description = description;
            this.energyCost = energyCost;
        }
        
        public String getName() { return name; }
        public String getDescription() { return description; }
        public int getEnergyCost() { return energyCost; }
    }
    
    public static ItemStack createTablet(Plugin plugin, TabletType type) {
        return createTablet(plugin, type, type.getMaxEnergy(), new HashSet<>());
    }
    
    public static ItemStack createTablet(Plugin plugin, TabletType type, int currentEnergy, Set<TabletModule> modules) {
        ItemStack tablet = new ItemStack(type.getMaterial());
        ItemMeta meta = tablet.getItemMeta();
        
        // Создаем название с эффектами
        Component name = createTabletName(type);
        meta.displayName(name);
        
        // Создаем описание
        List<Component> lore = createTabletLore(type, currentEnergy, modules);
        meta.lore(lore);
        
        // Добавляем энчанты для визуального эффекта
        addTabletEnchantments(meta, type);
        
        // Сохраняем данные в NBT
        saveTabletData(meta, plugin, type, currentEnergy, modules);
        
        tablet.setItemMeta(meta);
        return tablet;
    }
    
    private static Component createTabletName(TabletType type) {
        String prefix = getTabletPrefix(type);
        return Component.text()
            .append(Component.text(prefix + " ", NamedTextColor.YELLOW))
            .append(Component.text(type.getDisplayName(), type.getColor(), TextDecoration.BOLD))
            .append(Component.text(" " + prefix, NamedTextColor.YELLOW))
            .decoration(TextDecoration.ITALIC, false)
            .build();
    }
    
    private static String getTabletPrefix(TabletType type) {
        switch (type) {
            case STANDARD: return "⚡";
            case ADVANCED: return "🌟";
            case QUANTUM: return "⚛️";
            case REALITY: return "🌌";
            default: return "📱";
        }
    }
    
    private static List<Component> createTabletLore(TabletType type, int currentEnergy, Set<TabletModule> modules) {
        List<Component> lore = new ArrayList<>();
        
        // Заголовок
        lore.add(Component.text("▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬", NamedTextColor.DARK_GRAY));
        lore.add(Component.empty());
        
        // Энергия с визуальным индикатором
        double energyPercent = (double) currentEnergy / type.getMaxEnergy() * 100;
        String energyBar = createEnergyBar(energyPercent);
        NamedTextColor energyColor = getEnergyColor(energyPercent);
        
        lore.add(Component.text("🔋 Энергия:", NamedTextColor.GRAY)
            .append(Component.text(" " + energyBar, energyColor))
            .append(Component.text(" " + String.format("%.1f%%", energyPercent), NamedTextColor.WHITE)));
        lore.add(Component.text("   " + currentEnergy + " / " + type.getMaxEnergy() + " единиц", NamedTextColor.GRAY));
        lore.add(Component.empty());
        
        // Базовые функции
        lore.add(Component.text("📱 Базовые функции:", NamedTextColor.GOLD, TextDecoration.BOLD));
        lore.add(Component.text("  • Управление лабораториями", NamedTextColor.AQUA));
        lore.add(Component.text("  • Мониторинг радиации", NamedTextColor.YELLOW));
        lore.add(Component.text("  • Система телепортации", NamedTextColor.LIGHT_PURPLE));
        lore.add(Component.text("  • Статистика и достижения", NamedTextColor.GREEN));
        
        // Продвинутые функции
        if (type.ordinal() >= TabletType.ADVANCED.ordinal()) {
            lore.add(Component.empty());
            lore.add(Component.text("🌟 Продвинутые функции:", NamedTextColor.BLUE, TextDecoration.BOLD));
            lore.add(Component.text("  • Квантовая телепортация", NamedTextColor.DARK_PURPLE));
            lore.add(Component.text("  • Голографические проекции", NamedTextColor.BLUE));
            lore.add(Component.text("  • Автоматизация процессов", NamedTextColor.CYAN));
        }
        
        // Квантовые функции
        if (type.ordinal() >= TabletType.QUANTUM.ordinal()) {
            lore.add(Component.empty());
            lore.add(Component.text("⚛️ Квантовые функции:", NamedTextColor.DARK_PURPLE, TextDecoration.BOLD));
            lore.add(Component.text("  • Временные аномалии", NamedTextColor.RED));
            lore.add(Component.text("  • Материализация материи", NamedTextColor.DARK_RED));
            lore.add(Component.text("  • Квантовое сканирование", NamedTextColor.DARK_BLUE));
        }
        
        // Функции реальности
        if (type == TabletType.REALITY) {
            lore.add(Component.empty());
            lore.add(Component.text("🌌 Функции реальности:", NamedTextColor.GOLD, TextDecoration.BOLD));
            lore.add(Component.text("  • Контроль пространства-времени", NamedTextColor.DARK_RED));
            lore.add(Component.text("  • Межмерные порталы", NamedTextColor.DARK_GREEN));
            lore.add(Component.text("  • Манипуляция реальностью", NamedTextColor.DARK_PURPLE));
        }
        
        // Установленные модули
        if (!modules.isEmpty()) {
            lore.add(Component.empty());
            lore.add(Component.text("🔧 Установленные модули:", NamedTextColor.YELLOW, TextDecoration.BOLD));
            for (TabletModule module : modules) {
                lore.add(Component.text("  • " + module.getName(), NamedTextColor.WHITE));
                lore.add(Component.text("    " + module.getDescription(), NamedTextColor.GRAY));
            }
        }
        
        // Инструкции по использованию
        lore.add(Component.empty());
        lore.add(Component.text("▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬", NamedTextColor.DARK_GRAY));
        lore.add(Component.text("💡 Управление:", NamedTextColor.YELLOW, TextDecoration.BOLD));
        lore.add(Component.text("  ПКМ - Главное меню", NamedTextColor.WHITE));
        lore.add(Component.text("  Shift+ПКМ - Быстрый статус", NamedTextColor.WHITE));
        lore.add(Component.text("  F - Голографическая проекция", NamedTextColor.WHITE));
        lore.add(Component.text("  Q - Квантовое сканирование", NamedTextColor.WHITE));
        lore.add(Component.empty());
        lore.add(Component.text("⚠️ Планшет защищен от потери!", NamedTextColor.RED, TextDecoration.BOLD));
        lore.add(Component.text("🔄 Автоматическая регенерация энергии", NamedTextColor.GREEN));
        lore.add(Component.text("▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬", NamedTextColor.DARK_GRAY));
        
        return lore;
    }
    
    private static void addTabletEnchantments(ItemMeta meta, TabletType type) {
        switch (type) {
            case STANDARD:
                meta.addEnchant(Enchantment.UNBREAKING, 1, true);
                break;
            case ADVANCED:
                meta.addEnchant(Enchantment.UNBREAKING, 3, true);
                meta.addEnchant(Enchantment.MENDING, 1, true);
                break;
            case QUANTUM:
                meta.addEnchant(Enchantment.UNBREAKING, 5, true);
                meta.addEnchant(Enchantment.MENDING, 1, true);
                meta.addEnchant(Enchantment.EFFICIENCY, 5, true);
                break;
            case REALITY:
                meta.addEnchant(Enchantment.UNBREAKING, 10, true);
                meta.addEnchant(Enchantment.MENDING, 1, true);
                meta.addEnchant(Enchantment.EFFICIENCY, 10, true);
                meta.addEnchant(Enchantment.FORTUNE, 5, true);
                break;
        }
        
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES);
        meta.setUnbreakable(true);
    }
    
    private static void saveTabletData(ItemMeta meta, Plugin plugin, TabletType type, int energy, Set<TabletModule> modules) {
        NamespacedKey typeKey = new NamespacedKey(plugin, "tablet_type");
        NamespacedKey energyKey = new NamespacedKey(plugin, "tablet_energy");
        NamespacedKey maxEnergyKey = new NamespacedKey(plugin, "tablet_max_energy");
        NamespacedKey modulesKey = new NamespacedKey(plugin, "tablet_modules");
        NamespacedKey versionKey = new NamespacedKey(plugin, "tablet_version");
        
        meta.getPersistentDataContainer().set(typeKey, PersistentDataType.STRING, type.name());
        meta.getPersistentDataContainer().set(energyKey, PersistentDataType.INTEGER, energy);
        meta.getPersistentDataContainer().set(maxEnergyKey, PersistentDataType.INTEGER, type.getMaxEnergy());
        meta.getPersistentDataContainer().set(versionKey, PersistentDataType.STRING, "2.0");
        
        // Сохраняем модули как строку
        if (!modules.isEmpty()) {
            StringBuilder moduleString = new StringBuilder();
            for (TabletModule module : modules) {
                if (moduleString.length() > 0) moduleString.append(",");
                moduleString.append(module.name());
            }
            meta.getPersistentDataContainer().set(modulesKey, PersistentDataType.STRING, moduleString.toString());
        }
    }
    
    private static String createEnergyBar(double percent) {
        int bars = (int) (percent / 5); // 20 символов для более точного отображения
        StringBuilder bar = new StringBuilder();
        
        for (int i = 0; i < 20; i++) {
            if (i < bars) {
                if (percent > 80) bar.append("█");
                else if (percent > 60) bar.append("▉");
                else if (percent > 40) bar.append("▊");
                else if (percent > 20) bar.append("▋");
                else bar.append("▌");
            } else {
                bar.append("░");
            }
        }
        
        return bar.toString();
    }
    
    private static NamedTextColor getEnergyColor(double percent) {
        if (percent > 80) return NamedTextColor.GREEN;
        if (percent > 60) return NamedTextColor.YELLOW;
        if (percent > 40) return NamedTextColor.GOLD;
        if (percent > 20) return NamedTextColor.RED;
        return NamedTextColor.DARK_RED;
    }
    
    // Методы для работы с планшетом
    public static TabletType getTabletType(ItemStack item, Plugin plugin) {
        if (item == null || !item.hasItemMeta()) return null;
        
        NamespacedKey typeKey = new NamespacedKey(plugin, "tablet_type");
        String typeString = item.getItemMeta().getPersistentDataContainer().get(typeKey, PersistentDataType.STRING);
        
        if (typeString != null) {
            try {
                return TabletType.valueOf(typeString);
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
        
        return null;
    }
    
    public static int getTabletEnergy(ItemStack item, Plugin plugin) {
        if (item == null || !item.hasItemMeta()) return 0;
        
        NamespacedKey energyKey = new NamespacedKey(plugin, "tablet_energy");
        return item.getItemMeta().getPersistentDataContainer().getOrDefault(energyKey, PersistentDataType.INTEGER, 0);
    }
    
    public static void setTabletEnergy(ItemStack item, Plugin plugin, int energy) {
        if (item == null || !item.hasItemMeta()) return;
        
        TabletType type = getTabletType(item, plugin);
        if (type == null) return;
        
        ItemMeta meta = item.getItemMeta();
        NamespacedKey energyKey = new NamespacedKey(plugin, "tablet_energy");
        
        energy = Math.max(0, Math.min(energy, type.getMaxEnergy()));
        meta.getPersistentDataContainer().set(energyKey, PersistentDataType.INTEGER, energy);
        
        // Обновляем описание
        Set<TabletModule> modules = getTabletModules(item, plugin);
        List<Component> lore = createTabletLore(type, energy, modules);
        meta.lore(lore);
        
        item.setItemMeta(meta);
    }
    
    public static boolean consumeEnergy(ItemStack item, Plugin plugin, int amount) {
        int currentEnergy = getTabletEnergy(item, plugin);
        if (currentEnergy < amount) {
            return false;
        }
        
        setTabletEnergy(item, plugin, currentEnergy - amount);
        return true;
    }
    
    public static Set<TabletModule> getTabletModules(ItemStack item, Plugin plugin) {
        if (item == null || !item.hasItemMeta()) return new HashSet<>();
        
        NamespacedKey modulesKey = new NamespacedKey(plugin, "tablet_modules");
        String modulesString = item.getItemMeta().getPersistentDataContainer().get(modulesKey, PersistentDataType.STRING);
        
        Set<TabletModule> modules = new HashSet<>();
        if (modulesString != null && !modulesString.isEmpty()) {
            String[] moduleNames = modulesString.split(",");
            for (String moduleName : moduleNames) {
                try {
                    modules.add(TabletModule.valueOf(moduleName));
                } catch (IllegalArgumentException ignored) {
                    // Игнорируем неизвестные модули
                }
            }
        }
        
        return modules;
    }
    
    public static void addTabletModule(ItemStack item, Plugin plugin, TabletModule module) {
        Set<TabletModule> modules = getTabletModules(item, plugin);
        modules.add(module);
        
        TabletType type = getTabletType(item, plugin);
        int energy = getTabletEnergy(item, plugin);
        
        ItemStack newTablet = createTablet(plugin, type, energy, modules);
        item.setItemMeta(newTablet.getItemMeta());
    }
    
    public static boolean hasTabletModule(ItemStack item, Plugin plugin, TabletModule module) {
        return getTabletModules(item, plugin).contains(module);
    }
    
    public static boolean isUraniumTablet(ItemStack item, Plugin plugin) {
        return getTabletType(item, plugin) != null;
    }
    
    public static int getEnergyRegenRate(TabletType type, Set<TabletModule> modules) {
        int baseRegen = switch (type) {
            case STANDARD -> 10;
            case ADVANCED -> 20;
            case QUANTUM -> 35;
            case REALITY -> 50;
        };
        
        // Бонус от модулей
        if (modules.contains(TabletModule.ENERGY_AMPLIFIER)) {
            baseRegen = (int) (baseRegen * 1.5);
        }
        
        if (modules.contains(TabletModule.QUANTUM_PROCESSOR)) {
            baseRegen = (int) (baseRegen * 1.25);
        }
        
        return baseRegen;
    }
    
    public static boolean canUseFunction(ItemStack item, Plugin plugin, String functionName) {
        TabletType type = getTabletType(item, plugin);
        if (type == null) return false;
        
        Set<TabletModule> modules = getTabletModules(item, plugin);
        
        switch (functionName.toLowerCase()) {
            case "quantum_teleportation":
                return type.ordinal() >= TabletType.ADVANCED.ordinal();
            case "holographic_projection":
                return type.ordinal() >= TabletType.ADVANCED.ordinal() || 
                       modules.contains(TabletModule.HOLOGRAPHIC_PROJECTOR);
            case "time_anomaly":
                return type.ordinal() >= TabletType.QUANTUM.ordinal() || 
                       modules.contains(TabletModule.TEMPORAL_STABILIZER);
            case "matter_synthesis":
                return type.ordinal() >= TabletType.QUANTUM.ordinal() || 
                       modules.contains(TabletModule.MATTER_SYNTHESIZER);
            case "dimensional_portal":
                return type == TabletType.REALITY || 
                       modules.contains(TabletModule.DIMENSIONAL_GATEWAY);
            case "reality_manipulation":
                return type == TabletType.REALITY || 
                       modules.contains(TabletModule.REALITY_ANCHOR);
            default:
                return true; // Базовые функции доступны всем
        }
    }
}
