package com.uraniumcraft.laboratory;

import com.uraniumcraft.UraniumPlugin;
import com.uraniumcraft.items.UraniumItems;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class ResearchTerminalManager implements Listener {
    private final UraniumPlugin plugin;
    private final EnhancedLaboratoryManager laboratoryManager;
    private final Map<String, ResearchProject> researchDatabase;
    private final Map<String, Map<Material, Integer>> pendingMaterials;
    
    public ResearchTerminalManager(UraniumPlugin plugin, EnhancedLaboratoryManager laboratoryManager) {
        this.plugin = plugin;
        this.laboratoryManager = laboratoryManager;
        this.researchDatabase = new HashMap<>();
        this.pendingMaterials = new HashMap<>();
        initializeResearchDatabase();
    }
    
    // ==================== GUI METHODS ====================
    
    public void openMainTerminal(Player player, EnhancedLaboratory laboratory) {
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
        playTerminalAccessEffect(laboratory.getTerminalLocation());
    }
    
    public void openResearchCategory(Player player, EnhancedLaboratory laboratory, String category) {
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
    
    public void openResearchDetails(Player player, EnhancedLaboratory laboratory, String researchId) {
        ResearchProject project = researchDatabase.get(researchId);
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
    
    // ==================== EVENT HANDLERS ====================
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        
        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();
        
        if (title.equals(ChatColor.DARK_BLUE + "🔬 Исследовательский терминал")) {
            handleMainTerminalClick(player, event);
        } else if (title.startsWith(ChatColor.DARK_PURPLE + "📁 ")) {
            handleCategoryClick(player, event);
        } else if (title.startsWith(ChatColor.DARK_GREEN + "🔬 ")) {
            handleResearchDetailsClick(player, event);
        }
    }
    
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;
        
        Player player = (Player) event.getPlayer();
        String title = event.getView().getTitle();
        
        // Очищаем временные материалы при закрытии детального окна исследования
        if (title.startsWith(ChatColor.DARK_GREEN + "🔬 ")) {
            String playerKey = player.getUniqueId().toString();
            if (pendingMaterials.containsKey(playerKey)) {
                // Возвращаем материалы игроку
                Map<Material, Integer> materials = pendingMaterials.get(playerKey);
                for (Map.Entry<Material, Integer> entry : materials.entrySet()) {
                    ItemStack item = new ItemStack(entry.getKey(), entry.getValue());
                    player.getInventory().addItem(item);
                }
                pendingMaterials.remove(playerKey);
                
                player.sendMessage(ChatColor.YELLOW + "Материалы возвращены в инвентарь.");
            }
        }
    }
    
    // ==================== PRIVATE METHODS ====================
    
    private void handleMainTerminalClick(Player player, InventoryClickEvent event) {
        event.setCancelled(true);
        
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;
        
        String displayName = clicked.getItemMeta().getDisplayName();
        
        var playerLabs = laboratoryManager.getPlayerLaboratories(player.getUniqueId());
        if (playerLabs.isEmpty()) return;
        
        EnhancedLaboratory laboratory = playerLabs.get(0);
        
        if (displayName.equals(ChatColor.AQUA + "🔄 Обновить данные")) {
            openMainTerminal(player, laboratory);
            player.sendMessage(ChatColor.GREEN + "Данные терминала обновлены!");
            
        } else if (displayName.startsWith(ChatColor.GREEN + "📁 ") || 
                   displayName.startsWith(ChatColor.RED + "📁 ") ||
                   displayName.startsWith(ChatColor.LIGHT_PURPLE + "📁 ") ||
                   displayName.startsWith(ChatColor.BLUE + "📁 ") ||
                   displayName.startsWith(ChatColor.GOLD + "📁 ")) {
            
            String category = extractCategoryFromDisplayName(displayName);
            openResearchCategory(player, laboratory, category);
            
        } else if (displayName.startsWith(ChatColor.GOLD + "🔄 ")) {
            // Клик по активному исследованию - показываем детали
            String researchName = displayName.substring(4); // Убираем "🔄 "
            showActiveResearchDetails(player, laboratory, researchName);
        }
        
        // Звуковые эффекты
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.2f);
    }
    
    private void handleCategoryClick(Player player, InventoryClickEvent event) {
        event.setCancelled(true);
        
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;
        
        String displayName = clicked.getItemMeta().getDisplayName();
        
        var playerLabs = laboratoryManager.getPlayerLaboratories(player.getUniqueId());
        if (playerLabs.isEmpty()) return;
        
        EnhancedLaboratory laboratory = playerLabs.get(0);
        
        if (displayName.equals(ChatColor.YELLOW + "← Вернуться к главному меню")) {
            openMainTerminal(player, laboratory);
            
        } else if (displayName.startsWith(ChatColor.GREEN + "✅ ") ||
                   displayName.startsWith(ChatColor.GOLD + "🔄 ") ||
                   displayName.startsWith(ChatColor.YELLOW + "⭐ ") ||
                   displayName.startsWith(ChatColor.RED + "🔒 ")) {
            
            String researchName = extractResearchNameFromDisplayName(displayName);
            String researchId = convertNameToId(researchName);
            openResearchDetails(player, laboratory, researchId);
        }
        
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.4f, 1.1f);
    }
    
    private void handleResearchDetailsClick(Player player, InventoryClickEvent event) {
        event.setCancelled(true);
        
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;
        
        String displayName = clicked.getItemMeta().getDisplayName();
        
        var playerLabs = laboratoryManager.getPlayerLaboratories(player.getUniqueId());
        if (playerLabs.isEmpty()) return;
        
        EnhancedLaboratory laboratory = playerLabs.get(0);
        
        if (displayName.equals(ChatColor.YELLOW + "← Назад к категории")) {
            openMainTerminal(player, laboratory);
            
        } else if (displayName.equals(ChatColor.GREEN + "🚀 НАЧАТЬ ИССЛЕДОВАНИЕ")) {
            handleStartResearch(player, event, laboratory);
            
        } else if (displayName.startsWith(ChatColor.YELLOW + "Требуется: ")) {
            handleMaterialSlotClick(player, event);
        }
        
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.3f, 1.0f);
    }
    
    private void handleStartResearch(Player player, InventoryClickEvent event, EnhancedLaboratory laboratory) {
        String title = event.getView().getTitle();
        String researchName = title.substring(4); // Убираем "🔬 "
        String researchId = convertNameToId(researchName);
        
        String playerKey = player.getUniqueId().toString();
        Map<Material, Integer> materials = pendingMaterials.get(playerKey);
        
        if (materials == null || materials.isEmpty()) {
            player.sendMessage(ChatColor.RED + "❌ Поместите все необходимые материалы в слоты!");
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);
            return;
        }
        
        // Начинаем исследование
        if (laboratory.startResearch(researchId, materials)) {
            player.sendMessage(ChatColor.GREEN + "🚀 Исследование '" + researchName + "' начато!");
            player.sendMessage(ChatColor.YELLOW + "⏱️ Процесс займёт некоторое время...");
            
            // Очищаем временные материалы
            pendingMaterials.remove(playerKey);
            
            // Эффекты запуска исследования
            playResearchStartEffect(laboratory.getTerminalLocation());
            
            // Закрываем GUI и возвращаемся к главному терминалу
            new BukkitRunnable() {
                @Override
                public void run() {
                    openMainTerminal(player, laboratory);
                }
            }.runTaskLater(plugin, 10L);
            
        } else {
            player.sendMessage(ChatColor.RED + "❌ Не удалось начать исследование!");
            player.sendMessage(ChatColor.GRAY + "Проверьте требования и доступность лаборатории.");
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);
        }
    }
    
    private void handleMaterialSlotClick(Player player, InventoryClickEvent event) {
        // Если игрок кликает с предметом в руке - добавляем материал
        ItemStack cursor = event.getCursor();
        if (cursor != null && cursor.getType() != Material.AIR) {
            String playerKey = player.getUniqueId().toString();
            Map<Material, Integer> materials = pendingMaterials.computeIfAbsent(playerKey, k -> new HashMap<>());
            
            Material material = cursor.getType();
            int amount = cursor.getAmount();
            
            materials.put(material, materials.getOrDefault(material, 0) + amount);
            
            // Обновляем слот
            ItemStack slotItem = new ItemStack(material, amount);
            ItemMeta meta = slotItem.getItemMeta();
            meta.setDisplayName(ChatColor.GREEN + "✅ " + getMaterialDisplayName(material));
            meta.setLore(Arrays.asList(
                ChatColor.GRAY + "Количество: " + ChatColor.WHITE + amount,
                "",
                ChatColor.GREEN + "Материал размещён!"
            ));
            slotItem.setItemMeta(meta);
            event.setCurrentItem(slotItem);
            
            // Очищаем курсор
            event.setCursor(null);
            
            player.sendMessage(ChatColor.GREEN + "✅ Материал добавлен: " + getMaterialDisplayName(material) + " x" + amount);
            player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 0.8f, 1.2f);
        }
        // Если кликает по заполненному слоту - возвращаем материал
        else if (event.getCurrentItem() != null && 
                 event.getCurrentItem().getItemMeta().getDisplayName().startsWith(ChatColor.GREEN + "✅ ")) {
            
            ItemStack item = event.getCurrentItem();
            Material material = item.getType();
            int amount = item.getAmount();
            
            // Возвращаем материал игроку
            player.getInventory().addItem(new ItemStack(material, amount));
            
            // Удаляем из временного хранилища
            String playerKey = player.getUniqueId().toString();
            Map<Material, Integer> materials = pendingMaterials.get(playerKey);
            if (materials != null) {
                materials.remove(material);
            }
            
            // Возвращаем слот к исходному состоянию
            ItemStack placeholder = new ItemStack(material);
            ItemMeta placeholderMeta = placeholder.getItemMeta();
            placeholderMeta.setDisplayName(ChatColor.YELLOW + "Требуется: " + getMaterialDisplayName(material));
            placeholderMeta.setLore(Arrays.asList(
                ChatColor.GRAY + "Поместите материал в этот слот"
            ));
            placeholder.setItemMeta(placeholderMeta);
            event.setCurrentItem(placeholder);
            
            player.sendMessage(ChatColor.YELLOW + "Материал возвращён: " + getMaterialDisplayName(material) + " x" + amount);
            player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 0.6f, 0.8f);
        }
    }
    
    // ==================== SETUP METHODS ====================
    
    private void setupNavigationButtons(Inventory gui, EnhancedLaboratory laboratory) {
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
    
    private void setupActiveResearchDisplay(Inventory gui, EnhancedLaboratory laboratory) {
        int slot = 19;
        for (Map.Entry<String, EnhancedResearchProject> entry : 
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
    
    private void setupQuickAccessCategories(Inventory gui, EnhancedLaboratory laboratory) {
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
            ChatColor.GRAY + "• Ядро телепортации",
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
    
    private void setupLaboratoryStatus(Inventory gui, EnhancedLaboratory laboratory) {
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
    
    private void setupCategoryResearch(Inventory gui, EnhancedLaboratory laboratory, String category) {
        int slot = 9;
        for (Map.Entry<String, ResearchProject> entry : researchDatabase.entrySet()) {
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
    
    private void setupMaterialSlots(Inventory gui, ResearchProject project) {
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
    
    private void fillTerminalBackground(Inventory gui) {
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
    
    // ==================== VISUAL EFFECTS ====================
    
    private void playTerminalAccessEffect(Location location) {
        if (location == null || location.getWorld() == null) return;
        
        location.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, location.clone().add(0, 1, 0), 15, 0.5, 0.5, 0.5, 0.1);
        location.getWorld().spawnParticle(Particle.END_ROD, location.clone().add(0, 1.2, 0), 8, 0.3, 0.3, 0.3, 0.02);
        
        // Голографический эффект
        for (int i = 0; i < 10; i++) {
            double y = 1 + (i * 0.1);
            location.getWorld().spawnParticle(Particle.REDSTONE, location.clone().add(0, y, 0), 1, 0, 0, 0, 0,
                new Particle.DustOptions(Color.AQUA, 0.6f));
        }
        
        location.getWorld().playSound(location, Sound.BLOCK_BEACON_ACTIVATE, 0.6f, 1.8f);
        location.getWorld().playSound(location, Sound.UI_BUTTON_CLICK, 0.8f, 1.5f);
    }
    
    private void playResearchStartEffect(Location location) {
        if (location == null || location.getWorld() == null) return;
        
        // Начальный взрыв энергии
        location.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, location.clone().add(0, 1, 0), 1);
        location.getWorld().spawnParticle(Particle.ENCHANTMENT_TABLE, location.clone().add(0, 2, 0), 40, 1.5, 1.5, 1.5, 0.2);
        location.getWorld().spawnParticle(Particle.END_ROD, location.clone().add(0, 1, 0), 25, 1, 1, 1, 0.1);
        
        // Звуки
        location.getWorld().playSound(location, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 0.3f, 2.0f);
        location.getWorld().playSound(location, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1.0f, 1.3f);
        
        // Продолжительный эффект
        new BukkitRunnable() {
            int ticks = 0;
            
            @Override
            public void run() {
                if (ticks >= 100) { // 5 секунд
                    cancel();
                    return;
                }
                
                // Спиральные частицы
                double height = ticks * 0.05;
                double angle = ticks * 0.2;
                
                for (int i = 0; i < 3; i++) {
                    double spiralAngle = angle + i * (Math.PI * 2 / 3);
                    double x = Math.cos(spiralAngle) * 1.5;
                    double z = Math.sin(spiralAngle) * 1.5;
                    
                    location.getWorld().spawnParticle(Particle.REDSTONE, 
                        location.clone().add(x, 1 + height, z), 1, 0, 0, 0, 0,
                        new Particle.DustOptions(Color.LIME, 0.8f));
                }
                
                ticks++;
            }
        }.runTaskTimer(plugin, 0, 1);
    }
    
    // ==================== UTILITY METHODS ====================
    
    private void initializeResearchDatabase() {
        // Базовые технологии
        addResearch("radiation_detector", "Детектор радиации", "Базовые технологии", 
            "Устройство для обнаружения радиоактивного излучения", Material.COMPASS, 300,
            Map.of(Material.IRON_INGOT, 4, Material.REDSTONE, 8, Material.GLASS, 2), 50);
        
        addResearch("protective_suit", "Защитный костюм", "Базовые технологии",
            "Базовая защита от радиации", Material.LEATHER_CHESTPLATE, 600,
            Map.of(Material.LEATHER, 8, Material.IRON_INGOT, 4, Material.WOOL, 6), 75);
        
        addResearch("uranium_tools", "Урановые инструменты", "Базовые технологии",
            "Инструменты с урановым покрытием", Material.IRON_PICKAXE, 450,
            Map.of(UraniumItems.URANIUM_INGOT.getType(), 3, Material.IRON_INGOT, 6, Material.STICK, 2), 60);
        
        // Энергетические системы
        addResearch("quantum_reactor", "Квантовый реактор", "Энергетические системы",
            "Продвинутый источник энергии", Material.FURNACE, 1200,
            Map.of(UraniumItems.URANIUM_INGOT.getType(), 8, Material.IRON_BLOCK, 4, Material.REDSTONE_BLOCK, 2), 150);
        
        addResearch("energy_amplifier", "Энергетический усилитель", "Энергетические системы",
            "Увеличивает эффективность энергосистем", Material.REPEATER, 900,
            Map.of(Material.GOLD_INGOT, 6, Material.REDSTONE, 16, Material.QUARTZ, 4), 120);
        
        // Медицинские технологии
        addResearch("healing_chamber", "Лечебная камера", "Медицинские технологии",
            "Устройство для лечения радиационного отравления", Material.BEACON, 1800,
            Map.of(Material.DIAMOND, 4, Material.EMERALD, 2, Material.GOLD_BLOCK, 3), 200);
        
        addResearch("radiation_neutralizer", "Нейтрализатор радиации", "Медицинские технологии",
            "Очищает территорию от радиации", Material.CONDUIT, 1500,
            Map.of(Material.PRISMARINE_CRYSTALS, 8, Material.HEART_OF_THE_SEA, 1, Material.NAUTILUS_SHELL, 4), 180);
        
        // Квантовые технологии
        addResearch("teleportation_core", "Ядро телепортации", "Квантовые технологии",
            "Основа для создания телепортационных систем", Material.NETHER_STAR, 2400,
            Map.of(Material.NETHER_STAR, 1, UraniumItems.URANIUM_INGOT.getType(), 12, Material.ENDER_PEARL, 8), 300);
        
        addResearch("quantum_processor", "Квантовый процессор", "Квантовые технологии",
            "Высокопроизводительный вычислительный блок", Material.OBSERVER, 2100,
            Map.of(Material.DIAMOND, 6, Material.REDSTONE_BLOCK, 4, Material.QUARTZ_BLOCK, 8), 250);
        
        addResearch("holographic_display", "Голографический дисплей", "Квантовые технологии",
            "Трёхмерный проекционный дисплей", Material.GLASS, 1800,
            Map.of(Material.GLASS, 16, Material.GLOWSTONE_DUST, 12, Material.PRISMARINE_SHARD, 6), 220);
        
        // Защитные системы
        addResearch("power_armor", "Силовая броня", "Защитные системы",
            "Продвинутая защитная броня с энергопитанием", Material.NETHERITE_CHESTPLATE, 3000,
            Map.of(Material.NETHERITE_INGOT, 4, UraniumItems.URANIUM_INGOT.getType(), 16, Material.DIAMOND, 8), 400);
        
        addResearch("force_field", "Защитное поле", "Защитные системы",
            "Энергетический барьер для защиты территории", Material.BARRIER, 2700,
            Map.of(Material.BEACON, 2, Material.DIAMOND_BLOCK, 4, Material.REDSTONE_BLOCK, 6), 350);
        
        // Инструменты и утилиты
        addResearch("repair_kit", "Ремонтный набор", "Инструменты и утилиты",
            "Автоматический ремонт оборудования", Material.ANVIL, 1200,
            Map.of(Material.IRON_BLOCK, 3, Material.DIAMOND, 2, Material.EXPERIENCE_BOTTLE, 8), 150);
        
        addResearch("material_scanner", "Сканер материалов", "Инструменты и утилиты",
            "Анализирует состав и свойства материалов", Material.RECOVERY_COMPASS, 900,
            Map.of(Material.GOLD_INGOT, 4, Material.REDSTONE, 12, Material.GLASS_PANE, 8), 120);
    }
    
    private void addResearch(String id, String name, String category, String description, 
                           Material icon, int duration, Map<Material, Integer> materials, int energy) {
        ResearchProject project = new ResearchProject(id, name, category, description, icon, duration, materials, energy);
        researchDatabase.put(id, project);
    }
    
    private boolean canStartResearch(EnhancedLaboratory laboratory, ResearchProject project) {
        return hasPrerequisites(laboratory, project) && 
               hasEnoughEnergy(laboratory, project) &&
               !laboratory.isResearchCompleted(project.getId()) &&
               !laboratory.getActiveResearch().containsKey(project.getId());
    }
    
    private boolean hasPrerequisites(EnhancedLaboratory laboratory, ResearchProject project) {
        // Проверяем предварительные исследования
        for (String prerequisite : project.getPrerequisites()) {
            if (!laboratory.isResearchCompleted(prerequisite)) {
                return false;
            }
        }
        return true;
    }
    
    private boolean hasEnoughEnergy(EnhancedLaboratory laboratory, ResearchProject project) {
        return laboratory.getCurrentEnergy() >= project.getEnergyRequired() * 10; // Минимум на 10 секунд работы
    }
    
    private String formatTime(int seconds) {
        int hours = seconds / 3600;
        int minutes = (seconds % 3600) / 60;
        int secs = seconds % 60;
        
        if (hours > 0) {
            return String.format("%dч %dм %dс", hours, minutes, secs);
        } else if (minutes > 0) {
            return String.format("%dм %dс", minutes, secs);
        } else {
            return String.format("%dс", secs);
        }
    }
    
    private String createProgressBar(double progress) {
        int bars = 20;
        int filled = (int) (progress / 100.0 * bars);
        
        StringBuilder bar = new StringBuilder();
        bar.append(ChatColor.GREEN);
        for (int i = 0; i < filled; i++) {
            bar.append("█");
        }
        bar.append(ChatColor.GRAY);
        for (int i = filled; i < bars; i++) {
            bar.append("█");
        }
        
        return bar.toString();
    }
    
    private String getEnergyBar(EnhancedLaboratory laboratory) {
        double percentage = (double) laboratory.getCurrentEnergy() / laboratory.getMaxEnergy();
        int bars = 10;
        int filled = (int) (percentage * bars);
        
        StringBuilder bar = new StringBuilder();
        if (percentage > 0.6) {
            bar.append(ChatColor.GREEN);
        } else if (percentage > 0.3) {
            bar.append(ChatColor.YELLOW);
        } else {
            bar.append(ChatColor.RED);
        }
        
        for (int i = 0; i < filled; i++) {
            bar.append("█");
        }
        bar.append(ChatColor.GRAY);
        for (int i = filled; i < bars; i++) {
            bar.append("█");
        }
        
        return bar.toString();
    }
    
    private ItemStack getCategoryIcon(String category) {
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
    
    private String extractCategoryFromDisplayName(String displayName) {
        // Убираем цветовые коды и символы
        return displayName.replaceAll("§[0-9a-fk-or]", "").replace("📁 ", "");
    }
    
    private String extractResearchNameFromDisplayName(String displayName) {
        // Убираем цветовые коды и символы статуса
        return displayName.replaceAll("§[0-9a-fk-or]", "")
                         .replace("✅ ", "")
                         .replace("🔄 ", "")
                         .replace("⭐ ", "")
                         .replace("🔒 ", "");
    }
    
    private String convertNameToId(String name) {
        return name.toLowerCase()
                  .replace(" ", "_")
                  .replace("ё", "e")
                  .replace("а", "a")
                  .replace("б", "b")
                  .replace("в", "v")
                  .replace("г", "g")
                  .replace("д", "d")
                  .replace("е", "e")
                  .replace("ж", "zh")
                  .replace("з", "z")
                  .replace("и", "i")
                  .replace("й", "y")
                  .replace("к", "k")
                  .replace("л", "l")
                  .replace("м", "m")
                  .replace("н", "n")
                  .replace("о", "o")
                  .replace("п", "p")
                  .replace("р", "r")
                  .replace("с", "s")
                  .replace("т", "t")
                  .replace("у", "u")
                  .replace("ф", "f")
                  .replace("х", "h")
                  .replace("ц", "ts")
                  .replace("ч", "ch")
                  .replace("ш", "sh")
                  .replace("щ", "sch")
                  .replace("ъ", "")
                  .replace("ы", "y")
                  .replace("ь", "")
                  .replace("э", "e")
                  .replace("ю", "yu")
                  .replace("я", "ya");
    }
    
    private String getMaterialDisplayName(Material material) {
        return material.name().toLowerCase().replace("_", " ");
    }
    
    private void showActiveResearchDetails(Player player, EnhancedLaboratory laboratory, String researchName) {
        String researchId = convertNameToId(researchName);
        EnhancedResearchProject project = laboratory.getActiveResearch().get(researchId);
        
        if (project == null) return;
        
        player.sendMessage(ChatColor.GOLD + "=== Детали исследования ===");
        player.sendMessage(ChatColor.YELLOW + "Название: " + ChatColor.WHITE + researchName);
        player.sendMessage(ChatColor.YELLOW + "Прогресс: " + ChatColor.WHITE + String.format("%.1f", project.getProgress()) + "%");
        player.sendMessage(ChatColor.YELLOW + "Осталось времени: " + ChatColor.WHITE + project.getEstimatedTimeString());
        player.sendMessage(ChatColor.YELLOW + "Потребление энергии: " + ChatColor.WHITE + project.getEnergyPerSecond() + "/сек");
        
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 0.8f, 1.2f);
    }
    
    private ChatColor getEnergyStatusColor(EnhancedLaboratory laboratory) {
        double percentage = (double) laboratory.getCurrentEnergy() / laboratory.getMaxEnergy();
        if (percentage > 0.6) return ChatColor.GREEN;
        if (percentage > 0.3) return ChatColor.YELLOW;
        return ChatColor.RED;
    }
    
    private String getEnergyStatusText(EnhancedLaboratory laboratory) {
        double percentage = (double) laboratory.getCurrentEnergy() / laboratory.getMaxEnergy();
        if (percentage > 0.6) return "Энергосистема в норме";
        if (percentage > 0.3) return "Низкий уровень энергии";
        return "Критический уровень энергии!";
    }
    
    private ChatColor getStorageStatusColor(EnhancedLaboratory laboratory) {
        double percentage = (double) laboratory.getStorageUsage() / laboratory.getMaxStorageCapacity();
        if (percentage < 0.7) return ChatColor.GREEN;
        if (percentage < 0.9) return ChatColor.YELLOW;
        return ChatColor.RED;
    }
    
    private String getStorageStatusText(EnhancedLaboratory laboratory) {
        double percentage = (double) laboratory.getStorageUsage() / laboratory.getMaxStorageCapacity();
        if (percentage < 0.7) return "Достаточно места";
        if (percentage < 0.9) return "Хранилище заполняется";
        return "Хранилище почти заполнено!";
    }
}
