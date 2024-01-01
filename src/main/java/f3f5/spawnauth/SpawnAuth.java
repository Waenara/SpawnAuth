package f3f5.spawnauth;

import fr.xephi.authme.api.v3.AuthMeApi;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.HashMap;
import java.util.UUID;

public class SpawnAuth extends JavaPlugin implements Listener {
    public Events events;
    public static FileConfiguration config;

    @Override
    public void onEnable() {
        events = new Events();

        getServer().getPluginManager().registerEvents(events, this);

        config = getConfig();
        config.addDefault("world-name", "world");
        config.addDefault("spawn-x", 0);
        config.addDefault("spawn-y", 1);
        config.addDefault("spawn-z", 0);
        config.options().copyDefaults(true);
        saveConfig();
    }
    @Override
    public void onDisable(){

    }
}