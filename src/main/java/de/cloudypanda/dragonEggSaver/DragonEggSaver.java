package de.cloudypanda.dragonEggSaver;

import de.cloudypanda.dragonEggSaver.events.EventListener;
import lombok.Getter;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public final class DragonEggSaver extends JavaPlugin {

    @Getter
    private static final DragonEggManager dragonEggManager = new DragonEggManager();

    @Override
    public void onEnable() {
        PluginManager pm = getServer().getPluginManager();

        pm.registerEvents(new EventListener(), this);

        new BukkitRunnable() {
            @Override
            public void run() {
                dragonEggManager.updateGlowing();
            }

        }.runTaskTimer(this, 0, 20); // Run every second (20 ticks)
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
