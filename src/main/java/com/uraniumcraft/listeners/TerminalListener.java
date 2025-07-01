package com.uraniumcraft.listeners;

import com.uraniumcraft.UraniumPlugin;
import com.uraniumcraft.gui.TerminalGUI;
import com.uraniumcraft.teleporter.TeleportTerminal;
import com.uraniumcraft.teleporter.TeleportTerminalManager;
import com.uraniumcraft.teleporter.Teleporter;
import com.uraniumcraft.teleporter.TeleporterManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TerminalListener implements Listener {
    
    private final UraniumPlugin plugin;
    private final TeleportTerminalManager terminalManager;
    private final TeleporterManager teleporterManager;
    private final Map<UUID, String> renamingTerminals = new HashMap<>();
    
    public TerminalListener(UraniumPlugin plugin) {
        this.plugin = plugin;
        this.terminalManager = plugin.getTeleportTerminalManager();
        this.teleporterManager = plugin.getTeleporterManager();
    }
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        
        Block block = event.getClickedBlock();
        if (block == null || block.getType() != Material.OBSERVER) {
            return;
        }
        
        TeleportTerminal terminal = terminalManager.getTerminalAt(block.getLocation());
        if (terminal == null) {
            return;
        }
        
        event.setCancelled(true);
        Player player = event.getPlayer();
        
        if (!terminal.canPlayerUse(player)) {
            player.sendMessage(ChatColor.RED + "У вас нет доступа к этому терминалу!");
            return;
        }
        
        if (!terminal.canOperate()) {
            player.sendMessage(ChatColor.RED + "Терминал недоступен! Статус: " + terminal.getStatusString());
            return;
        }
        
        TerminalGUI.openTerminalMainMenu(player, terminal);
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();
        
        if (!title.startsWith(ChatColor.DARK_BLUE + "Терминал:") && 
            !title.startsWith(ChatColor.DARK_RED + "Управление:") &&
            !title.startsWith(ChatColor.DARK_BLUE + "Подключения:")) {
            return;
        }
        
        event.setCancelled(true);
        
        if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) {
            return;
        }
        
        ItemStack item = event.getCurrentItem();
        String itemName = item.getItemMeta().getDisplayName();
        
        // Определяем терминал из заголовка
        String terminalName = title.substring(title.indexOf(":") + 2);
        TeleportTerminal terminal = terminalManager.getTerminalByName(terminalName);
        
        if (terminal == null) {
            player.sendMessage(ChatColor.RED + "Терминал не найден!");
            player.closeInventory();
            return;
        }
        
        // Обработка основного меню терминала
        if (title.startsWith(ChatColor.DARK_BLUE + "Терминал:")) {
            handleTerminalMainMenu(player, terminal, item, itemName);
        }
        // Обработка меню управления
        else if (title.startsWith(ChatColor.DARK_RED + "Управление:")) {
            handleTerminalManagement(player, terminal, item, itemName);
        }
        // Обработка меню подключений
        else if (title.startsWith(ChatColor.DARK_BLUE + "Подключения:")) {
            handleConnectionManagement(player, terminal, item, itemName);
        }
    }
    
    private void handleTerminalMainMenu(Player player, TeleportTerminal terminal, ItemStack item, String itemName) {
        if (itemName.equals(ChatColor.RED + "Закрыть")) {
            player.closeInventory();
        }
        else if (itemName.equals(ChatColor.GOLD + "Управление терминалом")) {
            if (terminal.getOwner().equals(player.getUniqueId()) || player.hasPermission("uraniumcraft.teleporter.admin")) {
                TerminalGUI.openTerminalManagement(player, terminal);
            } else {
                player.sendMessage(ChatColor.RED + "У вас нет прав на управление этим терминалом!");
            }
        }
        else if (item.getType() == Material.ENDER_PEARL) {
            // Телепортация к выбранному телепорту
            String teleporterName = ChatColor.stripColor(itemName);
            
            for (Teleporter teleporter : terminal.getConnectedTeleporters()) {
                if (teleporter.getName().equals(teleporterName)) {
                    player.closeInventory();
                    terminal.startTeleportation(player, teleporter);
                    return;
                }
            }
            
            player.sendMessage(ChatColor.RED + "Телепорт не найден!");
        }
    }
    
    private void handleTerminalManagement(Player player, TeleportTerminal terminal, ItemStack item, String itemName) {
        if (itemName.equals(ChatColor.RED + "Закрыть")) {
            player.closeInventory();
        }
        else if (itemName.equals(ChatColor.GRAY + "Назад")) {
            TerminalGUI.openTerminalMainMenu(player, terminal);
        }
        else if (itemName.contains("активировать терминал")) {
            if (terminal.isActive()) {
                terminal.deactivate();
                player.sendMessage(ChatColor.RED + "Терминал деактивирован!");
            } else {
                if (terminal.validateStructure()) {
                    terminal.activate();
                    player.sendMessage(ChatColor.GREEN + "Терминал активирован!");
                } else {
                    player.sendMessage(ChatColor.RED + "Структура терминала повреждена! Проверьте конструкцию.");
                }
            }
            terminalManager.saveTerminals();
            TerminalGUI.openTerminalManagement(player, terminal);
        }
        else if (itemName.contains("публичным") || itemName.contains("приватным")) {
            terminal.setPublic(!terminal.isPublic());
            player.sendMessage(ChatColor.GREEN + "Режим доступа изменён на " + 
                (terminal.isPublic() ? "публичный" : "приватный") + "!");
            terminalManager.saveTerminals();
            TerminalGUI.openTerminalManagement(player, terminal);
        }
        else if (itemName.equals(ChatColor.BLUE + "Управление подключениями")) {
            TerminalGUI.openConnectionManagement(player, terminal);
        }
        else if (itemName.equals(ChatColor.YELLOW + "Зарядить терминал")) {
            handleTerminalCharging(player, terminal);
        }
        else if (itemName.equals(ChatColor.AQUA + "Переименовать терминал")) {
            player.closeInventory();
            player.sendMessage(ChatColor.YELLOW + "Введите новое название терминала в чат:");
            renamingTerminals.put(player.getUniqueId(), terminal.getName());
        }
        else if (itemName.equals(ChatColor.DARK_RED + "Удалить терминал")) {
            player.closeInventory();
            if (terminalManager.deleteTerminal(terminal.getId())) {
                player.sendMessage(ChatColor.GREEN + "Терминал '" + terminal.getName() + "' успешно удалён!");
            } else {
                player.sendMessage(ChatColor.RED + "Ошибка при удалении терминала!");
            }
        }
    }
    
    private void handleConnectionManagement(Player player, TeleportTerminal terminal, ItemStack item, String itemName) {
        if (itemName.equals(ChatColor.RED + "Закрыть")) {
            player.closeInventory();
        }
        else if (itemName.equals(ChatColor.GRAY + "Назад")) {
            TerminalGUI.openTerminalManagement(player, terminal);
        }
        else if (item.getType() == Material.LIME_DYE || item.getType() == Material.GRAY_DYE) {
            String teleporterName = ChatColor.stripColor(itemName);
            
            for (Teleporter teleporter : teleporterManager.getAllTeleporters()) {
                if (teleporter.getName().equals(teleporterName)) {
                    if (terminal.getConnectedTeleporters().contains(teleporter.getId())) {
                        terminal.disconnectTeleporter(teleporter.getId());
                        player.sendMessage(ChatColor.RED + "Телепорт '" + teleporterName + "' отключён!");
                    } else {
                        terminal.connectTeleporter(teleporter.getId());
                        player.sendMessage(ChatColor.GREEN + "Телепорт '" + teleporterName + "' подключён!");
                    }
                    terminalManager.saveTerminals();
                    TerminalGUI.openConnectionManagement(player, terminal);
                    return;
                }
            }
        }
    }
    
    private void handleTerminalCharging(Player player, TeleportTerminal terminal) {
        ItemStack redstone = null;
        int redstoneAmount = 0;
        
        // Ищем редстоун в инвентаре
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == Material.REDSTONE) {
                redstone = item;
                redstoneAmount = item.getAmount();
                break;
            }
        }
        
        if (redstone == null || redstoneAmount == 0) {
            player.sendMessage(ChatColor.RED + "У вас нет редстоуна для зарядки терминала!");
            return;
        }
        
        // Рассчитываем, сколько энергии можно добавить
        int maxEnergyToAdd = terminal.getMaxEnergy() - terminal.getEnergyLevel();
        int energyPerRedstone = 50;
        int maxRedstoneToUse = Math.min(redstoneAmount, maxEnergyToAdd / energyPerRedstone);
        
        if (maxRedstoneToUse == 0) {
            player.sendMessage(ChatColor.YELLOW + "Терминал уже полностью заряжен!");
            return;
        }
        
        // Используем редстоун
        int energyAdded = maxRedstoneToUse * energyPerRedstone;
        terminal.addEnergy(energyAdded);
        
        if (redstoneAmount == maxRedstoneToUse) {
            player.getInventory().remove(redstone);
        } else {
            redstone.setAmount(redstoneAmount - maxRedstoneToUse);
        }
        
        player.sendMessage(ChatColor.GREEN + "Терминал заряжен! Добавлено " + energyAdded + " энергии.");
        player.sendMessage(ChatColor.GRAY + "Использовано " + maxRedstoneToUse + " редстоуна.");
        
        terminalManager.saveTerminals();
        TerminalGUI.openTerminalManagement(player, terminal);
    }
    
    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        
        if (!renamingTerminals.containsKey(player.getUniqueId())) {
            return;
        }
        
        event.setCancelled(true);
        
        String oldName = renamingTerminals.remove(player.getUniqueId());
        String newName = event.getMessage().trim();
        
        if (newName.length() < 3 || newName.length() > 20) {
            player.sendMessage(ChatColor.RED + "Название должно быть от 3 до 20 символов!");
            return;
        }
        
        if (terminalManager.getTerminalByName(newName) != null) {
            player.sendMessage(ChatColor.RED + "Терминал с таким названием уже существует!");
            return;
        }
        
        TeleportTerminal terminal = terminalManager.getTerminalByName(oldName);
        if (terminal == null) {
            player.sendMessage(ChatColor.RED + "Терминал не найден!");
            return;
        }
        
        terminal.setName(newName);
        terminalManager.saveTerminals();
        
        player.sendMessage(ChatColor.GREEN + "Терминал переименован с '" + oldName + "' на '" + newName + "'!");
    }
    
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        TeleportTerminal terminal = terminalManager.getTerminalAt(block.getLocation());
        
        if (terminal == null) {
            return;
        }
        
        Player player = event.getPlayer();
        
        // Проверяем права на разрушение
        if (!terminal.getOwner().equals(player.getUniqueId()) && 
            !player.hasPermission("uraniumcraft.teleporter.admin")) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "Вы не можете разрушать блоки терминала '" + terminal.getName() + "'!");
            return;
        }
        
        // Если разрушается центральный блок (наблюдатель), деактивируем терминал
        if (block.getType() == Material.OBSERVER) {
            terminal.deactivate();
            player.sendMessage(ChatColor.YELLOW + "Терминал '" + terminal.getName() + "' деактивирован из-за повреждения!");
            terminalManager.saveTerminals();
        }
    }
}
