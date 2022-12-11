package handling;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MigrationServiceImpl implements MigrationService {

    private Map<String, ServerMigration> cache = new ConcurrentHashMap<>();

    private ScheduledExecutorService scheduledExecutorService;

    public MigrationServiceImpl() {
        this.scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
    }

    @Override
    public void putMigrationEntry(ServerMigration entry) {
        cache.put(entry.getRemoteHost(), entry);
        scheduledExecutorService.schedule(() -> {
            cache.remove(entry.getRemoteHost());
        }, 1, TimeUnit.MINUTES);
    }

    @Override
    public ServerMigration getServerMigration(int characterId, String host) {
        var serverMigration = cache.remove(host);
        if (serverMigration != null && serverMigration.getCharacterId() == characterId) {
            return serverMigration;
        }
        return null;
    }
}
