package de.cloudypanda.dragonEggSaver.events;

import com.destroystokyo.paper.event.entity.EntityRemoveFromWorldEvent;
import de.cloudypanda.dragonEggSaver.DragonEggSaver;
import de.cloudypanda.dragonEggSaver.Texts;
import lombok.extern.slf4j.Slf4j;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.List;

import static net.kyori.adventure.text.format.TextColor.color;

@Slf4j
public class EventListener implements Listener {

    private final List<InventoryType> allowedInventories = List.of(InventoryType.CRAFTING, InventoryType.CREATIVE, InventoryType.PLAYER);

    @EventHandler
    public void onItemInteractEvent(PlayerInteractEvent event) {

        if (!event.getAction().isRightClick()) return;

        if (event.hasBlock() && event.getClickedBlock().getType().equals(Material.DRAGON_EGG)) {
            event.setCancelled(true);
            return;
        }

        if (event.hasItem() && Material.COMPASS.equals(event.getItem().getType())) {
            DragonEggSaver.getDragonEggManager().getDirectionToEgg(event.getPlayer());
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerDeathEvent(PlayerDeathEvent e) {
        if (!DragonEggSaver.getDragonEggManager().isEggHolder(e.getPlayer().getUniqueId())) return;

        //Prevent Dragon Egg from dropping as item
        e.getDrops().removeIf(itemStack -> itemStack.getType().equals(Material.DRAGON_EGG));

        //Handle void death separately
        if (e.getDamageSource().getDamageType() == DamageType.OUT_OF_WORLD) {
            if (!DragonEggSaver.getDragonEggManager().getCurrentEggLocation().getPlayer().isActive()) return;

            e.getPlayer().getInventory().remove(Material.DRAGON_EGG);

            var endWorld = Bukkit.getServer()
                    .getWorlds()
                    .stream()
                    .filter(x -> x.getEnvironment() == World.Environment.THE_END)
                    .findFirst().orElse(null);

            log.info("Found end world: {}", endWorld != null ? endWorld.getName() : "null");

            if (endWorld == null) return;

            var location = new Location(endWorld, 0, 65, 0);
            location.getBlock().setType(Material.DRAGON_EGG);

            DragonEggSaver.getDragonEggManager().setLocation(location);
            Bukkit.getServer().broadcast(Texts.eggResetToEndspawn);
            log.info("Dragon Egg reset to End spawn due to holder {} dying in the void.", e.getPlayer().getName());
        } else {
            DragonEggSaver.getDragonEggManager().placeEggAtHolderLocationAndRemoveFromInventory();
            Bukkit.getServer().broadcast(Texts.playerWithEggDied);
            log.info("Dragon Egg dropped at holder {}'s death location.", e.getPlayer().getName());
        }
    }

    @EventHandler
    public void onBlockPlaceEvent(BlockPlaceEvent event) {
        if (!Material.DRAGON_EGG.equals(event.getBlockPlaced().getType())) return;

        //Update dragon egg location on placement
        DragonEggSaver.getDragonEggManager().setLocation(event.getBlockPlaced().getLocation());
    }

    @EventHandler
    public void PlayerPickupItemEvent(EntityPickupItemEvent e) {
        if (!(e.getEntity() instanceof Player player)) return;

        var itemPicked = e.getItem().getItemStack();

        //Only handle dragon egg pickups
        if (!Material.DRAGON_EGG.equals(itemPicked.getType())) return;

        //Switch dragon egg holder on pickup
        DragonEggSaver.getDragonEggManager().setHolder(player);
    }

    @EventHandler
    public void InventoryClickEvent(InventoryClickEvent e) {
        var player = e.getWhoClicked();

        //Only handle player clicks
        if (!DragonEggSaver.getDragonEggManager().isEggHolder(player.getUniqueId())) return;

        //Only block for non-allowed inventories
        if (allowedInventories.contains(e.getInventory().getType())) return;

        var itemClicked = e.getCurrentItem();

        //Disable moving dragon egg in any inventory
        if (itemClicked == null || !Material.DRAGON_EGG.equals(itemClicked.getType())) return;

        player.sendMessage(Texts.cannotMoveEggToInventory);
        e.setCancelled(true);
    }

    @EventHandler
    public void EntityRemoveFromWorldEvent(EntityRemoveFromWorldEvent e) {
        //No additional handling needed if not an item
        if (!e.getEntityType().equals(EntityType.ITEM)) return;

        var entity = e.getEntity();
        var item = ((Item) entity).getItemStack();

        //Disable dragon egg from being removed from world
        if (!Material.DRAGON_EGG.equals(item.getType())) return;

        //Player dropped it into the void
        if (!DragonEggSaver.getDragonEggManager().getCurrentEggLocation().getLocation().isActive()) return;

        DragonEggSaver.getDragonEggManager().returnEggToHolder();
    }

    @EventHandler
    public void PlayerJoinEvent(PlayerJoinEvent e) {
        var player = e.getPlayer();

        if (!player.getInventory().contains(Material.DRAGON_EGG)) return;

        DragonEggSaver.getDragonEggManager().setHolder(player);
        log.info("Transferred Dragon Egg holder to {} on join", player.getName());
    }

    @EventHandler
    public void onPlayerDropEvent(PlayerDropItemEvent e) {
        var player = e.getPlayer();

        if (!DragonEggSaver.getDragonEggManager().isEggHolder(player.getUniqueId())) return;

        var itemDropped = e.getItemDrop().getItemStack();

        //Switch dragon egg holder on drop
        if (!Material.DRAGON_EGG.equals(itemDropped.getType())) return;

        var location = e.getItemDrop().getLocation();
        DragonEggSaver.getDragonEggManager().setLocation(location);
    }

    @EventHandler
    public void ItemDespawnEvent(ItemDespawnEvent e) {
        var item = e.getEntity().getItemStack();

        if (!Material.DRAGON_EGG.equals(item.getType())) return;

        //Disable dragon egg from despawning
        DragonEggSaver.getDragonEggManager().returnEggToHolder();
    }

    @EventHandler
    public void onQuitEvent(PlayerQuitEvent e) {
        var player = e.getPlayer();

        if (!DragonEggSaver.getDragonEggManager().isEggHolder(player.getUniqueId())) return;

        if (!e.getPlayer().getInventory().contains(Material.DRAGON_EGG)) return;

        DragonEggSaver.getDragonEggManager().placeEggAtHolderLocationAndRemoveFromInventory();

        var leaveMessage = Texts.pluginPrefix.append(Component.text("Das Drachenei wurde bei " + player.getName() + " platziert, da der Spieler nun offline ist!", color(120, 120, 120)));
        Bukkit.getServer().broadcast(leaveMessage);
    }

    @EventHandler
    public void onInventoryPickupItemEvent(InventoryPickupItemEvent e) {
        var item = e.getItem().getItemStack();

        //Disable dragon egg from being picked up when dropped
        if (!Material.DRAGON_EGG.equals(item.getType())) return;

        e.setCancelled(true);
    }
}
