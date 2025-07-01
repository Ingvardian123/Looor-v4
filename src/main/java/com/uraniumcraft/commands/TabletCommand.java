package com.uraniumcraft.commands;

import com.uraniumcraft.UraniumPlugin;
import com.uraniumcraft.items.UraniumTablet;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class TabletCommand implements CommandExecutor, TabCompleter {
    
    private final UraniumPlugin plugin;
    
    public TabletCommand(UraniumPlugin plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            showHelp(sender);
            return true;
        }
        
        switch (args[0].toLowerCase()) {
            case "give":
                return handleGiveCommand(sender, args);
            case "info":
                return handleInfoCommand(sender, args);
            case "energy":
                return handleEnergyCommand(sender, args);
            case "module":
                return handleModuleCommand(sender, args);
            case "upgrade":
                return handleUpgradeCommand(sender, args);
            case "reset":
                return handleResetCommand(sender, args);
            case "list":
                return handleListCommand(sender);
            case "help":
                showHelp(sender);
                return true;
            default:
                sender.sendMessage(Component.text("❌ Неизвестная команда. Используйте /tablet help", 
                    NamedTextColor.RED));
                return true;
        }
    }
    
    private boolean handleGiveCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("uraniumcraft.tablet.give")) {
            sender.sendMessage(Component.text("❌ У вас нет прав для выполнения этой команды!", 
                NamedTextColor.RED));
            return true;
        }
        
        if (args.length < 3) {
            sender.sendMessage(Component.text("❌ Использование: /tablet give <игрок> <тип> [энергия]", 
                NamedTextColor.RED));
            sender.sendMessage(Component.text("Типы: standard, advanced, quantum, reality", 
                NamedTextColor.GRAY));
            return true;
        }
        
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(Component.text("❌ Игрок не найден!", NamedTextColor.RED));
            return true;
        }
        
        UraniumTablet.TabletType type;
        try {
            type = UraniumTablet.TabletType.valueOf(args[2].toUpperCase());
        } catch (IllegalArgumentException e) {
            sender.sendMessage(Component.text("❌ Неверный тип планшета! Доступные: standard, advanced, quantum, reality", 
                NamedTextColor.RED));
            return true;
        }
        
        int energy = type.getMaxEnergy();
        if (args.length > 3) {
            try {
                energy = Integer.parseInt(args[3]);
                energy = Math.max(0, Math.min(energy, type.getMaxEnergy()));
            } catch (NumberFormatException e) {
                sender.sendMessage(Component.text("❌ Неверное значение энергии!", NamedTextColor.RED));
                return true;
            }
        }
        
        ItemStack tablet = UraniumTablet.createTablet(plugin, type, energy, new HashSet<>());
        target.getInventory().addItem(tablet);
        
        sender.sendMessage(Component.text("✅ Планшет '" + type.getDisplayName() + "' выдан игроку " + target.getName(), 
            NamedTextColor.GREEN));
        target.sendMessage(Component.text("📱 Вы получили " + type.getDisplayName() + "!", 
            type.getColor(), TextDecoration.BOLD));
        
        return true;
    }
    
    private boolean handleInfoCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("❌ Эта команда доступна только игрокам!", NamedTextColor.RED));
            return true;
        }
        
        ItemStack item = player.getInventory().getItemInMainHand();
        if (!UraniumTablet.isUraniumTablet(item, plugin)) {
            sender.sendMessage(Component.text("❌ Возьмите планшет в руку!", NamedTextColor.RED));
            return true;
        }
        
        UraniumTablet.TabletType type = UraniumTablet.getTabletType(item, plugin);
        int energy = UraniumTablet.getTabletEnergy(item, plugin);
        Set<UraniumTablet.TabletModule> modules = UraniumTablet.getTabletModules(item, plugin);
        
        sender.sendMessage(Component.text("📱 ИНФОРМАЦИЯ О ПЛАНШЕТЕ", NamedTextColor.AQUA, TextDecoration.BOLD));
        sender.sendMessage(Component.text("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", NamedTextColor.DARK_GRAY));
        sender.sendMessage(Component.text("Тип: " + (type != null ? type.getDisplayName() : "Неизвестно"), 
            type != null ? type.getColor() : NamedTextColor.GRAY));
        sender.sendMessage(Component.text("Энергия: " + energy + " / " + (type != null ? type.getMaxEnergy() : "?"), 
            getEnergyColor(energy, type != null ? type.getMaxEnergy() : 1000)));
        sender.sendMessage(Component.text("Модулей установлено: " + modules.size(), NamedTextColor.YELLOW));
        
        if (!modules.isEmpty()) {
            sender.sendMessage(Component.text("Установленные модули:", NamedTextColor.GOLD));
            for (UraniumTablet.TabletModule module : modules) {
                sender.sendMessage(Component.text("  • " + module.getName(), NamedTextColor.WHITE));
            }
        }
        
        int regenRate = type != null ? UraniumTablet.getEnergyRegenRate(type, modules) : 0;
        sender.sendMessage(Component.text("Регенерация энергии: +" + regenRate + "/30сек", NamedTextColor.GREEN));
        sender.sendMessage(Component.text("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", NamedTextColor.DARK_GRAY));
        
        return true;
    }
    
    private boolean handleEnergyCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("uraniumcraft.tablet.energy")) {
            sender.sendMessage(Component.text("❌ У вас нет прав для выполнения этой команды!", 
                NamedTextColor.RED));
            return true;
        }
        
        if (args.length < 2) {
            sender.sendMessage(Component.text("❌ Использование: /tablet energy <set|add|remove> [игрок] <количество>", 
                NamedTextColor.RED));
            return true;
        }
        
        String action = args[1].toLowerCase();
        Player target;
        int amount;
        
        if (args.length == 3) {
            // /tablet energy <action> <amount>
            if (!(sender instanceof Player)) {
                sender.sendMessage(Component.text("❌ Укажите игрока!", NamedTextColor.RED));
                return true;
            }
            target = (Player) sender;
            try {
                amount = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                sender.sendMessage(Component.text("❌ Неверное количество!", NamedTextColor.RED));
                return true;
            }
        } else if (args.length == 4) {
            // /tablet energy <action> <player> <amount>
            target = Bukkit.getPlayer(args[2]);
            if (target == null) {
                sender.sendMessage(Component.text("❌ Игрок не найден!", NamedTextColor.RED));
                return true;
            }
            try {
                amount = Integer.parseInt(args[3]);
            } catch (NumberFormatException e) {
                sender.sendMessage(Component.text("❌ Неверное количество!", NamedTextColor.RED));
                return true;
            }
        } else {
            sender.sendMessage(Component.text("❌ Неверное количество аргументов!", NamedTextColor.RED));
            return true;
        }
        
        ItemStack tablet = target.getInventory().getItemInMainHand();
        if (!UraniumTablet.isUraniumTablet(tablet, plugin)) {
            sender.sendMessage(Component.text("❌ У игрока нет планшета в руке!", NamedTextColor.RED));
            return true;
        }
        
        UraniumTablet.TabletType type = UraniumTablet.getTabletType(tablet, plugin);
        int currentEnergy = UraniumTablet.getTabletEnergy(tablet, plugin);
        int newEnergy = currentEnergy;
        
        switch (action) {
            case "set":
                newEnergy = Math.max(0, Math.min(amount, type != null ? type.getMaxEnergy() : 1000));
                break;
            case "add":
                newEnergy = Math.min(currentEnergy + amount, type != null ? type.getMaxEnergy() : 1000);
                break;
            case "remove":
                newEnergy = Math.max(0, currentEnergy - amount);
                break;
            default:
                sender.sendMessage(Component.text("❌ Неверное действие! Используйте: set, add, remove", 
                    NamedTextColor.RED));
                return true;
        }
        
        UraniumTablet.setTabletEnergy(tablet, plugin, newEnergy);
        
        sender.sendMessage(Component.text("✅ Энергия планшета изменена: " + currentEnergy + " → " + newEnergy, 
            NamedTextColor.GREEN));
        
        if (!sender.equals(target)) {
            target.sendMessage(Component.text("⚡ Энергия вашего планшета изменена: " + newEnergy, 
                NamedTextColor.YELLOW));
        }
        
        return true;
    }
    
    private boolean handleModuleCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("uraniumcraft.tablet.module")) {
            sender.sendMessage(Component.text("❌ У вас нет прав для выполнения этой команды!", 
                NamedTextColor.RED));
            return true;
        }
        
        if (args.length < 3) {
            sender.sendMessage(Component.text("❌ Использование: /tablet module <add|remove|list> [игрок] [модуль]", 
                NamedTextColor.RED));
            return true;
        }
        
        String action = args[1].toLowerCase();
        
        if (action.equals("list")) {
            showModulesList(sender);
            return true;
        }
        
        Player target;
        if (args.length >= 4) {
            target = Bukkit.getPlayer(args[2]);
            if (target == null) {
                sender.sendMessage(Component.text("❌ Игрок не найден!", NamedTextColor.RED));
                return true;
            }
        } else {
            if (!(sender instanceof Player)) {
                sender.sendMessage(Component.text("❌ Укажите игрока!", NamedTextColor.RED));
                return true;
            }
            target = (Player) sender;
        }
        
        ItemStack tablet = target.getInventory().getItemInMainHand();
        if (!UraniumTablet.isUraniumTablet(tablet, plugin)) {
            sender.sendMessage(Component.text("❌ У игрока нет планшета в руке!", NamedTextColor.RED));
            return true;
        }
        
        if (args.length < (args.length >= 4 ? 4 : 3)) {
            sender.sendMessage(Component.text("❌ Укажите модуль!", NamedTextColor.RED));
            return true;
        }
        
        String moduleName = args[args.length >= 4 ? 3 : 2].toUpperCase();
        UraniumTablet.TabletModule module;
        
        try {
            module = UraniumTablet.TabletModule.valueOf(moduleName);
        } catch (IllegalArgumentException e) {
            sender.sendMessage(Component.text("❌ Неверный модуль! Используйте /tablet module list для просмотра", 
                NamedTextColor.RED));
            return true;
        }
        
        switch (action) {
            case "add":
                if (UraniumTablet.hasTabletModule(tablet, plugin, module)) {
                    sender.sendMessage(Component.text("❌ Модуль уже установлен!", NamedTextColor.RED));
                } else {
                    UraniumTablet.addTabletModule(tablet, plugin, module);
                    sender.sendMessage(Component.text("✅ Модуль '" + module.getName() + "' добавлен!", 
                        NamedTextColor.GREEN));
                    target.sendMessage(Component.text("🔧 Установлен модуль: " + module.getName(), 
                        NamedTextColor.AQUA));
                }
                break;
            case "remove":
                if (!UraniumTablet.hasTabletModule(tablet, plugin, module)) {
                    sender.sendMessage(Component.text("❌ Модуль не установлен!", NamedTextColor.RED));
                } else {
                    // Логика удаления модуля (требует реализации)
                    sender.sendMessage(Component.text("✅ Модуль '" + module.getName() + "' удален!", 
                        NamedTextColor.GREEN));
                    target.sendMessage(Component.text("🔧 Удален модуль: " + module.getName(), 
                        NamedTextColor.YELLOW));
                }
                break;
            default:
                sender.sendMessage(Component.text("❌ Неверное действие! Используйте: add, remove, list", 
                    NamedTextColor.RED));
                break;
        }
        
        return true;
    }
    
    private boolean handleUpgradeCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("uraniumcraft.tablet.upgrade")) {
            sender.sendMessage(Component.text("❌ У вас нет прав для выполнения этой команды!", 
                NamedTextColor.RED));
            return true;
        }
        
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("❌ Эта команда доступна только игрокам!", NamedTextColor.RED));
            return true;
        }
        
        ItemStack tablet = player.getInventory().getItemInMainHand();
        if (!UraniumTablet.isUraniumTablet(tablet, plugin)) {
            sender.sendMessage(Component.text("❌ Возьмите планшет в руку!", NamedTextColor.RED));
            return true;
        }
        
        UraniumTablet.TabletType currentType = UraniumTablet.getTabletType(tablet, plugin);
        if (currentType == null) {
            sender.sendMessage(Component.text("❌ Ошибка определения типа планшета!", NamedTextColor.RED));
            return true;
        }
        
        if (currentType == UraniumTablet.TabletType.REALITY) {
            sender.sendMessage(Component.text("❌ Планшет уже имеет максимальный уровень!", NamedTextColor.RED));
            return true;
        }
        
        UraniumTablet.TabletType nextType = UraniumTablet.TabletType.values()[currentType.ordinal() + 1];
        int currentEnergy = UraniumTablet.getTabletEnergy(tablet, plugin);
        Set<UraniumTablet.TabletModule> modules = UraniumTablet.getTabletModules(tablet, plugin);
        
        // Создаем улучшенный планшет
        ItemStack upgradedTablet = UraniumTablet.createTablet(plugin, nextType, currentEnergy, modules);
        player.getInventory().setItemInMainHand(upgradedTablet);
        
        sender.sendMessage(Component.text("✅ Планшет улучшен до уровня: " + nextType.getDisplayName(), 
            nextType.getColor(), TextDecoration.BOLD));
        player.playSound(player.getLocation(), org.bukkit.Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.2f);
        
        return true;
    }
    
    private boolean handleResetCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("uraniumcraft.tablet.reset")) {
            sender.sendMessage(Component.text("❌ У вас нет прав для выполнения этой команды!", 
                NamedTextColor.RED));
            return true;
        }
        
        Player target;
        if (args.length >= 2) {
            target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                sender.sendMessage(Component.text("❌ Игрок не найден!", NamedTextColor.RED));
                return true;
            }
        } else {
            if (!(sender instanceof Player)) {
                sender.sendMessage(Component.text("❌ Укажите игрока!", NamedTextColor.RED));
                return true;
            }
            target = (Player) sender;
        }
        
        ItemStack tablet = target.getInventory().getItemInMainHand();
        if (!UraniumTablet.isUraniumTablet(tablet, plugin)) {
            sender.sendMessage(Component.text("❌ У игрока нет планшета в руке!", NamedTextColor.RED));
            return true;
        }
        
        UraniumTablet.TabletType type = UraniumTablet.getTabletType(tablet, plugin);
        if (type == null) {
            sender.sendMessage(Component.text("❌ Ошибка определения типа планшета!", NamedTextColor.RED));
            return true;
        }
        
        // Сбрасываем планшет к заводским настройкам
        ItemStack resetTablet = UraniumTablet.createTablet(plugin, type);
        target.getInventory().setItemInMainHand(resetTablet);
        
        sender.sendMessage(Component.text("✅ Планшет сброшен к заводским настройкам!", NamedTextColor.GREEN));
        target.sendMessage(Component.text("🔄 Ваш планшет был сброшен к заводским настройкам!", 
            NamedTextColor.YELLOW));
        
        return true;
    }
    
    private boolean handleListCommand(CommandSender sender) {
        sender.sendMessage(Component.text("📱 ТИПЫ ПЛАНШЕТОВ", NamedTextColor.AQUA, TextDecoration.BOLD));
        sender.sendMessage(Component.text("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", NamedTextColor.DARK_GRAY));
        
        for (UraniumTablet.TabletType type : UraniumTablet.TabletType.values()) {
            sender.sendMessage(Component.text()
                .append(Component.text("• ", NamedTextColor.GRAY))
                .append(Component.text(type.name().toLowerCase(), type.getColor(), TextDecoration.BOLD))
                .append(Component.text(" - " + type.getDisplayName(), NamedTextColor.WHITE))
                .append(Component.text(" (⚡" + type.getMaxEnergy() + ")", NamedTextColor.YELLOW))
                .build());
        }
        
        sender.sendMessage(Component.text("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", NamedTextColor.DARK_GRAY));
        return true;
    }
    
    private void showModulesList(CommandSender sender) {
        sender.sendMessage(Component.text("🔧 ДОСТУПНЫЕ МОДУЛИ", NamedTextColor.GOLD, TextDecoration.BOLD));
        sender.sendMessage(Component.text("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", NamedTextColor.DARK_GRAY));
        
        for (UraniumTablet.TabletModule module : UraniumTablet.TabletModule.values()) {
            sender.sendMessage(Component.text()
                .append(Component.text("• ", NamedTextColor.GRAY))
                .append(Component.text(module.name().toLowerCase(), NamedTextColor.AQUA, TextDecoration.BOLD))
                .append(Component.text(" - " + module.getName(), NamedTextColor.WHITE))
                .build());
            sender.sendMessage(Component.text("  " + module.getDescription() + " (⚡" + module.getEnergyCost() + ")", 
                NamedTextColor.GRAY));
        }
        
        sender.sendMessage(Component.text("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", NamedTextColor.DARK_GRAY));
    }
    
    private void showHelp(CommandSender sender) {
        sender.sendMessage(Component.text("📱 КОМАНДЫ ПЛАНШЕТА", NamedTextColor.AQUA, TextDecoration.BOLD));
        sender.sendMessage(Component.text("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", NamedTextColor.DARK_GRAY));
        sender.sendMessage(Component.text("/tablet give <игрок> <тип> [энергия] - Выдать планшет", NamedTextColor.WHITE));
        sender.sendMessage(Component.text("/tablet info - Информация о планшете в руке", NamedTextColor.WHITE));
        sender.sendMessage(Component.text("/tablet energy <set|add|remove> [игрок] <кол-во> - Управление энергией", NamedTextColor.WHITE));
        sender.sendMessage(Component.text("/tablet module <add|remove|list> [игрок] [модуль] - Управление модулями", NamedTextColor.WHITE));
        sender.sendMessage(Component.text("/tablet upgrade - Улучшить планшет", NamedTextColor.WHITE));
        sender.sendMessage(Component.text("/tablet reset [игрок] - Сбросить настройки", NamedTextColor.WHITE));
        sender.sendMessage(Component.text("/tablet list - Список типов планшетов", NamedTextColor.WHITE));
        sender.sendMessage(Component.text("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", NamedTextColor.DARK_GRAY));
    }
    
    private NamedTextColor getEnergyColor(int energy, int maxEnergy) {
        double percent = (double) energy / maxEnergy * 100;
        if (percent > 75) return NamedTextColor.GREEN;
        if (percent > 50) return NamedTextColor.YELLOW;
        if (percent > 25) return NamedTextColor.GOLD;
        return NamedTextColor.RED;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            completions.addAll(Arrays.asList("give", "info", "energy", "module", "upgrade", "reset", "list", "help"));
        } else if (args.length == 2) {
            switch (args[0].toLowerCase()) {
                case "give":
                    completions.addAll(getOnlinePlayerNames());
                    break;
                case "energy":
                    completions.addAll(Arrays.asList("set", "add", "remove"));
                    break;
                case "module":
                    completions.addAll(Arrays.asList("add", "remove", "list"));
                    break;
                case "reset":
                    completions.addAll(getOnlinePlayerNames());
                    break;
            }
        } else if (args.length == 3) {
            switch (args[0].toLowerCase()) {
                case "give":
                    completions.addAll(Arrays.asList("standard", "advanced", "quantum", "reality"));
                    break;
                case "energy":
                    if (args[1].equals("set") || args[1].equals("add") || args[1].equals("remove")) {
                        completions.addAll(getOnlinePlayerNames());
                    }
                    break;
                case "module":
                    if (args[1].equals("add") || args[1].equals("remove")) {
                        completions.addAll(getOnlinePlayerNames());
                    }
                    break;
            }
        } else if (args.length == 4) {
            if (args[0].equals("module") && (args[1].equals("add") || args[1].equals("remove"))) {
                for (UraniumTablet.TabletModule module : UraniumTablet.TabletModule.values()) {
                    completions.add(module.name().toLowerCase());
                }
            }
        }
        
        return completions.stream()
            .filter(s -> s.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
            .sorted()
            .toList();
    }
    
    private List<String> getOnlinePlayerNames() {
        return Bukkit.getOnlinePlayers().stream()
            .map(Player::getName)
            .toList();
    }
}
