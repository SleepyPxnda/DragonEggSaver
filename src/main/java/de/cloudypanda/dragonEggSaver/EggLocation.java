package de.cloudypanda.dragonEggSaver;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.UUID;

class EggHolder<T> {
    @Getter @Setter private T holder = null;
    @Getter @Setter private boolean active = false;
}

@Slf4j
public class EggLocation {

    @Getter
    private EggHolder<Player> player;

    @Getter
    private EggHolder<Location> location;

    public EggLocation() {
        this.player = new EggHolder<>();
        this.location = new EggHolder<>();
    }

    public Location getCurrentLocation() {
        if (this.player.isActive()) {
            return this.player.getHolder().getLocation();
        }

        if(location != null) {
            return this.location.getHolder();
        }

        return null;
    }

    public void updatePlayerEggLocation(Player newHolder) {
        this.player.setHolder(newHolder);
        this.player.setActive(true);

        DragonEggSaver.getInstance().getConfig().set("currentPlayer.active", true);
        DragonEggSaver.getInstance().getConfig().set("currentPlayer.uuid", newHolder.getUniqueId().toString());

        this.location.setActive(false);

        DragonEggSaver.getInstance().getConfig().set("currentLocation.active", false);
        DragonEggSaver.getInstance().saveConfig();

        log.info("Saved dragon egg holder to config: {}", newHolder.getName());
    }

    public void updateOfflineLocation(Location newLocation) {
        this.player.setActive(false);
        DragonEggSaver.getInstance().getConfig().set("currentLocation.active", true);
        DragonEggSaver.getInstance().getConfig().set("currentPlayer.active", false);

        this.location.setHolder(newLocation);
        this.location.setActive(true);
        DragonEggSaver.getInstance().getConfig().set("currentLocation.x", newLocation.getBlockX());
        DragonEggSaver.getInstance().getConfig().set("currentLocation.y", newLocation.getBlockY());
        DragonEggSaver.getInstance().getConfig().set("currentLocation.z", newLocation.getBlockZ());
        DragonEggSaver.getInstance().getConfig().set("currentLocation.world", newLocation.getWorld().getName());
        DragonEggSaver.getInstance().saveConfig();

        log.info("Saved dragon egg location to config at x:{} y:{} z:{} in world {}", newLocation.getBlockX(), newLocation.getBlockY(), newLocation.getBlockZ(), newLocation.getWorld().getName());
    }

    public boolean isEggHolder(UUID uuid) {
        return this.player != null
                && this.player.getHolder().getUniqueId() == uuid
                && this.player.isActive();
    }
}
