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

import static org.bukkit.Bukkit.getScheduler;
import static org.bukkit.Bukkit.getServer;

public class Events implements Listener {
    private final Helpers helpers = new Helpers();
    private final HashMap<UUID, Location> playerLocations = helpers.getPlayerLocations();
    public AuthMeApi authMeApi = AuthMeApi.getInstance();

    @EventHandler
    private void onPlayerRespawn(PlayerRespawnEvent event) {
        if (!authMeApi.isAuthenticated(event.getPlayer())){
            UUID playerUniqueId = event.getPlayer().getUniqueId();
            playerLocations.remove(playerUniqueId);
            getServer().getScheduler().runTaskLater(SpawnAuth.getPlugin(SpawnAuth.class), () -> helpers.teleportAway(event.getPlayer()), 2L);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    private void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (!helpers.isPlayerInRadius(player,helpers.getLoginLocation(),10)){
            helpers.cacheOriginalLocation(player.getUniqueId(), player.getLocation());
        }
        helpers.teleportAway(player);
        getScheduler().runTaskTimer(SpawnAuth.getPlugin(SpawnAuth.class), () -> helpers.teleportPlayer(player), 0L, 4L);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    private void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (player.isInsideVehicle()) {
            player.leaveVehicle();
            helpers.teleportBack(player);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    private void onLogin(LoginEvent event) {
        World world = event.getPlayer().getWorld();
        Player player = event.getPlayer();
        player.setNoDamageTicks(60);
        if (!playerLocations.containsKey(player.getUniqueId())) {
            if (player.getBedSpawnLocation() != null) {
                player.teleport(player.getBedSpawnLocation());
                return;
            }
            player.teleport(helpers.getSpawnLocation(world));
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
