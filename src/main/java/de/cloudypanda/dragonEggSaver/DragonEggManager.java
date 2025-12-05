package de.cloudypanda.dragonEggSaver;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
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
    private Player previousHolder;
    @Getter
    private Player currentHolder;

    public boolean isEggHolder(UUID uniqueId) {
        return currentHolder != null && currentHolder.getUniqueId().equals(uniqueId);
    }

    public void updateCompassToHolder(Player player) {
        player.setCompassTarget(currentHolder != null ? currentHolder.getLocation() : player.getWorld().getSpawnLocation());
    }

    public void transferHolder(Player newHolder) {
        if(newHolder != null) {
            this.previousHolder = this.currentHolder;
        }

        this.currentHolder = newHolder;
        log.info("Dragon Egg holder transferred to {}", currentHolder == null ? "'none'" : currentHolder.getName());
    }

    public void returnEggToHolder() {
        currentHolder.getInventory().remove(Material.DRAGON_EGG);
        var notFitting = currentHolder.getInventory().addItem(new ItemStack(Material.DRAGON_EGG, 1));

        if(!notFitting.isEmpty()){
            currentHolder.sendMessage(Texts.eggDroppedDueToMissingSpace);
            currentHolder.getWorld().dropItemNaturally(currentHolder.getLocation(), notFitting.values().iterator().next());
            return;
        }

        currentHolder.sendMessage(Texts.eggReturnedToHolder);
        log.info("Returned Dragon Egg to {}", currentHolder.getName());
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
               if(entity instanceof Item item){
                   if(item.getItemStack().getType() == Material.DRAGON_EGG){
                          item.setGlowing(true);
                     }
               }
            });
        });
    }

    public void updateCompassForOtherPlayers() {
        Bukkit.getOnlinePlayers().forEach(player -> {
            var dragonEggLocation = currentHolder != null ? currentHolder.getLocation() : null;
            if(dragonEggLocation != null) {
                // Set the player's compass target to point towards the dragon egg holder
                player.setCompassTarget(dragonEggLocation);
            }
        });
    }

    public void dropEggAtHolder() {
        if(currentHolder == null) {
            log.warn("No current dragon egg holder to drop the egg at.");
            return;
        }

        // Drop the dragon egg at the current holder's location
        var world = currentHolder.getWorld();
        var location = currentHolder.getLocation();
        world.getBlockAt(location).setType(Material.DRAGON_EGG, false);
        log.info("Dropped Dragon Egg at {}", currentHolder.getName());

        var leaveMessage = Texts.pluginPrefix.append(Component.text("Das Dracheni wurde bei " + currentHolder.getName() + " platziert!", color(120,120,120)));

        Bukkit.getServer().broadcast(leaveMessage.build());

        // Remove the dragon egg from the current holder's inventory
        currentHolder.getInventory().remove(Material.DRAGON_EGG);

        // Update Dragonholder
        this.currentHolder = null;
    }
}
