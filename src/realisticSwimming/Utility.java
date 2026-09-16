/*
Copyright (c) 2016-2017 4a2e532e

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package realisticSwimming;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

public final class Utility {

    private Utility() {
    }

    public static boolean playerHasPermission(Player player, String permission) {
        return !Config.permsReq || player.hasPermission(permission);
    }

    public static boolean playerIsInCreativeMode(Player player) {
        return !Config.enabledInCreative && player.getGameMode() == GameMode.CREATIVE;
    }

    public static boolean isElytraWeared(Player player) {
        return isUsableElytra(player.getInventory().getChestplate());
    }

    public static boolean hasElytraStorage(Player player) {
        PlayerInventory inventory = player.getInventory();
        for (ItemStack item : inventory.getStorageContents()) {
            if (isUsableElytra(item)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isUsableElytra(ItemStack item) {
        if (item == null || item.getType() != Material.ELYTRA) {
            return false;
        }

        ItemMeta meta = item.getItemMeta();
        if (!(meta instanceof Damageable damageable)) {
            return true;
        }

        return damageable.getDamage() < item.getType().getMaxDurability() - 1;
    }
}
