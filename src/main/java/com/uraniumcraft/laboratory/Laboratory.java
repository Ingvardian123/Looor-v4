package com.uraniumcraft.laboratory;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import com.uraniumcraft.UraniumPlugin;

import java.util.*;

public class Laboratory {
    private final Location location;
    private final UUID owner;
    private final UraniumPlugin plugin;
    
    // Основные характеристики
    private LaboratoryLevel level;
    private LaboratorySpecialization specialization;
    private LaboratoryState state;
    private int constructionProgress;
    
    // Энергия и ресурсы
    private int currentEnergy;
    private int energyGeneration;
    private int energyConsumption;
    private final LaboratoryStorage storage;
    
    // Исследования
    private final Map<String, ResearchProject> activeResearch;
    private final Set<String> completedResearch;
    private final Queue<String> researchQueue;
    
    // Автоматизация
    private boolean autoResearch;
    private boolean autoUpgrade;
    private BukkitTask automationTask;
    
    // Строительство
    private final Map<Material, Integer> requiredMaterials;
    
    // Терминал исследований
    private Location terminalLocation;
    
    public enum LaboratoryState {
        BLUEPRINT,
        UNDER_CONSTRUCTION,
        OPERATIONAL,
        UPGRADING,
        MAINTENANCE
    }
    
    public Laboratory(Location location, UUID owner, UraniumPlugin plugin) {
        this.location = location;
        this.owner = owner;
        this.plugin = plugin;
        this.level = LaboratoryLevel.LEVEL_1;
        this.specialization = LaboratorySpecialization.UNIVERSAL;
        this.state = LaboratoryState.BLUEPRINT;
        this.constructionProgress = 0;
        this.currentEnergy = 0;
        this.energyGeneration = 10;
        this.energyConsumption = 0;
        this.storage = new LaboratoryStorage(level.getMaxEnergy() / 10);
        this.activeResearch = new HashMap<>();
        this.completedResearch = new HashSet<>();
        this.researchQueue = new LinkedList<>();
        this.requiredMaterials = new HashMap<>();
        this.autoResearch = false;
        this.autoUpgrade = false;
        
        initializeRequiredMaterials();
        startAutomationTask();
    }
    
    private void initializeRequiredMaterials() {
        // Материалы для красивой лаборатории
        requiredMaterials.put(Material.QUARTZ_BLOCK, 256);      // Основные стены
        requiredMaterials.put(Material.IRON_BLOCK, 128);       // Каркас
        requiredMaterials.put(Material.GLASS, 192);            // Окна
        requiredMaterials.put(Material.REDSTONE_BLOCK, 64);    // Энергосистема
        requiredMaterials.put(Material.DIAMOND_BLOCK, 32);     // Терминал
        requiredMaterials.put(Material.EMERALD_BLOCK, 16);     // Процессоры
        requiredMaterials.put(Material.BEACON, 8);             // Энергогенераторы
        requiredMaterials.put(Material.NETHER_STAR, 4);        // Квантовые стабилизаторы
        requiredMaterials.put(Material.GLOWSTONE, 128);        // Освещение
        requiredMaterials.put(Material.OBSIDIAN, 64);          // Защита
        requiredMaterials.put(Material.PRISMARINE, 96);        // Декор
        requiredMaterials.put(Material.SEA_LANTERN, 32);       // Дополнительное освещение
    }
    
    public boolean addMaterial(Material material, int amount) {
        if (state == LaboratoryState.UNDER_CONSTRUCTION || state == LaboratoryState.BLUEPRINT) {
            if (requiredMaterials.containsKey(material)) {
                int required = requiredMaterials.get(material);
                int toAdd = Math.min(amount, required);
                requiredMaterials.put(material, required - toAdd);
                updateConstructionProgress();
                return true;
            }
        } else if (state == LaboratoryState.OPERATIONAL) {
            return storage.addMaterial(material, amount);
        }
        
        return false;
    }
    
    private void updateConstructionProgress() {
        int totalRequired = 0;
        int totalProvided = 0;
        
        Map<Material, Integer> originalRequirements = getOriginalRequirements();
        
        for (Map.Entry<Material, Integer> entry : originalRequirements.entrySet()) {
            Material material = entry.getKey();
            int original = entry.getValue();
            int remaining = requiredMaterials.getOrDefault(material, 0);
            
            totalRequired += original;
            totalProvided += (original - remaining);
        }
        
        constructionProgress = (int) ((double) totalProvided / totalRequired * 100);
        
        if (constructionProgress >= 100) {
            state = LaboratoryState.OPERATIONAL;
            buildLaboratoryStructure();
            currentEnergy = level.getMaxEnergy() / 2;
            
            // Эффекты завершения строительства
            playLaboratoryActivationEffect();
        } else if (constructionProgress > 0) {
            state = LaboratoryState.UNDER_CONSTRUCTION;
        }
    }
    
