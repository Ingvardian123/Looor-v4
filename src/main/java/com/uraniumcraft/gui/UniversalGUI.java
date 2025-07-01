package com.uraniumcraft.gui;

import com.uraniumcraft.UraniumPlugin;
import com.uraniumcraft.centrifuge.Centrifuge;
import com.uraniumcraft.laboratory.Laboratory;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

public class UniversalGUI {
    
    public static void handleClick(Player player, InventoryClickEvent event, UraniumPlugin plugin) {
        event.setCancelled(true);
        // Обработка кликов в универсальном GUI
    }
    
    public static void openLaboratoryMenu(Player player, Laboratory laboratory) {
        // Открытие меню лаборатории
        LaboratoryGUI.openLaboratoryMenu(player, laboratory);
    }
    
    public static void openCentrifugeMenu(Player player, Centrifuge centrifuge) {
        // Открытие меню центрифуги
        CentrifugeGUI.openCentrifugeMenu(player, centrifuge);
    }
}
