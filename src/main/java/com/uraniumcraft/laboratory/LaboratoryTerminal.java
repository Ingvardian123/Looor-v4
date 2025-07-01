package com.uraniumcraft.laboratory;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class LaboratoryTerminal {
    private final Location location;
    private final UUID owner;
    private boolean isActive;
    private final Map<String, ResearchProject> activeResearch;
    private final Set<String> completedResearch;
    private final Map<Material, Integer> requiredMaterials;
    
    public LaboratoryTerminal(Location location, UUID owner) {
        this.location = location;
        this.owner = owner;
        this.isActive = false;
        this.activeResearch = new HashMap<>();
        this.completedResearch = new HashSet<>();
        this.requiredMaterials = new HashMap<>();
        
        initializeRequiredMaterials();
    }
    
    private void initializeRequiredMaterials() {
        requiredMaterials.put(Material.IRON_BLOCK, 16);
        requiredMaterials.put(Material.REDSTONE_BLOCK, 8);
        requiredMaterials.put(Material.DIAMOND, 4);
        requiredMaterials.put(Material.EMERALD, 2);
    }
    
    public boolean addMaterial(Material material, int amount) {
        if (isActive) return false;
        
        if (requiredMaterials.containsKey(material)) {
            int required = requiredMaterials.get(material);
            int toAdd = Math.min(amount, required);
            requiredMaterials.put(material, required - toAdd);
            
            // Проверяем активацию
            checkActivation();
            return true;
        }
        
        return false;
    }
    
    private void checkActivation() {
        boolean allMaterialsProvided = requiredMaterials.values().stream()
            .allMatch(amount -> amount == 0);
        
        if (allMaterialsProvided && !isActive) {
            isActive = true;
        }
    }
    
    public boolean startResearch(String researchType) {
        if (!isActive) return false;
        if (activeResearch.containsKey(researchType)) return false;
        if (completedResearch.contains(researchType)) return false;
        
        ResearchProject project = new ResearchProject(researchType);
        activeResearch.put(researchType, project);
        return true;
    }
    
    public void updateResearch() {
        Iterator<Map.Entry<String, ResearchProject>> iterator = activeResearch.entrySet().iterator();
        
        while (iterator.hasNext()) {
            Map.Entry<String, ResearchProject> entry = iterator.next();
            ResearchProject project = entry.getValue();
            
            project.addProgress(1.0);
            
            if (project.isCompleted()) {
                completedResearch.add(entry.getKey());
                iterator.remove();
            }
        }
    }
    
    public List<String> getAvailableResearch() {
        List<String> available = new ArrayList<>();
        String[] allResearch = {
            "hazmat_helmet", "hazmat_suit", 
            "power_armor_helmet", "power_armor_chestplate", 
            "power_armor_leggings", "power_armor_boots",
            "railgun", "uranium_tablet", "teleporter_core", "centrifuge_core"
        };
        
        for (String research : allResearch) {
            if (!completedResearch.contains(research) && !activeResearch.containsKey(research)) {
                available.add(research);
            }
        }
        
        return available;
    }
    
    // Геттеры
    public Location getLocation() { return location; }
    public UUID getOwner() { return owner; }
    public boolean isActive() { return isActive; }
    public Map<String, ResearchProject> getActiveResearch() { return activeResearch; }
    public Set<String> getCompletedResearch() { return completedResearch; }
    public Map<Material, Integer> getRequiredMaterials() { return requiredMaterials; }
    
    public boolean isResearchCompleted(String researchType) {
        return completedResearch.contains(researchType);
    }
    
    public int getActivationProgress() {
        if (isActive) return 100;
        
        int totalRequired = 16 + 8 + 4 + 2; // Сумма всех материалов
        int totalProvided = 0;
        
        totalProvided += (16 - requiredMaterials.getOrDefault(Material.IRON_BLOCK, 0));
        totalProvided += (8 - requiredMaterials.getOrDefault(Material.REDSTONE_BLOCK, 0));
        totalProvided += (4 - requiredMaterials.getOrDefault(Material.DIAMOND, 0));
        totalProvided += (2 - requiredMaterials.getOrDefault(Material.EMERALD, 0));
        
        return (int) ((double) totalProvided / totalRequired * 100);
    }
}
