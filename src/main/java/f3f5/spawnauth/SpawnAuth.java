package f3f5.spawnauth;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public class SpawnAuth extends JavaPlugin implements Listener {
    public Events events;
    public static FileConfiguration config;

    @Override
    public void onEnable() {
        config = getConfig();saveConfig();
        events = new Events();
        getServer().getPluginManager().registerEvents(events, this);
        events.helpers.loadData(getDataFolder());
    }

    @Override
    public void onDisable(){
        events.helpers.saveData(getDataFolder());
    }
}