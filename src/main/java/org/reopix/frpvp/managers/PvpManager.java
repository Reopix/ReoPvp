package org.reopix.frpvp.managers;

import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.reopix.frpvp.ReoPvp;
import org.reopix.frpvp.utils.Constants;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class PvpManager {
    private final int maxCPS;
    private final ConcurrentMap<UUID, List<Long>> playerClicks = new ConcurrentHashMap<>();
    private BukkitTask cleanupTask;
    
    public PvpManager(int maxCPS) {
        this.maxCPS = maxCPS;
        startCleanupTask();
    }
    
    public int recordClickAndGetCPS(@NotNull UUID playerId) {
        final long currentTime = System.currentTimeMillis();
        List<Long> clicks = playerClicks.computeIfAbsent(playerId, k -> new ArrayList<>());
        
        clicks.add(currentTime);
        final long expirationTime = currentTime - Constants.CPS_TRACKING_INTERVAL_MS;
        clicks.removeIf(clickTime -> clickTime < expirationTime);
        
        return clicks.size();
    }
    
    public boolean isOverCPSLimit(@NotNull UUID playerId) {
        int currentCPS = recordClickAndGetCPS(playerId);
        return currentCPS > maxCPS;
    }
    
    public void removePlayer(@NotNull UUID playerId) {
        playerClicks.remove(playerId);
    }
    
    private void startCleanupTask() {
        cleanupTask = new BukkitRunnable() {
            @Override
            public void run() {
                final long currentTime = System.currentTimeMillis();
                final long expirationTime = currentTime - Constants.CPS_TRACKING_INTERVAL_MS;
                
                playerClicks.forEach((uuid, clicks) ->
                        clicks.removeIf(clickTime -> clickTime < expirationTime)
                );
                
                playerClicks.entrySet().removeIf(entry -> entry.getValue().isEmpty());
            }
        }.runTaskTimer(ReoPvp.getInstance().orElseThrow(), Constants.CLEANUP_TASK_TICKS, Constants.CLEANUP_TASK_TICKS);
    }
    
    public void shutdown() {
        if (cleanupTask != null) {
            cleanupTask.cancel();
        }
        playerClicks.clear();
    }
} 