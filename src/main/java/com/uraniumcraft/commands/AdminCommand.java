package com.uraniumcraft.commands;

import com.uraniumcraft.UraniumPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AdminCommand implements CommandExecutor {
    
    private final UraniumPlugin plugin;
    
    public AdminCommand(UraniumPlugin plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("uraniumcraft.admin")) {
            sender.sendMessage(ChatColor.RED + "Unknown command. Type \"/help\" for help.");
            return true;
        }
        
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /ucadmin authorize <player>");
            return true;
        }
        
        if (args[0].equalsIgnoreCase("authorize")) {
            Player target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                sender.sendMessage(ChatColor.RED + "Player not found!");
                return true;
            }
            
            // Добавляем разрешение через LuckPerms или встроенную систему
            plugin.getEnhancedLaboratoryManager().authorizePlayer(target.getUniqueId());
            
            sender.sendMessage(ChatColor.GREEN + "Player " + target.getName() + " has been authorized for laboratories!");
            target.sendMessage(ChatColor.GREEN + "You have been authorized to create laboratories!");
            
            return true;
        }
        
        sender.sendMessage(ChatColor.RED + "Unknown subcommand!");
        return true;
    }
}
