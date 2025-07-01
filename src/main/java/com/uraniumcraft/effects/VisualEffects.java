package com.uraniumcraft.effects;

import com.uraniumcraft.UraniumPlugin;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class VisualEffects {
   
   // Эффекты телепорта
   public static void playTeleporterActivation(Location location) {
       World world = location.getWorld();
       if (world == null) return;
       
       // Частицы активации
       world.spawnParticle(Particle.ENCHANTMENT_TABLE, location.clone().add(0, 1, 0), 50, 1, 1, 1, 0.1);
       world.spawnParticle(Particle.END_ROD, location.clone().add(0, 2, 0), 20, 0.5, 0.5, 0.5, 0.05);
       
       // Звук активации
       world.playSound(location, Sound.BLOCK_BEACON_ACTIVATE, 1.0f, 1.0f);
   }
   
   public static void playTeleporterDeactivation(Location location) {
       World world = location.getWorld();
       if (world == null) return;
       
       // Частицы деактивации
       world.spawnParticle(Particle.SMOKE_NORMAL, location.clone().add(0, 1, 0), 30, 1, 1, 1, 0.05);
       
       // Звук деактивации
       world.playSound(location, Sound.BLOCK_BEACON_DEACTIVATE, 1.0f, 0.5f);
   }
   
   public static void playTeleporterAmbient(Location location) {
       World world = location.getWorld();
       if (world == null) return;
       
       // Окружающие частицы
       if (Math.random() < 0.3) {
           world.spawnParticle(Particle.ENCHANTMENT_TABLE, location.clone().add(0, 1, 0), 3, 1, 1, 1, 0.1);
       }
       
       if (Math.random() < 0.1) {
           world.spawnParticle(Particle.ELECTRIC_SPARK, location.clone().add(0, 2, 0), 2, 0.3, 0.3, 0.3, 0.05);
       }
   }
   
   public static void playTeleportCountdown(Location location, int countdown) {
       World world = location.getWorld();
       if (world == null) return;
       
       // Частицы обратного отсчёта
       world.spawnParticle(Particle.PORTAL, location.clone().add(0, 1, 0), countdown * 5, 0.5, 0.5, 0.5, 0.1);
       world.spawnParticle(Particle.ENCHANTMENT_TABLE, location.clone().add(0, 0.5, 0), countdown * 2, 0.3, 0.3, 0.3, 0.05);
   }
   
   public static void playTeleportationEffect(Location location) {
       World world = location.getWorld();
       if (world == null) return;
       
       // Эффект телепортации
       world.spawnParticle(Particle.PORTAL, location, 100, 1, 2, 1, 0.5);
       world.spawnParticle(Particle.ENCHANTMENT_TABLE, location, 50, 1, 1, 1, 0.3);
       world.spawnParticle(Particle.END_ROD, location.clone().add(0, 1, 0), 30, 0.5, 0.5, 0.5, 0.1);
       
       // Звук телепортации
       world.playSound(location, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
   }
   
   // Эффекты лаборатории
   public static void playLaboratoryEffect(Location location) {
       World world = location.getWorld();
       if (world == null) return;
       
       world.spawnParticle(Particle.VILLAGER_HAPPY, location.clone().add(0.5, 1, 0.5), 10, 0.5, 0.5, 0.5, 0.1);
       world.spawnParticle(Particle.ENCHANTMENT_TABLE, location.clone().add(0.5, 1.5, 0.5), 5, 0.3, 0.3, 0.3, 0.05);
   }
   
   // Эффекты центрифуги
   public static void playCentrifugeEffect(Location location) {
       World world = location.getWorld();
       if (world == null) return;
       
       world.spawnParticle(Particle.SMOKE_NORMAL, location.clone().add(0.5, 1, 0.5), 5, 0.3, 0.3, 0.3, 0.05);
       world.spawnParticle(Particle.CRIT, location.clone().add(0.5, 1.2, 0.5), 3, 0.2, 0.2, 0.2, 0.1);
   }
   
   // Эффекты радиации
   public static void playRadiationEffect(Location location) {
       World world = location.getWorld();
       if (world == null) return;
       
       world.spawnParticle(Particle.REDSTONE, location.clone().add(0, 1, 0), 8, 0.5, 0.5, 0.5, 0.1);
       world.spawnParticle(Particle.SMOKE_NORMAL, location.clone().add(0, 0.5, 0), 3, 0.3, 0.3, 0.3, 0.05);
   }
   
   // Эффекты достижений
   public static void playAchievementEffect(Player player) {
       Location location = player.getLocation();
       World world = location.getWorld();
       if (world == null) return;
       
       world.spawnParticle(Particle.FIREWORKS_SPARK, location.clone().add(0, 1, 0), 20, 1, 1, 1, 0.2);
       world.spawnParticle(Particle.VILLAGER_HAPPY, location.clone().add(0, 1.5, 0), 15, 0.5, 0.5, 0.5, 0.1);
       
       world.playSound(location, Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
   }
   
   // Анимированные эффекты
   public static void playSpinningEffect(Location center, Particle particle, int duration) {
       new BukkitRunnable() {
           int ticks = 0;
           final int maxTicks = duration * 20; // Конвертируем секунды в тики
           
           @Override
           public void run() {
               if (ticks >= maxTicks) {
                   cancel();
                   return;
               }
               
               double angle = (ticks * 0.2) % (2 * Math.PI);
               double radius = 1.5;
               
               double x = center.getX() + radius * Math.cos(angle);
               double z = center.getZ() + radius * Math.sin(angle);
               double y = center.getY() + 1 + 0.5 * Math.sin(ticks * 0.1);
               
               Location particleLocation = new Location(center.getWorld(), x, y, z);
               center.getWorld().spawnParticle(particle, particleLocation, 1, 0, 0, 0, 0);
               
               ticks++;
           }
       }.runTaskTimer(UraniumPlugin.getInstance(), 0, 1);
   }
   
   public static void playHelixEffect(Location center, Particle particle, int duration) {
       new BukkitRunnable() {
           int ticks = 0;
           final int maxTicks = duration * 20;
           
           @Override
           public void run() {
               if (ticks >= maxTicks) {
                   cancel();
                   return;
               }
               
               double angle = (ticks * 0.3) % (2 * Math.PI);
               double radius = 1.0;
               double height = (ticks % 40) * 0.1; // Высота от 0 до 4 блоков
               
               double x = center.getX() + radius * Math.cos(angle);
               double z = center.getZ() + radius * Math.sin(angle);
               double y = center.getY() + height;
               
               Location particleLocation = new Location(center.getWorld(), x, y, z);
               center.getWorld().spawnParticle(particle, particleLocation, 1, 0, 0, 0, 0);
               
               ticks++;
           }
       }.runTaskTimer(UraniumPlugin.getInstance(), 0, 2);
   }
   
   // Эффект взрыва энергии
   public static void playEnergyBurst(Location location) {
       World world = location.getWorld();
       if (world == null) return;
       
       // Основной взрыв
       world.spawnParticle(Particle.EXPLOSION_LARGE, location, 1, 0, 0, 0, 0);
       world.spawnParticle(Particle.ELECTRIC_SPARK, location, 50, 2, 2, 2, 0.3);
       world.spawnParticle(Particle.END_ROD, location, 30, 1.5, 1.5, 1.5, 0.2);
       
       // Звук взрыва
       world.playSound(location, Sound.ENTITY_GENERIC_EXPLODE, 0.5f, 1.5f);
       world.playSound(location, Sound.BLOCK_CONDUIT_ACTIVATE, 1.0f, 2.0f);
   }
   
   // Эффект зарядки
   public static void playChargingEffect(Location location, int level) {
       World world = location.getWorld();
       if (world == null) return;
       
       int particleCount = level * 2;
       double spread = 0.3 + (level * 0.1);
       
       world.spawnParticle(Particle.ELECTRIC_SPARK, location.clone().add(0, 1, 0), 
           particleCount, spread, spread, spread, 0.1);
       
       if (level > 5) {
           world.spawnParticle(Particle.END_ROD, location.clone().add(0, 1.5, 0), 
               level / 2, spread * 0.5, spread * 0.5, spread * 0.5, 0.05);
       }
       
       // Звук зарядки
       float pitch = 1.0f + (level * 0.1f);
       world.playSound(location, Sound.BLOCK_NOTE_BLOCK_CHIME, 0.3f, pitch);
   }
}
