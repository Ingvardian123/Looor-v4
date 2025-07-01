package com.uraniumcraft.recipes;

import com.uraniumcraft.UraniumPlugin;
import com.uraniumcraft.items.UraniumItems;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import com.uraniumcraft.items.AdvancedResearchItems;

public class UraniumRecipes {
   
   public static void registerRecipes(UraniumPlugin plugin) {
       registerBasicRecipes(plugin);
       registerArmorRecipes(plugin);
       registerMachineRecipes(plugin);
       registerToolRecipes(plugin);
   }

   private static void registerBasicRecipes(UraniumPlugin plugin) {
       // Урановый блок из слитков
       ShapedRecipe uraniumBlockRecipe = new ShapedRecipe(
           new NamespacedKey(plugin, "uranium_block"),
           UraniumItems.getItem("uranium_block")
       );
       uraniumBlockRecipe.shape("UUU", "UUU", "UUU");
       uraniumBlockRecipe.setIngredient('U', Material.PRISMARINE_SHARD); // Урановый слиток
       Bukkit.addRecipe(uraniumBlockRecipe);

       // Урановые слитки из блока
       ShapelessRecipe uraniumIngotsRecipe = new ShapelessRecipe(
           new NamespacedKey(plugin, "uranium_ingots_from_block"),
           UraniumItems.createUraniumIngot(9)
       );
       uraniumIngotsRecipe.addIngredient(Material.EMERALD_BLOCK); // Урановый блок
       Bukkit.addRecipe(uraniumIngotsRecipe);

       // Урановые слитки из пыли
       ShapedRecipe uraniumIngotFromDustRecipe = new ShapedRecipe(
           new NamespacedKey(plugin, "uranium_ingot_from_dust"),
           UraniumItems.getItem("uranium_ingot")
       );
       uraniumIngotFromDustRecipe.shape("DDD", "DDD", "DDD");
       uraniumIngotFromDustRecipe.setIngredient('D', Material.GUNPOWDER); // Урановая пыль
       Bukkit.addRecipe(uraniumIngotFromDustRecipe);

       // Дозиметр Гейгера
       ShapedRecipe geigerCounterRecipe = new ShapedRecipe(
           new NamespacedKey(plugin, "geiger_counter"),
           UraniumItems.getItem("geiger_counter")
       );
       geigerCounterRecipe.shape("IRI", "RCR", "IRI");
       geigerCounterRecipe.setIngredient('I', Material.IRON_INGOT);
       geigerCounterRecipe.setIngredient('R', Material.REDSTONE);
       geigerCounterRecipe.setIngredient('C', Material.CLOCK);
       Bukkit.addRecipe(geigerCounterRecipe);

       // Урановая капсула
       ShapedRecipe uraniumCapsuleRecipe = new ShapedRecipe(
           new NamespacedKey(plugin, "uranium_capsule"),
           UraniumItems.getItem("uranium_capsule")
       );
       uraniumCapsuleRecipe.shape("GGG", "GUG", "GGG");
       uraniumCapsuleRecipe.setIngredient('G', Material.GLASS);
       uraniumCapsuleRecipe.setIngredient('U', Material.PRISMARINE_SHARD); // Урановый слиток
       Bukkit.addRecipe(uraniumCapsuleRecipe);
   }
   
