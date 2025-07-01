package com.uraniumcraft.gui;

import com.uraniumcraft.items.UraniumItems;
import com.uraniumcraft.laboratory.LaboratoryTerminal;
import com.uraniumcraft.laboratory.ResearchProject;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;

public class LaboratoryGUI {
    
    public static void openMainMenu(Player player, LaboratoryTerminal terminal) {
        Inventory gui = Bukkit.createInventory(null, 54, ChatColor.DARK_PURPLE + "Терминал лаборатории");
        
        // Статус терминала
        ItemStack status = new ItemStack(terminal.isActive() ? Material.GREEN_CONCRETE : Material.RED_CONCRETE);
        ItemMeta statusMeta = status.getItemMeta();
        statusMeta.setDisplayName(ChatColor.AQUA + "Статус терминала");
        statusMeta.setLore(Arrays.asList(
            ChatColor.GRAY + "Состояние: " + (terminal.isActive() ? 
                ChatColor.GREEN + "Активен" : ChatColor.RED + "Неактивен"),
            ChatColor.GRAY + "Прогресс активации: " + terminal.getActivationProgress() + "%",
            ChatColor.GRAY + "Завершённых исследований: " + terminal.getCompletedResearch().size(),
            ChatColor.GRAY + "Активных исследований: " + terminal.getActiveResearch().size()
        ));
        status.setItemMeta(statusMeta);
        gui.setItem(4, status);
        
        if (!terminal.isActive()) {
            // Показываем требуемые материалы
            showRequiredMaterials(gui, terminal);
        } else {
            // Показываем исследования
            showResearchOptions(gui, terminal);
        }
        
        // Заполняем пустые слоты
        fillEmptySlots(gui);
        
        player.openInventory(gui);
    }
    
    private static void showRequiredMaterials(Inventory gui, LaboratoryTerminal terminal) {
        int slot = 19;
        
        for (Material material : terminal.getRequiredMaterials().keySet()) {
            int required = terminal.getRequiredMaterials().get(material);
            if (required > 0) {
                ItemStack item = new ItemStack(material);
                ItemMeta meta = item.getItemMeta();
                meta.setDisplayName(ChatColor.YELLOW + "Требуется: " + getMaterialName(material));
                meta.setLore(Arrays.asList(
                    ChatColor.GRAY + "Количество: " + required,
                    ChatColor.YELLOW + "Принесите материалы в руке и кликните по терминалу"
                ));
                item.setItemMeta(meta);
                gui.setItem(slot++, item);
            }
        }
    }
    
    private static void showResearchOptions(Inventory gui, LaboratoryTerminal terminal) {
        // Активные исследования
        int slot = 10;
        for (ResearchProject project : terminal.getActiveResearch().values()) {
            ItemStack item = new ItemStack(Material.CLOCK);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(ChatColor.YELLOW + "Исследование: " + getResearchName(project.getResearchType()));
            
            long remaining = project.getRemainingTime();
            int minutes = (int) (remaining / 60000);
            int seconds = (int) ((remaining % 60000) / 1000);
            
            meta.setLore(Arrays.asList(
                ChatColor.GRAY + "Статус: " + ChatColor.YELLOW + "В процессе",
                ChatColor.GRAY + "Прогресс: " + String.format("%.1f", project.getProgressPercentage()) + "%",
                ChatColor.GRAY + "Осталось: " + minutes + "м " + seconds + "с"
            ));
            item.setItemMeta(meta);
            gui.setItem(slot++, item);
        }
        
        // Доступные исследования
        slot = 28;
        for (String researchType : terminal.getAvailableResearch()) {
            ItemStack item = getResearchIcon(researchType);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(ChatColor.GREEN + "Начать: " + getResearchName(researchType));
            meta.setLore(Arrays.asList(
                ChatColor.GRAY + "Статус: " + ChatColor.GREEN + "Доступно",
                ChatColor.YELLOW + "Нажмите, чтобы начать исследование"
            ));
            item.setItemMeta(meta);
            gui.setItem(slot++, item);
        }
        
        // Завершённые исследования
        slot = 46;
        for (String completed : terminal.getCompletedResearch()) {
            ItemStack item = getResearchIcon(completed);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(ChatColor.AQUA + "Завершено: " + getResearchName(completed));
            meta.setLore(Arrays.asList(
                ChatColor.GRAY + "Статус: " + ChatColor.AQUA + "Завершено",
                ChatColor.GREEN + "Можно создавать предмет"
            ));
            item.setItemMeta(meta);
            gui.setItem(slot++, item);
        }
    }
    
    private static ItemStack getResearchIcon(String researchType) {
        switch (researchType.toLowerCase()) {
            case "hazmat_helmet":
                return new ItemStack(Material.LEATHER_HELMET);
            case "hazmat_suit":
                return new ItemStack(Material.LEATHER_CHESTPLATE);
            case "power_armor_helmet":
                return new ItemStack(Material.NETHERITE_HELMET);
            case "power_armor_chestplate":
                return new ItemStack(Material.NETHERITE_CHESTPLATE);
            case "power_armor_leggings":
                return new ItemStack(Material.NETHERITE_LEGGINGS);
            case "power_armor_boots":
                return new ItemStack(Material.NETHERITE_BOOTS);
            case "railgun":
                return new ItemStack(Material.CROSSBOW);
            case "uranium_tablet":
                return new ItemStack(Material.RECOVERY_COMPASS);
            case "teleporter_core":
                return new ItemStack(Material.BEACON);
            case "centrifuge_core":
                return new ItemStack(Material.DISPENSER);
            default:
                return new ItemStack(Material.PAPER);
        }
    }
    
    private static String getResearchName(String researchType) {
        switch (researchType.toLowerCase()) {
            case "hazmat_helmet": return "Шлем химзащиты";
            case "hazmat_suit": return "Костюм химзащиты";
            case "power_armor_helmet": return "Шлем силовой брони";
            case "power_armor_chestplate": return "Нагрудник силовой брони";
            case "power_armor_leggings": return "Поножи силовой брони";
            case "power_armor_boots": return "Ботинки силовой брони";
            case "railgun": return "Рельсотрон";
            case "uranium_tablet": return "Урановый планшет";
            case "teleporter_core": return "Ядро телепорта";
            case "centrifuge_core": return "Ядро центрифуги";
            default: return researchType;
        }
    }
    
    private static String getMaterialName(Material material) {
        switch (material) {
            case IRON_BLOCK: return "Железные блоки";
            case REDSTONE_BLOCK: return "Редстоун блоки";
            case DIAMOND: return "Алмазы";
            case EMERALD: return "Изумруды";
            default: return material.name();
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
}
