package com.uraniumcraft.teleporter;

import com.uraniumcraft.UraniumPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class TeleporterManager {
    
    private final UraniumPlugin plugin;
    private final Map<UUID, Teleporter> teleporters;
    private final Map<UUID, Set<UUID>> playerTeleporters; // UUID игрока -> Set UUID телепортов
    private File dataFile;
    private FileConfiguration dataConfig;
    
    public TeleporterManager(UraniumPlugin plugin) {
        this.plugin = plugin;
        this.teleporters = new HashMap<>();
        this.playerTeleporters = new HashMap<>();
        
        setupDataFile();
        loadTeleporters();
        
        // Автосохранение каждые 5 минут
        Bukkit.getScheduler().runTaskTimer(plugin, this::saveTeleporters, 6000, 6000);
    }
    
    private void setupDataFile() {
        dataFile = new File(plugin.getDataFolder(), "teleporters.yml");
        if (!dataFile.exists()) {
            try {
                dataFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Не удалось создать файл teleporters.yml: " + e.getMessage());
            }
        }
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
    }
    
    public Teleporter createTeleporter(Player player, String name, Location location) {
        // Проверяем лимит телепортов на игрока
        int maxPerPlayer = plugin.getConfig().getInt("teleporter.max_per_player", 5);
        Set<UUID> playerTeleporterIds = playerTeleporters.getOrDefault(player.getUniqueId(), new HashSet<>());
        
        if (playerTeleporterIds.size() >= maxPerPlayer) {
            player.sendMessage("§cВы достигли максимального количества телепортов (" + maxPerPlayer + ")!");
            return null;
        }
        
        // Проверяем, нет ли уже телепорта в этом месте
        for (Teleporter existing : teleporters.values()) {
            if (existing.getCoreLocation().distance(location) < 10) {
                player.sendMessage("§cСлишком близко к существующему телепорту!");
                return null;
            }
        }
        
        // Создаём новый телепорт
        Teleporter teleporter = new Teleporter(player.getUniqueId(), name, location);
        
        // Проверяем структуру
        if (!teleporter.validateStructure()) {
            player.sendMessage("§cНеправильная структура телепорта! Используйте /teleporter help для получения схемы.");
            return null;
        }
        
        // Регистрируем телепорт
        teleporters.put(teleporter.getId(), teleporter);
        playerTeleporterIds.add(teleporter.getId());
        playerTeleporters.put(player.getUniqueId(), playerTeleporterIds);
        
        // Активируем телепорт
        teleporter.activate();
        
        player.sendMessage("§aТелепорт '" + name + "' успешно создан!");
        saveTeleporters();
        
        return teleporter;
    }
    
    public boolean deleteTeleporter(UUID teleporterId, Player player) {
        Teleporter teleporter = teleporters.get(teleporterId);
        if (teleporter == null) {
            return false;
        }
        
        // Проверяем права
        if (!teleporter.getOwner().equals(player.getUniqueId()) && !player.hasPermission("uraniumcraft.teleporter.admin")) {
            player.sendMessage("§cУ вас нет прав для удаления этого телепорта!");
            return false;
        }
        
        // Деактивируем и удаляем
        teleporter.deactivate();
        teleporter.shutdown();
        teleporters.remove(teleporterId);
        
        // Удаляем из списка игрока
        Set<UUID> playerTeleporterIds = playerTeleporters.get(teleporter.getOwner());
        if (playerTeleporterIds != null) {
            playerTeleporterIds.remove(teleporterId);
        }
        
        player.sendMessage("§aТелепорт '" + teleporter.getName() + "' удалён!");
        saveTeleporters();
        
        return true;
    }
    
    public List<Teleporter> getPlayerTeleporters(UUID playerId) {
        Set<UUID> teleporterIds = playerTeleporters.getOrDefault(playerId, new HashSet<>());
        return teleporterIds.stream()
            .map(teleporters::get)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }
    
    public List<Teleporter> getAvailableTeleporters(Player player) {
        return teleporters.values().stream()
            .filter(t -> t.canPlayerUse(player))
            .filter(t -> t.validateStructure())
            .sorted((a, b) -> {
                // Сначала телепорты игрока, потом по расстоянию
                boolean aOwned = a.getOwner().equals(player.getUniqueId());
                boolean bOwned = b.getOwner().equals(player.getUniqueId());
                
                if (aOwned && !bOwned) return -1;
                if (!aOwned && bOwned) return 1;
                
                double distA = player.getLocation().distance(a.getCoreLocation());
                double distB = player.getLocation().distance(b.getCoreLocation());
                return Double.compare(distA, distB);
            })
            .collect(Collectors.toList());
    }
    
    public List<Teleporter> getPublicTeleporters() {
        return teleporters.values().stream()
            .filter(Teleporter::isPublic)
            .filter(t -> t.validateStructure())
            .filter(Teleporter::isActive)
            .collect(Collectors.toList());
    }
    
    public Teleporter getTeleporterAt(Location location) {
        return teleporters.values().stream()
            .filter(t -> t.getCoreLocation().distance(location) <= 2.0)
            .findFirst()
            .orElse(null);
    }
    
    public Teleporter getTeleporterById(UUID id) {
        return teleporters.get(id);
    }
    
    public Teleporter getTeleporterByName(String name, UUID ownerId) {
        return teleporters.values().stream()
            .filter(t -> t.getOwner().equals(ownerId))
            .filter(t -> t.getName().equalsIgnoreCase(name))
            .findFirst()
            .orElse(null);
    }
    
    public List<Teleporter> searchTeleporters(String query, Player player) {
        String lowerQuery = query.toLowerCase();
        return getAvailableTeleporters(player).stream()
            .filter(t -> t.getName().toLowerCase().contains(lowerQuery))
            .collect(Collectors.toList());
    }
    
    public void validateAllTeleporters() {
        List<UUID> toRemove = new ArrayList<>();
        
        for (Teleporter teleporter : teleporters.values()) {
            if (!teleporter.validateStructure()) {
                teleporter.deactivate();
                
                Player owner = Bukkit.getPlayer(teleporter.getOwner());
                if (owner != null) {
                    owner.sendMessage("§cВаш телепорт '" + teleporter.getName() + "' был деактивирован из-за повреждения структуры!");
                }
            }
        }
        
        // Удаляем телепорты в мирах, которые больше не существуют
        for (Map.Entry<UUID, Teleporter> entry : teleporters.entrySet()) {
            if (entry.getValue().getCoreLocation().getWorld() == null) {
                toRemove.add(entry.getKey());
            }
        }
        
        for (UUID id : toRemove) {
            Teleporter teleporter = teleporters.remove(id);
            if (teleporter != null) {
                teleporter.shutdown();
                
                Set<UUID> playerTeleporterIds = playerTeleporters.get(teleporter.getOwner());
                if (playerTeleporterIds != null) {
                    playerTeleporterIds.remove(id);
                }
            }
        }
        
        if (!toRemove.isEmpty()) {
            saveTeleporters();
            plugin.getLogger().info("Удалено " + toRemove.size() + " телепортов из несуществующих миров");
        }
    }
    
    public void saveTeleporters() {
        try {
            dataConfig = new YamlConfiguration();
            
            for (Map.Entry<UUID, Teleporter> entry : teleporters.entrySet()) {
                Teleporter teleporter = entry.getValue();
                String path = "teleporters." + entry.getKey().toString();
                
                // Проверяем, что мир существует перед сохранением
                if (teleporter.getCoreLocation().getWorld() == null) {
                    continue;
                }
                
                dataConfig.set(path + ".owner", teleporter.getOwner().toString());
                dataConfig.set(path + ".name", teleporter.getName());
                dataConfig.set(path + ".world", teleporter.getCoreLocation().getWorld().getName());
                dataConfig.set(path + ".x", teleporter.getCoreLocation().getX());
                dataConfig.set(path + ".y", teleporter.getCoreLocation().getY());
                dataConfig.set(path + ".z", teleporter.getCoreLocation().getZ());
                dataConfig.set(path + ".yaw", teleporter.getCoreLocation().getYaw());
                dataConfig.set(path + ".pitch", teleporter.getCoreLocation().getPitch());
                dataConfig.set(path + ".active", teleporter.isActive());
                dataConfig.set(path + ".public", teleporter.isPublic());
                dataConfig.set(path + ".energy", teleporter.getEnergyLevel());
                dataConfig.set(path + ".creation_time", teleporter.getCreationTime());
                dataConfig.set(path + ".total_teleportations", teleporter.getTotalTeleportations());
                
                // Сохраняем авторизованных игроков
                List<String> authorizedList = teleporter.getAuthorizedPlayers().stream()
                    .map(UUID::toString)
                    .collect(Collectors.toList());
                dataConfig.set(path + ".authorized", authorizedList);
            }
            
            dataConfig.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Не удалось сохранить телепорты: " + e.getMessage());
        } catch (Exception e) {
            plugin.getLogger().severe("Неожиданная ошибка при сохранении телепортов: " + e.getMessage());
        }
    }
    
    public void loadTeleporters() {
        if (!dataFile.exists()) {
            return;
        }
        
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
        ConfigurationSection teleportersSection = dataConfig.getConfigurationSection("teleporters");
        
        if (teleportersSection == null) {
            return;
        }
        
        int loaded = 0;
        int failed = 0;
        
        for (String idString : teleportersSection.getKeys(false)) {
            try {
                UUID id = UUID.fromString(idString);
                String path = "teleporters." + idString;
                
                UUID owner = UUID.fromString(dataConfig.getString(path + ".owner"));
                String name = dataConfig.getString(path + ".name");
                String worldName = dataConfig.getString(path + ".world");
                
                if (Bukkit.getWorld(worldName) == null) {
                    plugin.getLogger().warning("Мир " + worldName + " не найден для телепорта " + name);
                    failed++;
                    continue;
                }
                
                Location location = new Location(
                    Bukkit.getWorld(worldName),
                    dataConfig.getDouble(path + ".x"),
                    dataConfig.getDouble(path + ".y"),
                    dataConfig.getDouble(path + ".z"),
                    (float) dataConfig.getDouble(path + ".yaw"),
                    (float) dataConfig.getDouble(path + ".pitch")
                );
                
                Teleporter teleporter = new Teleporter(owner, name, location);
                teleporter.setActive(dataConfig.getBoolean(path + ".active"));
                teleporter.setPublic(dataConfig.getBoolean(path + ".public"));
                teleporter.setEnergyLevel(dataConfig.getInt(path + ".energy"));
                
                // Загружаем авторизованных игроков
                List<String> authorizedList = dataConfig.getStringList(path + ".authorized");
                for (String playerIdString : authorizedList) {
                    try {
                        teleporter.addAuthorizedPlayer(UUID.fromString(playerIdString));
                    } catch (IllegalArgumentException e) {
                        plugin.getLogger().warning("Неверный UUID игрока: " + playerIdString);
                    }
                }
                
                teleporters.put(id, teleporter);
                
                // Добавляем в список игрока
                playerTeleporters.computeIfAbsent(owner, k -> new HashSet<>()).add(id);
                
                loaded++;
                
            } catch (Exception e) {
                plugin.getLogger().warning("Не удалось загрузить телепорт " + idString + ": " + e.getMessage());
                failed++;
            }
        }
        
        plugin.getLogger().info("Загружено телепортов: " + loaded + ", ошибок: " + failed);
        
        // Проверяем структуры всех загруженных телепортов
        Bukkit.getScheduler().runTaskLater(plugin, this::validateAllTeleporters, 100);
    }
    
    public void shutdown() {
        saveTeleporters();
        
        for (Teleporter teleporter : teleporters.values()) {
            teleporter.shutdown();
        }
        
        teleporters.clear();
        playerTeleporters.clear();
    }
    
    public Map<UUID, Teleporter> getAllTeleporters() {
        return new HashMap<>(teleporters);
    }
    
    public int getTotalTeleporters() {
        return teleporters.size();
    }
    
    public int getActiveTeleporters() {
        return (int) teleporters.values().stream().filter(Teleporter::isActive).count();
    }
    
    public int getPublicTeleportersCount() {
        return (int) teleporters.values().stream().filter(Teleporter::isPublic).count();
    }
}
