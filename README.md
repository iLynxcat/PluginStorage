# Plugin Storage

Simple persistent storage for Bukkit-based plugins.

To create a local (SQLite) database:

```java
public class MyPlugin extends org.bukkit.plugin.java.JavaPlugin {
    private final LocalDatabase database;

    public MyPlugin() {
        this.database = LocalDatabase.create(this);  // creates "storage.db"
    }

    @Override
    public void onEnable() {
        // you can perform blocking operations, like setting up tables…
        try (var conn = this.database.getConnection();
             var stmt = conn.prepareStatement("""
                     CREATE TABLE IF NOT EXISTS …;
                     """)) {
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Table init failed", e);
        }
    }

    private void doThing() {
        // … or perform non-blocking operations using the provided executor
        CompletableFuture.runAsync(() -> {
            try (var conn = this.database.getConnection();
                 var stmt = conn.prepareStatement("""
                         INSERT INTO …;
                         """)) {
                stmt.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException("Operation thingy failed", e);
            }
        }, this.database.executor);
    }
}
```
