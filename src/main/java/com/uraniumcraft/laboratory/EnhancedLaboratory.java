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
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.Orientable;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import com.uraniumcraft.UraniumPlugin;

import java.util.*;

public class EnhancedLaboratory {
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
    private final Map<String, EnhancedResearchProject> activeResearch;
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
    
    public EnhancedLaboratory(Location location, UUID owner, UraniumPlugin plugin) {
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
        Location base = location.clone();
        int size = 6 + level.getLevel(); // Размер 7x7 для уровня 1
        
        // Очищаем область (больше места)
        clearArea(base, size + 2, 12);
        
        // Строим многоуровневую лабораторию
        buildFoundationAndBasement(base, size);
        buildMainFloor(base, size);
        buildSecondFloor(base, size);
        buildRooftopLevel(base, size);
        buildExteriorStructures(base, size);
        
        // Внутреннее оборудование
        buildAdvancedInterior(base, size);
        
        // Терминал исследований в центре
        buildAdvancedResearchTerminal(base);
        
        // Энергетические системы
        buildAdvancedEnergySystem(base, size);
        
        // Освещение и декор
        buildAdvancedLighting(base, size);
        buildAdvancedDecorations(base, size);
        
        // Защитные системы
        buildSecuritySystems(base, size);
    }
    
    private void clearArea(Location base, int size, int height) {
        for (int x = -size; x <= size; x++) {
            for (int z = -size; z <= size; z++) {
                for (int y = -2; y <= height; y++) {
                    Block block = base.clone().add(x, y, z).getBlock();
                    if (block.getType() != Material.BEDROCK) {
                        block.setType(Material.AIR);
                    }
                }
            }
        }
    }
    
    private void buildFoundationAndBasement(Location base, int size) {
        // Подземный уровень (-2 до -1)
        for (int y = -2; y <= -1; y++) {
            for (int x = -size; x <= size; x++) {
                for (int z = -size; z <= size; z++) {
                    Block block = base.clone().add(x, y, z).getBlock();
                    
                    // Стены подвала
                    if (x == -size || x == size || z == -size || z == size) {
                        if (y == -2) {
                            block.setType(Material.OBSIDIAN);
                        } else {
                            block.setType(Material.PRISMARINE_BRICKS);
                        }
                    }
                    // Пол подвала
                    else if (y == -2) {
                        if ((x + z) % 2 == 0) {
                            block.setType(Material.PRISMARINE);
                        } else {
                            block.setType(Material.DARK_PRISMARINE);
                        }
                    }
                }
            }
        }
        
        // Основание лаборатории (уровень 0)
        for (int x = -size; x <= size; x++) {
            for (int z = -size; z <= size; z++) {
                Block block = base.clone().add(x, 0, z).getBlock();
                
                // Периметр из обсидиана
                if (x == -size || x == size || z == -size || z == size) {
                    block.setType(Material.OBSIDIAN);
                }
                // Внутренняя часть - красивый узор
                else {
                    if (Math.abs(x) + Math.abs(z) <= 2) {
                        block.setType(Material.DIAMOND_BLOCK);
                    } else if ((x + z) % 3 == 0) {
                        block.setType(Material.QUARTZ_BLOCK);
                    } else {
                        block.setType(Material.IRON_BLOCK);
                    }
                }
            }
        }
    }
    
    private void buildMainFloor(Location base, int size) {
        // Основной этаж (1-4 уровни)
        for (int y = 1; y <= 4; y++) {
            for (int x = -size; x <= size; x++) {
                for (int z = -size; z <= size; z++) {
                    if (x == -size || x == size || z == -size || z == size) {
                        Block block = base.clone().add(x, y, z).getBlock();
                        
                        // Углы - железные блоки с деталями
                        if ((Math.abs(x) == size && Math.abs(z) == size)) {
                            if (y <= 2) {
                                block.setType(Material.IRON_BLOCK);
                            } else {
                                block.setType(Material.QUARTZ_PILLAR);
                            }
                        }
                        // Большие окна
                        else if (y >= 2 && y <= 3) {
                            if ((x == -size || x == size) && z % 2 == 0 && Math.abs(z) < size - 1) {
                                block.setType(Material.GLASS);
                            } else if ((z == -size || z == size) && x % 2 == 0 && Math.abs(x) < size - 1) {
                                block.setType(Material.GLASS);
                            } else {
                                block.setType(Material.QUARTZ_BLOCK);
                            }
                        }
                        // Основные стены
                        else {
                            if (y == 1 || y == 4) {
                                block.setType(Material.QUARTZ_BLOCK);
                            } else {
                                block.setType(Material.IRON_BLOCK);
                            }
                        }
                    }
                }
            }
        }
        
        // Входы с красивыми арками
        buildEntrances(base, size);
    }
    
    private void buildEntrances(Location base, int size) {
        // Главный вход (южная сторона)
        for (int x = -1; x <= 1; x++) {
            for (int y = 1; y <= 3; y++) {
                Block block = base.clone().add(x, y, size).getBlock();
                if (y <= 2) {
                    block.setType(Material.AIR);
                } else {
                    // Арка
                    if (x == 0) {
                        block.setType(Material.QUARTZ_BLOCK);
                    } else {
                        block.setType(Material.AIR);
                    }
                }
            }
        }
        
        // Декоративные колонны у входа
        for (int y = 0; y <= 4; y++) {
            base.clone().add(-2, y, size + 1).getBlock().setType(Material.QUARTZ_PILLAR);
            base.clone().add(2, y, size + 1).getBlock().setType(Material.QUARTZ_PILLAR);
        }
        
        // Боковые входы
        base.clone().add(-size, 1, 0).getBlock().setType(Material.AIR);
        base.clone().add(-size, 2, 0).getBlock().setType(Material.AIR);
        base.clone().add(size, 1, 0).getBlock().setType(Material.AIR);
        base.clone().add(size, 2, 0).getBlock().setType(Material.AIR);
    }
    
