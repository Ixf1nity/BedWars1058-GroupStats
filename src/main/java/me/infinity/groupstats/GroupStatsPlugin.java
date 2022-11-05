package me.infinity.groupstats;

import com.andrei1058.bedwars.api.BedWars;
import lombok.Getter;
import lombok.SneakyThrows;
import me.infinity.groupstats.database.DatabaseFactory;
import me.infinity.groupstats.papi.GroupStatsExpansion;
import org.bukkit.plugin.java.JavaPlugin;;

@Getter
public final class GroupStatsPlugin extends JavaPlugin {

    @Getter
    private static GroupStatsPlugin instance;
    private BedWars bedWarsAPI;
    private DatabaseFactory databaseFactory;

    private boolean isDisabling;

    @Override
    @SneakyThrows
    public void onEnable() {
        instance = this;
        this.isDisabling = false;
        this.saveDefaultConfig();
        if (!this.getServer().getPluginManager().isPluginEnabled("BedWars1058") || !this.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            this.getLogger().severe("BedWars1058/PlaceholderAPI was not found. Disabling...");
            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }
        this.bedWarsAPI = this.getServer().getServicesManager().getRegistration(BedWars .class).getProvider();
        this.databaseFactory = new DatabaseFactory(this);

        new GroupStatsExpansion().register();
    }

    @Override
    public void onDisable() {
        this.isDisabling = true;
        this.databaseFactory.closeDatabase();
    }
}