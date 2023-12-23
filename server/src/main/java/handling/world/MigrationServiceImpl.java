package handling.world;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MigrationServiceImpl implements MigrationService {

    private final Map<String, ServerMigration> cache = new ConcurrentHashMap<>();

    private final ScheduledExecutorService scheduledExecutorService;

    public MigrationServiceImpl() {
        this.scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
    }

    @Override
    public void putMigrationEntry(ServerMigration entry) {
        cache.put(entry.getRemoteHost() + entry.getCharacterId(), entry);
        scheduledExecutorService.schedule(
                () -> {
                    cache.remove(entry.getRemoteHost());
                },
                1,
                TimeUnit.MINUTES);
    }

    // TODO: Check if user is trying to login with another character in the same account
    @Override
    public ServerMigration getServerMigration(int characterId, String host) {
        var serverMigration = cache.remove(host + characterId);
        if (serverMigration != null && serverMigration.getCharacterId() == characterId) {
            return serverMigration;
        }
        return null;
    }
}
