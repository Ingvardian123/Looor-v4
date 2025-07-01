package com.uraniumcraft.commands;

import com.uraniumcraft.UraniumPlugin;
import com.uraniumcraft.gui.LaboratoryGUI;
import com.uraniumcraft.items.UraniumItems;
import com.uraniumcraft.laboratory.LaboratoryTerminal;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class LaboratoryCommand implements CommandExecutor {
    
    private final UraniumPlugin plugin;
    
    public LaboratoryCommand(UraniumPlugin plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Эта команда доступна только игрокам!");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (args.length == 0) {
            // Открываем GUI лаборатории
            List<LaboratoryTerminal> terminals = plugin.getLaboratoryManager().getPlayerTerminals(player.getUniqueId());
            if (terminals.isEmpty()) {
                player.sendMessage(ChatColor.RED + "У вас нет терминалов лаборатории!");
                player.sendMessage(ChatColor.YELLOW + "Создайте терминал для проведения исследований.");
                return true;
            }
            
            LaboratoryTerminal terminal = terminals.get(0);
            LaboratoryGUI.openMainMenu(player, terminal);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "give":
                if (!player.hasPermission("uraniumcraft.admin")) {
                    player.sendMessage(ChatColor.RED + "У вас нет прав для выполнения этой команды!");
                    return true;
                }
                
                ItemStack terminal = UraniumItems.getItem("laboratory_terminal");
                if (terminal != null) {
                    player.getInventory().addItem(terminal);
                    player.sendMessage(ChatColor.GREEN + "✓ Терминал лаборатории выдан!");
                }
                break;
                
            case "info":
                showLaboratoryInfo(player);
                break;
                
            case "help":
                showHelp(player);
                break;
                
            default:
                player.sendMessage(ChatColor.RED + "Неизвестная подкоманда! Используйте /laboratory help");
                break;
        }
        
        return true;
    }
    
    private void showLaboratoryInfo(Player player) {
        List<LaboratoryTerminal> terminals = plugin.getLaboratoryManager().getPlayerTerminals(player.getUniqueId());
        
        player.sendMessage(ChatColor.GOLD + "=== Информация о лабораториях ===");
        player.sendMessage(ChatColor.YELLOW + "Терминалов: " + ChatColor.WHITE + terminals.size());
        
        if (!terminals.isEmpty()) {
            LaboratoryTerminal terminal = terminals.get(0);
            player.sendMessage(ChatColor.YELLOW + "Статус: " + ChatColor.WHITE + 
                (terminal.isActive() ? ChatColor.GREEN + "Активна" : ChatColor.RED + "Неактивна"));
            player.sendMessage(ChatColor.YELLOW + "Завершённых исследований: " + ChatColor.WHITE + 
                terminal.getCompletedResearch().size());
            player.sendMessage(ChatColor.YELLOW + "Активных исследований: " + ChatColor.WHITE + 
                terminal.getActiveResearch().size());
        }
    }
    
    private void showHelp(Player player) {
        player.sendMessage(ChatColor.GOLD + "=== Команды лаборатории ===");
        player.sendMessage(ChatColor.YELLOW + "/laboratory" + ChatColor.WHITE + " - Открыть интерфейс лаборатории");
        player.sendMessage(ChatColor.YELLOW + "/laboratory info" + ChatColor.WHITE + " - Информация о лабораториях");
        player.sendMessage(ChatColor.YELLOW + "/laboratory give" + ChatColor.WHITE + " - Получить терминал (админ)");
        player.sendMessage(ChatColor.YELLOW + "/laboratory help" + ChatColor.WHITE + " - Показать эту справку");
    }
}
