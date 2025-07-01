package com.uraniumcraft.listeners;

import com.uraniumcraft.UraniumPlugin;
import com.uraniumcraft.gui.LaboratoryGUI;
import com.uraniumcraft.items.AdvancedItems;
import com.uraniumcraft.laboratory.Laboratory;
import com.uraniumcraft.laboratory.LaboratoryManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GUIListener implements Listener {
    private final UraniumPlugin plugin;
    private final LaboratoryManager laboratoryManager;
    
    public GUIListener(UraniumPlugin plugin, LaboratoryManager laboratoryManager) {
        this.plugin = plugin;
        this.laboratoryManager = laboratoryManager;
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        
        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();
        
        if (!title.startsWith(ChatColor.DARK_PURPLE + "Лаборатория")) return;
        
        event.setCancelled(true);
        
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;
        
        List<Laboratory> playerLabs = laboratoryManager.getPlayerLaboratories(player.getUniqueId());
        if (playerLabs.isEmpty()) return;
        
        Laboratory laboratory = playerLabs.get(0);
        
        if (title.contains("Главное меню")) {
            handleMainMenuClick(player, laboratory, clicked);
        } else if (title.contains("Исследования")) {
            handleResearchMenuClick(player, laboratory, clicked);
        } else if (title.contains("Производство")) {
            handleProductionMenuClick(player, laboratory, clicked);
        }
    }
    
    private void handleMainMenuClick(Player player, Laboratory laboratory, ItemStack clicked) {
        String displayName = clicked.getItemMeta().getDisplayName();
        
        if (displayName.equals(ChatColor.LIGHT_PURPLE + "Исследования")) {
            LaboratoryGUI.openResearchMenu(player, laboratory);
        } else if (displayName.equals(ChatColor.GOLD + "Производство")) {
            LaboratoryGUI.openProductionMenu(player, laboratory);
        } else if (displayName.equals(ChatColor.YELLOW + "Хранилище")) {
            player.sendMessage(ChatColor.YELLOW + "Хранилище пока не реализовано!");
        }
    }
    
    private void handleResearchMenuClick(Player player, Laboratory laboratory, ItemStack clicked) {
        String displayName = clicked.getItemMeta().getDisplayName();
        
        if (displayName.equals(ChatColor.GRAY + "Назад")) {
            LaboratoryGUI.openMainMenu(player, laboratory);
            return;
        }
        
        if (displayName.startsWith(ChatColor.GREEN + "Начать исследование: ")) {
            String researchType = displayName.replace(ChatColor.GREEN + "Начать исследование: ", "");
            startResearch(player, laboratory, researchType);
        }
    }
    
    private void handleProductionMenuClick(Player player, Laboratory laboratory, ItemStack clicked) {
        String displayName = clicked.getItemMeta().getDisplayName();
        
        if (displayName.equals(ChatColor.GRAY + "Назад")) {
            LaboratoryGUI.openMainMenu(player, laboratory);
            return;
        }
        
        if (displayName.startsWith(ChatColor.GREEN + "Создать: ")) {
            String itemType = displayName.replace(ChatColor.GREEN + "Создать: ", "");
            craftItem(player, laboratory, itemType);
        }
    }
    
    private void startResearch(Player player, Laboratory laboratory, String researchType) {
        Map<Material, Integer> requiredResources = getResearchRequirements(researchType);
        if (requiredResources == null) {
            player.sendMessage(ChatColor.RED + "Неизвестный тип исследования!");
            return;
        }
        
        // Проверяем наличие ресурсов у игрока
        for (Map.Entry<Material, Integer> entry : requiredResources.entrySet()) {
            if (!player.getInventory().contains(entry.getKey(), entry.getValue())) {
                player.sendMessage(ChatColor.RED + "Недостаточно ресурсов! Нужно: " + 
                                 entry.getKey().name() + " x" + entry.getValue());
                return;
            }
        }
        
        // Забираем ресурсы
        for (Map.Entry<Material, Integer> entry : requiredResources.entrySet()) {
            player.getInventory().removeItem(new ItemStack(entry.getKey(), entry.getValue()));
        }
        
        if (laboratory.startResearch(researchType, requiredResources)) {
            player.sendMessage(ChatColor.GREEN + "Исследование " + researchType + " начато!");
            player.closeInventory();
        } else {
            player.sendMessage(ChatColor.RED + "Не удалось начать исследование!");
        }
    }
    
    private void craftItem(Player player, Laboratory laboratory, String itemType) {
        if (!laboratory.isResearchCompleted(itemType)) {
            player.sendMessage(ChatColor.RED + "Исследование не завершено!");
            return;
        }
        
        ItemStack item = getItemByType(itemType);
        if (item == null) {
            player.sendMessage(ChatColor.RED + "Неизвестный предмет!");
            return;
        }
        
        player.getInventory().addItem(item);
        player.sendMessage(ChatColor.GREEN + "Предмет создан!");
        player.closeInventory();
    }
    
    private Map<Material, Integer> getResearchRequirements(String researchType) {
        Map<Material, Integer> requirements = new HashMap<>();
        
        switch (researchType.toLowerCase()) {
            case "hazmat_suit":
                requirements.put(Material.LEATHER, 32);
                requirements.put(Material.GLASS, 16);
                requirements.put(Material.IRON_INGOT, 8);
                break;
            case "uranium_capsule":
                requirements.put(Material.IRON_INGOT, 16);
                requirements.put(Material.GLASS, 8);
                requirements.put(Material.REDSTONE, 32);
                break;
            case "power_armor":
                requirements.put(Material.NETHERITE_INGOT, 8);
                requirements.put(Material.DIAMOND, 32);
                requirements.put(Material.REDSTONE_BLOCK, 16);
                break;
            case "auto_miner":
                requirements.put(Material.IRON_BLOCK, 16);
                requirements.put(Material.DIAMOND_PICKAXE, 1);
                requirements.put(Material.REDSTONE_BLOCK, 8);
                break;
            case "railgun":
                requirements.put(Material.NETHERITE_INGOT, 4);
                requirements.put(Material.COPPER_INGOT, 64);
                requirements.put(Material.REDSTONE_BLOCK, 32);
                break;
            case "electric_transport":
                requirements.put(Material.IRON_BLOCK, 32);
                requirements.put(Material.REDSTONE_BLOCK, 16);
                requirements.put(Material.GOLD_INGOT, 16);
                break;
            default:
                return null;
        }
        
        return requirements;
    }
    
    private ItemStack getItemByType(String itemType) {
        switch (itemType.toLowerCase()) {
            case "hazmat_suit":
                return AdvancedItems.createHazmatSuit();
            case "uranium_capsule":
                return AdvancedItems.createUraniumCapsule();
            case "power_armor":
                return AdvancedItems.createPowerArmorChestplate();
            case "auto_miner":
                return AdvancedItems.createAutoMiner();
            case "railgun":
                return AdvancedItems.createRailgun();
            case "electric_transport":
                return AdvancedItems.createElectricCar();
            default:
                return null;
        }
    }
}
