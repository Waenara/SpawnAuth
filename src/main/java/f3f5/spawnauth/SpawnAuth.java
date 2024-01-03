package f3f5.spawnauth;

import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public class SpawnAuth extends JavaPlugin implements Listener {
    public Events events;

    @Override
    public void onEnable() {
        events = new Events();
        getServer().getPluginManager().registerEvents(events, this);
        events.helpers.loadData();
    }

    @Override
    public void onDisable(){
        events.helpers.saveData();
    }
}