    private Map<Material, Integer> getOriginalRequirements() {
        Map<Material, Integer> original = new HashMap<>();
        original.put(Material.QUARTZ_BLOCK, 256);
        original.put(Material.IRON_BLOCK, 128);
        original.put(Material.GLASS, 192);
        original.put(Material.REDSTONE_BLOCK, 64);
        original.put(Material.DIAMOND_BLOCK, 32);
        original.put(Material.EMERALD_BLOCK, 16);
        original.put(Material.BEACON, 8);
        original.put(Material.NETHER_STAR, 4);
        original.put(Material.GLOWSTONE, 128);
        original.put(Material.OBSIDIAN, 64);
        original.put(Material.PRISMARINE, 96);
        original.put(Material.SEA_LANTERN, 32);
        return original;
    }
    
    private void buildLaboratoryStructure() {
        World world = location.getWorld();
        if (world == null) return;
        
        int baseX = location.getBlockX();
        int baseY = location.getBlockY();
        int baseZ = location.getBlockZ();
        
        // Очищаем область
        clearArea(world, baseX - 10, baseY - 2, baseZ - 10, baseX + 10, baseY + 15, baseZ + 10);
        
        // Строим подвал (энергосистемы и хранилище)
        buildBasement(world, baseX, baseY - 2, baseZ);
        
        // Строим основной этаж
        buildMainFloor(world, baseX, baseY, baseZ);
        
        // Строим второй этаж (специализированное оборудование)
        buildSecondFloor(world, baseX, baseY + 6, baseZ);
        
        // Строим башню управления
        buildControlTower(world, baseX, baseY + 12, baseZ);
        
        // Строим антенную систему
        buildAntennaSystem(world, baseX, baseY + 15, baseZ);
        
        // Внешние структуры
        buildExternalStructures(world, baseX, baseY, baseZ);
        
        // Устанавливаем терминал исследований
        terminalLocation = new Location(world, baseX, baseY + 1, baseZ);
        world.getBlockAt(terminalLocation).setType(Material.ENCHANTING_TABLE);
        
        // Создаем платформу для терминала
        buildTerminalPlatform(world, baseX, baseY, baseZ);
    }
    
    private void clearArea(World world, int x1, int y1, int z1, int x2, int y2, int z2) {
        for (int x = x1; x <= x2; x++) {
            for (int y = y1; y <= y2; y++) {
                for (int z = z1; z <= z2; z++) {
                    if (y >= 0 && y < world.getMaxHeight()) {
                        world.getBlockAt(x, y, z).setType(Material.AIR);
                    }
                }
            }
        }
    }
    
    private void buildBasement(World world, int baseX, int baseY, int baseZ) {
        // Пол подвала
        for (int x = baseX - 8; x <= baseX + 8; x++) {
            for (int z = baseZ - 8; z <= baseZ + 8; z++) {
                world.getBlockAt(x, baseY, z).setType(Material.OBSIDIAN);
            }
        }
        
        // Стены подвала
        for (int x = baseX - 8; x <= baseX + 8; x++) {
            for (int y = baseY + 1; y <= baseY + 3; y++) {
                world.getBlockAt(x, y, baseZ - 8).setType(Material.IRON_BLOCK);
                world.getBlockAt(x, y, baseZ + 8).setType(Material.IRON_BLOCK);
            }
        }
        for (int z = baseZ - 8; z <= baseZ + 8; z++) {
            for (int y = baseY + 1; y <= baseY + 3; y++) {
                world.getBlockAt(baseX - 8, y, z).setType(Material.IRON_BLOCK);
                world.getBlockAt(baseX + 8, y, z).setType(Material.IRON_BLOCK);
            }
        }
        
        // Энергогенераторы
        world.getBlockAt(baseX - 6, baseY + 1, baseZ - 6).setType(Material.BEACON);
        world.getBlockAt(baseX + 6, baseY + 1, baseZ - 6).setType(Material.BEACON);
        world.getBlockAt(baseX - 6, baseY + 1, baseZ + 6).setType(Material.BEACON);
        world.getBlockAt(baseX + 6, baseY + 1, baseZ + 6).setType(Material.BEACON);
        
        // Хранилища
        for (int x = baseX - 4; x <= baseX + 4; x++) {
            for (int z = baseZ - 4; z <= baseZ + 4; z++) {
                if ((x + z) % 2 == 0) {
                    world.getBlockAt(x, baseY + 1, z).setType(Material.CHEST);
                }
            }
        }
        
        // Освещение
        world.getBlockAt(baseX, baseY + 3, baseZ).setType(Material.GLOWSTONE);
        world.getBlockAt(baseX - 4, baseY + 3, baseZ).setType(Material.SEA_LANTERN);
        world.getBlockAt(baseX + 4, baseY + 3, baseZ).setType(Material.SEA_LANTERN);
        world.getBlockAt(baseX, baseY + 3, baseZ - 4).setType(Material.SEA_LANTERN);
        world.getBlockAt(baseX, baseY + 3, baseZ + 4).setType(Material.SEA_LANTERN);
    }
    
