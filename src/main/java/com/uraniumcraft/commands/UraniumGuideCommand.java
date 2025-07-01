package com.uraniumcraft.commands;

import com.uraniumcraft.UraniumPlugin;
import com.uraniumcraft.items.GuideBook;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class UraniumGuideCommand implements CommandExecutor {
    
    private final UraniumPlugin plugin;
    
    public UraniumGuideCommand(UraniumPlugin plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Эта команда только для игроков!");
            return true;
        }
        
        Player player = (Player) sender;
        
        // Проверяем разрешения
        if (!player.hasPermission("uranium.guide")) {
            player.sendMessage(ChatColor.RED + "У вас нет прав для получения руководства!");
            return true;
        }
        
        // Создаём и выдаём гайд-бук
        ItemStack guideBook = GuideBook.createGuideBook();
        
        // Проверяем, есть ли место в инвентаре
        if (player.getInventory().firstEmpty() == -1) {
            player.sendMessage(ChatColor.RED + "В вашем инвентаре нет места!");
            player.sendMessage(ChatColor.YELLOW + "Освободите место и попробуйте снова.");
            return true;
        }
        
        // Выдаём книгу
        player.getInventory().addItem(guideBook);
        
        // Сообщения и эффекты
        player.sendMessage(ChatColor.GREEN + "✓ Вы получили руководство UraniumCraft!");
        player.sendMessage(ChatColor.AQUA + "📖 Откройте книгу для изучения всех функций плагина.");
        player.sendMessage(ChatColor.YELLOW + "💡 Сохраните книгу - она содержит важную информацию!");
        
        // Звуковые эффекты
        player.playSound(player.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 1.0f, 1.0f);
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.2f);
        
        // Дополнительная информация для новых игроков
        if (args.length > 0 && args[0].equalsIgnoreCase("help")) {
            player.sendMessage(ChatColor.GOLD + "═══════════════════════════════");
            player.sendMessage(ChatColor.YELLOW + "📚 Быстрая справка:");
            player.sendMessage(ChatColor.WHITE + "• Урановые предметы радиоактивны");
            player.sendMessage(ChatColor.WHITE + "• Используйте защитную броню");
            player.sendMessage(ChatColor.WHITE + "• Стройте лаборатории для исследований");
            player.sendMessage(ChatColor.WHITE + "• Центрифуга производит урановую пыль");
            player.sendMessage(ChatColor.WHITE + "• Все детали в руководстве!");
            player.sendMessage(ChatColor.GOLD + "═══════════════════════════════");
        }
        
        return true;
    }
}
