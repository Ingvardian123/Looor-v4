package com.uraniumcraft.listeners;

import com.uraniumcraft.UraniumPlugin;
import com.uraniumcraft.gui.TeleporterGUI;
import com.uraniumcraft.teleporter.Teleporter;
import com.uraniumcraft.teleporter.TeleporterManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;

public class TeleporterListener implements Listener {
    
    private final UraniumPlugin plugin;
    private final TeleporterManager teleporterManager;
    
    public TeleporterListener(UraniumPlugin plugin) {
        this.plugin = plugin;
        this.teleporterManager = plugin.getTeleporterManager();
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null) return;
        
        Block block = event.getClickedBlock();
        Player player = event.getPlayer();
        
        // Проверяем взаимодействие с маяком телепорта
        if (block.getType() == Material.BEACON) {
            Teleporter teleporter = teleporterManager.getTeleporterAt(block.getLocation());
            
            if (teleporter != null) {
                event.setCancelled(true);
                
                if (player.isSneaking()) {
                    // Shift + ПКМ = управление телепортом (только для админов и владельцев)
                    if ((teleporter.getOwner().equals(player.getUniqueId()) || 
                        player.hasPermission("uraniumcraft.teleporter.admin")) &&
                        player.hasPermission("uraniumcraft.teleporter.admin")) {
                        TeleporterGUI.openTeleporterManagement(player, teleporter);
                    } else {
                        player.sendMessage(ChatColor.RED + "У вас нет прав для управления этим телепортом!");
                    }
                } else {
                    // Обычный клик = открыть меню телепортации для всех
                    if (player.hasPermission("uraniumcraft.teleporter.use")) {
                        TeleporterGUI.openPlayerTeleporterMenu(player);
                    } else {
                        player.sendMessage(ChatColor.RED + "У вас нет разрешения на использование телепортов!");
                    }
                }
                return;
            }
        }
        
