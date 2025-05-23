package org.reopix.frpvp;

import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;
import org.reopix.frpvp.config.ConfigurationManager;
import org.reopix.frpvp.listeners.ArmorEventListener;
import org.reopix.frpvp.listeners.PlayerEventListener;
import org.reopix.frpvp.listeners.WeaponEventListener;
import org.reopix.frpvp.managers.PvpManager;
import org.reopix.frpvp.managers.DurabilityManager;
import org.reopix.frpvp.service.AttackSpeedService;
import org.reopix.frpvp.utils.Constants;

import java.util.Optional;

public final class ReoPvp extends JavaPlugin {

    @Getter
    private static ReoPvp instance;
    
    @Getter
    private ConfigurationManager configManager;
    
    @Getter
    private PvpManager combatManager;
    
    @Getter
    private DurabilityManager durabilityManager;
    
    @Getter
    private AttackSpeedService attackSpeedService;

    @Override
    public void onEnable() {
        instance = this;
        
        initManagers();
        initServices();
        registerEventListeners();
        getServer().getOnlinePlayers().forEach(attackSpeedService::applyAttackSpeedModifier);
    }

    @Override
    public void onDisable() {
        getServer().getOnlinePlayers().forEach(attackSpeedService::resetAttackSpeedModifier);
        combatManager.shutdown();
    }
    
    private void initManagers() {
        configManager = new ConfigurationManager(this);
        configManager.loadConfig();
        
        combatManager = new PvpManager(configManager.getMaxCPS());
        durabilityManager = new DurabilityManager(
                configManager.getArmorDurabilityReductionFactor(),
                configManager.getArmorRepairXpFactor()
        );
    }
    
    private void initServices() {
        attackSpeedService = new AttackSpeedService(
                configManager.isAttackCooldownDisabled(),
                Constants.DEFAULT_ATTACK_SPEED,
                Constants.NO_COOLDOWN_ATTACK_SPEED
        );
    }
    
    private void registerEventListeners() {
        registerEvents(
                new PlayerEventListener(this),
                new WeaponEventListener(this),
                new ArmorEventListener(this)
        );
    }
    
    private void registerEvents(org.bukkit.event.Listener... listeners) {
        for (org.bukkit.event.Listener listener : listeners) {
            getServer().getPluginManager().registerEvents(listener, this);
        }
    }
    
    public static Optional<ReoPvp> getInstance() {
        return Optional.ofNullable(instance);
    }
}
