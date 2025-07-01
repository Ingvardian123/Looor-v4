package com.uraniumcraft.gui;

import com.uraniumcraft.centrifuge.Centrifuge;
import com.uraniumcraft.centrifuge.CentrifugeManager;
import com.uraniumcraft.items.UraniumItems;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class CentrifugeGUI {
    
    public static void openCentrifugeMenu(Player player, Centrifuge centrifuge) {
        Inventory gui = Bukkit.createInventory(null, 45, ChatColor.DARK_GREEN + "Центрифуга - Управление");
        
        // Информация о центрифуге
        ItemStack info = new ItemStack(Material.DISPENSER);
        ItemMeta infoMeta = info.getItemMeta();
        infoMeta.setDisplayName(ChatColor.AQUA + "Информация о центрифуге");
        
        if (centrifuge.isActive()) {
            long remaining = centrifuge.getRemainingTime();
            int minutes = (int) (remaining / 60000);
            int seconds = (int) ((remaining % 60000) / 1000);
            
            infoMeta.setLore(Arrays.asList(
                ChatColor.GRAY + "Статус: " + ChatColor.GREEN + "Работает",
                ChatColor.GRAY + "Осталось времени: " + ChatColor.YELLOW + minutes + "м " + seconds + "с",
                ChatColor.GRAY + "Прогресс: " + getProgressBar(centrifuge),
                ChatColor.GREEN + "Ожидаемый результат: 3x Урановая пыль"
            ));
        } else {
            infoMeta.setLore(Arrays.asList(
                ChatColor.GRAY + "Статус: " + ChatColor.RED + "Остановлена",
                ChatColor.GRAY + "Структура: " + (centrifuge.isValidStructure() ? 
                    ChatColor.GREEN + "Корректна" : ChatColor.RED + "Нарушена"),
                ChatColor.YELLOW + "Готова к запуску",
                ChatColor.GREEN + "Результат: 3x Урановая пыль"
            ));
        }
        
        info.setItemMeta(infoMeta);
        gui.setItem(4, info);
        
        // Кнопка запуска/остановки
        if (centrifuge.isActive()) {
            ItemStack stop = new ItemStack(Material.REDSTONE_BLOCK);
            ItemMeta stopMeta = stop.getItemMeta();
            stopMeta.setDisplayName(ChatColor.RED + "Остановить центрифугу");
            stopMeta.setLore(Arrays.asList(
                ChatColor.GRAY + "Принудительно остановить процесс",
                ChatColor.RED + "Прогресс будет потерян!"
            ));
            stop.setItemMeta(stopMeta);
            gui.setItem(20, stop);
        } else {
            ItemStack start = new ItemStack(Material.EMERALD_BLOCK);
            ItemMeta startMeta = start.getItemMeta();
            startMeta.setDisplayName(ChatColor.GREEN + "Запустить центрифугу");
            startMeta.setLore(Arrays.asList(
                ChatColor.GRAY + "Начать процесс центрифугирования",
                ChatColor.YELLOW + "Длительность: 5 минут",
                ChatColor.GREEN + "Результат: 3x Урановая пыль"
            ));
            start.setItemMeta(startMeta);
            gui.setItem(20, start);
        }
        
        // Проверка структуры
        ItemStack check = new ItemStack(Material.COMPASS);
        ItemMeta checkMeta = check.getItemMeta();
        checkMeta.setDisplayName(ChatColor.BLUE + "Проверить структуру");
        checkMeta.setLore(Arrays.asList(
            ChatColor.GRAY + "Диагностика структуры центрифуги",
            ChatColor.YELLOW + "Нажмите для подробной проверки"
        ));
        check.setItemMeta(checkMeta);
        gui.setItem(22, check);
        
        // Схема структуры
        ItemStack scheme = new ItemStack(Material.MAP);
        ItemMeta schemeMeta = scheme.getItemMeta();
        schemeMeta.setDisplayName(ChatColor.YELLOW + "Схема структуры");
        schemeMeta.setLore(Arrays.asList(
            ChatColor.WHITE + "  I C I",
            ChatColor.WHITE + "  C D C",
            ChatColor.WHITE + "  I C I",
            "",
            ChatColor.GRAY + "D = Диспенсер (центр)",
            ChatColor.BLUE + "C = Котёл с водой",
            ChatColor.DARK_GRAY + "I = Железный блок"
        ));
        scheme.setItemMeta(schemeMeta);
        gui.setItem(24, scheme);
        
        // Материалы для центрифуги
        addMaterialInfo(gui, 28, Material.DISPENSER, "Диспенсер", "Центральный блок центрифуги");
        addMaterialInfo(gui, 30, Material.WATER_CAULDRON, "Котёл с водой", "4 штуки по сторонам");
        addMaterialInfo(gui, 32, Material.IRON_BLOCK, "Железный блок", "4 штуки по углам");
        addMaterialInfo(gui, 34, Material.GUNPOWDER, "Урановая пыль", "Результат центрифугирования");
        
        // Заполняем пустые слоты
        fillEmptySlots(gui);
        
        player.openInventory(gui);
    }
    
    private static String getProgressBar(Centrifuge centrifuge) {
        long total = 5 * 60 * 1000; // 5 минут
        long remaining = centrifuge.getRemainingTime();
        long elapsed = total - remaining;
        
        int progress = (int) ((double) elapsed / total * 20); // 20 символов
        
        StringBuilder bar = new StringBuilder();
        bar.append(ChatColor.GREEN);
        for (int i = 0; i < 20; i++) {
            if (i < progress) {
                bar.append("█");
            } else {
                bar.append(ChatColor.GRAY).append("░");
            }
        }
        
        int percentage = (int) ((double) elapsed / total * 100);
        return bar.toString() + ChatColor.WHITE + " " + percentage + "%";
    }
    
    private static void addMaterialInfo(Inventory gui, int slot, Material material, String name, String description) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.YELLOW + name);
        meta.setLore(Arrays.asList(
            ChatColor.GRAY + description,
            ChatColor.DARK_GRAY + "Необходимо для работы центрифуги"
        ));
        item.setItemMeta(meta);
        gui.setItem(slot, item);
    }
    
    private static void fillEmptySlots(Inventory gui) {
        ItemStack filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
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