        // Проверяем размещение маяка для создания телепорта (только админы)
        if (event.getAction().name().contains("RIGHT_CLICK") && 
            player.getInventory().getItemInMainHand().getType() == Material.BEACON &&
            player.hasPermission("uraniumcraft.teleporter.admin")) {
            
            if (block.getType().isSolid()) {
                Block above = block.getRelative(0, 1, 0);
                if (above.getType() == Material.AIR) {
                    player.sendMessage(ChatColor.YELLOW + "Для создания телепорта постройте правильную структуру и используйте команду /teleporter create <название>");
                }
            }
        }
    }
    
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Block block = player.getLocation().getBlock();
        
        // Проверяем, стоит ли игрок на телепорте
        if (block.getType() == Material.BEACON || 
            block.getRelative(0, -1, 0).getType() == Material.BEACON) {
            
            Teleporter teleporter = teleporterManager.getTeleporterAt(player.getLocation());
            
            if (teleporter != null && teleporter.isActive() && !teleporter.isTeleporting()) {
                // Показываем информацию о телепорте
                String status = teleporter.getStatusString();
                String energyBar = getEnergyBar(teleporter);
                
                player.sendActionBar(ChatColor.LIGHT_PURPLE + teleporter.getName() + " " + 
                    status + " " + ChatColor.GRAY + "| " + energyBar);
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        Player player = event.getPlayer();
        
        // Проверяем, не ломает ли игрок часть телепорта
        Teleporter teleporter = teleporterManager.getTeleporterAt(block.getLocation());
        
        if (teleporter != null) {
            // Проверяем права на разрушение
            if (!teleporter.getOwner().equals(player.getUniqueId()) && 
                !player.hasPermission("uraniumcraft.teleporter.admin")) {
                
                event.setCancelled(true);
                player.sendMessage(ChatColor.RED + "Вы не можете разрушать чужие телепорты!");
                return;
            }
            
            // Предупреждаем о разрушении телепорта
            if (block.getType() == Material.BEACON) {
                player.sendMessage(ChatColor.YELLOW + "Внимание! Разрушение маяка деактивирует телепорт!");
                teleporter.deactivate();
            }
        }
        
        // Проверяем разрушение других частей структуры
        for (Teleporter t : teleporterManager.getAllTeleporters().values()) {
            if (isPartOfTeleporter(block.getLocation(), t.getCoreLocation())) {
                if (!t.getOwner().equals(player.getUniqueId()) && 
                    !player.hasPermission("uraniumcraft.teleporter.admin")) {
                    
                    event.setCancelled(true);
                    player.sendMessage(ChatColor.RED + "Вы не можете разрушать части телепорта!");
                    return;
                }
                
                player.sendMessage(ChatColor.YELLOW + "Разрушение структуры может повредить телепорт '" + t.getName() + "'!");
                break;
            }
        }
    }
    
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Block block = event.getBlock();
        Player player = event.getPlayer();
        
        // Проверяем размещение блоков рядом с телепортами
        for (Teleporter teleporter : teleporterManager.getAllTeleporters().values()) {
            if (block.getLocation().distance(teleporter.getCoreLocation()) <= 5) {
                if (!teleporter.getOwner().equals(player.getUniqueId()) && 
                    !player.hasPermission("uraniumcraft.teleporter.admin")) {
                    
                    event.setCancelled(true);
                    player.sendMessage(ChatColor.RED + "Вы не можете строить рядом с чужими телепортами!");
                    return;
                }
            }
        }
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        
        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();
        
        if (!title.contains("телепорт") && !title.contains("Телепортация")) return;
        
        event.setCancelled(true);
        
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;
        
        if (title.equals(ChatColor.LIGHT_PURPLE + "Телепортация")) {
            handlePlayerTeleporterGUI(player, clicked, event.getClick());
        } else if (title.contains("Квантовые телепорты") || title.contains("Управление телепортами")) {
            handleAdminTeleporterGUI(player, clicked, event.getSlot());
        } else if (title.contains("Управление:")) {
            handleTeleporterManagementGUI(player, clicked, event.getSlot(), title);
        } else if (title.contains("Информация:")) {
            handleTeleporterInfoGUI(player, clicked, event.getSlot(), title);
        }
    }
    
    private void handlePlayerTeleporterGUI(Player player, ItemStack clicked, ClickType clickType) {
        if (clicked.getType() == Material.ENDER_PEARL || clicked.getType() == Material.LIME_DYE || clicked.getType() == Material.GRAY_DYE) {
            // Телепортация к выбранному телепорту
            String teleporterName = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());
            
            for (Teleporter teleporter : teleporterManager.getAvailableTeleporters(player)) {
                if (teleporter.getName().equals(teleporterName)) {
                    if (clickType == ClickType.LEFT) {
                        // ЛКМ - телепортация
                        player.closeInventory();
                        
                        // Находим ближайший телепорт игрока
                        Teleporter nearestTeleporter = teleporterManager.getTeleporterAt(player.getLocation());
                        if (nearestTeleporter == null) {
                            player.sendMessage(ChatColor.RED + "Вы должны находиться рядом с телепортом для телепортации!");
                            return;
                        }
                        
                        nearestTeleporter.startTeleportation(player, teleporter);
                    } else if (clickType == ClickType.RIGHT) {
                        // ПКМ - информация
                        TeleporterGUI.openTeleporterInfo(player, teleporter);
                    }
                    return;
                }
            }
        } else if (clicked.getType() == Material.BOOK) {
            // Помощь
            player.closeInventory();
            showTeleporterHelp(player);
            
        } else if (clicked.getType() == Material.CLOCK) {
            // Обновить список
            TeleporterGUI.openPlayerTeleporterMenu(player);
        }
    }
    
    private void handleAdminTeleporterGUI(Player player, ItemStack clicked, int slot) {
        if (clicked.getType() == Material.ENDER_PEARL || clicked.getType() == Material.LIME_DYE || clicked.getType() == Material.GRAY_DYE) {
            // Телепортация к выбранному телепорту
            String teleporterName = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());
            
            for (Teleporter teleporter : teleporterManager.getAvailableTeleporters(player)) {
                if (teleporter.getName().equals(teleporterName)) {
                    player.closeInventory();
                    
                    // Находим ближайший телепорт игрока
                    Teleporter nearestTeleporter = teleporterManager.getTeleporterAt(player.getLocation());
                    if (nearestTeleporter == null) {
                        player.sendMessage(ChatColor.RED + "Вы должны находиться рядом с телепортом для телепортации!");
                        return;
                    }
                    
                    nearestTeleporter.startTeleportation(player, teleporter);
                    return;
                }
            }
        } else if (clicked.getType() == Material.CRAFTING_TABLE) {
            // Создать новый телепорт
            player.closeInventory();
            player.sendMessage(ChatColor.YELLOW + "Постройте структуру телепорта и используйте команду /teleporter create <название>");
            
        } else if (clicked.getType() == Material.BOOK) {
            // Помощь по структуре
            player.closeInventory();
            showTeleporterHelp(player);
            
        } else if (clicked.getType() == Material.CLOCK) {
            // Обновить список
            TeleporterGUI.openTeleporterMenu(player);
        }
    }
    
    private void handleTeleporterManagementGUI(Player player, ItemStack clicked, int slot, String title) {
        String teleporterName = title.substring(title.indexOf(":") + 2);
        Teleporter teleporter = teleporterManager.getTeleporterByName(teleporterName, player.getUniqueId());
        
        if (teleporter == null) {
            player.sendMessage(ChatColor.RED + "Телепорт не найден!");
            player.closeInventory();
            return;
        }
        
        switch (clicked.getType()) {
            case NAME_TAG:
                // Переименовать
                player.closeInventory();
                player.sendMessage(ChatColor.YELLOW + "Введите новое название в чат:");
                break;
                
            case LIME_DYE:
            case GRAY_DYE:
                // Переключить доступ
                teleporter.setPublic(!teleporter.isPublic());
                player.sendMessage(ChatColor.GREEN + "Режим доступа изменён на: " + 
                    (teleporter.isPublic() ? "Публичный" : "Приватный"));
                TeleporterGUI.openTeleporterManagement(player, teleporter);
                break;
                
            case REDSTONE:
                // Добавить энергию
                int redstoneCount = 0;
                for (ItemStack item : player.getInventory().getContents()) {
                    if (item != null && item.getType() == Material.REDSTONE) {
                        redstoneCount += item.getAmount();
                    }
                }
                
                if (redstoneCount > 0) {
                    int energyToAdd = Math.min(redstoneCount * 100, teleporter.getMaxEnergy() - teleporter.getEnergyLevel());
                    int redstoneNeeded = energyToAdd / 100;
                    
                    if (redstoneNeeded > 0) {
                        // Удаляем редстоун из инвентаря
                        int remaining = redstoneNeeded;
                        for (ItemStack item : player.getInventory().getContents()) {
                            if (item != null && item.getType() == Material.REDSTONE && remaining > 0) {
                                int toRemove = Math.min(item.getAmount(), remaining);
                                item.setAmount(item.getAmount() - toRemove);
                                remaining -= toRemove;
                            }
                        }
                        
                        teleporter.addEnergy(energyToAdd);
                        player.sendMessage(ChatColor.GREEN + "Добавлено " + energyToAdd + " энергии!");
                    } else {
                        player.sendMessage(ChatColor.YELLOW + "Телепорт уже полностью заряжен!");
                    }
                } else {
                    player.sendMessage(ChatColor.RED + "У вас нет редстоуна для зарядки!");
                }
                
                TeleporterGUI.openTeleporterManagement(player, teleporter);
                break;
                
            case TNT:
                // Удалить телепорт
                player.closeInventory();
                player.sendMessage(ChatColor.RED + "Введите 'CONFIRM' в чат для подтверждения удаления телепорта!");
                break;
                
            case ARROW:
                // Назад
                TeleporterGUI.openTeleporterMenu(player);
                break;
        }
    }
    
    private void handleTeleporterInfoGUI(Player player, ItemStack clicked, int slot, String title) {
        String teleporterName = title.substring(title.indexOf(":") + 2);
        
        // Ищем телепорт среди доступных
        Teleporter teleporter = null;
        for (Teleporter t : teleporterManager.getAvailableTeleporters(player)) {
            if (t.getName().equals(teleporterName)) {
                teleporter = t;
                break;
            }
        }
        
        if (teleporter == null) {
            player.sendMessage(ChatColor.RED + "Телепорт не найден!");
            player.closeInventory();
            return;
        }
        
        switch (clicked.getType()) {
            case ENDER_PEARL:
                // Телепортироваться
                player.closeInventory();
                
                // Находим ближайший телепорт игрока
                Teleporter nearestTeleporter = teleporterManager.getTeleporterAt(player.getLocation());
                if (nearestTeleporter == null) {
                    player.sendMessage(ChatColor.RED + "Вы должны находиться рядом с телепортом для телепортации!");
                    return;
                }
                
                nearestTeleporter.startTeleportation(player, teleporter);
                break;
                
            case ARROW:
                // Назад к списку
                TeleporterGUI.openPlayerTeleporterMenu(player);
                break;
        }
    }
    
    private void showTeleporterHelp(Player player) {
        player.sendMessage(ChatColor.GOLD + "=== Как пользоваться телепортами ===");
        player.sendMessage(ChatColor.YELLOW + "1. Подойдите к любому активному телепорту");
        player.sendMessage(ChatColor.YELLOW + "2. Кликните по маяку или используйте /tp");
        player.sendMessage(ChatColor.YELLOW + "3. Выберите пункт назначения в меню");
        player.sendMessage(ChatColor.YELLOW + "4. Не двигайтесь во время телепортации");
        player.sendMessage(ChatColor.GREEN + "Стоимость зависит от расстояния между телепортами");
        player.sendMessage(ChatColor.GREEN + "Между телепортациями есть кулдаун");
        
        if (player.hasPermission("uraniumcraft.teleporter.admin")) {
            player.sendMessage(ChatColor.GOLD + "\n=== Структура телепорта ===");
            player.sendMessage(ChatColor.AQUA + "1. Основание 3x3 из кварцевых блоков");
            player.sendMessage(ChatColor.AQUA + "2. Маяк в центре основания");
            player.sendMessage(ChatColor.AQUA + "3. 4 столба из кварцевых блоков по углам (высота 3)");
            player.sendMessage(ChatColor.AQUA + "4. Рама из стекла на высоте 3");
            player.sendMessage(ChatColor.AQUA + "5. 4 морских фонаря на расстоянии 2 блоков от центра");
        }
    }
    
    private boolean isPartOfTeleporter(org.bukkit.Location blockLocation, org.bukkit.Location coreLocation) {
        double distance = blockLocation.distance(coreLocation);
        return distance <= 3.0; // Радиус структуры телепорта
    }
    
    private String getEnergyBar(Teleporter teleporter) {
        double percentage = teleporter.getEnergyPercentage();
        int bars = (int) (percentage / 10); // 10 символов максимум
        
        StringBuilder bar = new StringBuilder();
        for (int i = 0; i < 10; i++) {
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
            (int)percentage + "%";
    }
}
