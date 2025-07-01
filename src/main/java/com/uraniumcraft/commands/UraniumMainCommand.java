package com.uraniumcraft.commands;

import com.uraniumcraft.UraniumPlugin;
import com.uraniumcraft.gui.MainGUI;
import com.uraniumcraft.items.GuideBook;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class UraniumMainCommand implements CommandExecutor {
    
    private final UraniumPlugin plugin;
    
    public UraniumMainCommand(UraniumPlugin plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Эта команда только для игроков!");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (args.length == 0) {
            // Открываем главное меню
            MainGUI.openMainMenu(player, plugin);
            player.playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 1.0f, 1.0f);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "guide":
            case "help":
                ItemStack guide = GuideBook.createGuideBook();
                player.getInventory().addItem(guide);
                player.sendMessage(ChatColor.GREEN + "✓ Руководство UraniumCraft получено!");
                player.playSound(player.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 1.0f, 1.0f);
                
                if (args.length > 1 && args[1].equalsIgnoreCase("help")) {
                    showQuickHelp(player);
                }
                break;
                
            case "menu":
            case "gui":
                MainGUI.openMainMenu(player, plugin);
                player.playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 1.0f, 1.0f);
                break;
                
            case "info":
                showPluginInfo(player);
                break;
                
            default:
                player.sendMessage(ChatColor.YELLOW + "Использование:");
                player.sendMessage(ChatColor.WHITE + "/uranium - Открыть главное меню");
                player.sendMessage(ChatColor.WHITE + "/uranium guide - Получить руководство");
                player.sendMessage(ChatColor.WHITE + "/uranium menu - Открыть GUI");
                player.sendMessage(ChatColor.WHITE + "/uranium info - Информация о плагине");
                break;
        }
        
        return true;
    }
    
    private void showQuickHelp(Player player) {
        player.sendMessage(ChatColor.GOLD + "═══════════════════════════════");
        player.sendMessage(ChatColor.YELLOW + "🚀 Быстрый старт UraniumCraft:");
        player.sendMessage(ChatColor.WHITE + "1. Добудьте урановую руду");
        player.sendMessage(ChatColor.WHITE + "2. Переплавьте в урановые слитки");
        player.sendMessage(ChatColor.WHITE + "3. Носите защитную броню");
        player.sendMessage(ChatColor.WHITE + "4. Постройте центрифугу для пыли");
        player.sendMessage(ChatColor.WHITE + "5. Получите авторизацию для лабораторий");
        player.sendMessage(ChatColor.WHITE + "6. Исследуйте продвинутые предметы");
        player.sendMessage(ChatColor.GOLD + "═══════════════════════════════");
    }
    
    private void showPluginInfo(Player player) {
        player.sendMessage(ChatColor.AQUA + "═══════════════════════════════");
        player.sendMessage(ChatColor.GOLD + "📋 UraniumCraft v2.0.0");
        player.sendMessage(ChatColor.GRAY + "Автор: UraniumCraft Team");
        player.sendMessage(ChatColor.YELLOW + "Возможности:");
        player.sendMessage(ChatColor.WHITE + "• Система радиации");
        player.sendMessage(ChatColor.WHITE + "• Центрифуга для переработки");
        player.sendMessage(ChatColor.WHITE + "• Лаборатории и исследования");
        player.sendMessage(ChatColor.WHITE + "• Продвинутые предметы");
        player.sendMessage(ChatColor.WHITE + "• GUI интерфейсы");
        player.sendMessage(ChatColor.WHITE + "• Система достижений");
        player.sendMessage(ChatColor.GREEN + "Используйте /uranium для доступа ко всем функциям!");
        player.sendMessage(ChatColor.AQUA + "═══════════════════════════════");
    }
}
