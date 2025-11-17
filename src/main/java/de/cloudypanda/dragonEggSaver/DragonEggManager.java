package de.cloudypanda.dragonEggSaver;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.UUID;

@Slf4j
public class DragonEggManager {
    @Getter
    private Player previousHolder;
    @Getter
    private Player currentHolder;

    public boolean isEggHolder(UUID uniqueId) {
        return currentHolder != null && currentHolder.getUniqueId().equals(uniqueId);
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
            currentHolder.sendMessage(Component.text("Your inventory was full, the Dragon Egg will be dropped on the ground."));
            currentHolder.getWorld().dropItemNaturally(currentHolder.getLocation(), notFitting.values().iterator().next());
            return;
        }

        currentHolder.sendMessage(Component.text("The Dragon Egg has been returned to you!"));
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
        });
    }
}