    private void buildSecondFloor(Location base, int size) {
        // Второй этаж (уровень 5)
        for (int x = -size; x <= size; x++) {
            for (int z = -size; z <= size; z++) {
                Block block = base.clone().add(x, 5, z).getBlock();
                
                // Крыша с узором
                if (x == -size || x == size || z == -size || z == size) {
                    block.setType(Material.QUARTZ_BLOCK);
                } else if (Math.abs(x) % 2 == 0 && Math.abs(z) % 2 == 0) {
                    block.setType(Material.IRON_BLOCK);
                } else {
                    block.setType(Material.QUARTZ_BLOCK);
                }
            }
        }
        
        // Стены второго этажа (6-8 уровни) - меньше размером
        int secondFloorSize = size - 2;
        for (int y = 6; y <= 8; y++) {
            for (int x = -secondFloorSize; x <= secondFloorSize; x++) {
                for (int z = -secondFloorSize; z <= secondFloorSize; z++) {
                    if (x == -secondFloorSize || x == secondFloorSize || z == -secondFloorSize || z == secondFloorSize) {
                        Block block = base.clone().add(x, y, z).getBlock();
                        
                        if (y == 7 && (x % 2 == 0 || z % 2 == 0)) {
                            block.setType(Material.GLASS);
                        } else {
                            block.setType(Material.PRISMARINE_BRICKS);
                        }
                    }
                }
            }
        }
        
        // Крыша второго этажа
        for (int x = -secondFloorSize; x <= secondFloorSize; x++) {
            for (int z = -secondFloorSize; z <= secondFloorSize; z++) {
                Block block = base.clone().add(x, 9, z).getBlock();
                block.setType(Material.PRISMARINE);
            }
        }
    }
    
    private void buildRooftopLevel(Location base, int size) {
        // Центральная башня на крыше
        int towerSize = 2;
        for (int y = 10; y <= 12; y++) {
            for (int x = -towerSize; x <= towerSize; x++) {
                for (int z = -towerSize; z <= towerSize; z++) {
                    if (x == -towerSize || x == towerSize || z == -towerSize || z == towerSize) {
                        Block block = base.clone().add(x, y, z).getBlock();
                        
                        if (y == 11 && (x == 0 || z == 0)) {
                            block.setType(Material.GLASS);
                        } else {
                            block.setType(Material.QUARTZ_BLOCK);
                        }
                    }
                }
            }
        }
        
        // Крыша башни
        for (int x = -towerSize; x <= towerSize; x++) {
            for (int z = -towerSize; z <= towerSize; z++) {
                base.clone().add(x, 13, z).getBlock().setType(Material.DIAMOND_BLOCK);
            }
        }
    }
    
    private void buildExteriorStructures(Location base, int size) {
        // Внешние генераторы по углам
        int[] corners = {-size - 2, size + 2};
        for (int x : corners) {
            for (int z : corners) {
                // Основание генератора
                for (int gx = x - 1; gx <= x + 1; gx++) {
                    for (int gz = z - 1; gz <= z + 1; gz++) {
                        base.clone().add(gx, 0, gz).getBlock().setType(Material.IRON_BLOCK);
                    }
                }
                
                // Столб генератора
                for (int y = 1; y <= 6; y++) {
                    Block block = base.clone().add(x, y, z).getBlock();
                    if (y == 6) {
                        block.setType(Material.BEACON);
                    } else if (y % 2 == 0) {
                        block.setType(Material.REDSTONE_BLOCK);
                    } else {
                        block.setType(Material.IRON_BLOCK);
                    }
                }
                
                // Защитные барьеры вокруг генераторов
                for (int gx = x - 2; gx <= x + 2; gx++) {
                    for (int gz = z - 2; gz <= z + 2; gz++) {
                        if (Math.abs(gx - x) == 2 || Math.abs(gz - z) == 2) {
                            if ((gx + gz) % 2 == 0) {
                                base.clone().add(gx, 1, gz).getBlock().setType(Material.IRON_BARS);
                            }
                        }
                    }
                }
            }
        }
        
        // Соединительные мостики
        buildConnectingBridges(base, size);
    }
    
    private void buildConnectingBridges(Location base, int size) {
        // Мостики к генераторам
        int bridgeY = 3;
        
        // К северо-западному генератору
        for (int i = -size; i >= -size - 2; i--) {
            base.clone().add(i, bridgeY, -size).getBlock().setType(Material.QUARTZ_SLAB);
            base.clone().add(i, bridgeY + 1, -size).getBlock().setType(Material.IRON_BARS);
        }
        
        // К северо-восточному генератору
        for (int i = size; i <= size + 2; i++) {
            base.clone().add(i, bridgeY, -size).getBlock().setType(Material.QUARTZ_SLAB);
            base.clone().add(i, bridgeY + 1, -size).getBlock().setType(Material.IRON_BARS);
        }
        
        // К юго-западному генератору
        for (int i = -size; i >= -size - 2; i--) {
            base.clone().add(i, bridgeY, size).getBlock().setType(Material.QUARTZ_SLAB);
            base.clone().add(i, bridgeY + 1, size).getBlock().setType(Material.IRON_BARS);
        }
        
        // К юго-восточному генератору
        for (int i = size; i <= size + 2; i++) {
            base.clone().add(i, bridgeY, size).getBlock().setType(Material.QUARTZ_SLAB);
            base.clone().add(i, bridgeY + 1, size).getBlock().setType(Material.IRON_BARS);
        }
    }
    
    private void buildAdvancedInterior(Location base, int size) {
        // Первый этаж - основные рабочие станции
        buildMainFloorInterior(base, size);
        
        // Второй этаж - специализированное оборудование
        buildSecondFloorInterior(base, size - 2);
        
        // Подвал - хранилища и энергосистемы
        buildBasementInterior(base, size);
    }
    
