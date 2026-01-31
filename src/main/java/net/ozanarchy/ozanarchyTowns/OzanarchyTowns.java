package net.ozanarchy.ozanarchyTowns;

import net.ozanarchy.ozanarchyEconomy.api.EconomyAPI;
import net.ozanarchy.ozanarchyTowns.commands.TownsCommand;
import net.ozanarchy.ozanarchyTowns.events.MemberEvents;
import net.ozanarchy.ozanarchyTowns.events.ProtectionListener;
import net.ozanarchy.ozanarchyTowns.events.TownEvents;
import net.ozanarchy.ozanarchyTowns.handlers.ChunkHandler;
import net.ozanarchy.ozanarchyTowns.handlers.DatabaseHandler;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

public final class OzanarchyTowns extends JavaPlugin {
    private Connection connection;
    public String host, database, username, password, table;
    public  int port;
    public static FileConfiguration config;
    private EconomyAPI economy;
    private final ChunkHandler chunkCache = new ChunkHandler();

    @Override
    public void onEnable() {
        //Config
        config = getConfig();
        getConfig().options().copyDefaults(true);
        saveDefaultConfig();
        //Economy Enabled?
        economy = Bukkit.getServicesManager().load(EconomyAPI.class);

        if (economy == null) {
            getLogger().severe("Economy API not found! Disabling.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        // Initialize handlers
        DatabaseHandler db = new DatabaseHandler(this);
        TownEvents claim = new TownEvents(db, this, economy);
        MemberEvents memberEvents = new MemberEvents(db, this);

        // Register commands
        getCommand("towns").setExecutor(new TownsCommand(db,claim,memberEvents));

        // Register events
        getServer().getPluginManager().registerEvents(claim, this);
        getServer().getPluginManager().registerEvents(memberEvents, this);
        getServer().getPluginManager().registerEvents(new ProtectionListener(this, db ,chunkCache), this);

        //MySQL
        setupMySql();
        createTables();

        //Reload chunk info
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, this::reloadChunkCache, 300L, 300L);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public void setupMySql(){
        host = config.getString("mysql.host");
        port = config.getInt("mysql.port");
        username = config.getString("mysql.username");
        password = config.getString("mysql.password");
        database = config.getString("mysql.database");

        try {
            synchronized (this) {
                if(getConnection() != null && !getConnection().isClosed()) return;
                Class.forName("com.mysql.cj.jdbc.Driver");
                setConnection(DriverManager.getConnection("jdbc:mysql://" + this.host + ":" + this.port + "/" + this.database + "?useSSL=false&allowPublicKeyRetrieval=true", this.username, this.password));
                getLogger().info("MYSQL Connected Successfully");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void createTables() {
        try (Statement stmt = connection.createStatement()) {
            String towns = "CREATE TABLE IF NOT EXISTS towns (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "name VARCHAR(32) UNIQUE," +
                    "mayor_uuid VARCHAR(36)" +
                    ")";
            stmt.executeUpdate(towns);
            String claims = "CREATE TABLE IF NOT EXISTS claims (" +
                    "world VARCHAR(32)," +
                    "chunkx INT," +
                    "chunkz INT," +
                    "town_id INT," +
                    "PRIMARY KEY (world, chunkx, chunkz)," +
                    "FOREIGN KEY (town_id) REFERENCES towns(id) ON DELETE CASCADE" +
                    ")";
            stmt.executeUpdate(claims);
            String members = "CREATE TABLE IF NOT EXISTS town_members (" +
                    "town_id INT NOT NULL," +
                    "uuid VARCHAR(36) NOT NULL," +
                    "role ENUM('MAYOR', 'OFFICER', 'MEMBER') NOT NULL," +
                    "PRIMARY KEY (uuid)," +
                    "INDEX (town_id)" +
                    ")";
            stmt.executeUpdate(members);
            getLogger().info("Tables checked/created successfully.");
        } catch (SQLException e) {
            getLogger().severe("Error creating tables: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    // Chunk Reloader
    private void reloadChunkCache() {
        DatabaseHandler db = new DatabaseHandler(this);
        Map<String, Integer> claims = db.loadAllClaims();
        chunkCache.setAll(claims);
    }
}
