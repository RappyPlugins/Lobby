package com.rappytv.lobby.listeners;

import com.rappytv.lobby.LobbyPlugin;
import com.rappytv.lobby.inventories.TeleporterInventory;
import com.rappytv.lobby.items.Teleporter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.*;
import org.bukkit.inventory.Inventory;

public class PlayerListener implements Listener {

    private final LobbyPlugin plugin;

    public PlayerListener(LobbyPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        event.setJoinMessage("");
        Player player = event.getPlayer();

        sendToSpawn(player);
        setPlayerInventory(player);
        player.setFoodLevel(20);
        player.setHealth(20.0);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        event.setQuitMessage("");
    }

    @EventHandler
    public void onHunger(FoodLevelChangeEvent event) {
        event.setCancelled(true);
        event.getEntity().setFoodLevel(20);
    }

    @EventHandler
    public void openInventory(InventoryOpenEvent event) {
        Player player = (Player) event.getPlayer();
        if(!player.hasPermission("lobby.gui"))
            event.setCancelled(event.getInventory().getType() != InventoryType.CHEST);
    }

    public void sendToSpawn(Player player) {
        Bukkit.getScheduler().runTaskLater(
                plugin,
                () -> player.teleport(plugin.getSpawn()),
                1L
        );
    }

    public void setPlayerInventory(Player player) {
        Inventory inv = player.getInventory();
        inv.clear();
        inv.setItem(4, new Teleporter(plugin));
    }

    @EventHandler
    public void onPlayerDamageByPlayer(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player) || !(event.getDamager() instanceof Player attacker)) return;
        event.setCancelled(!attacker.hasPermission("lobby.attack"));
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        if(!(event.getEntity() instanceof Player)) return;
        event.setCancelled(event.getCause() != EntityDamageEvent.DamageCause.ENTITY_ATTACK);
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        event.setDeathMessage("");
        event.setDroppedExp(0);
        event.setKeepInventory(true);
        event.getDrops().clear();
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        event.setRespawnLocation(plugin.getSpawn());
    }

    @EventHandler
    public void onItemDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        if(!player.hasPermission("lobby.items.drop")) {
            Item drop = event.getItemDrop();
            drop.getItemStack().setAmount(0);
            event.setCancelled(true);
            setPlayerInventory(player);
        }
    }

    @SuppressWarnings("deprecation")
    @EventHandler
    public void onItemPickup(PlayerPickupItemEvent event) {
        event.setCancelled(!event.getPlayer().hasPermission("lobby.items.pickup"));
    }

    @EventHandler
    public void onArmorStandManipulate(PlayerArmorStandManipulateEvent event) {
        Player player = event.getPlayer();

        event.setCancelled(!player.hasPermission("lobby.block.interact"));
    }

    @SuppressWarnings("ConstantConditions")
    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Teleporter item = new Teleporter(plugin);

        if(event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            event.setCancelled(!player.hasPermission("lobby.block.interact"));
        }
        if(event.getItem() == null || event.getItem().getItemMeta() == null) return;
        if(event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if(event.getItem().getItemMeta().getDisplayName().equalsIgnoreCase(item.getItemMeta().getDisplayName()) && player.hasPermission("lobby.page.default")) {
                player.openInventory(TeleporterInventory.get(player));
            }
        }
    }
}
