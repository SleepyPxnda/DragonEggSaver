package de.cloudypanda.dragonEggSaver.events;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;

public class EventListener implements Listener {

    @EventHandler
    public void onPlayerDrop(PlayerDropItemEvent e) {
        var itemDropped = e.getItemDrop().getItemStack();

        if (!e.getPlayer().getWorld().getEnvironment().equals(World.Environment.THE_END)) {
            return; //Allow dropping in The End dimension
        }

        //Disable dropping dragon egg
        if (!Material.DRAGON_EGG.equals(itemDropped.getType())) {
            return;
        }

        e.setCancelled(true);
    }

    @EventHandler
    public void InventoryMoveEvent(InventoryMoveItemEvent e) {
        var itemMoved = e.getItem();

        //Disable moving dragon egg in any inventory
        if (!Material.DRAGON_EGG.equals(itemMoved.getType())) {
            return;
        }

        e.setCancelled(true);
    }

    @EventHandler
    public void EntityCombustEvent(EntityCombustEvent e) {

        //No additional handling needed if not an item
        if (!e.getEntityType().equals(EntityType.ITEM)) {
            return;
        }

        var entity = e.getEntity();
        var item = ((Item) entity).getItemStack();

        //Disable dragon egg from burning
        if (!Material.DRAGON_EGG.equals(item.getType())) {
            return;
        }

        e.setCancelled(true);
    }
}
