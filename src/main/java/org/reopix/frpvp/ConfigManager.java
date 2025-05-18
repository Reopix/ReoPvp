package org.reopix.frpvp;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;

@Slf4j
public class ConfigManager {
    private final JavaPlugin plugin;
    private FileConfiguration config;
    
    @Getter private double armorDurabilityReductionFactor = 0.01;
    @Getter private boolean attackCooldownDisabled = true;
    @Getter private int maxCPS = 15;
    @Getter private final Map<Material, Double> swordDamageMap = new EnumMap<>(Material.class);
    
    public static final EnumSet<Material> SWORDS = createSwordsSet();
    
    private static EnumSet<Material> createSwordsSet() {
        EnumSet<Material> swordSet = EnumSet.noneOf(Material.class);
        String[] materials = {"WOODEN", "STONE", "IRON", "GOLDEN", "DIAMOND", "NETHERITE"};
        
        for (String material : materials) {
            swordSet.add(Material.valueOf(material + "_SWORD"));
        }
        swordSet.add(Material.TRIDENT);
        
        return swordSet;
    }
    
    public static final EnumSet<Material> ARMOR = createArmorSet();
    
    private static EnumSet<Material> createArmorSet() {
        EnumSet<Material> armorSet = EnumSet.noneOf(Material.class);
        String[] materials = {"LEATHER", "CHAINMAIL", "IRON", "GOLDEN", "DIAMOND", "NETHERITE"};
        String[] types = {"HELMET", "CHESTPLATE", "LEGGINGS", "BOOTS"};
        
        for (String material : materials) {
            for (String type : types) {
                armorSet.add(Material.valueOf(material + "_" + type));
            }
        }
        
        return armorSet;
    }

    private static final Map<Material, Double> DEFAULT_SWORD_DAMAGE;
    
    static {
        Map<Material, Double> map = new EnumMap<>(Material.class);
        map.put(Material.WOODEN_SWORD, 4.0);
        map.put(Material.STONE_SWORD, 5.0);
        map.put(Material.IRON_SWORD, 6.0);
        map.put(Material.GOLDEN_SWORD, 4.0);
        map.put(Material.DIAMOND_SWORD, 7.0);
        map.put(Material.NETHERITE_SWORD, 8.0);
        map.put(Material.TRIDENT, 7.0);
        DEFAULT_SWORD_DAMAGE = map;
    }
    
    public ConfigManager(@NotNull JavaPlugin plugin) {
        this.plugin = plugin;
        loadConfig();
    }
    
    public void loadConfig() {
        plugin.saveDefaultConfig();
        config = plugin.getConfig();
        
        armorDurabilityReductionFactor = config.getDouble("armor-durability-reduction-factor", 0.01);
        attackCooldownDisabled = config.getBoolean("disable-attack-cooldown", true);
        maxCPS = config.getInt("max-cps", 15);
        loadSwordDamage();
    }
    
    private void loadSwordDamage() {
        swordDamageMap.clear();
        swordDamageMap.putAll(DEFAULT_SWORD_DAMAGE);
        
        if (config.isConfigurationSection("sword-damage")) {
            for (Material sword : SWORDS) {
                String key = "sword-damage." + getSwordConfigKey(sword);
                if (config.contains(key)) {
                    swordDamageMap.put(sword, config.getDouble(key));
                }
            }
        }
    }
    
    private String getSwordConfigKey(Material sword) {
        if (sword == Material.TRIDENT) {
            return "trident";
        }
        return sword.name().toLowerCase().replace("_sword", "");
    }
    
    public void createDefaultConfig() {
        config = plugin.getConfig();
        config.addDefault("armor-durability-reduction-factor", 0.01);
        config.addDefault("disable-attack-cooldown", true);
        config.addDefault("max-cps", 15);
        
        for (Material sword : SWORDS) {
            String key = "sword-damage." + getSwordConfigKey(sword);
            config.addDefault(key, DEFAULT_SWORD_DAMAGE.get(sword));
        }
        
        config.options().copyDefaults(true);
        plugin.saveConfig();
    }
    
    public double getSwordDamage(@Nullable Material material) {
        if (material != null && SWORDS.contains(material)) {
            return swordDamageMap.getOrDefault(material, 0.0);
        }
        return 0.0;
    }

    public boolean isAttackCooldownDisabled() {
        return attackCooldownDisabled;
    }
    
    public int getMaxCPS() {
        return maxCPS;
    }
    
    public double getArmorDurabilityReductionFactor() {
        return armorDurabilityReductionFactor;
    }
} 