package com.uraniumcraft.teleporter;

import com.uraniumcraft.UraniumPlugin;
import com.uraniumcraft.effects.VisualEffects;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class Teleporter {
    
    private final UUID id;
    private UUID owner;
    private String name;
    private Location coreLocation;
    private boolean active;
    private boolean isPublic;
    private int energyLevel;
    private int maxEnergy;
    private long creationTime;
    private int totalTeleportations;
    private Set<UUID> authorizedPlayers;
    private Map<UUID, Long> lastTeleportation;
    private boolean isTeleporting;
    private BukkitTask ambientEffectTask;
    private BukkitTask energyRegenTask;
    
    public Teleporter(UUID owner, String name, Location coreLocation) {
        this.id = UUID.randomUUID();
        this.owner = owner;
        this.name = name;
        this.coreLocation = coreLocation.clone();
        this.active = false;
        this.isPublic = false;
        this.energyLevel = 1000; // Начальная энергия
        this.maxEnergy = 5000;
        this.creationTime = System.currentTimeMillis();
        this.totalTeleportations = 0;
        this.authorizedPlayers = new HashSet<>();
        this.lastTeleportation = new HashMap<>();
        this.isTeleporting = false;
    }
    
    public boolean validateStructure() {
        World world = coreLocation.getWorld();
        if (world == null) return false;
        
        int x = coreLocation.getBlockX();
        int y = coreLocation.getBlockY();
        int z = coreLocation.getBlockZ();
        
        // Проверяем маяк в центре
        if (world.getBlockAt(x, y, z).getType() != Material.BEACON) {
            return false;
        }
        
        // Проверяем основание 3x3 из кварцевых блоков
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                Block block = world.getBlockAt(x + dx, y - 1, z + dz);
                if (block.getType() != Material.QUARTZ_BLOCK) {
                    return false;
                }
            }
        }
        
        // Проверяем столбы по углам (высота 3)
        int[][] corners = {{-1, -1}, {-1, 1}, {1, -1}, {1, 1}};
        for (int[] corner : corners) {
            for (int dy = 0; dy < 3; dy++) {
                Block block = world.getBlockAt(x + corner[0], y + dy, z + corner[1]);
                if (block.getType() != Material.QUARTZ_PILLAR) {
                    return false;
                }
            }
        }
        
        // Проверяем раму из стекла на высоте 3
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (dx == 0 && dz == 0) continue; // Пропускаем центр
                Block block = world.getBlockAt(x + dx, y + 3, z + dz);
                if (block.getType() != Material.GLASS) {
                    return false;
                }
            }
        }
        
        // Проверяем морские фонари на расстоянии 2 блоков
        int[][] crystalPositions = {{-2, 0}, {2, 0}, {0, -2}, {0, 2}};
        for (int[] pos : crystalPositions) {
            Block block = world.getBlockAt(x + pos[0], y + 1, z + pos[1]);
            if (block.getType() != Material.SEA_LANTERN) {
                return false;
            }
        }
        
        return true;
    }
    
    public void activate() {
        if (!validateStructure()) {
            return;
        }
        
        this.active = true;
        
        // Запускаем окружающие эффекты
        startAmbientEffects();
        
        // Запускаем регенерацию энергии
        startEnergyRegeneration();
        
        // Эффект активации
        VisualEffects.playTeleporterActivation(coreLocation);
        
        // Звук активации
        coreLocation.getWorld().playSound(coreLocation, Sound.BLOCK_BEACON_ACTIVATE, 1.0f, 1.0f);
    }
    
    public void deactivate() {
        this.active = false;
        
        // Останавливаем эффекты
        stopAmbientEffects();
        stopEnergyRegeneration();
        
        // Эффект деактивации
        VisualEffects.playTeleporterDeactivation(coreLocation);
        
        // Звук деактивации
        coreLocation.getWorld().playSound(coreLocation, Sound.BLOCK_BEACON_DEACTIVATE, 1.0f, 0.5f);
    }
    
    public void startTeleportation(Player player, Teleporter destination) {
        if (!canTeleport()) {
            player.sendMessage(ChatColor.RED + "Телепорт недоступен!");
            return;
        }
        
        if (!destination.canTeleport()) {
            player.sendMessage(ChatColor.RED + "Телепорт назначения недоступен!");
            return;
        }
        
        if (!canPlayerUse(player)) {
            player.sendMessage(ChatColor.RED + "У вас нет доступа к этому телепорту!");
            return;
        }
        
        // Проверяем кулдаун
        long lastTp = lastTeleportation.getOrDefault(player.getUniqueId(), 0L);
        long cooldown = UraniumPlugin.getInstance().getConfig().getLong("teleporter.cooldown", 30000);
        
        if (System.currentTimeMillis() - lastTp < cooldown && !player.hasPermission("uraniumcraft.teleporter.nocooldown")) {
            long remaining = (cooldown - (System.currentTimeMillis() - lastTp)) / 1000;
            player.sendMessage(ChatColor.RED + "Подождите " + remaining + " секунд перед следующей телепортацией!");
            return;
        }
        
        // Рассчитываем стоимость энергии
        double distance = coreLocation.distance(destination.getCoreLocation());
        int energyCost = (int) Math.max(100, distance / 10);
        
        if (energyLevel < energyCost) {
            player.sendMessage(ChatColor.RED + "Недостаточно энергии! Требуется: " + energyCost + ", доступно: " + energyLevel);
            return;
        }
        
        // Начинаем процесс телепортации
        isTeleporting = true;
        Location playerStart = player.getLocation().clone();
        
        player.sendMessage(ChatColor.YELLOW + "Начинается телепортация к '" + destination.getName() + "'...");
        player.sendMessage(ChatColor.GRAY + "Не двигайтесь в течение 5 секунд!");
        
        // Обратный отсчёт
        new BukkitRunnable() {
            int countdown = 5;
            
            @Override
            public void run() {
                // Проверяем, не сдвинулся ли игрок
                if (player.getLocation().distance(playerStart) > 1.0) {
                    player.sendMessage(ChatColor.RED + "Телепортация отменена - вы сдвинулись с места!");
                    isTeleporting = false;
                    cancel();
                    return;
                }
                
                if (countdown > 0) {
                    player.sendTitle(ChatColor.GOLD + "Телепортация", 
                        ChatColor.YELLOW + "Осталось: " + countdown + " сек", 0, 20, 0);
                    
                    // Эффекты обратного отсчёта
                    VisualEffects.playTeleportCountdown(player.getLocation(), countdown);
                    player.getWorld().playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f + (countdown * 0.1f));
                    
                    countdown--;
                } else {
                    // Выполняем телепортацию
                    executeTeleportation(player, destination, energyCost);
                    isTeleporting = false;
                    cancel();
                }
            }
        }.runTaskTimer(UraniumPlugin.getInstance(), 0, 20);
    }
    
    private void executeTeleportation(Player player, Teleporter destination, int energyCost) {
        World sourceWorld = player.getWorld();
        World destWorld = destination.getCoreLocation().getWorld();
        
        // Эффекты телепортации - упрощенные
        if (sourceWorld != null) {
            sourceWorld.spawnParticle(Particle.PORTAL, player.getLocation(), 20, 1, 1, 1, 0.5);
            sourceWorld.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 0.5f);
        }
        
        // Телепортируем игрока
        Location teleportLoc = destination.getTeleportLocation();
        player.teleport(teleportLoc);
        
        // Эффекты в месте назначения
        if (destWorld != null) {
            destWorld.spawnParticle(Particle.PORTAL, teleportLoc, 20, 1, 1, 1, 0.5);
            destWorld.playSound(teleportLoc, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.5f);
        }
        
        // Тратим энергию
        this.energyLevel -= energyCost;
        
        // Обновляем статистику
        this.totalTeleportations++;
        destination.totalTeleportations++;
        this.lastTeleportation.put(player.getUniqueId(), System.currentTimeMillis());
        
        // Сообщения
        player.sendMessage(ChatColor.GREEN + "Телепортация завершена! Добро пожаловать в '" + destination.getName() + "'!");
        player.sendTitle(ChatColor.GREEN + "Телепортация завершена!", 
            ChatColor.AQUA + destination.getName(), 10, 40, 10);
        
        // Уведомляем владельца телепорта назначения
        Player destinationOwner = Bukkit.getPlayer(destination.getOwner());
        if (destinationOwner != null && !destinationOwner.equals(player)) {
            destinationOwner.sendMessage(ChatColor.GRAY + "Игрок " + player.getName() + 
                " телепортировался к вашему телепорту '" + destination.getName() + "'");
        }
    }
    
    public Location getTeleportLocation() {
        // Безопасная позиция для телепортации (на маяке)
        return coreLocation.clone().add(0.5, 1, 0.5);
    }
    
    public boolean canTeleport() {
        return active && validateStructure() && energyLevel >= 100;
    }
    
    public boolean canPlayerUse(Player player) {
        // Владелец может всегда
        if (owner.equals(player.getUniqueId())) {
            return true;
        }
        
        // Админы могут всегда
        if (player.hasPermission("uraniumcraft.teleporter.admin")) {
            return true;
        }
        
        // Публичные телепорты доступны всем
        if (isPublic) {
            return true;
        }
        
        // Авторизованные игроки
        return authorizedPlayers.contains(player.getUniqueId());
    }
    
    private void startAmbientEffects() {
        if (ambientEffectTask != null) {
            ambientEffectTask.cancel();
        }
        
        ambientEffectTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!active || !validateStructure()) {
                    cancel();
                    return;
                }
                
                try {
                    World world = coreLocation.getWorld();
                    if (world != null) {
                        // Простые окружающие эффекты
                        world.spawnParticle(Particle.PORTAL, 
                            coreLocation.clone().add(0, 1, 0), 3, 0.5, 0.5, 0.5, 0.1);
                        world.spawnParticle(Particle.ENCHANTMENT_TABLE, 
                            coreLocation.clone().add(0, 1.5, 0), 2, 0.3, 0.3, 0.3, 0.05);
                    }
                } catch (Exception e) {
                    // Игнорируем ошибки эффектов
                }
            }
        }.runTaskTimer(UraniumPlugin.getInstance(), 0, 100); // Каждые 5 секунд
    }
    
    private void stopAmbientEffects() {
        if (ambientEffectTask != null) {
            ambientEffectTask.cancel();
            ambientEffectTask = null;
        }
    }
    
    private void startEnergyRegeneration() {
        if (energyRegenTask != null) {
            energyRegenTask.cancel();
        }
        
        energyRegenTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!active) {
                    cancel();
                    return;
                }
                
                if (energyLevel < maxEnergy) {
                    energyLevel += 5; // Регенерация 5 энергии каждые 10 секунд
                    if (energyLevel > maxEnergy) {
                        energyLevel = maxEnergy;
                    }
                }
            }
        }.runTaskTimer(UraniumPlugin.getInstance(), 200, 200); // Каждые 10 секунд
    }
    
    private void stopEnergyRegeneration() {
        if (energyRegenTask != null) {
            energyRegenTask.cancel();
            energyRegenTask = null;
        }
    }
    
    public void shutdown() {
        stopAmbientEffects();
        stopEnergyRegeneration();
    }
    
    public String getStatusString() {
        if (!active) {
            return ChatColor.RED + "Неактивен";
        } else if (!validateStructure()) {
            return ChatColor.GOLD + "Повреждён";
        } else if (energyLevel < 100) {
            return ChatColor.YELLOW + "Мало энергии";
        } else {
            return ChatColor.GREEN + "Активен";
        }
    }
    
    public double getEnergyPercentage() {
        return ((double) energyLevel / maxEnergy) * 100.0;
    }
    
    public void addEnergy(int amount) {
        this.energyLevel = Math.min(maxEnergy, energyLevel + amount);
    }
    
    public void addAuthorizedPlayer(UUID playerId) {
        authorizedPlayers.add(playerId);
    }
    
    public void removeAuthorizedPlayer(UUID playerId) {
        authorizedPlayers.remove(playerId);
    }
    
    // Геттеры и сеттеры
    public UUID getId() { return id; }
    public UUID getOwner() { return owner; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Location getCoreLocation() { return coreLocation.clone(); }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public boolean isPublic() { return isPublic; }
    public void setPublic(boolean isPublic) { this.isPublic = isPublic; }
    public int getEnergyLevel() { return energyLevel; }
    public void setEnergyLevel(int energyLevel) { this.energyLevel = energyLevel; }
    public int getMaxEnergy() { return maxEnergy; }
    public long getCreationTime() { return creationTime; }
    public int getTotalTeleportations() { return totalTeleportations; }
    public void setTotalTeleportations(int totalTeleportations) { this.totalTeleportations = totalTeleportations; }
    public Set<UUID> getAuthorizedPlayers() { return new HashSet<>(authorizedPlayers); }
    public boolean isTeleporting() { return isTeleporting; }
}
