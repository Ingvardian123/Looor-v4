package com.uraniumcraft.gui;

import com.uraniumcraft.radiation.RadiationManager;
import com.uraniumcraft.items.UraniumItems;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.UUID;

public class RadiationGUI {
    
    public static void openRadiationMenu(Player player, RadiationManager radiationManager) {
        Inventory gui = Bukkit.createInventory(null, 54, ChatColor.DARK_RED + "Радиационный контроль");
        
        UUID playerId = player.getUniqueId();
        int radiation = radiationManager.getPlayerRadiation(playerId);
        
        // Текущий уровень радиации
        ItemStack radiationLevel = new ItemStack(Material.REDSTONE);
        ItemMeta radiationMeta = radiationLevel.getItemMeta();
        radiationMeta.setDisplayName(ChatColor.RED + "Уровень радиации: " + radiation + "%");
        
        String status;
        ChatColor statusColor;
        if (radiation == 0) {
            status = "Норма";
            statusColor = ChatColor.GREEN;
        } else if (radiation < 20) {
            status = "Низкий";
            statusColor = ChatColor.YELLOW;
        } else if (radiation < 50) {
            status = "Средний";
            statusColor = ChatColor.GOLD;
        } else if (radiation < 80) {
            status = "Высокий";
            statusColor = ChatColor.RED;
        } else {
            status = "КРИТИЧЕСКИЙ";
            statusColor = ChatColor.DARK_RED;
        }
        
        radiationMeta.setLore(Arrays.asList(
            ChatColor.GRAY + "Статус: " + statusColor + status,
            ChatColor.GRAY + "Прогресс: " + getRadiationBar(radiation),
            "",
            getRadiationEffects(radiation)
        ));
        radiationLevel.setItemMeta(radiationMeta);
        gui.setItem(4, radiationLevel);
        
        // Детектор радиации
        ItemStack detector = UraniumItems.getItem("radiation_detector");
        if (detector != null) {
            ItemMeta detectorMeta = detector.getItemMeta();
            detectorMeta.setDisplayName(ChatColor.BLUE + "Детектор радиации");
            detectorMeta.setLore(Arrays.asList(
                ChatColor.GRAY + "Измеряет уровень радиации",
                ChatColor.YELLOW + "Текущие показания: " + radiation + "%",
                ChatColor.GREEN + "Нажмите для получения"
            ));
            detector.setItemMeta(detectorMeta);
            gui.setItem(20, detector);
        }
        
        // Таблетки от радиации
        ItemStack pills = UraniumItems.getItem("anti_radiation_pills");
        if (pills != null) {
            ItemMeta pillsMeta = pills.getItemMeta();
            pillsMeta.setDisplayName(ChatColor.WHITE + "Таблетки от радиации");
            pillsMeta.setLore(Arrays.asList(
                ChatColor.GRAY + "Снижает радиацию на 20 единиц",
                ChatColor.GREEN + "Восстанавливает здоровье",
                radiation > 0 ? ChatColor.GREEN + "Нажмите для получения" : ChatColor.GRAY + "Радиация в норме"
            ));
            pills.setItemMeta(pillsMeta);
            gui.setItem(22, pills);
        }
        
        // Защитная экипировка
        addProtectionInfo(gui, 28, Material.LEATHER_CHESTPLATE, "Кожаная броня", "10% защиты");
        addProtectionInfo(gui, 30, Material.IRON_CHESTPLATE, "Железная броня", "25% защиты");
        addProtectionInfo(gui, 32, Material.DIAMOND_CHESTPLATE, "Алмазная броня", "40% защиты");
        addProtectionInfo(gui, 34, Material.NETHERITE_CHESTPLATE, "Незеритовая броня", "60% защиты");
        
        // Специальная защита
        ItemStack hazmat = new ItemStack(Material.LEATHER_CHESTPLATE);
        ItemMeta hazmatMeta = hazmat.getItemMeta();
        hazmatMeta.setDisplayName(ChatColor.YELLOW + "Костюм химзащиты");
        hazmatMeta.setLore(Arrays.asList(
            ChatColor.GREEN + "100% защиты от радиации",
            ChatColor.GRAY + "Полная изоляция",
            ChatColor.YELLOW + "Доступен через исследования"
        ));
        hazmat.setItemMeta(hazmatMeta);
        gui.setItem(37, hazmat);
        
        ItemStack powerArmor = new ItemStack(Material.NETHERITE_CHESTPLATE);
        ItemMeta powerMeta = powerArmor.getItemMeta();
        powerMeta.setDisplayName(ChatColor.AQUA + "Силовая броня");
        powerMeta.setLore(Arrays.asList(
            ChatColor.GREEN + "95% защиты от радиации",
            ChatColor.BLUE + "Дополнительные способности",
            ChatColor.YELLOW + "Доступна через исследования"
        ));
        powerArmor.setItemMeta(powerMeta);
        gui.setItem(39, powerArmor);
        
        // Источники радиации
        addRadiationSource(gui, 46, Material.EMERALD_ORE, "Урановая руда", "5 единиц");
        addRadiationSource(gui, 47, Material.EMERALD, "Урановый слиток", "10 единиц");
        addRadiationSource(gui, 48, Material.PRISMARINE_CRYSTALS, "Обогащённый уран", "25 единиц");
        addRadiationSource(gui, 49, Material.BLAZE_ROD, "Топливный стержень", "50 единиц");
        addRadiationSource(gui, 50, Material.GUNPOWDER, "Урановая пыль", "8 единиц");
        
        fillEmptySlots(gui);
        player.openInventory(gui);
    }
    
