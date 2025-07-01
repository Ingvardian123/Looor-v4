package com.uraniumcraft.centrifuge;

import com.uraniumcraft.UraniumPlugin;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CentrifugeManager {
    private final UraniumPlugin plugin;
    private final Map<Location, Centrifuge> centrifuges;
    
    public CentrifugeManager(UraniumPlugin plugin) {
        this.plugin = plugin;
        this.centrifuges = new HashMap<>();
    }
    
    public boolean createCentrifuge(Location location, Player player) {
        // Проверяем, что в центре стоит диспенсер
        if (location.getBlock().getType() != Material.DISPENSER) {
            player.sendMessage(ChatColor.RED + "Центрифуга должна быть построена на диспенсере!");
            return false;
        }
        
        if (centrifuges.containsKey(location)) {
            player.sendMessage(ChatColor.RED + "Здесь уже есть центрифуга!");
            return false;
        }
        
        // Проверяем права
        if (!player.hasPermission("uraniumcraft.centrifuge.create") && !player.hasPermission("uraniumcraft.admin")) {
            player.sendMessage(ChatColor.RED + "У вас нет прав для создания центрифуги!");
            return false;
        }
        
        try {
            Centrifuge centrifuge = new Centrifuge(player.getUniqueId(), location, plugin);
            if (centrifuge.isValidStructure()) {
                centrifuges.put(location, centrifuge);
                player.sendMessage(ChatColor.GREEN + "✅ Центрифуга успешно создана!");
                player.sendMessage(ChatColor.YELLOW + "💡 Принесите руды и кликните по центрифуге для начала обработки!");
                return true;
            } else {
                player.sendMessage(ChatColor.RED + "Структура центрифуги повреждена!");
                return false;
            }
        } catch (Exception e) {
            player.sendMessage(ChatColor.RED + "Ошибка при создании центрифуги: " + e.getMessage());
            plugin.getLogger().warning("Ошибка при создании центрифуги: " + e.getMessage());
            return false;
        }
    }
    
    public Centrifuge getCentrifuge(Location location) {
        return centrifuges.get(location);
    }
    
    public boolean startCentrifuge(Location location, Player player) {
        Centrifuge centrifuge = centrifuges.get(location);
        if (centrifuge == null) {
            player.sendMessage(ChatColor.RED + "Здесь нет центрифуги!");
            return false;
        }
        
        if (!centrifuge.getOwner().equals(player.getUniqueId()) && !player.hasPermission("uraniumcraft.admin")) {
            player.sendMessage(ChatColor.RED + "Это не ваша центрифуга!");
            return false;
        }
        
        return centrifuge.startCentrifuge(player);
    }
    
    public void showCentrifugeInfo(Location location, Player player) {
        Centrifuge centrifuge = centrifuges.get(location);
        if (centrifuge == null) {
            player.sendMessage(ChatColor.RED + "Здесь нет центрифуги!");
            return;
        }
        
        player.sendMessage(ChatColor.GOLD + "═══════════════════════════════");
        player.sendMessage(ChatColor.AQUA + "🔄 Информация о центрифуге:");
        
        if (centrifuge.isProcessing()) {
            player.sendMessage(ChatColor.GREEN + "Статус: " + ChatColor.YELLOW + "Работает");
            player.sendMessage(ChatColor.WHITE + "Прогресс: " + ChatColor.GREEN + 
                String.format("%.1f%%", centrifuge.getProgress()));
            player.sendMessage(ChatColor.WHITE + "Осталось времени: " + ChatColor.YELLOW + 
                centrifuge.getRemainingTimeString());
            
            if (!centrifuge.getInputMaterials().isEmpty()) {
                player.sendMessage(ChatColor.WHITE + "Обрабатываемые материалы:");
                for (Map.Entry<Material, Integer> entry : centrifuge.getInputMaterials().entrySet()) {
                    player.sendMessage(ChatColor.GRAY + "  • " + entry.getKey().name() + " x" + entry.getValue());
                }
            }
        } else {
            player.sendMessage(ChatColor.GREEN + "Статус: " + ChatColor.GRAY + "Ожидает");
            player.sendMessage(ChatColor.WHITE + "Использований: " + ChatColor.YELLOW + centrifuge.getProcessCount());
        }
        
        player.sendMessage(ChatColor.GOLD + "═══════════════════════════════");
    }
    
    public void removeCentrifuge(Location location) {
        Centrifuge centrifuge = centrifuges.get(location);
        if (centrifuge != null) {
            centrifuge.stopCentrifuge();
            centrifuges.remove(location);
        }
    }
    
    public int getTotalCentrifuges() {
        return centrifuges.size();
    }
    
    public int getActiveCentrifuges() {
        return (int) centrifuges.values().stream()
            .filter(Centrifuge::isProcessing)
            .count();
    }
    
    public void shutdown() {
        for (Centrifuge centrifuge : centrifuges.values()) {
            centrifuge.stopCentrifuge();
        }
        centrifuges.clear();
    }
}
