package com.uraniumcraft.commands;

import com.uraniumcraft.UraniumPlugin;
import com.uraniumcraft.gui.TeleporterGUI;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TpCommand implements CommandExecutor {
    
    private final UraniumPlugin plugin;
    
    public TpCommand(UraniumPlugin plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Эта команда доступна только игрокам!");
            return true;
        }
        
        Player player = (Player) sender;
        
        // Открываем меню телепортации для обычных игроков
        TeleporterGUI.openPlayerTeleporterMenu(player);
        
        return true;
    }
}
