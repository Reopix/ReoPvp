package org.reopix.frpvp.listeners;

import lombok.RequiredArgsConstructor;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.reopix.frpvp.ReoPvp;
import org.reopix.frpvp.utils.Constants;

@RequiredArgsConstructor
public class ArmorEventListener implements Listener {
    private final ReoPvp plugin;
    
    @EventHandler
    public void onArmorDamage(PlayerItemDamageEvent event) {
        ItemStack item = event.getItem();
        Material material = item.getType();
        
        if (!Constants.ARMOR.contains(material)) {
            return;
        }
        
        if (!plugin.getDurabilityManager().shouldReduceArmorDurability()) {
            event.setCancelled(true);
            return;
        }
        
        event.setDamage(1);
    }
} 