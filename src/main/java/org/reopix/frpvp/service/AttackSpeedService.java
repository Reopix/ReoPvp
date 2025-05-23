package org.reopix.frpvp.service;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class AttackSpeedService {
    private final boolean attackCooldownDisabled;
    private final double defaultAttackSpeed;
    private final double noCooldownAttackSpeed;
    
    public AttackSpeedService(boolean attackCooldownDisabled, double defaultAttackSpeed, double noCooldownAttackSpeed) {
        this.attackCooldownDisabled = attackCooldownDisabled;
        this.defaultAttackSpeed = defaultAttackSpeed;
        this.noCooldownAttackSpeed = noCooldownAttackSpeed;
    }
    
    public void applyAttackSpeedModifier(@NotNull Player player) {
        if (!attackCooldownDisabled) return;
        
        AttributeInstance attr = player.getAttribute(Attribute.GENERIC_ATTACK_SPEED);
        if (attr != null) {
            attr.setBaseValue(noCooldownAttackSpeed);
        }
    }
    
    public void resetAttackSpeedModifier(@NotNull Player player) {
        if (!attackCooldownDisabled) return;
        
        AttributeInstance attr = player.getAttribute(Attribute.GENERIC_ATTACK_SPEED);
        if (attr != null) {
            attr.setBaseValue(defaultAttackSpeed);
        }
    }
} 