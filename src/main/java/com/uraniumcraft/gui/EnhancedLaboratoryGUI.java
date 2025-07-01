package com.uraniumcraft.gui;

import com.uraniumcraft.laboratory.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.Map;

public class EnhancedLaboratoryGUI {
    
    public static void openMainMenu(Player player, EnhancedLaboratory laboratory) {
        Inventory gui = Bukkit.createInventory(null, 54, 
            ChatColor.DARK_PURPLE + "Лаборатория " + laboratory.getLevel().getName());
        
        // Информация о лаборатории
        ItemStack info = new ItemStack(Material.BEACON);
        ItemMeta infoMeta = info.getItemMeta();
        infoMeta.setDisplayName(ChatColor.AQUA + "Информация о лаборатории");
        infoMeta.setLore(Arrays.asList(
            ChatColor.GRAY + "Уровень: " + ChatColor.YELLOW + laboratory.getLevel().getLevel() + " (" + laboratory.getLevel().getName() + ")",
            ChatColor.GRAY + "Специализация: " + ChatColor.BLUE + laboratory.getSpecialization().getName(),
            ChatColor.GRAY + "Статус: " + getStatusColor(laboratory) + laboratory.getState().name(),
            ChatColor.GRAY + "Энергия: " + getEnergyBar(laboratory),
            ChatColor.GRAY + "Хранилище: " + ChatColor.YELLOW + laboratory.getStorageUsage() + "/" + laboratory.getMaxStorageCapacity(),
            "",
            ChatColor.GREEN + "Завершённых исследований: " + laboratory.getCompletedResearch().size(),
            ChatColor.YELLOW + "Активных исследований: " + laboratory.getActiveResearch().size(),
            ChatColor.BLUE + "В очереди: " + laboratory.getResearchQueue().size()
        ));
        info.setItemMeta(infoMeta);
        gui.setItem(4, info);
        
        // Исследования
        ItemStack research = new ItemStack(Material.ENCHANTING_TABLE);
        ItemMeta researchMeta = research.getItemMeta();
        researchMeta.setDisplayName(ChatColor.LIGHT_PURPLE + "Исследования");
        researchMeta.setLore(Arrays.asList(
            ChatColor.GRAY + "Управление исследованиями",
            ChatColor.GRAY + "Максимум одновременно: " + ChatColor.YELLOW + laboratory.getLevel().getMaxResearch(),
            ChatColor.YELLOW + "Нажмите для открытия"
        ));
        research.setItemMeta(researchMeta);
        gui.setItem(19, research);
        
        // Хранилище
        ItemStack storage = new ItemStack(Material.CHEST);
        ItemMeta storageMeta = storage.getItemMeta();
        storageMeta.setDisplayName(ChatColor.GOLD + "Хранилище");
        storageMeta.setLore(Arrays.asList(
            ChatColor.GRAY + "Управление ресурсами",
            ChatColor.GRAY + "Заполнено: " + ChatColor.YELLOW + laboratory.getStorageUsage() + "/" + laboratory.getMaxStorageCapacity(),
            ChatColor.YELLOW + "Нажмите для открытия"
        ));
        storage.setItemMeta(storageMeta);
        gui.setItem(21, storage);
        
        // Энергия
        ItemStack energy = new ItemStack(Material.REDSTONE_BLOCK);
        ItemMeta energyMeta = energy.getItemMeta();
        energyMeta.setDisplayName(ChatColor.RED + "Энергетическая система");
        energyMeta.setLore(Arrays.asList(
            ChatColor.GRAY + "Текущая энергия: " + ChatColor.YELLOW + laboratory.getCurrentEnergy() + "/" + laboratory.getMaxEnergy(),
            ChatColor.GRAY + "Генерация: " + ChatColor.GREEN + "+" + laboratory.getEnergyGeneration() + "/сек",
            ChatColor.GRAY + "Потребление: " + ChatColor.RED + "-" + laboratory.getEnergyConsumption() + "/сек",
            ChatColor.YELLOW + "Нажмите для управления"
        ));
        energy.setItemMeta(energyMeta);
        gui.setItem(23, energy);
        
        // Улучшения
        ItemStack upgrade = new ItemStack(Material.ANVIL);
        ItemMeta upgradeMeta = upgrade.getItemMeta();
        upgradeMeta.setDisplayName(ChatColor.GOLD + "Улучшения");
        
        if (laboratory.getLevel().getLevel() < 5) {
            LaboratoryLevel nextLevel = LaboratoryLevel.getByLevel(laboratory.getLevel().getLevel() + 1);
            upgradeMeta.setLore(Arrays.asList(
                ChatColor.GRAY + "Текущий уровень: " + ChatColor.YELLOW + laboratory.getLevel().getLevel(),
                ChatColor.GRAY + "Следующий уровень: " + ChatColor.GREEN + nextLevel.getName(),
                ChatColor.GRAY + "Макс. энергия: " + ChatColor.BLUE + nextLevel.getMaxEnergy(),
                ChatColor.GRAY + "Макс. исследований: " + ChatColor.PURPLE + nextLevel.getMaxResearch(),
                ChatColor.YELLOW + "Нажмите для улучшения"
            ));
        } else {
            upgradeMeta.setLore(Arrays.asList(
                ChatColor.GRAY + "Максимальный уровень достигнут!",
                ChatColor.GOLD + "Ваша лаборатория на пике технологий"
            ));
        }
        
        upgrade.setItemMeta(upgradeMeta);
        gui.setItem(25, upgrade);
        
        // Специализация
        ItemStack specialization = new ItemStack(laboratory.getSpecialization().getIcon());
        ItemMeta specMeta = specialization.getItemMeta();
        specMeta.setDisplayName(ChatColor.BLUE + "Специализация");
        specMeta.setLore(Arrays.asList(
            ChatColor.GRAY + "Текущая: " + ChatColor.AQUA + laboratory.getSpecialization().getName(),
            ChatColor.GRAY + laboratory.getSpecialization().getDescription(),
            "",
            ChatColor.YELLOW + "Нажмите для изменения"
        ));
        specialization.setItemMeta(specMeta);
        gui.setItem(37, specialization);
        
        // Автоматизация
        ItemStack automation = new ItemStack(Material.REDSTONE_TORCH);
        ItemMeta autoMeta = automation.getItemMeta();
        autoMeta.setDisplayName(ChatColor.YELLOW + "Автоматизация");
        autoMeta.setLore(Arrays.asList(
            ChatColor.GRAY + "Авто-исследования: " + (laboratory.isAutoResearch() ? 
                ChatColor.GREEN + "Включено" : ChatColor.RED + "Выключено"),
            ChatColor.GRAY + "Авто-улучшения: " + (laboratory.isAutoUpgrade() ? 
                ChatColor.GREEN + "Включено" : ChatColor.RED + "Выключено"),
            "",
            ChatColor.YELLOW + "Нажмите для настройки"
        ));
        automation.setItemMeta(autoMeta);
        gui.setItem(39, automation);
        
        // Статистика
        ItemStack stats = new ItemStack(Material.BOOK);
        ItemMeta statsMeta = stats.getItemMeta();
        statsMeta.setDisplayName(ChatColor.GREEN + "Статистика");
        statsMeta.setLore(Arrays.asList(
            ChatColor.GRAY + "Подробная статистика лаборатории",
            ChatColor.YELLOW + "Нажмите для просмотра"
        ));
        stats.setItemMeta(statsMeta);
        gui.setItem(41, stats);
        
        // Активные исследования (быстрый просмотр)
        int slot = 28;
        for (Map.Entry<String, EnhancedResearchProject> entry : laboratory.getActiveResearch().entrySet()) {
            if (slot > 34) break;
            
            ItemStack researchItem = new ItemStack(Material.CLOCK);
            ItemMeta researchItemMeta = researchItem.getItemMeta();
            researchItemMeta.setDisplayName(ChatColor.YELLOW + "Исследование: " + entry.getKey());
            
            EnhancedResearchProject project = entry.getValue();
            researchItemMeta.setLore(Arrays.asList(
                ChatColor.GRAY + "Прогресс: " + String.format("%.1f", project.getProgress()) + "%",
                ChatColor.GRAY + "Осталось: " + project.getEstimatedTimeString(),
                ChatColor.GRAY + "Скорость: " + String.format("%.1fx", project.getSpeedMultiplier())
            ));
            
            researchItem.setItemMeta(researchItemMeta);
            gui.setItem(slot++, researchItem);
        }
        
        fillEmptySlots(gui);
        player.openInventory(gui);
    }
    