   private static void registerArmorRecipes(UraniumPlugin plugin) {
       // Костюм химзащиты - шлем
       ShapedRecipe hazmatHelmetRecipe = new ShapedRecipe(
           new NamespacedKey(plugin, "hazmat_helmet"),
           UraniumItems.getItem("hazmat_helmet")
       );
       hazmatHelmetRecipe.shape("LLL", "L L", "   ");
       hazmatHelmetRecipe.setIngredient('L', Material.LEATHER);
       Bukkit.addRecipe(hazmatHelmetRecipe);
       
       // Костюм химзащиты - нагрудник
       ShapedRecipe hazmatChestplateRecipe = new ShapedRecipe(
           new NamespacedKey(plugin, "hazmat_chestplate"),
           UraniumItems.getItem("hazmat_chestplate")
       );
       hazmatChestplateRecipe.shape("L L", "LLL", "LLL");
       hazmatChestplateRecipe.setIngredient('L', Material.LEATHER);
       Bukkit.addRecipe(hazmatChestplateRecipe);
       
       // Костюм химзащиты - поножи
       ShapedRecipe hazmatLeggingsRecipe = new ShapedRecipe(
           new NamespacedKey(plugin, "hazmat_leggings"),
           UraniumItems.getItem("hazmat_leggings")
       );
       hazmatLeggingsRecipe.shape("LLL", "L L", "L L");
       hazmatLeggingsRecipe.setIngredient('L', Material.LEATHER);
       Bukkit.addRecipe(hazmatLeggingsRecipe);
       
       // Костюм химзащиты - ботинки
       ShapedRecipe hazmatBootsRecipe = new ShapedRecipe(
           new NamespacedKey(plugin, "hazmat_boots"),
           UraniumItems.getItem("hazmat_boots")
       );
       hazmatBootsRecipe.shape("   ", "L L", "L L");
       hazmatBootsRecipe.setIngredient('L', Material.LEATHER);
       Bukkit.addRecipe(hazmatBootsRecipe);
       
       // Силовая броня - шлем
       ShapedRecipe powerArmorHelmetRecipe = new ShapedRecipe(
           new NamespacedKey(plugin, "power_armor_helmet"),
           UraniumItems.getItem("power_armor_helmet")
       );
       powerArmorHelmetRecipe.shape("III", "I I", "RDR");
       powerArmorHelmetRecipe.setIngredient('I', Material.IRON_INGOT);
       powerArmorHelmetRecipe.setIngredient('R', Material.REDSTONE);
       powerArmorHelmetRecipe.setIngredient('D', Material.DIAMOND);
       Bukkit.addRecipe(powerArmorHelmetRecipe);
       
       // Силовая броня - нагрудник
       ShapedRecipe powerArmorChestplateRecipe = new ShapedRecipe(
           new NamespacedKey(plugin, "power_armor_chestplate"),
           UraniumItems.getItem("power_armor_chestplate")
       );
       powerArmorChestplateRecipe.shape("I I", "IDI", "IRB");
       powerArmorChestplateRecipe.setIngredient('I', Material.IRON_INGOT);
       powerArmorChestplateRecipe.setIngredient('R', Material.REDSTONE_BLOCK);
       powerArmorChestplateRecipe.setIngredient('D', Material.DIAMOND);
       powerArmorChestplateRecipe.setIngredient('B', Material.BEACON);
       Bukkit.addRecipe(powerArmorChestplateRecipe);
       
       // Силовая броня - поножи
       ShapedRecipe powerArmorLeggingsRecipe = new ShapedRecipe(
           new NamespacedKey(plugin, "power_armor_leggings"),
           UraniumItems.getItem("power_armor_leggings")
       );
       powerArmorLeggingsRecipe.shape("IDI", "I I", "R R");
       powerArmorLeggingsRecipe.setIngredient('I', Material.IRON_INGOT);
       powerArmorLeggingsRecipe.setIngredient('R', Material.REDSTONE);
       powerArmorLeggingsRecipe.setIngredient('D', Material.DIAMOND);
       Bukkit.addRecipe(powerArmorLeggingsRecipe);
       
       // Силовая броня - ботинки
       ShapedRecipe powerArmorBootsRecipe = new ShapedRecipe(
           new NamespacedKey(plugin, "power_armor_boots"),
           UraniumItems.getItem("power_armor_boots")
       );
       powerArmorBootsRecipe.shape("   ", "I I", "RDR");
       powerArmorBootsRecipe.setIngredient('I', Material.IRON_INGOT);
       powerArmorBootsRecipe.setIngredient('R', Material.REDSTONE);
       powerArmorBootsRecipe.setIngredient('D', Material.DIAMOND);
       Bukkit.addRecipe(powerArmorBootsRecipe);
   }
   
