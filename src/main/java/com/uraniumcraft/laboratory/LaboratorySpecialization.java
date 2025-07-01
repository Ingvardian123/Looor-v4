package com.uraniumcraft.laboratory;

import org.bukkit.Material;
import java.util.Arrays;
import java.util.List;

public enum LaboratorySpecialization {
    PHYSICS("Физика", Material.REDSTONE_BLOCK, Arrays.asList(
        "quantum_processor", "fusion_reactor", "energy_amplifier"
    ), "Изучение квантовой физики и энергетики"),
    
    CHEMISTRY("Химия", Material.BREWING_STAND, Arrays.asList(
        "advanced_hazmat_suit", "radiation_neutralizer", "chemical_synthesizer"
    ), "Химические процессы и материалы"),
    
    ENGINEERING("Инженерия", Material.PISTON, Arrays.asList(
        "mega_miner", "construction_drone", "automated_factory"
    ), "Механика и автоматизация"),
    
    BIOLOGY("Биология", Material.WHEAT, Arrays.asList(
        "healing_chamber", "bio_scanner", "genetic_modifier"
    ), "Биологические исследования"),
    
    UNIVERSAL("Универсальная", Material.BEACON, Arrays.asList(
        "power_armor", "railgun", "auto_miner", "electric_transport", "teleporter"
    ), "Общие исследования");
    
    private final String name;
    private final Material icon;
    private final List<String> specialResearch;
    private final String description;
    
    LaboratorySpecialization(String name, Material icon, List<String> specialResearch, String description) {
        this.name = name;
        this.icon = icon;
        this.specialResearch = specialResearch;
        this.description = description;
    }
    
    public String getName() { return name; }
    public Material getIcon() { return icon; }
    public List<String> getSpecialResearch() { return specialResearch; }
    public String getDescription() { return description; }
    
    public boolean canResearch(String researchType) {
        return specialResearch.contains(researchType) || this == UNIVERSAL;
    }
    
    public double getResearchSpeedMultiplier(String researchType) {
        if (specialResearch.contains(researchType)) {
            return 1.5; // 50% быстрее для специализированных исследований
        }
        return this == UNIVERSAL ? 1.0 : 0.7; // Универсальная - обычная скорость, остальные медленнее
    }
}
