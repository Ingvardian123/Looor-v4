package com.uraniumcraft.listeners;

import com.uraniumcraft.UraniumPlugin;
import com.uraniumcraft.centrifuge.Centrifuge;
import com.uraniumcraft.centrifuge.CentrifugeManager;
import com.uraniumcraft.gui.CentrifugeGUI;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class CentrifugeGUIListener implements Listener {
    private final UraniumPlugin plugin;
    private final CentrifugeManager centrifugeManager;
    
    public CentrifugeGUIListener(UraniumPlugin plugin, CentrifugeManager centrifugeManager) {
        this.plugin = plugin;
        this.centrifugeManager = centrifugeManager;
    }
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getClickedBlock() == null) return;
        if (event.getClickedBlock().getType() != Material.DISPENSER) return;
        
        Player player = event.getPlayer();
        Centrifuge centrifuge = centrifugeManager.getCentrifuge(event.getClickedBlock().getLocation());
        
        if (centrifuge != null && centrifuge.getOwner().equals(player.getUniqueId())) {
            event.setCancelled(true);
            CentrifugeGUI.openCentrifugeMenu(player, centrifuge);
        }
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        
        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();
        
        if (!title.startsWith(ChatColor.DARK_GREEN + "Центрифуга")) return;
        
        event.setCancelled(true);
        
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;
        
        String displayName = clicked.getItemMeta().getDisplayName();
        
        if (displayName.equals(ChatColor.GREEN + "Запустить центрифугу")) {
            player.closeInventory();
            player.performCommand("centrifuge start");
        } else if (displayName.equals(ChatColor.RED + "Остановить центрифугу")) {
            // Логика остановки центрифуги
            player.closeInventory();
            player.sendMessage(ChatColor.YELLOW + "Центрифуга остановлена!");
        } else if (displayName.equals(ChatColor.BLUE + "Проверить структуру")) {
            player.closeInventory();
            player.performCommand("centrifuge debug");
        }
    }
}
