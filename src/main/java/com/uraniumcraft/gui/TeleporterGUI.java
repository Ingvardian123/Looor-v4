package com.uraniumcraft.gui;

import com.uraniumcraft.UraniumPlugin;
import com.uraniumcraft.teleporter.Teleporter;
import com.uraniumcraft.teleporter.TeleporterManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;

public class TeleporterGUI {
    
    // Меню для обычных игроков
    public static void openPlayerTeleporterMenu(Player player) {
        UraniumPlugin plugin = UraniumPlugin.getInstance();
        TeleporterManager teleporterManager = plugin.getTeleporterManager();
        
        Inventory gui = Bukkit.createInventory(null, 54, ChatColor.LIGHT_PURPLE + "Телепортация");
        
        // Информационная панель
        ItemStack info = new ItemStack(Material.COMPASS);
        ItemMeta infoMeta = info.getItemMeta();
        infoMeta.setDisplayName(ChatColor.AQUA + "Доступные телепорты");
        infoMeta.setLore(Arrays.asList(
            ChatColor.GRAY + "Выберите телепорт для путешествия",
            ChatColor.GRAY + "Убедитесь, что находитесь рядом с телепортом",
            "",
            ChatColor.GREEN + "ЛКМ - Телепортироваться",
            ChatColor.YELLOW + "ПКМ - Информация"
        ));
        info.setItemMeta(infoMeta);
        gui.setItem(4, info);
        
        // Доступные телепорты для телепортации
        List<Teleporter> availableTeleporters = teleporterManager.getAvailableTeleporters(player);
        
        if (availableTeleporters.isEmpty()) {
            ItemStack noTeleporters = new ItemStack(Material.BARRIER);
            ItemMeta noTeleportersMeta = noTeleporters.getItemMeta();
            noTeleportersMeta.setDisplayName(ChatColor.RED + "Нет доступных телепортов");
            noTeleportersMeta.setLore(Arrays.asList(
                ChatColor.GRAY + "В данный момент нет телепортов,",
                ChatColor.GRAY + "к которым вы можете получить доступ.",
                "",
                ChatColor.YELLOW + "Обратитесь к администратору"
            ));
            noTeleporters.setItemMeta(noTeleportersMeta);
            gui.setItem(22, noTeleporters);
        } else {
            int slot = 9;
            for (Teleporter teleporter : availableTeleporters) {
                if (slot >= 45) break;
                
                ItemStack teleporterItem = createTeleporterItem(teleporter, player);
                gui.setItem(slot++, teleporterItem);
            }
        }
        
        // Помощь
        ItemStack help = new ItemStack(Material.BOOK);
        ItemMeta helpMeta = help.getItemMeta();
        helpMeta.setDisplayName(ChatColor.YELLOW + "Как пользоваться?");
        helpMeta.setLore(Arrays.asList(
            ChatColor.GRAY + "1. Подойдите к любому активному телепорту",
            ChatColor.GRAY + "2. Выберите пункт назначения в этом меню",
            ChatColor.GRAY + "3. Не двигайтесь во время телепортации",
            "",
            ChatColor.AQUA + "Стоимость зависит от расстояния",
            ChatColor.AQUA + "Между телепортациями есть кулдаун"
        ));
        help.setItemMeta(helpMeta);
        gui.setItem(45, help);
        
        // Обновить
        ItemStack refresh = new ItemStack(Material.CLOCK);
        ItemMeta refreshMeta = refresh.getItemMeta();
        refreshMeta.setDisplayName(ChatColor.AQUA + "Обновить список");
        refreshMeta.setLore(Arrays.asList(
            ChatColor.GRAY + "Обновить список доступных телепортов",
            "",
            ChatColor.GREEN + "Нажмите для обновления"
        ));
        refresh.setItemMeta(refreshMeta);
        gui.setItem(53, refresh);
        
        // Заполняем пустые слоты
        fillEmptySlots(gui, Material.PURPLE_STAINED_GLASS_PANE);
        
        player.openInventory(gui);
    }
    
