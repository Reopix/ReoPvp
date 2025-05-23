package org.reopix.frpvp.config;

import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.reopix.frpvp.utils.Constants;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

public class ConfigurationManager {
    private final JavaPlugin plugin;
    private FileConfiguration config;
    
    @Getter private double armorDurabilityReductionFactor;
    @Getter private double armorRepairXpFactor;
    @Getter private boolean attackCooldownDisabled;
    @Getter private int maxCPS;
    @Getter private final Map<Material, Double> swordDamageMap = new EnumMap<>(Material.class);

    private static final Map<Material, Double> DEFAULT_SWORD_DAMAGE = createDefaultSwordDamageMap();
    
    private static Map<Material, Double> createDefaultSwordDamageMap() {
        Map<Material, Double> map = new EnumMap<>(Material.class);
        map.put(Material.WOODEN_SWORD, 4.0);
        map.put(Material.STONE_SWORD, 5.0);
        map.put(Material.IRON_SWORD, 6.0);
        map.put(Material.GOLDEN_SWORD, 4.0);
        map.put(Material.DIAMOND_SWORD, 7.0);
        map.put(Material.NETHERITE_SWORD, 8.0);
        map.put(Material.TRIDENT, 7.0);
        return map;
    }
    
    public ConfigurationManager(@NotNull JavaPlugin plugin) {
        this.plugin = plugin;
    }
    
    public void loadConfig() {
        plugin.saveDefaultConfig();
        config = plugin.getConfig();
        
        armorDurabilityReductionFactor = config.getDouble("armor-durability-reduction-factor", 0.01);
        armorRepairXpFactor = config.getDouble("armor-repair-xp-factor", 1.0);
        attackCooldownDisabled = config.getBoolean("disable-attack-cooldown", true);
        maxCPS = config.getInt("max-cps", 15);
        
        loadSwordDamage();
    }
    
    private void loadSwordDamage() {
        swordDamageMap.clear();
        swordDamageMap.putAll(DEFAULT_SWORD_DAMAGE);
        
        if (config.isConfigurationSection("sword-damage")) {
            for (Material sword : Constants.SWORDS) {
                String key = "sword-damage." + getSwordConfigKey(sword);
                if (config.contains(key)) {
                    swordDamageMap.put(sword, config.getDouble(key));
                }
            }
        }
    }
    
    private String getSwordConfigKey(Material sword) {
        return sword == Material.TRIDENT ? 
               "trident" : 
               sword.name().toLowerCase().replace("_sword", "");
    }
    
    public double getSwordDamage(@Nullable Material material) {
        return Optional.ofNullable(material)
                .filter(Constants.SWORDS::contains)
                .map(swordDamageMap::get)
                .orElse(0.0);
    }
} 