    private static String getRadiationBar(int radiation) {
        int bars = radiation / 5; // 20 символов максимум
        
        StringBuilder bar = new StringBuilder();
        for (int i = 0; i < 20; i++) {
            if (i < bars) {
                if (radiation < 20) {
                    bar.append(ChatColor.YELLOW).append("█");
                } else if (radiation < 50) {
                    bar.append(ChatColor.GOLD).append("█");
                } else if (radiation < 80) {
                    bar.append(ChatColor.RED).append("█");
                } else {
                    bar.append(ChatColor.DARK_RED).append("█");
                }
            } else {
                bar.append(ChatColor.GRAY).append("░");
            }
        }
        
        return bar.toString();
    }
    
    private static String getRadiationEffects(int radiation) {
        if (radiation == 0) {
            return ChatColor.GREEN + "Никаких эффектов";
        } else if (radiation < 20) {
            return ChatColor.YELLOW + "Лёгкое недомогание";
        } else if (radiation < 40) {
            return ChatColor.GOLD + "Тошнота";
        } else if (radiation < 60) {
            return ChatColor.RED + "Тошнота, слабость";
        } else if (radiation < 80) {
            return ChatColor.RED + "Урон здоровью";
        } else {
            return ChatColor.DARK_RED + "Сильный урон, иссушение";
        }
    }
    
    private static void addProtectionInfo(Inventory gui, int slot, Material material, String name, String protection) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.BLUE + name);
        meta.setLore(Arrays.asList(
            ChatColor.GREEN + "Защита: " + protection,
            ChatColor.GRAY + "Снижает получаемую радиацию"
        ));
        item.setItemMeta(meta);
        gui.setItem(slot, item);
    }
    
    private static void addRadiationSource(Inventory gui, int slot, Material material, String name, String level) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.RED + name);
        meta.setLore(Arrays.asList(
            ChatColor.RED + "Радиация: " + level,
            ChatColor.GRAY + "Источник радиоактивного излучения"
        ));
        item.setItemMeta(meta);
        gui.setItem(slot, item);
    }
    
    private static void fillEmptySlots(Inventory gui) {
        ItemStack filler = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
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
