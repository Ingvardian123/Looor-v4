package com.uraniumcraft.laboratory;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class LaboratoryManager {
    private final Map<Location, LaboratoryTerminal> terminals;
    private final Map<UUID, List<LaboratoryTerminal>> playerTerminals;
    
    public LaboratoryManager() {
        this.terminals = new ConcurrentHashMap<>();
        this.playerTerminals = new HashMap<>();
    }
    
    public boolean createTerminal(Location location, Player player) {
        if (terminals.containsKey(location)) {
            return false;
        }
        
        LaboratoryTerminal terminal = new LaboratoryTerminal(location, player.getUniqueId());
        terminals.put(location, terminal);
        
        playerTerminals.computeIfAbsent(player.getUniqueId(), k -> new ArrayList<>()).add(terminal);
        
        return true;
    }
    
    public LaboratoryTerminal getTerminal(Location location) {
        return terminals.get(location);
    }
    
    public List<LaboratoryTerminal> getPlayerTerminals(UUID playerId) {
        return playerTerminals.getOrDefault(playerId, new ArrayList<>());
    }
    
    public void updateAllTerminals() {
        if (terminals.isEmpty()) return;
        
        for (LaboratoryTerminal terminal : terminals.values()) {
            try {
                if (terminal.isActive()) {
                    terminal.updateResearch();
                }
            } catch (Exception e) {
                // Логируем ошибку, но продолжаем обновление других терминалов
                System.err.println("Ошибка при обновлении терминала: " + e.getMessage());
            }
        }
    }
    
    public int getTotalTerminals() {
        return terminals.size();
    }
    
    public int getActiveTerminals() {
        return (int) terminals.values().stream()
            .filter(LaboratoryTerminal::isActive)
            .count();
    }
    
    public void saveAllData() {
        // Здесь можно добавить сохранение в файл
    }

    public static class LaboratoryTerminal {
        private final Location location;
        private final UUID owner;
        private boolean active = true;
        private final Map<String, ResearchProject> activeResearch = new HashMap<>();
        private final Set<String> completedResearch = new HashSet<>();

        public LaboratoryTerminal(Location location, UUID owner) {
            this.location = location;
            this.owner = owner;
        }

        public void updateResearch() {
            // Простое обновление исследований
            for (ResearchProject project : activeResearch.values()) {
                project.addProgress(1.0);
            }
        }

        public boolean isActive() { return active; }
        public Map<String, ResearchProject> getActiveResearch() { return activeResearch; }
        public Set<String> getCompletedResearch() { return completedResearch; }
        public Location getLocation() { return location; }
        public UUID getOwner() { return owner; }
    }
}