    public static void openSpecializationMenu(Player player, EnhancedLaboratory laboratory) {
        Inventory gui = Bukkit.createInventory(null, 45, ChatColor.BLUE + "Выбор специализации");
        
        int slot = 10;
        for (LaboratorySpecialization spec : LaboratorySpecialization.values()) {
            ItemStack item = new ItemStack(spec.getIcon());
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(ChatColor.AQUA + spec.getName());
            meta.setLore(Arrays.asList(
                ChatColor.GRAY + spec.getDescription(),
                "",
                ChatColor.YELLOW + "Специальные исследования:",
                ChatColor.WHITE + "• " + String.join("\n• ", spec.getSpecialResearch()),
                "",
                spec == laboratory.getSpecialization() ? 
                    ChatColor.GREEN + "✓ Текущая специализация" : 
                    ChatColor.YELLOW + "Нажмите для выбора"
            ));
            item.setItemMeta(meta);
            gui.setItem(slot++, item);
        }
        
        fillEmptySlots(gui);
        player.openInventory(gui);
    }
    
    public static void openStorageMenu(Player player, EnhancedLaboratory laboratory) {
        Inventory gui = Bukkit.createInventory(null, 54, ChatColor.GOLD + "Хранилище лаборатории");
        
        LaboratoryStorage storage = laboratory.getStorage();
        
        // Информация о хранилище
        ItemStack info = new ItemStack(Material.CHEST);
        ItemMeta infoMeta = info.getItemMeta();
        infoMeta.setDisplayName(ChatColor.GOLD + "Информация о хранилище");
        infoMeta.setLore(Arrays.asList(
            ChatColor.GRAY + "Заполнено: " + ChatColor.YELLOW + storage.getTotalItems() + "/" + laboratory.getMaxStorageCapacity(),
            ChatColor.GRAY + "Свободно: " + ChatColor.GREEN + storage.getFreeSpace(),
            storage.isFull() ? ChatColor.RED + "Хранилище заполнено!" : ChatColor.GREEN + "Есть свободное место"
        ));
        info.setItemMeta(infoMeta);
        gui.setItem(4, info);
        
        // Материалы
        int slot = 9;
        for (Map.Entry<Material, Integer> entry : storage.getAllMaterials().entrySet()) {
            if (slot >= 45) break;
            
            ItemStack item = new ItemStack(entry.getKey(), Math.min(64, entry.getValue()));
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(ChatColor.WHITE + entry.getKey().name());
            meta.setLore(Arrays.asList(
                ChatColor.GRAY + "Количество: " + ChatColor.YELLOW + entry.getValue(),
                ChatColor.GREEN + "Нажмите для извлечения"
            ));
            item.setItemMeta(meta);
            gui.setItem(slot++, item);
        }
        
        fillEmptySlots(gui);
        player.openInventory(gui);
    }
    
