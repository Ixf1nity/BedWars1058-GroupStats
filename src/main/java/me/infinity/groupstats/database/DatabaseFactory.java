package me.infinity.groupstats.database;

import com.j256.ormlite.jdbc.DataSourceConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.Getter;
import lombok.SneakyThrows;
import me.infinity.groupstats.GroupStatsPlugin;
import me.infinity.groupstats.database.profile.GroupProfileFactory;
import org.bukkit.configuration.ConfigurationSection;

import java.io.File;
import java.util.Arrays;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Getter
public class DatabaseFactory {

    private final GroupStatsPlugin instance;

    private final HikariDataSource hikariDataSource;
    private final ConnectionSource connectionSource;
    private final GroupProfileFactory groupProfileFactory;

    private final Executor hikariExecutor = Executors.newFixedThreadPool(8);

    private String address, database, username, password;
    private int port;
    private boolean ssl, dbEnabled;

    @SneakyThrows
    public DatabaseFactory(GroupStatsPlugin instance) {
        this.instance = instance;
        this.loadCredentials();

        final HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.addDataSourceProperty("characterEncoding", "utf8");
        hikariConfig.addDataSourceProperty("useUnicode", true);
        hikariConfig.addDataSourceProperty("useSSL", this.ssl);
        hikariConfig.setMaximumPoolSize(10);
        hikariConfig.setUsername(this.username);
        hikariConfig.setPassword(this.password);
        hikariConfig.setPoolName("bedwars1058-groupstats-pool");
        hikariConfig.setConnectionTestQuery("SELECT 1;");

        if (this.dbEnabled) {
            hikariConfig.setJdbcUrl("jdbc:mysql://" + this.address + ":" + this.port + "/" + this.database);
        } else {
            File database = new File(instance.getDataFolder(), "database.db");
            if (!database.exists()) database.createNewFile();
            hikariConfig.setJdbcUrl("jdbc:sqlite:" + this.database);
            hikariConfig.setDriverClassName("org.sqlite.JDBC");
        }

        instance.getLogger().info("");
        instance.getLogger().info(hikariConfig.getPoolName() + " - Starting...");

        this.hikariDataSource = new HikariDataSource(hikariConfig);
        this.connectionSource = new DataSourceConnectionSource(hikariDataSource, hikariDataSource.getJdbcUrl());
        this.groupProfileFactory = new GroupProfileFactory(this);

        instance.getLogger().info(hikariConfig.getPoolName() + " - Start completed.");
        instance.getLogger().info("");

    }

    private void loadCredentials() {
        ConfigurationSection configuration = instance.getConfig().getConfigurationSection("DATABASE");
        this.dbEnabled = configuration.getBoolean("ENABLED");
        this.address = Arrays.stream(configuration.getString("ADDRESS").split(":")).collect(Collectors.toList()).get(0);
        this.port = Integer.parseInt(Arrays.stream(configuration.getString("ADDRESS").split(":")).collect(Collectors.toList()).get(1));
        this.database = configuration.getString("DATABASE");
        this.username = configuration.getString("USERNAME");
        this.password = configuration.getString("PASSWORD");
        this.ssl = configuration.getBoolean("SSL");
    }

    public void closeDatabase() {
        this.groupProfileFactory.saveAll();
        this.hikariDataSource.close();
    }

}
