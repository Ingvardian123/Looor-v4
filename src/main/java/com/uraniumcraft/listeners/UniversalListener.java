package com.uraniumcraft.listeners;

import com.uraniumcraft.UraniumPlugin;
import com.uraniumcraft.gui.UniversalGUI;
import com.uraniumcraft.items.UniversalItems;
import com.uraniumcraft.laboratory.Laboratory;
import com.uraniumcraft.centrifuge.Centrifuge;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

public class UniversalListener implements Listener {
    private final UraniumPlugin plugin;
    private final Random random = new Random();
    
    public UniversalListener(UraniumPlugin plugin) {
        this.plugin = plugin;
    }
    
    // ==================== BLOCK EVENTS ====================
    
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItemInHand();
        
        if (item != null && item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
            String displayName = item.getItemMeta().getDisplayName();
            
            // Размещение лаборатории
            if (displayName.equals(ChatColor.LIGHT_PURPLE + "Блок лаборатории")) {
                if (!player.hasPermission("uraniumcraft.admin")) {
                    event.setCancelled(true);
                    player.sendMessage(ChatColor.RED + "У вас нет прав для создания лабораторий!");
                    return;
                }
                
                boolean created = plugin.getLaboratoryManager().createLaboratory(event.getBlock().getLocation(), player);
                if (created) {
                    player.sendMessage(ChatColor.GREEN + "✓ Лаборатория создана!");
                    player.sendMessage(ChatColor.AQUA + "Начните приносить материалы для строительства.");
                    player.playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1.0f, 1.0f);
                    
                    event.getBlock().getWorld().spawnParticle(Particle.ENCHANTMENT_TABLE, 
                        event.getBlock().getLocation().add(0.5, 1, 0.5), 20, 0.5, 0.5, 0.5, 0.1);
                    
                    showRequiredMaterials(player);
                } else {
                    event.setCancelled(true);
                    player.sendMessage(ChatColor.RED + "Здесь уже есть лаборатория!");
                }
            }
            
