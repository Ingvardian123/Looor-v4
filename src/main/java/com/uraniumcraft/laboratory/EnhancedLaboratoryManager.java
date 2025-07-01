package com.uraniumcraft.laboratory;

import com.uraniumcraft.UraniumPlugin;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import java.util.*;

public class EnhancedLaboratoryManager {
    private final UraniumPlugin plugin;
    private final Map<Location, EnhancedLaboratory> laboratories;
    private final Set<UUID> authorizedPlayers;
    
    public EnhancedLaboratoryManager(UraniumPlugin plugin) {
        this.plugin = plugin;
        this.laboratories = new HashMap<>();
        this.authorizedPlayers = new HashSet<>();
        startUpdateTask();
    }
    
    public void authorizePlayer(UUID playerId) {
        authorizedPlayers.add(playerId);
    }
    
    public void unauthorizePlayer(UUID playerId) {
        authorizedPlayers.remove(playerId);
    }
    
    public boolean isPlayerAuthorized(UUID playerId) {
        return authorizedPlayers.contains(playerId);
    }
    
    public boolean createLaboratory(Location location, Player player) {
        if (!isPlayerAuthorized(player.getUniqueId())) {
            return false;
        }
        
        if (laboratories.containsKey(location)) {
            return false;
        }
        
        EnhancedLaboratory laboratory = new EnhancedLaboratory(location, player.getUniqueId(), plugin);
        laboratories.put(location, laboratory);
        return true;
    }
    
    public EnhancedLaboratory getLaboratory(Location location) {
        return laboratories.get(location);
    }
    
    public List<EnhancedLaboratory> getPlayerLaboratories(UUID playerId) {
        List<EnhancedLaboratory> playerLabs = new ArrayList<>();
        for (EnhancedLaboratory lab : laboratories.values()) {
            if (lab.getOwner().equals(playerId)) {
                playerLabs.add(lab);
            }
        }
        return playerLabs;
    }
    
    public void removeLaboratory(Location location) {
        EnhancedLaboratory lab = laboratories.get(location);
        if (lab != null) {
            lab.shutdown();
            laboratories.remove(location);
        }
    }
    
    private void startUpdateTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (EnhancedLaboratory laboratory : laboratories.values()) {
                    laboratory.updateResearch();
                }
            }
        }.runTaskTimer(plugin, 20L, 20L); // Обновляем каждую секунду
    }
    
    public Set<UUID> getAuthorizedPlayers() {
        return new HashSet<>(authorizedPlayers);
    }
    
    public Collection<EnhancedLaboratory> getAllLaboratories() {
        return laboratories.values();
    }
    
    public void shutdown() {
        for (EnhancedLaboratory lab : laboratories.values()) {
            lab.shutdown();
        }
        laboratories.clear();
    }
    
    // Статистика
    public int getTotalLaboratories() {
        return laboratories.size();
    }
    
    public int getOperationalLaboratories() {
        return (int) laboratories.values().stream()
            .filter(lab -> lab.getState() == EnhancedLaboratory.LaboratoryState.OPERATIONAL)
            .count();
    }
    
    public int getTotalActiveResearch() {
        return laboratories.values().stream()
            .mapToInt(lab -> lab.getActiveResearch().size())
            .sum();
    }
    
    public Map<LaboratorySpecialization, Integer> getSpecializationStats() {
        Map<LaboratorySpecialization, Integer> stats = new HashMap<>();
        for (LaboratorySpecialization spec : LaboratorySpecialization.values()) {
            stats.put(spec, 0);
        }
        
        for (EnhancedLaboratory lab : laboratories.values()) {
            LaboratorySpecialization spec = lab.getSpecialization();
            stats.put(spec, stats.get(spec) + 1);
        }
        
        return stats;
    }
}
