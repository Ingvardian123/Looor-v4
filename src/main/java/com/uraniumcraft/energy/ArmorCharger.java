package com.uraniumcraft.energy;

import com.uraniumcraft.UraniumPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class ArmorCharger {
    
    private final UraniumPlugin plugin;
    
    public ArmorCharger(UraniumPlugin plugin) {
        this.plugin = plugin;
    }
    
    public ArmorCharger() {
        this.plugin = null;
    }
    
    public void chargeAllArmor() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            chargePlayerArmor(player);
        }
    }
    
    private void chargePlayerArmor(Player player) {
        // Логика зарядки брони игрока
    }
}
