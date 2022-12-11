package handling;

import database.AccountData;
import handling.world.helper.CharacterTransfer;
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


    public ServerMigration(int characterId, AccountData accountData, String remoteHost) {
        this.characterId = characterId;
        this.accountData = accountData;
        this.remoteHost = remoteHost;
    }
}
