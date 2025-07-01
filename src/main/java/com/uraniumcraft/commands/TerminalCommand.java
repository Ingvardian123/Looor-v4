package com.uraniumcraft.commands;

import com.uraniumcraft.UraniumPlugin;
import com.uraniumcraft.teleporter.TeleportTerminal;
import com.uraniumcraft.teleporter.TeleportTerminalManager;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class TerminalCommand implements CommandExecutor, TabCompleter {
    
    private final UraniumPlugin plugin;
    private final TeleportTerminalManager terminalManager;
    
    public TerminalCommand(UraniumPlugin plugin) {
        this.plugin = plugin;
        this.terminalManager = plugin.getTeleportTerminalManager();
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Эта команда доступна только игрокам!");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (args.length == 0) {
            showHelp(player);
            return true;
        }
        
        switch (args[0].toLowerCase()) {
            case "create":
                handleCreate(player, args);
                break;
            case "activate":
                handleActivate(player, args);
                break;
            case "deactivate":
                handleDeactivate(player, args);
                break;
            case "list":
                handleList(player);
                break;
            case "info":
                handleInfo(player, args);
                break;
            case "delete":
                handleDelete(player, args);
                break;
            case "access":
                handleAccess(player, args);
                break;
            case "public":
                handlePublic(player, args);
                break;
            case "connect":
                handleConnect(player, args);
                break;
            case "disconnect":
                handleDisconnect(player, args);
                break;
            case "help":
                showHelp(player);
                break;
            default:
                player.sendMessage(ChatColor.RED + "Неизвестная команда! Используйте /terminal help");
                break;
        }
        
        return true;
    }
    
    private void handleCreate(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Использование: /terminal create <название>");
            return;
        }
        
        if (!player.hasPermission("uraniumcraft.terminal.create")) {
            player.sendMessage(ChatColor.RED + "У вас нет прав для создания терминалов!");
            return;
        }
        
        String name = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        
        if (name.length() > 32) {
            player.sendMessage(ChatColor.RED + "Название терминала слишком длинное! (максимум 32 символа)");
            return;
        }
        
        // Проверяем, что игрок стоит на наблюдателе
        Location location = player.getLocation();
        if (location.getBlock().getType() != Material.OBSERVER) {
            player.sendMessage(ChatColor.RED + "Встаньте на блок наблюдателя для создания терминала!");
            return;
        }
        
        // Проверяем лимит терминалов
        List<TeleportTerminal> playerTerminals = terminalManager.getPlayerTerminals(player.getUniqueId());
        int maxTerminals = getMaxTerminals(player);
        
        if (playerTerminals.size() >= maxTerminals) {
            player.sendMessage(ChatColor.RED + "Достигнут лимит терминалов! (" + maxTerminals + ")");
            return;
        }
        
        // Создаём терминал
        TeleportTerminal terminal = new TeleportTerminal(player.getUniqueId(), name, location);
        
        if (!terminal.validateStructure()) {
            player.sendMessage(ChatColor.RED + "Неправильная структура терминала!");
            player.sendMessage(ChatColor.GRAY + "Требуется:");
            player.sendMessage(ChatColor.GRAY + "• Основание 3x3 из железных блоков");
            player.sendMessage(ChatColor.GRAY + "• Наблюдатель в центре");
            player.sendMessage(ChatColor.GRAY + "• 4 синих стекла по бокам");
            return;
        }
        
        terminalManager.addTerminal(terminal);
        
        player.sendMessage(ChatColor.GREEN + "✅ Терминал '" + name + "' создан!");
        player.sendMessage(ChatColor.YELLOW + "Для активации изучите 'Ядро телепортации' в лаборатории");
        player.sendMessage(ChatColor.GRAY + "Используйте /terminal activate " + name + " для активации");
    }
    
    private void handleActivate(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Использование: /terminal activate <название>");
            return;
        }
        
        String name = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        TeleportTerminal terminal = terminalManager.getPlayerTerminalByName(player.getUniqueId(), name);
        
        if (terminal == null) {
            player.sendMessage(ChatColor.RED + "Терминал '" + name + "' не найден!");
            return;
        }
        
        if (terminal.isActive()) {
            player.sendMessage(ChatColor.YELLOW + "Терминал '" + name + "' уже активен!");
            return;
        }
        
        terminal.activate(player);
    }
    
    private void handleDeactivate(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Использование: /terminal deactivate <название>");
            return;
        }
        
        String name = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        TeleportTerminal terminal = terminalManager.getPlayerTerminalByName(player.getUniqueId(), name);
        
        if (terminal == null) {
            player.sendMessage(ChatColor.RED + "Терминал '" + name + "' не найден!");
            return;
        }
        
        if (!terminal.isActive()) {
            player.sendMessage(ChatColor.YELLOW + "Терминал '" + name + "' уже неактивен!");
            return;
        }
        
        terminal.deactivate();
        player.sendMessage(ChatColor.GREEN + "Терминал '" + name + "' деактивирован!");
    }
    
    private void handleList(Player player) {
        List<TeleportTerminal> terminals = terminalManager.getPlayerTerminals(player.getUniqueId());
        
        if (terminals.isEmpty()) {
            player.sendMessage(ChatColor.YELLOW + "У вас нет терминалов!");
            player.sendMessage(ChatColor.GRAY + "Используйте /terminal create <название> для создания");
            return;
        }
        
        player.sendMessage(ChatColor.GOLD + "=== Ваши терминалы ===");
        for (TeleportTerminal terminal : terminals) {
            String status = terminal.getStatusString();
            String energy = String.format("%.0f%%", terminal.getEnergyPercentage());
            
            player.sendMessage(ChatColor.YELLOW + "• " + ChatColor.WHITE + terminal.getName() + 
                ChatColor.GRAY + " - " + status + 
                ChatColor.GRAY + " | Энергия: " + ChatColor.AQUA + energy);
            
            if (terminal.isActive()) {
                player.sendMessage(ChatColor.GRAY + "  Телепортаций: " + terminal.getTotalTeleportations() + 
                    " | Подключено: " + terminal.getConnectedTeleporters().size());
            }
        }
        
        int maxTerminals = getMaxTerminals(player);
        player.sendMessage(ChatColor.GRAY + "Терминалов: " + terminals.size() + "/" + maxTerminals);
    }
    
    private void handleInfo(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Использование: /terminal info <название>");
            return;
        }
        
        String name = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        TeleportTerminal terminal = terminalManager.getPlayerTerminalByName(player.getUniqueId(), name);
        
        if (terminal == null) {
            player.sendMessage(ChatColor.RED + "Терминал '" + name + "' не найден!");
            return;
        }
        
        player.sendMessage(ChatColor.GOLD + "=== Информация о терминале ===");
        player.sendMessage(ChatColor.YELLOW + "Название: " + ChatColor.WHITE + terminal.getName());
        player.sendMessage(ChatColor.YELLOW + "Статус: " + terminal.getStatusString());
        player.sendMessage(ChatColor.YELLOW + "Энергия: " + ChatColor.AQUA + terminal.getEnergyLevel() + "/" + terminal.getMaxEnergy() + 
            ChatColor.GRAY + " (" + String.format("%.1f", terminal.getEnergyPercentage()) + "%)");
        player.sendMessage(ChatColor.YELLOW + "Публичный: " + (terminal.isPublic() ? ChatColor.GREEN + "Да" : ChatColor.RED + "Нет"));
        player.sendMessage(ChatColor.YELLOW + "Телепортаций: " + ChatColor.WHITE + terminal.getTotalTeleportations());
        player.sendMessage(ChatColor.YELLOW + "Подключено телепортов: " + ChatColor.WHITE + terminal.getConnectedTeleporters().size());
        player.sendMessage(ChatColor.YELLOW + "Авторизованных игроков: " + ChatColor.WHITE + terminal.getAuthorizedPlayers().size());
        
        Location loc = terminal.getTerminalLocation();
        player.sendMessage(ChatColor.YELLOW + "Координаты: " + ChatColor.WHITE + 
            loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ() + 
            " (" + loc.getWorld().getName() + ")");
        
        long createdAgo = (System.currentTimeMillis() - terminal.getCreationTime()) / 1000;
        player.sendMessage(ChatColor.YELLOW + "Создан: " + ChatColor.WHITE + formatTime(createdAgo) + " назад");
    }
    
    private void handleDelete(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Использование: /terminal delete <название>");
            return;
        }
        
        String name = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        TeleportTerminal terminal = terminalManager.getPlayerTerminalByName(player.getUniqueId(), name);
        
        if (terminal == null) {
            player.sendMessage(ChatColor.RED + "Терминал '" + name + "' не найден!");
            return;
        }
        
        terminalManager.removeTerminal(terminal.getId());
        player.sendMessage(ChatColor.GREEN + "Терминал '" + name + "' удалён!");
    }
    
    private void handleAccess(Player player, String[] args) {
        if (args.length < 4) {
            player.sendMessage(ChatColor.RED + "Использование: /terminal access <название> <add/remove> <игрок>");
            return;
        }
        
        String terminalName = args[1];
        String action = args[2].toLowerCase();
        String targetPlayerName = args[3];
        
        TeleportTerminal terminal = terminalManager.getPlayerTerminalByName(player.getUniqueId(), terminalName);
        if (terminal == null) {
            player.sendMessage(ChatColor.RED + "Терминал '" + terminalName + "' не найден!");
            return;
        }
        
        UUID targetUUID = plugin.getServer().getOfflinePlayer(targetPlayerName).getUniqueId();
        
        if (action.equals("add")) {
            terminal.addAuthorizedPlayer(targetUUID);
            player.sendMessage(ChatColor.GREEN + "Игрок " + targetPlayerName + " получил доступ к терминалу '" + terminalName + "'");
        } else if (action.equals("remove")) {
            terminal.removeAuthorizedPlayer(targetUUID);
            player.sendMessage(ChatColor.GREEN + "У игрока " + targetPlayerName + " отозван доступ к терминалу '" + terminalName + "'");
        } else {
            player.sendMessage(ChatColor.RED + "Используйте 'add' или 'remove'");
        }
    }
    
    private void handlePublic(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage(ChatColor.RED + "Использование: /terminal public <название> <true/false>");
            return;
        }
        
        String name = args[1];
        boolean isPublic = Boolean.parseBoolean(args[2]);
        
        TeleportTerminal terminal = terminalManager.getPlayerTerminalByName(player.getUniqueId(), name);
        if (terminal == null) {
            player.sendMessage(ChatColor.RED + "Терминал '" + name + "' не найден!");
            return;
        }
        
        terminal.setPublic(isPublic);
        player.sendMessage(ChatColor.GREEN + "Терминал '" + name + "' теперь " + 
            (isPublic ? "публичный" : "приватный"));
    }
    
    private void handleConnect(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage(ChatColor.RED + "Использование: /terminal connect <терминал> <телепорт>");
            return;
        }
        
        String terminalName = args[1];
        String teleporterName = args[2];
        
        TeleportTerminal terminal = terminalManager.getPlayerTerminalByName(player.getUniqueId(), terminalName);
        if (terminal == null) {
            player.sendMessage(ChatColor.RED + "Терминал '" + terminalName + "' не найден!");
            return;
        }
        
        // Здесь должна быть логика поиска телепорта по имени
        // Пока что заглушка
        player.sendMessage(ChatColor.YELLOW + "Функция подключения телепортов в разработке");
    }
    
    private void handleDisconnect(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage(ChatColor.RED + "Использование: /terminal disconnect <терминал> <телепорт>");
            return;
        }
        
        String terminalName = args[1];
        String teleporterName = args[2];
        
        TeleportTerminal terminal = terminalManager.getPlayerTerminalByName(player.getUniqueId(), terminalName);
        if (terminal == null) {
            player.sendMessage(ChatColor.RED + "Терминал '" + terminalName + "' не найден!");
            return;
        }
        
        // Здесь должна быть логика отключения телепорта
        // Пока что заглушка
        player.sendMessage(ChatColor.YELLOW + "Функция отключения телепортов в разработке");
    }
    
    private void showHelp(Player player) {
        player.sendMessage(ChatColor.GOLD + "=== Команды терминала телепортации ===");
        player.sendMessage(ChatColor.YELLOW + "/terminal create <название>" + ChatColor.GRAY + " - Создать терминал");
        player.sendMessage(ChatColor.YELLOW + "/terminal activate <название>" + ChatColor.GRAY + " - Активировать терминал");
        player.sendMessage(ChatColor.YELLOW + "/terminal deactivate <название>" + ChatColor.GRAY + " - Деактивировать терминал");
        player.sendMessage(ChatColor.YELLOW + "/terminal list" + ChatColor.GRAY + " - Список ваших терминалов");
        player.sendMessage(ChatColor.YELLOW + "/terminal info <название>" + ChatColor.GRAY + " - Информация о терминале");
        player.sendMessage(ChatColor.YELLOW + "/terminal delete <название>" + ChatColor.GRAY + " - Удалить терминал");
        player.sendMessage(ChatColor.YELLOW + "/terminal public <название> <true/false>" + ChatColor.GRAY + " - Сделать публичным");
        player.sendMessage(ChatColor.YELLOW + "/terminal access <название> <add/remove> <игрок>" + ChatColor.GRAY + " - Управление доступом");
        
        player.sendMessage(ChatColor.AQUA + "\n=== Требования для создания ===");
        player.sendMessage(ChatColor.GRAY + "• Основание 3x3 из железных блоков");
        player.sendMessage(ChatColor.GRAY + "• Наблюдатель в центре");
        player.sendMessage(ChatColor.GRAY + "• 4 синих стекла по бокам");
        player.sendMessage(ChatColor.GRAY + "• Изученное исследование 'Ядро телепортации'");
    }
    
    private int getMaxTerminals(Player player) {
        if (player.hasPermission("uraniumcraft.terminal.unlimited")) {
            return Integer.MAX_VALUE;
        } else if (player.hasPermission("uraniumcraft.terminal.max.10")) {
            return 10;
        } else if (player.hasPermission("uraniumcraft.terminal.max.5")) {
            return 5;
        } else if (player.hasPermission("uraniumcraft.terminal.max.3")) {
            return 3;
        } else {
            return 1;
        }
    }
    
    private String formatTime(long seconds) {
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;
        
        if (hours > 0) {
            return String.format("%dч %dм", hours, minutes);
        } else if (minutes > 0) {
            return String.format("%dм %dс", minutes, secs);
        } else {
            return String.format("%dс", secs);
        }
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            completions.addAll(Arrays.asList("create", "activate", "deactivate", "list", "info", 
                "delete", "access", "public", "connect", "disconnect", "help"));
        } else if (args.length == 2 && sender instanceof Player) {
            Player player = (Player) sender;
            List<TeleportTerminal> terminals = terminalManager.getPlayerTerminals(player.getUniqueId());
            
            if (Arrays.asList("activate", "deactivate", "info", "delete", "public", "access", "connect", "disconnect")
                    .contains(args[0].toLowerCase())) {
                for (TeleportTerminal terminal : terminals) {
                    completions.add(terminal.getName());
                }
            }
        } else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("access")) {
                completions.addAll(Arrays.asList("add", "remove"));
            } else if (args[0].equalsIgnoreCase("public")) {
                completions.addAll(Arrays.asList("true", "false"));
            }
        }
        
        return completions;
    }
}
