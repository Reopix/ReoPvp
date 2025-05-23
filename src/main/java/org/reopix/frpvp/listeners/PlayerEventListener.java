package org.reopix.frpvp.listeners;

import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.reopix.frpvp.ReoPvp;

@RequiredArgsConstructor
public class PlayerEventListener implements Listener {
    private final ReoPvp plugin;
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        plugin.getAttackSpeedService().applyAttackSpeedModifier(player);
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        plugin.getAttackSpeedService().resetAttackSpeedModifier(player);
        plugin.getCombatManager().removePlayer(player.getUniqueId());
    }
    
    @EventHandler
    public void onPlayerGainExperience(PlayerExpChangeEvent event) {
        int xpAmount = event.getAmount();
        Player player = event.getPlayer();
        
        if (plugin.getDurabilityManager().repairPlayerArmor(player, xpAmount)) {
            player.updateInventory();
        }
    }
} 