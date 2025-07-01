package com.uraniumcraft.listeners;

import com.uraniumcraft.UraniumPlugin;
import com.uraniumcraft.gui.LaboratoryGUI;
import com.uraniumcraft.gui.TabletGUI;
import com.uraniumcraft.items.UraniumTablet;
import com.uraniumcraft.laboratory.LaboratoryTerminal;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class TabletListener implements Listener {
    
    private final UraniumPlugin plugin;
    private final Map<UUID, Long> lastUse = new HashMap<>();
    private final Map<UUID, Long> lastHologram = new HashMap<>();
    private final Map<UUID, Integer> hologramTasks = new HashMap<>();
    
    public TabletListener(UraniumPlugin plugin) {
        this.plugin = plugin;
        startEnergyRegenerationTask();
        startPerformanceMonitoring();
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        
        if (item != null && item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
            String displayName = item.getItemMeta().getDisplayName();
            
            if (displayName.equals(ChatColor.AQUA + "⚡ Урановый планшет ⚡")) {
                if (player.isSneaking()) {
                    // Быстрый статус
                    TabletGUI.openQuickStatus(player, plugin);
                } else {
                    // Главное меню
                    TabletGUI.openMainMenu(player, plugin);
                }
                event.setCancelled(true);
            }
        }
    }
    
    @EventHandler
    public void onPlayerSwapHandItems(PlayerSwapHandItemsEvent event) {
        Player player = event.getPlayer();
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        
        if (UraniumTablet.isUraniumTablet(mainHand, plugin)) {
            event.setCancelled(true);
            
            UraniumTablet.TabletType type = UraniumTablet.getTabletType(mainHand, plugin);
            if (type != null && type.ordinal() >= UraniumTablet.TabletType.ADVANCED.ordinal()) {
                if (UraniumTablet.consumeEnergy(mainHand, plugin, 75)) {
                    createAdvancedHolographicProjection(player, mainHand);
                } else {
                    showLowEnergyWarning(player);
                }
            } else {
                player.sendMessage(Component.text("⚠️ Голографические проекции доступны только для продвинутых планшетов!", 
                    NamedTextColor.YELLOW));
            }
        }
    }
    
    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        ItemStack item = event.getItemDrop().getItemStack();
        
        if (UraniumTablet.isUraniumTablet(item, plugin)) {
            event.setCancelled(true);
            
            Player player = event.getPlayer();
            player.sendMessage(Component.text("🛡️ Планшет защищен от потери системой безопасности!", 
                NamedTextColor.RED, TextDecoration.BOLD));
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.8f);
            
            // Эффект защиты
            player.spawnParticle(Particle.BARRIER, player.getLocation().add(0, 1, 0), 10, 0.5, 0.5, 0.5, 0);
        }
    }
    
    @EventHandler
    public void onPlayerItemHeld(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        ItemStack newItem = player.getInventory().getItem(event.getNewSlot());
        
        if (UraniumTablet.isUraniumTablet(newItem, plugin)) {
            // Показываем краткую информацию при взятии планшета в руки
            UraniumTablet.TabletType type = UraniumTablet.getTabletType(newItem, plugin);
            int energy = UraniumTablet.getTabletEnergy(newItem, plugin);
            
            Component message = Component.text()
                .append(Component.text("📱 ", NamedTextColor.AQUA))
                .append(Component.text(type != null ? type.getDisplayName() : "Планшет", 
                    type != null ? type.getColor() : NamedTextColor.WHITE))
                .append(Component.text(" | ⚡ " + energy + " энергии", NamedTextColor.YELLOW))
                .build();
            
            player.sendActionBar(message);
            
            // Тихий звук активации
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.3f, 1.5f);
        }
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        
        String title = event.getView().getTitle();
        
        if (title.equals(ChatColor.AQUA + "⚡ Урановый планшет ⚡")) {
            handleMainMenuClick(event, player);
        } else if (title.contains("Модули планшета")) {
            handleModulesInterfaceClick(event, player);
        } else if (title.contains("Диагностика планшета")) {
            handleDiagnosticsInterfaceClick(event, player);
        }
    }
    
    private void handleMainMenuClick(InventoryClickEvent event, Player player) {
        event.setCancelled(true);
        
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;
        
        String displayName = clicked.getItemMeta().getDisplayName();
        
        if (displayName.equals(ChatColor.AQUA + "Управление центрифугами")) {
            // Открываем GUI центрифуг
            player.closeInventory();
            player.sendMessage(ChatColor.YELLOW + "Функция центрифуг будет доступна позже!");
            
        } else if (displayName.equals(ChatColor.RED + "Мониторинг радиации")) {
            // Показываем информацию о радиации
            player.closeInventory();
            int radiation = plugin.getRadiationManager().getRadiation(player.getUniqueId());
            player.sendMessage(ChatColor.RED + "☢ Текущий уровень радиации: " + radiation);
            if (radiation > 50) {
                player.sendMessage(ChatColor.YELLOW + "⚠ Высокий уровень радиации! Используйте защиту!");
            }
            
        } else if (displayName.equals(ChatColor.LIGHT_PURPLE + "Система телепортации")) {
            // Открываем GUI телепортов
            player.closeInventory();
            player.sendMessage(ChatColor.YELLOW + "Функция телепортации будет доступна позже!");
            
        } else if (displayName.equals(ChatColor.LIGHT_PURPLE + "Терминал лаборатории")) {
            // Открываем GUI лаборатории
            List<LaboratoryTerminal> terminals = plugin.getLaboratoryManager().getPlayerTerminals(player.getUniqueId());
            if (terminals.isEmpty()) {
                player.closeInventory();
                player.sendMessage(ChatColor.RED + "У вас нет терминалов лаборатории!");
                player.sendMessage(ChatColor.YELLOW + "Создайте терминал для проведения исследований.");
            } else {
                LaboratoryTerminal terminal = terminals.get(0);
                player.closeInventory();
                LaboratoryGUI.openMainMenu(player, terminal);
            }
            
        } else if (displayName.equals(ChatColor.GOLD + "Статистика игрока")) {
            // Показываем статистику
            player.closeInventory();
            showPlayerStats(player);
        }
    }
    
    private void handleModulesInterfaceClick(InventoryClickEvent event, Player player) {
        event.setCancelled(true);
        
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;
        
        String itemName = clicked.getItemMeta().displayName().toString();
        
        if (itemName.contains("Назад")) {
            TabletGUI.openMainMenu(player, plugin);
            return;
        }
        
        // Обработка установки/удаления модулей
        for (UraniumTablet.TabletModule module : UraniumTablet.TabletModule.values()) {
            if (itemName.contains(module.getName())) {
                ItemStack tablet = getPlayerTablet(player);
                boolean hasModule = UraniumTablet.hasTabletModule(tablet, plugin, module);
                
                if (hasModule) {
                    // Удаление модуля
                    removeTabletModule(player, tablet, module);
                } else {
                    // Установка модуля
                    installTabletModule(player, tablet, module);
                }
                
                // Обновляем интерфейс
                TabletGUI.openModulesInterface(player, plugin);
                break;
            }
        }
    }
    
    private void handleDiagnosticsInterfaceClick(InventoryClickEvent event, Player player) {
        event.setCancelled(true);
        
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;
        
        String itemName = clicked.getItemMeta().displayName().toString();
        ItemStack tablet = getPlayerTablet(player);
        
        if (itemName.contains("Назад")) {
            TabletGUI.openMainMenu(player, plugin);
        } else if (itemName.contains("Тест производительности")) {
            if (UraniumTablet.consumeEnergy(tablet, plugin, 50)) {
                runPerformanceTest(player);
            } else {
                showLowEnergyWarning(player);
            }
        } else if (itemName.contains("Калибровка")) {
            if (UraniumTablet.consumeEnergy(tablet, plugin, 100)) {
                runCalibration(player, tablet);
            } else {
                showLowEnergyWarning(player);
            }
        } else if (itemName.contains("Сброс настроек")) {
            confirmFactoryReset(player, tablet);
        }
    }
    
    private void showPlayerStats(Player player) {
        player.sendMessage(ChatColor.GOLD + "═══════════════════════════════");
        player.sendMessage(ChatColor.YELLOW + "📊 Статистика " + player.getName());
        player.sendMessage(ChatColor.GOLD + "═══════════════════════════════");
        
        // Радиация
        int radiation = plugin.getRadiationManager().getRadiation(player.getUniqueId());
        player.sendMessage(ChatColor.WHITE + "☢ Радиация: " + ChatColor.RED + radiation);
        
        // Лаборатории
        List<LaboratoryTerminal> terminals = plugin.getLaboratoryManager().getPlayerTerminals(player.getUniqueId());
        player.sendMessage(ChatColor.WHITE + "🔬 Терминалов лаборатории: " + ChatColor.LIGHT_PURPLE + terminals.size());
        
        if (!terminals.isEmpty()) {
            LaboratoryTerminal terminal = terminals.get(0);
            player.sendMessage(ChatColor.WHITE + "📚 Завершённых исследований: " + ChatColor.GREEN + 
                terminal.getCompletedResearch().size());
            player.sendMessage(ChatColor.WHITE + "⏳ Активных исследований: " + ChatColor.YELLOW + 
                terminal.getActiveResearch().size());
        }
        
        // Центрифуги
        int centrifuges = plugin.getCentrifugeManager().getPlayerCentrifuges(player.getUniqueId()).size();
        player.sendMessage(ChatColor.WHITE + "⚙ Центрифуг: " + ChatColor.AQUA + centrifuges);
        
        // Телепорты
        int teleporters = plugin.getTeleporterManager().getPlayerTeleporters(player.getUniqueId()).size();
        player.sendMessage(ChatColor.WHITE + "🌀 Телепортов: " + ChatColor.LIGHT_PURPLE + teleporters);
        
        player.sendMessage(ChatColor.GOLD + "═══════════════════════════════");
    }
    
    private void createAdvancedHolographicProjection(Player player, ItemStack tablet) {
        if (isOnHologramCooldown(player)) {
            player.sendMessage(Component.text("⏰ Голографический проектор перезаряжается...", 
                NamedTextColor.YELLOW));
            return;
        }
        
        setHologramCooldown(player, 10000); // 10 секунд
        
        player.sendMessage(Component.text("🌌 Создание голографической проекции...", 
            NamedTextColor.LIGHT_PURPLE, TextDecoration.BOLD));
        
        // Создаем голографическую проекцию
        Location loc = player.getLocation().add(0, 3, 0);
        
        // Отменяем предыдущую голограмму если есть
        if (hologramTasks.containsKey(player.getUniqueId())) {
            Bukkit.getScheduler().cancelTask(hologramTasks.get(player.getUniqueId()));
        }
        
        // Создаем новую голограмму
        int taskId = new BukkitRunnable() {
            int ticks = 0;
            
            @Override
            public void run() {
                if (ticks >= 200) { // 10 секунд
                    this.cancel();
                    hologramTasks.remove(player.getUniqueId());
                    return;
                }
                
                // Создаем голографические эффекты
                double angle = ticks * 0.1;
                for (int i = 0; i < 8; i++) {
                    double x = Math.cos(angle + i * Math.PI / 4) * 2;
                    double z = Math.sin(angle + i * Math.PI / 4) * 2;
                    double y = Math.sin(ticks * 0.05) * 0.5;
                    
                    Location particleLoc = loc.clone().add(x, y, z);
                    player.getWorld().spawnParticle(Particle.END_ROD, particleLoc, 1, 0, 0, 0, 0);
                    player.getWorld().spawnParticle(Particle.PORTAL, particleLoc, 2, 0.1, 0.1, 0.1, 0);
                }
                
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L).getTaskId();
        
        hologramTasks.put(player.getUniqueId(), taskId);
        
        // Звуки голограммы
        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1.0f, 1.8f);
    }
    
    private void startEnergyRegenerationTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    regenerateTabletEnergy(player);
                }
            }
        }.runTaskTimer(plugin, 0L, 600L); // Каждые 30 секунд
    }
    
    private void regenerateTabletEnergy(Player player) {
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        ItemStack offHand = player.getInventory().getItemInOffHand();
        
        regenerateItemEnergy(mainHand, player);
        regenerateItemEnergy(offHand, player);
        
        // Проверяем весь инвентарь
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && UraniumTablet.isUraniumTablet(item, plugin)) {
                regenerateItemEnergy(item, player);
            }
        }
    }
    
    private void regenerateItemEnergy(ItemStack item, Player player) {
        if (!UraniumTablet.isUraniumTablet(item, plugin)) return;
        
        UraniumTablet.TabletType type = UraniumTablet.getTabletType(item, plugin);
        if (type == null) return;
        
        Set<UraniumTablet.TabletModule> modules = UraniumTablet.getTabletModules(item, plugin);
        int regenRate = UraniumTablet.getEnergyRegenRate(type, modules);
        int currentEnergy = UraniumTablet.getTabletEnergy(item, plugin);
        int maxEnergy = type.getMaxEnergy();
        
        if (currentEnergy < maxEnergy) {
            int newEnergy = Math.min(currentEnergy + regenRate, maxEnergy);
            UraniumTablet.setTabletEnergy(item, plugin, newEnergy);
            
            // Уведомление о полной зарядке
            if (currentEnergy < maxEnergy && newEnergy == maxEnergy) {
                player.sendMessage(Component.text("🔋 Планшет полностью заряжен!", 
                    NamedTextColor.GREEN, TextDecoration.BOLD));
                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.8f, 1.5f);
            }
        }
    }
    
    private void startPerformanceMonitoring() {
        new BukkitRunnable() {
            @Override
            public void run() {
                // Мониторинг производительности планшетов
                for (Player player : Bukkit.getOnlinePlayers()) {
                    monitorTabletPerformance(player);
                }
            }
        }.runTaskTimer(plugin, 0L, 1200L); // Каждую минуту
    }
    
    private void monitorTabletPerformance(Player player) {
        ItemStack tablet = getPlayerTablet(player);
        if (tablet == null) return;
        
        UraniumTablet.TabletType type = UraniumTablet.getTabletType(tablet, plugin);
        int energy = UraniumTablet.getTabletEnergy(tablet, plugin);
        
        // Предупреждения о низкой энергии
        if (type != null) {
            double energyPercent = (double) energy / type.getMaxEnergy() * 100;
            
            if (energyPercent <= 10 && energyPercent > 5) {
                player.sendMessage(Component.text("⚠️ Низкий заряд планшета: " + String.format("%.1f%%", energyPercent), 
                    NamedTextColor.YELLOW));
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.5f, 0.8f);
            } else if (energyPercent <= 5) {
                player.sendMessage(Component.text("🔋 КРИТИЧЕСКИ НИЗКИЙ ЗАРЯД: " + String.format("%.1f%%", energyPercent), 
                    NamedTextColor.RED, TextDecoration.BOLD));
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);
                
                // Эффект критического разряда
                player.spawnParticle(Particle.REDSTONE, player.getLocation().add(0, 1, 0), 10, 
                    0.5, 0.5, 0.5, 0, new Particle.DustOptions(Color.RED, 1.0f));
            }
        }
    }
    
    // Вспомогательные методы
    private boolean isOnCooldown(Player player) {
        long lastUseTime = lastUse.getOrDefault(player.getUniqueId(), 0L);
        return System.currentTimeMillis() - lastUseTime < 1000; // 1 секунда
    }
    
    private void setCooldown(Player player, long cooldown) {
        lastUse.put(player.getUniqueId(), System.currentTimeMillis() + cooldown);
    }
    
    private boolean isOnHologramCooldown(Player player) {
        long lastHologramTime = lastHologram.getOrDefault(player.getUniqueId(), 0L);
        return System.currentTimeMillis() - lastHologramTime < 10000; // 10 секунд
    }
    
    private void setHologramCooldown(Player player, long cooldown) {
        lastHologram.put(player.getUniqueId(), System.currentTimeMillis() + cooldown);
    }
    
    private ItemStack getPlayerTablet(Player player) {
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        if (UraniumTablet.isUraniumTablet(mainHand, plugin)) {
            return mainHand;
        }
        
        ItemStack offHand = player.getInventory().getItemInOffHand();
        if (UraniumTablet.isUraniumTablet(offHand, plugin)) {
            return offHand;
        }
        
        return null;
    }
    
    private void showLowEnergyWarning(Player player) {
        player.sendMessage(Component.text("⚡ Недостаточно энергии для выполнения операции!", 
            NamedTextColor.RED, TextDecoration.BOLD));
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);
        
        // Эффект низкой энергии
        player.spawnParticle(Particle.REDSTONE, player.getLocation().add(0, 1, 0), 5, 
            0.3, 0.3, 0.3, 0, new Particle.DustOptions(Color.YELLOW, 1.0f));
    }
    
    private NamedTextColor getEnergyColor(int energy, int maxEnergy) {
        double percent = (double) energy / maxEnergy * 100;
        if (percent > 75) return NamedTextColor.GREEN;
        if (percent > 50) return NamedTextColor.YELLOW;
        if (percent > 25) return NamedTextColor.GOLD;
        return NamedTextColor.RED;
    }
    
    private NamedTextColor getRadiationColor(int radiation) {
        if (radiation >= 80) return NamedTextColor.DARK_RED;
        if (radiation >= 60) return NamedTextColor.RED;
        if (radiation >= 40) return NamedTextColor.GOLD;
        if (radiation >= 20) return NamedTextColor.YELLOW;
        return NamedTextColor.GREEN;
    }
    
    private void installTabletModule(Player player, ItemStack tablet, UraniumTablet.TabletModule module) {
        UraniumTablet.addTabletModule(tablet, plugin, module);
        player.sendMessage(Component.text("✅ Модуль '" + module.getName() + "' успешно установлен!", 
            NamedTextColor.GREEN));
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.2f);
    }
    
    private void removeTabletModule(Player player, ItemStack tablet, UraniumTablet.TabletModule module) {
        // Логика удаления модуля (требует реализации в UraniumTablet)
        player.sendMessage(Component.text("❌ Модуль '" + module.getName() + "' удален!", 
            NamedTextColor.RED));
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 0.8f);
    }
    
    private void runPerformanceTest(Player player) {
        player.sendMessage(Component.text("🔧 Запуск теста производительности...", NamedTextColor.AQUA));
        player.closeInventory();
        
        new BukkitRunnable() {
            @Override
            public void run() {
                player.sendMessage(Component.text("✅ Тест завершен. Все системы работают нормально!", 
                    NamedTextColor.GREEN));
                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.2f);
            }
        }.runTaskLater(plugin, 60L);
    }
    
    private void runCalibration(Player player, ItemStack tablet) {
        player.sendMessage(Component.text("🎯 Запуск калибровки квантовых систем...", NamedTextColor.BLUE));
        player.closeInventory();
        
        new BukkitRunnable() {
            @Override
            public void run() {
                player.sendMessage(Component.text("✅ Калибровка завершена успешно!", NamedTextColor.GREEN));
                player.playSound(player.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1.0f, 1.5f);
            }
        }.runTaskLater(plugin, 80L);
    }
    
    private void confirmFactoryReset(Player player, ItemStack tablet) {
        player.sendMessage(Component.text("⚠️ Функция сброса настроек (требует подтверждения)", NamedTextColor.YELLOW));
        player.closeInventory();
    }
}
