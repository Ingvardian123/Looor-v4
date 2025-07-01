package com.uraniumcraft.listeners;

import com.uraniumcraft.UraniumPlugin;
import com.uraniumcraft.gui.ResearchTerminalGUI;
import com.uraniumcraft.laboratory.EnhancedLaboratory;
import com.uraniumcraft.laboratory.EnhancedLaboratoryManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;

public class ResearchTerminalListener implements Listener {
    private final UraniumPlugin plugin;
    private final EnhancedLaboratoryManager laboratoryManager;
    
    // Временное хранение материалов для исследований
    private final Map<String, Map<Material, Integer>> pendingResearchMaterials = new HashMap<>();
    
    public ResearchTerminalListener(UraniumPlugin plugin, EnhancedLaboratoryManager laboratoryManager) {
        this.plugin = plugin;
        this.laboratoryManager = laboratoryManager;
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        
        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();
        
        if (title.equals(ChatColor.DARK_BLUE + "🔬 Исследовательский терминал")) {
            handleMainTerminalClick(player, event);
        } else if (title.startsWith(ChatColor.DARK_PURPLE + "📁 ")) {
            handleCategoryClick(player, event);
        } else if (title.startsWith(ChatColor.DARK_GREEN + "🔬 ")) {
            handleResearchDetailsClick(player, event);
        }
    }
    
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;
        
        Player player = (Player) event.getPlayer();
        String title = event.getView().getTitle();
        
