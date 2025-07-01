package com.uraniumcraft.listeners;

import com.uraniumcraft.UraniumPlugin;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class PowerArmorListener implements Listener {
    
    private final UraniumPlugin plugin;
    
    public PowerArmorListener(UraniumPlugin plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        
        if (player.isSneaking() && event.getAction().name().contains("RIGHT_CLICK")) {
            ItemStack item = player.getInventory().getItemInMainHand();
            
            if (isPowerArmorPiece(item)) {
                handlePowerArmorModeSwitch(player, item);
                event.setCancelled(true);
            } else if (isRailgun(item)) {
                handleRailgunModeSwitch(player, item);
                event.setCancelled(true);
            }
        }
    }
    
    private boolean isPowerArmorPiece(ItemStack item) {
        return item != null && item.hasItemMeta() && 
               item.getItemMeta().hasDisplayName() &&
               item.getItemMeta().getDisplayName().contains("силовой брони");
    }
    
    private boolean isRailgun(ItemStack item) {
        return item != null && item.hasItemMeta() && 
               item.getItemMeta().hasDisplayName() &&
               item.getItemMeta().getDisplayName().contains("Рельсотрон");
    }
    
    private void handlePowerArmorModeSwitch(Player player, ItemStack item) {
        String displayName = item.getItemMeta().getDisplayName();
        
        if (displayName.contains("Шлем")) {
            switchHelmetMode(player);
        } else if (displayName.contains("Нагрудник")) {
            switchChestplateMode(player);
        } else if (displayName.contains("Поножи")) {
            switchLeggingsMode(player);
        } else if (displayName.contains("Ботинки")) {
            switchBootsMode(player);
        }
        
        // Эффекты переключения режима
        player.spawnParticle(Particle.ELECTRIC_SPARK, player.getLocation().add(0, 1, 0), 10, 0.5, 0.5, 0.5, 0.1);
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.5f);
    }
    
    private void switchHelmetMode(Player player) {
        player.sendMessage(ChatColor.AQUA + "🔄 Режим шлема переключен: Ночное видение");
        player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 1200, 0)); // 1 минута
    }
    
    private void switchChestplateMode(Player player) {
        player.sendMessage(ChatColor.AQUA + "🔄 Режим нагрудника переключен: Регенерация");
        player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 600, 1)); // 30 секунд
    }
    
    private void switchLeggingsMode(Player player) {
        player.sendMessage(ChatColor.AQUA + "🔄 Режим поножей переключен: Скорость");
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 1200, 1)); // 1 минута
    }
    
    private void switchBootsMode(Player player) {
        player.sendMessage(ChatColor.AQUA + "🔄 Режим ботинок переключен: Прыжки");
        player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 1200, 1)); // 1 минута
    }
    
    private void handleRailgunModeSwitch(Player player, ItemStack item) {
        player.sendMessage(ChatColor.RED + "⚡ Режим рельсотрона переключен: Мощный выстрел");
        
        // Эффекты зарядки рельсотрона
        player.spawnParticle(Particle.REDSTONE, player.getLocation().add(0, 1, 0), 20, 
            0.5, 0.5, 0.5, 0, new Particle.DustOptions(org.bukkit.Color.BLUE, 1.5f));
        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1.0f, 2.0f);
    }
}
