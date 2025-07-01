package com.uraniumcraft.commands;

import com.uraniumcraft.UraniumPlugin;
import com.uraniumcraft.laboratory.EnhancedLaboratory;
import com.uraniumcraft.laboratory.EnhancedLaboratoryManager;
import com.uraniumcraft.laboratory.LaboratorySpecialization;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;

public class EnhancedLaboratoryCommand implements CommandExecutor {
    private final UraniumPlugin plugin;
    private final EnhancedLaboratoryManager laboratoryManager;
    
    public EnhancedLaboratoryCommand(UraniumPlugin plugin, EnhancedLaboratoryManager laboratoryManager) {
        this.plugin = plugin;
        this.laboratoryManager = laboratoryManager;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("uranium.laboratory.admin")) {
            sender.sendMessage(ChatColor.RED + "У вас нет прав для использования этой команды!");
            return true;
        }
        
        if (args.length < 1) {
            showHelp(sender);
            return true;
        }
        
        String action = args[0].toLowerCase();
        
        switch (action) {
            case "authorize":
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Укажите имя игрока!");
                    return true;
                }
                
                Player targetPlayer = plugin.getServer().getPlayer(args[1]);
                if (targetPlayer == null) {
                    sender.sendMessage(ChatColor.RED + "Игрок не найден!");
                    return true;
                }
                
                laboratoryManager.authorizePlayer(targetPlayer.getUniqueId());
                sender.sendMessage(ChatColor.GREEN + "Игрок " + targetPlayer.getName() + " авторизован для создания лабораторий!");
                targetPlayer.sendMessage(ChatColor.GREEN + "Вы получили доступ к созданию лабораторий!");
                break;
                
