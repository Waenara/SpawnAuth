package f3f5.spawnauth;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public class SpawnAuth extends JavaPlugin implements Listener {
    public Events events;
    public static FileConfiguration config;

    @Override
    public void onEnable() {
        config = getConfig();
        config.addDefault("world-name", "world");
        config.addDefault("spawn-x", 0);
        config.addDefault("spawn-y", 1);
        config.addDefault("spawn-z", 0);
        config.options().copyDefaults(true);
        saveConfig();

        events = new Events();
        getServer().getPluginManager().registerEvents(events, this);
    }
}