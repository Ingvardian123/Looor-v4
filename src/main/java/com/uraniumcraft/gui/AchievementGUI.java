package com.uraniumcraft.gui;

import com.uraniumcraft.achievements.Achievement;
import com.uraniumcraft.achievements.AchievementCategory;
import com.uraniumcraft.achievements.AchievementManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class AchievementGUI {
    
    public static void openAchievementMenu(Player player) {
        Inventory gui = Bukkit.createInventory(null, 54, ChatColor.GOLD + "🏆 Достижения");
        
        // Статистика игрока
        ItemStack stats = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta statsMeta = stats.getItemMeta();
        statsMeta.setDisplayName(ChatColor.AQUA + "Ваша статистика");
        
        int playerAchievements = AchievementManager.getAchievementCount(player);
        int totalAchievements = AchievementManager.getTotalAchievements();
        double percentage = (double) playerAchievements / totalAchievements * 100;
        
        statsMeta.setLore(Arrays.asList(
            ChatColor.GRAY + "Игрок: " + ChatColor.WHITE + player.getName(),
            ChatColor.GRAY + "Получено: " + ChatColor.YELLOW + playerAchievements + "/" + totalAchievements,
            ChatColor.GRAY + "Прогресс: " + ChatColor.GREEN + String.format("%.1f%%", percentage),
            "",
            ChatColor.GOLD + "Продолжайте исследовать!"
        ));
        stats.setItemMeta(statsMeta);
        gui.setItem(4, stats);
        
        // Категории достижений
        int slot = 19;
        for (AchievementCategory category : AchievementCategory.values()) {
            ItemStack categoryItem = new ItemStack(category.getIcon());
            ItemMeta categoryMeta = categoryItem.getItemMeta();
            categoryMeta.setDisplayName(category.getColor() + category.getDisplayName());
            
            // Подсчёт достижений в категории
            int categoryAchievements = 0;
            int categoryTotal = 0;
            
            for (Achievement achievement : AchievementManager.getAllAchievements().values()) {
                if (achievement.getCategory() == category) {
                    categoryTotal++;
                    if (AchievementManager.hasAchievement(player, achievement.getId())) {
                        categoryAchievements++;
                    }
                }
            }
            
            categoryMeta.setLore(Arrays.asList(
                ChatColor.GRAY + "Достижения в категории",
                ChatColor.YELLOW + "Получено: " + categoryAchievements + "/" + categoryTotal,
                "",
                ChatColor.GREEN + "Нажмите для просмотра"
            ));
            categoryItem.setItemMeta(categoryMeta);
            gui.setItem(slot, categoryItem);
            slot++;
        }
        
        // Заполнить пустые слоты
        fillEmptySlots(gui, Material.YELLOW_STAINED_GLASS_PANE);
        
        player.openInventory(gui);
    }
    
    public static void openCategoryMenu(Player player, AchievementCategory category) {
        Inventory gui = Bukkit.createInventory(null, 54, 
            category.getColor() + category.getDisplayName() + " - Достижения");
        
        // Кнопка возврата
        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta backMeta = back.getItemMeta();
        backMeta.setDisplayName(ChatColor.GRAY + "← Назад");
        back.setItemMeta(backMeta);
        gui.setItem(45, back);
        
        // Достижения категории
        int slot = 0;
        for (Map.Entry<String, Achievement> entry : AchievementManager.getAllAchievements().entrySet()) {
            Achievement achievement = entry.getValue();
            
            if (achievement.getCategory() != category) {
                continue;
            }
            
            boolean hasAchievement = AchievementManager.hasAchievement(player, achievement.getId());
            
            ItemStack achievementItem;
            if (hasAchievement) {
                achievementItem = new ItemStack(Material.EMERALD);
            } else if (achievement.isSecret()) {
                achievementItem = new ItemStack(Material.BARRIER);
            } else {
                achievementItem = new ItemStack(Material.COAL);
            }
            
            ItemMeta achievementMeta = achievementItem.getItemMeta();
            
            if (achievement.isSecret() && !hasAchievement) {
                achievementMeta.setDisplayName(ChatColor.DARK_GRAY + "Секретное достижение");
                achievementMeta.setLore(Arrays.asList(
                    ChatColor.GRAY + "Это достижение скрыто",
                    ChatColor.GRAY + "Продолжайте играть, чтобы открыть его!"
                ));
            } else {
                ChatColor nameColor = hasAchievement ? ChatColor.GREEN : ChatColor.GRAY;
                achievementMeta.setDisplayName(nameColor + achievement.getName());
                
                List<String> lore = new ArrayList<>();
                lore.add(ChatColor.WHITE + achievement.getDescription());
                lore.add("");
                lore.add(ChatColor.GOLD + "Награда: +" + achievement.getExperienceReward() + " опыта");
                
                if (hasAchievement) {
                    lore.add("");
                    lore.add(ChatColor.GREEN + "✓ Получено!");
                } else {
                    lore.add("");
                    lore.add(ChatColor.RED + "✗ Не получено");
                }
                
                achievementMeta.setLore(lore);
            }
            
            achievementItem.setItemMeta(achievementMeta);
            gui.setItem(slot, achievementItem);
            slot++;
            
            if (slot >= 45) break; // Максимум 45 достижений на страницу
        }
        
        // Заполнить пустые слоты
        fillEmptySlots(gui, Material.GRAY_STAINED_GLASS_PANE);
        
        player.openInventory(gui);
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
