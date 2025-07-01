package com.uraniumcraft.recipes;

import com.uraniumcraft.items.UraniumTablet;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;

public class TabletRecipes {
    
    public static void registerTabletRecipes() {
        registerBasicTabletRecipe();
        registerAdvancedTabletRecipe();
    }
    
    private static void registerBasicTabletRecipe() {
        ItemStack tablet = UraniumTablet.createTablet();
        NamespacedKey key = new NamespacedKey("uraniumcraft", "uranium_tablet");
        
        ShapedRecipe recipe = new ShapedRecipe(key, tablet);
        recipe.shape(
            "IGI",
            "RMR", 
            "IDI"
        );
        
        recipe.setIngredient('I', Material.IRON_INGOT);
        recipe.setIngredient('G', Material.GLASS);
        recipe.setIngredient('R', Material.REDSTONE);
        recipe.setIngredient('M', Material.MAP);
        recipe.setIngredient('D', Material.DIAMOND);
        
        Bukkit.addRecipe(recipe);
    }
    
    private static void registerAdvancedTabletRecipe() {
        ItemStack advancedTablet = UraniumTablet.createAdvancedTablet();
        NamespacedKey key = new NamespacedKey("uraniumcraft", "advanced_uranium_tablet");
        
        ShapedRecipe recipe = new ShapedRecipe(key, advancedTablet);
        recipe.shape(
            "EGE",
            "NTN",
            "EDE"
        );
        
        recipe.setIngredient('E', Material.EMERALD);
        recipe.setIngredient('G', Material.GLASS);
        recipe.setIngredient('N', Material.NETHERITE_INGOT);
        recipe.setIngredient('T', UraniumTablet.createTablet().getType()); // Обычный планшет
        recipe.setIngredient('D', Material.DIAMOND_BLOCK);
        
        Bukkit.addRecipe(recipe);
    }
}
