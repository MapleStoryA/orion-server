package handling;

import client.MapleCoolDownValueHolder;
import client.MapleDiseaseValueHolder;
import client.PlayerBuffValueHolder;
import database.AccountData;
import handling.world.helper.CharacterTransfer;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Getter;
import lombok.Setter;

public class ServerMigration {
    @Getter
    private final AccountData accountData;

    @Getter
    private final String remoteHost;

    @Getter
    private final int characterId;

    @Getter
    @Setter
    private CharacterTransfer characterTransfer;

    private final Map<Integer, List<PlayerBuffValueHolder>> buffs = new ConcurrentHashMap<>();
    private final Map<Integer, List<MapleCoolDownValueHolder>> coolDowns = new ConcurrentHashMap<>();
    private final Map<Integer, List<MapleDiseaseValueHolder>> diseases = new ConcurrentHashMap<>();

    public ServerMigration(int characterId, AccountData accountData, String remoteHost) {
        this.characterId = characterId;
        this.accountData = accountData;
        this.remoteHost = remoteHost;
    }

    public void addBuffsToStorage(final int chrid, final List<PlayerBuffValueHolder> toStore) {
        buffs.put(chrid, toStore);
    }

    public void addCooldownsToStorage(final int chrid, final List<MapleCoolDownValueHolder> toStore) {
        coolDowns.put(chrid, toStore);
    }

    public final void addDiseaseToStorage(final int chrid, final List<MapleDiseaseValueHolder> toStore) {
        diseases.put(chrid, toStore);
    }

    public List<PlayerBuffValueHolder> getBuffsFromStorage(final int chrid) {
        return buffs.remove(chrid);
    }

    public List<MapleCoolDownValueHolder> getCooldownsFromStorage(final int chrid) {
        return coolDowns.remove(chrid);
    }

    public final List<MapleDiseaseValueHolder> getDiseaseFromStorage(final int chrid) {
        return diseases.remove(chrid);
    }
}
