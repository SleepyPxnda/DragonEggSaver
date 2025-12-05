package de.cloudypanda.dragonEggSaver;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.UUID;

import static net.kyori.adventure.text.format.TextColor.color;

@Slf4j
public class DragonEggManager {

    @Getter
    private final EggLocation currentEggLocation = new EggLocation();

    public DragonEggManager(UUID uuid, boolean active, int x, int y, int z, String worldName) {
        if(uuid != null){
            var player = Bukkit.getPlayer(uuid);
            if (player != null) {
                currentEggLocation.updatePlayerEggLocation(player);
                log.info("Loaded dragon egg holder: {}", player.getName());
                return;
            } else {
                log.warn("Player with UUID {} not found while loading egg holder!", uuid);
            }
            return;
        }

        if(active){
            var world = Bukkit.getWorld(worldName);
            if (world == null) {
                log.warn("World {} not found while loading egg location!", worldName);
                return;
            }

            var location = new Location(world, x, y, z);
            currentEggLocation.updateOfflineLocation(location);
            log.info("Loaded dragon egg location at x:{} y:{} z:{} in world {}", x, y, z, worldName);
            return;
        }
        log.info("No dragon egg holder or location found during load.");
    }

    public boolean isEggHolder(UUID uniqueId) {
        return currentEggLocation.isEggHolder(uniqueId);
    }

    public void setHolder(Player newHolder) {
        currentEggLocation.updatePlayerEggLocation(newHolder);
        updateCompassForOtherPlayers();
    }

    public void setLocation(Location newLocation) {
        currentEggLocation.updateOfflineLocation(newLocation);
        updateCompassForOtherPlayers();
    }

    public void returnEggToHolder() {
        //This will be the old owner before he dropped it into the void
        var holder = currentEggLocation.getPlayer().getHolder();
        holder.getInventory().remove(Material.DRAGON_EGG);
        var notFitting = holder.getInventory().addItem(new ItemStack(Material.DRAGON_EGG, 1));

        if (!notFitting.isEmpty()) {
            holder.sendMessage(Texts.eggDroppedDueToMissingSpace);
            holder.getWorld().dropItemNaturally(holder.getLocation(), notFitting.values().iterator().next());
            return;
        }

        setHolder(holder);
        holder.sendMessage(Texts.eggReturnedToHolder);
        log.info("Returned Dragon Egg to {}", holder.getName());
    }

    public void updateGlowing() {
        Bukkit.getOnlinePlayers().forEach(player -> {
            boolean hasEgg = player.getInventory().contains(Material.DRAGON_EGG);

            if (hasEgg) {
                player.removePotionEffect(PotionEffectType.GLOWING);
                var glowingEffect = new PotionEffect(PotionEffectType.GLOWING, 40, 1, false, false, true);
                player.addPotionEffect(glowingEffect);
            }

            player.getNearbyEntities(player.getX(), player.getY(), player.getZ()).forEach(entity -> {
                if (entity instanceof Item item && item.getItemStack().getType() == Material.DRAGON_EGG) {
                    item.setGlowing(true);
                }
            });
        });
    }

    public void updateCompassForOtherPlayers() {
        if (currentEggLocation.getCurrentLocation() == null) {
            return;
        }

        Bukkit.getOnlinePlayers().forEach(player -> player.setCompassTarget(currentEggLocation.getCurrentLocation()));
    }

    public void placeEggAtHolderLocationAndRemoveFromInventory() {
        var playerHolder = currentEggLocation.getPlayer();

        if(!playerHolder.isActive()) return;

        var player = playerHolder.getHolder();

        log.info("Dropping Dragon Egg at holder {}", player.getName());
        player.getInventory().remove(Material.DRAGON_EGG);

        // Drop the dragon egg at the current holder's location
        var location = player.getLocation();

        var world = player.getWorld();
        world.getBlockAt(location).setType(Material.DRAGON_EGG, false);

        log.info("Dropped Dragon Egg at {}", player.getName());

        var leaveMessage = Texts.pluginPrefix.append(Component.text("Das Drachenei wurde bei " + player.getName() + " platziert!", color(120, 120, 120)));

        Bukkit.getServer().broadcast(leaveMessage);
        currentEggLocation.updateOfflineLocation(location.clone());
    }

    public void getDirectionToEgg(Player player) {
        if (currentEggLocation.getCurrentLocation() == null) {
            player.sendMessage(Texts.noEggHolder);
            log.info("Player {} tried to use compass, but no egg holder is set", player.getName());
            return;
        }

        if (DragonEggSaver.getDragonEggManager().isEggHolder(player.getUniqueId())) {
            player.sendMessage(Texts.selfEggHolder);
            return;
        }

        if(!player.getWorld().equals(currentEggLocation.getCurrentLocation().getWorld())) {
            var distanceMessage = Texts.pluginPrefix.append(
                    Component.text("Das Drachenei ist in der Welt " + currentEggLocation.getCurrentLocation().getWorld().getName() + "!", color(0x00FF00))
            );
            player.sendMessage(distanceMessage);
            return;
        }

        if(player.getWorld().getEnvironment() == World.Environment.NETHER){
            var x = currentEggLocation.getCurrentLocation().getX();
            var z = currentEggLocation.getCurrentLocation().getZ();
            var y = currentEggLocation.getCurrentLocation().getY();

            var distanceMessage = Texts.pluginPrefix.append(
                    Component.text("Das Drachenei ist bei ")
                            .append(Component.text("x %.1f | y %.1f | z %.1f".formatted(x, y, z), color(0x00FF00)))
            );
            player.sendMessage(distanceMessage);
            return;
        }

        var distanceMessage = Texts.pluginPrefix.append(
                Component.text("Das Drachenei ist noch ")
                        .append(Component.text("%.1f".formatted(player.getLocation().distance(currentEggLocation.getCurrentLocation())), color(0x00FF00)))
                        .append(Component.text(" Blöcke entfernt!"))
        );
        player.sendMessage(distanceMessage);
    }
}
