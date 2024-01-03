package f3f5.spawnauth;

import fr.xephi.authme.api.v3.AuthMeApi;
import org.bukkit.*;
import org.bukkit.entity.Player;

import java.io.File;
import java.sql.*;
import java.util.*;

import static f3f5.spawnauth.SpawnAuth.config;


public class Helpers {
    public AuthMeApi authMeApi = AuthMeApi.getInstance();
    private static final HashMap<String, Location> playerLocations = new HashMap<>();
    public boolean isPlayerInRadius(Player player, Location center, double radius) {
        return player.getLocation().distanceSquared(center) <= (radius * radius);
    }
    public void cacheOriginalLocation(String playerName, Location loginLocation) {
        if (!playerLocations.containsKey(playerName)){
            playerLocations.put(playerName, loginLocation);
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
        String playerName = player.getName();
        if (!playerLocations.containsKey(playerName)) return;
        Location teleportDestination = playerLocations.get(playerName);

        player.teleport(teleportDestination);
        playerLocations.remove(playerName);
    }
    public HashMap<String, Location> getPlayerLocations(){
        return playerLocations;
    }


    public void saveData(File dataFolder){
        String jdbcUrl = "jdbc:sqlite:"+dataFolder+File.separator+"location.db";

        try (Connection connection = DriverManager.getConnection(jdbcUrl)) {
            String createTableSQL = "CREATE TABLE IF NOT EXISTS \"location\" (\n" +
                    "\t\"Name\"\tTEXT NOT NULL,\n" +
                    "\t\"World\"\tTEXT NOT NULL,\n" +
                    "\t\"x\"\tINTEGER NOT NULL,\n" +
                    "\t\"y\"\tINTEGER NOT NULL,\n" +
                    "\t\"z\"\tINTEGER NOT NULL\n" +
                    ")";
            try (PreparedStatement preparedStatement = connection.prepareStatement(createTableSQL)) {
                preparedStatement.executeUpdate();
            }
            Set<Map.Entry<String, Location>> entrySet = playerLocations.entrySet();
            for (Map.Entry<String, Location> entry : entrySet) {
                String name = entry.getKey();
                Location location = entry.getValue();
                String insertDataSQL = "INSERT INTO location (Name, World, x, y, z) VALUES (?, ?, ?, ?, ?)";
                try (PreparedStatement preparedStatement = connection.prepareStatement(insertDataSQL)) {
                    preparedStatement.setString(1, name);
                    preparedStatement.setString(2, location.getWorld().getName());
                    preparedStatement.setInt(3, (int)location.getX());
                    preparedStatement.setInt(4, (int)location.getY());
                    preparedStatement.setInt(5, (int)location.getZ());
                    preparedStatement.executeUpdate();
                }
            }
        } catch (SQLException ignored) {}
    }

    public void loadData(File dataFolder) {
        String url = "jdbc:sqlite:" + dataFolder + File.separator +"location.db";
        String sqlQuery = "SELECT * FROM location";
        try (Connection connection = DriverManager.getConnection(url);
             PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                String name = resultSet.getString("Name");
                String world = resultSet.getString("World");
                int x = resultSet.getInt("x");
                int y = resultSet.getInt("y");
                int z = resultSet.getInt("z");
                playerLocations.put(name, new Location(Bukkit.getWorld(world),x,y,z));
            }}
        catch (SQLException ignored) {}
        try (Connection connection = DriverManager.getConnection(url);
             Statement statement = connection.createStatement()) {
            statement.executeUpdate("DELETE FROM location");
        } catch (SQLException ignored) {}
    }
}
