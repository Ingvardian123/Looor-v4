package com.uraniumcraft.listeners;

import com.uraniumcraft.UraniumPlugin;
import com.uraniumcraft.gui.*;
import com.uraniumcraft.items.GuideBook;
import com.uraniumcraft.items.UraniumItems;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class MainGUIListener implements Listener {
    private final UraniumPlugin plugin;
    
    public MainGUIListener(UraniumPlugin plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        
        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();
        
        if (title.startsWith(ChatColor.DARK_BLUE + "UraniumCraft - Главное меню")) {
            handleMainMenuClick(player, event);
        } else if (title.startsWith(ChatColor.DARK_RED + "Радиационный контроль")) {
            handleRadiationMenuClick(player, event);
        } else if (title.startsWith(ChatColor.DARK_PURPLE + "Продвинутые предметы")) {
            handleAdvancedItemsClick(player, event);
        } else if (title.startsWith(ChatColor.GOLD + "Статистика игрока")) {
            event.setCancelled(true); // Только просмотр
        }
    }
    
    private void handleMainMenuClick(Player player, InventoryClickEvent event) {
        event.setCancelled(true);
        
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;
        
        String displayName = clicked.getItemMeta().getDisplayName();
        
        if (displayName.equals(ChatColor.RED + "Радиационный контроль")) {
            RadiationGUI.openRadiationMenu(player, plugin.getRadiationManager());
        } else if (displayName.equals(ChatColor.LIGHT_PURPLE + "Лаборатории")) {
            // Открываем GUI лабораторий или сообщаем о необходимости авторизации
            if (plugin.getLaboratoryManager().isPlayerAuthorized(player.getUniqueId())) {
                player.closeInventory();
                player.sendMessage(ChatColor.YELLOW + "Постройте лабораторию для доступа к GUI!");
            } else {
                player.sendMessage(ChatColor.RED + "Нужна авторизация администратора!");
            }
        } else if (displayName.equals(ChatColor.DARK_GREEN + "Центрифуга")) {
            player.closeInventory();
            player.sendMessage(ChatColor.YELLOW + "Постройте центрифугу и кликните ПКМ по диспенсеру!");
        } else if (displayName.equals(ChatColor.AQUA + "Продвинутые предметы")) {
            AdvancedItemsGUI.openAdvancedItemsMenu(player, plugin.getLaboratoryManager());
        } else if (displayName.equals(ChatColor.GOLD + "Статистика")) {
            StatsGUI.openStatsMenu(player);
        } else if (displayName.equals(ChatColor.YELLOW + "Руководство")) {
            ItemStack guide = GuideBook.createGuideBook();
            player.getInventory().addItem(guide);
            player.sendMessage(ChatColor.GREEN + "Руководство добавлено в инвентарь!");
            player.closeInventory();
        } else if (displayName.equals(ChatColor.GREEN + "Детектор радиации")) {
            ItemStack detector = UraniumItems.getItem("radiation_detector");
            if (detector != null) {
                player.getInventory().addItem(detector);
                player.sendMessage(ChatColor.GREEN + "Детектор радиации получен!");
            }
            player.closeInventory();
        } else if (displayName.equals(ChatColor.GREEN + "Таблетки")) {
            ItemStack pills = UraniumItems.getItem("anti_radiation_pills");
            if (pills != null) {
                player.getInventory().addItem(pills);
                player.sendMessage(ChatColor.GREEN + "Таблетки от радиации получены!");
            }
            player.closeInventory();
        }
    }
    
    private void handleRadiationMenuClick(Player player, InventoryClickEvent event) {
        event.setCancelled(true);
        
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;
        
        String displayName = clicked.getItemMeta().getDisplayName();
        
        if (displayName.equals(ChatColor.BLUE + "Детектор радиации")) {
            ItemStack detector = UraniumItems.getItem("radiation_detector");
            if (detector != null) {
                player.getInventory().addItem(detector);
                player.sendMessage(ChatColor.GREEN + "Детектор радиации получен!");
            }
            player.closeInventory();
        } else if (displayName.equals(ChatColor.WHITE + "Таблетки от радиации")) {
            int radiation = plugin.getRadiationManager().getPlayerRadiation(player.getUniqueId());
            if (radiation > 0) {
                ItemStack pills = UraniumItems.getItem("anti_radiation_pills");
                if (pills != null) {
                    player.getInventory().addItem(pills);
                    player.sendMessage(ChatColor.GREEN + "Таблетки от радиации получены!");
                }
            } else {
                player.sendMessage(ChatColor.YELLOW + "Ваша радиация в норме!");
            }
            player.closeInventory();
        }
    }
    
    private void handleAdvancedItemsClick(Player player, InventoryClickEvent event) {
        event.setCancelled(true);
        
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;
        
        // Проверяем, доступен ли предмет для создания
        if (clicked.getItemMeta().getLore().contains(ChatColor.GREEN + "✓ Исследовано")) {
            // Выдаём предмет игроку
            ItemStack item = clicked.clone();
            item.getItemMeta().setLore(null); // Убираем лор
            player.getInventory().addItem(item);
            player.sendMessage(ChatColor.GREEN + "Предмет создан!");
            player.closeInventory();
        } else {
            player.sendMessage(ChatColor.RED + "Предмет не исследован!");
        }
    }
}