            case "unauthorize":
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Укажите имя игрока!");
                    return true;
                }
                
                Player targetPlayer2 = plugin.getServer().getPlayer(args[1]);
                if (targetPlayer2 == null) {
                    sender.sendMessage(ChatColor.RED + "Игрок не найден!");
                    return true;
                }
                
                laboratoryManager.unauthorizePlayer(targetPlayer2.getUniqueId());
                sender.sendMessage(ChatColor.YELLOW + "У игрока " + targetPlayer2.getName() + " отозван доступ к лабораториям!");
                targetPlayer2.sendMessage(ChatColor.YELLOW + "Ваш доступ к созданию лабораторий отозван!");
                break;
                
            case "list":
                showAuthorizedPlayers(sender);
                break;
                
            case "stats":
                showStatistics(sender);
                break;
                
            case "info":
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Укажите имя игрока!");
                    return true;
                }
                
                Player infoPlayer = plugin.getServer().getPlayer(args[1]);
                if (infoPlayer == null) {
                    sender.sendMessage(ChatColor.RED + "Игрок не найден!");
                    return true;
                }
                
                showPlayerInfo(sender, infoPlayer);
                break;
                
            default:
                showHelp(sender);
                break;
        }
        
        return true;
    }
    
    private void showHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "═══════════════════════════════");
        sender.sendMessage(ChatColor.YELLOW + "📋 Команды управления лабораториями:");
        sender.sendMessage(ChatColor.WHITE + "/laboratory authorize <игрок> - Авторизовать игрока");
        sender.sendMessage(ChatColor.WHITE + "/laboratory unauthorize <игрок> - Убрать авторизацию");
        sender.sendMessage(ChatColor.WHITE + "/laboratory list - Список авторизованных игроков");
        sender.sendMessage(ChatColor.WHITE + "/laboratory stats - Общая статистика");
        sender.sendMessage(ChatColor.WHITE + "/laboratory info <игрок> - Информация о лабораториях игрока");
        sender.sendMessage(ChatColor.GOLD + "═══════════════════════════════");
    }
    
    private void showAuthorizedPlayers(CommandSender sender) {
        sender.sendMessage(ChatColor.AQUA + "Авторизованные игроки:");
        boolean hasAuthorized = false;
        
        for (java.util.UUID playerId : laboratoryManager.getAuthorizedPlayers()) {
            Player player = plugin.getServer().getPlayer(playerId);
            if (player != null) {
                int labCount = laboratoryManager.getPlayerLaboratories(playerId).size();
                sender.sendMessage(ChatColor.WHITE + "- " + player.getName() + 
                    " (онлайн, лабораторий: " + ChatColor.YELLOW + labCount + ChatColor.WHITE + ")");
                hasAuthorized = true;
            } else {
                sender.sendMessage(ChatColor.GRAY + "- " + playerId.toString() + " (оффлайн)");
                hasAuthorized = true;
            }
        }
        
        if (!hasAuthorized) {
            sender.sendMessage(ChatColor.GRAY + "Нет авторизованных игроков");
        }
    }
    
    private void showStatistics(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "═══════════════════════════════");
        sender.sendMessage(ChatColor.AQUA + "📊 Статистика лабораторий:");
        sender.sendMessage(ChatColor.WHITE + "Всего лабораторий: " + ChatColor.YELLOW + laboratoryManager.getTotalLaboratories());
        sender.sendMessage(ChatColor.WHITE + "Работающих: " + ChatColor.GREEN + laboratoryManager.getOperationalLaboratories());
        sender.sendMessage(ChatColor.WHITE + "Активных исследований: " + ChatColor.BLUE + laboratoryManager.getTotalActiveResearch());
        sender.sendMessage(ChatColor.WHITE + "Авторизованных игроков: " + ChatColor.PURPLE + laboratoryManager.getAuthorizedPlayers().size());
        
        sender.sendMessage(ChatColor.YELLOW + "Специализации:");
        Map<LaboratorySpecialization, Integer> specStats = laboratoryManager.getSpecializationStats();
        for (Map.Entry<LaboratorySpecialization, Integer> entry : specStats.entrySet()) {
            if (entry.getValue() > 0) {
                sender.sendMessage(ChatColor.WHITE + "- " + entry.getKey().getName() + ": " + 
                    ChatColor.CYAN + entry.getValue());
            }
        }
        sender.sendMessage(ChatColor.GOLD + "═══════════════════════════════");
    }
    
    private void showPlayerInfo(CommandSender sender, Player player) {
        var playerLabs = laboratoryManager.getPlayerLaboratories(player.getUniqueId());
        
        sender.sendMessage(ChatColor.AQUA + "Информация о лабораториях игрока " + player.getName() + ":");
        
        if (playerLabs.isEmpty()) {
            sender.sendMessage(ChatColor.GRAY + "У игрока нет лабораторий");
            return;
        }
        
        for (int i = 0; i < playerLabs.size(); i++) {
            EnhancedLaboratory lab = playerLabs.get(i);
            sender.sendMessage(ChatColor.YELLOW + "Лаборатория #" + (i + 1) + ":");
            sender.sendMessage(ChatColor.WHITE + "- Уровень: " + ChatColor.GOLD + lab.getLevel().getLevel() + 
                " (" + lab.getLevel().getName() + ")");
            sender.sendMessage(ChatColor.WHITE + "- Специализация: " + ChatColor.BLUE + lab.getSpecialization().getName());
            sender.sendMessage(ChatColor.WHITE + "- Статус: " + getStatusColor(lab) + lab.getState().name());
            sender.sendMessage(ChatColor.WHITE + "- Энергия: " + ChatColor.RED + lab.getCurrentEnergy() + 
                "/" + lab.getMaxEnergy());
            sender.sendMessage(ChatColor.WHITE + "- Исследований: " + ChatColor.GREEN + 
                lab.getCompletedResearch().size() + " завершено, " + 
                ChatColor.YELLOW + lab.getActiveResearch().size() + " активно");
        }
    }
    
    private ChatColor getStatusColor(EnhancedLaboratory laboratory) {
        switch (laboratory.getState()) {
            case OPERATIONAL:
                return ChatColor.GREEN;
            case UNDER_CONSTRUCTION:
                return ChatColor.YELLOW;
            case UPGRADING:
                return ChatColor.GOLD;
            case MAINTENANCE:
                return ChatColor.RED;
            case BLUEPRINT:
                return ChatColor.GRAY;
            default:
                return ChatColor.WHITE;
        }
    }
}
