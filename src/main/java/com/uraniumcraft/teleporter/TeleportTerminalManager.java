package com.uraniumcraft.teleporter;

import com.uraniumcraft.UraniumPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class TeleportTerminalManager {
    
    private final UraniumPlugin plugin;
    private final Map<UUID, TeleportTerminal> terminals;
    private final Map<String, UUID> terminalsByName;
    private File terminalsFile;
    private FileConfiguration terminalsConfig;
    
    public TeleportTerminalManager(UraniumPlugin plugin) {
        this.plugin = plugin;
        this.terminals = new HashMap<>();
        this.terminalsByName = new HashMap<>();
        
        setupTerminalsFile();
        loadTerminals();
    }
    
    private void setupTerminalsFile() {
        terminalsFile = new File(plugin.getDataFolder(), "terminals.yml");
        if (!terminalsFile.exists()) {
            try {
                terminalsFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Не удалось создать файл terminals.yml: " + e.getMessage());
            }
        }
        terminalsConfig = YamlConfiguration.loadConfiguration(terminalsFile);
    }
    
    public void loadTerminals() {
        terminals.clear();
        terminalsByName.clear();
        
        ConfigurationSection terminalsSection = terminalsConfig.getConfigurationSection("terminals");
        if (terminalsSection == null) {
            return;
        }
        
        for (String terminalIdStr : terminalsSection.getKeys(false)) {
            try {
                UUID terminalId = UUID.fromString(terminalIdStr);
                ConfigurationSection terminalSection = terminalsSection.getConfigurationSection(terminalIdStr);
                
                if (terminalSection == null) continue;
                
                // Загружаем основные данные
                UUID owner = UUID.fromString(terminalSection.getString("owner"));
                String name = terminalSection.getString("name");
                Location location = deserializeLocation(terminalSection.getConfigurationSection("location"));
                
                if (location == null) continue;
                
                TeleportTerminal terminal = new TeleportTerminal(owner, name, location);
                
                // Загружаем дополнительные данные
                terminal.setActive(terminalSection.getBoolean("active", false));
                terminal.setPublic(terminalSection.getBoolean("public", false));
                terminal.setEnergyLevel(terminalSection.getInt("energy", 500));
                
                // Загружаем авторизованных игроков
                List<String> authorizedList = terminalSection.getStringList("authorized");
                for (String playerIdStr : authorizedList) {
                    try {
                        terminal.addAuthorizedPlayer(UUID.fromString(playerIdStr));
                    } catch (IllegalArgumentException ignored) {}
                }
                
                // Загружаем подключённые телепорты
                List<String> connectedList = terminalSection.getStringList("connected");
                for (String teleporterIdStr : connectedList) {
                    try {
                        terminal.connectTeleporter(UUID.fromString(teleporterIdStr));
                    } catch (IllegalArgumentException ignored) {}
                }
                
                terminals.put(terminalId, terminal);
                terminalsByName.put(name.toLowerCase(), terminalId);
                
                // Активируем терминал если он был активен
                if (terminal.isActive()) {
                    terminal.activate();
                }
                
            } catch (Exception e) {
                plugin.getLogger().warning("Ошибка загрузки терминала " + terminalIdStr + ": " + e.getMessage());
            }
        }
        
        plugin.getLogger().info("Загружено " + terminals.size() + " терминалов телепортации");
    }
    
    public void saveTerminals() {
        terminalsConfig.set("terminals", null); // Очищаем секцию
        
        for (TeleportTerminal terminal : terminals.values()) {
            String terminalPath = "terminals." + terminal.getId().toString();
            
            terminalsConfig.set(terminalPath + ".owner", terminal.getOwner().toString());
            terminalsConfig.set(terminalPath + ".name", terminal.getName());
            terminalsConfig.set(terminalPath + ".active", terminal.isActive());
            terminalsConfig.set(terminalPath + ".public", terminal.isPublic());
            terminalsConfig.set(terminalPath + ".energy", terminal.getEnergyLevel());
            
            // Сохраняем локацию
            serializeLocation(terminalsConfig, terminalPath + ".location", terminal.getTerminalLocation());
            
            // Сохраняем авторизованных игроков
            List<String> authorizedList = terminal.getAuthorizedPlayers().stream()
                .map(UUID::toString)
                .collect(Collectors.toList());
            terminalsConfig.set(terminalPath + ".authorized", authorizedList);
            
            // Сохраняем подключённые телепорты
            List<String> connectedList = terminal.getConnectedTeleporters().stream()
                .map(UUID::toString)
                .collect(Collectors.toList());
            terminalsConfig.set(terminalPath + ".connected", connectedList);
        }
        
        try {
            terminalsConfig.save(terminalsFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Ошибка сохранения терминалов: " + e.getMessage());
        }
    }
    
    public TeleportTerminal createTerminal(Player owner, String name, Location location) {
        // Проверяем уникальность имени
        if (terminalsByName.containsKey(name.toLowerCase())) {
            return null;
        }
        
        // Проверяем, нет ли уже терминала в этом месте
        for (TeleportTerminal existing : terminals.values()) {
            if (existing.getTerminalLocation().distance(location) < 5.0) {
                return null;
            }
        }
        
        TeleportTerminal terminal = new TeleportTerminal(owner.getUniqueId(), name, location);
        terminals.put(terminal.getId(), terminal);
        terminalsByName.put(name.toLowerCase(), terminal.getId());
        
        saveTerminals();
        return terminal;
    }
    
    public boolean deleteTerminal(UUID terminalId) {
        TeleportTerminal terminal = terminals.get(terminalId);
        if (terminal == null) {
            return false;
        }
        
        terminal.shutdown();
        terminals.remove(terminalId);
        terminalsByName.remove(terminal.getName().toLowerCase());
        
        saveTerminals();
        return true;
    }
    
    public boolean deleteTerminal(String name) {
        UUID terminalId = terminalsByName.get(name.toLowerCase());
        if (terminalId == null) {
            return false;
        }
        
        return deleteTerminal(terminalId);
    }
    
    public TeleportTerminal getTerminalById(UUID terminalId) {
        return terminals.get(terminalId);
    }
    
    public TeleportTerminal getTerminalByName(String name) {
        UUID terminalId = terminalsByName.get(name.toLowerCase());
        return terminalId != null ? terminals.get(terminalId) : null;
    }
    
    public TeleportTerminal getTerminalAt(Location location) {
        for (TeleportTerminal terminal : terminals.values()) {
            if (terminal.getTerminalLocation().distance(location) < 2.0) {
                return terminal;
            }
        }
        return null;
    }
    
    public List<TeleportTerminal> getPlayerTerminals(UUID playerId) {
        return terminals.values().stream()
            .filter(terminal -> terminal.getOwner().equals(playerId))
            .collect(Collectors.toList());
    }
    
    public List<TeleportTerminal> getAccessibleTerminals(Player player) {
        return terminals.values().stream()
            .filter(terminal -> terminal.canPlayerUse(player))
            .collect(Collectors.toList());
    }
    
    public List<TeleportTerminal> getAllTerminals() {
        return new ArrayList<>(terminals.values());
    }
    
    public void shutdown() {
        for (TeleportTerminal terminal : terminals.values()) {
            terminal.shutdown();
        }
        saveTerminals();
    }
    
    public void validateAllTerminals() {
        List<TeleportTerminal> toDeactivate = new ArrayList<>();
        
        for (TeleportTerminal terminal : terminals.values()) {
            if (terminal.isActive() && !terminal.validateStructure()) {
                toDeactivate.add(terminal);
            }
        }
        
        for (TeleportTerminal terminal : toDeactivate) {
            terminal.deactivate();
            
            Player owner = Bukkit.getPlayer(terminal.getOwner());
            if (owner != null) {
                owner.sendMessage(ChatColor.RED + "Терминал '" + terminal.getName() + 
                    "' был деактивирован из-за повреждения структуры!");
            }
        }
        
        if (!toDeactivate.isEmpty()) {
            saveTerminals();
        }
    }
    
    private void serializeLocation(FileConfiguration config, String path, Location location) {
        config.set(path + ".world", location.getWorld().getName());
        config.set(path + ".x", location.getX());
        config.set(path + ".y", location.getY());
        config.set(path + ".z", location.getZ());
        config.set(path + ".yaw", location.getYaw());
        config.set(path + ".pitch", location.getPitch());
    }
    
    private Location deserializeLocation(ConfigurationSection section) {
        if (section == null) return null;
        
        String worldName = section.getString("world");
        if (worldName == null || Bukkit.getWorld(worldName) == null) {
            return null;
        }
        
        return new Location(
            Bukkit.getWorld(worldName),
            section.getDouble("x"),
            section.getDouble("y"),
            section.getDouble("z"),
            (float) section.getDouble("yaw"),
            (float) section.getDouble("pitch")
        );
    }
    
    public int getTerminalCount() {
        return terminals.size();
    }
    
    public int getActiveTerminalCount() {
        return (int) terminals.values().stream()
            .filter(TeleportTerminal::isActive)
            .count();
    }
}
