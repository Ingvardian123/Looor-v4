package com.uraniumcraft.listeners;

import com.uraniumcraft.UraniumPlugin;
import com.uraniumcraft.items.UraniumItems;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;

public class UraniumListener implements Listener {
    
    private final UraniumPlugin plugin;
    
    public UraniumListener(UraniumPlugin plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItemInHand();
        
        if (item != null && item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
            String displayName = item.getItemMeta().getDisplayName();
            
            if (displayName.equals(ChatColor.GREEN + "Урановый блок")) {
                player.sendMessage(ChatColor.YELLOW + "⚠ Урановый блок излучает радиацию!");
                plugin.getRadiationManager().addRadiation(player, 5);
            }
        }
    }
    
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        Player player = event.getPlayer();
        
        // Проверяем, ломает ли игрок урановый блок
        if (block.getType() == Material.EMERALD_BLOCK) {
            // Дополнительная проверка через NBT или другие способы
            // Пока просто добавляем радиацию при ломании изумрудных блоков
            player.sendMessage(ChatColor.RED + "☢ Получена радиация от разрушения уранового блока!");
            plugin.getRadiationManager().addRadiation(player, 3);
        }
    }
}
