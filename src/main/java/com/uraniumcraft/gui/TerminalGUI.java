package com.uraniumcraft.gui;

import com.uraniumcraft.UraniumPlugin;
import com.uraniumcraft.teleporter.TeleportTerminal;
import com.uraniumcraft.teleporter.Teleporter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class TerminalGUI {
    
    public static void openTerminalMainMenu(Player player, TeleportTerminal terminal) {
        Inventory gui = Bukkit.createInventory(null, 54, ChatColor.DARK_BLUE + "Терминал: " + terminal.getName());
        
        // Информация о терминале
        ItemStack info = new ItemStack(Material.OBSERVER);
        ItemMeta infoMeta = info.getItemMeta();
        infoMeta.setDisplayName(ChatColor.AQUA + "Информация о терминале");
        List<String> infoLore = new ArrayList<>();
        infoLore.add(ChatColor.GRAY + "Название: " + ChatColor.WHITE + terminal.getName());
        infoLore.add(ChatColor.GRAY + "Статус: " + terminal.getStatusString());
        infoLore.add(ChatColor.GRAY + "Энергия: " + ChatColor.YELLOW + terminal.getEnergyLevel() + "/" + terminal.getMaxEnergy());
        infoLore.add(ChatColor.GRAY + "Заряд: " + ChatColor.GREEN + String.format("%.1f%%", terminal.getEnergyPercentage()));
        infoLore.add(ChatColor.GRAY + "Телепортаций: " + ChatColor.WHITE + terminal.getTotalTeleportations());
        infoLore.add("");
        if (terminal.isPublic()) {
            infoLore.add(ChatColor.GREEN + "✓ Публичный доступ");
        } else {
            infoLore.add(ChatColor.RED + "✗ Приватный доступ");
        }
        infoMeta.setLore(infoLore);
        info.setItemMeta(infoMeta);
        gui.setItem(4, info);
        
        // Доступные телепорты
        List<Teleporter> connectedTeleporters = terminal.getConnectedTeleporters();
        
        if (connectedTeleporters.isEmpty()) {
            ItemStack noTeleporters = new ItemStack(Material.BARRIER);
            ItemMeta noTeleportersMeta = noTeleporters.getItemMeta();
            noTeleportersMeta.setDisplayName(ChatColor.RED + "Нет доступных телепортов");
            List<String> noTeleportersLore = new ArrayList<>();
            noTeleportersLore.add(ChatColor.GRAY + "К этому терминалу не подключены");
            noTeleportersLore.add(ChatColor.GRAY + "телепорты для назначения.");
            noTeleportersLore.add("");
            noTeleportersLore.add(ChatColor.YELLOW + "Обратитесь к владельцу терминала");
            noTeleportersLore.add(ChatColor.YELLOW + "для настройки подключений.");
            noTeleportersMeta.setLore(noTeleportersLore);
            noTeleporters.setItemMeta(noTeleportersMeta);
            gui.setItem(22, noTeleporters);
        } else {
            int slot = 18;
            for (Teleporter teleporter : connectedTeleporters) {
                if (slot >= 27) break; // Ограничиваем количество отображаемых телепортов
                
                ItemStack teleporterItem = new ItemStack(Material.ENDER_PEARL);
                ItemMeta teleporterMeta = teleporterItem.getItemMeta();
                teleporterMeta.setDisplayName(ChatColor.GREEN + teleporter.getName());
                
                List<String> teleporterLore = new ArrayList<>();
                teleporterLore.add(ChatColor.GRAY + "Владелец: " + ChatColor.WHITE + 
                    Bukkit.getOfflinePlayer(teleporter.getOwner()).getName());
                teleporterLore.add(ChatColor.GRAY + "Мир: " + ChatColor.WHITE + 
                    teleporter.getCoreLocation().getWorld().getName());
                
                double distance = terminal.getTerminalLocation().distance(teleporter.getCoreLocation());
                teleporterLore.add(ChatColor.GRAY + "Расстояние: " + ChatColor.WHITE + 
                    String.format("%.0f блоков", distance));
                
                int energyCost = (int) Math.max(150, distance / 8);
                teleporterLore.add(ChatColor.GRAY + "Стоимость: " + ChatColor.YELLOW + energyCost + " энергии");
                
                teleporterLore.add("");
                teleporterLore.add(ChatColor.GRAY + "Статус: " + teleporter.getStatusString());
                
                if (teleporter.canTeleport() && terminal.getEnergyLevel() >= energyCost) {
                    teleporterLore.add("");
                    teleporterLore.add(ChatColor.GREEN + "▶ Нажмите для телепортации");
                } else if (!teleporter.canTeleport()) {
                    teleporterLore.add("");
                    teleporterLore.add(ChatColor.RED + "✗ Телепорт недоступен");
                } else {
                    teleporterLore.add("");
                    teleporterLore.add(ChatColor.RED + "✗ Недостаточно энергии");
                }
                
                teleporterMeta.setLore(teleporterLore);
                teleporterItem.setItemMeta(teleporterMeta);
                gui.setItem(slot, teleporterItem);
                slot++;
            }
        }
        
        // Управление (только для владельца)
        if (terminal.getOwner().equals(player.getUniqueId()) || player.hasPermission("uraniumcraft.teleporter.admin")) {
            ItemStack manage = new ItemStack(Material.REDSTONE);
            ItemMeta manageMeta = manage.getItemMeta();
            manageMeta.setDisplayName(ChatColor.GOLD + "Управление терминалом");
            List<String> manageLore = new ArrayList<>();
            manageLore.add(ChatColor.GRAY + "Настройки доступа, подключения");
            manageLore.add(ChatColor.GRAY + "и другие параметры терминала.");
            manageLore.add("");
            manageLore.add(ChatColor.YELLOW + "▶ Нажмите для открытия");
            manageMeta.setLore(manageLore);
            manage.setItemMeta(manageMeta);
            gui.setItem(49, manage);
        }
        
        // Закрыть
        ItemStack close = new ItemStack(Material.BARRIER);
        ItemMeta closeMeta = close.getItemMeta();
        closeMeta.setDisplayName(ChatColor.RED + "Закрыть");
        close.setItemMeta(closeMeta);
        gui.setItem(53, close);
        
        player.openInventory(gui);
    }
    
    public static void openTerminalManagement(Player player, TeleportTerminal terminal) {
        Inventory gui = Bukkit.createInventory(null, 45, ChatColor.DARK_RED + "Управление: " + terminal.getName());
        
        // Активация/деактивация
        ItemStack toggle = new ItemStack(terminal.isActive() ? Material.LIME_DYE : Material.GRAY_DYE);
        ItemMeta toggleMeta = toggle.getItemMeta();
        toggleMeta.setDisplayName(terminal.isActive() ? 
            ChatColor.GREEN + "Деактивировать терминал" : 
            ChatColor.RED + "Активировать терминал");
        List<String> toggleLore = new ArrayList<>();
        toggleLore.add(ChatColor.GRAY + "Текущий статус: " + terminal.getStatusString());
        toggleLore.add("");
        if (terminal.isActive()) {
            toggleLore.add(ChatColor.YELLOW + "▶ Нажмите для деактивации");
        } else {
            toggleLore.add(ChatColor.YELLOW + "▶ Нажмите для активации");
        }
        toggleMeta.setLore(toggleLore);
        toggle.setItemMeta(toggleMeta);
        gui.setItem(10, toggle);
        
        // Публичный/приватный доступ
        ItemStack access = new ItemStack(terminal.isPublic() ? Material.EMERALD : Material.REDSTONE);
        ItemMeta accessMeta = access.getItemMeta();
        accessMeta.setDisplayName(terminal.isPublic() ? 
            ChatColor.GREEN + "Сделать приватным" : 
            ChatColor.RED + "Сделать публичным");
        List<String> accessLore = new ArrayList<>();
        accessLore.add(ChatColor.GRAY + "Текущий режим: " + 
            (terminal.isPublic() ? ChatColor.GREEN + "Публичный" : ChatColor.RED + "Приватный"));
        accessLore.add("");
        if (terminal.isPublic()) {
            accessLore.add(ChatColor.GRAY + "Любой игрок может использовать");
            accessLore.add(ChatColor.GRAY + "этот терминал.");
        } else {
            accessLore.add(ChatColor.GRAY + "Только авторизованные игроки");
            accessLore.add(ChatColor.GRAY + "могут использовать терминал.");
        }
        accessLore.add("");
        accessLore.add(ChatColor.YELLOW + "▶ Нажмите для изменения");
        accessMeta.setLore(accessLore);
        access.setItemMeta(accessMeta);
        gui.setItem(12, access);
        
        // Управление подключениями
        ItemStack connections = new ItemStack(Material.ENDER_EYE);
        ItemMeta connectionsMeta = connections.getItemMeta();
        connectionsMeta.setDisplayName(ChatColor.BLUE + "Управление подключениями");
        List<String> connectionsLore = new ArrayList<>();
        connectionsLore.add(ChatColor.GRAY + "Подключённых телепортов: " + 
            ChatColor.WHITE + terminal.getConnectedTeleporters().size());
        connectionsLore.add("");
        connectionsLore.add(ChatColor.GRAY + "Настройте, к каким телепортам");
        connectionsLore.add(ChatColor.GRAY + "может подключаться терминал.");
        connectionsLore.add("");
        connectionsLore.add(ChatColor.YELLOW + "▶ Нажмите для настройки");
        connectionsMeta.setLore(connectionsLore);
        connections.setItemMeta(connectionsMeta);
        gui.setItem(14, connections);
        
        // Управление доступом
        ItemStack permissions = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta permissionsMeta = permissions.getItemMeta();
        permissionsMeta.setDisplayName(ChatColor.PURPLE + "Управление доступом");
        List<String> permissionsLore = new ArrayList<>();
        permissionsLore.add(ChatColor.GRAY + "Авторизованных игроков: " + 
            ChatColor.WHITE + terminal.getAuthorizedPlayers().size());
        permissionsLore.add("");
        permissionsLore.add(ChatColor.GRAY + "Добавьте или удалите игроков,");
        permissionsLore.add(ChatColor.GRAY + "которые могут использовать терминал.");
        permissionsLore.add("");
        permissionsLore.add(ChatColor.YELLOW + "▶ Нажмите для настройки");
        permissionsMeta.setLore(permissionsLore);
        permissions.setItemMeta(permissionsMeta);
        gui.setItem(16, permissions);
        
        // Зарядка энергии
        ItemStack charge = new ItemStack(Material.REDSTONE_BLOCK);
        ItemMeta chargeMeta = charge.getItemMeta();
        chargeMeta.setDisplayName(ChatColor.YELLOW + "Зарядить терминал");
        List<String> chargeLore = new ArrayList<>();
        chargeLore.add(ChatColor.GRAY + "Текущая энергия: " + ChatColor.YELLOW + 
            terminal.getEnergyLevel() + "/" + terminal.getMaxEnergy());
        chargeLore.add(ChatColor.GRAY + "Заряд: " + ChatColor.GREEN + 
            String.format("%.1f%%", terminal.getEnergyPercentage()));
        chargeLore.add("");
        chargeLore.add(ChatColor.GRAY + "Используйте редстоун для зарядки.");
        chargeLore.add(ChatColor.GRAY + "1 редстоун = 50 энергии");
        chargeLore.add("");
        chargeLore.add(ChatColor.YELLOW + "▶ Нажмите для зарядки");
        chargeMeta.setLore(chargeLore);
        charge.setItemMeta(chargeMeta);
        gui.setItem(28, charge);
        
        // Переименование
        ItemStack rename = new ItemStack(Material.NAME_TAG);
        ItemMeta renameMeta = rename.getItemMeta();
        renameMeta.setDisplayName(ChatColor.AQUA + "Переименовать терминал");
        List<String> renameLore = new ArrayList<>();
        renameLore.add(ChatColor.GRAY + "Текущее название: " + ChatColor.WHITE + terminal.getName());
        renameLore.add("");
        renameLore.add(ChatColor.GRAY + "Напишите новое название в чат");
        renameLore.add(ChatColor.GRAY + "после нажатия на эту кнопку.");
        renameLore.add("");
        renameLore.add(ChatColor.YELLOW + "▶ Нажмите для переименования");
        renameMeta.setLore(renameLore);
        rename.setItemMeta(renameMeta);
        gui.setItem(30, rename);
        
        // Удаление терминала
        ItemStack delete = new ItemStack(Material.TNT);
        ItemMeta deleteMeta = delete.getItemMeta();
        deleteMeta.setDisplayName(ChatColor.DARK_RED + "Удалить терминал");
        List<String> deleteLore = new ArrayList<>();
        deleteLore.add(ChatColor.RED + "ВНИМАНИЕ! Это действие необратимо!");
        deleteLore.add("");
        deleteLore.add(ChatColor.GRAY + "Терминал будет полностью удалён");
        deleteLore.add(ChatColor.GRAY + "из системы. Структура останется.");
        deleteLore.add("");
        deleteLore.add(ChatColor.DARK_RED + "▶ Нажмите для удаления");
        deleteMeta.setLore(deleteLore);
        delete.setItemMeta(deleteMeta);
        gui.setItem(32, delete);
        
        // Назад
        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta backMeta = back.getItemMeta();
        backMeta.setDisplayName(ChatColor.GRAY + "Назад");
        back.setItemMeta(backMeta);
        gui.setItem(36, back);
        
        // Закрыть
        ItemStack close = new ItemStack(Material.BARRIER);
        ItemMeta closeMeta = close.getItemMeta();
        closeMeta.setDisplayName(ChatColor.RED + "Закрыть");
        close.setItemMeta(closeMeta);
        gui.setItem(44, close);
        
        player.openInventory(gui);
    }
    
    public static void openConnectionManagement(Player player, TeleportTerminal terminal) {
        Inventory gui = Bukkit.createInventory(null, 54, ChatColor.DARK_BLUE + "Подключения: " + terminal.getName());
        
        // Получаем все доступные телепорты
        List<Teleporter> allTeleporters = UraniumPlugin.getInstance().getTeleporterManager().getAllTeleporters();
        
        int slot = 0;
        for (Teleporter teleporter : allTeleporters) {
            if (slot >= 45) break; // Оставляем место для кнопок управления
            
            boolean isConnected = terminal.getConnectedTeleporters().contains(teleporter.getId());
            
            ItemStack teleporterItem = new ItemStack(isConnected ? Material.LIME_DYE : Material.GRAY_DYE);
            ItemMeta teleporterMeta = teleporterItem.getItemMeta();
            teleporterMeta.setDisplayName((isConnected ? ChatColor.GREEN : ChatColor.GRAY) + teleporter.getName());
            
            List<String> teleporterLore = new ArrayList<>();
            teleporterLore.add(ChatColor.GRAY + "Владелец: " + ChatColor.WHITE + 
                Bukkit.getOfflinePlayer(teleporter.getOwner()).getName());
            teleporterLore.add(ChatColor.GRAY + "Мир: " + ChatColor.WHITE + 
                teleporter.getCoreLocation().getWorld().getName());
            
            double distance = terminal.getTerminalLocation().distance(teleporter.getCoreLocation());
            teleporterLore.add(ChatColor.GRAY + "Расстояние: " + ChatColor.WHITE + 
                String.format("%.0f блоков", distance));
            
            teleporterLore.add("");
            teleporterLore.add(ChatColor.GRAY + "Статус: " + (isConnected ? 
                ChatColor.GREEN + "Подключён" : ChatColor.RED + "Отключён"));
            
            teleporterLore.add("");
            if (isConnected) {
                teleporterLore.add(ChatColor.RED + "▶ Нажмите для отключения");
            } else {
                teleporterLore.add(ChatColor.GREEN + "▶ Нажмите для подключения");
            }
            
            teleporterMeta.setLore(teleporterLore);
            teleporterItem.setItemMeta(teleporterMeta);
            gui.setItem(slot, teleporterItem);
            slot++;
        }
        
        // Назад
        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta backMeta = back.getItemMeta();
        backMeta.setDisplayName(ChatColor.GRAY + "Назад");
        back.setItemMeta(backMeta);
        gui.setItem(45, back);
        
        // Закрыть
        ItemStack close = new ItemStack(Material.BARRIER);
        ItemMeta closeMeta = close.getItemMeta();
        closeMeta.setDisplayName(ChatColor.RED + "Закрыть");
        close.setItemMeta(closeMeta);
        gui.setItem(53, close);
        
        player.openInventory(gui);
    }
}
