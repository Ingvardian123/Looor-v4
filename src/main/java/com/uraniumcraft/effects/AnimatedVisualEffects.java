package com.uraniumcraft.effects;

import com.uraniumcraft.UraniumPlugin;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AnimatedVisualEffects {
    
    private static final Map<UUID, Integer> activeEffects = new HashMap<>();
    private static UraniumPlugin plugin;
    
    public static void initialize(UraniumPlugin pluginInstance) {
        plugin = pluginInstance;
    }
    
    // Анимированные эффекты для уранового планшета
    public static void playTabletBootupAnimation(Player player) {
        Location loc = player.getLocation().add(0, 1.5, 0);
        
        new BukkitRunnable() {
            int stage = 0;
            int ticks = 0;
            
            @Override
            public void run() {
                if (ticks > 100) {
                    cancel();
                    return;
                }
                
                switch (stage) {
                    case 0: // Инициализация
                        if (ticks % 5 == 0) {
                            loc.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, loc, 3, 0.3, 0.3, 0.3, 0.1);
                            loc.getWorld().playSound(loc, Sound.UI_BUTTON_CLICK, 0.3f, 1.5f + (ticks * 0.05f));
                        }
                        if (ticks == 20) stage = 1;
                        break;
                        
                    case 1: // Загрузка системы
                        if (ticks % 3 == 0) {
                            loc.getWorld().spawnParticle(Particle.END_ROD, loc, 1, 0.2, 0.2, 0.2, 0.05);
                            loc.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, loc, 2, 0.4, 0.4, 0.4, 0.1);
                        }
                        if (ticks == 50) stage = 2;
                        break;
                        
                    case 2: // Активация
                        if (ticks % 2 == 0) {
                            loc.getWorld().spawnParticle(Particle.ENCHANTMENT_TABLE, loc, 5, 0.5, 0.5, 0.5, 0.2);
                            loc.getWorld().spawnParticle(Particle.PORTAL, loc, 3, 0.3, 0.3, 0.3, 0.1);
                        }
                        if (ticks == 80) {
                            loc.getWorld().playSound(loc, Sound.BLOCK_BEACON_ACTIVATE, 1.0f, 1.8f);
                            playHolographicInterface(player, loc);
                        }
                        break;
                }
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
    
    // Голографический интерфейс планшета
    public static void playHolographicInterface(Player player, Location center) {
        int taskId = new BukkitRunnable() {
            int ticks = 0;
            double angle = 0;
            
            @Override
            public void run() {
                if (ticks > 200) {
                    cancel();
                    activeEffects.remove(player.getUniqueId());
                    return;
                }
                
                // Вращающиеся голографические панели
                for (int i = 0; i < 4; i++) {
                    double panelAngle = angle + (i * Math.PI / 2);
                    double x = center.getX() + Math.cos(panelAngle) * 1.5;
                    double z = center.getZ() + Math.sin(panelAngle) * 1.5;
                    double y = center.getY() + Math.sin(ticks * 0.1) * 0.3;
                    
                    Location panelLoc = new Location(center.getWorld(), x, y, z);
                    
                    // Голографические частицы
                    center.getWorld().spawnParticle(Particle.END_ROD, panelLoc, 2, 0.1, 0.1, 0.1, 0.02);
                    center.getWorld().spawnParticle(Particle.ENCHANTMENT_TABLE, panelLoc, 1, 0.05, 0.05, 0.05, 0.01);
                    
                    // Данные на панелях
                    if (ticks % 10 == 0) {
                        center.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, panelLoc, 1, 0.02, 0.02, 0.02, 0.01);
                    }
                }
                
                // Центральный процессор
                if (ticks % 5 == 0) {
                    center.getWorld().spawnParticle(Particle.PORTAL, center, 3, 0.2, 0.2, 0.2, 0.1);
                }
                
                angle += 0.05;
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L).getTaskId();
        
        activeEffects.put(player.getUniqueId(), taskId);
    }
    
    // Анимация зарядки рельсотрона
    public static void playRailgunChargingEffect(Player player) {
        Location loc = player.getEyeLocation();
        Vector direction = loc.getDirection();
        
        new BukkitRunnable() {
            int ticks = 0;
            double intensity = 0;
            
            @Override
            public void run() {
                if (ticks > 60) {
                    cancel();
                    playRailgunFireEffect(player, loc, direction);
                    return;
                }
                
                intensity = (double) ticks / 60.0;
                
                // Нарастающие электрические разряды
                int sparkCount = (int) (intensity * 15);
                for (int i = 0; i < sparkCount; i++) {
                    Vector offset = new Vector(
                        (Math.random() - 0.5) * intensity,
                        (Math.random() - 0.5) * intensity,
                        (Math.random() - 0.5) * intensity
                    );
                    Location sparkLoc = loc.clone().add(offset);
                    loc.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, sparkLoc, 1, 0, 0, 0, 0);
                }
                
                // Магнитное поле
                if (ticks % 3 == 0) {
                    for (int i = 0; i < 8; i++) {
                        double angle = (i * Math.PI / 4) + (ticks * 0.2);
                        Vector magneticField = new Vector(
                            Math.cos(angle) * intensity * 0.5,
                            Math.sin(angle) * intensity * 0.3,
                            0
                        );
                        Location fieldLoc = loc.clone().add(magneticField);
                        loc.getWorld().spawnParticle(Particle.END_ROD, fieldLoc, 1, 0, 0, 0, 0);
                    }
                }
                
                // Звуковые эффекты зарядки
                float pitch = 0.5f + (float) intensity * 1.5f;
                loc.getWorld().playSound(loc, Sound.BLOCK_CONDUIT_AMBIENT, (float) intensity * 0.5f, pitch);
                
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
    
    // Эффект выстрела рельсотрона
    public static void playRailgunFireEffect(Player player, Location start, Vector direction) {
        // Мощная вспышка
        start.getWorld().spawnParticle(Particle.FLASH, start, 5, 0, 0, 0, 0);
        start.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, start, 3, 0.5, 0.5, 0.5, 0);
        
        // Звук выстрела
        start.getWorld().playSound(start, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 2.0f, 1.8f);
        start.getWorld().playSound(start, Sound.ENTITY_GENERIC_EXPLODE, 1.5f, 0.8f);
        
        // Трассирующий снаряд
        new BukkitRunnable() {
            Location projectile = start.clone();
            int ticks = 0;
            
            @Override
            public void run() {
                if (ticks > 100 || projectile.getBlock().getType().isSolid()) {
                    // Взрыв при попадании
                    projectile.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, projectile, 5, 1, 1, 1, 0);
                    projectile.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, projectile, 30, 2, 2, 2, 0.5);
                    projectile.getWorld().playSound(projectile, Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.2f);
                    cancel();
                    return;
                }
                
                // Движение снаряда
                projectile.add(direction.clone().multiply(2));
                
                // Плазменный след
                projectile.getWorld().spawnParticle(Particle.DRAGON_BREATH, projectile, 5, 0.2, 0.2, 0.2, 0.02);
                projectile.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, projectile, 3, 0.1, 0.1, 0.1, 0.1);
                projectile.getWorld().spawnParticle(Particle.END_ROD, projectile, 2, 0.05, 0.05, 0.05, 0.05);
                
                // Искажение воздуха
                if (ticks % 2 == 0) {
                    projectile.getWorld().spawnParticle(Particle.CLOUD, projectile, 2, 0.3, 0.3, 0.3, 0.1);
                }
                
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
    
    // Анимация активации силовой брони
    public static void playPowerArmorActivation(Player player) {
        Location center = player.getLocation().add(0, 1, 0);
        
        new BukkitRunnable() {
            int ticks = 0;
            
            @Override
            public void run() {
                if (ticks > 80) {
                    cancel();
                    return;
                }
                
                // Энергетическое поле вокруг игрока
                double radius = 1.5 - (ticks * 0.01);
                for (int i = 0; i < 16; i++) {
                    double angle = (i * Math.PI / 8) + (ticks * 0.1);
                    double x = center.getX() + Math.cos(angle) * radius;
                    double z = center.getZ() + Math.sin(angle) * radius;
                    double y = center.getY() + (Math.sin(ticks * 0.2) * 0.5);
                    
                    Location fieldLoc = new Location(center.getWorld(), x, y, z);
                    center.getWorld().spawnParticle(Particle.END_ROD, fieldLoc, 1, 0, 0, 0, 0);
                    
                    if (ticks % 4 == 0) {
                        center.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, fieldLoc, 1, 0.1, 0.1, 0.1, 0.05);
                    }
                }
                
                // Центральная энергетическая вспышка
                if (ticks % 10 == 0) {
                    center.getWorld().spawnParticle(Particle.ENCHANTMENT_TABLE, center, 10, 0.5, 1, 0.5, 0.2);
                    center.getWorld().playSound(center, Sound.BLOCK_BEACON_POWER_SELECT, 0.5f, 1.5f + (ticks * 0.02f));
                }
                
                // Финальная активация
                if (ticks == 70) {
                    center.getWorld().spawnParticle(Particle.FLASH, center, 3, 0, 0, 0, 0);
                    center.getWorld().playSound(center, Sound.BLOCK_BEACON_ACTIVATE, 1.0f, 2.0f);
                    
                    // Защитный барьер
                    for (int i = 0; i < 32; i++) {
                        double angle = i * Math.PI / 16;
                        Vector offset = new Vector(Math.cos(angle) * 2, Math.random() * 2, Math.sin(angle) * 2);
                        Location barrierLoc = center.clone().add(offset);
                        center.getWorld().spawnParticle(Particle.BARRIER, barrierLoc, 1, 0, 0, 0, 0);
                    }
                }
                
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
    
    // Эффект работы центрифуги
    public static void playCentrifugeOperation(Location location) {
        int taskId = new BukkitRunnable() {
            int ticks = 0;
            double rotationSpeed = 0.1;
            
            @Override
            public void run() {
                if (ticks > 600) { // 30 секунд работы
                    cancel();
                    return;
                }
                
                // Увеличение скорости вращения
                if (ticks < 100) {
                    rotationSpeed += 0.002;
                }
                
                // Вращающиеся частицы
                for (int i = 0; i < 8; i++) {
                    double angle = (ticks * rotationSpeed) + (i * Math.PI / 4);
                    double radius = 1.0 + Math.sin(ticks * 0.05) * 0.2;
                    
                    double x = location.getX() + Math.cos(angle) * radius;
                    double z = location.getZ() + Math.sin(angle) * radius;
                    double y = location.getY() + 1 + Math.sin(angle * 2) * 0.3;
                    
                    Location particleLoc = new Location(location.getWorld(), x, y, z);
                    location.getWorld().spawnParticle(Particle.CRIT, particleLoc, 1, 0, 0, 0, 0);
                    
                    if (i % 2 == 0) {
                        location.getWorld().spawnParticle(Particle.SMOKE_NORMAL, particleLoc, 1, 0.1, 0.1, 0.1, 0.02);
                    }
                }
                
                // Центральный вихрь
                if (ticks % 5 == 0) {
                    location.getWorld().spawnParticle(Particle.PORTAL, location.clone().add(0.5, 1, 0.5), 3, 0.3, 0.3, 0.3, 0.1);
                }
                
                // Звук работы
                if (ticks % 20 == 0) {
                    float pitch = 0.8f + (float) rotationSpeed * 2;
                    location.getWorld().playSound(location, Sound.BLOCK_PISTON_EXTEND, 0.3f, pitch);
                }
                
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L).getTaskId();
    }
    
    // Квантовый телепорт - эффект зарядки
    public static void playQuantumTeleporterCharging(Location location) {
        new BukkitRunnable() {
            int ticks = 0;
            double energy = 0;
            
            @Override
            public void run() {
                if (ticks > 100) {
                    cancel();
                    playQuantumTeleportation(location);
                    return;
                }
                
                energy = (double) ticks / 100.0;
                
                // Квантовые кольца
                for (int ring = 0; ring < 3; ring++) {
                    double ringRadius = 1.0 + (ring * 0.5);
                    double ringHeight = 1.0 + (ring * 0.3);
                    
                    for (int i = 0; i < 16; i++) {
                        double angle = (i * Math.PI / 8) + (ticks * 0.1 * (ring + 1));
                        double x = location.getX() + Math.cos(angle) * ringRadius * energy;
                        double z = location.getZ() + Math.sin(angle) * ringRadius * energy;
                        double y = location.getY() + ringHeight;
                        
                        Location ringLoc = new Location(location.getWorld(), x, y, z);
                        location.getWorld().spawnParticle(Particle.PORTAL, ringLoc, 1, 0, 0, 0, 0);
                        location.getWorld().spawnParticle(Particle.ENCHANTMENT_TABLE, ringLoc, 1, 0.1, 0.1, 0.1, 0.02);
                    }
                }
                
                // Центральная энергия
                if (ticks % 3 == 0) {
                    int particleCount = (int) (energy * 10);
                    location.getWorld().spawnParticle(Particle.END_ROD, location.clone().add(0.5, 1.5, 0.5), 
                        particleCount, 0.3, 0.3, 0.3, 0.1);
                }
                
                // Квантовые флуктуации
                if (ticks % 5 == 0) {
                    for (int i = 0; i < 5; i++) {
                        Vector randomOffset = new Vector(
                            (Math.random() - 0.5) * 3 * energy,
                            Math.random() * 2 * energy,
                            (Math.random() - 0.5) * 3 * energy
                        );
                        Location fluctLoc = location.clone().add(randomOffset);
                        location.getWorld().spawnParticle(Particle.DRAGON_BREATH, fluctLoc, 1, 0, 0, 0, 0);
                    }
                }
                
                // Звуки зарядки
                float pitch = 0.5f + (float) energy * 2.0f;
                location.getWorld().playSound(location, Sound.BLOCK_CONDUIT_ACTIVATE, (float) energy, pitch);
                
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
    
    // Квантовая телепортация
    public static void playQuantumTeleportation(Location location) {
        // Мощная квантовая вспышка
        location.getWorld().spawnParticle(Particle.FLASH, location.clone().add(0.5, 1, 0.5), 10, 0, 0, 0, 0);
        location.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, location.clone().add(0.5, 1, 0.5), 5, 1, 1, 1, 0);
        
        // Квантовый разрыв пространства
        new BukkitRunnable() {
            int ticks = 0;
            
            @Override
            public void run() {
                if (ticks > 40) {
                    cancel();
                    return;
                }
                
                // Расширяющийся портал
                double radius = ticks * 0.1;
                for (int i = 0; i < 32; i++) {
                    double angle = i * Math.PI / 16;
                    double x = location.getX() + Math.cos(angle) * radius;
                    double z = location.getZ() + Math.sin(angle) * radius;
                    double y = location.getY() + 1 + Math.sin(ticks * 0.3) * 0.5;
                    
                    Location portalLoc = new Location(location.getWorld(), x, y, z);
                    location.getWorld().spawnParticle(Particle.PORTAL, portalLoc, 3, 0.1, 0.1, 0.1, 0.5);
                    location.getWorld().spawnParticle(Particle.END_ROD, portalLoc, 1, 0, 0, 0, 0);
                }
                
                // Квантовые искажения
                if (ticks % 2 == 0) {
                    for (int i = 0; i < 10; i++) {
                        Vector randomOffset = new Vector(
                            (Math.random() - 0.5) * 4,
                            Math.random() * 3,
                            (Math.random() - 0.5) * 4
                        );
                        Location distortLoc = location.clone().add(randomOffset);
                        location.getWorld().spawnParticle(Particle.DRAGON_BREATH, distortLoc, 2, 0.2, 0.2, 0.2, 0.1);
                    }
                }
                
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
        
        // Звуки телепортации
        location.getWorld().playSound(location, Sound.ENTITY_ENDERMAN_TELEPORT, 2.0f, 0.5f);
        location.getWorld().playSound(location, Sound.BLOCK_PORTAL_TRIGGER, 1.5f, 1.8f);
    }
    
    // Эффект работы дозиметра
    public static void playGeigerCounterEffect(Player player, int radiationLevel) {
        Location loc = player.getLocation().add(0, 1, 0);
        
        // Частота щелчков зависит от уровня радиации
        int clickFrequency = Math.max(1, 20 - (radiationLevel / 5));
        
        new BukkitRunnable() {
            int ticks = 0;
            
            @Override
            public void run() {
                if (ticks > 100) {
                    cancel();
                    return;
                }
                
                if (ticks % clickFrequency == 0) {
                    // Звук щелчка дозиметра
                    float pitch = 1.0f + (radiationLevel * 0.01f);
                    loc.getWorld().playSound(loc, Sound.UI_BUTTON_CLICK, 0.3f, pitch);
                    
                    // Визуальная индикация радиации
                    if (radiationLevel > 20) {
                        Color particleColor = Color.fromRGB(255, 255 - (radiationLevel * 2), 0);
                        Particle.DustOptions dustOptions = new Particle.DustOptions(particleColor, 0.8f);
                        loc.getWorld().spawnParticle(Particle.REDSTONE, loc, 2, 0.3, 0.3, 0.3, 0, dustOptions);
                    }
                    
                    if (radiationLevel > 50) {
                        loc.getWorld().spawnParticle(Particle.SMOKE_NORMAL, loc, 1, 0.2, 0.2, 0.2, 0.02);
                    }
                }
                
                // Экран дозиметра
                if (ticks % 10 == 0) {
                    loc.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, 
                        loc.clone().add(0.3, 0, 0), 1, 0.05, 0.05, 0.05, 0.01);
                }
                
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
    
    // Эффект лабораторного терминала
    public static void playLaboratoryTerminalEffect(Location location) {
        new BukkitRunnable() {
            int ticks = 0;
            
            @Override
            public void run() {
                if (ticks > 200) {
                    cancel();
                    return;
                }
                
                // Голографические данные
                if (ticks % 8 == 0) {
                    for (int i = 0; i < 3; i++) {
                        double x = location.getX() + 0.5 + (Math.random() - 0.5) * 0.8;
                        double y = location.getY() + 1.2 + (i * 0.3);
                        double z = location.getZ() + 0.5 + (Math.random() - 0.5) * 0.8;
                        
                        Location dataLoc = new Location(location.getWorld(), x, y, z);
                        location.getWorld().spawnParticle(Particle.ENCHANTMENT_TABLE, dataLoc, 1, 0.05, 0.05, 0.05, 0.02);
                        location.getWorld().spawnParticle(Particle.END_ROD, dataLoc, 1, 0.02, 0.02, 0.02, 0.01);
                    }
                }
                
                // Сканирующий луч
                if (ticks % 20 == 0) {
                    double scanHeight = 1.0 + Math.sin(ticks * 0.1) * 0.5;
                    for (int i = 0; i < 8; i++) {
                        double angle = i * Math.PI / 4;
                        double x = location.getX() + 0.5 + Math.cos(angle) * 0.7;
                        double z = location.getZ() + 0.5 + Math.sin(angle) * 0.7;
                        double y = location.getY() + scanHeight;
                        
                        Location scanLoc = new Location(location.getWorld(), x, y, z);
                        location.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, scanLoc, 1, 0, 0, 0, 0);
                    }
                }
                
                // Звуки работы терминала
                if (ticks % 30 == 0) {
                    location.getWorld().playSound(location, Sound.UI_BUTTON_CLICK, 0.2f, 1.8f);
                }
                
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
    
    // Остановка всех эффектов для игрока
    public static void stopAllEffects(Player player) {
        Integer taskId = activeEffects.remove(player.getUniqueId());
        if (taskId != null) {
            Bukkit.getScheduler().cancelTask(taskId);
        }
    }
    
    // Очистка всех активных эффектов
    public static void cleanup() {
        for (Integer taskId : activeEffects.values()) {
            Bukkit.getScheduler().cancelTask(taskId);
        }
        activeEffects.clear();
    }
}
