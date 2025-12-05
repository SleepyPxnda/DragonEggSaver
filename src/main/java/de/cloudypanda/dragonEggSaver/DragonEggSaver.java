package de.cloudypanda.dragonEggSaver;

import de.cloudypanda.dragonEggSaver.events.EventListener;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public final class DragonEggSaver extends JavaPlugin {

    @Getter
    private static DragonEggManager dragonEggManager;

    @Getter
    private static DragonEggSaver instance;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        getServer().getPluginManager().registerEvents(new EventListener(), this);


        var playerIdString = getConfig().getString("currentPlayerId");
        var active = getConfig().getBoolean("currentLocation.active");
        Integer x = getConfig().getInt("currentLocation.x");
        Integer y = getConfig().getInt("currentLocation.y");
        Integer z = getConfig().getInt("currentLocation.z");
        String worldName = getConfig().getString("currentLocation.world");

        if (playerIdString != null && !playerIdString.isEmpty()) {
            try {
                var uuid = java.util.UUID.fromString(playerIdString);
                dragonEggManager = new DragonEggManager(uuid, false, 0,0,0,null);
            } catch (IllegalArgumentException e) {
                getLogger().warning("Invalid UUID string in config: " + playerIdString);
                dragonEggManager = new DragonEggManager(null, active, x, y, z, worldName);
            }
        } else {
            dragonEggManager = new DragonEggManager(null, active, x, y, z, worldName);
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                dragonEggManager.updateGlowing();
                dragonEggManager.updateCompassForOtherPlayers();
            }

        }.runTaskTimer(this, 0, 20); // Run every second (20 ticks)


    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
