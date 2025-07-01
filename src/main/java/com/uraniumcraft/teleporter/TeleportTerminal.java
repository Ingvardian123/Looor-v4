package com.uraniumcraft.teleporter;

import com.uraniumcraft.UraniumPlugin;
import com.uraniumcraft.effects.VisualEffects;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class TeleportTerminal {
    
    private final UUID id;
    private UUID owner;
    private String name;
    private Location terminalLocation;
    private boolean active;
    private boolean isPublic;
    private int energyLevel;
    private int maxEnergy;
    private long creationTime;
    private int totalTeleportations;
    private Set<UUID> authorizedPlayers;
    private Map<UUID, Long> lastTeleportation;
    private Set<UUID> connectedTeleporters;
    private BukkitTask ambientEffectTask;
    private BukkitTask energyRegenTask;
    private boolean isOperating;
    
    public TeleportTerminal(UUID owner, String name, Location terminalLocation) {
        this.id = UUID.randomUUID();
        this.owner = owner;
        this.name = name;
        this.terminalLocation = terminalLocation.clone();
        this.active = false;
        this.isPublic = false;
        this.energyLevel = 500; // Начальная энергия
        this.maxEnergy = 2000;
        this.creationTime = System.currentTimeMillis();
        this.totalTeleportations = 0;
        this.authorizedPlayers = new HashSet<>();
        this.lastTeleportation = new HashMap<>();
        this.connectedTeleporters = new HashSet<>();
        this.isOperating = false;
    }
    
    public boolean validateStructure() {
        World world = terminalLocation.getWorld();
        if (world == null) return false;
        
        int x = terminalLocation.getBlockX();
        int y = terminalLocation.getBlockY();
        int z = terminalLocation.getBlockZ();
        
        // Проверяем компьютер в центре
        if (world.getBlockAt(x, y, z).getType() != Material.OBSERVER) {
            return false;
        }
        
        // Проверяем основание 3x3 из железных блоков
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                Block block = world.getBlockAt(x + dx, y - 1, z + dz);
                if (block.getType() != Material.IRON_BLOCK) {
                    return false;
                }
            }
        }
        
        // Проверяем экраны по бокам (стекло)
        Block[] screens = {
            world.getBlockAt(x - 1, y, z),
            world.getBlockAt(x + 1, y, z),
            world.getBlockAt(x, y, z - 1),
            world.getBlockAt(x, y, z + 1)
        };
        
        for (Block screen : screens) {
            if (screen.getType() != Material.BLUE_STAINED_GLASS) {
                return false;
            }
        }
        
        return true;
    }
    
    public boolean canActivate(Player player) {
        // Проверяем, изучил ли игрок "ядро телепортации"
        UraniumPlugin plugin = UraniumPlugin.getInstance();
        
        // Проверяем через систему достижений/исследований
        if (plugin.getAchievementManager() != null) {
            return plugin.getAchievementManager().hasPlayerCompletedResearch(player.getUniqueId(), "teleportation_core");
        }
        
        // Если система достижений недоступна, проверяем права
        return player.hasPermission("uraniumcraft.teleporter.core") || player.hasPermission("uraniumcraft.teleporter.admin");
    }
    
    public void activate(Player activator) {
        if (!validateStructure()) {
            if (activator != null) {
                activator.sendMessage(ChatColor.RED + "Структура терминала повреждена!");
            }
            return;
        }
        
        if (activator != null && !canActivate(activator)) {
            activator.sendMessage(ChatColor.RED + "Для активации терминала необходимо изучить 'Ядро телепортации'!");
            activator.sendMessage(ChatColor.GRAY + "Завершите соответствующее исследование в лаборатории.");
            return;
        }
        
        this.active = true;
        
        // Запускаем окружающие эффекты
        startAmbientEffects();
        
        // Запускаем регенерацию энергии
        startEnergyRegeneration();
        
        // Эффект активации
        VisualEffects.playTerminalActivation(terminalLocation);
        
        // Звук активации
        terminalLocation.getWorld().playSound(terminalLocation, Sound.BLOCK_BEACON_ACTIVATE, 1.0f, 1.5f);
        
        // Уведомляем владельца
        Player owner = Bukkit.getPlayer(this.owner);
        if (owner != null) {
            owner.sendMessage(ChatColor.GREEN + "Терминал телепортации '" + name + "' активирован!");
        }
        
        if (activator != null && !activator.equals(owner)) {
            activator.sendMessage(ChatColor.GREEN + "Терминал '" + name + "' успешно активирован!");
        }
    }
    
    public void deactivate() {
        this.active = false;
        
        // Останавливаем эффекты
        stopAmbientEffects();
        stopEnergyRegeneration();
        
        // Эффект деактивации
        VisualEffects.playTerminalDeactivation(terminalLocation);
        
        // Звук деактивации
        terminalLocation.getWorld().playSound(terminalLocation, Sound.BLOCK_BEACON_DEACTIVATE, 1.0f, 0.8f);
        
        // Уведомляем владельца
        Player owner = Bukkit.getPlayer(this.owner);
        if (owner != null) {
            owner.sendMessage(ChatColor.RED + "Терминал телепортации '" + name + "' деактивирован!");
        }
    }
    
    public void startTeleportation(Player player, Teleporter destination) {
        if (!canOperate()) {
            player.sendMessage(ChatColor.RED + "Терминал недоступен!");
            return;
        }
        
        if (!destination.canTeleport()) {
            player.sendMessage(ChatColor.RED + "Телепорт назначения недоступен!");
            return;
        }
        
        if (!canPlayerUse(player)) {
            player.sendMessage(ChatColor.RED + "У вас нет доступа к этому терминалу!");
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
        
        // Рассчитываем стоимость энергии (терминал тратит больше энергии)
        double distance = terminalLocation.distance(destination.getCoreLocation());
        int energyCost = (int) Math.max(150, distance / 8); // Терминал дороже обычного телепорта
        
        if (energyLevel < energyCost) {
            player.sendMessage(ChatColor.RED + "Недостаточно энергии! Требуется: " + energyCost + ", доступно: " + energyLevel);
            return;
        }
        
        // Начинаем процесс телепортации
        isOperating = true;
        Location playerStart = player.getLocation().clone();
        
        player.sendMessage(ChatColor.AQUA + "Инициализация телепортации к '" + destination.getName() + "'...");
        player.sendMessage(ChatColor.GRAY + "Не отходите от терминала в течение 3 секунд!");
        
        // Обратный отсчёт (терминал быстрее)
        new BukkitRunnable() {
            int countdown = 3;
            
            @Override
            public void run() {
                // Проверяем, не отошёл ли игрок от терминала
                if (player.getLocation().distance(terminalLocation) > 3.0) {
                    player.sendMessage(ChatColor.RED + "Телепортация отменена - вы отошли от терминала!");
                    isOperating = false;
                    cancel();
                    return;
                }
                
                if (countdown > 0) {
                    player.sendTitle(ChatColor.AQUA + "Телепортация", 
                        ChatColor.YELLOW + "Инициализация: " + countdown + " сек", 0, 20, 0);
                    
                    // Эффекты обратного отсчёта
                    VisualEffects.playTerminalCountdown(terminalLocation, countdown);
                    terminalLocation.getWorld().playSound(terminalLocation, Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.5f + (countdown * 0.2f));
                    
                    countdown--;
                } else {
                    // Выполняем телепортацию
                    executeTerminalTeleportation(player, destination, energyCost);
                    isOperating = false;
                    cancel();
                }
            }
        }.runTaskTimer(UraniumPlugin.getInstance(), 0, 20);
    }
    
    private void executeTerminalTeleportation(Player player, Teleporter destination, int energyCost) {
        // Эффекты телепортации
        VisualEffects.playTerminalTeleportation(terminalLocation);
        VisualEffects.playTeleportationEffect(destination.getTeleportLocation());
        
        // Звуки
        terminalLocation.getWorld().playSound(terminalLocation, Sound.BLOCK_CONDUIT_ACTIVATE, 1.0f, 1.0f);
        destination.getCoreLocation().getWorld().playSound(destination.getCoreLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.5f);
        
        // Телепортируем игрока
        player.teleport(destination.getTeleportLocation());
        
        // Тратим энергию
        this.energyLevel -= energyCost;
        
        // Обновляем статистику
        this.totalTeleportations++;
        destination.setTotalTeleportations(destination.getTotalTeleportations() + 1);
        this.lastTeleportation.put(player.getUniqueId(), System.currentTimeMillis());
        
        // Сообщения
        player.sendMessage(ChatColor.GREEN + "Телепортация завершена! Добро пожаловать в '" + destination.getName() + "'!");
        player.sendTitle(ChatColor.GREEN + "Телепортация завершена!", 
            ChatColor.AQUA + destination.getName(), 10, 40, 10);
        
        // Уведомляем владельца телепорта назначения
        Player destinationOwner = Bukkit.getPlayer(destination.getOwner());
        if (destinationOwner != null && !destinationOwner.equals(player)) {
            destinationOwner.sendMessage(ChatColor.GRAY + "Игрок " + player.getName() + 
                " телепортировался к вашему телепорту '" + destination.getName() + "' через терминал");
        }
        
        // Уведомляем владельца терминала
        Player terminalOwner = Bukkit.getPlayer(this.owner);
        if (terminalOwner != null && !terminalOwner.equals(player)) {
            terminalOwner.sendMessage(ChatColor.GRAY + "Игрок " + player.getName() + 
                " использовал ваш терминал '" + name + "'");
        }
    }
    
    public boolean canOperate() {
        return active && validateStructure() && energyLevel >= 150;
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
        
        // Публичные терминалы доступны всем
        if (isPublic) {
            return true;
        }
        
        // Авторизованные игроки
        return authorizedPlayers.contains(player.getUniqueId());
    }
    
    public void connectTeleporter(UUID teleporterId) {
        connectedTeleporters.add(teleporterId);
    }
    
    public void disconnectTeleporter(UUID teleporterId) {
        connectedTeleporters.remove(teleporterId);
    }
    
    public List<Teleporter> getConnectedTeleporters() {
        TeleporterManager teleporterManager = UraniumPlugin.getInstance().getTeleporterManager();
        List<Teleporter> connected = new ArrayList<>();
        
        for (UUID teleporterId : connectedTeleporters) {
            Teleporter teleporter = teleporterManager.getTeleporterById(teleporterId);
            if (teleporter != null && teleporter.canTeleport()) {
                connected.add(teleporter);
            }
        }
        
        return connected;
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
                
                VisualEffects.playTerminalAmbient(terminalLocation);
                
                // Звуковые эффекты
                if (Math.random() < 0.1) {
                    terminalLocation.getWorld().playSound(terminalLocation, Sound.BLOCK_CONDUIT_AMBIENT, 0.3f, 1.0f);
                }
            }
        }.runTaskTimer(UraniumPlugin.getInstance(), 0, 80); // Каждые 4 секунды
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
                    energyLevel += 3; // Терминал регенерирует энергию медленнее
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
        } else if (energyLevel < 150) {
            return ChatColor.YELLOW + "Мало энергии";
        } else if (isOperating) {
            return ChatColor.BLUE + "Работает...";
        } else {
            return ChatColor.GREEN + "Готов";
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
    public Location getTerminalLocation() { return terminalLocation.clone(); }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public boolean isPublic() { return isPublic; }
    public void setPublic(boolean isPublic) { this.isPublic = isPublic; }
    public int getEnergyLevel() { return energyLevel; }
    public void setEnergyLevel(int energyLevel) { this.energyLevel = energyLevel; }
    public int getMaxEnergy() { return maxEnergy; }
    public long getCreationTime() { return creationTime; }
    public int getTotalTeleportations() { return totalTeleportations; }
    public Set<UUID> getAuthorizedPlayers() { return new HashSet<>(authorizedPlayers); }
    public Set<UUID> getConnectedTeleporters() { return new HashSet<>(connectedTeleporters); }
    public boolean isOperating() { return isOperating; }
}
