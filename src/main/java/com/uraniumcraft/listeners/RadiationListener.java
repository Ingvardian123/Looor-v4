package com.uraniumcraft.listeners;

import com.uraniumcraft.UraniumPlugin;
import com.uraniumcraft.items.UraniumItems;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;

public class RadiationListener implements Listener {
    
    private final UraniumPlugin plugin;
    
    public RadiationListener(UraniumPlugin plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerPickupItem(PlayerPickupItemEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem().getItemStack();
        
        int radiation = UraniumItems.getItemRadiation(item);
        if (radiation > 0) {
            plugin.getRadiationManager().addRadiation(player, radiation * item.getAmount());
        }
    }
    
    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItemDrop().getItemStack();
        
        int radiation = UraniumItems.getItemRadiation(item);
        if (radiation > 0) {
            plugin.getRadiationManager().removeRadiation(player, radiation * item.getAmount());
        }
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        
        Player player = (Player) event.getWhoClicked();
        ItemStack item = event.getCurrentItem();
        
        if (item != null && UraniumItems.isUraniumItem(item)) {
            // Проверяем, надевает ли игрок защитную броню
            if (isArmorSlot(event.getSlot()) && event.getInventory().equals(player.getInventory())) {
                if (isProtectiveArmor(item)) {
                    player.sendMessage(ChatColor.GREEN + "✓ Защита от радиации активирована!");
                }
            }
        }
    }
    
    private boolean isArmorSlot(int slot) {
        return slot >= 36 && slot <= 39; // Слоты брони в инвентаре игрока
    }
    
    private boolean isProtectiveArmor(ItemStack item) {
        if (!item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) return false;
        
        String displayName = item.getItemMeta().getDisplayName();
        return displayName.contains("химзащиты") || displayName.contains("силовой брони");
    }
}