    private void buildMainFloorInterior(Location base, int size) {
        // Рабочие станции по периметру
        for (int i = -size + 2; i <= size - 2; i += 2) {
            // Северная стена
            if (i != 0) { // Оставляем место для центрального терминала
                base.clone().add(i, 1, -size + 2).getBlock().setType(Material.CRAFTING_TABLE);
                base.clone().add(i, 2, -size + 2).getBlock().setType(Material.ITEM_FRAME);
                base.clone().add(i + 1, 1, -size + 2).getBlock().setType(Material.FURNACE);
            }
            
            // Южная стена
            if (i != 0) {
                base.clone().add(i, 1, size - 2).getBlock().setType(Material.ENCHANTING_TABLE);
                base.clone().add(i + 1, 1, size - 2).getBlock().setType(Material.ANVIL);
            }
            
            // Восточная стена
            if (Math.abs(i) < size - 2) {
                base.clone().add(size - 2, 1, i).getBlock().setType(Material.BREWING_STAND);
                base.clone().add(size - 2, 2, i).getBlock().setType(Material.ITEM_FRAME);
            }
            
            // Западная стена
            if (Math.abs(i) < size - 2) {
                base.clone().add(-size + 2, 1, i).getBlock().setType(Material.SMITHING_TABLE);
                base.clone().add(-size + 2, 2, i).getBlock().setType(Material.ITEM_FRAME);
            }
        }
        
        // Хранилища в углах
        base.clone().add(-size + 1, 1, -size + 1).getBlock().setType(Material.CHEST);
        base.clone().add(size - 1, 1, -size + 1).getBlock().setType(Material.CHEST);
        base.clone().add(-size + 1, 1, size - 1).getBlock().setType(Material.CHEST);
        base.clone().add(size - 1, 1, size - 1).getBlock().setType(Material.CHEST);
        
        // Дополнительные сундуки на втором уровне углов
        base.clone().add(-size + 1, 2, -size + 1).getBlock().setType(Material.CHEST);
        base.clone().add(size - 1, 2, -size + 1).getBlock().setType(Material.CHEST);
        base.clone().add(-size + 1, 2, size - 1).getBlock().setType(Material.CHEST);
        base.clone().add(size - 1, 2, size - 1).getBlock().setType(Material.CHEST);
        
        // Декоративные элементы
        for (int x = -2; x <= 2; x += 4) {
            for (int z = -2; z <= 2; z += 4) {
                if (x != 0 || z != 0) {
                    base.clone().add(x, 1, z).getBlock().setType(Material.LECTERN);
                    base.clone().add(x, 4, z).getBlock().setType(Material.SEA_LANTERN);
                }
            }
        }
    }
    
    private void buildSecondFloorInterior(Location base, int secondFloorSize) {
        // Специализированные станции на втором этаже
        base.clone().add(-1, 6, -1).getBlock().setType(Material.BEACON);
        base.clone().add(1, 6, -1).getBlock().setType(Material.BEACON);
        base.clone().add(-1, 6, 1).getBlock().setType(Material.BEACON);
        base.clone().add(1, 6, 1).getBlock().setType(Material.BEACON);
        
        // Продвинутые рабочие станции
        base.clone().add(-secondFloorSize + 1, 6, 0).getBlock().setType(Material.CARTOGRAPHY_TABLE);
        base.clone().add(secondFloorSize - 1, 6, 0).getBlock().setType(Material.LOOM);
        base.clone().add(0, 6, -secondFloorSize + 1).getBlock().setType(Material.GRINDSTONE);
        base.clone().add(0, 6, secondFloorSize - 1).getBlock().setType(Material.STONECUTTER);
        
        // Центральный процессор второго этажа
        base.clone().add(0, 6, 0).getBlock().setType(Material.EMERALD_BLOCK);
        base.clone().add(0, 7, 0).getBlock().setType(Material.END_ROD);
    }
    
    private void buildBasementInterior(Location base, int size) {
        // Энергетические системы в подвале
        for (int x = -size + 2; x <= size - 2; x += 3) {
            for (int z = -size + 2; z <= size - 2; z += 3) {
                base.clone().add(x, -1, z).getBlock().setType(Material.REDSTONE_BLOCK);
                
                // Энергетические кабели
                if (x != 0) {
                    base.clone().add(x > 0 ? x - 1 : x + 1, -1, z).getBlock().setType(Material.REDSTONE_WIRE);
                }
                if (z != 0) {
                    base.clone().add(x, -1, z > 0 ? z - 1 : z + 1).getBlock().setType(Material.REDSTONE_WIRE);
                }
            }
        }
        
        // Центральный энергетический узел
        base.clone().add(0, -1, 0).getBlock().setType(Material.BEACON);
        
        // Хранилища ресурсов в подвале
        base.clone().add(-3, -1, -3).getBlock().setType(Material.BARREL);
        base.clone().add(3, -1, -3).getBlock().setType(Material.BARREL);
        base.clone().add(-3, -1, 3).getBlock().setType(Material.BARREL);
        base.clone().add(3, -1, 3).getBlock().setType(Material.BARREL);
        
        // Системы охлаждения
        for (int i = -2; i <= 2; i += 2) {
            base.clone().add(i, -1, 0).getBlock().setType(Material.PACKED_ICE);
            base.clone().add(0, -1, i).getBlock().setType(Material.PACKED_ICE);
        }
    }
    
