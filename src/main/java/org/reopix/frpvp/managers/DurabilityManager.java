package org.reopix.frpvp.managers;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.reopix.frpvp.utils.Constants;

import java.util.Random;

public class DurabilityManager {
    private final double armorDurabilityReductionFactor;
    private final double armorRepairXpFactor;
    private final Random random = new Random();
    
    public DurabilityManager(double armorDurabilityReductionFactor, double armorRepairXpFactor) {
        this.armorDurabilityReductionFactor = armorDurabilityReductionFactor;
        this.armorRepairXpFactor = armorRepairXpFactor;
    }
    
    public boolean shouldReduceArmorDurability() {
        return random.nextDouble() <= armorDurabilityReductionFactor;
    }
    
    public int calculateRepairAmount(int xpAmount) {
        return Math.max(1, (int)(xpAmount * armorRepairXpFactor / 100.0));
    }
    
    public boolean isArmorItem(ItemStack item) {
        return item != null && !item.getType().isAir() && Constants.ARMOR.contains(item.getType());
    }
    
    public boolean repairArmorItem(ItemStack item, int repairAmount) {
        if (item == null || !item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        if (!(meta instanceof Damageable)) return false;
        
        Damageable damageable = (Damageable) meta;
        int damage = damageable.getDamage();
        if (damage <= 0) return false;
        
        damageable.setDamage(Math.max(0, damage - repairAmount));
        item.setItemMeta(meta);

        return true;
    }
    
    public boolean repairPlayerArmor(Player player, int xpAmount) {
        if (xpAmount <= 0) return false;
        
        int repairAmount = calculateRepairAmount(xpAmount);
        ItemStack[] armorItems = player.getInventory().getArmorContents();
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        ItemStack offHand = player.getInventory().getItemInOffHand();
        boolean repaired = false;
        
        for (ItemStack item : armorItems) {
            if (isArmorItem(item)) {
                repaired |= repairArmorItem(item, repairAmount);
            }
        }
        
        if (isArmorItem(mainHand)) repaired |= repairArmorItem(mainHand, repairAmount);
        if (isArmorItem(offHand)) repaired |= repairArmorItem(offHand, repairAmount);
        
        return repaired;
    }
} 