    private void buildMainFloor(World world, int baseX, int baseY, int baseZ) {
        // Пол основного этажа
        for (int x = baseX - 9; x <= baseX + 9; x++) {
            for (int z = baseZ - 9; z <= baseZ + 9; z++) {
                world.getBlockAt(x, baseY, z).setType(Material.QUARTZ_BLOCK);
            }
        }
        
        // Внешние стены
        for (int x = baseX - 9; x <= baseX + 9; x++) {
            for (int y = baseY + 1; y <= baseY + 5; y++) {
                if (y == baseY + 3) {
                    world.getBlockAt(x, y, baseZ - 9).setType(Material.GLASS);
                    world.getBlockAt(x, y, baseZ + 9).setType(Material.GLASS);
                } else {
                    world.getBlockAt(x, y, baseZ - 9).setType(Material.QUARTZ_BLOCK);
                    world.getBlockAt(x, y, baseZ + 9).setType(Material.QUARTZ_BLOCK);
                }
            }
        }
        for (int z = baseZ - 9; z <= baseZ + 9; z++) {
            for (int y = baseY + 1; y <= baseY + 5; y++) {
                if (y == baseY + 3) {
                    world.getBlockAt(baseX - 9, y, z).setType(Material.GLASS);
                    world.getBlockAt(baseX + 9, y, z).setType(Material.GLASS);
                } else {
                    world.getBlockAt(baseX - 9, y, z).setType(Material.QUARTZ_BLOCK);
                    world.getBlockAt(baseX + 9, y, z).setType(Material.QUARTZ_BLOCK);
                }
            }
        }
        
        // Потолок
        for (int x = baseX - 9; x <= baseX + 9; x++) {
            for (int z = baseZ - 9; z <= baseZ + 9; z++) {
                world.getBlockAt(x, baseY + 6, z).setType(Material.QUARTZ_BLOCK);
            }
        }
        
        // Рабочие станции по углам
        buildWorkstation(world, baseX - 7, baseY + 1, baseZ - 7);
        buildWorkstation(world, baseX + 7, baseY + 1, baseZ - 7);
        buildWorkstation(world, baseX - 7, baseY + 1, baseZ + 7);
        buildWorkstation(world, baseX + 7, baseY + 1, baseZ + 7);
        
        // Освещение
        world.getBlockAt(baseX - 6, baseY + 5, baseZ - 6).setType(Material.GLOWSTONE);
        world.getBlockAt(baseX + 6, baseY + 5, baseZ - 6).setType(Material.GLOWSTONE);
        world.getBlockAt(baseX - 6, baseY + 5, baseZ + 6).setType(Material.GLOWSTONE);
        world.getBlockAt(baseX + 6, baseY + 5, baseZ + 6).setType(Material.GLOWSTONE);
    }
    
    private void buildSecondFloor(World world, int baseX, int baseY, int baseZ) {
        // Пол второго этажа
        for (int x = baseX - 7; x <= baseX + 7; x++) {
            for (int z = baseZ - 7; z <= baseZ + 7; z++) {
                world.getBlockAt(x, baseY, z).setType(Material.PRISMARINE);
            }
        }
        
        // Стены второго этажа
        for (int x = baseX - 7; x <= baseX + 7; x++) {
            for (int y = baseY + 1; y <= baseY + 5; y++) {
                if (y == baseY + 2 || y == baseY + 4) {
                    world.getBlockAt(x, y, baseZ - 7).setType(Material.GLASS);
                    world.getBlockAt(x, y, baseZ + 7).setType(Material.GLASS);
                } else {
                    world.getBlockAt(x, y, baseZ - 7).setType(Material.PRISMARINE);
                    world.getBlockAt(x, y, baseZ + 7).setType(Material.PRISMARINE);
                }
            }
        }
        for (int z = baseZ - 7; z <= baseZ + 7; z++) {
            for (int y = baseY + 1; y <= baseY + 5; y++) {
                if (y == baseY + 2 || y == baseY + 4) {
                    world.getBlockAt(baseX - 7, y, z).setType(Material.GLASS);
                    world.getBlockAt(baseX + 7, y, z).setType(Material.GLASS);
                } else {
                    world.getBlockAt(baseX - 7, y, z).setType(Material.PRISMARINE);
                    world.getBlockAt(baseX + 7, y, z).setType(Material.PRISMARINE);
                }
            }
        }
        
        // Потолок
        for (int x = baseX - 7; x <= baseX + 7; x++) {
            for (int z = baseZ - 7; z <= baseZ + 7; z++) {
                world.getBlockAt(x, baseY + 6, z).setType(Material.PRISMARINE);
            }
        }
        
        // Специализированное оборудование
        buildSpecializedEquipment(world, baseX, baseY + 1, baseZ);
        
        // Освещение
        world.getBlockAt(baseX, baseY + 5, baseZ).setType(Material.SEA_LANTERN);
        world.getBlockAt(baseX - 4, baseY + 5, baseZ).setType(Material.SEA_LANTERN);
        world.getBlockAt(baseX + 4, baseY + 5, baseZ).setType(Material.SEA_LANTERN);
        world.getBlockAt(baseX, baseY + 5, baseZ - 4).setType(Material.SEA_LANTERN);
        world.getBlockAt(baseX, baseY + 5, baseZ + 4).setType(Material.SEA_LANTERN);
    }
    
