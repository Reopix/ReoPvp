package org.reopix.frpvp.listeners;

import lombok.RequiredArgsConstructor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.reopix.frpvp.ReoPvp;
import org.reopix.frpvp.utils.Constants;

import java.util.UUID;

@RequiredArgsConstructor
public class WeaponEventListener implements Listener {
    private final ReoPvp plugin;
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) {
            return;
        }
        
        Player attacker = (Player) event.getDamager();
        ItemStack itemInHand = attacker.getInventory().getItemInMainHand();
        Material material = itemInHand.getType();
        
        if (!Constants.SWORDS.contains(material)) {
            return;
        }
        
        UUID attackerId = attacker.getUniqueId();
        
        if (plugin.getCombatManager().isOverCPSLimit(attackerId)) {
            event.setCancelled(true);
            return;
        }
        
        double customDamage = plugin.getConfigManager().getSwordDamage(material);
        if (customDamage > 0) {
            event.setDamage(customDamage);
        }
    }
} 