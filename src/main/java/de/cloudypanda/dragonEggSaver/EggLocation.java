package de.cloudypanda.dragonEggSaver;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.UUID;

public class EggLocation {

    @Getter
    private Player currentHolder;

    @Getter
    private Location location;

    public EggLocation() {
        this.currentHolder = null;
        this.location = null;
    }

    public Location getCurrentLocation() {
        if (this.currentHolder != null) {
            return this.currentHolder.getLocation();
        }

        if(location != null) {
            return this.location;
        }

        return null;
    }

    public void updatePlayerEggLocation(Player newHolder) {
        this.currentHolder = newHolder;
        this.location = null;
    }

    public void updateOfflineLocation(Location newLocation) {
        this.location = newLocation;
        this.currentHolder = null;
    }

    public boolean isEggHolder(UUID uuid) {
        return this.currentHolder != null && this.currentHolder.getUniqueId() == uuid;
    }
}
