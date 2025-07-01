package com.uraniumcraft.laboratory;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import java.util.HashMap;
import java.util.Map;

public class LaboratoryStorage {
    private final Map<Material, Integer> materials;
    private final Map<String, ItemStack> advancedItems;
    private final int maxCapacity;
    
    public LaboratoryStorage(int maxCapacity) {
        this.materials = new HashMap<>();
        this.advancedItems = new HashMap<>();
        this.maxCapacity = maxCapacity;
    }
    
    public boolean addMaterial(Material material, int amount) {
        int currentTotal = getTotalItems();
        if (currentTotal + amount > maxCapacity) {
            int canAdd = maxCapacity - currentTotal;
            if (canAdd > 0) {
                materials.put(material, materials.getOrDefault(material, 0) + canAdd);
                return true;
            }
            return false;
        }
        
        materials.put(material, materials.getOrDefault(material, 0) + amount);
        return true;
    }
    
    public boolean removeMaterial(Material material, int amount) {
        int current = materials.getOrDefault(material, 0);
        if (current >= amount) {
            materials.put(material, current - amount);
            if (materials.get(material) == 0) {
                materials.remove(material);
            }
            return true;
        }
        return false;
    }
    
    public int getMaterialAmount(Material material) {
        return materials.getOrDefault(material, 0);
    }
    
    public boolean hasMaterial(Material material, int amount) {
        return getMaterialAmount(material) >= amount;
    }
    
    public void addAdvancedItem(String itemId, ItemStack item) {
        advancedItems.put(itemId, item.clone());
    }
    
    public ItemStack getAdvancedItem(String itemId) {
        return advancedItems.get(itemId);
    }
    
    public void removeAdvancedItem(String itemId) {
        advancedItems.remove(itemId);
    }
    
    public int getTotalItems() {
        return materials.values().stream().mapToInt(Integer::intValue).sum() + advancedItems.size();
    }
    
    public int getFreeSpace() {
        return maxCapacity - getTotalItems();
    }
    
    public Map<Material, Integer> getAllMaterials() {
        return new HashMap<>(materials);
    }
    
    public Map<String, ItemStack> getAllAdvancedItems() {
        return new HashMap<>(advancedItems);
    }
    
    public boolean isFull() {
        return getTotalItems() >= maxCapacity;
    }
}
