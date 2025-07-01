package com.uraniumcraft.recipes;

import com.uraniumcraft.items.AdvancedItems;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.Plugin;

public class AdvancedRecipes {
    
    public static void registerRecipes(Plugin plugin) {
        registerLaboratoryBlockRecipe(plugin);
    }
    
    private static void registerLaboratoryBlockRecipe(Plugin plugin) {
        NamespacedKey key = new NamespacedKey(plugin, "laboratory_block");
        ShapedRecipe recipe = new ShapedRecipe(key, AdvancedItems.createLaboratoryBlock());
        
        recipe.shape(
            "IRI",
            "RBR", 
            "IRI"
        );
        
        recipe.setIngredient('I', Material.IRON_BLOCK);
        recipe.setIngredient('R', Material.REDSTONE_BLOCK);
        recipe.setIngredient('B', Material.BEACON);
        
        Bukkit.addRecipe(recipe);
    }
}
