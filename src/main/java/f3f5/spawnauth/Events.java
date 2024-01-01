package f3f5.spawnauth;

import fr.xephi.authme.api.v3.AuthMeApi;
import fr.xephi.authme.events.LoginEvent;
import fr.xephi.authme.events.LogoutEvent;
import fr.xephi.authme.events.UnregisterByPlayerEvent;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import java.util.HashMap;
import java.util.Random;
import java.util.UUID;

public class Events implements Listener {
    private final Helpers helpers = new Helpers();
    private final HashMap<UUID, Location> playerLocations = helpers.getPlayerLocations();
    public AuthMeApi authMeApi=AuthMeApi.getInstance();
    @EventHandler
    private void onPlayerRespawn(PlayerRespawnEvent event) {
        UUID playerUniqueId = event.getPlayer().getUniqueId();
        playerLocations.remove(playerUniqueId);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    private void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (helpers.isPlayerInRadius(player,helpers.getSpawnLocation(),10)){
            helpers.cacheOriginalLocation(player.getUniqueId(), player.getLocation());
        }
        helpers.teleportAway(player);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    private void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (player.isInsideVehicle()) {
            player.leaveVehicle();
            helpers.teleportBack(player);
        }
    }

    private int getRandomCoordinate(int Limit) {
        Random random = new Random();
        return random.nextInt(Limit);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    private void onLogin(LoginEvent event) {
        World world = event.getPlayer().getWorld();
        int spawnRadius = Integer.parseInt(world.getGameRuleValue("spawnRadius"));
        Player player = event.getPlayer();
        player.setNoDamageTicks(60);
        System.out.println(playerLocations);
        if (!playerLocations.containsKey(player.getUniqueId())) {
            if (player.getBedSpawnLocation() != null) {
                player.teleport(player.getBedSpawnLocation());
                return;
            }
            int x = getRandomCoordinate(spawnRadius);
            int z = getRandomCoordinate(spawnRadius);
            int y = world.getHighestBlockYAt(x, z);

            player.teleport(new Location(world, x, y, z));
        } else {
            helpers.teleportBack(player);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    private void OnLogout(LogoutEvent event) {
        Player player = event.getPlayer();
        helpers.cacheOriginalLocation(player.getUniqueId(), player.getLocation());
        helpers.teleportAway(player);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    private void onUnregisterByPlayer(UnregisterByPlayerEvent event) {
        Player player = event.getPlayer();
        helpers.cacheOriginalLocation(player.getUniqueId(), player.getLocation());
        helpers.teleportAway(player);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    private void onUnregisterByAdmin(UnregisterByPlayerEvent event) {
        Player player = event.getPlayer();
        if (player != null && player.isOnline()) {
            helpers.cacheOriginalLocation(player.getUniqueId(), player.getLocation());
            helpers.teleportAway(player);
        }
    }
}
