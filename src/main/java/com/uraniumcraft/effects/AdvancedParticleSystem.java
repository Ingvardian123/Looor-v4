package com.uraniumcraft.effects;

import com.uraniumcraft.UraniumPlugin;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class AdvancedParticleSystem {
    
    private static final Map<UUID, Set<ParticleEffect>> activeEffects = new ConcurrentHashMap<>();
    private static UraniumPlugin plugin;
    
    public static void initialize(UraniumPlugin pluginInstance) {
        plugin = pluginInstance;
    }
    
    // Класс для представления эффекта частиц
    public static class ParticleEffect {
        private final UUID id;
        private final Location location;
        private final EffectType type;
        private final Map<String, Object> parameters;
        private int duration;
        private boolean active;
        
        public ParticleEffect(Location location, EffectType type, int duration) {
            this.id = UUID.randomUUID();
            this.location = location.clone();
            this.type = type;
            this.duration = duration;
            this.parameters = new HashMap<>();
            this.active = true;
        }
        
        public ParticleEffect setParameter(String key, Object value) {
            parameters.put(key, value);
            return this;
        }
        
        public Object getParameter(String key) {
            return parameters.get(key);
        }
        
        // Геттеры
        public UUID getId() { return id; }
        public Location getLocation() { return location; }
        public EffectType getType() { return type; }
        public int getDuration() { return duration; }
        public boolean isActive() { return active; }
        
        public void setDuration(int duration) { this.duration = duration; }
        public void setActive(boolean active) { this.active = active; }
    }
    
    // Типы эффектов
    public enum EffectType {
        RADIATION_FIELD,
        QUANTUM_DISTORTION,
        ENERGY_DISCHARGE,
        PLASMA_TRAIL,
        MAGNETIC_FIELD,
        NUCLEAR_REACTION,
        HOLOGRAPHIC_DISPLAY,
        ELECTROMAGNETIC_PULSE,
        PARTICLE_ACCELERATOR,
        FUSION_REACTION,
        ANTIMATTER_CONTAINMENT,
        WARP_FIELD,
        GRAVITATIONAL_ANOMALY,
        TEMPORAL_DISTORTION,
        DIMENSIONAL_RIFT
    }
    
    // Создание радиационного поля
    public static ParticleEffect createRadiationField(Location center, int intensity, int duration) {
        ParticleEffect effect = new ParticleEffect(center, EffectType.RADIATION_FIELD, duration)
            .setParameter("intensity", intensity)
            .setParameter("radius", 3.0 + (intensity * 0.1))
            .setParameter("color", getRadiationColor(intensity));
        
        startRadiationFieldEffect(effect);
        return effect;
    }
    
    private static void startRadiationFieldEffect(ParticleEffect effect) {
        new BukkitRunnable() {
            int ticks = 0;
            double angle = 0;
            
            @Override
            public void run() {
                if (!effect.isActive() || ticks > effect.getDuration()) {
                    effect.setActive(false);
                    cancel();
                    return;
                }
                
                Location center = effect.getLocation();
                int intensity = (Integer) effect.getParameter("intensity");
                double radius = (Double) effect.getParameter("radius");
                Color color = (Color) effect.getParameter("color");
                
                // Основное радиационное поле
                for (int i = 0; i < 16; i++) {
                    double particleAngle = angle + (i * Math.PI / 8);
                    double x = center.getX() + Math.cos(particleAngle) * radius;
                    double z = center.getZ() + Math.sin(particleAngle) * radius;
                    double y = center.getY() + Math.sin(ticks * 0.1) * 0.5;
                    
                    Location particleLoc = new Location(center.getWorld(), x, y, z);
                    
                    // Радиоактивные частицы
                    Particle.DustOptions dustOptions = new Particle.DustOptions(color, 1.2f);
                    center.getWorld().spawnParticle(Particle.REDSTONE, particleLoc, 1, 0.1, 0.1, 0.1, 0, dustOptions);
                    
                    // Дополнительные эффекты для высокой радиации
                    if (intensity > 50) {
                        center.getWorld().spawnParticle(Particle.SMOKE_NORMAL, particleLoc, 1, 0.2, 0.2, 0.2, 0.02);
                    }
                    if (intensity > 80) {
                        center.getWorld().spawnParticle(Particle.FLAME, particleLoc, 1, 0.1, 0.1, 0.1, 0.01);
                    }
                }
                
                // Центральный источник
                if (ticks % 10 == 0) {
                    center.getWorld().spawnParticle(Particle.END_ROD, center, intensity / 10, 0.3, 0.3, 0.3, 0.1);
                }
                
                // Радиационные волны
                if (ticks % 20 == 0) {
                    double waveRadius = (ticks % 100) * 0.1;
                    for (int i = 0; i < 32; i++) {
                        double waveAngle = i * Math.PI / 16;
                        double x = center.getX() + Math.cos(waveAngle) * waveRadius;
                        double z = center.getZ() + Math.sin(waveAngle) * waveRadius;
                        
                        Location waveLoc = new Location(center.getWorld(), x, center.getY(), z);
                        Particle.DustOptions waveOptions = new Particle.DustOptions(color, 0.8f);
                        center.getWorld().spawnParticle(Particle.REDSTONE, waveLoc, 1, 0, 0, 0, 0, waveOptions);
                    }
                }
                
                angle += 0.05;
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
    
    // Создание квантового искажения
    public static ParticleEffect createQuantumDistortion(Location center, double intensity, int duration) {
        ParticleEffect effect = new ParticleEffect(center, EffectType.QUANTUM_DISTORTION, duration)
            .setParameter("intensity", intensity)
            .setParameter("phase", 0.0);
        
        startQuantumDistortionEffect(effect);
        return effect;
    }
    
    private static void startQuantumDistortionEffect(ParticleEffect effect) {
        new BukkitRunnable() {
            int ticks = 0;
            double phase = 0;
            
            @Override
            public void run() {
                if (!effect.isActive() || ticks > effect.getDuration()) {
                    effect.setActive(false);
                    cancel();
                    return;
                }
                
                Location center = effect.getLocation();
                double intensity = (Double) effect.getParameter("intensity");
                
                // Квантовые флуктуации
                for (int i = 0; i < 20; i++) {
                    Vector randomOffset = new Vector(
                        (Math.random() - 0.5) * intensity * 2,
                        (Math.random() - 0.5) * intensity,
                        (Math.random() - 0.5) * intensity * 2
                    );
                    
                    Location fluctLoc = center.clone().add(randomOffset);
                    
                    // Квантовые частицы
                    center.getWorld().spawnParticle(Particle.DRAGON_BREATH, fluctLoc, 1, 0.1, 0.1, 0.1, 0.02);
                    center.getWorld().spawnParticle(Particle.PORTAL, fluctLoc, 2, 0.2, 0.2, 0.2, 0.5);
                    
                    // Вероятностные облака
                    if (Math.random() < 0.3) {
                        center.getWorld().spawnParticle(Particle.END_ROD, fluctLoc, 1, 0.05, 0.05, 0.05, 0.01);
                    }
                }
                
                // Квантовые кольца
                for (int ring = 0; ring < 3; ring++) {
                    double ringRadius = (ring + 1) * intensity * 0.5;
                    double ringPhase = phase + (ring * Math.PI / 3);
                    
                    for (int i = 0; i < 16; i++) {
                        double angle = (i * Math.PI / 8) + ringPhase;
                        double x = center.getX() + Math.cos(angle) * ringRadius;
                        double z = center.getZ() + Math.sin(angle) * ringRadius;
                        double y = center.getY() + Math.sin(angle * 2 + phase) * 0.5;
                        
                        Location ringLoc = new Location(center.getWorld(), x, y, z);
                        
                        // Квантовые частицы кольца
                        Color quantumColor = Color.fromRGB(
                            (int) (128 + 127 * Math.sin(phase + ring)),
                            (int) (64 + 64 * Math.cos(phase + ring)),
                            255
                        );
                        Particle.DustOptions quantumOptions = new Particle.DustOptions(quantumColor, 1.0f);
                        center.getWorld().spawnParticle(Particle.REDSTONE, ringLoc, 1, 0, 0, 0, 0, quantumOptions);
                    }
                }
                
                // Центральная сингулярность
                if (ticks % 5 == 0) {
                    center.getWorld().spawnParticle(Particle.ENCHANTMENT_TABLE, center, 10, 0.1, 0.1, 0.1, 1.0);
                }
                
                phase += 0.1;
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
    
    // Создание энергетического разряда
    public static ParticleEffect createEnergyDischarge(Location start, Location end, int power, int duration) {
        ParticleEffect effect = new ParticleEffect(start, EffectType.ENERGY_DISCHARGE, duration)
            .setParameter("end", end)
            .setParameter("power", power);
        
        startEnergyDischargeEffect(effect);
        return effect;
    }
    
    private static void startEnergyDischargeEffect(ParticleEffect effect) {
        new BukkitRunnable() {
            int ticks = 0;
            
            @Override
            public void run() {
                if (!effect.isActive() || ticks > effect.getDuration()) {
                    effect.setActive(false);
                    cancel();
                    return;
                }
                
                Location start = effect.getLocation();
                Location end = (Location) effect.getParameter("end");
                int power = (Integer) effect.getParameter("power");
                
                Vector direction = end.toVector().subtract(start.toVector()).normalize();
                double distance = start.distance(end);
                
                // Основной луч
                for (double d = 0; d < distance; d += 0.2) {
                    Location beamLoc = start.clone().add(direction.clone().multiply(d));
                    
                    // Добавляем случайные отклонения для эффекта молнии
                    Vector randomOffset = new Vector(
                        (Math.random() - 0.5) * 0.3,
                        (Math.random() - 0.5) * 0.3,
                        (Math.random() - 0.5) * 0.3
                    );
                    beamLoc.add(randomOffset);
                    
                    // Электрические частицы
                    start.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, beamLoc, 2, 0.1, 0.1, 0.1, 0.1);
                    start.getWorld().spawnParticle(Particle.END_ROD, beamLoc, 1, 0.05, 0.05, 0.05, 0.02);
                    
                    // Дополнительные эффекты для мощных разрядов
                    if (power > 50) {
                        start.getWorld().spawnParticle(Particle.FLAME, beamLoc, 1, 0.1, 0.1, 0.1, 0.01);
                    }
                }
                
                // Боковые разряды
                if (ticks % 3 == 0) {
                    for (int i = 0; i < power / 20; i++) {
                        double progress = Math.random();
                        Location sideLoc = start.clone().add(direction.clone().multiply(distance * progress));
                        
                        Vector sideDirection = new Vector(
                            (Math.random() - 0.5) * 2,
                            (Math.random() - 0.5) * 2,
                            (Math.random() - 0.5) * 2
                        ).normalize();
                        
                        for (double s = 0; s < 1.0; s += 0.3) {
                            Location sparkLoc = sideLoc.clone().add(sideDirection.clone().multiply(s));
                            start.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, sparkLoc, 1, 0, 0, 0, 0);
                        }
                    }
                }
                
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
    
    // Создание плазменного следа
    public static ParticleEffect createPlasmaTrail(Location start, Vector direction, double speed, int duration) {
        ParticleEffect effect = new ParticleEffect(start, EffectType.PLASMA_TRAIL, duration)
            .setParameter("direction", direction)
            .setParameter("speed", speed)
            .setParameter("temperature", 10000.0);
        
        startPlasmaTrailEffect(effect);
        return effect;
    }
    
    private static void startPlasmaTrailEffect(ParticleEffect effect) {
        new BukkitRunnable() {
            Location currentLoc = effect.getLocation().clone();
            int ticks = 0;
            
            @Override
            public void run() {
                if (!effect.isActive() || ticks > effect.getDuration() || currentLoc.getBlock().getType().isSolid()) {
                    // Взрыв при столкновении
                    if (currentLoc.getBlock().getType().isSolid()) {
                        createPlasmaExplosion(currentLoc);
                    }
                    effect.setActive(false);
                    cancel();
                    return;
                }
                
                Vector direction = (Vector) effect.getParameter("direction");
                double speed = (Double) effect.getParameter("speed");
                double temperature = (Double) effect.getParameter("temperature");
                
                // Движение снаряда
                currentLoc.add(direction.clone().multiply(speed));
                
                // Плазменное ядро
                Color plasmaColor = getPlasmaColor(temperature);
                Particle.DustOptions plasmaOptions = new Particle.DustOptions(plasmaColor, 1.5f);
                currentLoc.getWorld().spawnParticle(Particle.REDSTONE, currentLoc, 3, 0.1, 0.1, 0.1, 0, plasmaOptions);
                
                // Ионизированный след
                currentLoc.getWorld().spawnParticle(Particle.DRAGON_BREATH, currentLoc, 5, 0.2, 0.2, 0.2, 0.02);
                currentLoc.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, currentLoc, 3, 0.15, 0.15, 0.15, 0.1);
                
                // Магнитное поле вокруг плазмы
                if (ticks % 2 == 0) {
                    for (int i = 0; i < 8; i++) {
                        double angle = i * Math.PI / 4;
                        Vector magneticOffset = new Vector(
                            Math.cos(angle) * 0.3,
                            Math.sin(angle) * 0.3,
                            0
                        );
                        Location magneticLoc = currentLoc.clone().add(magneticOffset);
                        currentLoc.getWorld().spawnParticle(Particle.END_ROD, magneticLoc, 1, 0, 0, 0, 0);
                    }
                }
                
                // Охлаждение плазмы
                temperature *= 0.995;
                effect.setParameter("temperature", temperature);
                
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
    
    // Взрыв плазмы
    private static void createPlasmaExplosion(Location center) {
        // Основной взрыв
        center.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, center, 5, 1, 1, 1, 0);
        center.getWorld().spawnParticle(Particle.FLASH, center, 3, 0, 0, 0, 0);
        
        // Плазменные осколки
        for (int i = 0; i < 20; i++) {
            Vector fragmentDirection = new Vector(
                (Math.random() - 0.5) * 2,
                Math.random(),
                (Math.random() - 0.5) * 2
            ).normalize();
            
            new BukkitRunnable() {
                Location fragmentLoc = center.clone();
                int life = 20;
                
                @Override
                public void run() {
                    if (life <= 0 || fragmentLoc.getBlock().getType().isSolid()) {
                        cancel();
                        return;
                    }
                    
                    fragmentLoc.add(fragmentDirection.clone().multiply(0.3));
                    
                    Color fragmentColor = getPlasmaColor(5000 + life * 100);
                    Particle.DustOptions fragmentOptions = new Particle.DustOptions(fragmentColor, 1.0f);
                    center.getWorld().spawnParticle(Particle.REDSTONE, fragmentLoc, 1, 0.05, 0.05, 0.05, 0, fragmentOptions);
                    
                    life--;
                }
            }.runTaskTimer(plugin, 0L, 1L);
        }
    }
    
    // Создание магнитного поля
    public static ParticleEffect createMagneticField(Location center, double strength, int duration) {
        ParticleEffect effect = new ParticleEffect(center, EffectType.MAGNETIC_FIELD, duration)
            .setParameter("strength", strength)
            .setParameter("fieldLines", generateMagneticFieldLines(center, strength));
        
        startMagneticFieldEffect(effect);
        return effect;
    }
    
    @SuppressWarnings("unchecked")
    private static void startMagneticFieldEffect(ParticleEffect effect) {
        new BukkitRunnable() {
            int ticks = 0;
            
            @Override
            public void run() {
                if (!effect.isActive() || ticks > effect.getDuration()) {
                    effect.setActive(false);
                    cancel();
                    return;
                }
                
                Location center = effect.getLocation();
                double strength = (Double) effect.getParameter("strength");
                List<Vector> fieldLines = (List<Vector>) effect.getParameter("fieldLines");
                
                // Отображение силовых линий
                for (Vector fieldLine : fieldLines) {
                    for (double t = 0; t <= 1.0; t += 0.1) {
                        Vector point = fieldLine.clone().multiply(t * strength);
                        Location fieldLoc = center.clone().add(point);
                        
                        // Магнитные частицы
                        Color magneticColor = Color.fromRGB(0, 100, 255);
                        Particle.DustOptions magneticOptions = new Particle.DustOptions(magneticColor, 0.8f);
                        center.getWorld().spawnParticle(Particle.REDSTONE, fieldLoc, 1, 0.02, 0.02, 0.02, 0, magneticOptions);
                    }
                }
                
                // Магнитные диполи
                if (ticks % 10 == 0) {
                    for (int i = 0; i < 8; i++) {
                        double angle = i * Math.PI / 4;
                        double radius = strength * 0.5;
                        
                        double x = center.getX() + Math.cos(angle) * radius;
                        double z = center.getZ() + Math.sin(angle) * radius;
                        double y = center.getY() + Math.sin(ticks * 0.1) * 0.2;
                        
                        Location dipoleLoc = new Location(center.getWorld(), x, y, z);
                        center.getWorld().spawnParticle(Particle.END_ROD, dipoleLoc, 1, 0, 0, 0, 0);
                    }
                }
                
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
    
    // Генерация силовых линий магнитного поля
    private static List<Vector> generateMagneticFieldLines(Location center, double strength) {
        List<Vector> fieldLines = new ArrayList<>();
        
        // Дипольное магнитное поле
        for (int i = 0; i < 16; i++) {
            double angle = i * Math.PI / 8;
            Vector fieldLine = new Vector(
                Math.cos(angle) * strength,
                Math.sin(angle * 2) * strength * 0.5,
                Math.sin(angle) * strength
            );
            fieldLines.add(fieldLine);
        }
        
        return fieldLines;
    }
    
    // Создание ядерной реакции
    public static ParticleEffect createNuclearReaction(Location center, String reactionType, int duration) {
        ParticleEffect effect = new ParticleEffect(center, EffectType.NUCLEAR_REACTION, duration)
            .setParameter("reactionType", reactionType)
            .setParameter("energy", 0.0)
            .setParameter("neutronCount", 0);
        
        startNuclearReactionEffect(effect);
        return effect;
    }
    
    private static void startNuclearReactionEffect(ParticleEffect effect) {
        new BukkitRunnable() {
            int ticks = 0;
            double energy = 0;
            
            @Override
            public void run() {
                if (!effect.isActive() || ticks > effect.getDuration()) {
                    effect.setActive(false);
                    cancel();
                    return;
                }
                
                Location center = effect.getLocation();
                String reactionType = (String) effect.getParameter("reactionType");
                
                // Увеличение энергии реакции
                energy += Math.random() * 10;
                effect.setParameter("energy", energy);
                
                switch (reactionType) {
                    case "fission":
                        performFissionReaction(center, energy, ticks);
                        break;
                    case "fusion":
                        performFusionReaction(center, energy, ticks);
                        break;
                    case "decay":
                        performDecayReaction(center, energy, ticks);
                        break;
                }
                
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
    
    // Реакция деления
    private static void performFissionReaction(Location center, double energy, int ticks) {
        // Ядро урана
        Color uraniumColor = Color.fromRGB(0, 255, 0);
        Particle.DustOptions uraniumOptions = new Particle.DustOptions(uraniumColor, 2.0f);
        center.getWorld().spawnParticle(Particle.REDSTONE, center, 1, 0, 0, 0, 0, uraniumOptions);
        
        // Нейтроны
        if (ticks % 5 == 0) {
            for (int i = 0; i < 3; i++) {
                Vector neutronDirection = new Vector(
                    (Math.random() - 0.5) * 2,
                    (Math.random() - 0.5) * 2,
                    (Math.random() - 0.5) * 2
                ).normalize();
                
                createNeutron(center, neutronDirection, energy);
            }
        }
        
        // Осколки деления
        if (ticks % 10 == 0) {
            for (int i = 0; i < 2; i++) {
                Vector fragmentDirection = new Vector(
                    (Math.random() - 0.5) * 2,
                    Math.random(),
                    (Math.random() - 0.5) * 2
                ).normalize();
                
                createFissionFragment(center, fragmentDirection, energy);
            }
        }
        
        // Гамма-излучение
        if (ticks % 3 == 0) {
            center.getWorld().spawnParticle(Particle.END_ROD, center, 5, 0.5, 0.5, 0.5, 0.2);
        }
    }
    
    // Реакция синтеза
    private static void performFusionReaction(Location center, double energy, int ticks) {
        // Дейтерий и тритий
        Color deuteriumColor = Color.fromRGB(100, 150, 255);
        Color tritiumColor = Color.fromRGB(255, 100, 150);
        
        Particle.DustOptions deuteriumOptions = new Particle.DustOptions(deuteriumColor, 1.5f);
        Particle.DustOptions tritiumOptions = new Particle.DustOptions(tritiumColor, 1.5f);
        
        // Ядра сближаются
        double separation = Math.max(0.1, 2.0 - (ticks * 0.05));
        
        Location deuteriumLoc = center.clone().add(separation / 2, 0, 0);
        Location tritiumLoc = center.clone().add(-separation / 2, 0, 0);
        
        center.getWorld().spawnParticle(Particle.REDSTONE, deuteriumLoc, 1, 0, 0, 0, 0, deuteriumOptions);
        center.getWorld().spawnParticle(Particle.REDSTONE, tritiumLoc, 1, 0, 0, 0, 0, tritiumOptions);
        
        // Кулоновский барьер
        if (separation > 0.5) {
            center.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, center, 3, separation, 0.1, separation, 0.1);
        }
        
        // Синтез произошел
        if (separation <= 0.5) {
            // Альфа-частица
            Color alphaColor = Color.fromRGB(255, 255, 0);
            Particle.DustOptions alphaOptions = new Particle.DustOptions(alphaColor, 2.0f);
            center.getWorld().spawnParticle(Particle.REDSTONE, center, 1, 0, 0, 0, 0, alphaOptions);
            
            // Нейтрон высокой энергии
            Vector neutronDirection = new Vector(Math.random() - 0.5, Math.random() - 0.5, Math.random() - 0.5).normalize();
            createHighEnergyNeutron(center, neutronDirection, energy * 2);
            
            // Энергия синтеза
            center.getWorld().spawnParticle(Particle.FLASH, center, 1, 0, 0, 0, 0);
            center.getWorld().spawnParticle(Particle.END_ROD, center, 10, 1, 1, 1, 0.5);
        }
    }
    
    // Радиоактивный распад
    private static void performDecayReaction(Location center, double energy, int ticks) {
        // Нестабильное ядро
        Color unstableColor = Color.fromRGB(255, (int)(255 * Math.random()), 0);
        Particle.DustOptions unstableOptions = new Particle.DustOptions(unstableColor, 1.8f);
        center.getWorld().spawnParticle(Particle.REDSTONE, center, 1, 0.1, 0.1, 0.1, 0, unstableOptions);
        
        // Случайный распад
        if (Math.random() < 0.1) {
            String decayType = getRandomDecayType();
            
            switch (decayType) {
                case "alpha":
                    // Альфа-распад
                    Vector alphaDirection = new Vector(Math.random() - 0.5, Math.random() - 0.5, Math.random() - 0.5).normalize();
                    createAlphaParticle(center, alphaDirection, energy);
                    break;
                    
                case "beta":
                    // Бета-распад
                    Vector betaDirection = new Vector(Math.random() - 0.5, Math.random() - 0.5, Math.random() - 0.5).normalize();
                    createBetaParticle(center, betaDirection, energy);
                    break;
                    
                case "gamma":
                    // Гамма-распад
                    center.getWorld().spawnParticle(Particle.END_ROD, center, 3, 0.3, 0.3, 0.3, 0.3);
                    break;
            }
        }
    }
    
    // Создание нейтрона
    private static void createNeutron(Location start, Vector direction, double energy) {
        new BukkitRunnable() {
            Location neutronLoc = start.clone();
            int life = 40;
            
            @Override
            public void run() {
                if (life <= 0 || neutronLoc.getBlock().getType().isSolid()) {
                    cancel();
                    return;
                }
                
                neutronLoc.add(direction.clone().multiply(0.2));
                
                Color neutronColor = Color.fromRGB(200, 200, 200);
                Particle.DustOptions neutronOptions = new Particle.DustOptions(neutronColor, 0.8f);
                start.getWorld().spawnParticle(Particle.REDSTONE, neutronLoc, 1, 0.02, 0.02, 0.02, 0, neutronOptions);
                
                life--;
            }
        }.runTaskTimer(plugin, 0L, 2L);
    }
    
    // Создание высокоэнергетического нейтрона
    private static void createHighEnergyNeutron(Location start, Vector direction, double energy) {
        new BukkitRunnable() {
            Location neutronLoc = start.clone();
            int life = 60;
            
            @Override
            public void run() {
                if (life <= 0 || neutronLoc.getBlock().getType().isSolid()) {
                    cancel();
                    return;
                }
                
                neutronLoc.add(direction.clone().multiply(0.4));
                
                Color neutronColor = Color.fromRGB(255, 255, 255);
                Particle.DustOptions neutronOptions = new Particle.DustOptions(neutronColor, 1.2f);
                start.getWorld().spawnParticle(Particle.REDSTONE, neutronLoc, 1, 0.05, 0.05, 0.05, 0, neutronOptions);
                start.getWorld().spawnParticle(Particle.END_ROD, neutronLoc, 1, 0.02, 0.02, 0.02, 0.02);
                
                life--;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
    
    // Создание осколка деления
    private static void createFissionFragment(Location start, Vector direction, double energy) {
        new BukkitRunnable() {
            Location fragmentLoc = start.clone();
            int life = 30;
            
            @Override
            public void run() {
                if (life <= 0 || fragmentLoc.getBlock().getType().isSolid()) {
                    cancel();
                    return;
                }
                
                fragmentLoc.add(direction.clone().multiply(0.15));
                
                Color fragmentColor = Color.fromRGB(255, 100, 0);
                Particle.DustOptions fragmentOptions = new Particle.DustOptions(fragmentColor, 1.5f);
                start.getWorld().spawnParticle(Particle.REDSTONE, fragmentLoc, 1, 0.1, 0.1, 0.1, 0, fragmentOptions);
                start.getWorld().spawnParticle(Particle.FLAME, fragmentLoc, 1, 0.05, 0.05, 0.05, 0.01);
                
                life--;
            }
        }.runTaskTimer(plugin, 0L, 3L);
    }
    
    // Создание альфа-частицы
    private static void createAlphaParticle(Location start, Vector direction, double energy) {
        new BukkitRunnable() {
            Location alphaLoc = start.clone();
            int life = 25;
            
            @Override
            public void run() {
                if (life <= 0 || alphaLoc.getBlock().getType().isSolid()) {
                    cancel();
                    return;
                }
                
                alphaLoc.add(direction.clone().multiply(0.1));
                
                Color alphaColor = Color.fromRGB(255, 255, 0);
                Particle.DustOptions alphaOptions = new Particle.DustOptions(alphaColor, 1.0f);
                start.getWorld().spawnParticle(Particle.REDSTONE, alphaLoc, 1, 0.03, 0.03, 0.03, 0, alphaOptions);
                
                life--;
            }
        }.runTaskTimer(plugin, 0L, 4L);
    }
    
    // Создание бета-частицы
    private static void createBetaParticle(Location start, Vector direction, double energy) {
        new BukkitRunnable() {
            Location betaLoc = start.clone();
            int life = 50;
            
            @Override
            public void run() {
                if (life <= 0 || betaLoc.getBlock().getType().isSolid()) {
                    cancel();
                    return;
                }
                
                betaLoc.add(direction.clone().multiply(0.3));
                
                Color betaColor = Color.fromRGB(0, 0, 255);
                Particle.DustOptions betaOptions = new Particle.DustOptions(betaColor, 0.6f);
                start.getWorld().spawnParticle(Particle.REDSTONE, betaLoc, 1, 0.02, 0.02, 0.02, 0, betaOptions);
                start.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, betaLoc, 1, 0.01, 0.01, 0.01, 0.01);
                
                life--;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
    
    // Вспомогательные методы
    private static Color getRadiationColor(int intensity) {
        if (intensity < 20) return Color.fromRGB(0, 255, 0);
        if (intensity < 40) return Color.fromRGB(128, 255, 0);
        if (intensity < 60) return Color.fromRGB(255, 255, 0);
        if (intensity < 80) return Color.fromRGB(255, 128, 0);
        return Color.fromRGB(255, 0, 0);
    }
    
    private static Color getPlasmaColor(double temperature) {
        if (temperature < 3000) return Color.fromRGB(255, 0, 0);
        if (temperature < 6000) return Color.fromRGB(255, 128, 0);
        if (temperature < 10000) return Color.fromRGB(255, 255, 0);
        if (temperature < 15000) return Color.fromRGB(255, 255, 255);
        return Color.fromRGB(128, 128, 255);
    }
    
    private static String getRandomDecayType() {
        String[] types = {"alpha", "beta", "gamma"};
        return types[(int) (Math.random() * types.length)];
    }
    
    // Управление эффектами
    public static void addEffect(UUID playerId, ParticleEffect effect) {
        activeEffects.computeIfAbsent(playerId, k -> ConcurrentHashMap.newKeySet()).add(effect);
    }
    
    public static void removeEffect(UUID playerId, UUID effectId) {
        Set<ParticleEffect> effects = activeEffects.get(playerId);
        if (effects != null) {
            effects.removeIf(effect -> effect.getId().equals(effectId));
        }
    }
    
    public static void removeAllEffects(UUID playerId) {
        Set<ParticleEffect> effects = activeEffects.remove(playerId);
        if (effects != null) {
            effects.forEach(effect -> effect.setActive(false));
        }
    }
    
    public static void cleanup() {
        activeEffects.values().forEach(effects -> 
            effects.forEach(effect -> effect.setActive(false))
        );
        activeEffects.clear();
    }
}
