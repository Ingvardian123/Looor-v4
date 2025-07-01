package com.uraniumcraft.effects;

import com.uraniumcraft.UraniumPlugin;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class HolographicDisplay {
    
    private static final Map<UUID, Set<Hologram>> activeHolograms = new ConcurrentHashMap<>();
    private static UraniumPlugin plugin;
    
    public static void initialize(UraniumPlugin pluginInstance) {
        plugin = pluginInstance;
    }
    
    // Класс голограммы
    public static class Hologram {
        private final UUID id;
        private final Location location;
        private final HologramType type;
        private final Map<String, Object> data;
        private boolean active;
        private int duration;
        
        public Hologram(Location location, HologramType type, int duration) {
            this.id = UUID.randomUUID();
            this.location = location.clone();
            this.type = type;
            this.duration = duration;
            this.data = new HashMap<>();
            this.active = true;
        }
        
        public Hologram setData(String key, Object value) {
            data.put(key, value);
            return this;
        }
        
        public Object getData(String key) {
            return data.get(key);
        }
        
        // Геттеры и сеттеры
        public UUID getId() { return id; }
        public Location getLocation() { return location; }
        public HologramType getType() { return type; }
        public boolean isActive() { return active; }
        public int getDuration() { return duration; }
        
        public void setActive(boolean active) { this.active = active; }
        public void setDuration(int duration) { this.duration = duration; }
    }
    
    // Типы голограмм
    public enum HologramType {
        RADIATION_SCANNER,
        ENERGY_MONITOR,
        QUANTUM_INTERFACE,
        LABORATORY_DATA,
        WEAPON_TARGETING,
        ARMOR_STATUS,
        TELEPORTER_COORDINATES,
        RESEARCH_PROGRESS,
        MOLECULAR_STRUCTURE,
        PARTICLE_ACCELERATOR_DATA
    }
    
    // Создание сканера радиации
    public static Hologram createRadiationScanner(Location center, Player player, int duration) {
        Hologram hologram = new Hologram(center, HologramType.RADIATION_SCANNER, duration)
            .setData("player", player)
            .setData("scanRadius", 10.0)
            .setData("scanAngle", 0.0);
        
        startRadiationScannerDisplay(hologram);
        return hologram;
    }
    
    private static void startRadiationScannerDisplay(Hologram hologram) {
        new BukkitRunnable() {
            int ticks = 0;
            double scanAngle = 0;
            
            @Override
            public void run() {
                if (!hologram.isActive() || ticks > hologram.getDuration()) {
                    hologram.setActive(false);
                    cancel();
                    return;
                }
                
                Location center = hologram.getLocation();
                Player player = (Player) hologram.getData("player");
                double scanRadius = (Double) hologram.getData("scanRadius");
                
                // Основная голографическая панель
                displayHolographicPanel(center.clone().add(0, 2, 0), "RADIATION SCANNER", Color.fromRGB(0, 255, 0));
                
                // Сканирующий луч
                for (double r = 0; r <= scanRadius; r += 0.5) {
                    double x = center.getX() + Math.cos(scanAngle) * r;
                    double z = center.getZ() + Math.sin(scanAngle) * r;
                    double y = center.getY() + 1;
                    
                    Location scanLoc = new Location(center.getWorld(), x, y, z);
                    
                    // Луч сканера
                    Color scanColor = Color.fromRGB(0, 255, 255);
                    Particle.DustOptions scanOptions = new Particle.DustOptions(scanColor, 0.8f);
                    center.getWorld().spawnParticle(Particle.REDSTONE, scanLoc, 1, 0, 0, 0, 0, scanOptions);
                    
                    // Проверка радиации в точке
                    int radiation = plugin.getRadiationManager().getRadiationAtLocation(scanLoc);
                    if (radiation > 0) {
                        Color radiationColor = getRadiationColor(radiation);
                        Particle.DustOptions radiationOptions = new Particle.DustOptions(radiationColor, 1.2f);
                        center.getWorld().spawnParticle(Particle.REDSTONE, scanLoc, 2, 0.1, 0.1, 0.1, 0, radiationOptions);
                        
                        // Голографическое значение
                        displayFloatingNumber(scanLoc.clone().add(0, 0.5, 0), radiation + "%", radiationColor);
                    }
                }
                
                // Радар-дисплей
                displayRadarScreen(center.clone().add(1.5, 1.5, 0), scanAngle, scanRadius);
                
                // Данные игрока
                int playerRadiation = plugin.getRadiationManager().getRadiation(player.getUniqueId());
                displayPlayerRadiationData(center.clone().add(-1.5, 1.5, 0), player, playerRadiation);
                
                scanAngle += Math.PI / 16; // Поворот на 11.25 градусов
                if (scanAngle >= 2 * Math.PI) scanAngle = 0;
                
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 2L);
    }
    
    // Создание монитора энергии
    public static Hologram createEnergyMonitor(Location center, String systemName, int duration) {
        Hologram hologram = new Hologram(center, HologramType.ENERGY_MONITOR, duration)
            .setData("systemName", systemName)
            .setData("energyLevel", 100.0)
            .setData("powerConsumption", 0.0)
            .setData("efficiency", 95.0);
        
        startEnergyMonitorDisplay(hologram);
        return hologram;
    }
    
    private static void startEnergyMonitorDisplay(Hologram hologram) {
        new BukkitRunnable() {
            int ticks = 0;
            
            @Override
            public void run() {
                if (!hologram.isActive() || ticks > hologram.getDuration()) {
                    hologram.setActive(false);
                    cancel();
                    return;
                }
                
                Location center = hologram.getLocation();
                String systemName = (String) hologram.getData("systemName");
                double energyLevel = (Double) hologram.getData("energyLevel");
                double powerConsumption = (Double) hologram.getData("powerConsumption");
                double efficiency = (Double) hologram.getData("efficiency");
                
                // Заголовок системы
                displayHolographicPanel(center.clone().add(0, 2.5, 0), systemName.toUpperCase(), Color.fromRGB(0, 150, 255));
                
                // Энергетические батареи
                displayEnergyBars(center.clone().add(0, 1.5, 0), energyLevel);
                
                // График потребления энергии
                displayEnergyGraph(center.clone().add(1.5, 1, 0), powerConsumption, ticks);
                
                // Показатели эффективности
                displayEfficiencyIndicator(center.clone().add(-1.5, 1, 0), efficiency);
                
                // Предупреждения
                if (energyLevel < 20) {
                    displayWarning(center.clone().add(0, 0.5, 0), "LOW ENERGY", Color.fromRGB(255, 0, 0));
                }
                if (efficiency < 70) {
                    displayWarning(center.clone().add(0, 0, 0), "LOW EFFICIENCY", Color.fromRGB(255, 128, 0));
                }
                
                // Симуляция изменения данных
                energyLevel = Math.max(0, energyLevel - (Math.random() * 2 - 1));
                powerConsumption = Math.max(0, powerConsumption + (Math.random() * 10 - 5));
                efficiency = Math.max(0, Math.min(100, efficiency + (Math.random() * 4 - 2)));
                
                hologram.setData("energyLevel", energyLevel);
                hologram.setData("powerConsumption", powerConsumption);
                hologram.setData("efficiency", efficiency);
                
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 5L);
    }
    
    // Создание квантового интерфейса
    public static Hologram createQuantumInterface(Location center, Player player, int duration) {
        Hologram hologram = new Hologram(center, HologramType.QUANTUM_INTERFACE, duration)
            .setData("player", player)
            .setData("quantumState", "SUPERPOSITION")
            .setData("entanglement", 0.0)
            .setData("coherence", 100.0);
        
        startQuantumInterfaceDisplay(hologram);
        return hologram;
    }
    
    private static void startQuantumInterfaceDisplay(Hologram hologram) {
        new BukkitRunnable() {
            int ticks = 0;
            double phase = 0;
            
            @Override
            public void run() {
                if (!hologram.isActive() || ticks > hologram.getDuration()) {
                    hologram.setActive(false);
                    cancel();
                    return;
                }
                
                Location center = hologram.getLocation();
                Player player = (Player) hologram.getData("player");
                String quantumState = (String) hologram.getData("quantumState");
                double entanglement = (Double) hologram.getData("entanglement");
                double coherence = (Double) hologram.getData("coherence");
                
                // Квантовые орбитали
                displayQuantumOrbitals(center, phase);
                
                // Волновая функция
                displayWaveFunction(center.clone().add(0, 2, 0), phase, coherence);
                
                // Состояние запутанности
                displayEntanglementState(center.clone().add(2, 1, 0), entanglement);
                
                // Квантовые флуктуации
                displayQuantumFluctuations(center, phase);
                
                // Измерительные приборы
                displayQuantumMeasurements(center.clone().add(-2, 1, 0), quantumState, coherence);
                
                // Обновление квантовых параметров
                entanglement = Math.max(0, Math.min(100, entanglement + (Math.random() * 10 - 5)));
                coherence = Math.max(0, Math.min(100, coherence - Math.random() * 0.5));
                
                hologram.setData("entanglement", entanglement);
                hologram.setData("coherence", coherence);
                
                phase += 0.1;
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 2L);
    }
    
    // Создание лабораторных данных
    public static Hologram createLaboratoryData(Location center, String experimentName, int duration) {
        Hologram hologram = new Hologram(center, HologramType.LABORATORY_DATA, duration)
            .setData("experimentName", experimentName)
            .setData("progress", 0.0)
            .setData("temperature", 293.15)
            .setData("pressure", 101325.0)
            .setData("samples", new ArrayList<String>());
        
        startLaboratoryDataDisplay(hologram);
        return hologram;
    }
    
    @SuppressWarnings("unchecked")
    private static void startLaboratoryDataDisplay(Hologram hologram) {
        new BukkitRunnable() {
            int ticks = 0;
            
            @Override
            public void run() {
                if (!hologram.isActive() || ticks > hologram.getDuration()) {
                    hologram.setActive(false);
                    cancel();
                    return;
                }
                
                Location center = hologram.getLocation();
                String experimentName = (String) hologram.getData("experimentName");
                double progress = (Double) hologram.getData("progress");
                double temperature = (Double) hologram.getData("temperature");
                double pressure = (Double) hologram.getData("pressure");
                List<String> samples = (List<String>) hologram.getData("samples");
                
                // Название эксперимента
                displayHolographicPanel(center.clone().add(0, 3, 0), experimentName, Color.fromRGB(0, 255, 150));
                
                // Прогресс-бар
                displayProgressBar(center.clone().add(0, 2.5, 0), progress, "PROGRESS");
                
                // Температурный график
                displayTemperatureGraph(center.clone().add(1.5, 2, 0), temperature, ticks);
                
                // Давление
                displayPressureGauge(center.clone().add(-1.5, 2, 0), pressure);
                
                // Молекулярная структура
                displayMolecularStructure(center.clone().add(0, 1, 0), experimentName);
                
                // Список образцов
                displaySampleList(center.clone().add(2, 1, 0), samples);
                
                // Обновление данных
                progress = Math.min(100, progress + Math.random() * 2);
                temperature += (Math.random() - 0.5) * 5;
                pressure += (Math.random() - 0.5) * 1000;
                
                // Добавление новых образцов
                if (ticks % 50 == 0 && samples.size() < 10) {
                    samples.add("Sample-" + (samples.size() + 1));
                }
                
                hologram.setData("progress", progress);
                hologram.setData("temperature", temperature);
                hologram.setData("pressure", pressure);
                hologram.setData("samples", samples);
                
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 3L);
    }
    
    // Создание системы наведения оружия
    public static Hologram createWeaponTargeting(Location center, Player player, Location target, int duration) {
        Hologram hologram = new Hologram(center, HologramType.WEAPON_TARGETING, duration)
            .setData("player", player)
            .setData("target", target)
            .setData("distance", center.distance(target))
            .setData("accuracy", 95.0)
            .setData("windSpeed", Math.random() * 10);
        
        startWeaponTargetingDisplay(hologram);
        return hologram;
    }
    
    private static void startWeaponTargetingDisplay(Hologram hologram) {
        new BukkitRunnable() {
            int ticks = 0;
            
            @Override
            public void run() {
                if (!hologram.isActive() || ticks > hologram.getDuration()) {
                    hologram.setActive(false);
                    cancel();
                    return;
                }
                
                Location center = hologram.getLocation();
                Player player = (Player) hologram.getData("player");
                Location target = (Location) hologram.getData("target");
                double distance = (Double) hologram.getData("distance");
                double accuracy = (Double) hologram.getData("accuracy");
                double windSpeed = (Double) hologram.getData("windSpeed");
                
                // Прицельная сетка
                displayTargetingReticle(player.getEyeLocation().add(player.getLocation().getDirection().multiply(2)), target);
                
                // Баллистические данные
                displayBallisticData(center.clone().add(1, 1, 0), distance, windSpeed, accuracy);
                
                // Траектория снаряда
                displayProjectileTrajectory(center, target, windSpeed);
                
                // Информация о цели
                displayTargetInfo(center.clone().add(-1, 1, 0), target, distance);
                
                // Обновление данных
                distance = center.distance(target);
                accuracy = Math.max(50, Math.min(100, accuracy + (Math.random() * 4 - 2)));
                windSpeed += (Math.random() - 0.5) * 2;
                
                hologram.setData("distance", distance);
                hologram.setData("accuracy", accuracy);
                hologram.setData("windSpeed", windSpeed);
                
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 2L);
    }
    
    // Вспомогательные методы отображения
    private static void displayHolographicPanel(Location location, String text, Color color) {
        // Фон панели
        for (int x = -2; x <= 2; x++) {
            for (int y = -1; y <= 1; y++) {
                Location panelLoc = location.clone().add(x * 0.2, y * 0.2, 0);
                Particle.DustOptions panelOptions = new Particle.DustOptions(color, 0.5f);
                location.getWorld().spawnParticle(Particle.REDSTONE, panelLoc, 1, 0, 0, 0, 0, panelOptions);
            }
        }
        
        // Рамка панели
        Color frameColor = Color.fromRGB(255, 255, 255);
        Particle.DustOptions frameOptions = new Particle.DustOptions(frameColor, 0.8f);
        
        for (int i = -2; i <= 2; i++) {
            location.getWorld().spawnParticle(Particle.REDSTONE, location.clone().add(i * 0.2, 0.2, 0), 1, 0, 0, 0, 0, frameOptions);
            location.getWorld().spawnParticle(Particle.REDSTONE, location.clone().add(i * 0.2, -0.2, 0), 1, 0, 0, 0, 0, frameOptions);
        }
        for (int i = -1; i <= 1; i++) {
            location.getWorld().spawnParticle(Particle.REDSTONE, location.clone().add(-0.4, i * 0.2, 0), 1, 0, 0, 0, 0, frameOptions);
            location.getWorld().spawnParticle(Particle.REDSTONE, location.clone().add(0.4, i * 0.2, 0), 1, 0, 0, 0, 0, frameOptions);
        }
        
        // Текстовые точки (имитация текста)
        displayHolographicText(location, text, color);
    }
    
    private static void displayHolographicText(Location location, String text, Color color) {
        // Простая имитация текста через частицы
        Particle.DustOptions textOptions = new Particle.DustOptions(color, 0.6f);
        
        int textLength = Math.min(text.length(), 10);
        for (int i = 0; i < textLength; i++) {
            double x = (i - textLength / 2.0) * 0.1;
            Location charLoc = location.clone().add(x, 0, 0);
            location.getWorld().spawnParticle(Particle.REDSTONE, charLoc, 1, 0.02, 0.02, 0.02, 0, textOptions);
            location.getWorld().spawnParticle(Particle.END_ROD, charLoc, 1, 0.01, 0.01, 0.01, 0.01);
        }
    }
    
    private static void displayFloatingNumber(Location location, String number, Color color) {
        Particle.DustOptions numberOptions = new Particle.DustOptions(color, 1.0f);
        
        for (int i = 0; i < number.length(); i++) {
            double x = (i - number.length() / 2.0) * 0.05;
            Location digitLoc = location.clone().add(x, 0, 0);
            location.getWorld().spawnParticle(Particle.REDSTONE, digitLoc, 1, 0.01, 0.01, 0.01, 0, numberOptions);
        }
    }
    
    private static void displayRadarScreen(Location center, double scanAngle, double maxRadius) {
        // Экран радара
        Color screenColor = Color.fromRGB(0, 100, 0);
        Particle.DustOptions screenOptions = new Particle.DustOptions(screenColor, 0.8f);
        
        // Концентрические круги
        for (int ring = 1; ring <= 3; ring++) {
            double radius = (maxRadius / 3) * ring * 0.1;
            for (int i = 0; i < 32; i++) {
                double angle = i * Math.PI / 16;
                double x = center.getX() + Math.cos(angle) * radius;
                double z = center.getZ() + Math.sin(angle) * radius;
                Location ringLoc = new Location(center.getWorld(), x, center.getY(), z);
                center.getWorld().spawnParticle(Particle.REDSTONE, ringLoc, 1, 0, 0, 0, 0, screenOptions);
            }
        }
        
        // Сканирующая линия
        Color scanLineColor = Color.fromRGB(0, 255, 0);
        Particle.DustOptions scanLineOptions = new Particle.DustOptions(scanLineColor, 1.0f);
        
        for (double r = 0; r <= maxRadius * 0.1; r += 0.05) {
            double x = center.getX() + Math.cos(scanAngle) * r;
            double z = center.getZ() + Math.sin(scanAngle) * r;
            Location scanLoc = new Location(center.getWorld(), x, center.getY(), z);
            center.getWorld().spawnParticle(Particle.REDSTONE, scanLoc, 1, 0, 0, 0, 0, scanLineOptions);
        }
    }
    
    private static void displayPlayerRadiationData(Location center, Player player, int radiation) {
        // Силуэт игрока
        Color playerColor = getRadiationColor(radiation);
        Particle.DustOptions playerOptions = new Particle.DustOptions(playerColor, 1.2f);
        
        // Простой силуэт человека
        Location[] bodyPoints = {
            center.clone().add(0, 0.8, 0), // голова
            center.clone().add(0, 0.4, 0), // тело верх
            center.clone().add(0, 0, 0),   // тело низ
            center.clone().add(-0.2, 0.4, 0), // левая рука
            center.clone().add(0.2, 0.4, 0),  // правая рука
            center.clone().add(-0.1, -0.4, 0), // левая нога
            center.clone().add(0.1, -0.4, 0)   // правая нога
        };
        
        for (Location point : bodyPoints) {
            center.getWorld().spawnParticle(Particle.REDSTONE, point, 1, 0.02, 0.02, 0.02, 0, playerOptions);
        }
        
        // Уровень радиации
        displayFloatingNumber(center.clone().add(0, 1.2, 0), radiation + "%", playerColor);
    }
    
    private static void displayEnergyBars(Location center, double energyLevel) {
        int bars = (int) (energyLevel / 10);
        
        for (int i = 0; i < 10; i++) {
            Color barColor;
            if (i < bars) {
                if (i < 3) barColor = Color.fromRGB(255, 0, 0);      // Красный (низкий)
                else if (i < 7) barColor = Color.fromRGB(255, 255, 0); // Желтый (средний)
                else barColor = Color.fromRGB(0, 255, 0);              // Зеленый (высокий)
            } else {
                barColor = Color.fromRGB(50, 50, 50); // Пустой
            }
            
            Particle.DustOptions barOptions = new Particle.DustOptions(barColor, 1.0f);
            Location barLoc = center.clone().add((i - 5) * 0.1, 0, 0);
            
            for (int j = 0; j < 5; j++) {
                center.getWorld().spawnParticle(Particle.REDSTONE, barLoc.clone().add(0, j * 0.05, 0), 1, 0, 0, 0, 0, barOptions);
            }
        }
    }
    
    private static void displayEnergyGraph(Location center, double powerConsumption, int ticks) {
        // График потребления энергии за последние 20 тиков
        Color graphColor = Color.fromRGB(0, 150, 255);
        Particle.DustOptions graphOptions = new Particle.DustOptions(graphColor, 0.8f);
        
        for (int i = 0; i < 20; i++) {
            double x = (i - 10) * 0.05;
            double y = Math.sin((ticks - i) * 0.1) * 0.3 + powerConsumption * 0.01;
            Location graphLoc = center.clone().add(x, y, 0);
            center.getWorld().spawnParticle(Particle.REDSTONE, graphLoc, 1, 0, 0, 0, 0, graphOptions);
        }
    }
    
    private static void displayEfficiencyIndicator(Location center, double efficiency) {
        // Круговой индикатор эффективности
        Color efficiencyColor;
        if (efficiency > 80) efficiencyColor = Color.fromRGB(0, 255, 0);
        else if (efficiency > 60) efficiencyColor = Color.fromRGB(255, 255, 0);
        else efficiencyColor = Color.fromRGB(255, 0, 0);
        
        Particle.DustOptions efficiencyOptions = new Particle.DustOptions(efficiencyColor, 1.0f);
        
        double maxAngle = (efficiency / 100.0) * 2 * Math.PI;
        for (double angle = 0; angle < maxAngle; angle += Math.PI / 16) {
            double x = center.getX() + Math.cos(angle) * 0.3;
            double z = center.getZ() + Math.sin(angle) * 0.3;
            Location effLoc = new Location(center.getWorld(), x, center.getY(), z);
            center.getWorld().spawnParticle(Particle.REDSTONE, effLoc, 1, 0, 0, 0, 0, efficiencyOptions);
        }
        
        // Процент в центре
        displayFloatingNumber(center, String.format("%.0f%%", efficiency), efficiencyColor);
    }
    
    private static void displayWarning(Location center, String message, Color color) {
        // Мигающее предупреждение
        if (System.currentTimeMillis() % 1000 < 500) {
            displayHolographicPanel(center, message, color);
            
            // Дополнительные эффекты предупреждения
            center.getWorld().spawnParticle(Particle.FLAME, center, 3, 0.2, 0.2, 0.2, 0.02);
        }
    }
    
    private static void displayQuantumOrbitals(Location center, double phase) {
        // s-орбиталь
        Color sColor = Color.fromRGB(255, 0, 0);
        Particle.DustOptions sOptions = new Particle.DustOptions(sColor, 1.0f);
        center.getWorld().spawnParticle(Particle.REDSTONE, center, 3, 0.2, 0.2, 0.2, 0, sOptions);
        
        // p-орбитали
        Color pColor = Color.fromRGB(0, 255, 0);
        Particle.DustOptions pOptions = new Particle.DustOptions(pColor, 0.8f);
        
        for (int i = 0; i < 3; i++) {
            Vector axis = new Vector(i == 0 ? 1 : 0, i == 1 ? 1 : 0, i == 2 ? 1 : 0);
            for (double t = -1; t <= 1; t += 0.2) {
                Location orbitalLoc = center.clone().add(axis.clone().multiply(t * 0.5));
                center.getWorld().spawnParticle(Particle.REDSTONE, orbitalLoc, 1, 0.05, 0.05, 0.05, 0, pOptions);
            }
        }
        
        // d-орбитали (упрощенные)
        Color dColor = Color.fromRGB(0, 0, 255);
        Particle.DustOptions dOptions = new Particle.DustOptions(dColor, 0.6f);
        
        for (int i = 0; i < 16; i++) {
            double angle = i * Math.PI / 8 + phase;
            double x = Math.cos(angle) * Math.cos(angle * 2) * 0.4;
            double z = Math.sin(angle) * Math.cos(angle * 2) * 0.4;
            double y = Math.sin(angle * 2) * 0.2;
            
            Location dLoc = center.clone().add(x, y, z);
            center.getWorld().spawnParticle(Particle.REDSTONE, dLoc, 1, 0.02, 0.02, 0.02, 0, dOptions);
        }
    }
    
    private static void displayWaveFunction(Location center, double phase, double coherence) {
        // Волновая функция как синусоида
        Color waveColor = Color.fromRGB((int)(255 * coherence / 100), 0, (int)(255 * (100 - coherence) / 100));
        Particle.DustOptions waveOptions = new Particle.DustOptions(waveColor, 1.0f);
        
        for (double x = -2; x <= 2; x += 0.1) {
            double y = Math.sin(x * Math.PI + phase) * 0.5 * (coherence / 100);
            Location waveLoc = center.clone().add(x * 0.2, y, 0);
            center.getWorld().spawnParticle(Particle.REDSTONE, waveLoc, 1, 0.01, 0.01, 0.01, 0, waveOptions);
        }
    }
    
    private static void displayEntanglementState(Location center, double entanglement) {
        // Две запутанные частицы
        Color particle1Color = Color.fromRGB(255, 0, 255);
        Color particle2Color = Color.fromRGB(0, 255, 255);
        
        Particle.DustOptions p1Options = new Particle.DustOptions(particle1Color, 1.2f);
        Particle.DustOptions p2Options = new Particle.DustOptions(particle2Color, 1.2f);
        
        double separation = 2.0 - (entanglement / 100.0) * 1.5;
        
        Location p1Loc = center.clone().add(-separation / 2, 0, 0);
        Location p2Loc = center.clone().add(separation / 2, 0, 0);
        
        center.getWorld().spawnParticle(Particle.REDSTONE, p1Loc, 1, 0.05, 0.05, 0.05, 0, p1Options);
        center.getWorld().spawnParticle(Particle.REDSTONE, p2Loc, 1, 0.05, 0.05, 0.05, 0, p2Options);
        
        // Связь между частицами
        if (entanglement > 50) {
            Vector connection = p2Loc.toVector().subtract(p1Loc.toVector());
            for (double t = 0; t <= 1; t += 0.1) {
                Location linkLoc = p1Loc.clone().add(connection.clone().multiply(t));
                center.getWorld().spawnParticle(Particle.END_ROD, linkLoc, 1, 0.01, 0.01, 0.01, 0.01);
            }
        }
    }
    
    private static void displayQuantumFluctuations(Location center, double phase) {
        // Случайные квантовые флуктуации
        for (int i = 0; i < 10; i++) {
            Vector randomOffset = new Vector(
                (Math.random() - 0.5) * 2,
                (Math.random() - 0.5) * 2,
                (Math.random() - 0.5) * 2
            );
            
            Location fluctLoc = center.clone().add(randomOffset);
            
            if (Math.random() < 0.3) {
                Color fluctColor = Color.fromRGB(
                    (int)(Math.random() * 255),
                    (int)(Math.random() * 255),
                    (int)(Math.random() * 255)
                );
                Particle.DustOptions fluctOptions = new Particle.DustOptions(fluctColor, 0.5f);
                center.getWorld().spawnParticle(Particle.REDSTONE, fluctLoc, 1, 0.02, 0.02, 0.02, 0, fluctOptions);
            }
        }
    }
    
    private static void displayQuantumMeasurements(Location center, String state, double coherence) {
        // Измерительные приборы
        displayHolographicPanel(center.clone().add(0, 0.5, 0), "STATE: " + state, Color.fromRGB(255, 255, 0));
        displayHolographicPanel(center.clone().add(0, 0, 0), String.format("COHERENCE: %.1f%%", coherence), 
            coherence > 70 ? Color.fromRGB(0, 255, 0) : Color.fromRGB(255, 0, 0));
    }
    
    private static void displayProgressBar(Location center, double progress, String label) {
        // Прогресс-бар
        int segments = 20;
        int filledSegments = (int) (progress / 100.0 * segments);
        
        for (int i = 0; i < segments; i++) {
            Color segmentColor;
            if (i < filledSegments) {
                segmentColor = Color.fromRGB(0, 255, 0);
            } else {
                segmentColor = Color.fromRGB(100, 100, 100);
            }
            
            Particle.DustOptions segmentOptions = new Particle.DustOptions(segmentColor, 0.8f);
            Location segmentLoc = center.clone().add((i - segments / 2.0) * 0.05, 0, 0);
            center.getWorld().spawnParticle(Particle.REDSTONE, segmentLoc, 1, 0.01, 0.01, 0.01, 0, segmentOptions);
        }
        
        // Процент
        displayFloatingNumber(center.clone().add(0, 0.3, 0), String.format("%.1f%%", progress), Color.fromRGB(255, 255, 255));
    }
    
    private static void displayTemperatureGraph(Location center, double temperature, int ticks) {
        // График температуры
        Color tempColor = getTemperatureColor(temperature);
        Particle.DustOptions tempOptions = new Particle.DustOptions(tempColor, 0.8f);
        
        for (int i = 0; i < 15; i++) {
            double x = (i - 7) * 0.05;
            double tempVariation = Math.sin((ticks - i) * 0.05) * 10;
            double y = (temperature + tempVariation - 273.15) * 0.01; // Конвертация в Цельсии и масштабирование
            
            Location tempLoc = center.clone().add(x, y, 0);
            center.getWorld().spawnParticle(Particle.REDSTONE, tempLoc, 1, 0.01, 0.01, 0.01, 0, tempOptions);
        }
        
        // Текущая температура
        displayFloatingNumber(center.clone().add(0, 0.5, 0), 
            String.format("%.1f°C", temperature - 273.15), tempColor);
    }
    
    private static void displayPressureGauge(Location center, double pressure) {
        // Манометр
        double normalizedPressure = pressure / 101325.0; // Нормализация к атмосферному давлению
        Color pressureColor = getPressureColor(normalizedPressure);
        
        // Циферблат
        for (int i = 0; i < 12; i++) {
            double angle = i * Math.PI / 6;
            double x = Math.cos(angle) * 0.3;
            double z = Math.sin(angle) * 0.3;
            Location dialLoc = center.clone().add(x, 0, z);
            
            Color dialColor = Color.fromRGB(200, 200, 200);
            Particle.DustOptions dialOptions = new Particle.DustOptions(dialColor, 0.6f);
            center.getWorld().spawnParticle(Particle.REDSTONE, dialLoc, 1, 0, 0, 0, 0, dialOptions);
        }
        
        // Стрелка
        double needleAngle = normalizedPressure * Math.PI;
        for (double r = 0; r <= 0.25; r += 0.05) {
            double x = Math.cos(needleAngle) * r;
            double z = Math.sin(needleAngle) * r;
            Location needleLoc = center.clone().add(x, 0, z);
            
            Particle.DustOptions needleOptions = new Particle.DustOptions(pressureColor, 1.0f);
            center.getWorld().spawnParticle(Particle.REDSTONE, needleLoc, 1, 0, 0, 0, 0, needleOptions);
        }
        
        // Значение давления
        displayFloatingNumber(center.clone().add(0, -0.5, 0), 
            String.format("%.0f Pa", pressure), pressureColor);
    }
    
    private static void displayMolecularStructure(Location center, String moleculeName) {
        // Упрощенная молекулярная структура
        if (moleculeName.toLowerCase().contains("uranium")) {
            displayUraniumAtom(center);
        } else if (moleculeName.toLowerCase().contains("water")) {
            displayWaterMolecule(center);
        } else {
            displayGenericMolecule(center);
        }
    }
    
    private static void displayUraniumAtom(Location center) {
        // Ядро урана
        Color nucleusColor = Color.fromRGB(0, 255, 0);
        Particle.DustOptions nucleusOptions = new Particle.DustOptions(nucleusColor, 2.0f);
        center.getWorld().spawnParticle(Particle.REDSTONE, center, 1, 0.05, 0.05, 0.05, 0, nucleusOptions);
        
        // Электронные оболочки
        for (int shell = 1; shell <= 7; shell++) {
            double radius = shell * 0.1;
            int electrons = getElectronsInShell(shell);
            
            for (int e = 0; e < electrons; e++) {
                double angle = (e * 2 * Math.PI / electrons) + (System.currentTimeMillis() * 0.001 * shell);
                double x = Math.cos(angle) * radius;
                double z = Math.sin(angle) * radius;
                
                Location electronLoc = center.clone().add(x, 0, z);
                Color electronColor = Color.fromRGB(0, 0, 255);
                Particle.DustOptions electronOptions = new Particle.DustOptions(electronColor, 0.5f);
                center.getWorld().spawnParticle(Particle.REDSTONE, electronLoc, 1, 0.01, 0.01, 0.01, 0, electronOptions);
            }
        }
    }
    
    private static void displayWaterMolecule(Location center) {
        // Кислород
        Color oxygenColor = Color.fromRGB(255, 0, 0);
        Particle.DustOptions oxygenOptions = new Particle.DustOptions(oxygenColor, 1.5f);
        center.getWorld().spawnParticle(Particle.REDSTONE, center, 1, 0.02, 0.02, 0.02, 0, oxygenOptions);
        
        // Водороды
        Color hydrogenColor = Color.fromRGB(255, 255, 255);
        Particle.DustOptions hydrogenOptions = new Particle.DustOptions(hydrogenColor, 1.0f);
        
        Location h1 = center.clone().add(0.15, 0.1, 0);
        Location h2 = center.clone().add(-0.15, 0.1, 0);
        
        center.getWorld().spawnParticle(Particle.REDSTONE, h1, 1, 0.01, 0.01, 0.01, 0, hydrogenOptions);
        center.getWorld().spawnParticle(Particle.REDSTONE, h2, 1, 0.01, 0.01, 0.01, 0, hydrogenOptions);
        
        // Связи
        displayBond(center, h1);
        displayBond(center, h2);
    }
    
    private static void displayGenericMolecule(Location center) {
        // Простая молекула из 5 атомов
        Location[] atoms = {
            center,
            center.clone().add(0.2, 0, 0),
            center.clone().add(-0.2, 0, 0),
            center.clone().add(0, 0.2, 0),
            center.clone().add(0, -0.2, 0)
        };
        
        Color[] atomColors = {
            Color.fromRGB(255, 0, 0),   // Красный
            Color.fromRGB(0, 255, 0),   // Зеленый
            Color.fromRGB(0, 0, 255),   // Синий
            Color.fromRGB(255, 255, 0), // Желтый
            Color.fromRGB(255, 0, 255)  // Пурпурный
        };
        
        for (int i = 0; i < atoms.length; i++) {
            Particle.DustOptions atomOptions = new Particle.DustOptions(atomColors[i], 1.0f);
            center.getWorld().spawnParticle(Particle.REDSTONE, atoms[i], 1, 0.02, 0.02, 0.02, 0, atomOptions);
        }
        
        // Связи между атомами
        for (int i = 1; i < atoms.length; i++) {
            displayBond(atoms[0], atoms[i]);
        }
    }
    
    private static void displayBond(Location atom1, Location atom2) {
        Vector bond = atom2.toVector().subtract(atom1.toVector());
        int segments = 5;
        
        Color bondColor = Color.fromRGB(128, 128, 128);
        Particle.DustOptions bondOptions = new Particle.DustOptions(bondColor, 0.6f);
        
        for (int i = 1; i < segments; i++) {
            Location bondLoc = atom1.clone().add(bond.clone().multiply((double) i / segments));
            atom1.getWorld().spawnParticle(Particle.REDSTONE, bondLoc, 1, 0.005, 0.005, 0.005, 0, bondOptions);
        }
    }
    
    @SuppressWarnings("unchecked")
    private static void displaySampleList(Location center, List<String> samples) {
        for (int i = 0; i < Math.min(samples.size(), 5); i++) {
            String sample = samples.get(i);
            Location sampleLoc = center.clone().add(0, -i * 0.2, 0);
            
            Color sampleColor = Color.fromRGB(0, 255, 255);
            displayHolographicText(sampleLoc, sample, sampleColor);
        }
    }
    
    private static void displayTargetingReticle(Location center, Location target) {
        // Прицельная сетка
        Color reticleColor = Color.fromRGB(255, 0, 0);
        Particle.DustOptions reticleOptions = new Particle.DustOptions(reticleColor, 1.0f);
        
        // Крест
        for (double i = -0.3; i <= 0.3; i += 0.05) {
            center.getWorld().spawnParticle(Particle.REDSTONE, center.clone().add(i, 0, 0), 1, 0, 0, 0, 0, reticleOptions);
            center.getWorld().spawnParticle(Particle.REDSTONE, center.clone().add(0, i, 0), 1, 0, 0, 0, 0, reticleOptions);
        }
        
        // Круг
        for (int i = 0; i < 32; i++) {
            double angle = i * Math.PI / 16;
            double x = Math.cos(angle) * 0.2;
            double y = Math.sin(angle) * 0.2;
            center.getWorld().spawnParticle(Particle.REDSTONE, center.clone().add(x, y, 0), 1, 0, 0, 0, 0, reticleOptions);
        }
    }
    
    private static void displayBallisticData(Location center, double distance, double windSpeed, double accuracy) {
        displayHolographicPanel(center.clone().add(0, 0.4, 0), 
            String.format("DIST: %.1fm", distance), Color.fromRGB(255, 255, 0));
        displayHolographicPanel(center.clone().add(0, 0.2, 0), 
            String.format("WIND: %.1fm/s", windSpeed), Color.fromRGB(0, 255, 255));
        displayHolographicPanel(center, 
            String.format("ACC: %.1f%%", accuracy), 
            accuracy > 80 ? Color.fromRGB(0, 255, 0) : Color.fromRGB(255, 0, 0));
    }
    
    private static void displayProjectileTrajectory(Location start, Location target, double windSpeed) {
        Vector direction = target.toVector().subtract(start.toVector()).normalize();
        double distance = start.distance(target);
        
        Color trajectoryColor = Color.fromRGB(255, 128, 0);
        Particle.DustOptions trajectoryOptions = new Particle.DustOptions(trajectoryColor, 0.8f);
        
        for (double d = 0; d < distance; d += 0.5) {
            double progress = d / distance;
            double drop = progress * progress * 0.5; // Гравитация
            double windDrift = windSpeed * progress * 0.1; // Ветер
            
            Location trajectoryPoint = start.clone().add(direction.clone().multiply(d));
            trajectoryPoint.add(0, -drop, windDrift);
            
            start.getWorld().spawnParticle(Particle.REDSTONE, trajectoryPoint, 1, 0.02, 0.02, 0.02, 0, trajectoryOptions);
        }
    }
    
    private static void displayTargetInfo(Location center, Location target, double distance) {
        displayHolographicPanel(center.clone().add(0, 0.3, 0), "TARGET", Color.fromRGB(255, 0, 0));
        displayHolographicPanel(center.clone().add(0, 0.1, 0), 
            String.format("X: %.1f", target.getX()), Color.fromRGB(255, 255, 255));
        displayHolographicPanel(center.clone().add(0, -0.1, 0), 
            String.format("Y: %.1f", target.getY()), Color.fromRGB(255, 255, 255));
        displayHolographicPanel(center.clone().add(0, -0.3, 0), 
            String.format("Z: %.1f", target.getZ()), Color.fromRGB(255, 255, 255));
    }
    
    // Вспомогательные методы для цветов
    private static Color getRadiationColor(int radiation) {
        if (radiation < 20) return Color.fromRGB(0, 255, 0);
        if (radiation < 40) return Color.fromRGB(128, 255, 0);
        if (radiation < 60) return Color.fromRGB(255, 255, 0);
        if (radiation < 80) return Color.fromRGB(255, 128, 0);
        return Color.fromRGB(255, 0, 0);
    }
    
    private static Color getTemperatureColor(double temperature) {
        if (temperature < 273) return Color.fromRGB(0, 0, 255);      // Синий (холодный)
        if (temperature < 373) return Color.fromRGB(0, 255, 0);      // Зеленый (комнатная)
        if (temperature < 573) return Color.fromRGB(255, 255, 0);    // Желтый (теплый)
        if (temperature < 1273) return Color.fromRGB(255, 128, 0);   // Оранжевый (горячий)
        return Color.fromRGB(255, 0, 0);                             // Красный (очень горячий)
    }
    
    private static Color getPressureColor(double normalizedPressure) {
        if (normalizedPressure < 0.5) return Color.fromRGB(0, 0, 255);    // Низкое давление
        if (normalizedPressure < 1.5) return Color.fromRGB(0, 255, 0);    // Нормальное давление
        return Color.fromRGB(255, 0, 0);                                  // Высокое давление
    }
    
    private static int getElectronsInShell(int shell) {
        // Упрощенная модель электронных оболочек урана
        switch (shell) {
            case 1: return 2;
            case 2: return 8;
            case 3: return 18;
            case 4: return 32;
            case 5: return 21;
            case 6: return 9;
            case 7: return 2;
            default: return 0;
        }
    }
    
    // Управление голограммами
    public static void addHologram(UUID playerId, Hologram hologram) {
        activeHolograms.computeIfAbsent(playerId, k -> ConcurrentHashMap.newKeySet()).add(hologram);
    }
    
    public static void removeHologram(UUID playerId, UUID hologramId) {
        Set<Hologram> holograms = activeHolograms.get(playerId);
        if (holograms != null) {
            holograms.removeIf(hologram -> hologram.getId().equals(hologramId));
        }
    }
    
    public static void removeAllHolograms(UUID playerId) {
        Set<Hologram> holograms = activeHolograms.remove(playerId);
        if (holograms != null) {
            holograms.forEach(hologram -> hologram.setActive(false));
        }
    }
    
    public static void cleanup() {
        activeHolograms.values().forEach(holograms -> 
            holograms.forEach(hologram -> hologram.setActive(false))
        );
        activeHolograms.clear();
    }
}
