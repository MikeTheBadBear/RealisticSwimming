/*
Copyright (c) 2016-2017 4a2e532e

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package realisticSwimming.main;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleSprintEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;

import realisticSwimming.Config;
import realisticSwimming.Utility;
import realisticSwimming.events.PlayerStartSwimmingEvent;
import realisticSwimming.stamina.Stamina;

public class RSwimListener implements Listener {

    private final Plugin plugin;
    private final Map<UUID, Stamina> playerStamina = new HashMap<>();

    RSwimListener(Plugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerMoveEvent(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        if (playerCanSwim(player)) {
            if (event.getTo().getY() <= event.getFrom().getY() || Config.enableSwimmingUp) {
                if (!player.hasMetadata("swimmingDisabled")
                        && Utility.playerHasPermission(player, "rs.user.swim")) {
                    player.setGliding(!player.isSprinting());
                    startSwimming(player);
                }
            } else if (event.getTo().getY() <= 62) {
                player.setGliding(false);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerItemDamage(PlayerItemDamageEvent event) {
        if (!Config.durabilityLoss
                && event.getItem().getType() == Material.ELYTRA
                && event.getPlayer().hasMetadata("swimming")) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityToggleGlideEvent(EntityToggleGlideEvent event) {
        if (event.getEntity() instanceof Player player
                && playerCanSwim(player)
                && !player.hasMetadata("swimmingDisabled")) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerToggleSprintEvent(PlayerToggleSprintEvent event) {
        Player player = event.getPlayer();
        if (player.isSwimming() && !event.isSprinting()) {
            player.setSwimming(false);
            player.setGliding(true);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Stamina stamina = playerStamina.remove(event.getPlayer().getUniqueId());
        if (stamina != null && !stamina.isCancelled()) {
            stamina.cancel();
        }
        event.getPlayer().removeMetadata("swimming", plugin);
    }

    public void startSwimming(Player player) {
        if (!player.hasMetadata("swimming")) {
            startStaminaSystem(player);
            player.setMetadata("swimming", new FixedMetadataValue(plugin, null));

            PlayerStartSwimmingEvent event = new PlayerStartSwimmingEvent(player);
            Bukkit.getServer().getPluginManager().callEvent(event);
        }
    }

    public boolean playerCanSwim(Player player) {
        if (player.getLocation().getBlock().getType() == Material.WATER
                && player.getLocation().subtract(0, Config.minWaterDepth, 0).getBlock().getType() == Material.WATER
                && player.getVehicle() == null
                && !Utility.playerIsInCreativeMode(player)
                && !player.isFlying()) {
            return !isInWaterElevator(player);
        }
        return false;
    }

    public void boost(Player player) {
        if (Utility.playerHasPermission(player, "rs.user.boost")
                && Config.enableBoost
                && player.isSprinting()
                && (player.getLocation().getDirection().getY() < -0.1 || !Config.ehmCompatibility)) {
            player.setVelocity(player.getLocation().getDirection().multiply(Config.sprintSpeed));
        }
    }

    public void startStaminaSystem(Player player) {
        if (!Utility.playerHasPermission(player, "rs.bypass.stamina") || !Config.permsReq) {
            Stamina existing = playerStamina.remove(player.getUniqueId());
            if (existing != null && !existing.isCancelled()) {
                existing.cancel();
            }

            Stamina stamina = new Stamina(plugin, player, this);
            stamina.runTaskTimer(plugin, 0L, Math.max(1, Config.staminaUpdateDelay));
            playerStamina.put(player.getUniqueId(), stamina);
        }
    }

    public static boolean isInWaterElevator(Player player) {
        if (!Config.disableSwimInWaterfall) {
            return false;
        }

        int width = Math.max(1, Config.maxWaterfallDiameter);
        return player.getLocation().add(width, 0, 0).getBlock().getType() != Material.WATER
                && player.getLocation().add(-width, 0, 0).getBlock().getType() != Material.WATER
                && player.getLocation().add(0, 0, width).getBlock().getType() != Material.WATER
                && player.getLocation().add(0, 0, -width).getBlock().getType() != Material.WATER;
    }

    @EventHandler
    public void blockRocketBoost(PlayerInteractEvent event) {
        if (event.hasItem()
                && event.getItem().getType() == Material.FIREWORK_ROCKET
                && event.getPlayer().hasMetadata("swimming")) {
            event.setCancelled(true);
        }
    }

    public float getPlayerStamina(Player player) {
        Stamina stamina = playerStamina.get(player.getUniqueId());
        if (stamina == null || stamina.isCancelled()) {
            return 1000;
        }
        return Math.max(0, stamina.getCurrentStamina());
    }
}