    private static String getStatusColor(EnhancedLaboratory laboratory) {
        switch (laboratory.getState()) {
            case OPERATIONAL:
                return ChatColor.GREEN.toString();
            case UNDER_CONSTRUCTION:
                return ChatColor.YELLOW.toString();
            case UPGRADING:
                return ChatColor.GOLD.toString();
            case MAINTENANCE:
                return ChatColor.RED.toString();
            case BLUEPRINT:
                return ChatColor.GRAY.toString();
            default:
                return ChatColor.WHITE.toString();
        }
    }
    
    private static String getEnergyBar(EnhancedLaboratory laboratory) {
        double percentage = laboratory.getEnergyPercentage();
        int bars = (int) (percentage / 5); // 20 символов максимум
        
        StringBuilder bar = new StringBuilder();
        for (int i = 0; i < 20; i++) {
            if (i < bars) {
                if (percentage > 80) {
                    bar.append(ChatColor.GREEN).append("█");
                } else if (percentage > 50) {
                    bar.append(ChatColor.YELLOW).append("█");
                } else if (percentage > 20) {
                    bar.append(ChatColor.GOLD).append("█");
                } else {
                    bar.append(ChatColor.RED).append("█");
                }
            } else {
                bar.append(ChatColor.GRAY).append("░");
            }
        }
        
        return bar.toString() + ChatColor.WHITE + " " + laboratory.getCurrentEnergy() + "/" + laboratory.getMaxEnergy();
    }
    
    private static void fillEmptySlots(Inventory gui) {
        ItemStack filler = new ItemStack(Material.PURPLE_STAINED_GLASS_PANE);
        ItemMeta fillerMeta = filler.getItemMeta();
        fillerMeta.setDisplayName(" ");
        filler.setItemMeta(fillerMeta);
        
        for (int i = 0; i < gui.getSize(); i++) {
            if (gui.getItem(i) == null) {
                gui.setItem(i, filler);
            }
        }
    }
}
