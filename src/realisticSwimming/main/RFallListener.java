/*
Copyright (c) 2016-2017 4a2e532e

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package realisticSwimming.main;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

import realisticSwimming.Config;
import realisticSwimming.Utility;
import realisticSwimming.events.PlayerStartFallingEvent;

public class RFallListener implements Listener {

    private final Plugin plugin;
    private final Plugin glidePlugin;

    public RFallListener(Plugin plugin) {
        this.plugin = plugin;
        this.glidePlugin = Bukkit.getPluginManager().getPlugin("Glide");
    }

    @EventHandler
    public void onPlayerMoveEvent(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (playerCanFall(player)) {
            player.setGliding(true);
            player.setMetadata("falling", new FixedMetadataValue(plugin, null));
        } else if (player.hasMetadata("falling")) {
            player.removeMetadata("falling", plugin);
        }
    }

    @EventHandler
    public void onEntityToggleGlideEvent(EntityToggleGlideEvent event) {
        if (event.getEntity() instanceof Player player
                && playerCanFall(player)
                && player.getLocation().subtract(0, 1, 0).getBlock().getType() != Material.WATER) {
            PlayerStartFallingEvent fallingEvent = new PlayerStartFallingEvent(player);
            Bukkit.getPluginManager().callEvent(fallingEvent);

            if (!fallingEvent.isCancelled()) {
                player.setVelocity(new Vector(
                        player.getLocation().getDirection().getX() * Config.fallGlideSpeed,
                        -Config.fallDownwardSpeed,
                        player.getLocation().getDirection().getZ() * Config.fallGlideSpeed));
                event.setCancelled(true);
            } else {
                player.setGliding(false);
            }
        }
    }

    @EventHandler
    public void onPlayerFall(PlayerStartFallingEvent event) {
        if (isGlidePluginGliding(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    public boolean playerCanFall(Player player) {
        if (!player.hasMetadata("fallingDisabled")
                && Utility.playerHasPermission(player, "rs.user.fall")
                && player.getFallDistance() > Config.minFallDistance
                && Config.enableFall
                && player.getLocation().getBlock().getType() != Material.WATER
                && player.getLocation().subtract(0, 1, 0).getBlock().getType() == Material.AIR) {
            return !isElytraDeploying(player);
        }
        return false;
    }

    public boolean isElytraDeploying(Player player) {
        Plugin elytraPlugin = Bukkit.getPluginManager().getPlugin("Elytra");
        if (elytraPlugin != null && elytraPlugin.isEnabled() && player.hasPermission("elytra.auto")) {
            return Utility.isElytraWeared(player)
                    || (player.hasPermission("elytra.auto-equip") && Utility.hasElytraStorage(player));
        }
        return false;
    }

    private boolean isGlidePluginGliding(Player player) {
        if (glidePlugin == null || !glidePlugin.isEnabled()) {
            return false;
        }

        try {
            Class<?> glideClass = Class.forName(
                    "io.github.dailystruggle.glide.Glide",
                    false,
                    glidePlugin.getClass().getClassLoader());
            Method isGliding = glideClass.getMethod("isGliding", Player.class);
            return Boolean.TRUE.equals(isGliding.invoke(null, player));
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException
                 | InvocationTargetException | LinkageError ignored) {
            return false;
        }
    }
}
