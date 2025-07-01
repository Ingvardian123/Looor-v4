package com.uraniumcraft.gui;

import com.uraniumcraft.UraniumPlugin;
import com.uraniumcraft.items.UraniumTablet;
import com.uraniumcraft.laboratory.LaboratoryTerminal;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class TabletGUI {
    
    public static void openMainMenu(Player player, UraniumPlugin plugin) {
        Inventory gui = Bukkit.createInventory(null, 54, ChatColor.AQUA + "⚡ Урановый планшет ⚡");
        
        // Центрифуги
        ItemStack centrifuge = new ItemStack(Material.DISPENSER);
        ItemMeta centrifugeMeta = centrifuge.getItemMeta();
        centrifugeMeta.setDisplayName(ChatColor.AQUA + "Управление центрифугами");
        centrifugeMeta.setLore(Arrays.asList(
            ChatColor.GRAY + "Контроль всех центрифуг",
            ChatColor.YELLOW + "Нажмите для открытия"
        ));
        centrifuge.setItemMeta(centrifugeMeta);
        gui.setItem(20, centrifuge);
        
        // Радиация
        ItemStack radiation = new ItemStack(Material.REDSTONE);
        ItemMeta radiationMeta = radiation.getItemMeta();
        radiationMeta.setDisplayName(ChatColor.RED + "Мониторинг радиации");
        radiationMeta.setLore(Arrays.asList(
            ChatColor.GRAY + "Текущий уровень радиации: " + 
                plugin.getRadiationManager().getRadiation(player.getUniqueId()),
            ChatColor.YELLOW + "Нажмите для подробностей"
        ));
        radiation.setItemMeta(radiationMeta);
        gui.setItem(22, radiation);
        
        // Телепорты
        ItemStack teleporter = new ItemStack(Material.BEACON);
        ItemMeta teleporterMeta = teleporter.getItemMeta();
        teleporterMeta.setDisplayName(ChatColor.LIGHT_PURPLE + "Система телепортации");
        teleporterMeta.setLore(Arrays.asList(
            ChatColor.GRAY + "Управление телепортами",
            ChatColor.YELLOW + "Нажмите для открытия"
        ));
        teleporter.setItemMeta(teleporterMeta);
        gui.setItem(24, teleporter);
        
        // Лаборатория
        ItemStack laboratory = new ItemStack(Material.ENCHANTING_TABLE);
        ItemMeta laboratoryMeta = laboratory.getItemMeta();
        laboratoryMeta.setDisplayName(ChatColor.LIGHT_PURPLE + "Терминал лаборатории");
        
        List<LaboratoryTerminal> terminals = plugin.getLaboratoryManager().getPlayerTerminals(player.getUniqueId());
        if (terminals.isEmpty()) {
            laboratoryMeta.setLore(Arrays.asList(
                ChatColor.GRAY + "У вас нет терминалов лаборатории",
                ChatColor.YELLOW + "Создайте терминал для исследований"
            ));
        } else {
            LaboratoryTerminal terminal = terminals.get(0);
            laboratoryMeta.setLore(Arrays.asList(
                ChatColor.GRAY + "Статус: " + (terminal.isActive() ? 
                    ChatColor.GREEN + "Активен" : ChatColor.RED + "Неактивен"),
                ChatColor.GRAY + "Завершённых исследований: " + terminal.getCompletedResearch().size(),
                ChatColor.YELLOW + "Нажмите для открытия"
            ));
        }
        laboratory.setItemMeta(laboratoryMeta);
        gui.setItem(26, laboratory);
        
        // Статистика
        ItemStack stats = new ItemStack(Material.BOOK);
        ItemMeta statsMeta = stats.getItemMeta();
        statsMeta.setDisplayName(ChatColor.GOLD + "Статистика игрока");
        statsMeta.setLore(Arrays.asList(
            ChatColor.GRAY + "Ваши достижения и прогресс",
            ChatColor.YELLOW + "Нажмите для просмотра"
        ));
        stats.setItemMeta(statsMeta);
        gui.setItem(40, stats);
        
        // Заполняем пустые слоты
        fillEmptySlots(gui);
        
        player.openInventory(gui);
    }
    
    public static void openQuickStatus(Player player, UraniumPlugin plugin) {
        player.sendMessage(ChatColor.AQUA + "⚡ Быстрый статус ⚡");
        player.sendMessage(ChatColor.WHITE + "Радиация: " + ChatColor.RED + 
            plugin.getRadiationManager().getRadiation(player.getUniqueId()));
        
        List<LaboratoryTerminal> terminals = plugin.getLaboratoryManager().getPlayerTerminals(player.getUniqueId());
        if (!terminals.isEmpty()) {
            LaboratoryTerminal terminal = terminals.get(0);
            player.sendMessage(ChatColor.WHITE + "Лаборатория: " + 
                (terminal.isActive() ? ChatColor.GREEN + "Активна" : ChatColor.RED + "Неактивна"));
            player.sendMessage(ChatColor.WHITE + "Исследований завершено: " + ChatColor.YELLOW + 
                terminal.getCompletedResearch().size());
        }
        
        int centrifuges = plugin.getCentrifugeManager().getPlayerCentrifuges(player.getUniqueId()).size();
        player.sendMessage(ChatColor.WHITE + "Центрифуг: " + ChatColor.AQUA + centrifuges);
        
        int teleporters = plugin.getTeleporterManager().getPlayerTeleporters(player.getUniqueId()).size();
        player.sendMessage(ChatColor.WHITE + "Телепортов: " + ChatColor.LIGHT_PURPLE + teleporters);
    }
    
    public static void openMainInterface(Player player, UraniumPlugin plugin) {
        ItemStack tablet = getPlayerTablet(player);
        UraniumTablet.TabletType type = UraniumTablet.getTabletType(tablet, plugin);
        
        Component title = Component.text()
            .append(Component.text("⚡ ", NamedTextColor.YELLOW))
            .append(Component.text("Урановый планшет", type != null ? type.getColor() : NamedTextColor.AQUA, TextDecoration.BOLD))
            .append(Component.text(" ⚡", NamedTextColor.YELLOW))
            .build();
        
        Inventory gui = Bukkit.createInventory(null, 54, title);
        
        // Заполняем фон
        fillBackground(gui);
        
        // Информационная панель (верх)
        addInformationPanel(gui, player, plugin, tablet);
        
        // Основные функции
        addMainFunctions(gui, player, plugin, tablet);
        
        // Продвинутые функции
        if (type != null && type.ordinal() >= UraniumTablet.TabletType.ADVANCED.ordinal()) {
            addAdvancedFunctions(gui, player, plugin, tablet);
        }
        
        // Квантовые функции
        if (type != null && type.ordinal() >= UraniumTablet.TabletType.QUANTUM.ordinal()) {
            addQuantumFunctions(gui, player, plugin, tablet);
        }
        
        // Функции реальности
        if (type == UraniumTablet.TabletType.REALITY) {
            addRealityFunctions(gui, player, plugin, tablet);
        }
        
        // Системные функции
        addSystemFunctions(gui, player, plugin);
        
        player.openInventory(gui);
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.8f, 1.2f);
    }
    
    private static void addInformationPanel(Inventory gui, Player player, UraniumPlugin plugin, ItemStack tablet) {
        // Информация о игроке
        ItemStack playerInfo = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta playerMeta = playerInfo.getItemMeta();
        
        playerMeta.displayName(Component.text("👤 " + player.getName(), NamedTextColor.AQUA, TextDecoration.BOLD));
        
        int radiation = plugin.getRadiationManager().getRadiation(player);
        String world = player.getWorld().getName();
        int x = player.getLocation().getBlockX();
        int y = player.getLocation().getBlockY();
        int z = player.getLocation().getBlockZ();
        
        playerMeta.lore(Arrays.asList(
            Component.empty(),
            Component.text("☢️ Радиация: " + radiation + " рад", getRadiationColor(radiation)),
            Component.text("🌍 Мир: " + world, NamedTextColor.GREEN),
            Component.text("📍 Координаты: " + x + ", " + y + ", " + z, NamedTextColor.GRAY),
            Component.text("⏰ Время: " + getFormattedTime(player.getWorld().getTime()), NamedTextColor.YELLOW),
            Component.empty(),
            Component.text("Статус: " + getPlayerStatus(radiation), getRadiationColor(radiation))
        ));
        
        playerInfo.setItemMeta(playerMeta);
        gui.setItem(4, playerInfo);
        
        // Энергия планшета
        if (tablet != null) {
            UraniumTablet.TabletType type = UraniumTablet.getTabletType(tablet, plugin);
            int energy = UraniumTablet.getTabletEnergy(tablet, plugin);
            int maxEnergy = type != null ? type.getMaxEnergy() : 1000;
            
            gui.setItem(22, createEnergyDisplay(energy, maxEnergy, type));
        }
    }
    
    private static void addMainFunctions(Inventory gui, Player player, UraniumPlugin plugin, ItemStack tablet) {
        // Центрифуги
        gui.setItem(20, createFunctionItem(Material.BLAST_FURNACE, "⚙️ Центрифуги", 
            "Контроль процессов обогащения", "Производство урановых слитков", 12));
        
        // Радиация
        gui.setItem(21, createFunctionItem(Material.REDSTONE, "☢️ Радиация", 
            "Мониторинг радиационной обстановки", "Защита и детоксикация", 8));
        
        // Телепортация
        gui.setItem(23, createFunctionItem(Material.ENDER_PEARL, "🌀 Телепортация", 
            "Система квантовой телепортации", "Быстрое перемещение", 25));
        
        // Статистика
        gui.setItem(24, createFunctionItem(Material.BOOK, "📊 Статистика", 
            "Детальная статистика игрока", "Прогресс и достижения", 5));
    }
    
    private static void addAdvancedFunctions(Inventory gui, Player player, UraniumPlugin plugin, ItemStack tablet) {
        // Пока пусто - только базовые функции
    }
    
    private static void addQuantumFunctions(Inventory gui, Player player, UraniumPlugin plugin, ItemStack tablet) {
        // Временные аномалии
        gui.setItem(32, createFunctionItem(Material.CLOCK, "⏰ Временные аномалии", 
            "Манипуляции с течением времени", "Ускорение и замедление", 150));
        
        // Материализация материи
        gui.setItem(33, createFunctionItem(Material.NETHER_STAR, "✨ Материализация", 
            "Создание материи из энергии", "Квантовая алхимия", 200));
        
        // Квантовые порталы
        gui.setItem(34, createFunctionItem(Material.END_PORTAL_FRAME, "🌈 Квантовые порталы", 
            "Порталы в квантовые измерения", "Межмерные переходы", 300));
    }
    
    private static void addRealityFunctions(Inventory gui, Player player, UraniumPlugin plugin, ItemStack tablet) {
        // Контроль реальности
        gui.setItem(37, createFunctionItem(Material.COMMAND_BLOCK, "🌌 Контроль реальности", 
            "Манипуляция пространством-временем", "Изменение законов физики", 500));
        
        // Межмерные порталы
        gui.setItem(38, createFunctionItem(Material.END_GATEWAY, "🌀 Межмерные порталы", 
            "Доступ к параллельным вселенным", "Путешествия между мирами", 750));
        
        // Якорь реальности
        gui.setItem(39, createFunctionItem(Material.BEACON, "⚓ Якорь реальности", 
            "Стабилизация пространства-времени", "Защита от аномалий", 1000));
    }
    
    private static void addSystemFunctions(Inventory gui, Player player, UraniumPlugin plugin) {
        // Модули планшета
        gui.setItem(46, createFunctionItem(Material.REDSTONE_TORCH, "🔧 Модули", 
            "Управление модулями планшета", "Улучшения и настройки", 10));
        
        // Настройки
        gui.setItem(47, createFunctionItem(Material.COMPARATOR, "⚙️ Настройки", 
            "Конфигурация планшета", "Персонализация интерфейса", 5));
        
        // Справка
        gui.setItem(48, createFunctionItem(Material.WRITABLE_BOOK, "📖 Справка", 
            "Руководство пользователя", "Помощь и инструкции", 0));
        
        // Обновления
        gui.setItem(50, createFunctionItem(Material.EXPERIENCE_BOTTLE, "🔄 Обновления", 
            "Система обновлений планшета", "Новые функции и исправления", 20));
        
        // Диагностика
        gui.setItem(51, createFunctionItem(Material.REDSTONE_LAMP, "🔧 Диагностика", 
            "Диагностика систем планшета", "Проверка работоспособности", 15));
        
        // Закрыть
        gui.setItem(53, createFunctionItem(Material.BARRIER, "❌ Закрыть", 
            "Закрыть интерфейс планшета", "Возврат в игру", 0));
    }
    
    public static void openModulesInterface(Player player, UraniumPlugin plugin) {
        Component title = Component.text("🔧 Модули планшета", NamedTextColor.GOLD, TextDecoration.BOLD);
        Inventory gui = Bukkit.createInventory(null, 54, title);
        
        fillBackground(gui);
        
        ItemStack tablet = getPlayerTablet(player);
        Set<UraniumTablet.TabletModule> installedModules = UraniumTablet.getTabletModules(tablet, plugin);
        
        // Доступные модули
        int slot = 10;
        for (UraniumTablet.TabletModule module : UraniumTablet.TabletModule.values()) {
            boolean installed = installedModules.contains(module);
            Material material = installed ? Material.LIME_CONCRETE : Material.RED_CONCRETE;
            
            ItemStack moduleItem = new ItemStack(material);
            ItemMeta meta = moduleItem.getItemMeta();
            
            Component name = Component.text(module.getName(), 
                installed ? NamedTextColor.GREEN : NamedTextColor.RED, TextDecoration.BOLD);
            meta.displayName(name);
            
            meta.lore(Arrays.asList(
                Component.empty(),
                Component.text(module.getDescription(), NamedTextColor.GRAY),
                Component.empty(),
                Component.text("Энергопотребление: " + module.getEnergyCost(), NamedTextColor.YELLOW),
                Component.text("Статус: " + (installed ? "Установлен" : "Не установлен"), 
                    installed ? NamedTextColor.GREEN : NamedTextColor.RED),
                Component.empty(),
                installed ? 
                    Component.text("▶ Нажмите для удаления", NamedTextColor.RED) :
                    Component.text("▶ Нажмите для установки", NamedTextColor.GREEN)
            ));
            
            moduleItem.setItemMeta(meta);
            gui.setItem(slot, moduleItem);
            
            slot++;
            if (slot == 17) slot = 19;
            if (slot == 26) slot = 28;
            if (slot == 35) slot = 37;
        }
        
        // Назад
        gui.setItem(49, createFunctionItem(Material.ARROW, "← Назад", 
            "Вернуться в главное меню", "", 0));
        
        player.openInventory(gui);
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.8f, 1.0f);
    }
    
    public static void openDiagnosticsInterface(Player player, UraniumPlugin plugin) {
        Component title = Component.text("🔧 Диагностика планшета", NamedTextColor.AQUA, TextDecoration.BOLD);
        Inventory gui = Bukkit.createInventory(null, 45, title);
        
        fillBackground(gui);
        
        ItemStack tablet = getPlayerTablet(player);
        UraniumTablet.TabletType type = UraniumTablet.getTabletType(tablet, plugin);
        int energy = UraniumTablet.getTabletEnergy(tablet, plugin);
        Set<UraniumTablet.TabletModule> modules = UraniumTablet.getTabletModules(tablet, plugin);
        
        // Общее состояние
        gui.setItem(13, createDiagnosticItem(Material.EMERALD_BLOCK, "✅ Общее состояние", 
            "Планшет функционирует нормально", "Все системы работают стабильно"));
        
        // Энергосистема
        double energyPercent = type != null ? (double) energy / type.getMaxEnergy() * 100 : 0;
        Material energyMaterial = energyPercent > 50 ? Material.EMERALD_BLOCK : 
                                 energyPercent > 25 ? Material.GOLD_BLOCK : Material.REDSTONE_BLOCK;
        String energyStatus = energyPercent > 50 ? "Отличное" : 
                             energyPercent > 25 ? "Удовлетворительное" : "Требует внимания";
        
        gui.setItem(20, createDiagnosticItem(energyMaterial, "🔋 Энергосистема", 
            "Состояние: " + energyStatus, String.format("Заряд: %.1f%%", energyPercent)));
        
        // Модули
        gui.setItem(22, createDiagnosticItem(Material.REDSTONE_TORCH, "🔧 Модули", 
            "Установлено модулей: " + modules.size(), "Все модули работают корректно"));
        
        // Квантовые системы
        if (type != null && type.ordinal() >= UraniumTablet.TabletType.QUANTUM.ordinal()) {
            gui.setItem(24, createDiagnosticItem(Material.END_CRYSTAL, "⚛️ Квантовые системы", 
                "Квантовая стабильность: 98.7%", "Флуктуации в пределах нормы"));
        }
        
        // Тесты производительности
        gui.setItem(29, createFunctionItem(Material.CLOCK, "⏱️ Тест производительности", 
            "Запустить полную диагностику", "Проверка всех систем", 50));
        
        // Калибровка
        gui.setItem(31, createFunctionItem(Material.COMPASS, "🎯 Калибровка", 
            "Калибровка квантовых систем", "Оптимизация производительности", 100));
        
        // Сброс настроек
        gui.setItem(33, createFunctionItem(Material.TNT, "🔄 Сброс настроек", 
            "Сброс к заводским настройкам", "⚠️ Осторожно! Необратимо", 0));
        
        // Назад
        gui.setItem(40, createFunctionItem(Material.ARROW, "← Назад", 
            "Вернуться в главное меню", "", 0));
        
        player.openInventory(gui);
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.2f);
    }
    
    private static ItemStack getPlayerTablet(Player player) {
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        if (UraniumTablet.isUraniumTablet(mainHand, null)) {
            return mainHand;
        }
        
        ItemStack offHand = player.getInventory().getItemInOffHand();
        if (UraniumTablet.isUraniumTablet(offHand, null)) {
            return offHand;
        }
        
        return null;
    }
    
    private static void fillBackground(Inventory gui) {
        ItemStack glass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta glassMeta = glass.getItemMeta();
        glassMeta.displayName(Component.text(" "));
        glass.setItemMeta(glassMeta);
        
        // Заполняем края и пустые слоты
        for (int i = 0; i < gui.getSize(); i++) {
            if (gui.getItem(i) == null) {
                // Края
                if (i < 9 || i >= gui.getSize() - 9 || i % 9 == 0 || i % 9 == 8) {
                    gui.setItem(i, glass);
                }
            }
        }
    }
    
    private static void fillEmptySlots(Inventory gui) {
        ItemStack filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta fillerMeta = filler.getItemMeta();
        fillerMeta.setDisplayName(" ");
        filler.setItemMeta(fillerMeta);
        
        for (int i = 0; i < gui.getSize(); i++) {
            if (gui.getItem(i) == null) {
                gui.setItem(i, filler);
            }
        }
    }
    
    private static ItemStack createFunctionItem(Material material, String name, String description, String details, int energyCost) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        
        meta.displayName(Component.text(name, NamedTextColor.WHITE, TextDecoration.BOLD)
            .decoration(TextDecoration.ITALIC, false));
        
        List<Component> lore = Arrays.asList(
            Component.empty(),
            Component.text(description, NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false),
            details.isEmpty() ? Component.empty() : Component.text(details, NamedTextColor.DARK_GRAY).decoration(TextDecoration.ITALIC, false),
            Component.empty(),
            energyCost > 0 ? 
                Component.text("⚡ Энергия: " + energyCost, NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false) :
                Component.text("🆓 Бесплатно", NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, false),
            Component.empty(),
            Component.text("▶ Нажмите для использования", NamedTextColor.AQUA).decoration(TextDecoration.ITALIC, false)
        );
        
        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }
    
    private static ItemStack createDiagnosticItem(Material material, String name, String status, String details) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        
        meta.displayName(Component.text(name, NamedTextColor.WHITE, TextDecoration.BOLD)
            .decoration(TextDecoration.ITALIC, false));
        
        meta.lore(Arrays.asList(
            Component.empty(),
            Component.text(status, NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, false),
            Component.text(details, NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false),
            Component.empty(),
            Component.text("📊 Диагностическая информация", NamedTextColor.AQUA).decoration(TextDecoration.ITALIC, false)
        ));
        
        item.setItemMeta(meta);
        return item;
    }
    
    private static ItemStack createEnergyDisplay(int current, int max, UraniumTablet.TabletType type) {
        double percent = (double) current / max * 100;
        Material material = percent > 75 ? Material.EMERALD_BLOCK :
                           percent > 50 ? Material.GOLD_BLOCK :
                           percent > 25 ? Material.IRON_BLOCK : Material.REDSTONE_BLOCK;
        
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        
        NamedTextColor color = percent > 75 ? NamedTextColor.GREEN :
                              percent > 50 ? NamedTextColor.YELLOW :
                              percent > 25 ? NamedTextColor.GOLD : NamedTextColor.RED;
        
        meta.displayName(Component.text("🔋 Энергия планшета", color, TextDecoration.BOLD)
            .decoration(TextDecoration.ITALIC, false));
        
        String energyBar = createEnergyBar(percent);
        String regenRate = type != null ? String.valueOf(UraniumTablet.getEnergyRegenRate(type, Set.of())) : "10";
        
        meta.lore(Arrays.asList(
            Component.empty(),
            Component.text("Текущий заряд:", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false),
            Component.text(energyBar + " " + String.format("%.1f%%", percent), color).decoration(TextDecoration.ITALIC, false),
            Component.text(current + " / " + max + " единиц", NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false),
            Component.empty(),
            Component.text("🔄 Регенерация: +" + regenRate + "/30сек", NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, false),
            Component.text("⚡ Тип планшета: " + (type != null ? type.getDisplayName() : "Неизвестно"), 
                type != null ? type.getColor() : NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false)
        ));
        
        item.setItemMeta(meta);
        return item;
    }
    
    private static String createEnergyBar(double percent) {
        int bars = (int) (percent / 5);
        StringBuilder bar = new StringBuilder();
        
        for (int i = 0; i < 20; i++) {
            if (i < bars) {
                bar.append("█");
            } else {
                bar.append("░");
            }
        }
        
        return bar.toString();
    }
    
    private static NamedTextColor getRadiationColor(int radiation) {
        if (radiation >= 80) return NamedTextColor.DARK_RED;
        if (radiation >= 60) return NamedTextColor.RED;
        if (radiation >= 40) return NamedTextColor.GOLD;
        if (radiation >= 20) return NamedTextColor.YELLOW;
        return NamedTextColor.GREEN;
    }
    
    private static String getPlayerStatus(int radiation) {
        if (radiation >= 80) return "КРИТИЧЕСКОЕ СОСТОЯНИЕ";
        if (radiation >= 60) return "Опасное облучение";
        if (radiation >= 40) return "Повышенная радиация";
        if (radiation >= 20) return "Легкое облучение";
        return "Здоров";
    }
    
    private static String getFormattedTime(long worldTime) {
        long hours = (worldTime / 1000 + 6) % 24;
        long minutes = (worldTime % 1000) * 60 / 1000;
        return String.format("%02d:%02d", hours, minutes);
    }
}
