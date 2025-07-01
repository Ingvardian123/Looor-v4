package com.uraniumcraft.commands;

import com.uraniumcraft.items.UraniumItems;
import com.uraniumcraft.items.AdvancedItems;
import com.uraniumcraft.items.AdvancedResearchItems;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class GiveUraniumCommand implements CommandExecutor {
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Эта команда доступна только игрокам!");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (!player.hasPermission("uranium.give")) {
            player.sendMessage(ChatColor.RED + "У вас нет прав для использования этой команды!");
            return true;
        }
        
        if (args.length < 1) {
            player.sendMessage(ChatColor.YELLOW + "Использование: /giveuranium <предмет> [количество]");
            player.sendMessage(ChatColor.GRAY + "Доступные предметы:");
            player.sendMessage(ChatColor.AQUA + "Базовые: uranium_ore, uranium_ingot, enriched_uranium, uranium_fuel_rod, depleted_uranium, uranium_dust, radiation_detector, anti_radiation_pills");
            player.sendMessage(ChatColor.GOLD + "Продвинутые: hazmat_suit, geiger_counter, uranium_generator, centrifuge_core, radiation_shield");
            player.sendMessage(ChatColor.LIGHT_PURPLE + "Исследовательские: quantum_processor, teleporter, healing_chamber, advanced_hazmat_suit, radiation_neutralizer, energy_amplifier, bio_scanner, quantum_reactor, nano_repair_kit, holographic_display");
            return true;
        }
        
        String itemName = args[0].toLowerCase();
        int amount = 1;
        
        if (args.length >= 2) {
            try {
                amount = Integer.parseInt(args[1]);
                if (amount <= 0 || amount > 64) {
                    player.sendMessage(ChatColor.RED + "Количество должно быть от 1 до 64!");
                    return true;
                }
            } catch (NumberFormatException e) {
                player.sendMessage(ChatColor.RED + "Неверное количество!");
                return true;
            }
        }
        
        ItemStack item = getItemByName(itemName);
        
        if (item == null) {
            player.sendMessage(ChatColor.RED + "Предмет '" + itemName + "' не найден!");
            return true;
        }
        
        item.setAmount(amount);
        player.getInventory().addItem(item);
        player.sendMessage(ChatColor.GREEN + "Вы получили " + amount + "x " + item.getItemMeta().getDisplayName());
        
        return true;
    }
    
    private ItemStack getItemByName(String name) {
        // Базовые предметы
        ItemStack basicItem = UraniumItems.getItem(name);
        if (basicItem != null) {
            return basicItem;
        }
        
        // Продвинутые предметы
        ItemStack advancedItem = AdvancedItems.getItem(name);
        if (advancedItem != null) {
            return advancedItem;
        }
        
        // Исследовательские предметы
        ItemStack researchItem = AdvancedResearchItems.get(name);
        if (researchItem != null) {
            return researchItem;
        }
        
        return null;
    }
}
