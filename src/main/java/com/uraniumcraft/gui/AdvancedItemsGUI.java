package com.uraniumcraft.gui;

import com.uraniumcraft.items.AdvancedItems;
import com.uraniumcraft.laboratory.Laboratory;
import com.uraniumcraft.laboratory.LaboratoryManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;

public class AdvancedItemsGUI {
    
    public static void openAdvancedItemsMenu(Player player, LaboratoryManager laboratoryManager) {
        Inventory gui = Bukkit.createInventory(null, 54, ChatColor.DARK_PURPLE + "Продвинутые предметы");
        
        List<Laboratory> playerLabs = laboratoryManager.getPlayerLaboratories(player.getUniqueId());
        boolean hasLab = !playerLabs.isEmpty();
        Laboratory lab = hasLab ? playerLabs.get(0) : null;
        
        // Силовая броня
        addAdvancedItem(gui, 10, AdvancedItems.createPowerArmorChestplate(), 
            "power_armor", hasLab && lab.isResearchCompleted("power_armor"));
        
        // Рельсотрон
        addAdvancedItem(gui, 12, AdvancedItems.createRailgun(), 
            "railgun", hasLab && lab.isResearchCompleted("railgun"));
        
        // Костюм химзащиты
        addAdvancedItem(gui, 14, AdvancedItems.createHazmatSuit(), 
            "hazmat_suit", hasLab && lab.isResearchCompleted("hazmat_suit"));
        
        // Автошахтёр
        addAdvancedItem(gui, 16, AdvancedItems.createAutoMiner(), 
            "auto_miner", hasLab && lab.isResearchCompleted("auto_miner"));
        
        // Электротранспорт
        addAdvancedItem(gui, 28, AdvancedItems.createElectricCar(), 
            "electric_transport", hasLab && lab.isResearchCompleted("electric_transport"));
        
        // Урановая капсула
        addAdvancedItem(gui, 30, AdvancedItems.createUraniumCapsule(), 
            "uranium_capsule", hasLab && lab.isResearchCompleted("uranium_capsule"));
        
        // Блок лаборатории
        addAdvancedItem(gui, 32, AdvancedItems.createLaboratoryBlock(), 
            "laboratory", laboratoryManager.isPlayerAuthorized(player.getUniqueId()));
        
        // Информация о исследованиях
        ItemStack researchInfo = new ItemStack(Material.ENCHANTING_TABLE);
        ItemMeta researchMeta = researchInfo.getItemMeta();
        researchMeta.setDisplayName(ChatColor.LIGHT_PURPLE + "Информация об исследованиях");
        
        if (hasLab) {
            researchMeta.setLore(Arrays.asList(
                ChatColor.GRAY + "Лаборатория: " + ChatColor.GREEN + "Есть",
                ChatColor.GRAY + "Завершённые исследования: " + ChatColor.YELLOW + lab.getCompletedResearch().size(),
                ChatColor.GRAY + "Активные исследования: " + ChatColor.YELLOW + lab.getActiveResearch().size(),
                "",
                ChatColor.GREEN + "Вы можете создавать изученные предметы!"
            ));
        } else {
            researchMeta.setLore(Arrays.asList(
                ChatColor.GRAY + "Лаборатория: " + ChatColor.RED + "Нет",
                ChatColor.RED + "Постройте лаборатории для исследований!",
                "",
                ChatColor.YELLOW + "Обратитесь к администратору за авторизацией"
            ));
        }
        
        researchInfo.setItemMeta(researchMeta);
        gui.setItem(4, researchInfo);
        
        // Режимы предметов
        addModeInfo(gui, 37, Material.REDSTONE, "Режимы силовой брони", Arrays.asList(
            "Стандартный - обычная защита",
            "Защита - сопротивление урону",
            "Скорость - увеличенная скорость",
            "Прыжки - высокие прыжки"
        ));
        
        addModeInfo(gui, 39, Material.CROSSBOW, "Режимы рельсотрона", Arrays.asList(
            "Одиночный - высокий урон",
            "Очередь - быстрая стрельба",
            "Пробивной - проходит сквозь блоки"
        ));
        
        addModeInfo(gui, 41, Material.MINECART, "Режимы транспорта", Arrays.asList(
            "Стандартный - обычная скорость",
            "Турбо - максимальная скорость",
            "Эко - экономия энергии"
        ));
        
        addModeInfo(gui, 43, Material.DISPENSER, "Режимы автошахтёра", Arrays.asList(
            "Обычная - добыча всех блоков",
            "Руды - только ценные руды",
            "Глубокая - добыча вниз"
        ));
        
        fillEmptySlots(gui);
        player.openInventory(gui);
    }
    
    private static void addAdvancedItem(Inventory gui, int slot, ItemStack item, String researchType, boolean researched) {
        ItemMeta meta = item.getItemMeta();
        
        if (researched) {
            meta.setLore(Arrays.asList(
                ChatColor.GREEN + "✓ Исследовано",
                ChatColor.GRAY + "Доступно для создания",
                ChatColor.YELLOW + "Нажмите для получения"
            ));
        } else {
            meta.setLore(Arrays.asList(
                ChatColor.RED + "✗ Не исследовано",
                ChatColor.GRAY + "Требует исследования в лаборатории",
                ChatColor.YELLOW + "Постройте лабораторию для изучения"
            ));
        }
        
        item.setItemMeta(meta);
        gui.setItem(slot, item);
    }
    
    private static void addModeInfo(Inventory gui, int slot, Material material, String title, List<String> modes) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.BLUE + title);
        
        List<String> lore = Arrays.asList(
            ChatColor.GRAY + "Доступные режимы:",
            ChatColor.WHITE + "• " + modes.get(0),
            ChatColor.WHITE + "• " + modes.get(1),
            ChatColor.WHITE + "• " + modes.get(2),
            modes.size() > 3 ? ChatColor.WHITE + "• " + modes.get(3) : "",
            "",
            ChatColor.YELLOW + "ПКМ для переключения режимов"
        );
        
        meta.setLore(lore);
        item.setItemMeta(meta);
        gui.setItem(slot, item);
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
