package handling;

public interface MigrationService {

    void putMigrationEntry(ServerMigration entry);

    ServerMigration getServerMigration(int characterId, String host);
}
