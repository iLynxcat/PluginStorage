package me.ilynxcat.jpstorage;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class JPStorage {

    private static final int THREAD_POOL_COUNT = 3;
    private static final int MAX_POOL_CONNECTIONS = 5;
    private static final long CONNECTION_TIMEOUT_MS = 5000;

    private static final String INIT_SQL = """
                PRAGMA journal_mode = WAL;
                PRAGMA synchronous = NORMAL;
                PRAGMA foreign_keys = ON;
            """;

    private final HikariDataSource dataSource;
    final ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_COUNT);

    public JPStorage(JavaPlugin plugin) {
        final File dbFile;
        var dataFolder = plugin.getDataFolder();
        if (!dataFolder.isDirectory() && !dataFolder.mkdirs())
            throw new IllegalStateException("Plugin data folder is not a directory");
        dbFile = new File(dataFolder, "storage.db");

        final HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:sqlite:" + dbFile.getAbsolutePath());
        config.setMaximumPoolSize(MAX_POOL_CONNECTIONS);
        config.setConnectionTimeout(CONNECTION_TIMEOUT_MS);
        config.setConnectionInitSql(INIT_SQL);
        this.dataSource = new HikariDataSource(config);
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public void shutdown() {
        executor.shutdown();

        try {
            if (!executor.awaitTermination(CONNECTION_TIMEOUT_MS, TimeUnit.MILLISECONDS))
                executor.shutdownNow();
        } catch (InterruptedException e) {
            executor.shutdownNow();
        }

        dataSource.close();
    }

}