    // Админское меню (старое)
    public static void openTeleporterMenu(Player player) {
        UraniumPlugin plugin = UraniumPlugin.getInstance();
        TeleporterManager teleporterManager = plugin.getTeleporterManager();
        
        Inventory gui = Bukkit.createInventory(null, 54, ChatColor.LIGHT_PURPLE + "Управление телепортами");
        
        // Информация о телепортах игрока
        List<Teleporter> playerTeleporters = teleporterManager.getPlayerTeleporters(player.getUniqueId());
        
        ItemStack info = new ItemStack(Material.BEACON);
        ItemMeta infoMeta = info.getItemMeta();
        infoMeta.setDisplayName(ChatColor.AQUA + "Ваши телепорты");
        infoMeta.setLore(Arrays.asList(
            ChatColor.GRAY + "Всего телепортов: " + ChatColor.YELLOW + playerTeleporters.size(),
            ChatColor.GRAY + "Максимум: " + ChatColor.YELLOW + plugin.getConfig().getInt("teleporter.max_per_player", 5),
            "",
            ChatColor.GREEN + "Нажмите для управления"
        ));
        info.setItemMeta(infoMeta);
        gui.setItem(4, info);
        
        // Доступные телепорты для телепортации
        List<Teleporter> availableTeleporters = teleporterManager.getAvailableTeleporters(player);
        
        int slot = 9;
        for (Teleporter teleporter : availableTeleporters) {
            if (slot >= 45) break;
            
            ItemStack teleporterItem = createTeleporterItem(teleporter, player);
            gui.setItem(slot++, teleporterItem);
        }
        
        // Создать новый телепорт
        ItemStack create = new ItemStack(Material.CRAFTING_TABLE);
        ItemMeta createMeta = create.getItemMeta();
        createMeta.setDisplayName(ChatColor.GREEN + "Создать телепорт");
        createMeta.setLore(Arrays.asList(
            ChatColor.GRAY + "Создать новый телепорт",
            ChatColor.YELLOW + "Требует правильную структуру",
            "",
            ChatColor.GREEN + "Нажмите для создания"
        ));
        create.setItemMeta(createMeta);
        gui.setItem(45, create);
        
        // Помощь по структуре
        ItemStack help = new ItemStack(Material.BOOK);
        ItemMeta helpMeta = help.getItemMeta();
        helpMeta.setDisplayName(ChatColor.YELLOW + "Помощь по структуре");
        helpMeta.setLore(Arrays.asList(
            ChatColor.GRAY + "Показать схему постройки",
            ChatColor.GRAY + "телепортационной камеры",
            "",
            ChatColor.GREEN + "Нажмите для просмотра"
        ));
        help.setItemMeta(helpMeta);
        gui.setItem(49, help);
        
        // Обновить
        ItemStack refresh = new ItemStack(Material.CLOCK);
        ItemMeta refreshMeta = refresh.getItemMeta();
        refreshMeta.setDisplayName(ChatColor.AQUA + "Обновить");
        refreshMeta.setLore(Arrays.asList(
            ChatColor.GRAY + "Обновить список телепортов",
            "",
            ChatColor.GREEN + "Нажмите для обновления"
        ));
        refresh.setItemMeta(refreshMeta);
        gui.setItem(53, refresh);
        
        // Заполняем пустые слоты
        fillEmptySlots(gui, Material.PURPLE_STAINED_GLASS_PANE);
        
        player.openInventory(gui);
    }
    
    public static void openTeleporterManagement(Player player, Teleporter teleporter) {
        Inventory gui = Bukkit.createInventory(null, 27, ChatColor.DARK_PURPLE + "Управление: " + teleporter.getName());
        
        // Информация о телепорте
        ItemStack info = new ItemStack(Material.BEACON);
        ItemMeta infoMeta = info.getItemMeta();
        infoMeta.setDisplayName(ChatColor.AQUA + teleporter.getName());
        infoMeta.setLore(Arrays.asList(
            ChatColor.GRAY + "Статус: " + teleporter.getStatusString(),
            ChatColor.GRAY + "Энергия: " + getEnergyBar(teleporter),
            ChatColor.GRAY + "Доступ: " + (teleporter.isPublic() ? 
                ChatColor.GREEN + "Публичный" : ChatColor.YELLOW + "Приватный"),
            ChatColor.GRAY + "Координаты: " + 
                teleporter.getCoreLocation().getBlockX() + ", " +
                teleporter.getCoreLocation().getBlockY() + ", " +
                teleporter.getCoreLocation().getBlockZ()
        ));
        info.setItemMeta(infoMeta);
        gui.setItem(4, info);
        
        // Переименовать
        ItemStack rename = new ItemStack(Material.NAME_TAG);
        ItemMeta renameMeta = rename.getItemMeta();
        renameMeta.setDisplayName(ChatColor.YELLOW + "Переименовать");
        renameMeta.setLore(Arrays.asList(
            ChatColor.GRAY + "Изменить название телепорта",
            "",
            ChatColor.GREEN + "Нажмите для переименования"
        ));
        rename.setItemMeta(renameMeta);
        gui.setItem(10, rename);
        
        // Переключить доступ
        ItemStack toggleAccess = new ItemStack(teleporter.isPublic() ? Material.LIME_DYE : Material.GRAY_DYE);
        ItemMeta toggleMeta = toggleAccess.getItemMeta();
        toggleMeta.setDisplayName(ChatColor.BLUE + "Переключить доступ");
        toggleMeta.setLore(Arrays.asList(
            ChatColor.GRAY + "Текущий режим: " + (teleporter.isPublic() ? 
                ChatColor.GREEN + "Публичный" : ChatColor.YELLOW + "Приватный"),
            "",
            ChatColor.GREEN + "Нажмите для изменения"
        ));
        toggleAccess.setItemMeta(toggleMeta);
        gui.setItem(12, toggleAccess);
        
        // Добавить энергию
        ItemStack addEnergy = new ItemStack(Material.REDSTONE);
        ItemMeta addEnergyMeta = addEnergy.getItemMeta();
        addEnergyMeta.setDisplayName(ChatColor.RED + "Добавить энергию");
        addEnergyMeta.setLore(Arrays.asList(
            ChatColor.GRAY + "Зарядить телепорт энергией",
            ChatColor.YELLOW + "Стоимость: 1 редстоун = 100 энергии",
            "",
            ChatColor.GREEN + "Нажмите для зарядки"
        ));
        addEnergy.setItemMeta(addEnergyMeta);
        gui.setItem(14, addEnergy);
        
        // Удалить телепорт
        ItemStack delete = new ItemStack(Material.TNT);
        ItemMeta deleteMeta = delete.getItemMeta();
        deleteMeta.setDisplayName(ChatColor.RED + "Удалить телепорт");
        deleteMeta.setLore(Arrays.asList(
            ChatColor.GRAY + "Полностью удалить телепорт",
            ChatColor.RED + "Это действие необратимо!",
            "",
            ChatColor.RED + "Нажмите для удаления"
        ));
        delete.setItemMeta(deleteMeta);
        gui.setItem(16, delete);
        
        // Назад
        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta backMeta = back.getItemMeta();
        backMeta.setDisplayName(ChatColor.GRAY + "← Назад");
        back.setItemMeta(backMeta);
        gui.setItem(22, back);
        
        fillEmptySlots(gui, Material.PURPLE_STAINED_GLASS_PANE);
        player.openInventory(gui);
    }
    
