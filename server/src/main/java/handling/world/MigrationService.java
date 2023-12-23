package handling.world;

public interface MigrationService {

    void putMigrationEntry(ServerMigration entry);

    ServerMigration getServerMigration(int characterId, String host);
}