    private void buildControlTower(World world, int baseX, int baseY, int baseZ) {
        // Пол башни
        for (int x = baseX - 3; x <= baseX + 3; x++) {
            for (int z = baseZ - 3; z <= baseZ + 3; z++) {
                world.getBlockAt(x, baseY, z).setType(Material.DIAMOND_BLOCK);
            }
        }
        
        // Стены башни
        for (int x = baseX - 3; x <= baseX + 3; x++) {
            for (int y = baseY + 1; y <= baseY + 3; y++) {
                world.getBlockAt(x, y, baseZ - 3).setType(Material.GLASS);
                world.getBlockAt(x, y, baseZ + 3).setType(Material.GLASS);
            }
        }
        for (int z = baseZ - 3; z <= baseZ + 3; z++) {
            for (int y = baseY + 1; y <= baseY + 3; y++) {
                world.getBlockAt(baseX - 3, y, z).setType(Material.GLASS);
                world.getBlockAt(baseX + 3, y, z).setType(Material.GLASS);
            }
        }
        
        // Центральная консоль управления
        world.getBlockAt(baseX, baseY + 1, baseZ).setType(Material.COMMAND_BLOCK);
        world.getBlockAt(baseX - 1, baseY + 1, baseZ).setType(Material.REDSTONE_BLOCK);
        world.getBlockAt(baseX + 1, baseY + 1, baseZ).setType(Material.REDSTONE_BLOCK);
        world.getBlockAt(baseX, baseY + 1, baseZ - 1).setType(Material.REDSTONE_BLOCK);
        world.getBlockAt(baseX, baseY + 1, baseZ + 1).setType(Material.REDSTONE_BLOCK);
        
        // Освещение
        world.getBlockAt(baseX, baseY + 3, baseZ).setType(Material.GLOWSTONE);
    }
    
    private void buildAntennaSystem(World world, int baseX, int baseY, int baseZ) {
        // Центральная антенна
        for (int y = baseY; y <= baseY + 8; y++) {
            world.getBlockAt(baseX, y, baseZ).setType(Material.IRON_BLOCK);
        }
        
        // Антенные элементы
        world.getBlockAt(baseX - 2, baseY + 6, baseZ).setType(Material.IRON_BLOCK);
        world.getBlockAt(baseX + 2, baseY + 6, baseZ).setType(Material.IRON_BLOCK);
        world.getBlockAt(baseX, baseY + 6, baseZ - 2).setType(Material.IRON_BLOCK);
        world.getBlockAt(baseX, baseY + 6, baseZ + 2).setType(Material.IRON_BLOCK);
        
        // Энергетические кристаллы
        world.getBlockAt(baseX - 1, baseY + 8, baseZ - 1).setType(Material.AMETHYST_BLOCK);
        world.getBlockAt(baseX + 1, baseY + 8, baseZ - 1).setType(Material.AMETHYST_BLOCK);
        world.getBlockAt(baseX - 1, baseY + 8, baseZ + 1).setType(Material.AMETHYST_BLOCK);
        world.getBlockAt(baseX + 1, baseY + 8, baseZ + 1).setType(Material.AMETHYST_BLOCK);
        
        // Верхушка антенны
        world.getBlockAt(baseX, baseY + 9, baseZ).setType(Material.NETHER_STAR);
    }
    
    private void buildExternalStructures(World world, int baseX, int baseY, int baseZ) {
        // Внешние опоры
        buildPillar(world, baseX - 12, baseY, baseZ - 12, 8);
        buildPillar(world, baseX + 12, baseY, baseZ - 12, 8);
        buildPillar(world, baseX - 12, baseY, baseZ + 12, 8);
        buildPillar(world, baseX + 12, baseY, baseZ + 12, 8);
        
        // Соединительные мостики
        buildBridge(world, baseX - 12, baseY + 6, baseZ - 12, baseX - 9, baseY + 6, baseZ - 9);
        buildBridge(world, baseX + 9, baseY + 6, baseZ - 9, baseX + 12, baseY + 6, baseZ - 12);
        buildBridge(world, baseX - 12, baseY + 6, baseZ + 12, baseX - 9, baseY + 6, baseZ + 9);
        buildBridge(world, baseX + 9, baseY + 6, baseZ + 9, baseX + 12, baseY + 6, baseZ + 12);
        
        // Защитные барьеры
        buildBarrier(world, baseX - 15, baseY, baseZ - 15, baseX + 15, baseY, baseZ + 15);
    }
    
    private void buildTerminalPlatform(World world, int baseX, int baseY, int baseZ) {
        // Платформа 3x3 для терминала
        for (int x = baseX - 1; x <= baseX + 1; x++) {
            for (int z = baseZ - 1; z <= baseZ + 1; z++) {
                world.getBlockAt(x, baseY, z).setType(Material.DIAMOND_BLOCK);
            }
        }
        
        // Голографические проекторы по углам
        world.getBlockAt(baseX - 1, baseY + 1, baseZ - 1).setType(Material.END_ROD);
        world.getBlockAt(baseX + 1, baseY + 1, baseZ - 1).setType(Material.END_ROD);
        world.getBlockAt(baseX - 1, baseY + 1, baseZ + 1).setType(Material.END_ROD);
        world.getBlockAt(baseX + 1, baseY + 1, baseZ + 1).setType(Material.END_ROD);
        
        // Энергетические кристаллы
        world.getBlockAt(baseX - 2, baseY + 1, baseZ).setType(Material.AMETHYST_BLOCK);
        world.getBlockAt(baseX + 2, baseY + 1, baseZ).setType(Material.AMETHYST_BLOCK);
        world.getBlockAt(baseX, baseY + 1, baseZ - 2).setType(Material.AMETHYST_BLOCK);
        world.getBlockAt(baseX, baseY + 1, baseZ + 2).setType(Material.AMETHYST_BLOCK);
    }
    
