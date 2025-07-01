package com.uraniumcraft.commands;

import com.uraniumcraft.UraniumPlugin;
import com.uraniumcraft.gui.TeleporterGUI;
import com.uraniumcraft.teleporter.Teleporter;
import com.uraniumcraft.teleporter.TeleporterManager;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class TeleporterCommand implements CommandExecutor, TabCompleter {
    
    private final UraniumPlugin plugin;
    private final TeleporterManager teleporterManager;
    
    public TeleporterCommand(UraniumPlugin plugin) {
        this.plugin = plugin;
        this.teleporterManager = plugin.getTeleporterManager();
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Эта команда доступна только игрокам!");
            return true;
        }
        
        Player player = (Player) sender;
        
        // Проверяем права администратора для всех команд кроме базовой
        if (args.length > 0 && !player.hasPermission("uraniumcraft.teleporter.admin")) {
            player.sendMessage(ChatColor.RED + "У вас нет прав администратора для использования команд телепорта!");
            player.sendMessage(ChatColor.YELLOW + "Используйте /tp для открытия меню телепортации.");
            return true;
        }
        
        if (args.length == 0) {
            if (player.hasPermission("uraniumcraft.teleporter.admin")) {
                showAdminHelp(player);
            } else {
                player.sendMessage(ChatColor.RED + "У вас нет прав для использования этой команды!");
                player.sendMessage(ChatColor.YELLOW + "Используйте /tp для телепортации.");
            }
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "create":
                return handleCreate(player, args);
            case "delete":
            case "remove":
                return handleDelete(player, args);
            case "list":
                return handleList(player, args);
            case "info":
                return handleInfo(player, args);
            case "rename":
                return handleRename(player, args);
            case "public":
                return handlePublic(player, args);
            case "private":
                return handlePrivate(player, args);
            case "authorize":
            case "auth":
                return handleAuthorize(player, args);
            case "unauthorize":
            case "unauth":
                return handleUnauthorize(player, args);
            case "charge":
                return handleCharge(player, args);
            case "help":
                return showAdminHelp(player);
            case "validate":
                return handleValidate(player);
            case "stats":
                return handleStats(player);
            case "reload":
                return handleReload(player);
            default:
                player.sendMessage(ChatColor.RED + "Неизвестная подкоманда! Используйте /teleporter help");
                return true;
        }
    }
    
    private boolean handleCreate(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Использование: /teleporter create <название>");
            return true;
        }
        
        String name = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        
        if (name.length() > 32) {
            player.sendMessage(ChatColor.RED + "Название слишком длинное! Максимум 32 символа.");
            return true;
        }
        
        // Проверяем, нет ли уже телепорта с таким названием у игрока
        if (teleporterManager.getTeleporterByName(name, player.getUniqueId()) != null) {
            player.sendMessage(ChatColor.RED + "У вас уже есть телепорт с таким названием!");
            return true;
        }
        
        Location location = player.getLocation();
        Teleporter teleporter = teleporterManager.createTeleporter(player, name, location);
        
        if (teleporter == null) {
            player.sendMessage(ChatColor.RED + "Не удалось создать телепорт! Проверьте структуру и лимиты.");
        }
        
        return true;
    }
    
    private boolean handleDelete(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Использование: /teleporter delete <название>");
            return true;
        }
        
        String name = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        Teleporter teleporter = teleporterManager.getTeleporterByName(name, player.getUniqueId());
        
        if (teleporter == null) {
            player.sendMessage(ChatColor.RED + "Телепорт с названием '" + name + "' не найден!");
            return true;
        }
        
        teleporterManager.deleteTeleporter(teleporter.getId(), player);
        return true;
    }
    
    private boolean handleList(Player player, String[] args) {
        List<Teleporter> teleporters = teleporterManager.getPlayerTeleporters(player.getUniqueId());
        
        if (teleporters.isEmpty()) {
            player.sendMessage(ChatColor.YELLOW + "У вас нет телепортов.");
            return true;
        }
        
        player.sendMessage(ChatColor.GOLD + "=== Ваши телепорты ===");
        for (Teleporter teleporter : teleporters) {
            String status = teleporter.getStatusString();
            String access = teleporter.isPublic() ? ChatColor.GREEN + "Публичный" : ChatColor.YELLOW + "Приватный";
            
            player.sendMessage(ChatColor.AQUA + teleporter.getName() + ChatColor.GRAY + " - " + 
                status + ChatColor.GRAY + " | " + access + 
                ChatColor.GRAY + " | Энергия: " + ChatColor.WHITE + teleporter.getEnergyLevel() + "/" + teleporter.getMaxEnergy());
        }
        
        return true;
    }
    
    private boolean handleInfo(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Использование: /teleporter info <название>");
            return true;
        }
        
        String name = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        Teleporter teleporter = teleporterManager.getTeleporterByName(name, player.getUniqueId());
        
        if (teleporter == null) {
            player.sendMessage(ChatColor.RED + "Телепорт с названием '" + name + "' не найден!");
            return true;
        }
        
        player.sendMessage(ChatColor.GOLD + "=== Информация о телепорте ===");
        player.sendMessage(ChatColor.YELLOW + "Название: " + ChatColor.WHITE + teleporter.getName());
        player.sendMessage(ChatColor.YELLOW + "Статус: " + teleporter.getStatusString());
        player.sendMessage(ChatColor.YELLOW + "Доступ: " + (teleporter.isPublic() ? 
            ChatColor.GREEN + "Публичный" : ChatColor.YELLOW + "Приватный"));
        player.sendMessage(ChatColor.YELLOW + "Энергия: " + ChatColor.WHITE + 
            teleporter.getEnergyLevel() + "/" + teleporter.getMaxEnergy() + 
            " (" + String.format("%.1f", teleporter.getEnergyPercentage()) + "%)");
        player.sendMessage(ChatColor.YELLOW + "Координаты: " + ChatColor.WHITE + 
            teleporter.getCoreLocation().getBlockX() + ", " +
            teleporter.getCoreLocation().getBlockY() + ", " +
            teleporter.getCoreLocation().getBlockZ());
        player.sendMessage(ChatColor.YELLOW + "Телепортаций: " + ChatColor.WHITE + teleporter.getTotalTeleportations());
        player.sendMessage(ChatColor.YELLOW + "Авторизованных игроков: " + ChatColor.WHITE + teleporter.getAuthorizedPlayers().size());
        
        return true;
    }
    
    private boolean handleRename(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage(ChatColor.RED + "Использование: /teleporter rename <старое_название> <новое_название>");
            return true;
        }
        
        String oldName = args[1];
        String newName = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
        
        if (newName.length() > 32) {
            player.sendMessage(ChatColor.RED + "Название слишком длинное! Максимум 32 символа.");
            return true;
        }
        
        Teleporter teleporter = teleporterManager.getTeleporterByName(oldName, player.getUniqueId());
        if (teleporter == null) {
            player.sendMessage(ChatColor.RED + "Телепорт с названием '" + oldName + "' не найден!");
            return true;
        }
        
        if (teleporterManager.getTeleporterByName(newName, player.getUniqueId()) != null) {
            player.sendMessage(ChatColor.RED + "У вас уже есть телепорт с названием '" + newName + "'!");
            return true;
        }
        
        teleporter.setName(newName);
        player.sendMessage(ChatColor.GREEN + "Телепорт переименован в '" + newName + "'!");
        
        return true;
    }
    
    private boolean handlePublic(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Использование: /teleporter public <название>");
            return true;
        }
        
        String name = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        Teleporter teleporter = teleporterManager.getTeleporterByName(name, player.getUniqueId());
        
        if (teleporter == null) {
            player.sendMessage(ChatColor.RED + "Телепорт с названием '" + name + "' не найден!");
            return true;
        }
        
        teleporter.setPublic(true);
        player.sendMessage(ChatColor.GREEN + "Телепорт '" + name + "' теперь публичный!");
        
        return true;
    }
    
    private boolean handlePrivate(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Использование: /teleporter private <название>");
            return true;
        }
        
        String name = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        Teleporter teleporter = teleporterManager.getTeleporterByName(name, player.getUniqueId());
        
        if (teleporter == null) {
            player.sendMessage(ChatColor.RED + "Телепорт с названием '" + name + "' не найден!");
            return true;
        }
        
        teleporter.setPublic(false);
        player.sendMessage(ChatColor.GREEN + "Телепорт '" + name + "' теперь приватный!");
        
        return true;
    }
    
    private boolean handleAuthorize(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage(ChatColor.RED + "Использование: /teleporter authorize <название> <игрок>");
            return true;
        }
        
        String name = args[1];
        String playerName = args[2];
        
        Teleporter teleporter = teleporterManager.getTeleporterByName(name, player.getUniqueId());
        if (teleporter == null) {
            player.sendMessage(ChatColor.RED + "Телепорт с названием '" + name + "' не найден!");
            return true;
        }
        
        Player target = plugin.getServer().getPlayer(playerName);
        if (target == null) {
            player.sendMessage(ChatColor.RED + "Игрок '" + playerName + "' не найден!");
            return true;
        }
        
        teleporter.addAuthorizedPlayer(target.getUniqueId());
        player.sendMessage(ChatColor.GREEN + "Игрок " + target.getName() + " получил доступ к телепорту '" + name + "'!");
        target.sendMessage(ChatColor.GREEN + "Вам предоставлен доступ к телепорту '" + name + "' игрока " + player.getName() + "!");
        
        return true;
    }
    
    private boolean handleUnauthorize(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage(ChatColor.RED + "Использование: /teleporter unauthorize <название> <игрок>");
            return true;
        }
        
        String name = args[1];
        String playerName = args[2];
        
        Teleporter teleporter = teleporterManager.getTeleporterByName(name, player.getUniqueId());
        if (teleporter == null) {
            player.sendMessage(ChatColor.RED + "Телепорт с названием '" + name + "' не найден!");
            return true;
        }
        
        Player target = plugin.getServer().getPlayer(playerName);
        if (target == null) {
            player.sendMessage(ChatColor.RED + "Игрок '" + playerName + "' не найден!");
            return true;
        }
        
        teleporter.removeAuthorizedPlayer(target.getUniqueId());
        player.sendMessage(ChatColor.GREEN + "У игрока " + target.getName() + " отозван доступ к телепорту '" + name + "'!");
        target.sendMessage(ChatColor.YELLOW + "У вас отозван доступ к телепорту '" + name + "' игрока " + player.getName() + "!");
        
        return true;
    }
    
    private boolean handleCharge(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Использование: /teleporter charge <название> [количество]");
            return true;
        }
        
        String name = args[1];
        int amount = 100;
        
        if (args.length > 2) {
            try {
                amount = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                player.sendMessage(ChatColor.RED + "Неверное количество энергии!");
                return true;
            }
        }
        
        Teleporter teleporter = teleporterManager.getTeleporterByName(name, player.getUniqueId());
        if (teleporter == null) {
            player.sendMessage(ChatColor.RED + "Телепорт с названием '" + name + "' не найден!");
            return true;
        }
        
        teleporter.addEnergy(amount);
        player.sendMessage(ChatColor.GREEN + "Телепорт '" + name + "' заряжен на " + amount + " энергии!");
        
        return true;
    }
    
    private boolean handleValidate(Player player) {
        teleporterManager.validateAllTeleporters();
        player.sendMessage(ChatColor.GREEN + "Проверка всех телепортов завершена!");
        return true;
    }
    
    private boolean handleStats(Player player) {
        player.sendMessage(ChatColor.GOLD + "=== Статистика телепортов ===");
        player.sendMessage(ChatColor.YELLOW + "Всего телепортов: " + ChatColor.WHITE + teleporterManager.getTotalTeleporters());
        player.sendMessage(ChatColor.YELLOW + "Активных: " + ChatColor.WHITE + teleporterManager.getActiveTeleporters());
        player.sendMessage(ChatColor.YELLOW + "Публичных: " + ChatColor.WHITE + teleporterManager.getPublicTeleportersCount());
        return true;
    }
    
    private boolean handleReload(Player player) {
        teleporterManager.saveTeleporters();
        teleporterManager.loadTeleporters();
        player.sendMessage(ChatColor.GREEN + "Данные телепортов перезагружены!");
        return true;
    }
    
    private boolean showAdminHelp(Player player) {
        player.sendMessage(ChatColor.GOLD + "=== Админ-команды телепортов ===");
        player.sendMessage(ChatColor.YELLOW + "/teleporter create <название>" + ChatColor.WHITE + " - Создать телепорт");
        player.sendMessage(ChatColor.YELLOW + "/teleporter delete <название>" + ChatColor.WHITE + " - Удалить телепорт");
        player.sendMessage(ChatColor.YELLOW + "/teleporter list" + ChatColor.WHITE + " - Список ваших телепортов");
        player.sendMessage(ChatColor.YELLOW + "/teleporter info <название>" + ChatColor.WHITE + " - Информация о телепорте");
        player.sendMessage(ChatColor.YELLOW + "/teleporter rename <старое> <новое>" + ChatColor.WHITE + " - Переименовать");
        player.sendMessage(ChatColor.YELLOW + "/teleporter public/private <название>" + ChatColor.WHITE + " - Изменить доступ");
        player.sendMessage(ChatColor.YELLOW + "/teleporter auth <название> <игрок>" + ChatColor.WHITE + " - Дать доступ");
        player.sendMessage(ChatColor.YELLOW + "/teleporter charge <название> [энергия]" + ChatColor.WHITE + " - Зарядить");
        player.sendMessage(ChatColor.YELLOW + "/teleporter validate" + ChatColor.WHITE + " - Проверить все телепорты");
        player.sendMessage(ChatColor.YELLOW + "/teleporter stats" + ChatColor.WHITE + " - Статистика");
        player.sendMessage(ChatColor.YELLOW + "/teleporter reload" + ChatColor.WHITE + " - Перезагрузить данные");
        
        player.sendMessage(ChatColor.GOLD + "\n=== Структура телепорта ===");
        player.sendMessage(ChatColor.AQUA + "1. Основание 3x3 из кварцевых блоков");
        player.sendMessage(ChatColor.AQUA + "2. Маяк в центре основания");
        player.sendMessage(ChatColor.AQUA + "3. 4 столба из кварцевых блоков по углам (высота 3)");
        player.sendMessage(ChatColor.AQUA + "4. Рама из стекла на высоте 3");
        player.sendMessage(ChatColor.AQUA + "5. 4 морских фонаря на расстоянии 2 блоков от центра");
        
        return true;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player)) {
            return new ArrayList<>();
        }
        
        Player player = (Player) sender;
        
        if (!player.hasPermission("uraniumcraft.teleporter.admin")) {
            return new ArrayList<>();
        }
        
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            List<String> subCommands = Arrays.asList(
                "create", "delete", "list", "info", "rename", "public", "private",
                "authorize", "unauthorize", "charge", "help", "validate", "stats", "reload"
            );
            
            return subCommands.stream()
                .filter(cmd -> cmd.toLowerCase().startsWith(args[0].toLowerCase()))
                .collect(Collectors.toList());
        }
        
        if (args.length == 2) {
            String subCommand = args[0].toLowerCase();
            
            switch (subCommand) {
                case "delete":
                case "info":
                case "public":
                case "private":
                case "charge":
                    return teleporterManager.getPlayerTeleporters(player.getUniqueId()).stream()
                        .map(Teleporter::getName)
                        .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            }
        }
        
        if (args.length == 3) {
            String subCommand = args[0].toLowerCase();
            
            if (subCommand.equals("authorize") || subCommand.equals("unauthorize")) {
                return plugin.getServer().getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[2].toLowerCase()))
                    .collect(Collectors.toList());
            }
        }
        
        return completions;
    }
}
