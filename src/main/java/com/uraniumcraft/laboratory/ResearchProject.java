package com.uraniumcraft.laboratory;

import org.bukkit.Material;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ResearchProject {
    private final String id;
    private final String name;
    private final String category;
    private final String description;
    private final Material icon;
    private final int duration;
    private final Map<Material, Integer> requiredMaterials;
    private final int energyRequired;
    private final List<String> prerequisites;
    private double progress;
    private final double maxProgress;
    private final long startTime;
    
    public ResearchProject(String id, String name, String category, String description, 
                          Material icon, int duration, Map<Material, Integer> materials, int energy) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.description = description;
        this.icon = icon;
        this.duration = duration;
        this.requiredMaterials = new HashMap<>(materials);
        this.energyRequired = energy;
        this.prerequisites = new ArrayList<>();
        this.progress = 0.0;
        this.maxProgress = getMaxProgressForResearch(name);
        this.startTime = System.currentTimeMillis();
    }
    
    private double getMaxProgressForResearch(String researchType) {
        switch (researchType.toLowerCase()) {
            case "hazmat_helmet":
            case "hazmat_suit":
                return 100.0;
            case "power_armor_helmet":
            case "power_armor_chestplate":
            case "power_armor_leggings":
            case "power_armor_boots":
                return 200.0;
            case "railgun":
                return 300.0;
            case "uranium_tablet":
                return 150.0;
            case "teleporter_core":
                return 250.0;
            case "centrifuge_core":
                return 120.0;
            default:
                return 100.0;
        }
    }
    
    public void addProgress(double amount) {
        this.progress = Math.min(maxProgress, progress + amount);
    }
    
    public boolean isCompleted() {
        return progress >= maxProgress;
    }
    
    public double getProgressPercentage() {
        return (progress / maxProgress) * 100.0;
    }
    
    public long getRemainingTime() {
        if (isCompleted()) return 0;
        
        long elapsed = System.currentTimeMillis() - startTime;
        double progressRate = progress / elapsed; // прогресс в миллисекунду
        double remainingProgress = maxProgress - progress;
        
        return (long) (remainingProgress / progressRate);
    }
    
    // Геттеры
    public String getId() { return id; }
    public String getName() { return name; }
    public String getCategory() { return category; }
    public String getDescription() { return description; }
    public Material getIcon() { return icon; }
    public int getDuration() { return duration; }
    public Map<Material, Integer> getRequiredMaterials() { return requiredMaterials; }
    public int getEnergyRequired() { return energyRequired; }
    public List<String> getPrerequisites() { return prerequisites; }
    public double getProgress() { return progress; }
    public double getMaxProgress() { return maxProgress; }
    public long getStartTime() { return startTime; }
}
