package com.uraniumcraft.listeners;

import com.uraniumcraft.UraniumPlugin;
import com.uraniumcraft.effects.AnimatedVisualEffects;
import com.uraniumcraft.items.UraniumTablet;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AnimatedItemsListener implements Listener {
    
    private final UraniumPlugin plugin;
    private final Map<UUID, Long> lastInteraction = new HashMap<>();
    private final Map<UUID, String> lastHeldItem = new HashMap<>();
    
    public AnimatedItemsListener(UraniumPlugin plugin) {
        this.plugin = plugin;
        AnimatedVisualEffects.initialize(plugin);
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        // Приветственная анимация для новых игроков
        new BukkitRunnable() {
            @Override
            public void run() {
                if (player.isOnline()) {
                    AnimatedVisualEffects.playTabletBootupAnimation(player);
                }
            }
        }.runTaskLater(plugin, 40L); // 2 секунды после входа
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        AnimatedVisualEffects.stopAllEffects(player);
        lastInteraction.remove(player.getUniqueId());
        lastHeldItem.remove(player.getUniqueId());
    }
    
    @EventHandler
    public void onPlayerItemHeld(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        ItemStack newItem = player.getInventory().getItem(event.getNewSlot());
        
        if (newItem != null && newItem.hasItemMeta() && newItem.getItemMeta().hasDisplayName()) {
            String displayName = newItem.getItemMeta().getDisplayName();
            String lastItem = lastHeldItem.get(player.getUniqueId());
            
            // Проверяем, изменился ли предмет
            if (!displayName.equals(lastItem)) {
                lastHeldItem.put(player.getUniqueId(), displayName);
                handleItemEquip(player, newItem, displayName);
            }
        } else {
            lastHeldItem.remove(player.getUniqueId());
        }
    }
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        
        if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) {
            return;
        }
        
        // Проверка кулдауна
        long currentTime = System.currentTimeMillis();
        Long lastTime = lastInteraction.get(player.getUniqueId());
        if (lastTime != null && currentTime - lastTime < 1000) {
            return; // Кулдаун 1 секунда
        }
        
        String displayName = item.getItemMeta().getDisplayName();
        Action action = event.getAction();
        
        if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
            handleItemInteraction(player, item, displayName);
            lastInteraction.put(player.getUniqueId(), currentTime);
        }
    }
    
    private void handleItemEquip(Player player, ItemStack item, String displayName) {
        // Урановый планшет
        if (UraniumTablet.isUraniumTablet(item, plugin)) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.8f);
            
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (player.isOnline() && player.getInventory().getItemInMainHand().equals(item)) {
                        AnimatedVisualEffects.playTabletBootupAnimation(player);
                    }
                }
            }.runTaskLater(plugin, 10L);
        }
        
        // Рельсотрон
        else if (displayName.contains("Рельсотрон")) {
            player.playSound(player.getLocation(), Sound.BLOCK_PISTON_EXTEND, 0.8f, 0.5f);
            player.sendMessage(ChatColor.RED + "⚡ Рельсотрон активирован - система зарядки готова");
        }
        
        // Силовая броня
        else if (displayName.contains("силовой брони")) {
            player.playSound(player.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 0.6f, 1.2f);
            
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (player.isOnline()) {
                        AnimatedVisualEffects.playPowerArmorActivation(player);
                    }
                }
            }.runTaskLater(plugin, 5L);
        }
        
        // Дозиметр Гейгера
        else if (displayName.contains("Дозиметр Гейгера")) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.4f, 2.0f);
            int radiation = plugin.getRadiationManager().getRadiation(player.getUniqueId());
            AnimatedVisualEffects.playGeigerCounterEffect(player, radiation);
        }
        
        // Квантовый телепортер
        else if (displayName.contains("Квантовый телепортер")) {
            player.playSound(player.getLocation(), Sound.BLOCK_CONDUIT_AMBIENT, 0.5f, 1.5f);
            player.sendMessage(ChatColor.LIGHT_PURPLE + "🌀 Квантовый телепортер готов к использованию");
        }
    }
    
    private void handleItemInteraction(Player player, ItemStack item, String displayName) {
        // Урановый планшет
        if (UraniumTablet.isUraniumTablet(item, plugin)) {
            if (player.isSneaking()) {
                // Быстрый статус с анимацией
                showAnimatedQuickStatus(player);
            } else {
                // Главное меню с голографическим интерфейсом
                AnimatedVisualEffects.playHolographicInterface(player, player.getLocation().add(0, 1.5, 0));
            }
        }
        
        // Рельсотрон
        else if (displayName.contains("Рельсотрон")) {
            if (player.isSneaking()) {
                // Переключение режима
                player.sendMessage(ChatColor.YELLOW + "🔧 Переключение режима рельсотрона...");
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.5f);
            } else {
                // Зарядка и выстрел
                player.sendMessage(ChatColor.RED + "⚡ Зарядка рельсотрона...");
                AnimatedVisualEffects.playRailgunChargingEffect(player);
            }
        }
        
        // Дозиметр Гейгера
        else if (displayName.contains("Дозиметр Гейгера")) {
            int radiation = plugin.getRadiationManager().getRadiation(player.getUniqueId());
            AnimatedVisualEffects.playGeigerCounterEffect(player, radiation);
            
            // Показываем детальную информацию
            showRadiationAnalysis(player, radiation);
        }
        
        // Квантовый телепортер
        else if (displayName.contains("Квантовый телепортер")) {
            player.sendMessage(ChatColor.LIGHT_PURPLE + "🌀 Инициализация квантового телепорта...");
            AnimatedVisualEffects.playQuantumTeleporterCharging(player.getLocation());
        }
        
        // Урановая капсула
        else if (displayName.contains("Урановая капсула")) {
            player.sendMessage(ChatColor.GREEN + "☢ Урановая капсула излучает энергию...");
            showRadioactiveWarning(player);
        }
        
        // Центрифуга
        else if (displayName.contains("Центрифуга")) {
            player.sendMessage(ChatColor.AQUA + "⚙ Запуск центрифуги...");
            AnimatedVisualEffects.playCentrifugeOperation(player.getLocation());
        }
        
        // Лабораторный терминал
        else if (displayName.contains("Лабораторный терминал")) {
            player.sendMessage(ChatColor.BLUE + "🔬 Активация лабораторного терминала...");
            AnimatedVisualEffects.playLaboratoryTerminalEffect(player.getLocation());
        }
    }
    
    private void showAnimatedQuickStatus(Player player) {
        player.sendMessage(ChatColor.AQUA + "═══════════════════════════════");
        player.sendMessage(ChatColor.YELLOW + "📱 БЫСТРЫЙ СТАТУС ПЛАНШЕТА");
        player.sendMessage(ChatColor.AQUA + "═══════════════════════════════");
        
        // Анимированный вывод информации
        new BukkitRunnable() {
            int stage = 0;
            
            @Override
            public void run() {
                if (!player.isOnline()) {
                    cancel();
                    return;
                }
                
                switch (stage) {
                    case 0:
                        player.sendMessage(ChatColor.WHITE + "⚡ Энергия: " + ChatColor.GREEN + "████████░░ 80%");
                        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.3f, 1.5f);
                        break;
                    case 1:
                        int radiation = plugin.getRadiationManager().getRadiation(player.getUniqueId());
                        ChatColor radiationColor = getRadiationColor(radiation);
                        player.sendMessage(ChatColor.WHITE + "☢ Радиация: " + radiationColor + radiation + "%");
                        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.3f, 1.7f);
                        break;
                    case 2:
                        player.sendMessage(ChatColor.WHITE + "🌡 Температура: " + ChatColor.YELLOW + "23°C");
                        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.3f, 1.9f);
                        break;
                    case 3:
                        player.sendMessage(ChatColor.WHITE + "📡 Связь: " + ChatColor.GREEN + "Стабильная");
                        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.3f, 2.1f);
                        break;
                    case 4:
                        player.sendMessage(ChatColor.AQUA + "═══════════════════════════════");
                        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 0.8f, 2.0f);
                        cancel();
                        break;
                }
                stage++;
            }
        }.runTaskTimer(plugin, 0L, 10L);
    }
    
    private void showRadiationAnalysis(Player player, int radiation) {
        new BukkitRunnable() {
            int dots = 0;
            
            @Override
            public void run() {
                if (!player.isOnline() || dots > 3) {
                    if (player.isOnline()) {
                        showRadiationResults(player, radiation);
                    }
                    cancel();
                    return;
                }
                
                String dotString = ".".repeat(dots);
                player.sendMessage(ChatColor.BLUE + "🔍 Анализ радиационного фона" + dotString);
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.2f, 1.0f + (dots * 0.2f));
                dots++;
            }
        }.runTaskTimer(plugin, 0L, 15L);
    }
    
    private void showRadiationResults(Player player, int radiation) {
        player.sendMessage(ChatColor.BLUE + "═══════════════════════════════");
        player.sendMessage(ChatColor.YELLOW + "📊 РЕЗУЛЬТАТЫ АНАЛИЗА");
        player.sendMessage(ChatColor.BLUE + "═══════════════════════════════");
        
        ChatColor radiationColor = getRadiationColor(radiation);
        player.sendMessage(ChatColor.WHITE + "☢ Уровень радиации: " + radiationColor + radiation + "%");
        
        String status;
        String recommendation;
        
        if (radiation == 0) {
            status = ChatColor.GREEN + "✅ БЕЗОПАСНО";
            recommendation = ChatColor.WHITE + "Радиационный фон в норме";
        } else if (radiation < 20) {
            status = ChatColor.YELLOW + "⚠ НИЗКИЙ РИСК";
            recommendation = ChatColor.WHITE + "Рекомендуется мониторинг";
        } else if (radiation < 40) {
            status = ChatColor.GOLD + "⚠ СРЕДНИЙ РИСК";
            recommendation = ChatColor.WHITE + "Используйте защитную экипировку";
        } else if (radiation < 60) {
            status = ChatColor.RED + "⚠ ВЫСОКИЙ РИСК";
            recommendation = ChatColor.WHITE + "Немедленно покиньте зону!";
        } else if (radiation < 80) {
            status = ChatColor.RED + "☢ КРИТИЧЕСКИЙ";
            recommendation = ChatColor.RED + "ОПАСНО ДЛЯ ЖИЗНИ!";
        } else {
            status = ChatColor.DARK_RED + "☠ СМЕРТЕЛЬНЫЙ";
            recommendation = ChatColor.DARK_RED + "ЭВАКУАЦИЯ НЕМЕДЛЕННО!";
        }
        
        player.sendMessage(ChatColor.WHITE + "Статус: " + status);
        player.sendMessage(ChatColor.WHITE + "Рекомендация: " + recommendation);
        player.sendMessage(ChatColor.BLUE + "═══════════════════════════════");
        
        // Звуковое предупреждение в зависимости от уровня
        if (radiation > 60) {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);
        } else if (radiation > 40) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 0.8f);
        } else {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.8f, 1.5f);
        }
    }
    
    private void showRadioactiveWarning(Player player) {
        new BukkitRunnable() {
            int blinks = 0;
            
            @Override
            public void run() {
                if (!player.isOnline() || blinks >= 6) {
                    cancel();
                    return;
                }
                
                if (blinks % 2 == 0) {
                    player.sendMessage(ChatColor.RED + "⚠⚠⚠ " + ChatColor.YELLOW + "РАДИОАКТИВНЫЙ МАТЕРИАЛ" + ChatColor.RED + " ⚠⚠⚠");
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.8f, 0.8f);
                } else {
                    player.sendMessage(ChatColor.DARK_RED + "☢☢☢ " + ChatColor.RED + "СОБЛЮДАЙТЕ ОСТОРОЖНОСТЬ" + ChatColor.DARK_RED + " ☢☢☢");
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.8f, 1.2f);
                }
                
                blinks++;
            }
        }.runTaskTimer(plugin, 0L, 10L);
    }
    
    private ChatColor getRadiationColor(int radiation) {
        if (radiation < 20) return ChatColor.GREEN;
        if (radiation < 40) return ChatColor.YELLOW;
        if (radiation < 60) return ChatColor.GOLD;
        if (radiation < 80) return ChatColor.RED;
        return ChatColor.DARK_RED;
    }
}
