package de.cloudypanda.dragonEggSaver.events;

import com.destroystokyo.paper.event.entity.EntityRemoveFromWorldEvent;
import de.cloudypanda.dragonEggSaver.DragonEggSaver;
import de.cloudypanda.dragonEggSaver.Texts;
import lombok.extern.slf4j.Slf4j;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.List;

@Slf4j
public class EventListener implements Listener {

    private final List<InventoryType> allowedInventories = List.of(InventoryType.CRAFTING, InventoryType.CREATIVE, InventoryType.PLAYER);

    @EventHandler
    public void onItemInteractEvent(PlayerInteractEvent event) {
        if (event.hasItem() && Material.COMPASS.equals(event.getItem().getType())) {
            if (DragonEggSaver.getDragonEggManager().getCurrentHolder() == null) {
                event.getPlayer().sendMessage(Texts.noEggHolder);
                return;
            }

            if (DragonEggSaver.getDragonEggManager().isEggHolder(event.getPlayer().getUniqueId())) {
                event.getPlayer().sendMessage(Texts.selfEggHolder);
                return;
            }

            var distanceMessage = Texts.pluginPrefix.append(
                    Component.text("Das Drachenei ist noch ")
                            .append(Component.text("%.1f".formatted(event.getPlayer().getLocation().distance(DragonEggSaver.getDragonEggManager().getCurrentHolder().getLocation())), TextColor.color(0x00FF00)))
                            .append(Component.text(" Blöcke entfernt!"))
            );

            event.getPlayer().sendMessage(distanceMessage);
            DragonEggSaver.getDragonEggManager().updateCompassToHolder(event.getPlayer());
        }
    }

    @EventHandler
    public void PlayerPickupItemEvent(EntityPickupItemEvent e) {
        if (!(e.getEntity() instanceof Player player)) {
            return;
        }

        var itemPicked = e.getItem().getItemStack();

        //Switch dragon egg holder on pickup
        if (Material.DRAGON_EGG.equals(itemPicked.getType())) {
            DragonEggSaver.getDragonEggManager().transferHolder(player);
        }
    }

    @EventHandler
    public void InventoryClickEvent(InventoryClickEvent e) {
        var player = e.getWhoClicked();

        if (!DragonEggSaver.getDragonEggManager().isEggHolder(player.getUniqueId())) {
            return; //Only handle clicks from the dragon egg holder
        }

        if (allowedInventories.contains(e.getInventory().getType())) {
            return; //Only handle clicks in chest inventories
        }

        var itemClicked = e.getCurrentItem();

        //Disable moving dragon egg in any inventory
        if (itemClicked == null || !Material.DRAGON_EGG.equals(itemClicked.getType())) {
            return;
        }

        player.sendMessage(Texts.cannotMoveEggToInventory);
        e.setCancelled(true);
    }

    @EventHandler
    public void EntityRemoveFromWorldEvent(EntityRemoveFromWorldEvent e) {
        //No additional handling needed if not an item
        if (!e.getEntityType().equals(EntityType.ITEM)) {
            return;
        }

        var entity = e.getEntity();
        var item = ((Item) entity).getItemStack();

        //Disable dragon egg from being removed from world
        if (!Material.DRAGON_EGG.equals(item.getType())) {
            return;
        }

        DragonEggSaver.getDragonEggManager().returnEggToHolder();
    }

    @EventHandler
    public void PlayerJoinEvent(PlayerJoinEvent e) {
        var player = e.getPlayer();

        if (player.getInventory().contains(Material.DRAGON_EGG)) {
            DragonEggSaver.getDragonEggManager().transferHolder(player);
            log.info("Transferred Dragon Egg holder to {} on join", player.getName());
        }
    }

    @EventHandler
    public void ItemDespawnEvent(ItemDespawnEvent e) {
        var item = e.getEntity().getItemStack();

        //Disable dragon egg from despawning
        if (!Material.DRAGON_EGG.equals(item.getType())) {
            return;
        }

        DragonEggSaver.getDragonEggManager().returnEggToHolder();
    }

    @EventHandler
    public void onQuitEvent(PlayerQuitEvent e) {
        var player = e.getPlayer();

        if (DragonEggSaver.getDragonEggManager().isEggHolder(player.getUniqueId())) {
            DragonEggSaver.getDragonEggManager().dropEggAtHolder();
        }
    }

    @EventHandler
    public void onInventoryPickupItemEvent(InventoryPickupItemEvent e) {
        var item = e.getItem().getItemStack();

        //Disable dragon egg from being picked up when dropped
        if (!Material.DRAGON_EGG.equals(item.getType())) {
            return;
        }

        e.setCancelled(true);
    }
}