        // Очищаем временные материалы при закрытии детального окна исследования
        if (title.startsWith(ChatColor.DARK_GREEN + "🔬 ")) {
            String playerKey = player.getUniqueId().toString();
            if (pendingResearchMaterials.containsKey(playerKey)) {
                // Возвращаем материалы игроку
                Map<Material, Integer> materials = pendingResearchMaterials.get(playerKey);
                for (Map.Entry<Material, Integer> entry : materials.entrySet()) {
                    ItemStack item = new ItemStack(entry.getKey(), entry.getValue());
                    player.getInventory().addItem(item);
                }
                pendingResearchMaterials.remove(playerKey);
                
                player.sendMessage(ChatColor.YELLOW + "Материалы возвращены в инвентарь.");
            }
        }
    }
    
    private void handleMainTerminalClick(Player player, InventoryClickEvent event) {
        event.setCancelled(true);
        
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;
        
        String displayName = clicked.getItemMeta().getDisplayName();
        
        var playerLabs = laboratoryManager.getPlayerLaboratories(player.getUniqueId());
        if (playerLabs.isEmpty()) return;
        
        EnhancedLaboratory laboratory = playerLabs.get(0);
        
        if (displayName.equals(ChatColor.AQUA + "🔄 Обновить данные")) {
            ResearchTerminalGUI.openMainTerminal(player, laboratory);
            player.sendMessage(ChatColor.GREEN + "Данные терминала обновлены!");
            
        } else if (displayName.equals(ChatColor.GOLD + "⚙️ Настройки лаборатории")) {
            // Открываем настройки лаборатории
            player.sendMessage(ChatColor.YELLOW + "Настройки лаборатории временно недоступны.");
            
        } else if (displayName.startsWith(ChatColor.GREEN + "📁 ") || 
                   displayName.startsWith(ChatColor.RED + "📁 ") ||
                   displayName.startsWith(ChatColor.LIGHT_PURPLE + "📁 ") ||
                   displayName.startsWith(ChatColor.BLUE + "📁 ") ||
                   displayName.startsWith(ChatColor.GOLD + "📁 ")) {
            
            String category = extractCategoryFromDisplayName(displayName);
            ResearchTerminalGUI.openResearchCategory(player, laboratory, category);
            
        } else if (displayName.startsWith(ChatColor.GOLD + "🔄 ")) {
            // Клик по активному исследованию - показываем детали
            String researchName = displayName.substring(4); // Убираем "🔄 "
            showActiveResearchDetails(player, laboratory, researchName);
        }
        
        // Звуковые эффекты
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.2f);
    }
    
    private void handleCategoryClick(Player player, InventoryClickEvent event) {
        event.setCancelled(true);
        
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;
        
        String displayName = clicked.getItemMeta().getDisplayName();
        
        var playerLabs = laboratoryManager.getPlayerLaboratories(player.getUniqueId());
        if (playerLabs.isEmpty()) return;
        
        EnhancedLaboratory laboratory = playerLabs.get(0);
        
        if (displayName.equals(ChatColor.YELLOW + "← Вернуться к главному меню")) {
            ResearchTerminalGUI.openMainTerminal(player, laboratory);
            
        } else if (displayName.startsWith(ChatColor.GREEN + "✅ ") ||
                   displayName.startsWith(ChatColor.GOLD + "🔄 ") ||
                   displayName.startsWith(ChatColor.YELLOW + "⭐ ") ||
                   displayName.startsWith(ChatColor.RED + "🔒 ")) {
            
            String researchName = extractResearchNameFromDisplayName(displayName);
            String researchId = convertNameToId(researchName);
            ResearchTerminalGUI.openResearchDetails(player, laboratory, researchId);
        }
        
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.4f, 1.1f);
    }
    
    private void handleResearchDetailsClick(Player player, InventoryClickEvent event) {
        event.setCancelled(true);
        
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;
        
        String displayName = clicked.getItemMeta().getDisplayName();
        
        var playerLabs = laboratoryManager.getPlayerLaboratories(player.getUniqueId());
        if (playerLabs.isEmpty()) return;
        
        EnhancedLaboratory laboratory = playerLabs.get(0);
        
        if (displayName.equals(ChatColor.YELLOW + "← Назад к категории")) {
            // Возвращаемся к категории (нужно определить какой)
            ResearchTerminalGUI.openMainTerminal(player, laboratory);
            
        } else if (displayName.equals(ChatColor.GREEN + "🚀 НАЧАТЬ ИССЛЕДОВАНИЕ")) {
            handleStartResearch(player, event, laboratory);
            
        } else if (displayName.startsWith(ChatColor.YELLOW + "Требуется: ")) {
            handleMaterialSlotClick(player, event);
        }
        
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.3f, 1.0f);
    }
    
    private void handleStartResearch(Player player, InventoryClickEvent event, EnhancedLaboratory laboratory) {
        String title = event.getView().getTitle();
        String researchName = title.substring(4); // Убираем "🔬 "
        String researchId = convertNameToId(researchName);
        
        String playerKey = player.getUniqueId().toString();
        Map<Material, Integer> materials = pendingResearchMaterials.get(playerKey);
        
        if (materials == null || materials.isEmpty()) {
            player.sendMessage(ChatColor.RED + "❌ Поместите все необходимые материалы в слоты!");
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);
            return;
        }
        
        // Проверяем, все ли материалы размещены
        // TODO: Добавить проверку соответствия требованиям исследования
        
        // Начинаем исследование
        if (laboratory.startResearch(researchId, materials)) {
            player.sendMessage(ChatColor.GREEN + "🚀 Исследование '" + researchName + "' начато!");
            player.sendMessage(ChatColor.YELLOW + "⏱️ Процесс займёт некоторое время...");
            
            // Очищаем временные материалы
            pendingResearchMaterials.remove(playerKey);
            
            // Эффекты запуска исследования
            showResearchStartEffects(player, laboratory);
            
            // Закрываем GUI и возвращаемся к главному терминалу
            new BukkitRunnable() {
                @Override
                public void run() {
                    ResearchTerminalGUI.openMainTerminal(player, laboratory);
                }
            }.runTaskLater(plugin, 10L);
            
        } else {
            player.sendMessage(ChatColor.RED + "❌ Не удалось начать исследование!");
            player.sendMessage(ChatColor.GRAY + "Проверьте требования и доступность лаборатории.");
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);
        }
    }
    
    private void handleMaterialSlotClick(Player player, InventoryClickEvent event) {
        // Если игрок кликает с предметом в руке - добавляем материал
        ItemStack cursor = event.getCursor();
        if (cursor != null && cursor.getType() != Material.AIR) {
            String playerKey = player.getUniqueId().toString();
            Map<Material, Integer> materials = pendingResearchMaterials.computeIfAbsent(playerKey, k -> new HashMap<>());
            
            Material material = cursor.getType();
            int amount = cursor.getAmount();
            
            materials.put(material, materials.getOrDefault(material, 0) + amount);
            
            // Обновляем слот
            ItemStack slotItem = new ItemStack(material, amount);
            ItemMeta meta = slotItem.getItemMeta();
            meta.setDisplayName(ChatColor.GREEN + "✅ " + getMaterialDisplayName(material));
            meta.setLore(java.util.Arrays.asList(
                ChatColor.GRAY + "Количество: " + ChatColor.WHITE + amount,
                "",
                ChatColor.GREEN + "Материал размещён!"
            ));
            slotItem.setItemMeta(meta);
            event.setCurrentItem(slotItem);
            
            // Очищаем курсор
            event.setCursor(null);
            
            player.sendMessage(ChatColor.GREEN + "✅ Материал добавлен: " + getMaterialDisplayName(material) + " x" + amount);
            player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 0.8f, 1.2f);
        }
        // Если кликает по заполненному слоту - возвращаем материал
        else if (event.getCurrentItem() != null && 
                 event.getCurrentItem().getItemMeta().getDisplayName().startsWith(ChatColor.GREEN + "✅ ")) {
            
            ItemStack item = event.getCurrentItem();
            Material material = item.getType();
            int amount = item.getAmount();
            
            // Возвращаем материал игроку
            player.getInventory().addItem(new ItemStack(material, amount));
            
            // Удаляем из временного хранилища
            String playerKey = player.getUniqueId().toString();
            Map<Material, Integer> materials = pendingResearchMaterials.get(playerKey);
            if (materials != null) {
                materials.remove(material);
            }
            
            // Возвращаем слот к исходному состоянию
            ItemStack placeholder = new ItemStack(material);
            ItemMeta placeholderMeta = placeholder.getItemMeta();
            placeholderMeta.setDisplayName(ChatColor.YELLOW + "Требуется: " + getMaterialDisplayName(material));
            placeholderMeta.setLore(java.util.Arrays.asList(
                ChatColor.GRAY + "Поместите материал в этот слот"
            ));
            placeholder.setItemMeta(placeholderMeta);
            event.setCurrentItem(placeholder);
            
            player.sendMessage(ChatColor.YELLOW + "Материал возвращён: " + getMaterialDisplayName(material) + " x" + amount);
            player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 0.6f, 0.8f);
        }
    }
    
    private void showActiveResearchDetails(Player player, EnhancedLaboratory laboratory, String researchName) {
        var activeProject = laboratory.getActiveResearch().get(researchName);
        if (activeProject == null) return;
        
        player.sendMessage(ChatColor.GOLD + "🔄 Активное исследование: " + researchName);
        player.sendMessage(ChatColor.YELLOW + "Прогресс: " + String.format("%.1f", activeProject.getProgress()) + "%");
        player.sendMessage(ChatColor.YELLOW + "Осталось времени: " + activeProject.getEstimatedTimeString());
        player.sendMessage(ChatColor.GRAY + "Исследование будет завершено автоматически.");
    }
    
    private void showResearchStartEffects(Player player, EnhancedLaboratory laboratory) {
        if (laboratory.getTerminalLocation() != null) {
            // Частицы на терминале
            laboratory.getTerminalLocation().getWorld().spawnParticle(
                Particle.ENCHANTMENT_TABLE, 
                laboratory.getTerminalLocation().clone().add(0, 2, 0), 
                30, 1, 1, 1, 0.1
            );
            
            laboratory.getTerminalLocation().getWorld().spawnParticle(
                Particle.END_ROD, 
                laboratory.getTerminalLocation().clone().add(0, 1, 0), 
                15, 0.5, 0.5, 0.5, 0.05
            );
        }
        
        // Звуковые эффекты
        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1.0f, 1.5f);
        player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 0.8f, 1.2f);
        
        // Задержанные эффекты
        new BukkitRunnable() {
            @Override
            public void run() {
                player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 0.6f, 1.8f);
            }
        }.runTaskLater(plugin, 20L);
    }
    
    // Вспомогательные методы
    private String extractCategoryFromDisplayName(String displayName) {
        // Убираем цветовые коды и символы
        return displayName.replaceAll("§[0-9a-fk-or]", "").replace("📁 ", "").trim();
    }
    
    private String extractResearchNameFromDisplayName(String displayName) {
        // Убираем префиксы статуса
        return displayName.replaceAll("§[0-9a-fk-or]", "")
                         .replace("✅ ", "")
                         .replace("🔄 ", "")
                         .replace("⭐ ", "")
                         .replace("🔒 ", "")
                         .trim();
    }
    
    private String convertNameToId(String name) {
        // Простое преобразование имени в ID
        return name.toLowerCase()
                  .replace(" ", "_")
                  .replace("ё", "e")
                  .replace("а", "a")
                  .replace("и", "i")
                  .replace("о", "o")
                  .replace("у", "u")
                  .replace("е", "e")
                  .replace("ы", "y")
                  .replace("э", "e")
                  .replace("ю", "yu")
                  .replace("я", "ya");
    }
    
    private String getMaterialDisplayName(Material material) {
        switch (material) {
            case IRON_INGOT: return "Железные слитки";
            case REDSTONE: return "Красная пыль";
            case DIAMOND: return "Алмазы";
            case EMERALD: return "Изумруды";
            case NETHER_STAR: return "Звезда Нижнего мира";
            case BEACON: return "Маяк";
            case GOLD_INGOT: return "Золотые слитки";
            case QUARTZ: return "Кварц";
            case GLOWSTONE_DUST: return "Светопыль";
            case ENDER_PEARL: return "Жемчуг Края";
            case LEATHER: return "Кожа";
            case SUGAR: return "Сахар";
            case GOLDEN_APPLE: return "Золотое яблоко";
            case REDSTONE_BLOCK: return "Блок красной пыли";
            case IRON_BLOCK: return "Железный блок";
            case DIAMOND_BLOCK: return "Алмазный блок";
            case OBSIDIAN: return "Обсидиан";
            case SPYGLASS: return "Подзорная труба";
            case CONDUIT: return "Проводник";
            case PRISMARINE_CRYSTALS: return "Призмариновые кристаллы";
            case NETHERITE_INGOT: return "Незеритовые слитки";
            case GLASS: return "Стекло";
            case GLOWSTONE: return "Светокамень";
            default: return material.name().toLowerCase().replace("_", " ");
        }
    }
}
