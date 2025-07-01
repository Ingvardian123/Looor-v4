package com.uraniumcraft.listeners;

import com.uraniumcraft.UraniumPlugin;
import com.uraniumcraft.gui.LaboratoryGUI;
import com.uraniumcraft.items.UraniumItems;
import com.uraniumcraft.laboratory.LaboratoryManager;
import com.uraniumcraft.laboratory.LaboratoryTerminal;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class LaboratoryListener implements Listener {
    private final UraniumPlugin plugin;
    private final LaboratoryManager laboratoryManager;
    
    public LaboratoryListener(UraniumPlugin plugin, LaboratoryManager laboratoryManager) {
        this.plugin = plugin;
        this.laboratoryManager = laboratoryManager;
    }
    
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItemInHand();
        
        if (item != null && item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
            String displayName = item.getItemMeta().getDisplayName();
            
            if (displayName.equals(ChatColor.LIGHT_PURPLE + "Терминал лаборатории")) {
                boolean created = laboratoryManager.createTerminal(event.getBlock().getLocation(), player);
                if (created) {
                    player.sendMessage(ChatColor.GREEN + "✓ Терминал лаборатории создан!");
                    player.sendMessage(ChatColor.AQUA + "Принесите материалы для активации:");
                    player.sendMessage(ChatColor.WHITE + "• Железные блоки x16");
                    player.sendMessage(ChatColor.WHITE + "• Редстоун блоки x8");
                    player.sendMessage(ChatColor.WHITE + "• Алмазы x4");
                    player.sendMessage(ChatColor.WHITE + "• Изумруды x2");
                    player.playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1.0f, 1.0f);
                    
                    // Эффекты
                    event.getBlock().getWorld().spawnParticle(Particle.ENCHANTMENT_TABLE, 
                        event.getBlock().getLocation().add(0.5, 1, 0.5), 20, 0.5, 0.5, 0.5, 0.1);
                } else {
                    event.setCancelled(true);
                    player.sendMessage(ChatColor.RED + "Здесь уже есть терминал!");
                }
            }
        }
    }
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null) return;
        
        Block block = event.getClickedBlock();
        LaboratoryTerminal terminal = laboratoryManager.getTerminal(block.getLocation());
        
        if (terminal != null) {
            Player player = event.getPlayer();
            
            if (!terminal.getOwner().equals(player.getUniqueId())) {
                player.sendMessage(ChatColor.RED + "Это не ваш терминал!");
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);
                return;
            }
            
            if (terminal.isActive()) {
                // Открываем GUI терминала
                LaboratoryGUI.openMainMenu(player, terminal);
                player.playSound(player.getLocation(), Sound.BLOCK_BEACON_AMBIENT, 1.0f, 1.0f);
            } else {
                // Проверяем, есть ли у игрока нужные материалы
                ItemStack handItem = player.getInventory().getItemInMainHand();
                if (handItem != null && handItem.getType() != Material.AIR) {
                    Material material = handItem.getType();
                    int amount = handItem.getAmount();
                    
                    if (terminal.addMaterial(material, amount)) {
                        player.getInventory().setItemInMainHand(null);
                        player.sendMessage(ChatColor.GREEN + "✓ Материал добавлен!");
                        player.sendMessage(ChatColor.YELLOW + "Прогресс активации: " + 
                                         terminal.getActivationProgress() + "%");
                        
                        // Звуковые эффекты
                        player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1.0f, 1.5f);
                        
                        // Частицы
                        block.getWorld().spawnParticle(Particle.CRIT, 
                            block.getLocation().add(0.5, 1, 0.5), 10, 0.3, 0.3, 0.3, 0.1);
                        
                        if (terminal.isActive()) {
                            player.sendMessage(ChatColor.GOLD + "🎉 Терминал лаборатории активирован!");
                            player.sendMessage(ChatColor.AQUA + "Теперь вы можете проводить исследования!");
                            player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
                            
                            // Эффекты активации
                            block.getWorld().spawnParticle(Particle.FIREWORKS_SPARK, 
                                block.getLocation().add(0.5, 2, 0.5), 50, 1, 1, 1, 0.2);
                        }
                    } else {
                        player.sendMessage(ChatColor.GRAY + "Этот материал не нужен для активации.");
                    }
                } else {
                    // Показываем статус
                    showTerminalStatus(player, terminal);
                }
            }
            
            event.setCancelled(true);
        }
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        
        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();
        
        if (!title.equals(ChatColor.DARK_PURPLE + "Терминал лаборатории")) return;
        
        event.setCancelled(true);
        
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;
        
        String displayName = clicked.getItemMeta().getDisplayName();
        
        if (displayName.startsWith(ChatColor.GREEN + "Начать: ")) {
            String researchName = displayName.replace(ChatColor.GREEN + "Начать: ", "");
            String researchType = getResearchTypeByName(researchName);
            
            // Находим терминал игрока
            LaboratoryTerminal terminal = findPlayerTerminal(player);
            if (terminal != null && terminal.startResearch(researchType)) {
                player.sendMessage(ChatColor.GREEN + "Исследование '" + researchName + "' начато!");
                player.closeInventory();
                player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1.0f, 1.2f);
            } else {
                player.sendMessage(ChatColor.RED + "Не удалось начать исследование!");
            }
        }
    }
    
    private void showTerminalStatus(Player player, LaboratoryTerminal terminal) {
        player.sendMessage(ChatColor.AQUA + "🔬 Статус терминала лаборатории:");
        player.sendMessage(ChatColor.WHITE + "Активация: " + terminal.getActivationProgress() + "%");
        
        if (!terminal.isActive()) {
            player.sendMessage(ChatColor.YELLOW + "📦 Ещё нужно:");
            for (Material material : terminal.getRequiredMaterials().keySet()) {
                int required = terminal.getRequiredMaterials().get(material);
                if (required > 0) {
                    String materialName = getMaterialDisplayName(material);
                    player.sendMessage(ChatColor.WHITE + "• " + materialName + ": " + 
                                     ChatColor.YELLOW + required);
                }
            }
        }
    }
    
    private String getMaterialDisplayName(Material material) {
        switch (material) {
            case IRON_BLOCK: return ChatColor.GRAY + "Железные блоки";
            case REDSTONE_BLOCK: return ChatColor.RED + "Редстоун блоки";
            case DIAMOND: return ChatColor.AQUA + "Алмазы";
            case EMERALD: return ChatColor.GREEN + "Изумруды";
            default: return material.name();
        }
    }
    
    private String getResearchTypeByName(String researchName) {
        switch (researchName) {
            case "Шлем химзащиты": return "hazmat_helmet";
            case "Костюм химзащиты": return "hazmat_suit";
            case "Шлем силовой брони": return "power_armor_helmet";
            case "Нагрудник силовой брони": return "power_armor_chestplate";
            case "Поножи силовой брони": return "power_armor_leggings";
            case "Ботинки силовой брони": return "power_armor_boots";
            case "Рельсотрон": return "railgun";
            case "Урановый планшет": return "uranium_tablet";
            case "Ядро телепорта": return "teleporter_core";
            case "Ядро центрифуги": return "centrifuge_core";
            default: return researchName.toLowerCase();
        }
    }
    
    private LaboratoryTerminal findPlayerTerminal(Player player) {
        return laboratoryManager.getPlayerTerminals(player.getUniqueId())
            .stream()
            .findFirst()
            .orElse(null);
    }
}