    private void buildWorkstation(World world, int x, int y, int z) {
        world.getBlockAt(x, y, z).setType(Material.CRAFTING_TABLE);
        world.getBlockAt(x + 1, y, z).setType(Material.FURNACE);
        world.getBlockAt(x, y, z + 1).setType(Material.ANVIL);
        world.getBlockAt(x + 1, y, z + 1).setType(Material.ENCHANTING_TABLE);
    }
    
    private void buildSpecializedEquipment(World world, int baseX, int baseY, int baseZ) {
        // Центральный процессор
        world.getBlockAt(baseX, baseY, baseZ).setType(Material.EMERALD_BLOCK);
        
        // Специализированное оборудование в зависимости от специализации
        switch (specialization) {
            case ENERGY:
                world.getBlockAt(baseX - 2, baseY, baseZ).setType(Material.REDSTONE_BLOCK);
                world.getBlockAt(baseX + 2, baseY, baseZ).setType(Material.REDSTONE_BLOCK);
                break;
            case MEDICAL:
                world.getBlockAt(baseX - 2, baseY, baseZ).setType(Material.BREWING_STAND);
                world.getBlockAt(baseX + 2, baseY, baseZ).setType(Material.CAULDRON);
                break;
            case QUANTUM:
                world.getBlockAt(baseX - 2, baseY, baseZ).setType(Material.END_PORTAL_FRAME);
                world.getBlockAt(baseX + 2, baseY, baseZ).setType(Material.END_PORTAL_FRAME);
                break;
            case DEFENSE:
                world.getBlockAt(baseX - 2, baseY, baseZ).setType(Material.DISPENSER);
                world.getBlockAt(baseX + 2, baseY, baseZ).setType(Material.DISPENSER);
                break;
            default:
                world.getBlockAt(baseX - 2, baseY, baseZ).setType(Material.IRON_BLOCK);
                world.getBlockAt(baseX + 2, baseY, baseZ).setType(Material.IRON_BLOCK);
                break;
        }
    }
    
    private void buildPillar(World world, int x, int y, int z, int height) {
        for (int i = 0; i < height; i++) {
            world.getBlockAt(x, y + i, z).setType(Material.QUARTZ_PILLAR);
        }
        world.getBlockAt(x, y + height, z).setType(Material.GLOWSTONE);
    }
    
    private void buildBridge(World world, int x1, int y, int z1, int x2, int y2, int z2) {
        int dx = x2 - x1;
        int dz = z2 - z1;
        int steps = Math.max(Math.abs(dx), Math.abs(dz));
        
        for (int i = 0; i <= steps; i++) {
            int x = x1 + (dx * i / steps);
            int z = z1 + (dz * i / steps);
            world.getBlockAt(x, y, z).setType(Material.QUARTZ_SLAB);
        }
    }
    
    private void buildBarrier(World world, int x1, int y, int z1, int x2, int y2, int z2) {
        // Простой барьер по периметру
        for (int x = x1; x <= x2; x++) {
            world.getBlockAt(x, y, z1).setType(Material.IRON_BARS);
            world.getBlockAt(x, y, z2).setType(Material.IRON_BARS);
        }
        for (int z = z1; z <= z2; z++) {
            world.getBlockAt(x1, y, z).setType(Material.IRON_BARS);
            world.getBlockAt(x2, y, z).setType(Material.IRON_BARS);
        }
    }
    
    private void playLaboratoryActivationEffect() {
        World world = location.getWorld();
        if (world == null) return;
        
        // Звуковые эффекты
        world.playSound(location, Sound.BLOCK_BEACON_ACTIVATE, 2.0f, 1.0f);
        world.playSound(location, Sound.BLOCK_PORTAL_AMBIENT, 1.0f, 1.5f);
        
        // Упрощенные визуальные эффекты
        new BukkitRunnable() {
            int ticks = 0;
            
            @Override
            public void run() {
                if (ticks >= 60) { // Уменьшено с 100 до 60
                    cancel();
                    return;
                }
                
                try {
                    // Упрощенные спиральные частицы
                    if (ticks % 3 == 0) { // Каждый третий тик
                        double angle = ticks * 0.3;
                        for (int i = 0; i < 4; i++) { // Уменьшено с 8 до 4
                            double currentAngle = angle + (i * Math.PI / 2);
                            double radius = 10 - (ticks * 0.1);
                            double x = location.getX() + Math.cos(currentAngle) * radius;
                            double z = location.getZ() + Math.sin(currentAngle) * radius;
                            double y = location.getY() + 5 + Math.sin(ticks * 0.2) * 2;
                            
                            world.spawnParticle(Particle.FIREWORKS_SPARK, x, y, z, 1, 0, 0, 0, 0);
                        }
                    }
                    
                    // Центральный столб энергии - реже
                    if (ticks % 5 == 0) {
                        for (int y = 0; y < 15; y += 3) { // Меньше частиц
                            world.spawnParticle(Particle.TOTEM, 
                                location.getX(), location.getY() + y, location.getZ(), 
                                1, 0.2, 0.2, 0.2, 0.05);
                        }
                    }
                    
                    // Пульсирующие кольца - реже
                    if (ticks % 30 == 0) {
                        for (int r = 5; r <= 15; r += 5) {
                            for (int deg = 0; deg < 360; deg += 20) { // Меньше частиц
                                double radians = Math.toRadians(deg);
                                double x = location.getX() + Math.cos(radians) * r;
                                double z = location.getZ() + Math.sin(radians) * r;
                                
                                world.spawnParticle(Particle.ELECTRIC_SPARK, 
                                    x, location.getY() + 1, z, 1, 0, 0, 0, 0);
                            }
                        }
                    }
                } catch (Exception e) {
                    // Игнорируем ошибки эффектов
                }
                
                ticks++;
            }
        }.runTaskTimer(plugin, 0, 3); // Увеличен интервал с 1 до 3
    }
    