    private void buildAdvancedResearchTerminal(Location base) {
        // Центральный терминал исследований - более сложная конструкция
        terminalLocation = base.clone().add(0, 1, 0);
        
        // Основание терминала - платформа 3x3
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                Block block = base.clone().add(x, 1, z).getBlock();
                if (x == 0 && z == 0) {
                    block.setType(Material.DIAMOND_BLOCK);
                } else if (Math.abs(x) + Math.abs(z) == 1) {
                    block.setType(Material.EMERALD_BLOCK);
                } else {
                    block.setType(Material.QUARTZ_BLOCK);
                }
            }
        }
        
        // Главный экран терминала
        base.clone().add(0, 2, 0).getBlock().setType(Material.BEACON);
        
        // Консоли управления вокруг
        base.clone().add(1, 1, 0).getBlock().setType(Material.LECTERN);
        base.clone().add(-1, 1, 0).getBlock().setType(Material.LECTERN);
        base.clone().add(0, 1, 1).getBlock().setType(Material.LECTERN);
        base.clone().add(0, 1, -1).getBlock().setType(Material.LECTERN);
        
        // Процессоры по углам
        base.clone().add(1, 1, 1).getBlock().setType(Material.EMERALD_BLOCK);
        base.clone().add(-1, 1, 1).getBlock().setType(Material.EMERALD_BLOCK);
        base.clone().add(1, 1, -1).getBlock().setType(Material.EMERALD_BLOCK);
        base.clone().add(-1, 1, -1).getBlock().setType(Material.EMERALD_BLOCK);
        
        // Голографические проекторы
        base.clone().add(2, 2, 0).getBlock().setType(Material.END_ROD);
        base.clone().add(-2, 2, 0).getBlock().setType(Material.END_ROD);
        base.clone().add(0, 2, 2).getBlock().setType(Material.END_ROD);
        base.clone().add(0, 2, -2).getBlock().setType(Material.END_ROD);
        
        // Верхняя антенна
        base.clone().add(0, 3, 0).getBlock().setType(Material.LIGHTNING_ROD);
        
        // Энергетические кристаллы вокруг терминала
        for (int i = 0; i < 8; i++) {
            double angle = i * Math.PI / 4;
            int x = (int) Math.round(Math.cos(angle) * 3);
            int z = (int) Math.round(Math.sin(angle) * 3);
            
            base.clone().add(x, 1, z).getBlock().setType(Material.AMETHYST_BLOCK);
            base.clone().add(x, 2, z).getBlock().setType(Material.AMETHYST_CLUSTER);
        }
    }
    
    private void buildAdvancedEnergySystem(Location base, int size) {
        // Главные энергогенераторы на крыше
        base.clone().add(-2, 6, -2).getBlock().setType(Material.BEACON);
        base.clone().add(2, 6, -2).getBlock().setType(Material.BEACON);
        base.clone().add(-2, 6, 2).getBlock().setType(Material.BEACON);
        base.clone().add(2, 6, 2).getBlock().setType(Material.BEACON);
        
        // Энергетические кабели на крыше
        for (int i = -2; i <= 2; i++) {
            if (i != 0) {
                base.clone().add(i, 5, 0).getBlock().setType(Material.REDSTONE_BLOCK);
                base.clone().add(0, 5, i).getBlock().setType(Material.REDSTONE_BLOCK);
            }
        }
        
        // Центральный энергетический узел на крыше
        base.clone().add(0, 5, 0).getBlock().setType(Material.NETHER_STAR.createBlockData().getMaterial());
        
        // Дополнительные генераторы по периметру
        for (int i = -size + 1; i <= size - 1; i += (size - 1)) {
            base.clone().add(i, 3, 0).getBlock().setType(Material.REDSTONE_LAMP);
            base.clone().add(0, 3, i).getBlock().setType(Material.REDSTONE_LAMP);
        }
        
        // Энергетические стабилизаторы
        base.clone().add(-1, 10, -1).getBlock().setType(Material.END_ROD);
        base.clone().add(1, 10, -1).getBlock().setType(Material.END_ROD);
        base.clone().add(-1, 10, 1).getBlock().setType(Material.END_ROD);
        base.clone().add(1, 10, 1).getBlock().setType(Material.END_ROD);
    }
    
    private void buildAdvancedLighting(Location base, int size) {
        // Основное освещение по периметру
        for (int i = -size + 1; i <= size - 1; i += 2) {
            // Внутреннее освещение на разных уровнях
            base.clone().add(i, 4, -size + 2).getBlock().setType(Material.SEA_LANTERN);
            base.clone().add(i, 4, size - 2).getBlock().setType(Material.SEA_LANTERN);
            base.clone().add(-size + 2, 4, i).getBlock().setType(Material.SEA_LANTERN);
            base.clone().add(size - 2, 4, i).getBlock().setType(Material.SEA_LANTERN);
            
            // Внешнее освещение
            if (Math.abs(i) < size) {
                base.clone().add(i, 2, -size - 1).getBlock().setType(Material.GLOWSTONE);
                base.clone().add(i, 2, size + 1).getBlock().setType(Material.GLOWSTONE);
                base.clone().add(-size - 1, 2, i).getBlock().setType(Material.GLOWSTONE);
                base.clone().add(size + 1, 2, i).getBlock().setType(Material.GLOWSTONE);
            }
        }
        
        // Центральное освещение на разных этажах
        base.clone().add(0, 4, 0).getBlock().setType(Material.SEA_LANTERN);
        base.clone().add(0, 8, 0).getBlock().setType(Material.GLOWSTONE);
        
        // Декоративное освещение в углах
        base.clone().add(-size + 1, 3, -size + 1).getBlock().setType(Material.LANTERN);
        base.clone().add(size - 1, 3, -size + 1).getBlock().setType(Material.LANTERN);
        base.clone().add(-size + 1, 3, size - 1).getBlock().setType(Material.LANTERN);
        base.clone().add(size - 1, 3, size - 1).getBlock().setType(Material.LANTERN);
        
        // Подсветка входов
        base.clone().add(-1, 3, size + 1).getBlock().setType(Material.SOUL_LANTERN);
        base.clone().add(1, 3, size + 1).getBlock().setType(Material.SOUL_LANTERN);
    }
    
    private void buildAdvancedDecorations(Location base, int size) {
        // Главная антенна на башне
        for (int y = 14; y <= 18; y++) {
            base.clone().add(0, y, 0).getBlock().setType(Material.IRON_BARS);
        }
        base.clone().add(0, 19, 0).getBlock().setType(Material.LIGHTNING_ROD);
        
        // Боковые антенны
        base.clone().add(-1, 14, -1).getBlock().setType(Material.IRON_BARS);
        base.clone().add(1, 14, -1).getBlock().setType(Material.IRON_BARS);
        base.clone().add(-1, 14, 1).getBlock().setType(Material.IRON_BARS);
        base.clone().add(1, 14, 1).getBlock().setType(Material.IRON_BARS);
        
        // Вентиляционные системы на крыше
        base.clone().add(-3, 6, -3).getBlock().setType(Material.IRON_TRAPDOOR);
        base.clone().add(3, 6, -3).getBlock().setType(Material.IRON_TRAPDOOR);
        base.clone().add(-3, 6, 3).getBlock().setType(Material.IRON_TRAPDOOR);
        base.clone().add(3, 6, 3).getBlock().setType(Material.IRON_TRAPDOOR);
        
        // Декоративные панели на стенах
        for (int y = 2; y <= 3; y++) {
            base.clone().add(-size, y, -2).getBlock().setType(Material.IRON_TRAPDOOR);
            base.clone().add(-size, y, 2).getBlock().setType(Material.IRON_TRAPDOOR);
            base.clone().add(size, y, -2).getBlock().setType(Material.IRON_TRAPDOOR);
            base.clone().add(size, y, 2).getBlock().setType(Material.IRON_TRAPDOOR);
        }
        
        // Голографические проекторы по углам
        base.clone().add(-size + 1, 5, -size + 1).getBlock().setType(Material.END_ROD);
        base.clone().add(size - 1, 5, -size + 1).getBlock().setType(Material.END_ROD);
        base.clone().add(-size + 1, 5, size - 1).getBlock().setType(Material.END_ROD);
        base.clone().add(size - 1, 5, size - 1).getBlock().setType(Material.END_ROD);
        
        // Энергетические кристаллы на внешних углах
        base.clone().add(-size - 1, 1, -size - 1).getBlock().setType(Material.AMETHYST_BLOCK);
        base.clone().add(size + 1, 1, -size - 1).getBlock().setType(Material.AMETHYST_BLOCK);
        base.clone().add(-size - 1, 1, size + 1).getBlock().setType(Material.AMETHYST_BLOCK);
        base.clone().add(size + 1, 1, size + 1).getBlock().setType(Material.AMETHYST_BLOCK);
        
        // Декоративные флаги
        for (int i = -size + 3; i <= size - 3; i += 3) {
            base.clone().add(i, 5, -size).getBlock().setType(Material.BANNER);
            base.clone().add(i, 5, size).getBlock().setType(Material.BANNER);
        }
    }
    
    private void buildSecuritySystems(Location base, int size) {
        // Защитный периметр
        for (int i = -size - 3; i <= size + 3; i++) {
            if (i % 4 == 0) {
                // Защитные столбы
                for (int y = 0; y <= 3; y++) {
                    base.clone().add(i, y, -size - 3).getBlock().setType(Material.IRON_BARS);
                    base.clone().add(i, y, size + 3).getBlock().setType(Material.IRON_BARS);
                    base.clone().add(-size - 3, y, i).getBlock().setType(Material.IRON_BARS);
                    base.clone().add(size + 3, y, i).getBlock().setType(Material.IRON_BARS);
                }
                
                // Сенсоры на столбах
                base.clone().add(i, 4, -size - 3).getBlock().setType(Material.OBSERVER);
                base.clone().add(i, 4, size + 3).getBlock().setType(Material.OBSERVER);
                base.clone().add(-size - 3, 4, i).getBlock().setType(Material.OBSERVER);
                base.clone().add(size + 3, 4, i).getBlock().setType(Material.OBSERVER);
            }
        }
        
        // Защитные турели по углам периметра
        int[] corners = {-size - 3, size + 3};
        for (int x : corners) {
            for (int z : corners) {
                // Основание турели
                base.clone().add(x, 0, z).getBlock().setType(Material.OBSIDIAN);
                
                // Столб турели
                for (int y = 1; y <= 4; y++) {
                    base.clone().add(x, y, z).getBlock().setType(Material.IRON_BLOCK);
                }
                
                // Турель
                base.clone().add(x, 5, z).getBlock().setType(Material.DISPENSER);
                
                // Сенсор турели
                base.clone().add(x, 6, z).getBlock().setType(Material.OBSERVER);
            }
        }
        
        // Центральная система безопасности в подвале
        base.clone().add(0, -2, 0).getBlock().setType(Material.COMMAND_BLOCK);
        
        // Сенсорная сеть
        for (int x = -2; x <= 2; x += 2) {
            for (int z = -2; z <= 2; z += 2) {
                if (x != 0 || z != 0) {
                    base.clone().add(x, -2, z).getBlock().setType(Material.OBSERVER);
                }
            }
        }
    }
    
    public boolean startResearch(String researchType, Map<Material, Integer> resources) {
        if (state != LaboratoryState.OPERATIONAL) {
            return false;
        }
        
        if (activeResearch.size() >= level.getMaxResearch()) {
            if (autoResearch) {
                researchQueue.offer(researchType);
                return true;
            }
            return false;
        }
        
        if (!specialization.canResearch(researchType)) {
            return false;
        }
        
        // Проверяем наличие ресурсов в хранилище
        for (Map.Entry<Material, Integer> entry : resources.entrySet()) {
            if (!storage.hasMaterial(entry.getKey(), entry.getValue())) {
                return false;
            }
        }
        
        // Забираем ресурсы из хранилища
        for (Map.Entry<Material, Integer> entry : resources.entrySet()) {
            storage.removeMaterial(entry.getKey(), entry.getValue());
        }
        
        double speedMultiplier = specialization.getResearchSpeedMultiplier(researchType);
        EnhancedResearchProject project = new EnhancedResearchProject(researchType, resources, speedMultiplier);
        activeResearch.put(researchType, project);
        
        // Эффекты начала исследования
        playResearchStartEffect();
        
        return true;
    }
    
    public void updateResearch() {
        Iterator<Map.Entry<String, EnhancedResearchProject>> iterator = activeResearch.entrySet().iterator();
        
        while (iterator.hasNext()) {
            Map.Entry<String, EnhancedResearchProject> entry = iterator.next();
            EnhancedResearchProject project = entry.getValue();
            
            // Потребляем энергию для исследований
            if (currentEnergy >= 10) {
                currentEnergy -= 10;
                energyConsumption += 10;
                
                if (project.update()) {
                    completedResearch.add(entry.getKey());
                    iterator.remove();
                    
                    // Уведомляем игрока
                    Player player = plugin.getServer().getPlayer(owner);
                    if (player != null) {
                        player.sendMessage(ChatColor.GREEN + "🔬 Исследование " + entry.getKey() + " завершено!");
                        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
                        playResearchCompleteEffect();
                    }
                    
                    // Автоматически начинаем следующее исследование
                    if (autoResearch && !researchQueue.isEmpty()) {
                        String nextResearch = researchQueue.poll();
                        Map<Material, Integer> requirements = getResearchRequirements(nextResearch);
                        if (requirements != null) {
                            startResearch(nextResearch, requirements);
                        }
                    }
                }
            }
        }
        
        // Генерируем энергию
        if (currentEnergy < level.getMaxEnergy()) {
            currentEnergy = Math.min(level.getMaxEnergy(), currentEnergy + energyGeneration);
        }
    }
    
    public boolean upgradeLevel() {
        if (level.getLevel() >= 5 || state != LaboratoryState.OPERATIONAL) {
            return false;
        }
        
        LaboratoryLevel nextLevel = LaboratoryLevel.getByLevel(level.getLevel() + 1);
        Map<Material, Integer> upgradeMaterials = nextLevel.getUpgradeMaterials();
        
        // Проверяем наличие материалов
        for (Map.Entry<Material, Integer> entry : upgradeMaterials.entrySet()) {
            if (!storage.hasMaterial(entry.getKey(), entry.getValue())) {
                return false;
            }
        }
        
        // Забираем материалы
        for (Map.Entry<Material, Integer> entry : upgradeMaterials.entrySet()) {
            storage.removeMaterial(entry.getKey(), entry.getValue());
        }
        
        state = LaboratoryState.UPGRADING;
        
        // Запускаем процесс апгрейда
        new BukkitRunnable() {
            int progress = 0;
            
            @Override
            public void run() {
                progress += 10;
                
                if (progress >= 100) {
                    level = nextLevel;
                    state = LaboratoryState.OPERATIONAL;
                    buildLaboratoryStructure();
                    
                    Player player = plugin.getServer().getPlayer(owner);
                    if (player != null) {
                        player.sendMessage(ChatColor.GOLD + "🏗️ Лаборатория улучшена до уровня " + level.getLevel() + "!");
                        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
                    }
                    
                    this.cancel();
                } else {
                    playUpgradeEffect();
                }
            }
        }.runTaskTimer(plugin, 0L, 60L);
        
        return true;
    }
    
    public void setSpecialization(LaboratorySpecialization newSpecialization) {
        if (state == LaboratoryState.OPERATIONAL && activeResearch.isEmpty()) {
            this.specialization = newSpecialization;
            buildLaboratoryStructure();
            
            Player player = plugin.getServer().getPlayer(owner);
            if (player != null) {
                player.sendMessage(ChatColor.AQUA + "🔬 Специализация изменена на: " + newSpecialization.getName());
            }
        }
    }
    
    private void startAutomationTask() {
        automationTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (state == LaboratoryState.OPERATIONAL) {
                    updateResearch();
                    
                    // Автоматическое улучшение
                    if (autoUpgrade && level.getLevel() < 5) {
                        upgradeLevel();
                    }
                    
                    // Показываем эффекты работы
                    if (currentEnergy > level.getMaxEnergy() * 0.8) {
                        playOperationEffect();
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }
    
    // ==================== VISUAL EFFECTS ====================
    
    private void playLaboratoryActivationEffect() {
        if (location == null || location.getWorld() == null) return;
        
        World world = location.getWorld();
        
        // Основной эффект активации
        world.spawnParticle(Particle.ENCHANTMENT_TABLE, location.clone().add(0, 3, 0), 100, 3, 3, 3, 0.2);
        world.spawnParticle(Particle.END_ROD, location.clone().add(0, 2, 0), 60, 2, 2, 2, 0.1);
        world.spawnParticle(Particle.ELECTRIC_SPARK, location.clone().add(0, 2.5, 0), 40, 2.5, 2.5, 2.5, 0.15);
        world.spawnParticle(Particle.FIREWORKS_SPARK, location.clone().add(0, 4, 0), 80, 3, 3, 3, 0.3);
        
        // Звуковые эффекты
        world.playSound(location, Sound.BLOCK_BEACON_ACTIVATE, 1.5f, 1.2f);
        world.playSound(location, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1.2f, 1.5f);
        world.playSound(location, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 0.5f, 2.0f);
        
        // Последовательные эффекты
        new BukkitRunnable() {
            int ticks = 0;
            
            @Override
            public void run() {
                if (ticks >= 120) { // 6 секунд
                    cancel();
                    return;
                }
                
                // Вращающиеся частицы на разных уровнях
                double angle = ticks * 0.2;
                for (int level = 1; level <= 4; level++) {
                    for (int i = 0; i < 6; i++) {
                        double levelAngle = angle + i * Math.PI / 3 + level * 0.5;
                        double radius = 2 + level * 0.5;
                        double x = Math.cos(levelAngle) * radius;
                        double z = Math.sin(levelAngle) * radius;
                        double y = level + Math.sin(ticks * 0.1 + level) * 0.5;
                        
                        Location particleLoc = location.clone().add(x, y, z);
                        
                        Color[] colors = {Color.AQUA, Color.LIME, Color.YELLOW, Color.WHITE, Color.PURPLE};
                        Color color = colors[level % colors.length];
                        
                        world.spawnParticle(Particle.REDSTONE, particleLoc, 1, 0, 0, 0, 0, 
                            new Particle.DustOptions(color, 1.2f));
                    }
                }
                
                // Пульсирующий эффект каждые 30 тиков
                if (ticks % 30 == 0) {
                    world.spawnParticle(Particle.FLASH, location.clone().add(0, 2, 0), 3);
                    world.spawnParticle(Particle.TOTEM, location.clone().add(0, 3, 0), 20, 2, 2, 2, 0.1);
                    world.playSound(location, Sound.BLOCK_BEACON_POWER_SELECT, 0.8f, 1.8f);
                }
                
                // Энергетические разряды
                if (ticks % 10 == 0) {
                    for (int i = 0; i < 4; i++) {
                        double x = (Math.random() - 0.5) * 6;
                        double z = (Math.random() - 0.5) * 6;
                        double y = Math.random() * 4 + 1;
                        
                        world.spawnParticle(Particle.ELECTRIC_SPARK, 
                            location.clone().add(x, y, z), 3, 0.2, 0.2, 0.2, 0.05);
                    }
                }
                
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
    
    private void playResearchStartEffect() {
        if (terminalLocation == null || terminalLocation.getWorld() == null) return;
        
        World world = terminalLocation.getWorld();
        
        // Начальный взрыв энергии
        world.spawnParticle(Particle.EXPLOSION_LARGE, terminalLocation.clone().add(0, 2, 0), 2);
        world.spawnParticle(Particle.ENCHANTMENT_TABLE, terminalLocation.clone().add(0, 3, 0), 80, 2.5, 2.5, 2.5, 0.3);
        world.spawnParticle(Particle.END_ROD, terminalLocation.clone().add(0, 2, 0), 50, 1.5, 1.5, 1.5, 0.15);
        world.spawnParticle(Particle.ELECTRIC_SPARK, terminalLocation.clone().add(0, 2.5, 0), 30, 1.5, 1.5, 1.5, 0.1);
        
        // Звуки
        world.playSound(terminalLocation, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 0.4f, 2.0f);
        world.playSound(terminalLocation, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1.2f, 1.3f);
        world.playSound(terminalLocation, Sound.BLOCK_BEACON_ACTIVATE, 0.8f, 1.8f);
        
        // Продолжительный эффект
        new BukkitRunnable() {
            int ticks = 0;
            
            @Override
            public void run() {
                if (ticks >= 150) { // 7.5 секунд
                    cancel();
                    return;
                }
                
                // Двойные спиральные частицы
                double height = ticks * 0.04;
                double angle1 = ticks * 0.3;
                double angle2 = ticks * -0.25;
                
                for (int spiral = 0; spiral < 2; spiral++) {
                    double currentAngle = spiral == 0 ? angle1 : angle2;
                    
                    for (int i = 0; i < 4; i++) {
                        double spiralAngle = currentAngle + i * (Math.PI / 2);
                        double radius = 2.0 - (height * 0.3);
                        
                        double x = Math.cos(spiralAngle) * radius;
                        double z = Math.sin(spiralAngle) * radius;
                        
                        Location spiralLoc = terminalLocation.clone().add(x, 2 + height, z);
                        
                        Color color = spiral == 0 ? Color.LIME : Color.AQUA;
                        world.spawnParticle(Particle.REDSTONE, spiralLoc, 1, 0, 0, 0, 0,
                            new Particle.DustOptions(color, 1.0f));
                    }
                }
                
                // Центральный столб энергии
                if (ticks % 3 == 0) {
                    world.spawnParticle(Particle.END_ROD, 
                        terminalLocation.clone().add(0, 2 + height, 0), 5, 0.15, 0.15, 0.15, 0.03);
                }
                
                // Голографические эффекты
                if (ticks % 20 == 0) {
                    for (int i = 0; i < 8; i++) {
                        double holoAngle = i * Math.PI / 4;
                        double x = Math.cos(holoAngle) * 3;
                        double z = Math.sin(holoAngle) * 3;
                        
                        world.spawnParticle(Particle.END_ROD, 
                            terminalLocation.clone().add(x, 3, z), 3, 0.1, 0.1, 0.1, 0.02);
                    }
                }
                
                ticks++;
            }
        }.runTaskTimer(plugin, 20L, 1L);
    }
    
    private void playResearchCompleteEffect() {
        if (terminalLocation == null || terminalLocation.getWorld() == null) return;
        
        World world = terminalLocation.getWorld();
        
        // Финальный взрыв успеха
        world.spawnParticle(Particle.FIREWORKS_SPARK, terminalLocation.clone().add(0, 3, 0), 200, 3, 3, 3, 0.4);
        world.spawnParticle(Particle.TOTEM, terminalLocation.clone().add(0, 2, 0), 100, 2.5, 2.5, 2.5, 0.3);
        world.spawnParticle(Particle.ENCHANTMENT_TABLE, terminalLocation.clone().add(0, 2.5, 0), 120, 3, 3, 3, 0.2);
        world.spawnParticle(Particle.END_ROD, terminalLocation.clone().add(0, 4, 0), 80, 2, 2, 2, 0.2);
        
        // Звуки успеха
        world.playSound(terminalLocation, Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.5f, 1.2f);
        world.playSound(terminalLocation, Sound.ENTITY_PLAYER_LEVELUP, 1.2f, 1.5f);
        world.playSound(terminalLocation, Sound.BLOCK_BEACON_POWER_SELECT, 1.2f, 2.0f);
        world.playSound(terminalLocation, Sound.ENTITY_FIREWORK_ROCKET_BLAST, 0.8f, 1.3f);
        
        // Праздничный эффект
        new BukkitRunnable() {
            int ticks = 0;
            
            @Override
            public void run() {
                if (ticks >= 120) { // 6 секунд
                    cancel();
                    return;
                }
                
                // Фейерверк частицы
                for (int i = 0; i < 8; i++) {
                    double angle = Math.random() * Math.PI * 2;
                    double radius = Math.random() * 4 + 1;
                    double height = Math.random() * 6 + 2;
                    
                    double x = Math.cos(angle) * radius;
                    double z = Math.sin(angle) * radius;
                    
                    Location fireworkLoc = terminalLocation.clone().add(x, height, z);
                    
                    Color[] colors = {Color.LIME, Color.AQUA, Color.YELLOW, Color.WHITE, Color.ORANGE, Color.PURPLE, Color.PINK};
                    Color randomColor = colors[(int) (Math.random() * colors.length)];
                    
                    world.spawnParticle(Particle.REDSTONE, fireworkLoc, 1, 0, 0, 0, 0,
                        new Particle.DustOptions(randomColor, 1.5f));
                }
                
                // Звуковые акценты
                if (ticks % 25 == 0) {
                    world.playSound(terminalLocation, Sound.ENTITY_FIREWORK_ROCKET_BLAST, 0.6f, 1.0f + (float) Math.random());
                    world.playSound(terminalLocation, Sound.ENTITY_FIREWORK_ROCKET_TWINKLE, 0.4f, 1.5f);
                }
                
                // Кольцевые волны успеха
                if (ticks % 15 == 0) {
                    double radius = (ticks % 60) * 0.1;
                    for (int i = 0; i < 16; i++) {
                        double waveAngle = i * Math.PI / 8;
                        double x = Math.cos(waveAngle) * radius;
                        double z = Math.sin(waveAngle) * radius;
                        
                        world.spawnParticle(Particle.ENCHANTMENT_TABLE, 
                            terminalLocation.clone().add(x, 2, z), 2, 0.1, 0.1, 0.1, 0.05);
                    }
                }
                
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 2L);
    }
    
    private void playUpgradeEffect() {
        if (location == null || location.getWorld() == null) return;
        
        World world = location.getWorld();
        
        // Эффекты улучшения
        world.spawnParticle(Particle.TOTEM, location.clone().add(0, 4, 0), 25, 3, 3, 3, 0.15);
        world.spawnParticle(Particle.CRIT_MAGIC, location.clone().add(0, 3, 0), 20, 2, 2, 2, 0.1);
        world.spawnParticle(Particle.ENCHANTMENT_TABLE, location.clone().add(0, 2, 0), 30, 2.5, 2.5, 2.5, 0.2);
        
        // Звуки улучшения
        world.playSound(location, Sound.BLOCK_BEACON_POWER_SELECT, 0.8f, 1.5f);
        world.playSound(location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.2f);
    }
    
    private void playOperationEffect() {
        if (location == null || location.getWorld() == null) return;
        
        World world = location.getWorld();
        
        // Эффекты на терминале
        if (terminalLocation != null) {
            world.spawnParticle(Particle.END_ROD, 
                terminalLocation.clone().add(0, 3, 0), 5, 0.3, 0.3, 0.3, 0.03);
            world.spawnParticle(Particle.ENCHANTMENT_TABLE, 
                terminalLocation.clone().add(0, 2.5, 0), 8, 0.5, 0.5, 0.5, 0.05);
        }
        
        // Эффекты на энергогенераторах
        int size = 6 + level.getLevel();
        Location base = location.clone();
        
        // Главные генераторы на крыше
        world.spawnParticle(Particle.ELECTRIC_SPARK, 
            base.clone().add(-2, 7, -2), 3, 0.2, 0.2, 0.2, 0.02);
        world.spawnParticle(Particle.ELECTRIC_SPARK, 
            base.clone().add(2, 7, -2), 3, 0.2, 0.2, 0.2, 0.02);
        world.spawnParticle(Particle.ELECTRIC_SPARK, 
            base.clone().add(-2, 7, 2), 3, 0.2, 0.2, 0.2, 0.02);
        world.spawnParticle(Particle.ELECTRIC_SPARK, 
            base.clone().add(2, 7, 2), 3, 0.2, 0.2, 0.2, 0.02);
        
        // Внешние генераторы
        int[] corners = {-size - 2, size + 2};
        for (int x : corners) {
            for (int z : corners) {
                world.spawnParticle(Particle.REDSTONE, 
                    base.clone().add(x, 7, z), 2, 0.1, 0.1, 0.1, 0,
                    new Particle.DustOptions(Color.AQUA, 0.8f));
            }
        }
        
        // Антенна на башне
        world.spawnParticle(Particle.END_ROD, 
            base.clone().add(0, 19, 0), 2, 0.1, 0.1, 0.1, 0.02);
    }
    
    private Map<Material, Integer> getResearchRequirements(String researchType) {
        Map<Material, Integer> requirements = new HashMap<>();
        requirements.put(Material.IRON_INGOT, 16);
        requirements.put(Material.REDSTONE, 32);
        requirements.put(Material.DIAMOND, 8);
        return requirements;
    }
    
    public void shutdown() {
        if (automationTask != null) {
            automationTask.cancel();
        }
    }
    
    // Геттеры
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
    public LaboratoryStorage getStorage() { return storage; }
    public Map<String, EnhancedResearchProject> getActiveResearch() { return new HashMap<>(activeResearch); }
    public Set<String> getCompletedResearch() { return new HashSet<>(completedResearch); }
    public Queue<String> getResearchQueue() { return new LinkedList<>(researchQueue); }
    public boolean isAutoResearch() { return autoResearch; }
    public boolean isAutoUpgrade() { return autoUpgrade; }
    public Map<Material, Integer> getRequiredMaterials() { return new HashMap<>(requiredMaterials); }
    public Location getTerminalLocation() { return terminalLocation; }
    
    // Сеттеры
    public void setAutoResearch(boolean autoResearch) { this.autoResearch = autoResearch; }
    public void setAutoUpgrade(boolean autoUpgrade) { this.autoUpgrade = autoUpgrade; }
    
    public boolean isResearchCompleted(String researchType) {
        return completedResearch.contains(researchType);
    }
    
    public double getEnergyPercentage() {
        return (double) currentEnergy / level.getMaxEnergy() * 100;
    }
    
    public int getStorageUsage() {
        return storage.getTotalItems();
    }
    
    public int getMaxStorageCapacity() {
        return level.getMaxEnergy() / 10;
    }
}