    public static void openTeleporterInfo(Player player, Teleporter teleporter) {
        Inventory gui = Bukkit.createInventory(null, 27, ChatColor.AQUA + "Информация: " + teleporter.getName());
        
        // Основная информация
        ItemStack info = new ItemStack(Material.BEACON);
        ItemMeta infoMeta = info.getItemMeta();
        infoMeta.setDisplayName(ChatColor.AQUA + teleporter.getName());
        
        String ownerName = "Неизвестно";
        Player owner = Bukkit.getPlayer(teleporter.getOwner());
        if (owner != null) {
            ownerName = owner.getName();
        }
        
        double distance = player.getLocation().distance(teleporter.getCoreLocation());
        int energyCost = (int) Math.max(100, distance / 10);
        
        infoMeta.setLore(Arrays.asList(
            ChatColor.GRAY + "Владелец: " + ChatColor.WHITE + ownerName,
            ChatColor.GRAY + "Статус: " + teleporter.getStatusString(),
            ChatColor.GRAY + "Доступ: " + (teleporter.isPublic() ? 
                ChatColor.GREEN + "Публичный" : ChatColor.YELLOW + "Приватный"),
            ChatColor.GRAY + "Энергия: " + getEnergyBar(teleporter),
            "",
            ChatColor.GRAY + "Расстояние: " + ChatColor.YELLOW + String.format("%.1f", distance) + " блоков",
            ChatColor.GRAY + "Стоимость: " + ChatColor.GOLD + energyCost + " энергии",
            ChatColor.GRAY + "Координаты: " + ChatColor.WHITE + 
                teleporter.getCoreLocation().getBlockX() + ", " +
                teleporter.getCoreLocation().getBlockY() + ", " +
                teleporter.getCoreLocation().getBlockZ(),
            ChatColor.GRAY + "Использований: " + ChatColor.WHITE + teleporter.getTotalTeleportations()
        ));
        info.setItemMeta(infoMeta);
        gui.setItem(13, info);
        
        // Телепортироваться
        if (teleporter.canTeleport() && teleporter.canPlayerUse(player)) {
            ItemStack teleport = new ItemStack(Material.ENDER_PEARL);
            ItemMeta teleportMeta = teleport.getItemMeta();
            teleportMeta.setDisplayName(ChatColor.GREEN + "Телепортироваться");
            teleportMeta.setLore(Arrays.asList(
                ChatColor.GRAY + "Начать телепортацию к этому телепорту",
                ChatColor.YELLOW + "Убедитесь, что находитесь рядом с телепортом",
                "",
                ChatColor.GREEN + "Нажмите для телепортации"
            ));
            teleport.setItemMeta(teleportMeta);
            gui.setItem(11, teleport);
        } else {
            ItemStack cantTeleport = new ItemStack(Material.BARRIER);
            ItemMeta cantTeleportMeta = cantTeleport.getItemMeta();
            cantTeleportMeta.setDisplayName(ChatColor.RED + "Телепортация недоступна");
            cantTeleportMeta.setLore(Arrays.asList(
                ChatColor.GRAY + "Причины:",
                !teleporter.isActive() ? ChatColor.RED + "• Телепорт неактивен" : "",
                !teleporter.validateStructure() ? ChatColor.RED + "• Повреждена структура" : "",
                teleporter.getEnergyLevel() < 100 ? ChatColor.RED + "• Недостаточно энергии" : "",
                !teleporter.canPlayerUse(player) ? ChatColor.RED + "• Нет доступа" : ""
            ));
            cantTeleport.setItemMeta(cantTeleportMeta);
            gui.setItem(11, cantTeleport);
        }
        
        // Назад
        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta backMeta = back.getItemMeta();
        backMeta.setDisplayName(ChatColor.GRAY + "← Назад к списку");
        back.setItemMeta(backMeta);
        gui.setItem(15, back);
        
        fillEmptySlots(gui, Material.LIGHT_BLUE_STAINED_GLASS_PANE);
        player.openInventory(gui);
    }
    