    public void playResearchStartEffect(String researchName) {
        World world = location.getWorld();
        if (world == null) return;
        
        // Звуковые эффекты
        world.playSound(location, Sound.BLOCK_BEACON_POWER_SELECT, 1.0f, 1.2f);
        world.playSound(location, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1.0f, 1.5f);
        
        // Визуальные эффекты
        new BukkitRunnable() {
            int ticks = 0;
            
            @Override
            public void run() {
                if (ticks >= 60) {
                    cancel();
                    return;
                }
                
                // Частицы вокруг терминала
                if (terminalLocation != null) {
                    for (int i = 0; i < 10; i++) {
                        double angle = (ticks + i) * 0.3;
                        double x = terminalLocation.getX() + Math.cos(angle) * 2;
                        double z = terminalLocation.getZ() + Math.sin(angle) * 2;
                        double y = terminalLocation.getY() + 1 + Math.sin(ticks * 0.2) * 0.5;
                        
                        world.spawnParticle(Particle.ENCHANTMENT_TABLE, x, y, z, 1, 0, 0, 0, 0.1);
                        world.spawnParticle(Particle.PORTAL, x, y, z, 1, 0.1, 0.1, 0.1, 0.05);
                    }
                }
                
                // Энергетические импульсы
                if (ticks % 10 == 0) {
                    world.spawnParticle(Particle.ELECTRIC_SPARK, 
                        location.getX(), location.getY() + 8, location.getZ(), 
                        20, 3, 3, 3, 0.1);
                }
                
                ticks++;
            }
        }.runTaskTimer(plugin, 0, 2);
        
        // Уведомление владельца
        Player owner = Bukkit.getPlayer(this.owner);
        if (owner != null && owner.isOnline()) {
            owner.sendMessage(ChatColor.BLUE + "🔬 Исследование '" + researchName + "' начато!");
            owner.sendMessage(ChatColor.GRAY + "Прогресс можно отслеживать в терминале исследований.");
        }
    }
    
