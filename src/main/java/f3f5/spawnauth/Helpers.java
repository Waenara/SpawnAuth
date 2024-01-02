package f3f5.spawnauth;

import fr.xephi.authme.api.v3.AuthMeApi;
import io.papermc.lib.PaperLib;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import java.util.HashMap;
import java.util.Random;
import java.util.UUID;
import static f3f5.spawnauth.SpawnAuth.config;


public class Helpers {
    public AuthMeApi authMeApi = AuthMeApi.getInstance();
    private static final HashMap<UUID, Location> playerLocations = new HashMap<>();
    public boolean isPlayerInRadius(Player player, Location center, double radius) {
        return player.getLocation().distanceSquared(center) <= (radius * radius);
    }
    public void cacheOriginalLocation(UUID playerUniqueId, Location loginLocation) {
        if (!playerLocations.containsKey(playerUniqueId)){
            playerLocations.put(playerUniqueId, loginLocation);
        }
    }
    public void teleportPlayer(Player player){
        if (authMeApi.isAuthenticated(player)) return;
        player.teleport(getLoginLocation());
    }
    public Location getSpawnLocation(World world) {
        int spawnRadius = Integer.parseInt(world.getGameRuleValue("spawnRadius")) <= 10 ? 200 : Integer.parseInt(world.getGameRuleValue("spawnRadius"));

        int x = new Random().nextInt(spawnRadius*2)-spawnRadius;
        int z = new Random().nextInt(spawnRadius*2)-spawnRadius;
        double y = world.getHighestBlockYAt(x, z);

        return new Location(world, x, y, z);
    }

    public void teleportAway(Player player) {
        Location teleportDestination = getLoginLocation();
        player.teleport(teleportDestination);
    }
    public Location getLoginLocation(){
        return new Location(Bukkit.getServer().getWorld(config.getString("world-name")), config.getDouble("spawn-x"), config.getDouble("spawn-y"), config.getDouble("spawn-z"));
    }

    public void teleportBack(Player player) {
        UUID playerUniqueId = player.getUniqueId();
        if (!playerLocations.containsKey(playerUniqueId)) return;
        Location teleportDestination = playerLocations.get(playerUniqueId);

        player.teleport(teleportDestination);
        playerLocations.remove(playerUniqueId);
    }
    public HashMap<UUID, Location> getPlayerLocations(){
        return playerLocations;
    }
}
