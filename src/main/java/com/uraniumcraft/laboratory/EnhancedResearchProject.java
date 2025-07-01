package com.uraniumcraft.laboratory;

import org.bukkit.Material;
import java.util.Map;

public class EnhancedResearchProject {
    private final String researchType;
    private final Map<Material, Integer> requiredResources;
    private final long startTime;
    private final long duration;
    private final double speedMultiplier;
    private boolean completed;
    private int priority;
    
    public EnhancedResearchProject(String researchType, Map<Material, Integer> requiredResources, double speedMultiplier) {
        this.researchType = researchType;
        this.requiredResources = requiredResources;
        this.startTime = System.currentTimeMillis();
        this.speedMultiplier = speedMultiplier;
        this.duration = (long) (getBaseDuration(researchType) / speedMultiplier);
        this.completed = false;
        this.priority = 1;
    }
    
    private long getBaseDuration(String researchType) {
        switch (researchType.toLowerCase()) {
            // Базовые исследования
            case "hazmat_suit": return 20 * 60 * 1000; // 20 минут
            case "uranium_capsule": return 15 * 60 * 1000; // 15 минут
            case "power_armor": return 30 * 60 * 1000; // 30 минут
            case "auto_miner": return 40 * 60 * 1000; // 40 минут
            case "railgun": return 45 * 60 * 1000; // 45 минут
            case "electric_transport": return 60 * 60 * 1000; // 1 час
            
            // Физика
            case "quantum_processor": return 90 * 60 * 1000; // 1.5 часа
            case "fusion_reactor": return 120 * 60 * 1000; // 2 часа
            case "energy_amplifier": return 100 * 60 * 1000; // 1.67 часа
            
            // Химия
            case "advanced_hazmat_suit": return 75 * 60 * 1000; // 1.25 часа
            case "radiation_neutralizer": return 100 * 60 * 1000; // 1.67 часа
            case "chemical_synthesizer": return 150 * 60 * 1000; // 2.5 часа
            
            // Инженерия
            case "mega_miner": return 80 * 60 * 1000; // 1.33 часа
            case "construction_drone": return 110 * 60 * 1000; // 1.83 часа
            case "automated_factory": return 160 * 60 * 1000; // 2.67 часа
            
            // Биология
            case "healing_chamber": return 170 * 60 * 1000; // 2.83 часа
            case "bio_scanner": return 70 * 60 * 1000; // 1.17 часа
            case "genetic_modifier": return 130 * 60 * 1000; // 2.17 часа
            
            // Универсальные
            case "teleporter": return 240 * 60 * 1000; // 4 часа
            
            default: return 30 * 60 * 1000; // По умолчанию 30 минут
        }
    }
    
    public boolean update() {
        if (completed) {
            return true;
        }
        
        long currentTime = System.currentTimeMillis();
        if (currentTime - startTime >= duration) {
            completed = true;
            return true;
        }
        
        return false;
    }
    
    public long getRemainingTime() {
        if (completed) {
            return 0;
        }
        
        long elapsed = System.currentTimeMillis() - startTime;
        return Math.max(0, duration - elapsed);
    }
    
    public double getProgress() {
        if (completed) {
            return 100.0;
        }
        
        long elapsed = System.currentTimeMillis() - startTime;
        return Math.min(100.0, (double) elapsed / duration * 100.0);
    }
    
    public String getEstimatedTimeString() {
        long remaining = getRemainingTime();
        long hours = remaining / (60 * 60 * 1000);
        long minutes = (remaining % (60 * 60 * 1000)) / (60 * 1000);
        long seconds = (remaining % (60 * 1000)) / 1000;
        
        if (hours > 0) {
            return String.format("%dч %dм %dс", hours, minutes, seconds);
        } else if (minutes > 0) {
            return String.format("%dм %dс", minutes, seconds);
        } else {
            return String.format("%dс", seconds);
        }
    }
    
    // Геттеры
    public String getResearchType() { return researchType; }
    public Map<Material, Integer> getRequiredResources() { return requiredResources; }
    public boolean isCompleted() { return completed; }
    public double getSpeedMultiplier() { return speedMultiplier; }
    public int getPriority() { return priority; }
    public void setPriority(int priority) { this.priority = priority; }
}