   private static void registerMachineRecipes(UraniumPlugin plugin) {
       // Ядро центрифуги
       ShapedRecipe centrifugeCoreRecipe = new ShapedRecipe(
           new NamespacedKey(plugin, "centrifuge_core"),
           UraniumItems.getItem("centrifuge_core")
       );
       centrifugeCoreRecipe.shape("IRI", "RDR", "IUI");
       centrifugeCoreRecipe.setIngredient('I', Material.IRON_BLOCK);
       centrifugeCoreRecipe.setIngredient('R', Material.REDSTONE_BLOCK);
       centrifugeCoreRecipe.setIngredient('D', Material.DISPENSER);
       centrifugeCoreRecipe.setIngredient('U', Material.PRISMARINE_SHARD); // Урановый слиток
       Bukkit.addRecipe(centrifugeCoreRecipe);
       
       // Терминал лаборатории
       ShapedRecipe laboratoryTerminalRecipe = new ShapedRecipe(
           new NamespacedKey(plugin, "laboratory_terminal"),
           UraniumItems.getItem("laboratory_terminal")
       );
       laboratoryTerminalRecipe.shape("RUR", "UEU", "RDR");
       laboratoryTerminalRecipe.setIngredient('R', Material.REDSTONE_BLOCK);
       laboratoryTerminalRecipe.setIngredient('U', Material.PRISMARINE_SHARD); // Урановый слиток
       laboratoryTerminalRecipe.setIngredient('E', Material.ENCHANTING_TABLE);
       laboratoryTerminalRecipe.setIngredient('D', Material.DIAMOND_BLOCK);
       Bukkit.addRecipe(laboratoryTerminalRecipe);
       
       // Ядро телепорта
       ShapedRecipe teleporterCoreRecipe = new ShapedRecipe(
           new NamespacedKey(plugin, "teleporter_core"),
           UraniumItems.getItem("teleporter_core")
       );
       teleporterCoreRecipe.shape("EUE", "UBU", "EUE");
       teleporterCoreRecipe.setIngredient('E', Material.ENDER_PEARL);
       teleporterCoreRecipe.setIngredient('U', Material.PRISMARINE_SHARD); // Урановый слиток
       teleporterCoreRecipe.setIngredient('B', Material.BEACON);
       Bukkit.addRecipe(teleporterCoreRecipe);
   }
   
   private static void registerToolRecipes(UraniumPlugin plugin) {
       // Рельсотрон
       ShapedRecipe railgunRecipe = new ShapedRecipe(
           new NamespacedKey(plugin, "railgun"),
           UraniumItems.getItem("railgun")
       );
       railgunRecipe.shape("IUI", "RCR", "IDI");
       railgunRecipe.setIngredient('I', Material.IRON_BLOCK);
       railgunRecipe.setIngredient('U', Material.PRISMARINE_SHARD); // Урановый слиток
       railgunRecipe.setIngredient('R', Material.REDSTONE_BLOCK);
       railgunRecipe.setIngredient('C', Material.CROSSBOW);
       railgunRecipe.setIngredient('D', Material.DIAMOND);
       Bukkit.addRecipe(railgunRecipe);
       
       // Урановый планшет
       ShapedRecipe uraniumTabletRecipe = new ShapedRecipe(
           new NamespacedKey(plugin, "uranium_tablet"),
           UraniumItems.getItem("uranium_tablet")
       );
       uraniumTabletRecipe.shape("RGR", "UCU", "RDR");
       uraniumTabletRecipe.setIngredient('R', Material.REDSTONE);
       uraniumTabletRecipe.setIngredient('G', Material.GLASS);
       uraniumTabletRecipe.setIngredient('U', Material.PRISMARINE_SHARD); // Урановый слиток
       uraniumTabletRecipe.setIngredient('C', Material.RECOVERY_COMPASS);
       uraniumTabletRecipe.setIngredient('D', Material.DIAMOND);
       Bukkit.addRecipe(uraniumTabletRecipe);
   }

    static {
        // Квантовый процессор
        ShapedRecipe quantumProcessorRecipe = new ShapedRecipe(
                new NamespacedKey(UraniumPlugin.getPlugin(), "quantum_processor"),
                AdvancedResearchItems.get("quantum_processor")
        );
        quantumProcessorRecipe.shape("RUR", "UNU", "RUR");
        quantumProcessorRecipe.setIngredient('R', Material.REDSTONE_BLOCK);
        quantumProcessorRecipe.setIngredient('U', Material.PRISMARINE_SHARD); // Урановый слиток
        quantumProcessorRecipe.setIngredient('N', Material.NETHER_STAR);
        Bukkit.addRecipe(quantumProcessorRecipe);

        // Квантовый телепорт
        ShapedRecipe quantumTeleporterRecipe = new ShapedRecipe(
                new NamespacedKey(UraniumPlugin.getPlugin(), "quantum_teleporter"),
                AdvancedResearchItems.get("teleporter")
        );
        quantumTeleporterRecipe.shape("EUE", "UPU", "EUE");
        quantumTeleporterRecipe.setIngredient('E', Material.ENDER_PEARL);
        quantumTeleporterRecipe.setIngredient('U', Material.PRISMARINE_SHARD); // Урановый слиток
        quantumTeleporterRecipe.setIngredient('P', Material.END_PORTAL_FRAME);
        Bukkit.addRecipe(quantumTeleporterRecipe);
    }
}
