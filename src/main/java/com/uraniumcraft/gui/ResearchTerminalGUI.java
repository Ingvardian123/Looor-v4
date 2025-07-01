package com.uraniumcraft.gui;

import com.uraniumcraft.items.AdvancedItems;
import com.uraniumcraft.items.AdvancedResearchItems;
import com.uraniumcraft.items.UraniumItems;
import com.uraniumcraft.laboratory.EnhancedLaboratory;
import com.uraniumcraft.laboratory.ResearchProject;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

public class ResearchTerminalGUI {
    
    private static final Map<String, ResearchProject> RESEARCH_DATABASE = new HashMap<>();
    
    static {
        initializeResearchDatabase();
    }
    
    public static void openMainTerminal(Player player, EnhancedLaboratory laboratory) {
        Inventory gui = Bukkit.createInventory(null, 54, 
            ChatColor.DARK_BLUE + "🔬 Исследовательский терминал");
        
        // Заголовок терминала
        ItemStack header = new ItemStack(Material.BEACON);
        ItemMeta headerMeta = header.getItemMeta();
        headerMeta.setDisplayName(ChatColor.AQUA + "⚡ ИССЛЕДОВАТЕЛЬСКИЙ ТЕРМИНАЛ ⚡");
        headerMeta.setLore(Arrays.asList(
            ChatColor.GRAY + "Система управления исследованиями",
            ChatColor.GRAY + "Версия: 3.0.1 | Статус: " + ChatColor.GREEN + "ОНЛАЙН",
            "",
            ChatColor.YELLOW + "Завершено: " + ChatColor.GREEN + laboratory.getCompletedResearch().size() + ChatColor.GRAY + " исследований",
            ChatColor.YELLOW + "Активно: " + ChatColor.GOLD + laboratory.getActiveResearch().size() + ChatColor.GRAY + " проектов",
            ChatColor.YELLOW + "Энергия: " + getEnergyBar(laboratory) + ChatColor.GRAY + " (" + laboratory.getCurrentEnergy() + "/" + laboratory.getMaxEnergy() + ")"
        ));
        header.setItemMeta(headerMeta);
        gui.setItem(4, header);
        
        // Навигационные кнопки
        setupNavigationButtons(gui, laboratory);
        
        // Активные исследования
        setupActiveResearchDisplay(gui, laboratory);
        
        // Быстрый доступ к категориям
        setupQuickAccessCategories(gui, laboratory);
        
        // Статус лаборатории
        setupLaboratoryStatus(gui, laboratory);
        
        // Заполняем пустые слоты
        fillTerminalBackground(gui);
        
        player.openInventory(gui);
        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 0.8f, 1.5f);
    }
    
    public static void openResearchCategory(Player player, EnhancedLaboratory laboratory, String category) {
        Inventory gui = Bukkit.createInventory(null, 54, 
            ChatColor.DARK_PURPLE + "📁 " + category);
        
        // Заголовок категории
        ItemStack categoryHeader = getCategoryIcon(category);
        ItemMeta categoryMeta = categoryHeader.getItemMeta();
        categoryMeta.setDisplayName(ChatColor.GOLD + "📁 " + category.toUpperCase());
        categoryMeta.setLore(Arrays.asList(
            ChatColor.GRAY + "Доступные исследования в категории",
            ChatColor.GRAY + "Выберите проект для начала исследования",
            "",
            ChatColor.YELLOW + "💡 Подсказка: Принесите необходимые материалы",
            ChatColor.YELLOW + "   и поместите их в слоты терминала"
        ));
        categoryHeader.setItemMeta(categoryMeta);
        gui.setItem(4, categoryHeader);
        
        // Кнопка назад
        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta backMeta = back.getItemMeta();
        backMeta.setDisplayName(ChatColor.YELLOW + "← Вернуться к главному меню");
        backMeta.setLore(Arrays.asList(ChatColor.GRAY + "Нажмите для возврата"));
        back.setItemMeta(backMeta);
        gui.setItem(45, back);
        
        // Исследования категории
        setupCategoryResearch(gui, laboratory, category);
        
        fillTerminalBackground(gui);
        player.openInventory(gui);
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.6f, 1.2f);
    }
    
    public static void openResearchDetails(Player player, EnhancedLaboratory laboratory, String researchId) {
        ResearchProject project = RESEARCH_DATABASE.get(researchId);
        if (project == null) return;
        
        Inventory gui = Bukkit.createInventory(null, 54, 
            ChatColor.DARK_GREEN + "🔬 " + project.getName());
        
        // Детали исследования
        ItemStack details = new ItemStack(project.getIcon());
        ItemMeta detailsMeta = details.getItemMeta();
        detailsMeta.setDisplayName(ChatColor.AQUA + "🔬 " + project.getName());
        
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + project.getDescription());
        lore.add("");
        lore.add(ChatColor.YELLOW + "⏱️ Время исследования: " + ChatColor.WHITE + formatTime(project.getDuration()));
        lore.add(ChatColor.YELLOW + "⚡ Потребление энергии: " + ChatColor.WHITE + project.getEnergyRequired() + "/сек");
        lore.add("");
        
        if (laboratory.isResearchCompleted(researchId)) {
            lore.add(ChatColor.GREEN + "✅ ИССЛЕДОВАНИЕ ЗАВЕРШЕНО");
        } else if (laboratory.getActiveResearch().containsKey(researchId)) {
            lore.add(ChatColor.GOLD + "🔄 ИССЛЕДОВАНИЕ В ПРОЦЕССЕ");
            var activeProject = laboratory.getActiveResearch().get(researchId);
            lore.add(ChatColor.GRAY + "Прогресс: " + String.format("%.1f", activeProject.getProgress()) + "%");
            lore.add(ChatColor.GRAY + "Осталось: " + activeProject.getEstimatedTimeString());
        } else if (canStartResearch(laboratory, project)) {
            lore.add(ChatColor.GREEN + "✅ ГОТОВО К ЗАПУСКУ");
            lore.add(ChatColor.YELLOW + "Поместите материалы в слоты ниже");
        } else {
            lore.add(ChatColor.RED + "❌ ТРЕБОВАНИЯ НЕ ВЫПОЛНЕНЫ");
        }
        
        detailsMeta.setLore(lore);
        details.setItemMeta(detailsMeta);
        gui.setItem(4, details);
        
        // Слоты для материалов (3x3 сетка)
        setupMaterialSlots(gui, project);
        
        // Кнопка запуска
        if (!laboratory.isResearchCompleted(researchId) && 
            !laboratory.getActiveResearch().containsKey(researchId)) {
            ItemStack startButton = new ItemStack(Material.LIME_CONCRETE);
            ItemMeta startMeta = startButton.getItemMeta();
            startMeta.setDisplayName(ChatColor.GREEN + "🚀 НАЧАТЬ ИССЛЕДОВАНИЕ");
            startMeta.setLore(Arrays.asList(
                ChatColor.GRAY + "Поместите все необходимые материалы",
                ChatColor.GRAY + "в слоты выше и нажмите эту кнопку",
                "",
                canStartResearch(laboratory, project) ? 
                    ChatColor.GREEN + "Готово к запуску!" : 
                    ChatColor.RED + "Не хватает материалов или энергии"
            ));
            startButton.setItemMeta(startMeta);
            gui.setItem(49, startButton);
        }
        
        // Кнопка назад
        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta backMeta = back.getItemMeta();
        backMeta.setDisplayName(ChatColor.YELLOW + "← Назад к категории");
        back.setItemMeta(backMeta);
        gui.setItem(45, back);
        
        fillTerminalBackground(gui);
        player.openInventory(gui);
        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 0.7f, 1.3f);
    }
    
    private static void setupNavigationButtons(Inventory gui, EnhancedLaboratory laboratory) {
        // Кнопка обновления
        ItemStack refresh = new ItemStack(Material.RECOVERY_COMPASS);
        ItemMeta refreshMeta = refresh.getItemMeta();
        refreshMeta.setDisplayName(ChatColor.AQUA + "🔄 Обновить данные");
        refreshMeta.setLore(Arrays.asList(
            ChatColor.GRAY + "Обновить информацию о терминале",
            ChatColor.YELLOW + "Нажмите для обновления"
        ));
        refresh.setItemMeta(refreshMeta);
        gui.setItem(8, refresh);
        
        // Настройки лаборатории
        ItemStack settings = new ItemStack(Material.COMPARATOR);
        ItemMeta settingsMeta = settings.getItemMeta();
        settingsMeta.setDisplayName(ChatColor.GOLD + "⚙️ Настройки лаборатории");
        settingsMeta.setLore(Arrays.asList(
            ChatColor.GRAY + "Управление параметрами лаборатории",
            ChatColor.GRAY + "Автоматизация, специализация",
            ChatColor.YELLOW + "Нажмите для открытия"
        ));
        settings.setItemMeta(settingsMeta);
        gui.setItem(0, settings);
    }
    
    private static void setupActiveResearchDisplay(Inventory gui, EnhancedLaboratory laboratory) {
        int slot = 19;
        for (Map.Entry<String, com.uraniumcraft.laboratory.EnhancedResearchProject> entry : 
             laboratory.getActiveResearch().entrySet()) {
            if (slot > 25) break;
            
            ItemStack research = new ItemStack(Material.CLOCK);
            ItemMeta meta = research.getItemMeta();
            meta.setDisplayName(ChatColor.GOLD + "🔄 " + entry.getKey());
            
            double progress = entry.getValue().getProgress();
            String progressBar = createProgressBar(progress);
            
            meta.setLore(Arrays.asList(
                ChatColor.GRAY + "Исследование в процессе",
                "",
                ChatColor.YELLOW + "Прогресс: " + progressBar,
                ChatColor.WHITE + String.format("%.1f", progress) + "%",
                ChatColor.YELLOW + "Осталось: " + ChatColor.WHITE + entry.getValue().getEstimatedTimeString(),
                "",
                ChatColor.GREEN + "Нажмите для подробностей"
            ));
            research.setItemMeta(meta);
            gui.setItem(slot++, research);
        }
        
        // Если нет активных исследований
        if (laboratory.getActiveResearch().isEmpty()) {
            ItemStack noResearch = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
            ItemMeta noMeta = noResearch.getItemMeta();
            noMeta.setDisplayName(ChatColor.GRAY + "Нет активных исследований");
            noMeta.setLore(Arrays.asList(
                ChatColor.DARK_GRAY + "Выберите исследование из категорий ниже"
            ));
            noResearch.setItemMeta(noMeta);
            gui.setItem(22, noResearch);
        }
    }
    
    private static void setupQuickAccessCategories(Inventory gui, EnhancedLaboratory laboratory) {
        // Базовые технологии
        ItemStack basic = new ItemStack(Material.CRAFTING_TABLE);
        ItemMeta basicMeta = basic.getItemMeta();
        basicMeta.setDisplayName(ChatColor.GREEN + "📁 Базовые технологии");
        basicMeta.setLore(Arrays.asList(
            ChatColor.GRAY + "Основы урановых технологий",
            ChatColor.GRAY + "• Детекторы радиации",
            ChatColor.GRAY + "• Защитные костюмы",
            ChatColor.GRAY + "• Базовые инструменты",
            "",
            ChatColor.YELLOW + "Нажмите для просмотра"
        ));
        basic.setItemMeta(basicMeta);
        gui.setItem(37, basic);
        
        // Энергетические системы
        ItemStack energy = new ItemStack(Material.REDSTONE_BLOCK);
        ItemMeta energyMeta = energy.getItemMeta();
        energyMeta.setDisplayName(ChatColor.RED + "📁 Энергетические системы");
        energyMeta.setLore(Arrays.asList(
            ChatColor.GRAY + "Генерация и управление энергией",
            ChatColor.GRAY + "• Квантовые реакторы",
            ChatColor.GRAY + "• Энергетические усилители",
            ChatColor.GRAY + "• Аккумуляторы",
            "",
            ChatColor.YELLOW + "Нажмите для просмотра"
        ));
        energy.setItemMeta(energyMeta);
        gui.setItem(38, energy);
        
        // Медицинские технологии
        ItemStack medical = new ItemStack(Material.BEACON);
        ItemMeta medicalMeta = medical.getItemMeta();
        medicalMeta.setDisplayName(ChatColor.GREEN + "📁 Медицинские технологии");
        medicalMeta.setLore(Arrays.asList(
            ChatColor.GRAY + "Лечение и защита от радиации",
            ChatColor.GRAY + "• Лечебные камеры",
            ChatColor.GRAY + "• Нейтрализаторы радиации",
            ChatColor.GRAY + "• Био-сканеры",
            "",
            ChatColor.YELLOW + "Нажмите для просмотра"
        ));
        medical.setItemMeta(medicalMeta);
        gui.setItem(39, medical);
        
        // Квантовые технологии
        ItemStack quantum = new ItemStack(Material.NETHER_STAR);
        ItemMeta quantumMeta = quantum.getItemMeta();
        quantumMeta.setDisplayName(ChatColor.LIGHT_PURPLE + "📁 Квантовые технологии");
        quantumMeta.setLore(Arrays.asList(
            ChatColor.GRAY + "Продвинутые квантовые системы",
            ChatColor.GRAY + "• Телепорты",
            ChatColor.GRAY + "• Квантовые процессоры",
            ChatColor.GRAY + "• Голографические дисплеи",
            "",
            ChatColor.YELLOW + "Нажмите для просмотра"
        ));
        quantum.setItemMeta(quantumMeta);
        gui.setItem(40, quantum);
        
        // Защитные системы
        ItemStack protection = new ItemStack(Material.NETHERITE_CHESTPLATE);
        ItemMeta protectionMeta = protection.getItemMeta();
        protectionMeta.setDisplayName(ChatColor.BLUE + "📁 Защитные системы");
        protectionMeta.setLore(Arrays.asList(
            ChatColor.GRAY + "Продвинутая защита и броня",
            ChatColor.GRAY + "• Силовая броня",
            ChatColor.GRAY + "• Защитные поля",
            ChatColor.GRAY + "• Системы очистки",
            "",
            ChatColor.YELLOW + "Нажмите для просмотра"
        ));
        protection.setItemMeta(protectionMeta);
        gui.setItem(41, protection);
        
        // Инструменты и утилиты
        ItemStack tools = new ItemStack(Material.RECOVERY_COMPASS);
        ItemMeta toolsMeta = tools.getItemMeta();
        toolsMeta.setDisplayName(ChatColor.GOLD + "📁 Инструменты и утилиты");
        toolsMeta.setLore(Arrays.asList(
            ChatColor.GRAY + "Специализированные инструменты",
            ChatColor.GRAY + "• Ремонтные наборы",
            ChatColor.GRAY + "• Сканеры материалов",
            ChatColor.GRAY + "• Автоматические системы",
            "",
            ChatColor.YELLOW + "Нажмите для просмотра"
        ));
        tools.setItemMeta(toolsMeta);
        gui.setItem(42, tools);
    }
    
    private static void setupLaboratoryStatus(Inventory gui, EnhancedLaboratory laboratory) {
        // Статус энергии
        ItemStack energyStatus = new ItemStack(Material.LIGHTNING_ROD);
        ItemMeta energyMeta = energyStatus.getItemMeta();
        energyMeta.setDisplayName(ChatColor.YELLOW + "⚡ Энергетический статус");
        energyMeta.setLore(Arrays.asList(
            ChatColor.GRAY + "Текущее состояние энергосистемы",
            "",
            ChatColor.YELLOW + "Энергия: " + ChatColor.WHITE + laboratory.getCurrentEnergy() + "/" + laboratory.getMaxEnergy(),
            ChatColor.YELLOW + "Генерация: " + ChatColor.GREEN + "+" + laboratory.getEnergyGeneration() + "/сек",
            ChatColor.YELLOW + "Потребление: " + ChatColor.RED + "-" + laboratory.getEnergyConsumption() + "/сек",
            "",
            getEnergyStatusColor(laboratory) + getEnergyStatusText(laboratory)
        ));
        energyStatus.setItemMeta(energyMeta);
        gui.setItem(46, energyStatus);
        
        // Статус хранилища
        ItemStack storageStatus = new ItemStack(Material.CHEST);
        ItemMeta storageMeta = storageStatus.getItemMeta();
        storageMeta.setDisplayName(ChatColor.AQUA + "📦 Статус хранилища");
        storageMeta.setLore(Arrays.asList(
            ChatColor.GRAY + "Использование хранилища лаборатории",
            "",
            ChatColor.YELLOW + "Занято: " + ChatColor.WHITE + laboratory.getStorageUsage() + "/" + laboratory.getMaxStorageCapacity(),
            ChatColor.YELLOW + "Свободно: " + ChatColor.GREEN + (laboratory.getMaxStorageCapacity() - laboratory.getStorageUsage()),
            "",
            getStorageStatusColor(laboratory) + getStorageStatusText(laboratory)
        ));
        storageStatus.setItemMeta(storageMeta);
        gui.setItem(47, storageStatus);
        
        // Уровень лаборатории
        ItemStack levelStatus = new ItemStack(Material.EXPERIENCE_BOTTLE);
        ItemMeta levelMeta = levelStatus.getItemMeta();
        levelMeta.setDisplayName(ChatColor.GOLD + "🏆 Уровень лаборатории");
        levelMeta.setLore(Arrays.asList(
            ChatColor.GRAY + "Текущий уровень развития",
            "",
            ChatColor.YELLOW + "Уровень: " + ChatColor.WHITE + laboratory.getLevel().getLevel() + "/5",
            ChatColor.YELLOW + "Специализация: " + ChatColor.WHITE + laboratory.getSpecialization().getName(),
            ChatColor.YELLOW + "Макс. исследований: " + ChatColor.WHITE + laboratory.getLevel().getMaxResearch(),
            "",
            laboratory.getLevel().getLevel() < 5 ? 
                ChatColor.GREEN + "Доступно улучшение" : 
                ChatColor.GOLD + "Максимальный уровень"
        ));
        levelStatus.setItemMeta(levelMeta);
        gui.setItem(48, levelStatus);
    }
    
    private static void setupCategoryResearch(Inventory gui, EnhancedLaboratory laboratory, String category) {
        int slot = 9;
        for (Map.Entry<String, ResearchProject> entry : RESEARCH_DATABASE.entrySet()) {
            if (!entry.getValue().getCategory().equals(category) || slot >= 45) continue;
            
            String researchId = entry.getKey();
            ResearchProject project = entry.getValue();
            
            ItemStack research = new ItemStack(project.getIcon());
            ItemMeta meta = research.getItemMeta();
            
            boolean completed = laboratory.isResearchCompleted(researchId);
            boolean active = laboratory.getActiveResearch().containsKey(researchId);
            boolean canStart = canStartResearch(laboratory, project);
            
            if (completed) {
                meta.setDisplayName(ChatColor.GREEN + "✅ " + project.getName());
            } else if (active) {
                meta.setDisplayName(ChatColor.GOLD + "🔄 " + project.getName());
            } else if (canStart) {
                meta.setDisplayName(ChatColor.YELLOW + "⭐ " + project.getName());
            } else {
                meta.setDisplayName(ChatColor.RED + "🔒 " + project.getName());
            }
            
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + project.getDescription());
            lore.add("");
            lore.add(ChatColor.YELLOW + "⏱️ Время: " + ChatColor.WHITE + formatTime(project.getDuration()));
            lore.add(ChatColor.YELLOW + "⚡ Энергия: " + ChatColor.WHITE + project.getEnergyRequired() + "/сек");
            lore.add("");
            
            if (completed) {
                lore.add(ChatColor.GREEN + "✅ Исследование завершено!");
            } else if (active) {
                var activeProject = laboratory.getActiveResearch().get(researchId);
                lore.add(ChatColor.GOLD + "🔄 В процессе исследования");
                lore.add(ChatColor.GRAY + "Прогресс: " + String.format("%.1f", activeProject.getProgress()) + "%");
            } else if (canStart) {
                lore.add(ChatColor.GREEN + "⭐ Готово к запуску");
                lore.add(ChatColor.YELLOW + "Нажмите для начала исследования");
            } else {
                lore.add(ChatColor.RED + "🔒 Требования не выполнены");
                if (!hasPrerequisites(laboratory, project)) {
                    lore.add(ChatColor.RED + "Нужны предварительные исследования");
                }
                if (!hasEnoughEnergy(laboratory, project)) {
                    lore.add(ChatColor.RED + "Недостаточно энергии");
                }
            }
            
            meta.setLore(lore);
            research.setItemMeta(meta);
            gui.setItem(slot++, research);
        }
    }
    
    private static void setupMaterialSlots(Inventory gui, ResearchProject project) {
        // Заголовок для материалов
        ItemStack materialsHeader = new ItemStack(Material.CHEST);
        ItemMeta headerMeta = materialsHeader.getItemMeta();
        headerMeta.setDisplayName(ChatColor.AQUA + "📦 Необходимые материалы");
        headerMeta.setLore(Arrays.asList(
            ChatColor.GRAY + "Поместите материалы в слоты ниже",
            ChatColor.YELLOW + "Все материалы должны быть размещены"
        ));
        materialsHeader.setItemMeta(headerMeta);
        gui.setItem(13, materialsHeader);
        
        // Слоты для материалов (3x3 сетка в центре)
        int[] materialSlots = {19, 20, 21, 28, 29, 30, 37, 38, 39};
        int slotIndex = 0;
        
        for (Map.Entry<Material, Integer> entry : project.getRequiredMaterials().entrySet()) {
            if (slotIndex >= materialSlots.length) break;
            
            ItemStack placeholder = new ItemStack(entry.getKey());
            ItemMeta placeholderMeta = placeholder.getItemMeta();
            placeholderMeta.setDisplayName(ChatColor.YELLOW + "Требуется: " + getMaterialDisplayName(entry.getKey()));
            placeholderMeta.setLore(Arrays.asList(
                ChatColor.GRAY + "Количество: " + ChatColor.WHITE + entry.getValue(),
                "",
                ChatColor.YELLOW + "Поместите материал в этот слот"
            ));
            placeholder.setItemMeta(placeholderMeta);
            gui.setItem(materialSlots[slotIndex++], placeholder);
        }
    }
    
    private static void fillTerminalBackground(Inventory gui) {
        ItemStack background = new ItemStack(Material.CYAN_STAINED_GLASS_PANE);
        ItemMeta backgroundMeta = background.getItemMeta();
        backgroundMeta.setDisplayName(" ");
        background.setItemMeta(backgroundMeta);
        
        for (int i = 0; i < gui.getSize(); i++) {
            if (gui.getItem(i) == null) {
                gui.setItem(i, background);
            }
        }
    }
    
    // Вспомогательные методы
    private static String getEnergyBar(EnhancedLaboratory laboratory) {
        double percentage = laboratory.getEnergyPercentage();
        int bars = (int) (percentage / 10);
        
        StringBuilder energyBar = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            if (i < bars) {
                energyBar.append(ChatColor.GREEN).append("█");
            } else {
                energyBar.append(ChatColor.GRAY).append("░");
            }
        }
        return energyBar.toString();
    }
    
    private static String createProgressBar(double progress) {
        int bars = (int) (progress / 5);
        StringBuilder progressBar = new StringBuilder();
        
        for (int i = 0; i < 20; i++) {
            if (i < bars) {
                progressBar.append(ChatColor.GREEN).append("█");
            } else {
                progressBar.append(ChatColor.GRAY).append("░");
            }
        }
        return progressBar.toString();
    }
    
    private static String formatTime(long milliseconds) {
        long seconds = milliseconds / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        
        if (hours > 0) {
            return hours + "ч " + (minutes % 60) + "м";
        } else if (minutes > 0) {
            return minutes + "м " + (seconds % 60) + "с";
        } else {
            return seconds + "с";
        }
    }
    
    private static ItemStack getCategoryIcon(String category) {
        switch (category) {
            case "Базовые технологии": return new ItemStack(Material.CRAFTING_TABLE);
            case "Энергетические системы": return new ItemStack(Material.REDSTONE_BLOCK);
            case "Медицинские технологии": return new ItemStack(Material.BEACON);
            case "Квантовые технологии": return new ItemStack(Material.NETHER_STAR);
            case "Защитные системы": return new ItemStack(Material.NETHERITE_CHESTPLATE);
            case "Инструменты и утилиты": return new ItemStack(Material.RECOVERY_COMPASS);
            default: return new ItemStack(Material.BOOK);
        }
    }
    
    private static boolean canStartResearch(EnhancedLaboratory laboratory, ResearchProject project) {
        return hasPrerequisites(laboratory, project) && 
               hasEnoughEnergy(laboratory, project) &&
               !laboratory.isResearchCompleted(project.getId()) &&
               !laboratory.getActiveResearch().containsKey(project.getId());
    }
    
    private static boolean hasPrerequisites(EnhancedLaboratory laboratory, ResearchProject project) {
        for (String prerequisite : project.getPrerequisites()) {
            if (!laboratory.isResearchCompleted(prerequisite)) {
                return false;
            }
        }
        return true;
    }
    
    private static boolean hasEnoughEnergy(EnhancedLaboratory laboratory, ResearchProject project) {
        return laboratory.getCurrentEnergy() >= project.getEnergyRequired() * 10; // 10 секунд запаса
    }
    
    private static String getEnergyStatusText(EnhancedLaboratory laboratory) {
        double percentage = laboratory.getEnergyPercentage();
        if (percentage > 80) return "Энергосистема работает оптимально";
        if (percentage > 50) return "Энергии достаточно для работы";
        if (percentage > 20) return "Низкий уровень энергии";
        return "Критически низкий уровень энергии!";
    }
    
    private static ChatColor getEnergyStatusColor(EnhancedLaboratory laboratory) {
        double percentage = laboratory.getEnergyPercentage();
        if (percentage > 80) return ChatColor.GREEN;
        if (percentage > 50) return ChatColor.YELLOW;
        if (percentage > 20) return ChatColor.GOLD;
        return ChatColor.RED;
    }
    
    private static String getStorageStatusText(EnhancedLaboratory laboratory) {
        double percentage = (double) laboratory.getStorageUsage() / laboratory.getMaxStorageCapacity() * 100;
        if (percentage < 50) return "Достаточно свободного места";
        if (percentage < 80) return "Хранилище заполняется";
        if (percentage < 95) return "Мало свободного места";
        return "Хранилище почти заполнено!";
    }
    
    private static ChatColor getStorageStatusColor(EnhancedLaboratory laboratory) {
        double percentage = (double) laboratory.getStorageUsage() / laboratory.getMaxStorageCapacity() * 100;
        if (percentage < 50) return ChatColor.GREEN;
        if (percentage < 80) return ChatColor.YELLOW;
        if (percentage < 95) return ChatColor.GOLD;
        return ChatColor.RED;
    }
    
    private static String getMaterialDisplayName(Material material) {
        switch (material) {
            case IRON_INGOT: return "Железные слитки";
            case REDSTONE: return "Красная пыль";
            case DIAMOND: return "Алмазы";
            case EMERALD: return "Изумруды";
            case NETHER_STAR: return "Звезда Нижнего мира";
            case BEACON: return "Маяк";
            case GOLD_INGOT: return "Золотые слитки";
            case QUARTZ: return "Кварц";
            case GLOWSTONE_DUST: return "Светопыль";
            case ENDER_PEARL: return "Жемчуг Края";
            default: return material.name().toLowerCase().replace("_", " ");
        }
    }
    
    private static void initializeResearchDatabase() {
        // Базовые технологии
        RESEARCH_DATABASE.put("radiation_detector", new ResearchProject(
            "radiation_detector", "Детектор радиации", "Базовые технологии",
            "Устройство для точного измерения уровня радиации в окружающей среде",
            Material.COMPASS, 15 * 60 * 1000, 5,
            Map.of(Material.IRON_INGOT, 8, Material.REDSTONE, 16, Material.QUARTZ, 4),
            List.of()
        ));
        
        RESEARCH_DATABASE.put("hazmat_suit", new ResearchProject(
            "hazmat_suit", "Защитный костюм", "Защитные системы",
            "Базовая защита от радиационного воздействия",
            Material.LEATHER_CHESTPLATE, 20 * 60 * 1000, 8,
            Map.of(Material.LEATHER, 16, Material.IRON_INGOT, 8, Material.REDSTONE, 8),
            List.of("radiation_detector")
        ));
        
        RESEARCH_DATABASE.put("anti_radiation_pills", new ResearchProject(
            "anti_radiation_pills", "Таблетки от радиации", "Медицинские технологии",
            "Медицинские препараты для снижения уровня радиации в организме",
            Material.SUGAR, 25 * 60 * 1000, 6,
            Map.of(Material.SUGAR, 16, Material.GOLDEN_APPLE, 2, Material.GLOWSTONE_DUST, 8),
            List.of("radiation_detector")
        ));
        
        // Энергетические системы
        RESEARCH_DATABASE.put("energy_amplifier", new ResearchProject(
            "energy_amplifier", "Энергетический усилитель", "Энергетические системы",
            "Устройство для увеличения эффективности энергетических процессов",
            Material.REDSTONE_BLOCK, 45 * 60 * 1000, 12,
            Map.of(Material.REDSTONE_BLOCK, 8, Material.IRON_BLOCK, 4, Material.DIAMOND, 4),
            List.of("hazmat_suit")
        ));
        
        RESEARCH_DATABASE.put("quantum_reactor", new ResearchProject(
            "quantum_reactor", "Квантовый реактор", "Энергетические системы",
            "Мощный источник энергии на основе квантовых процессов",
            Material.RESPAWN_ANCHOR, 90 * 60 * 1000, 20,
            Map.of(Material.NETHER_STAR, 2, Material.BEACON, 4, Material.OBSIDIAN, 32),
            List.of("energy_amplifier")
        ));
        
        // Медицинские технологии
        RESEARCH_DATABASE.put("healing_chamber", new ResearchProject(
            "healing_chamber", "Лечебная камера", "Медицинские технологии",
            "Автоматическая система восстановления здоровья игроков",
            Material.BEACON, 60 * 60 * 1000, 15,
            Map.of(Material.BEACON, 2, Material.DIAMOND, 8, Material.EMERALD, 4),
            List.of("anti_radiation_pills")
        ));
        
        RESEARCH_DATABASE.put("bio_scanner", new ResearchProject(
            "bio_scanner", "Био-сканер", "Медицинские технологии",
            "Устройство для анализа биологических параметров живых организмов",
            Material.SPYGLASS, 40 * 60 * 1000, 10,
            Map.of(Material.SPYGLASS, 2, Material.REDSTONE, 16, Material.IRON_INGOT, 8),
            List.of("healing_chamber")
        ));
        
        RESEARCH_DATABASE.put("radiation_neutralizer", new ResearchProject(
            "radiation_neutralizer", "Нейтрализатор радиации", "Медицинские технологии",
            "Система для очистки территории от радиационного загрязнения",
            Material.CONDUIT, 120 * 60 * 1000, 25,
            Map.of(Material.CONDUIT, 2, Material.PRISMARINE_CRYSTALS, 16, Material.BEACON, 2),
            List.of("bio_scanner")
        ));
        
        // Квантовые технологии
        RESEARCH_DATABASE.put("quantum_processor", new ResearchProject(
            "quantum_processor", "Квантовый процессор", "Квантовые технологии",
            "Продвинутый вычислительный блок на основе квантовых эффектов",
            Material.NETHER_STAR, 150 * 60 * 1000, 30,
            Map.of(Material.NETHER_STAR, 2, Material.DIAMOND_BLOCK, 4, Material.REDSTONE_BLOCK, 8),
            List.of("quantum_reactor")
        ));
        
        RESEARCH_DATABASE.put("teleporter", new ResearchProject(
            "teleporter", "Квантовый телепорт", "Квантовые технологии",
            "Система мгновенной телепортации на большие расстояния",
            Material.END_PORTAL_FRAME, 240 * 60 * 1000, 40,
            Map.of(Material.ENDER_PEARL, 32, Material.NETHER_STAR, 4, Material.OBSIDIAN, 16),
            List.of("quantum_processor")
        ));
        
        RESEARCH_DATABASE.put("holographic_display", new ResearchProject(
            "holographic_display", "Голографический дисплей", "Квантовые технологии",
            "Система отображения трёхмерной информации в виде голограмм",
            Material.TINTED_GLASS, 180 * 60 * 1000, 35,
            Map.of(Material.GLASS, 16, Material.GLOWSTONE, 8, Material.REDSTONE, 32),
            List.of("quantum_processor")
        ));
        
        // Защитные системы
        RESEARCH_DATABASE.put("advanced_hazmat_suit", new ResearchProject(
            "advanced_hazmat_suit", "Продвинутый защитный костюм", "Защитные системы",
            "Улучшенная защита с интегрированной системой жизнеобеспечения",
            Material.NETHERITE_CHESTPLATE, 200 * 60 * 1000, 45,
            Map.of(Material.NETHERITE_INGOT, 8, Material.BEACON, 2, Material.DIAMOND, 16),
            List.of("radiation_neutralizer")
        ));
        
        // Инструменты и утилиты
        RESEARCH_DATABASE.put("nano_repair_kit", new ResearchProject(
            "nano_repair_kit", "Нано-ремонтный набор", "Инструменты и утилиты",
            "Автоматическая система восстановления прочности предметов",
            Material.RECOVERY_COMPASS, 100 * 60 * 1000, 20,
            Map.of(Material.DIAMOND, 8, Material.REDSTONE, 32, Material.IRON_INGOT, 16),
            List.of("quantum_processor")
        ));
    }
}
