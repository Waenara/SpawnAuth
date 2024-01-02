package f3f5.spawnauth;

import io.papermc.lib.PaperLib;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import java.util.HashMap;
import java.util.UUID;
import static f3f5.spawnauth.SpawnAuth.config;
import static org.bukkit.Bukkit.getServer;

public class Helpers {
    private static final HashMap<UUID, Location> playerLocations = new HashMap<>();
    public boolean isPlayerInRadius(Player player, Location center, double radius) {
        return player.getLocation().distanceSquared(center) <= (radius * radius);
    }
    public void cacheOriginalLocation(UUID playerUniqueId, Location loginLocation) {
        if (!playerLocations.containsKey(playerUniqueId)){
            playerLocations.put(playerUniqueId, loginLocation);
        }
    }

    public void teleportAway(Player player) {
        Location teleportDestination = getSpawnLocation();
        player.teleport(teleportDestination);
    }
    public Location getSpawnLocation(){
        return new Location(getServer().getWorld(config.getString("world-name")), config.getDouble("spawn-x"), config.getDouble("spawn-y"), config.getDouble("spawn-z"));
    }

    public void teleportBack(Player player) {
        UUID playerUniqueId = player.getUniqueId();
        if (!playerLocations.containsKey(playerUniqueId)) return;
        Location teleportDestination = playerLocations.get(playerUniqueId);

        PaperLib.getChunkAtAsync(teleportDestination, false).thenAccept(chunk -> PaperLib.teleportAsync(player, teleportDestination).thenAccept(teleportHasHappened -> {
            if (!teleportHasHappened) player.teleport(teleportDestination);
            playerLocations.remove(playerUniqueId);
        }));
    }
    public HashMap<UUID, Location> getPlayerLocations(){
        return playerLocations;
    }
}