    private static ItemStack createTeleporterItem(Teleporter teleporter, Player player) {
        ItemStack teleporterItem = new ItemStack(Material.ENDER_PEARL);
        ItemMeta teleporterMeta = teleporterItem.getItemMeta();
        teleporterMeta.setDisplayName(ChatColor.LIGHT_PURPLE + teleporter.getName());
        
        String ownerName = "Неизвестно";
        Player owner = Bukkit.getPlayer(teleporter.getOwner());
        if (owner != null) {
            ownerName = owner.getName();
        }
        
        String status = teleporter.getStatusString();
        String access = teleporter.isPublic() ? 
            ChatColor.GREEN + "Публичный" : ChatColor.YELLOW + "Приватный";
        
        double distance = player.getLocation().distance(teleporter.getCoreLocation());
        int energyCost = (int) Math.max(100, distance / 10);
        
        teleporterMeta.setLore(Arrays.asList(
            ChatColor.GRAY + "Владелец: " + ChatColor.WHITE + ownerName,
            ChatColor.GRAY + "Статус: " + status,
            ChatColor.GRAY + "Доступ: " + access,
            ChatColor.GRAY + "Энергия: " + getEnergyBar(teleporter),
            ChatColor.GRAY + "Расстояние: " + ChatColor.YELLOW + String.format("%.1f", distance) + " блоков",
            ChatColor.GRAY + "Стоимость: " + ChatColor.GOLD + energyCost + " энергии",
            "",
            teleporter.canTeleport() && teleporter.canPlayerUse(player) ? 
                ChatColor.GREEN + "ЛКМ - Телепортироваться" : 
                ChatColor.RED + "Телепорт недоступен",
            ChatColor.YELLOW + "ПКМ - Подробная информация"
        ));
        
        // Меняем материал в зависимости от статуса
        if (!teleporter.canTeleport()) {
            teleporterItem.setType(Material.GRAY_DYE);
        } else if (teleporter.getOwner().equals(player.getUniqueId())) {
            teleporterItem.setType(Material.LIME_DYE);
        }
        
        teleporterItem.setItemMeta(teleporterMeta);
        return teleporterItem;
    }
    
    private static String getEnergyBar(Teleporter teleporter) {
        double percentage = teleporter.getEnergyPercentage();
        int bars = (int) (percentage / 5); // 20 символов максимум
        
        StringBuilder bar = new StringBuilder();
        for (int i = 0; i < 20; i++) {
            if (i < bars) {
                if (percentage > 80) {
                    bar.append(ChatColor.GREEN).append("█");
                } else if (percentage > 50) {
                    bar.append(ChatColor.YELLOW).append("█");
                } else if (percentage > 20) {
                    bar.append(ChatColor.GOLD).append("█");
                } else {
                    bar.append(ChatColor.RED).append("█");
                }
            } else {
                bar.append(ChatColor.GRAY).append("░");
            }
        }
        
        return bar.toString() + ChatColor.WHITE + " " + 
            teleporter.getEnergyLevel() + "/" + teleporter.getMaxEnergy();
    }
    
    private static void fillEmptySlots(Inventory gui, Material material) {
        ItemStack filler = new ItemStack(material);
        ItemMeta fillerMeta = filler.getItemMeta();
        fillerMeta.setDisplayName(" ");
        filler.setItemMeta(fillerMeta);
        
        for (int i = 0; i < gui.getSize(); i++) {
            if (gui.getItem(i) == null) {
                gui.setItem(i, filler);
            }
        }
    }
}