    public void playResearchCompleteEffect(String researchName) {
        World world = location.getWorld();
        if (world == null) return;
        
        // Звуковые эффекты
        world.playSound(location, Sound.UI_TOAST_CHALLENGE_COMPLETE, 2.0f, 1.0f);
        world.playSound(location, Sound.BLOCK_BEACON_ACTIVATE, 1.0f, 1.5f);
        
        // Визуальные эффекты
        new BukkitRunnable() {
            int ticks = 0;
            
            @Override
            public void run() {
                if (ticks >= 80) {
                    cancel();
                    return;
                }
                
                // Фейерверк
                world.spawnParticle(Particle.FIREWORKS_SPARK, 
                    location.getX(), location.getY() + 10, location.getZ(), 
                    50, 5, 5, 5, 0.3);
                
                // Золотые частицы
                for (int i = 0; i < 20; i++) {
                    double angle = Math.random() * Math.PI * 2;
                    double radius = Math.random() * 10;
                    double x = location.getX() + Math.cos(angle) * radius;
                    double z = location.getZ() + Math.sin(angle) * radius;
                    double y = location.getY() + Math.random() * 15;
                    
                    world.spawnParticle(Particle.TOTEM, x, y, z, 1, 0, 0, 0, 0);
                }
                
                // Энергетические волны
                if (ticks % 15 == 0) {
                    for (int r = 2; r <= 15; r += 3) {
                        for (int deg = 0; deg < 360; deg += 15) {
                            double radians = Math.toRadians(deg);
                            double x = location.getX() + Math.cos(radians) * r;
                            double z = location.getZ() + Math.sin(radians) * r;
                            
                            world.spawnParticle(Particle.ENCHANTMENT_TABLE, 
                                x, location.getY() + 2, z, 1, 0, 0, 0, 0.1);
                        }
                    }
                }
                
                ticks++;
            }
        }.runTaskTimer(plugin, 0, 2);
        
        // Уведомление владельца
        Player owner = Bukkit.getPlayer(this.owner);
        if (owner != null && owner.isOnline()) {
            owner.sendMessage(ChatColor.GOLD + "🎉 Исследование '" + researchName + "' завершено!");
            owner.sendMessage(ChatColor.GREEN + "Новая технология доступна для производства!");
            owner.playSound(owner.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
        }
    }
    
    private void startAutomationTask() {
        automationTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (state == LaboratoryState.OPERATIONAL) {
                    try {
                        // Обновляем энергию
                        updateEnergy();
                        
                        // Обновляем исследования
                        updateResearch();
                        
                        // Автоматизация - только если включена
                        if (autoResearch) {
                            processAutoResearch();
                        }
                        
                        if (autoUpgrade) {
                            processAutoUpgrade();
                        }
                    } catch (Exception e) {
                        plugin.getLogger().warning("Ошибка в автоматизации лаборатории: " + e.getMessage());
                    }
                }
            }
        }.runTaskTimer(plugin, 20, 40); // Каждые 2 секунды вместо каждую секунду
    }
    
    private void updateEnergy() {
        int netGeneration = energyGeneration - energyConsumption;
        currentEnergy = Math.max(0, Math.min(level.getMaxEnergy(), currentEnergy + netGeneration));
    }
    
    private void updateResearch() {
        Iterator<Map.Entry<String, ResearchProject>> iterator = activeResearch.entrySet().iterator();
        
        while (iterator.hasNext()) {
            Map.Entry<String, ResearchProject> entry = iterator.next();
            ResearchProject project = entry.getValue();
            
            if (currentEnergy >= project.getEnergyPerTick()) {
                currentEnergy -= project.getEnergyPerTick();
                project.addProgress(1.0 * project.getSpeedMultiplier());
                
                if (project.isCompleted()) {
                    String researchName = entry.getKey();
                    completedResearch.add(researchName);
                    iterator.remove();
                    
                    playResearchCompleteEffect(researchName);
                    
                    // Начинаем следующее исследование из очереди
                    if (!researchQueue.isEmpty() && activeResearch.size() < level.getMaxResearch()) {
                        String nextResearch = researchQueue.poll();
                        // Здесь нужно получить требования для исследования
                        Map<Material, Integer> requirements = getResearchRequirements(nextResearch);
                        if (requirements != null) {
                            startResearch(nextResearch, requirements);
                        }
                    }
                }
            }
        }
    }
    
    private void processAutoResearch() {
        if (activeResearch.size() < level.getMaxResearch() && !researchQueue.isEmpty()) {
            String nextResearch = researchQueue.poll();
            Map<Material, Integer> requirements = getResearchRequirements(nextResearch);
            if (requirements != null) {
                startResearch(nextResearch, requirements);
            }
        }
    }
    
    private void processAutoUpgrade() {
        if (level.getLevel() < 5 && canUpgrade()) {
            upgradeLevel();
        }
    }
    
    private Map<Material, Integer> getResearchRequirements(String researchType) {
        Map<Material, Integer> requirements = new HashMap<>();
        
        switch (researchType.toLowerCase()) {
            case "защитный костюм":
                requirements.put(Material.IRON_INGOT, 16);
                requirements.put(Material.LEATHER, 8);
                requirements.put(Material.REDSTONE, 4);
                break;
            case "урановая капсула":
                requirements.put(Material.EMERALD, 4);
                requirements.put(Material.IRON_INGOT, 8);
                requirements.put(Material.REDSTONE, 16);
                break;
            case "силовая броня":
                requirements.put(Material.DIAMOND, 8);
                requirements.put(Material.IRON_BLOCK, 4);
                requirements.put(Material.REDSTONE_BLOCK, 2);
                break;
            case "автоматический шахтёр":
                requirements.put(Material.IRON_BLOCK, 12);
                requirements.put(Material.REDSTONE_BLOCK, 8);
                requirements.put(Material.DIAMOND, 4);
                break;
            case "рельсотрон":
                requirements.put(Material.IRON_BLOCK, 16);
                requirements.put(Material.REDSTONE_BLOCK, 12);
                requirements.put(Material.EMERALD, 8);
                break;
            case "электротранспорт":
                requirements.put(Material.IRON_INGOT, 24);
                requirements.put(Material.REDSTONE, 32);
                requirements.put(Material.GOLD_INGOT, 8);
                break;
            default:
                requirements.put(Material.IRON_INGOT, 8);
                requirements.put(Material.REDSTONE, 16);
                break;
        }
        
        return requirements;
    }
    
    public boolean startResearch(String researchName, Map<Material, Integer> requirements) {
        if (activeResearch.size() >= level.getMaxResearch()) {
            researchQueue.offer(researchName);
            return false;
        }
        
        if (completedResearch.contains(researchName)) {
            return false;
        }
        
        // Проверяем наличие материалов
        for (Map.Entry<Material, Integer> entry : requirements.entrySet()) {
            if (!storage.hasMaterial(entry.getKey(), entry.getValue())) {
                return false;
            }
        }
        
        // Забираем материалы
        for (Map.Entry<Material, Integer> entry : requirements.entrySet()) {
            storage.removeMaterial(entry.getKey(), entry.getValue());
        }
        
        // Создаем проект исследования
        ResearchProject project = new ResearchProject(researchName, requirements, specialization);
        activeResearch.put(researchName, project);
        
        // Увеличиваем потребление энергии
        energyConsumption += project.getEnergyPerTick();
        
        playResearchStartEffect(researchName);
        
        return true;
    }
    
    public boolean upgradeLevel() {
        if (level.getLevel() >= 5) return false;
        
        LaboratoryLevel nextLevel = LaboratoryLevel.getByLevel(level.getLevel() + 1);
        if (nextLevel == null) return false;
        
        // Проверяем требования для улучшения
        Map<Material, Integer> upgradeRequirements = getUpgradeRequirements(nextLevel);
        for (Map.Entry<Material, Integer> entry : upgradeRequirements.entrySet()) {
            if (!storage.hasMaterial(entry.getKey(), entry.getValue())) {
                return false;
            }
        }
        
        // Забираем материалы
        for (Map.Entry<Material, Integer> entry : upgradeRequirements.entrySet()) {
            storage.removeMaterial(entry.getKey(), entry.getValue());
        }
        
        state = LaboratoryState.UPGRADING;
        
        // Запускаем процесс улучшения
        new BukkitRunnable() {
            int progress = 0;
            
            @Override
            public void run() {
                progress += 10;
                
                if (progress >= 100) {
                    level = nextLevel;
                    state = LaboratoryState.OPERATIONAL;
                    
                    // Увеличиваем характеристики
                    storage.setMaxCapacity(level.getMaxEnergy() / 10);
                    
                    // Эффекты улучшения
                    playLaboratoryActivationEffect();
                    
                    Player owner = Bukkit.getPlayer(Laboratory.this.owner);
                    if (owner != null && owner.isOnline()) {
                        owner.sendMessage(ChatColor.GOLD + "🔧 Лаборатория улучшена до уровня " + level.getLevel() + "!");
                        owner.sendMessage(ChatColor.GREEN + "Новые возможности разблокированы!");
                    }
                    
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 0, 60); // Каждые 3 секунды
        
        return true;
    }
    
    private Map<Material, Integer> getUpgradeRequirements(LaboratoryLevel targetLevel) {
        Map<Material, Integer> requirements = new HashMap<>();
        
        int level = targetLevel.getLevel();
        requirements.put(Material.IRON_BLOCK, level * 16);
        requirements.put(Material.REDSTONE_BLOCK, level * 8);
        requirements.put(Material.DIAMOND, level * 4);
        requirements.put(Material.EMERALD, level * 2);
        
        if (level >= 3) {
            requirements.put(Material.NETHER_STAR, level - 2);
        }
        
        return requirements;
    }
    
    private boolean canUpgrade() {
        if (level.getLevel() >= 5) return false;
        
        LaboratoryLevel nextLevel = LaboratoryLevel.getByLevel(level.getLevel() + 1);
        if (nextLevel == null) return false;
        
        Map<Material, Integer> requirements = getUpgradeRequirements(nextLevel);
        for (Map.Entry<Material, Integer> entry : requirements.entrySet()) {
            if (!storage.hasMaterial(entry.getKey(), entry.getValue())) {
                return false;
            }
        }
        
        return true;
    }
    
    public void stopAutomation() {
        if (automationTask != null) {
            automationTask.cancel();
        }
    }
    
    // ==================== GETTERS AND SETTERS ====================
    
    public Location getLocation() { return location; }
    public UUID getOwner() { return owner; }
    public LaboratoryLevel getLevel() { return level; }
    public LaboratorySpecialization getSpecialization() { return specialization; }
    public LaboratoryState getState() { return state; }
    public int getConstructionProgress() { return constructionProgress; }
    public int getCurrentEnergy() { return currentEnergy; }
    public int getMaxEnergy() { return level.getMaxEnergy(); }
    public int getEnergyGeneration() { return energyGeneration; }
    public int getEnergyConsumption() { return energyConsumption; }
    public double getEnergyPercentage() { return (double) currentEnergy / level.getMaxEnergy() * 100; }
    public LaboratoryStorage getStorage() { return storage; }
    public Map<String, ResearchProject> getActiveResearch() { return activeResearch; }
    public Set<String> getCompletedResearch() { return completedResearch; }
    public Queue<String> getResearchQueue() { return researchQueue; }
    public boolean isAutoResearch() { return autoResearch; }
    public boolean isAutoUpgrade() { return autoUpgrade; }
    public Map<Material, Integer> getRequiredMaterials() { return requiredMaterials; }
    public Location getTerminalLocation() { return terminalLocation; }
    
    public void setSpecialization(LaboratorySpecialization specialization) { 
        this.specialization = specialization; 
    }
    public void setAutoResearch(boolean autoResearch) { this.autoResearch = autoResearch; }
    public void setAutoUpgrade(boolean autoUpgrade) { this.autoUpgrade = autoUpgrade; }
    
    public boolean isResearchCompleted(String researchName) {
        return completedResearch.contains(researchName);
    }
    
    public int getStorageUsage() {
        return storage.getTotalItems();
    }
    
    public int getMaxStorageCapacity() {
        return storage.getMaxCapacity();
    }
}
