package org.reopix.frpvp;

import lombok.extern.slf4j.Slf4j;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Slf4j
public final class ReoPvp extends JavaPlugin implements Listener {

    private ConfigManager configManager;
    private final ConcurrentMap<UUID, List<Long>> playerClicks = new ConcurrentHashMap<>();
    private final Random random = new Random();

    private static final double DEFAULT_ATTACK_SPEED = 4.0;
    private static final double NO_COOLDOWN_ATTACK_SPEED = 1024.0;

    @Override
    public void onEnable() {
        configManager = new ConfigManager(this);
        configManager.createDefaultConfig();
        getServer().getPluginManager().registerEvents(this, this);

        if (configManager.isAttackCooldownDisabled()) {
            getServer().getOnlinePlayers().forEach(this::removeAttackCooldown);
        }

        startCPSCleanupTask();
        log.info("ReoPvp enable");
    }

    @Override
    public void onDisable() {
        if (configManager.isAttackCooldownDisabled()) {
            getServer().getOnlinePlayers().forEach(this::resetAttackCooldown);
        }
        playerClicks.clear();
        log.info("ReoPvp disable");
    }

    private void startCPSCleanupTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                final long currentTime = System.currentTimeMillis();
                final long expirationTime = currentTime - 1000; // 1 секунда

                playerClicks.forEach((uuid, clicks) ->
                        clicks.removeIf(clickTime -> clickTime < expirationTime)
                );

                playerClicks.entrySet().removeIf(entry -> entry.getValue().isEmpty());
            }
        }.runTaskTimer(this, 20L, 20L);
    }

    private int recordClickAndGetCPS(@NotNull UUID playerId) {
        final long currentTime = System.currentTimeMillis();
        List<Long> clicks = playerClicks.computeIfAbsent(playerId, k -> new ArrayList<>());

        clicks.add(currentTime);
        clicks.removeIf(clickTime -> currentTime - clickTime > 1000);

        return clicks.size();
    }

    private void removeAttackCooldown(@NotNull Player player) {
        AttributeInstance attr = player.getAttribute(Attribute.GENERIC_ATTACK_SPEED);
        if (attr != null) {
            attr.setBaseValue(NO_COOLDOWN_ATTACK_SPEED);
        }
    }

    private void resetAttackCooldown(@NotNull Player player) {
        AttributeInstance attr = player.getAttribute(Attribute.GENERIC_ATTACK_SPEED);
        if (attr != null) {
            attr.setBaseValue(DEFAULT_ATTACK_SPEED);
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (configManager.isAttackCooldownDisabled()) {
            removeAttackCooldown(event.getPlayer());
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (configManager.isAttackCooldownDisabled()) {
            resetAttackCooldown(event.getPlayer());
        }
        playerClicks.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) {
            return;
        }

        Player attacker = (Player) event.getDamager();
        ItemStack itemInHand = attacker.getInventory().getItemInMainHand();
        Material material = itemInHand.getType();

        if (!ConfigManager.SWORDS.contains(material)) {
            return;
        }

        UUID attackerId = attacker.getUniqueId();
        int currentCPS = recordClickAndGetCPS(attackerId);
        int maxCPS = configManager.getMaxCPS();

        if (currentCPS > maxCPS) {
            event.setCancelled(true);
            return;
        }

        double customDamage = configManager.getSwordDamage(material);
        if (customDamage > 0) {
            event.setDamage(customDamage);
        }
    }

    @EventHandler
    public void onArmorDamage(PlayerItemDamageEvent event) {
        Material material = event.getItem().getType();

        if (ConfigManager.ARMOR.contains(material)) {
            double factor = configManager.getArmorDurabilityReductionFactor();

            if (random.nextDouble() > factor) {
                event.setCancelled(true);
                return;
            }

            event.setDamage(1);
        }
    }

    @EventHandler
    public void onPlayerGainExperience(PlayerExpChangeEvent event) {
        int xpAmount = event.getAmount();
        if (xpAmount <= 0) return;
        Player player = event.getPlayer();
        int repairAmount = Math.max(1, (int)(xpAmount * configManager.getArmorRepairXpFactor() / 100.0));
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

        if (repaired) player.updateInventory();
    }

    private boolean isArmorItem(ItemStack item) {
        return item != null && ConfigManager.ARMOR.contains(item.getType());
    }

    private boolean repairArmorItem(ItemStack item, int repairAmount) {
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
}
