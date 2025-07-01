package com.uraniumcraft.laboratory;

import org.bukkit.Material;
import java.util.HashMap;
import java.util.Map;

public enum LaboratoryLevel {
    LEVEL_1(1, "Базовая лаборатория", 1000, 2, getLevel1Materials()),
    LEVEL_2(2, "Продвинутая лаборатория", 2500, 4, getLevel2Materials()),
    LEVEL_3(3, "Высокотехнологичная лаборатория", 5000, 6, getLevel3Materials()),
    LEVEL_4(4, "Исследовательский комплекс", 10000, 8, getLevel4Materials()),
    LEVEL_5(5, "Мегалаборатория", 20000, 12, getLevel5Materials());
    
    private final int level;
    private final String name;
    private final int maxEnergy;
    private final int maxResearch;
    private final Map<Material, Integer> upgradeMaterials;
    
    LaboratoryLevel(int level, String name, int maxEnergy, int maxResearch, Map<Material, Integer> upgradeMaterials) {
        this.level = level;
        this.name = name;
        this.maxEnergy = maxEnergy;
        this.maxResearch = maxResearch;
        this.upgradeMaterials = upgradeMaterials;
    }
    
    private static Map<Material, Integer> getLevel1Materials() {
        Map<Material, Integer> materials = new HashMap<>();
        materials.put(Material.IRON_BLOCK, 32);
        materials.put(Material.REDSTONE_BLOCK, 16);
        materials.put(Material.DIAMOND_BLOCK, 8);
        return materials;
    }
    
    private static Map<Material, Integer> getLevel2Materials() {
        Map<Material, Integer> materials = new HashMap<>();
        materials.put(Material.DIAMOND_BLOCK, 16);
        materials.put(Material.EMERALD_BLOCK, 8);
        materials.put(Material.BEACON, 2);
        materials.put(Material.NETHER_STAR, 1);
        return materials;
    }
    
    private static Map<Material, Integer> getLevel3Materials() {
        Map<Material, Integer> materials = new HashMap<>();
        materials.put(Material.NETHERITE_BLOCK, 8);
        materials.put(Material.BEACON, 4);
        materials.put(Material.NETHER_STAR, 2);
        materials.put(Material.DRAGON_EGG, 1);
        return materials;
    }
    
    private static Map<Material, Integer> getLevel4Materials() {
        Map<Material, Integer> materials = new HashMap<>();
        materials.put(Material.NETHERITE_BLOCK, 16);
        materials.put(Material.BEACON, 8);
        materials.put(Material.NETHER_STAR, 4);
        materials.put(Material.ELYTRA, 1);
        return materials;
    }
    
    private static Map<Material, Integer> getLevel5Materials() {
        Map<Material, Integer> materials = new HashMap<>();
        materials.put(Material.NETHERITE_BLOCK, 32);
        materials.put(Material.BEACON, 16);
        materials.put(Material.NETHER_STAR, 8);
        materials.put(Material.TOTEM_OF_UNDYING, 4);
        return materials;
    }
    
    public int getLevel() { return level; }
    public String getName() { return name; }
    public int getMaxEnergy() { return maxEnergy; }
    public int getMaxResearch() { return maxResearch; }
    public Map<Material, Integer> getUpgradeMaterials() { return new HashMap<>(upgradeMaterials); }
    
    public static LaboratoryLevel getByLevel(int level) {
        for (LaboratoryLevel labLevel : values()) {
            if (labLevel.getLevel() == level) {
                return labLevel;
            }
        }
        return LEVEL_1;
    }
}
