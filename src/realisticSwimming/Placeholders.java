package realisticSwimming;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import realisticSwimming.main.RSMain;

public class Placeholders extends PlaceholderExpansion {

    private final Plugin plugin;

    public Placeholders(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public @NotNull String getAuthor() {
        return String.join(", ", plugin.getDescription().getAuthors());
    }

    @Override
    public @NotNull String getIdentifier() {
        return "realisticswimming";
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String identifier) {
        if (identifier.equals("max_stamina")) {
            return "1000";
        }

        if (player == null) {
            return null;
        }

        return switch (identifier) {
            case "swim_toggled" -> Boolean.toString(!player.hasMetadata("swimmingDisabled"));
            case "fall_toggled" -> Boolean.toString(!player.hasMetadata("fallingDisabled"));
            case "is_swimming" -> Boolean.toString(player.hasMetadata("swimming"));
            case "is_falling" -> Boolean.toString(player.hasMetadata("falling"));
            case "current_stamina" -> Float.toString(RSMain.getMain().getPlayerStamina(player));
            default -> null;
        };
    }
}
