package f3f5.spawnauth;

import fr.xephi.authme.api.v3.AuthMeApi;
import fr.xephi.authme.events.LoginEvent;
import fr.xephi.authme.events.LogoutEvent;
import fr.xephi.authme.events.UnregisterByPlayerEvent;
import io.papermc.lib.PaperLib;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Random;
import java.util.UUID;

public class SpawnAuth extends JavaPlugin implements Listener {
    private AuthMeApi authMeApi;
    private FileConfiguration config;
    private final HashMap<UUID, Location> playerLocations = new HashMap<>();

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        authMeApi = AuthMeApi.getInstance();
        config = getConfig();
        config.addDefault("world-name", "world");
        config.addDefault("spawn-x", 0);
        config.addDefault("spawn-y", 1);
        config.addDefault("spawn-z", 0);
        config.options().copyDefaults(true);
        saveConfig();
    }

    private void cacheOriginalLocation(UUID playerUniqueId, Location loginLocation) {
        if (!playerLocations.containsKey(playerUniqueId)) {
            playerLocations.put(playerUniqueId, loginLocation);
        }
    }

    private void teleportAway(Player player) {
        Location teleportDestination = new Location(getServer().getWorld(config.getString("world-name")), config.getDouble("spawn-x"), config.getDouble("spawn-y"), config.getDouble("spawn-z"));

        PaperLib.getChunkAtAsync(teleportDestination, false).thenAccept(chunk -> player.teleport(teleportDestination));
    }

    private void teleportBack(Player player) {
        UUID playerUniqueId = player.getUniqueId();
        if (!playerLocations.containsKey(playerUniqueId)) return;
        Location teleportDestination = playerLocations.get(playerUniqueId);

        PaperLib.getChunkAtAsync(teleportDestination, false).thenAccept(chunk -> PaperLib.teleportAsync(player, teleportDestination).thenAccept(teleportHasHappened -> {
            if (!teleportHasHappened) player.teleport(teleportDestination);
            playerLocations.remove(playerUniqueId);
        }));
    }

    @EventHandler
    private void onPlayerRespawn(PlayerRespawnEvent event) {
        UUID playerUniqueId = event.getPlayer().getUniqueId();
        playerLocations.remove(playerUniqueId);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    private void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        teleportAway(player);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    private void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (player.isInsideVehicle()) {
            player.leaveVehicle();
            teleportBack(player);
        }
        if (authMeApi.isAuthenticated(player)) {
            cacheOriginalLocation(player.getUniqueId(), player.getLocation());
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
            teleportBack(player);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    private void OnLogout(LogoutEvent event) {
        Player player = event.getPlayer();
        cacheOriginalLocation(player.getUniqueId(), player.getLocation());
        teleportAway(player);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    private void onUnregisterByPlayer(UnregisterByPlayerEvent event) {
        Player player = event.getPlayer();
        cacheOriginalLocation(player.getUniqueId(), player.getLocation());
        teleportAway(player);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    private void onUnregisterByAdmin(UnregisterByPlayerEvent event) {
        Player player = event.getPlayer();
        if (player != null && player.isOnline()) {
            cacheOriginalLocation(player.getUniqueId(), player.getLocation());
            teleportAway(player);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    private void onPluginDisable(PluginDisableEvent event) {
        if (event.getPlugin().equals(this)) {
            playerLocations.forEach((uuid, location) -> getServer().getPlayer(uuid).teleport(location));
        }
        getServer().shutdown();
    }
}