            // Размещение центрифуги
            else if (displayName.equals(ChatColor.GOLD + "Центрифуга")) {
                boolean created = plugin.getCentrifugeManager().createCentrifuge(event.getBlock().getLocation(), player);
                if (created) {
                    player.sendMessage(ChatColor.GREEN + "✓ Центрифуга установлена!");
                    player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_PLACE, 1.0f, 1.0f);
                } else {
                    event.setCancelled(true);
                    player.sendMessage(ChatColor.RED + "Здесь уже есть центрифуга!");
                }
            }
        }
    }
    
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        
        // Добыча урана
        if (block.getType() == Material.COAL_ORE || block.getType() == Material.DEEPSLATE_COAL_ORE) {
            if (random.nextInt(100) < 15) { // 15% шанс
                ItemStack uranium = UniversalItems.createUraniumOre();
                block.getWorld().dropItemNaturally(block.getLocation(), uranium);
                
                player.sendMessage(ChatColor.GREEN + "Вы нашли уран!");
                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.5f);
                
                // Статистика
                plugin.getPlayerStats().addUraniumMined(player.getUniqueId(), 1);
                
                // Достижения
                plugin.getAchievementManager().checkAchievement(player, "first_uranium");
                
                // Радиация
                plugin.getRadiationManager().addRadiation(player, 5);
            }
        }
        
        // Добыча компонентов
        if (block.getType() == Material.IRON_ORE || block.getType() == Material.DEEPSLATE_IRON_ORE) {
            if (random.nextInt(100) < 10) {
                ItemStack component = UniversalItems.createRadioactiveComponent();
                block.getWorld().dropItemNaturally(block.getLocation(), component);
                player.sendMessage(ChatColor.YELLOW + "Вы нашли радиоактивный компонент!");
            }
        }
    }
    
    // ==================== PLAYER INTERACTIONS ====================
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null) return;
        
        Block block = event.getClickedBlock();
        Player player = event.getPlayer();
        
        // Взаимодействие с лабораторией
        Laboratory laboratory = plugin.getLaboratoryManager().getLaboratory(block.getLocation());
        if (laboratory != null) {
            handleLaboratoryInteraction(player, laboratory, block);
            event.setCancelled(true);
            return;
        }
        
        // Взаимодействие с центрифугой
        Centrifuge centrifuge = plugin.getCentrifugeManager().getCentrifuge(block.getLocation());
        if (centrifuge != null) {
            handleCentrifugeInteraction(player, centrifuge);
            event.setCancelled(true);
            return;
        }
        
        // Использование детектора радиации
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item != null && item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
            String displayName = item.getItemMeta().getDisplayName();
            
            if (displayName.equals(ChatColor.YELLOW + "Детектор радиации")) {
                int radiation = plugin.getRadiationManager().getRadiation(player);
                player.sendMessage(ChatColor.YELLOW + "🔬 Уровень радиации: " + ChatColor.RED + radiation + " рад");
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.5f);
                event.setCancelled(true);
            }
        }
    }
    
    @EventHandler
    public void onPlayerItemConsume(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        
        if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
            String displayName = item.getItemMeta().getDisplayName();
            
            // Антирадиационная таблетка
            if (displayName.equals(ChatColor.GREEN + "Антирадиационная таблетка")) {
                plugin.getRadiationManager().removeRadiation(player, 50);
                player.sendMessage(ChatColor.GREEN + "💊 Радиация снижена!");
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_BURP, 1.0f, 1.0f);
                
                plugin.getPlayerStats().addPillsTaken(player.getUniqueId(), 1);
            }
            
            // Энергетический напиток
            else if (displayName.equals(ChatColor.BLUE + "Энергетический напиток")) {
                player.setFoodLevel(20);
                player.setSaturation(20);
                player.sendMessage(ChatColor.BLUE + "⚡ Энергия восстановлена!");
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_BURP, 1.0f, 1.2f);
            }
        }
    }
    
    // ==================== GUI EVENTS ====================
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        
        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();
        
        // Обработка всех GUI
        if (title.startsWith(ChatColor.DARK_PURPLE.toString()) || 
            title.startsWith(ChatColor.GOLD.toString()) ||
            title.startsWith(ChatColor.BLUE.toString()) ||
            title.startsWith(ChatColor.GREEN.toString())) {
            
            UniversalGUI.handleClick(player, event, plugin);
        }
    }
    
    // ==================== HELPER METHODS ====================
    
    private void handleLaboratoryInteraction(Player player, Laboratory laboratory, Block block) {
        if (!laboratory.getOwner().equals(player.getUniqueId()) && !player.hasPermission("uraniumcraft.admin")) {
            player.sendMessage(ChatColor.RED + "Это не ваша лаборатория!");
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);
            return;
        }
        
        if (laboratory.getState() == Laboratory.LaboratoryState.COMPLETED) {
            UniversalGUI.openLaboratoryMenu(player, laboratory);
            player.playSound(player.getLocation(), Sound.BLOCK_BEACON_AMBIENT, 1.0f, 1.0f);
        } else {
            showConstructionStatus(player, laboratory);
            
            // Добавление материалов
            ItemStack handItem = player.getInventory().getItemInMainHand();
            if (handItem != null && handItem.getType() != Material.AIR) {
                Material material = handItem.getType();
                int amount = handItem.getAmount();
                
                if (laboratory.addMaterial(material, amount)) {
                    player.getInventory().setItemInMainHand(null);
                    player.sendMessage(ChatColor.GREEN + "✓ Материал добавлен!");
                    player.sendMessage(ChatColor.YELLOW + "Прогресс строительства: " + 
                                     laboratory.getConstructionProgress() + "%");
                    
                    player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1.0f, 1.5f);
                    block.getWorld().spawnParticle(Particle.CRIT, 
                        block.getLocation().add(0.5, 1, 0.5), 10, 0.3, 0.3, 0.3, 0.1);
                    
                    if (laboratory.getState() == Laboratory.LaboratoryState.COMPLETED) {
                        player.sendMessage(ChatColor.GOLD + "🎉 Лаборатория построена!");
                        player.sendMessage(ChatColor.AQUA + "Теперь вы можете проводить исследования!");
                        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
                        
                        block.getWorld().spawnParticle(Particle.FIREWORKS_SPARK, 
                            block.getLocation().add(0.5, 2, 0.5), 50, 1, 1, 1, 0.2);
                    }
                } else {
                    player.sendMessage(ChatColor.GRAY + "Этот материал не нужен для строительства.");
                }
            }
        }
    }
    
    private void handleCentrifugeInteraction(Player player, Centrifuge centrifuge) {
        if (!centrifuge.getOwner().equals(player.getUniqueId()) && !player.hasPermission("uraniumcraft.admin")) {
            player.sendMessage(ChatColor.RED + "Это не ваша центрифуга!");
            return;
        }
        
        UniversalGUI.openCentrifugeMenu(player, centrifuge);
        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_AMBIENT, 1.0f, 1.2f);
    }
    
    private void showRequiredMaterials(Player player) {
        player.sendMessage(ChatColor.GOLD + "═══════════════════════════════");
        player.sendMessage(ChatColor.YELLOW + "📋 Необходимые материалы для строительства:");
        player.sendMessage(ChatColor.WHITE + "• " + ChatColor.GRAY + "Железные блоки: " + ChatColor.YELLOW + "64");
        player.sendMessage(ChatColor.WHITE + "• " + ChatColor.RED + "Редстоун блоки: " + ChatColor.YELLOW + "32");
        player.sendMessage(ChatColor.WHITE + "• " + ChatColor.AQUA + "Алмазные блоки: " + ChatColor.YELLOW + "16");
        player.sendMessage(ChatColor.WHITE + "• " + ChatColor.GREEN + "Изумрудные блоки: " + ChatColor.YELLOW + "8");
        player.sendMessage(ChatColor.WHITE + "• " + ChatColor.LIGHT_PURPLE + "Маяки: " + ChatColor.YELLOW + "4");
        player.sendMessage(ChatColor.WHITE + "• " + ChatColor.DARK_PURPLE + "Звёзды Нижнего мира: " + ChatColor.YELLOW + "2");
        player.sendMessage(ChatColor.GOLD + "═══════════════════════════════");
        player.sendMessage(ChatColor.GRAY + "Держите материалы в руке и кликните по блоку лаборатории");
    }
    
    private void showConstructionStatus(Player player, Laboratory laboratory) {
        int progress = laboratory.getConstructionProgress();
        
        player.sendMessage(ChatColor.AQUA + "🏗️ Статус строительства лаборатории:");
        
        // Прогресс-бар
        StringBuilder progressBar = new StringBuilder();
        progressBar.append(ChatColor.GREEN);
        int filledBars = progress / 5;
        for (int i = 0; i < 20; i++) {
            if (i < filledBars) {
                progressBar.append("█");
            } else if (i == filledBars && progress % 5 > 2) {
                progressBar.append("▌");
            } else {
                progressBar.append(ChatColor.GRAY).append("░");
            }
        }
        
        player.sendMessage(ChatColor.WHITE + "Прогресс: " + progressBar.toString() + 
                         ChatColor.WHITE + " " + progress + "%");
        
        var required = laboratory.getRequiredMaterials();
        if (!required.isEmpty()) {
            player.sendMessage(ChatColor.YELLOW + "📦 Ещё нужно:");
            for (var entry : required.entrySet()) {
                if (entry.getValue() > 0) {
                    String materialName = getMaterialDisplayName(entry.getKey());
                    player.sendMessage(ChatColor.WHITE + "• " + materialName + ": " + 
                                     ChatColor.YELLOW + entry.getValue());
                }
            }
        }
    }
    
    private String getMaterialDisplayName(Material material) {
        switch (material) {
            case IRON_BLOCK: return ChatColor.GRAY + "Железные блоки";
            case REDSTONE_BLOCK: return ChatColor.RED + "Редстоун блоки";
            case DIAMOND_BLOCK: return ChatColor.AQUA + "Алмазные блоки";
            case EMERALD_BLOCK: return ChatColor.GREEN + "Изумрудные блоки";
            case BEACON: return ChatColor.LIGHT_PURPLE + "Маяки";
            case NETHER_STAR: return ChatColor.DARK_PURPLE + "Звёзды Нижнего мира";
            default: return material.name();
        }
    }
}
