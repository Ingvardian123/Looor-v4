package com.uraniumcraft.listeners;

import com.uraniumcraft.UraniumPlugin;
import com.uraniumcraft.gui.EnhancedLaboratoryGUI;
import com.uraniumcraft.gui.ResearchTerminalGUI;
import com.uraniumcraft.items.AdvancedItems;
import com.uraniumcraft.laboratory.EnhancedLaboratory;
import com.uraniumcraft.laboratory.EnhancedLaboratoryManager;
import com.uraniumcraft.laboratory.LaboratorySpecialization;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Particle;

public class EnhancedLaboratoryListener implements Listener {
    private final UraniumPlugin plugin;
    private final EnhancedLaboratoryManager laboratoryManager;
    
    public EnhancedLaboratoryListener(UraniumPlugin plugin, EnhancedLaboratoryManager laboratoryManager) {
        this.plugin = plugin;
        this.laboratoryManager = laboratoryManager;
    }
    
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItemInHand();
        
        if (item != null && item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
            String displayName = item.getItemMeta().getDisplayName();
            
            if (displayName.equals(ChatColor.LIGHT_PURPLE + "Блок лаборатории")) {
                if (!player.hasPermission("uraniumcraft.admin")) {
                    event.setCancelled(true);
                    player.sendMessage(ChatColor.RED + "У вас нет прав для создания лабораторий!");
                    return;
                }
                
                boolean created = laboratoryManager.createLaboratory(event.getBlock().getLocation(), player);
                if (created) {
                    player.sendMessage(ChatColor.GREEN + "✓ Лаборатория создана!");
                    player.sendMessage(ChatColor.AQUA + "Начните приносить материалы для строительства.");
                    player.playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1.0f, 1.0f);
                    
                    // Эффекты
                    event.getBlock().getWorld().spawnParticle(Particle.ENCHANTMENT_TABLE, 
                        event.getBlock().getLocation().add(0.5, 1, 0.5), 20, 0.5, 0.5, 0.5, 0.1);
                    
                    showRequiredMaterials(player);
                } else {
                    event.setCancelled(true);
                    player.sendMessage(ChatColor.RED + "Здесь уже есть лаборатория!");
                }
            }
        }
    }
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null) return;
        
        Block block = event.getClickedBlock();
        EnhancedLaboratory laboratory = laboratoryManager.getLaboratory(block.getLocation());
        
        if (laboratory != null) {
            Player player = event.getPlayer();
            
            if (!laboratory.getOwner().equals(player.getUniqueId()) && !player.hasPermission("uraniumcraft.admin")) {
                player.sendMessage(ChatColor.RED + "Это не ваша лаборатория!");
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);
                return;
            }
            
            if (laboratory.getState() == EnhancedLaboratory.LaboratoryState.OPERATIONAL) {
                // Проверяем, кликнул ли игрок по терминалу исследований
                if (laboratory.getTerminalLocation() != null && 
                    block.getLocation().equals(laboratory.getTerminalLocation())) {
                    ResearchTerminalGUI.openMainMenu(player, laboratory);
                    player.playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 0.8f, 1.5f);
                } else {
                    // Открываем основное GUI лаборатории
                    EnhancedLaboratoryGUI.openMainMenu(player, laboratory);
                    player.playSound(player.getLocation(), Sound.BLOCK_BEACON_AMBIENT, 1.0f, 1.0f);
                }
            } else {
                showConstructionStatus(player, laboratory);
                
                // Проверяем, есть ли у игрока нужные материалы
                ItemStack handItem = player.getInventory().getItemInMainHand();
                if (handItem != null && handItem.getType() != Material.AIR) {
                    Material material = handItem.getType();
                    int amount = handItem.getAmount();
                    
                    if (laboratory.addMaterial(material, amount)) {
                        player.getInventory().setItemInMainHand(null);
                        player.sendMessage(ChatColor.GREEN + "✓ Материал добавлен!");
                        player.sendMessage(ChatColor.YELLOW + "Прогресс строительства: " + 
                                         laboratory.getConstructionProgress() + "%");
                        
                        // Звуковые эффекты
                        player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1.0f, 1.5f);
                        
                        // Частицы
                        block.getWorld().spawnParticle(Particle.CRIT, 
                            block.getLocation().add(0.5, 1, 0.5), 10, 0.3, 0.3, 0.3, 0.1);
                        
                        if (laboratory.getState() == EnhancedLaboratory.LaboratoryState.OPERATIONAL) {
                            player.sendMessage(ChatColor.GOLD + "🎉 Лаборатория построена!");
                            player.sendMessage(ChatColor.AQUA + "Теперь вы можете проводить исследования!");
                            player.sendMessage(ChatColor.LIGHT_PURPLE + "💡 Кликните по терминалу в центре для исследований!");
                            player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
                            
                            // Эффекты завершения строительства
                            block.getWorld().spawnParticle(Particle.FIREWORKS_SPARK, 
                                block.getLocation().add(0.5, 3, 0.5), 50, 2, 2, 2, 0.2);
                            block.getWorld().spawnParticle(Particle.TOTEM, 
                                block.getLocation().add(0.5, 2, 0.5), 30, 1, 1, 1, 0.1);
                        }
                    } else {
                        player.sendMessage(ChatColor.GRAY + "Этот материал не нужен для строительства.");
                    }
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
        
        if (title.startsWith(ChatColor.DARK_PURPLE + "Лаборатория")) {
            handleLaboratoryMenuClick(player, event);
        } else if (title.equals(ChatColor.BLUE + "Выбор специализации")) {
            handleSpecializationClick(player, event);
        } else if (title.equals(ChatColor.GOLD + "Хранилище лаборатории")) {
            handleStorageClick(player, event);
        } else if (title.equals(ChatColor.DARK_BLUE + "Терминал исследований")) {
            handleResearchTerminalClick(player, event);
        } else if (title.startsWith(ChatColor.DARK_PURPLE + "Исследования:")) {
            handleResearchCategoryClick(player, event);
        }
    }
    
    private void handleLaboratoryMenuClick(Player player, InventoryClickEvent event) {
        event.setCancelled(true);
        
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;
        
        String displayName = clicked.getItemMeta().getDisplayName();
        
        // Получаем лаборатории игрока
        var playerLabs = laboratoryManager.getPlayerLaboratories(player.getUniqueId());
        if (playerLabs.isEmpty()) return;
        
        EnhancedLaboratory laboratory = playerLabs.get(0);
        
        if (displayName.equals(ChatColor.LIGHT_PURPLE + "Исследования")) {
            ResearchTerminalGUI.openMainMenu(player, laboratory);
        } else if (displayName.equals(ChatColor.GOLD + "Хранилище")) {
            EnhancedLaboratoryGUI.openStorageMenu(player, laboratory);
        } else if (displayName.equals(ChatColor.BLUE + "Специализация")) {
            EnhancedLaboratoryGUI.openSpecializationMenu(player, laboratory);
        } else if (displayName.equals(ChatColor.GOLD + "Улучшения")) {
            if (laboratory.upgradeLevel()) {
                player.sendMessage(ChatColor.GREEN + "Улучшение лаборатории начато!");
                player.closeInventory();
            } else {
                player.sendMessage(ChatColor.RED + "Невозможно улучшить лабораторию!");
            }
        } else if (displayName.equals(ChatColor.YELLOW + "Автоматизация")) {
            // Переключаем автоматизацию
            laboratory.setAutoResearch(!laboratory.isAutoResearch());
            laboratory.setAutoUpgrade(!laboratory.isAutoUpgrade());
            player.sendMessage(ChatColor.GREEN + "Настройки автоматизации изменены!");
            EnhancedLaboratoryGUI.openMainMenu(player, laboratory);
        }
    }
    
    private void handleSpecializationClick(Player player, InventoryClickEvent event) {
        event.setCancelled(true);
        
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;
        
        // Определяем специализацию по иконке
        for (LaboratorySpecialization spec : LaboratorySpecialization.values()) {
            if (clicked.getType() == spec.getIcon()) {
                var playerLabs = laboratoryManager.getPlayerLaboratories(player.getUniqueId());
                if (!playerLabs.isEmpty()) {
                    EnhancedLaboratory laboratory = playerLabs.get(0);
                    laboratory.setSpecialization(spec);
                    player.closeInventory();
                    EnhancedLaboratoryGUI.openMainMenu(player, laboratory);
                }
                break;
            }
        }
    }
    
    private void handleStorageClick(Player player, InventoryClickEvent event) {
        event.setCancelled(true);
        
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;
        
        if (clicked.getItemMeta().getLore().contains(ChatColor.GREEN + "Нажмите для извлечения")) {
            // Извлекаем материал из хранилища
            var playerLabs = laboratoryManager.getPlayerLaboratories(player.getUniqueId());
            if (!playerLabs.isEmpty()) {
                EnhancedLaboratory laboratory = playerLabs.get(0);
                Material material = clicked.getType();
                
                if (laboratory.getStorage().removeMaterial(material, 1)) {
                    player.getInventory().addItem(new ItemStack(material, 1));
                    player.sendMessage(ChatColor.GREEN + "Материал извлечён!");
                    EnhancedLaboratoryGUI.openStorageMenu(player, laboratory);
                }
            }
        }
    }
    
    private void handleResearchTerminalClick(Player player, InventoryClickEvent event) {
        event.setCancelled(true);
        
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;
        
        String displayName = clicked.getItemMeta().getDisplayName();
        
        var playerLabs = laboratoryManager.getPlayerLaboratories(player.getUniqueId());
        if (playerLabs.isEmpty()) return;
        
        EnhancedLaboratory laboratory = playerLabs.get(0);
        
        if (displayName.equals(ChatColor.GREEN + "Базовые технологии")) {
            ResearchTerminalGUI.openResearchCategory(player, laboratory, "Базовые технологии");
        } else if (displayName.equals(ChatColor.RED + "Энергетические системы")) {
            ResearchTerminalGUI.openResearchCategory(player, laboratory, "Энергетические системы");
        } else if (displayName.equals(ChatColor.GREEN + "Медицинские технологии")) {
            ResearchTerminalGUI.openResearchCategory(player, laboratory, "Медицинские технологии");
        } else if (displayName.equals(ChatColor.LIGHT_PURPLE + "Квантовые технологии")) {
            ResearchTerminalGUI.openResearchCategory(player, laboratory, "Квантовые технологии");
        } else if (displayName.equals(ChatColor.BLUE + "Защитные системы")) {
            ResearchTerminalGUI.openResearchCategory(player, laboratory, "Защитные системы");
        } else if (displayName.equals(ChatColor.GOLD + "Инструменты и утилиты")) {
            ResearchTerminalGUI.openResearchCategory(player, laboratory, "Инструменты и утилиты");
        }
    }
    
    private void handleResearchCategoryClick(Player player, InventoryClickEvent event) {
        event.setCancelled(true);
        
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;
        
        String displayName = clicked.getItemMeta().getDisplayName();
        
        if (displayName.equals(ChatColor.YELLOW + "← Назад")) {
            var playerLabs = laboratoryManager.getPlayerLaboratories(player.getUniqueId());
            if (!playerLabs.isEmpty()) {
                ResearchTerminalGUI.openMainMenu(player, playerLabs.get(0));
            }
        } else if (displayName.startsWith(ChatColor.YELLOW.toString())) {
            // Начинаем исследование
            String researchName = displayName.substring(2); // Убираем цвет и пробел
            
            var playerLabs = laboratoryManager.getPlayerLaboratories(player.getUniqueId());
            if (!playerLabs.isEmpty()) {
                EnhancedLaboratory laboratory = playerLabs.get(0);
                
                // Здесь нужно получить требования для исследования и начать его
                // Пока что просто уведомляем игрока
                player.sendMessage(ChatColor.GREEN + "Исследование " + researchName + " начато!");
                player.playSound(player.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1.0f, 1.2f);
                player.closeInventory();
            }
        }
    }
    
    private void showRequiredMaterials(Player player) {
        player.sendMessage(ChatColor.GOLD + "═══════════════════════════════");
        player.sendMessage(ChatColor.YELLOW + "🏗️ Необходимые материалы для строительства:");
        player.sendMessage("");
        player.sendMessage(ChatColor.WHITE + "• " + ChatColor.GRAY + "Кварцевые блоки: " + ChatColor.YELLOW + "128");
        player.sendMessage(ChatColor.WHITE + "• " + ChatColor.GRAY + "Железные блоки: " + ChatColor.YELLOW + "64");
        player.sendMessage(ChatColor.WHITE + "• " + ChatColor.AQUA + "Стекло: " + ChatColor.YELLOW + "96");
        player.sendMessage(ChatColor.WHITE + "• " + ChatColor.RED + "Редстоун блоки: " + ChatColor.YELLOW + "32");
        player.sendMessage(ChatColor.WHITE + "• " + ChatColor.AQUA + "Алмазные блоки: " + ChatColor.YELLOW + "16");
        player.sendMessage(ChatColor.WHITE + "• " + ChatColor.GREEN + "Изумрудные блоки: " + ChatColor.YELLOW + "8");
        player.sendMessage(ChatColor.WHITE + "• " + ChatColor.LIGHT_PURPLE + "Маяки: " + ChatColor.YELLOW + "4");
        player.sendMessage(ChatColor.WHITE + "• " + ChatColor.DARK_PURPLE + "Звёзды Нижнего мира: " + ChatColor.YELLOW + "2");
        player.sendMessage(ChatColor.WHITE + "• " + ChatColor.YELLOW + "Светокамень: " + ChatColor.YELLOW + "64");
        player.sendMessage(ChatColor.WHITE + "• " + ChatColor.DARK_PURPLE + "Обсидиан: " + ChatColor.YELLOW + "32");
        player.sendMessage("");
        player.sendMessage(ChatColor.GOLD + "═══════════════════════════════");
        player.sendMessage(ChatColor.GRAY + "Держите материалы в руке и кликните по блоку лаборатории");
        player.sendMessage(ChatColor.LIGHT_PURPLE + "После постройки кликните по терминалу в центре для исследований!");
    }
    
    private void showConstructionStatus(Player player, EnhancedLaboratory laboratory) {
        int progress = laboratory.getConstructionProgress();
        
        player.sendMessage(ChatColor.AQUA + "🏗️ Статус строительства лаборатории:");
        
        // Прогресс-бар
        StringBuilder progressBar = new StringBuilder();
        progressBar.append(ChatColor.GREEN);
        int filledBars = progress / 5; // 20 символов максимум
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
            case QUARTZ_BLOCK: return ChatColor.WHITE + "Кварцевые блоки";
            case IRON_BLOCK: return ChatColor.GRAY + "Железные блоки";
            case GLASS: return ChatColor.AQUA + "Стекло";
            case REDSTONE_BLOCK: return ChatColor.RED + "Редстоун блоки";
            case DIAMOND_BLOCK: return ChatColor.AQUA + "Алмазные блоки";
            case EMERALD_BLOCK: return ChatColor.GREEN + "Изумрудные блоки";
            case BEACON: return ChatColor.LIGHT_PURPLE + "Маяки";
            case NETHER_STAR: return ChatColor.DARK_PURPLE + "Звёзды Нижнего мира";
            case GLOWSTONE: return ChatColor.YELLOW + "Светокамень";
            case OBSIDIAN: return ChatColor.DARK_PURPLE + "Обсидиан";
            default: return material.name();
        }
    }
}
