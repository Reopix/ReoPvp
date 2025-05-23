package org.reopix.frpvp.utils;

import lombok.experimental.UtilityClass;
import org.bukkit.Material;

import java.util.EnumSet;

@UtilityClass
public class Constants {
    public static final double DEFAULT_ATTACK_SPEED = 4.0;
    public static final double NO_COOLDOWN_ATTACK_SPEED = 1024.0;
    
    public static final long CPS_TRACKING_INTERVAL_MS = 1000;
    public static final long CLEANUP_TASK_TICKS = 20L;
    
    public static final EnumSet<Material> SWORDS = createSwordsSet();
    public static final EnumSet<Material> ARMOR = createArmorSet();
    
    private static EnumSet<Material> createSwordsSet() {
        EnumSet<Material> swords = EnumSet.of(
                Material.WOODEN_SWORD,
                Material.STONE_SWORD,
                Material.IRON_SWORD,
                Material.GOLDEN_SWORD,
                Material.DIAMOND_SWORD,
                Material.NETHERITE_SWORD,
                Material.TRIDENT
        );
        return swords;
    }
    
    private static EnumSet<Material> createArmorSet() {
        EnumSet<Material> armor = EnumSet.noneOf(Material.class);
        String[] materials = {"LEATHER", "CHAINMAIL", "IRON", "GOLDEN", "DIAMOND", "NETHERITE"};
        String[] types = {"HELMET", "CHESTPLATE", "LEGGINGS", "BOOTS"};
        
        for (String material : materials) {
            for (String type : types) {
                armor.add(Material.valueOf(material + "_" + type));
            }
        }
        
        return armor;
    }
} 